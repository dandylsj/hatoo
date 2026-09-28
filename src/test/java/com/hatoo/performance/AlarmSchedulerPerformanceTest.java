package com.hatoo.performance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mockingDetails;

import com.hatoo.domain.alarm.AlarmScheduler;
import com.hatoo.domain.alarm.FcmService;
import com.hatoo.domain.groups.Group;
import com.hatoo.domain.groups.GroupRepository;
import com.hatoo.domain.task.DeadLine;
import com.hatoo.domain.task.Frequency;
import com.hatoo.domain.task.Task;
import com.hatoo.domain.task.TaskAlarmTimeBackfill;
import com.hatoo.domain.task.TaskAlarmTimes;
import com.hatoo.domain.task.TaskAssignee;
import com.hatoo.domain.task.TaskAssigneeRepository;
import com.hatoo.domain.task.TaskRepository;
import com.hatoo.domain.user.User;
import com.hatoo.domain.user.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 할일 알림 스케줄러(매 분 실행되는 시작/마감 임박/마감 초과 알림 3종) 1회 실행 비용:
 * 개선 전(미완료 할일 전체 조회 + 자바에서 문자열 날짜 파싱) vs 개선 후(알림 시각 인덱스로 이번 1분만 조회).
 *
 * <p>반복 할일은 매일 새로 생성되고, 완료 안 된 할일은 계속 남는다. 개선 전 방식은 이 누적된 미완료 할일을 매 분
 * 전부 읽었으므로 서비스가 오래될수록 느려진다. 할일 수를 2천 → 2만 건으로 늘려가며 두 방식의 실행시간·쿼리 수·
 * 읽어온 엔티티 수를 비교하고, 두 방식이 정확히 같은 할일들에 알림을 보내는지도 검증한다.
 *
 * <p>Firebase/MinIO/Redis 없이 JPA 계층만 띄우는 {@code @DataJpaTest}로, 실제 MySQL의 별도 스키마
 * ({@code hatoo_perf})에서 돈다. {@code PERF_TEST=true}일 때만 실행:
 * <pre>PERF_TEST=true PERF_DB_PASSWORD=비밀번호 ./gradlew test --tests "com.hatoo.performance.*"</pre>
 */
