package com.hatoo.domain.task;

import com.hatoo.domain.groupMember.GroupMember;
import com.hatoo.domain.groups.Group;
import com.hatoo.domain.user.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ============================================================
 *  N+1 쿼리 문제 — 개선 전 / 후 성능 비교 테스트
 * ============================================================
 *
 * [문제 상황]
 *   그룹에 속한 할일 목록을 조회할 때, 각 할일의 담당자(assignees)가
 *   LAZY 로딩으로 설정되어 있어 할일 하나당 1번의 SELECT 쿼리가 추가로
 *   발생합니다.
 *
 *   예) 할일 10개 조회 시
 *     - 할일 목록 조회:     1 query
 *     - 담당자 조회 (×10): 10 queries
 *     - 합계:             11 queries  ← N+1 문제
 *
 * [해결 방법]
 *   JOIN FETCH를 사용하여 할일과 담당자를 하나의 쿼리로 함께 조회합니다.
 *
 *     SELECT DISTINCT t FROM Task t
 *     JOIN FETCH t.assignees
 *     JOIN t.groups g
 *     WHERE g.id = :groupId
 *
 *   예) 할일 10개 조회 시
 *     - 할일 + 담당자 한 번에 조회: 1 query  ← 해결!
 *
 * [테스트 데이터]
 *   - 그룹: 1개
 *   - 유저: 3명 (user1, user2, user3)
 *   - 할일: 10개, 각 할일에 담당자 2명씩 배정
 *
 * ============================================================
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(locations = "classpath:application-test.properties")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("N+1 쿼리 성능 테스트: 개선 전 vs 개선 후")
class N1QueryPerformanceTest {

    private static final int TASK_COUNT = 10; // 테스트 할일 수

    @Autowired
    private TestEntityManager testEntityManager;

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private TaskRepository taskRepository;

    private UUID testGroupId;

    // ──────────────────────────────────────────
    // 공통 테스트 데이터 세팅
    // ──────────────────────────────────────────

    @BeforeEach
    void setUp() {
        // 유저 3명 생성
        User user1 = User.builder()
                .loginId("user1@test.com")
                .email("user1@test.com")
                .nickname("유저1")
                .password("password")
                .isTermsAgreed(true)
                .isPrivacyAgreed(true)
                .isOverFourteen(true)
                .build();

        User user2 = User.builder()
                .loginId("user2@test.com")
                .email("user2@test.com")
                .nickname("유저2")
                .password("password")
                .isTermsAgreed(true)
                .isPrivacyAgreed(true)
                .isOverFourteen(true)
                .build();

        User user3 = User.builder()
                .loginId("user3@test.com")
                .email("user3@test.com")
                .nickname("유저3")
                .password("password")
                .isTermsAgreed(true)
                .isPrivacyAgreed(true)
                .isOverFourteen(true)
                .build();

        testEntityManager.persist(user1);
        testEntityManager.persist(user2);
        testEntityManager.persist(user3);

        // 그룹 1개 생성
        Group group = new Group("테스트 그룹", "N+1 테스트용", user1.getId(), false);
        testEntityManager.persist(group);
        testGroupId = group.getId();

        // 그룹 멤버 등록
        testEntityManager.persist(new GroupMember(user1, group, null, false));
        testEntityManager.persist(new GroupMember(user2, group, null, false));
        testEntityManager.persist(new GroupMember(user3, group, null, false));

        // 할일 10개 생성 (각 할일에 담당자 2명)
        for (int i = 1; i <= TASK_COUNT; i++) {
            Task task = new Task(
                    "할일 " + i,
                    "설명 " + i,
                    Frequency.NONE,
                    "2026-05-01T09:00:00",
                    "2026-05-10T18:00:00",
                    DeadLine.NONE,
                    false,
                    null
            );
            task.addAssignee(user1);
            task.addAssignee(user2);  // 할일 1개에 담당자 2명
            task.addGroup(group);
            testEntityManager.persist(task);
        }

        // 영속성 컨텍스트 초기화 (캐시 효과 제거)
        testEntityManager.flush();
        testEntityManager.clear();
    }

    // ──────────────────────────────────────────
    // Hibernate Statistics 유틸
    // ──────────────────────────────────────────

    private Statistics getStatistics() {
        SessionFactory sf = em.getEntityManagerFactory().unwrap(SessionFactory.class);
        Statistics stats = sf.getStatistics();
        stats.setStatisticsEnabled(true);
        return stats;
    }

    // ──────────────────────────────────────────
    // 테스트 1: 개선 전 — N+1 문제 발생 확인
    // ──────────────────────────────────────────