@EnabledIfEnvironmentVariable(named = "PERF_TEST", matches = "true")
@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:mysql://${PERF_DB_HOST:localhost}:${PERF_DB_PORT:3306}/hatoo_perf"
                + "?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=Asia/Seoul&characterEncoding=UTF-8"
                + "&allowPublicKeyRetrieval=true&rewriteBatchedStatements=true",
        "spring.datasource.username=${PERF_DB_USERNAME:root}",
        "spring.datasource.password=${PERF_DB_PASSWORD:root}",
        "spring.jpa.hibernate.ddl-auto=update",
        "spring.jpa.show-sql=false",
        "spring.jpa.properties.hibernate.format_sql=false",
        "spring.jpa.properties.hibernate.generate_statistics=true",
        "spring.jpa.properties.hibernate.jdbc.batch_size=500",
        "spring.jpa.properties.hibernate.order_inserts=true",
        "logging.level.org.hibernate.engine.internal.StatisticalLoggingSessionEventListener=WARN"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({AlarmScheduler.class, TaskAlarmTimeBackfill.class})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class AlarmSchedulerPerformanceTest {

    /** 측정 기준 시각. 배경 할일은 분(分)을 0/15/45로만 만들어서 이 시각(17분)에 우연히 걸리지 않게 한다. */
    private static final LocalDateTime NOW = LocalDateTime.of(2027, 1, 4, 9, 17);
    private static final int[] BACKGROUND_MINUTES = {0, 15, 45};
    private static final int RUNS = 10;

    @MockitoBean
    private FcmService fcmService;

    @Autowired
    private AlarmScheduler alarmScheduler;
    @Autowired
    private TaskAlarmTimeBackfill backfill;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private TaskAssigneeRepository taskAssigneeRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private EntityManagerFactory entityManagerFactory;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    void 알림_스케줄러_1회_실행비용_비교() {
        resetData();
        List<User> users = createUsers(30);
        List<Group> groups = createGroups(10, users);
        Set<UUID> expectedTargets = createAlarmTargets(users, groups);

        System.out.printf("%n=== 알림 스케줄러 1회(시작+마감임박+마감초과) 실행 비용, %d회 평균 ===%n", RUNS);
        System.out.printf("%10s | %-24s | %8s %8s %10s%n", "미완료 할일", "방식", "시간(ms)", "쿼리 수", "읽은 엔티티");

        int seeded = 0;
        Result before = null;
        Result after = null;
        for (int total : new int[]{2_000, 20_000}) {
            createBackgroundTasks(users, groups, seeded, total - seeded);
            seeded = total;

            before = measure(this::runLegacyTick);
            after = measure(this::runNewTick);

            assertThat(before.notifiedTasks()).isEqualTo(expectedTargets);
            assertThat(after.notifiedTasks()).isEqualTo(expectedTargets);

            System.out.printf("%,10d | %-24s | %8.1f %8d %10d%n", total, "개선 전 (전체 조회+파싱)",
                    before.millis(), before.queries(), before.entitiesLoaded());
            System.out.printf("%,10d | %-24s | %8.1f %8d %10d%n", total, "개선 후 (알림 시각 인덱스)",
                    after.millis(), after.queries(), after.entitiesLoaded());
        }
        System.out.printf("2만 건 기준: %.1fms → %.1fms (%.1f%% 단축, %.0f배), 읽은 엔티티 %,d개 → %,d개%n",
                before.millis(), after.millis(), (1 - after.millis() / before.millis()) * 100,
                before.millis() / after.millis(), before.entitiesLoaded(), after.entitiesLoaded());
        System.out.printf("스케줄러는 이 작업을 매 분 실행: 하루 %,.0f초 → %,.1f초의 DB·CPU 사용%n",
                before.millis() * 1440 / 1000, after.millis() * 1440 / 1000);

        assertThat(after.millis()).isLessThan(before.millis());
    }

    @Test
    void 기존_할일의_알림시각을_기동시_백필한다() {
        resetData();
        User user = userRepository.save(User.builder().email("backfill@hatoo.test").nickname("b").build());
        Task task = new Task("백필", null, Frequency.NONE, "2027-01-04 09:17:00", "2027-01-04T01:30:00.000Z", DeadLine.MIN_30, true, 1);
        taskRepository.save(task);
        taskAssigneeRepository.save(new TaskAssignee(task, user));
        // 컬럼 추가 전 데이터 재현
        jdbcTemplate.update("UPDATE tasks SET start_alarm_at = NULL, deadline_alarm_at = NULL, overdue_alarm_at = NULL");

        backfill.run(null);

        Task reloaded = taskRepository.findById(task.getId()).orElseThrow();
        assertThat(reloaded.getStartAlarmAt()).isEqualTo(LocalDateTime.of(2027, 1, 4, 9, 17));
        assertThat(reloaded.getDeadlineAlarmAt()).isEqualTo(LocalDateTime.of(2027, 1, 4, 10, 0));   // 01:30Z = 10:30 KST - 30분
        assertThat(reloaded.getOverdueAlarmAt()).isEqualTo(LocalDateTime.of(2027, 1, 4, 12, 30));  // 10:30 KST + 2시간
    }

    // ──────────────────────────────────────────
    // 측정
    // ──────────────────────────────────────────

    private record Result(double millis, long queries, long entitiesLoaded, Set<UUID> notifiedTasks) {
    }

    /** 스케줄러 1틱을 RUNS번 실행한 평균. 알림 발송 플래그가 남지 않게 매번 롤백한다. */
    private Result measure(Function<LocalDateTime, Set<UUID>> tick) {
        Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        TransactionTemplate rollbackTx = new TransactionTemplate(transactionManager);
        rollbackTx.execute(status -> { status.setRollbackOnly(); return tick.apply(NOW); }); // warmup

        long totalNanos = 0;
        long queries = 0;
        long entities = 0;
        Set<UUID> notified = Set.of();
        for (int i = 0; i < RUNS; i++) {
            clearInvocations(fcmService);
            statistics.clear();
            long start = System.nanoTime();
            notified = rollbackTx.execute(status -> { status.setRollbackOnly(); return tick.apply(NOW); });
            totalNanos += System.nanoTime() - start;
            queries = statistics.getPrepareStatementCount();
            entities = statistics.getEntityLoadCount();
        }
        return new Result(totalNanos / 1_000_000.0 / RUNS, queries, entities, notified);
    }

    /** 개선 후: 실제 스케줄러 메서드. 알림을 보낸 할일 id를 FcmService mock 호출 인자로 모은다. */
    private Set<UUID> runNewTick(LocalDateTime nowMinute) {
        alarmScheduler.sendTaskStartAlarm(nowMinute);
        alarmScheduler.sendTaskDeadlineAlarm(nowMinute);
        alarmScheduler.sendTaskOverdueAlarm(nowMinute);
        return notifiedTaskIds();
    }

    /**
     * 개선 전 스케줄러 로직을 그대로 재현: 조건에 맞는 미완료 할일을 "전부" 불러와서 문자열 날짜를 파싱하고
     * 현재 분과 같은 것만 발송했다(파싱 로직은 그대로 TaskAlarmTimes.parse로 옮김).
     */
    private Set<UUID> runLegacyTick(LocalDateTime nowMinute) {
        List<Task> startCandidates = entityManager.createQuery(
                "SELECT t FROM Task t WHERE t.starter = true AND t.finished = false AND t.startAlarmSent = false", Task.class)
                .getResultList();
        startCandidates.forEach(task -> {
            LocalDateTime at = TaskAlarmTimes.parse(task.getDueFrom());
            if (at != null && at.truncatedTo(ChronoUnit.MINUTES).equals(nowMinute)) legacyNotify(task, fcmService::sendTaskStart);
        });

        List<Task> deadlineCandidates = entityManager.createQuery(
                "SELECT t FROM Task t WHERE t.deadLine IS NOT NULL AND t.deadLine != com.hatoo.domain.task.DeadLine.NONE "
                        + "AND t.finished = false AND t.deadlineAlarmSent = false", Task.class)
                .getResultList();
        deadlineCandidates.forEach(task -> {
            LocalDateTime dueTo = TaskAlarmTimes.parse(task.getDueTo());
            Duration duration = legacyDuration(task.getDeadLine());
            if (dueTo != null && duration != null
                    && dueTo.minus(duration).truncatedTo(ChronoUnit.MINUTES).equals(nowMinute)) {
                legacyNotify(task, fcmService::sendTaskDeadline);
            }
        });

        List<Task> overdueCandidates = entityManager.createQuery(
                "SELECT t FROM Task t WHERE t.finished = false AND t.overdueAlarmSent = false", Task.class)
                .getResultList();
        overdueCandidates.forEach(task -> {
            LocalDateTime dueTo = TaskAlarmTimes.parse(task.getDueTo());
            if (dueTo != null && dueTo.plusHours(2).truncatedTo(ChronoUnit.MINUTES).equals(nowMinute)) {
                legacyNotify(task, fcmService::sendTaskOverdue);
            }
        });
        return notifiedTaskIds();
    }

    private interface Sender {
        void send(UUID userId, String taskTitle, UUID taskId, UUID groupId, String groupName);
    }

    private void legacyNotify(Task task, Sender sender) {
        if (task.getAssignees().isEmpty()) return;
        Group group = task.getGroups().isEmpty() ? null : task.getGroups().get(0);
        task.getAssignees().forEach(user -> sender.send(user.getId(), task.getTitle(), task.getId(),
                group != null ? group.getId() : null, group != null ? group.getName() : null));
    }

    private static Duration legacyDuration(DeadLine deadLine) {
        if (deadLine == null) return null;
        return switch (deadLine) {
            case MIN_10 -> Duration.ofMinutes(10);
            case MIN_30 -> Duration.ofMinutes(30);
            case HOUR_1 -> Duration.ofHours(1);
            case DAY_1 -> Duration.ofDays(1);
            case WEEK_1 -> Duration.ofDays(7);
            case NONE -> null;
        };
    }

    private Set<UUID> notifiedTaskIds() {
        Set<UUID> ids = new HashSet<>();
        mockingDetails(fcmService).getInvocations().forEach(invocation -> ids.add(invocation.getArgument(2)));
        return ids;
    }

    // ──────────────────────────────────────────
    // 데이터
    // ──────────────────────────────────────────

    private void resetData() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
        for (String table : List.of("task_assignees", "group_tasks", "tasks", "group_members", "`groups`", "users")) {
            jdbcTemplate.execute("TRUNCATE TABLE " + table);
        }
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
    }

    private List<User> createUsers(int count) {
        List<User> users = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            users.add(User.builder().email("perf" + i + "@hatoo.test").nickname("user" + i).build());
        }
        return userRepository.saveAll(users);
    }

    private List<Group> createGroups(int count, List<User> users) {
        List<Group> groups = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            groups.add(new Group("그룹" + i, null, users.get(i).getId()));
        }
        return groupRepository.saveAll(groups);
    }

    /** 지금(NOW) 알림이 나가야 하는 할일 - 시작 3, 마감 임박 3, 마감 초과 3. 날짜 형식도 섞는다. */
    private Set<UUID> createAlarmTargets(List<User> users, List<Group> groups) {
        List<Task> targets = List.of(
                new Task("시작1", null, Frequency.NONE, "2027-01-04 09:17:00", "2027-01-04 18:00:00", DeadLine.NONE, true, 1),
                new Task("시작2", null, Frequency.NONE, "2027-01-04T00:17:30.000Z", null, null, true, 1),
                new Task("시작3", null, Frequency.NONE, "2027-01-04T09:17:59", null, null, true, 1),
                new Task("임박1", null, Frequency.NONE, null, "2027-01-04 09:27:00", DeadLine.MIN_10, false, 1),
                new Task("임박2", null, Frequency.NONE, null, "2027-01-04T01:17:00.000Z", DeadLine.HOUR_1, false, 1),
                new Task("임박3", null, Frequency.NONE, null, "2027-01-05 09:17", DeadLine.DAY_1, false, 1),
                new Task("초과1", null, Frequency.NONE, null, "2027-01-04 07:17:00", DeadLine.NONE, false, 1),
                new Task("초과2", null, Frequency.NONE, null, "2027-01-03T22:17:10.000Z", DeadLine.NONE, false, 1),
                new Task("초과3", null, Frequency.NONE, null, "2027-01-04T07:17:00", null, false, 1)
        );
        saveWithAssignees(targets, users, groups, new Random(7));
        Set<UUID> ids = new HashSet<>();
        targets.forEach(task -> ids.add(task.getId()));
        return ids;
    }

    /**
     * 알림 시각이 지금이 아닌 미완료 할일 - 1년간 쌓인 반복 할일(날짜만), 앱에서 만든 할일(UTC ISO), 공백 구분 형식을
     * 섞고, 과거(쌓인 것)와 미래(예정된 것)에 고루 퍼뜨린다.
     */
    private void createBackgroundTasks(List<User> users, List<Group> groups, int from, int count) {
        Random random = new Random(42L + from);
        DeadLine[] deadLines = DeadLine.values();
        DateTimeFormatter spaced = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter dateOnly = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (int done = 0; done < count; done += 1_000) {
            List<Task> tasks = new ArrayList<>();
            for (int i = 0; i < Math.min(1_000, count - done); i++) {
                LocalDateTime dueTo = NOW.minusDays(365).plusDays(random.nextInt(395))
                        .withHour(random.nextInt(24)).withMinute(BACKGROUND_MINUTES[random.nextInt(3)]);
                if (dueTo.toLocalDate().equals(NOW.toLocalDate()) || dueTo.toLocalDate().equals(NOW.toLocalDate().plusDays(1))) {
                    dueTo = dueTo.plusDays(3); // 날짜만 형식(자정) + 하루/일주일 전 알림이 우연히 겹치지 않게
                }
                LocalDateTime dueFrom = dueTo.minusHours(1 + random.nextInt(48));
                Function<LocalDateTime, String> format = switch (random.nextInt(10)) {
                    case 0, 1, 2, 3, 4 -> t -> t.format(dateOnly);                                            // 반복 할일
                    case 5, 6, 7 -> t -> t.minusHours(9).atOffset(ZoneOffset.UTC).toInstant().toString(); // 앱 (UTC)
                    default -> t -> t.format(spaced);
                };
                tasks.add(new Task("할일" + (from + done + i), null, Frequency.DAILY, format.apply(dueFrom), format.apply(dueTo),
                        deadLines[random.nextInt(deadLines.length)], random.nextBoolean(), 1));
            }
            saveWithAssignees(tasks, users, groups, random);
        }
    }

    private void saveWithAssignees(List<Task> tasks, List<User> users, List<Group> groups, Random random) {
        tasks.forEach(task -> task.addGroup(groups.get(random.nextInt(groups.size()))));
        taskRepository.saveAll(tasks);
        List<TaskAssignee> assignees = new ArrayList<>();
        for (Task task : tasks) {
            int first = random.nextInt(users.size());
            assignees.add(new TaskAssignee(task, users.get(first)));
            if (random.nextBoolean()) {
                assignees.add(new TaskAssignee(task, users.get((first + 1) % users.size())));
            }
        }
        taskAssigneeRepository.saveAll(assignees);
    }
}