    @Test
    @Order(1)
    @DisplayName("[개선 전] LAZY 로딩으로 N+1 쿼리 발생")
    void before_N1_문제_발생() {
        // given
        Statistics stats = getStatistics();
        stats.clear();

        // when
        long startTime = System.currentTimeMillis();

        /*
         * ⚠ N+1 문제 발생 지점
         *
         * findByGroupsIdOrderByDueToAsc()는 tasks 목록만 조회 (1 query).
         * 이후 반복문에서 task.getAssignees()를 호출할 때마다
         * 각 task의 assignees를 가져오는 SELECT 쿼리가 추가 발생 (N queries).
         *
         * SQL 실행 순서:
         *   1. SELECT t.* FROM tasks t JOIN group_tasks gt ON ...  -- 1번
         *   2. SELECT u.* FROM task_assignees ta JOIN users u ON ta.user_id = u.id WHERE ta.task_id = ?  -- task 1번
         *   3. SELECT u.* FROM task_assignees ta JOIN users u ON ta.user_id = u.id WHERE ta.task_id = ?  -- task 2번
         *   ...
         *   11. SELECT u.* FROM task_assignees ta JOIN users u ON ta.user_id = u.id WHERE ta.task_id = ?  -- task 10번
         */
        List<Task> tasks = taskRepository.findByGroupsIdOrderByDueToAsc(testGroupId);

        // 담당자 접근 → LAZY 로딩 발동
        int totalAssignees = 0;
        for (Task task : tasks) {
            totalAssignees += task.getAssignees().size();  // ⚠ 여기서 매번 SELECT 발생!
        }

        long elapsed = System.currentTimeMillis() - startTime;
// ↓ 이 두 줄 교체
        long hqlQueryCount = stats.getQueryExecutionCount();      // JPQL 쿼리 수: 1
        long lazyFetchCount = stats.getCollectionFetchCount();    // LAZY 로딩 수: N (10)
        long queryCount = hqlQueryCount + lazyFetchCount;         // 합계: N+1 (11)

// then
        System.out.println("\n" + "=".repeat(60));
        System.out.println("【개선 전】 N+1 문제 발생 결과");
        System.out.println("=".repeat(60));
        System.out.printf("  할일 수:           %d개%n", tasks.size());
        System.out.printf("  총 담당자 수:       %d명%n", totalAssignees);
        System.out.printf("  JPQL 쿼리:         %d개  ← findByGroupsIdOrderByDueToAsc%n", hqlQueryCount);
        System.out.printf("  LAZY 로딩 쿼리:    %d개  ← task.getAssignees() 호출마다 발생%n", lazyFetchCount);
        System.out.printf("  총 실행 쿼리:      %d개  (N+1 문제)%n", queryCount);
        System.out.printf("  소요 시간:         %dms%n", elapsed);
        System.out.println("=".repeat(60) + "\n");

        assertThat(tasks).hasSize(TASK_COUNT);
        assertThat(totalAssignees).isEqualTo(TASK_COUNT * 2);
        assertThat(hqlQueryCount).isEqualTo(1);
        assertThat(lazyFetchCount).isEqualTo(TASK_COUNT);   // N+1 핵심 검증
        assertThat(queryCount).isEqualTo(1 + TASK_COUNT);   // 11
    }

    // ──────────────────────────────────────────
    // 테스트 2: 개선 후 — JOIN FETCH로 1 쿼리
    // ──────────────────────────────────────────

    @Test
    @Order(2)
    @DisplayName("[개선 후] JOIN FETCH로 1개 쿼리에 모두 조회")
    void after_JOIN_FETCH_최적화() {
        // given
        Statistics stats = getStatistics();
        stats.clear();

        // when
        long startTime = System.currentTimeMillis();

        /*
         * ✅ N+1 해결: JOIN FETCH
         *
         * JOIN FETCH t.assignees를 사용하면 tasks와 assignees를
         * 하나의 쿼리로 함께 조회합니다.
         *
         * 실행되는 SQL (단 1번):
         *   SELECT DISTINCT t.*, u.*
         *   FROM tasks t
         *   INNER JOIN task_assignees ta ON t.id = ta.task_id
         *   INNER JOIN users u ON ta.user_id = u.id
         *   INNER JOIN group_tasks gt ON t.id = gt.task_id
         *   WHERE gt.group_id = ?
         *   ORDER BY CASE WHEN t.due_to IS NULL THEN 1 ELSE 0 END, t.due_to
         */
        List<Task> tasks = taskRepository.findByGroupsIdWithAssigneesOrderByDueToAsc(testGroupId);

        // 담당자 접근 → 이미 로딩됨, 추가 쿼리 없음
        int totalAssignees = 0;
        for (Task task : tasks) {
            totalAssignees += task.getAssignees().size();  // ✅ 추가 쿼리 없음!
        }

        long elapsed = System.currentTimeMillis() - startTime;
// ↓ 교체
        long hqlQueryCount = stats.getQueryExecutionCount();      // JPQL 쿼리 수: 1
        long lazyFetchCount = stats.getCollectionFetchCount();    // LAZY 로딩 수: 0 (이미 로딩됨)
        long queryCount = hqlQueryCount + lazyFetchCount;         // 합계: 1

        System.out.println("\n" + "=".repeat(60));
        System.out.println("【개선 후】 JOIN FETCH 최적화 결과");
        System.out.println("=".repeat(60));
        System.out.printf("  할일 수:           %d개%n", tasks.size());
        System.out.printf("  총 담당자 수:       %d명%n", totalAssignees);
        System.out.printf("  JPQL 쿼리:         %d개  ← assignees까지 한 번에 조회%n", hqlQueryCount);
        System.out.printf("  LAZY 로딩 쿼리:    %d개  ← 추가 쿼리 없음!%n", lazyFetchCount);
        System.out.printf("  총 실행 쿼리:      %d개%n", queryCount);
        System.out.printf("  소요 시간:         %dms%n", elapsed);
        System.out.println("=".repeat(60) + "\n");

        assertThat(tasks).hasSize(TASK_COUNT);
        assertThat(totalAssignees).isEqualTo(TASK_COUNT * 2);
        assertThat(lazyFetchCount).isEqualTo(0);    // N+1 해결 검증
        assertThat(queryCount).isEqualTo(1);
    }

    // ──────────────────────────────────────────
    // 테스트 3: 개선 효과 수치 비교 (종합 리포트)
    // ──────────────────────────────────────────

    @Test
    @Order(3)
    @DisplayName("[성능 비교] N+1 개선 전 vs 후 — 쿼리 수 비교")
    void 개선_전후_성능_비교_리포트() {
        Statistics stats = getStatistics();

        // ── Before: N+1 측정 ──────────────────────
        stats.clear();
        testEntityManager.clear();

        long beforeStart = System.nanoTime();
        List<Task> beforeTasks = taskRepository.findByGroupsIdOrderByDueToAsc(testGroupId);
        beforeTasks.forEach(t -> t.getAssignees().size());
        long beforeElapsed = System.nanoTime() - beforeStart;

        // ↓ 여기 수정 (기존: stats.getQueryExecutionCount())
        long beforeQueryCount = stats.getQueryExecutionCount() + stats.getCollectionFetchCount();

        // ── After: JOIN FETCH 측정 ─────────────────
        stats.clear();
        testEntityManager.clear();

        long afterStart = System.nanoTime();
        List<Task> afterTasks = taskRepository.findByGroupsIdWithAssigneesOrderByDueToAsc(testGroupId);
        afterTasks.forEach(t -> t.getAssignees().size());
        long afterElapsed = System.nanoTime() - afterStart;

        // ↓ 여기 수정 (기존: stats.getQueryExecutionCount())
        long afterQueryCount = stats.getQueryExecutionCount() + stats.getCollectionFetchCount();

        // ── 종합 리포트 출력 ───────────────────────
        System.out.println("\n" + "█".repeat(60));
        System.out.println("  📊 N+1 쿼리 성능 개선 비교 리포트");
        System.out.println("█".repeat(60));
        System.out.printf("  테스트 환경:  할일 %d개, 담당자 2명/할일%n", TASK_COUNT);
        System.out.println("─".repeat(60));
        System.out.printf("  %-20s  %-15s  %-15s%n", "구분", "개선 전 (N+1)", "개선 후 (JOIN FETCH)");
        System.out.println("─".repeat(60));
        System.out.printf("  %-20s  %-15s  %-15s%n",
                "실행 쿼리 수",
                beforeQueryCount + "개",
                afterQueryCount + "개");
        System.out.printf("  %-20s  %-15s  %-15s%n",
                "소요 시간",
                String.format("%.2fms", beforeElapsed / 1_000_000.0),
                String.format("%.2fms", afterElapsed / 1_000_000.0));
        System.out.printf("  %-20s  %-15s  %-15s%n",
                "쿼리 감소율",
                "-",
                String.format("%.0f%%", (1.0 - (double) afterQueryCount / beforeQueryCount) * 100));
        System.out.println("─".repeat(60));
        System.out.printf("  → 쿼리 %d개 → %d개로 %d개 감소 (약 %.0f%% 개선)%n",
                beforeQueryCount,
                afterQueryCount,
                beforeQueryCount - afterQueryCount,
                (1.0 - (double) afterQueryCount / beforeQueryCount) * 100);
        System.out.println("█".repeat(60) + "\n");

        // ↓ 검증도 수정
        assertThat(beforeQueryCount).isEqualTo(1 + TASK_COUNT);  // 11
        assertThat(afterQueryCount).isEqualTo(1);
        assertThat(afterQueryCount).isLessThan(beforeQueryCount);
        assertThat(afterTasks).hasSize(beforeTasks.size());
    }
}
