package com.hatoo.domain.user;

import com.hatoo.common.email.SmtpEmailSender;
import com.hatoo.common.exception.CustomException;
import com.hatoo.common.exception.ErrorMessage;
import com.hatoo.common.util.JwtUtil;
import com.hatoo.domain.alarmUserAgree.AlarmUserAgree;
import com.hatoo.domain.alarmUserAgree.AlarmUserAgreeRepository;
import com.hatoo.domain.auth.EmailRepository;
import com.hatoo.domain.auth.EmailVerification;
import com.hatoo.domain.groupAlarmSetting.GroupAlarmSetting;
import com.hatoo.domain.groupAlarmSetting.GroupAlarmSettingRepository;
import com.hatoo.domain.groupMember.GroupMember;
import com.hatoo.domain.groupMember.GroupMemberRepository;
import com.hatoo.domain.groups.Group;
import com.hatoo.domain.groups.GroupRepository;
import com.hatoo.domain.task.Task;
import com.hatoo.domain.task.TaskRepository;
import com.hatoo.domain.oAuth.KakaoService;
import com.hatoo.domain.oAuth.NaverService;
import com.hatoo.domain.user.dto.*;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupRepository groupRepository;
    private final TaskRepository taskRepository;
    private final AlarmUserAgreeRepository alarmUserAgreeRepository;
    private final GroupAlarmSettingRepository groupAlarmSettingRepository;
    private final SmtpEmailSender smtpEmailSender;
    private final EmailRepository emailRepository;
    private final KakaoService kakaoService;
    private final NaverService naverService;

    private static final int MAX_SEND_COUNT = 3;
    private static final int COOLDOWN_SECONDS = 10;
    private static final int COUNT_RESET_MINUTES = 5;

    // 필수 동의 저장 (소셜 로그인 후 1회 호출)
    @Transactional
    public Boolean saveUserAgree(String accessToken, UserAgreeRequest request) {
        jwtUtil.validateToken(accessToken);
        String loginId = jwtUtil.extractLoginId(accessToken);

        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new CustomException(ErrorMessage.USER_NOT_FOUND));

        // 필수 동의 3개 → User 엔티티 업데이트
        user.updateAgree(request.getIsTermsAgreed(), request.getIsPrivacyAgreed(), request.getIsOverFourteen());

        return true;
    }

    // 전체 알림 설정 조회
    @Transactional(readOnly = true)
    public AlarmSettingResponse getAlarmSetting(String accessToken) {
        jwtUtil.validateToken(accessToken);
        String loginId = jwtUtil.extractLoginId(accessToken);

        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new CustomException(ErrorMessage.USER_NOT_FOUND));

        AlarmUserAgree agree = alarmUserAgreeRepository.findByUserId(user.getId()).orElse(null);

        // 신규 컬럼은 기존 레코드에 NULL이 있을 수 있으므로 null 안전 처리 (기본값 true)
        Boolean isAllNotiEnabled = getOrDefault(agree != null ? agree.getIsAllNotiEnabled() : null, true);
        Boolean isMarketingNotiAllowed = getOrDefault(agree != null ? agree.getIsMarketingNotiAllowed() : null, false);
        Boolean isPersonalNotiEnabled = getOrDefault(agree != null ? agree.getIsPersonalNotiEnabled() : null, true);
        Boolean isGroupNotiAllGlobal = getOrDefault(agree != null ? agree.getIsGroupNotiAllGlobalEnabled() : null, true);

//        // 내가 속한 그룹 목록 조회
//        List<GroupMember> myGroups = groupMemberRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
//
//        List<AlarmSettingResponse.GroupAlarmDto> groupSettings = myGroups.stream()
//                .map(gm -> {
//                    GroupAlarmSetting setting = groupAlarmSettingRepository
//                            .findByUserIdAndGroupId(user.getId(), gm.getGroup().getId())
//                            .orElse(null);
//                    return new AlarmSettingResponse.GroupAlarmDto(
//                            gm.getGroup().getId(),
//                            gm.getGroup().getName(),
//                            setting != null ? setting.getIsGroupNotiEnabled()        : true,
//                            setting != null ? setting.getIsNewTaskNotiEnabled()      : true,
//                            setting != null ? setting.getIsNewMemberNotiEnabled()    : true,
//                            setting != null ? setting.getIsTaskCompleteNotiEnabled() : true
//                    );
//                })
//                .collect(Collectors.toList());

        return new AlarmSettingResponse(
                isAllNotiEnabled,
                isMarketingNotiAllowed,
                isPersonalNotiEnabled,
                isGroupNotiAllGlobal
//                groupSettings
        );
    }

    // 알림 설정 수정 (설정에서 언제든 변경 가능)
    @Transactional
    public AlarmSettingResponse saveAlarmAgree(String accessToken, AlarmAgreeRequest request) {
        jwtUtil.validateToken(accessToken);
        String loginId = jwtUtil.extractLoginId(accessToken);

        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new CustomException(ErrorMessage.USER_NOT_FOUND));

        AlarmUserAgree alarmAgree = alarmUserAgreeRepository.findByUserId(user.getId())
                .orElse(null);

        if (alarmAgree == null) {
            alarmAgree = new AlarmUserAgree(true, request.getIsMarketingNotiAllowed() != null
                    ? request.getIsMarketingNotiAllowed() : false, user);
            alarmUserAgreeRepository.save(alarmAgree);
        }

        alarmAgree.update(
                request.getIsAllNotiEnabled(),
                request.getIsMarketingNotiAllowed(),
                request.getIsPersonalNotiEnabled(),
                request.getIsGroupNotiAllGlobalEnabled()
        );

        return getAlarmSetting(accessToken);
    }

    // FCM 토큰 갱신 (앱 실행 시 호출)
    @Transactional
    public Boolean updateFcmToken(String accessToken, String fcmToken) {
        jwtUtil.validateToken(accessToken);
        String loginId = jwtUtil.extractLoginId(accessToken);

        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new CustomException(ErrorMessage.USER_NOT_FOUND));

        user.updateInfo(null, null, null, fcmToken);
        return true;
    }

    //아이디 중복 확인
    @Transactional(readOnly = true)
    public boolean checkLoginIdApi(String loginId) {
        try {
            return !userRepository.existsByLoginId(loginId);
        } catch (Exception e) {
            return false;
        }
    }

    //닉네임 중복 확인
    @Transactional
    public boolean checkNicknameApi(String nickname) {
        try {
            return !userRepository.existsByNickname(nickname);
        } catch (Exception e) {
            return false;
        }
    }

    //유저 정보 수정.
    @Transactional
    public UserInfoModifyResponse userInfoModifyResponse(String accessToken, UserInfoModifyRequest request) {
        // 1. 토큰 검증
        jwtUtil.validateToken(accessToken);

        // 2. 토큰에서 로그인 아이디 추출
        String loginId = jwtUtil.extractLoginId(accessToken);

        // 3. 유저 조회
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new CustomException(ErrorMessage.USER_NOT_FOUND));

        // 4. 비밀번호 암호화 (수정 요청에 비밀번호가 있는 경우)
        String encodedPassword = null;
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            encodedPassword = passwordEncoder.encode(request.getPassword());
        }

        // 5. 유저 정보 수정
        user.updateInfo(
                request.getNickname(),
                encodedPassword,
                request.getProfileImg(),
                request.getFcmToken()
        );

        // 6. 닉네임이 변경된 경우 기본그룹(isPersonal=true) 이름도 동기화
        if (request.getNickname() != null && !request.getNickname().isBlank()) {
            groupRepository.findByAssignerIdAndIsPersonalTrue(user.getId())
                    .ifPresent(personalGroup -> personalGroup.updateName(request.getNickname()));
        }

        // 7. 프로필 이미지가 변경된 경우, profileImg가 null인 GroupMember에만 반영
        //    (이미 그룹에서 별도 프로필을 선택한 경우 덮어쓰지 않음)
        if (request.getProfileImg() != null) {
            List<GroupMember> groupMembers = groupMemberRepository.findByUserId(user.getId());
            for (GroupMember gm : groupMembers) {
                if (gm.getProfileImg() == null) {
                    gm.updateProfileImg(request.getProfileImg());
                }
            }
        }

        // 8. 응답 DTO 생성 반환
        return new UserInfoModifyResponse(user.getId(), user.getNickname(), user.getProfileImg());
    }

    //이전 비밀번호 확인
    @Transactional(readOnly = true)
    public boolean prePasswordVerification(String accessToken, PasswordCheckRequest request) {
        try {
            // 1. 토큰 검증
            jwtUtil.validateToken(accessToken);

            // 2. 토큰에서 로그인 아이디 추출
            String loginId = jwtUtil.extractLoginId(accessToken);

            // 3. 유저 조회
            User user = userRepository.findByLoginId(loginId)
                    .orElseThrow(() -> new CustomException(ErrorMessage.USER_NOT_FOUND));

            // 4. 비밀번호 일치 여부 확인.
            return passwordEncoder.matches(request.getPassword(), user.getPassword());
        } catch (Exception e) {
            return false;
        }
    }

    //비밀번호 변경
    @Transactional
    public boolean changePassword(String accessToken, PasswordCheckRequest request) {
        try {
            //1.토큰 검증
            jwtUtil.validateToken(accessToken);

            //2. 토큰에서 로그인 아이디 추출 하기;
            String loginId = jwtUtil.extractLoginId(accessToken);

            //3. 유저 조회
            User user = userRepository.findByLoginId(loginId)
                    .orElseThrow(() -> new CustomException(ErrorMessage.USER_NOT_FOUND));

            //4. 이전 비밀번호와 같을경우 예외
            if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                return false;
            }
            //5. 비밀번호 암호화
            String encodedPassword = passwordEncoder.encode(request.getPassword());

            //6. 변경된 비밀번호 저장
            user.changePassword(encodedPassword);

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    //회원탈퇴
    @Transactional
    public boolean withdrawUser(String accessToken) {
        // 1. 토큰 검증
        jwtUtil.validateToken(accessToken);

        // 2. 토큰에서 로그인 아이디 추출
        String loginId = jwtUtil.extractLoginId(accessToken);

        // 3. 유저 조회
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new CustomException(ErrorMessage.USER_NOT_FOUND));

        // 4. 소셜 계정 연결 해제 (DB에 저장된 정보로 서버에서 직접 처리)
        if (user.getKakaoId() != null) {
            kakaoService.unlinkKakaoAccount(user.getKakaoId()); // admin key 사용
        }
        if (user.getNaverId() != null) {
            naverService.revokeNaverAccount(user.getNaverRefreshToken()); // refresh token 사용
        }

        // 5. 그룹별 처리
        List<GroupMember> myGroupMembers = groupMemberRepository.findByUserId(user.getId());
        for (GroupMember gm : myGroupMembers) {
            Group group = gm.getGroup();
            boolean isAssigner = user.getId().equals(group.getAssignerId());

            // 나를 제외한 다른 멤버 목록
            List<GroupMember> otherMembers = groupMemberRepository
                    .findByGroupIdOrderByCreatedAtAsc(group.getId())
                    .stream()
                    .filter(m -> !m.getUser().getId().equals(user.getId()))
                    .collect(Collectors.toList());

            if (isAssigner && otherMembers.isEmpty()) {
                // 내가 방장이고 혼자인 그룹 → 그룹의 모든 할일 삭제 후 그룹 삭제
                // TaskAssignee는 CascadeType.ALL로 자동 삭제
                List<Task> groupTasks = taskRepository.findByGroupsId(group.getId());
                for (Task task : groupTasks) {
                    task.getGroups().clear();
                }
                taskRepository.deleteAll(groupTasks);
                groupMemberRepository.delete(gm);
                groupRepository.delete(group);

            } else if (isAssigner) {
                // 내가 방장이고 다른 멤버가 있음 → 내 담당 할일 삭제 후 방장 이전
                deleteMyTasksInGroup(user.getId(), group.getId());
                GroupMember newAssigner = otherMembers.get(0); // 가입 순서 첫 번째
                group.changeAssigner(newAssigner.getUser().getId());
                groupMemberRepository.delete(gm);

            } else {
                // 일반 그룹원 → 해당 그룹에서 내 담당 할일 삭제 후 탈퇴
                deleteMyTasksInGroup(user.getId(), group.getId());
                groupMemberRepository.delete(gm);
            }
        }

        // 6. 유저 삭제
        userRepository.delete(user);

        return true;
    }

    // 특정 그룹에서 내가 담당자인 할일 삭제
    // TaskAssignee는 CascadeType.ALL + orphanRemoval = true 로 자동 삭제
    private void deleteMyTasksInGroup(UUID userId, UUID groupId) {
        List<Task> myTasks = taskRepository.findByAssigneesIdAndGroupsId(userId, groupId);
        for (Task task : myTasks) {
            task.getGroups().clear();
        }
        taskRepository.deleteAll(myTasks);
    }

    // ──────────────────────────────────────────
    // 아이디 찾기
    // ──────────────────────────────────────────

    // 아이디 찾기 - 이메일로 인증코드 발송
    @Transactional
    public Boolean findUserIdApi(String email) {
        // 해당 이메일로 가입된 유저가 없으면 예외
        if (userRepository.findByEmail(email).isEmpty()) {
            throw new CustomException(ErrorMessage.USER_NOT_FOUND);
        }
        sendVerificationCode(email);
        return true;
    }

    // 아이디 찾기 - 인증코드 확인 후 loginId 반환
    @Transactional
    public UserFindIdEmailCodeResponse enterTheVerifcationCodeApi(String email, String token) {
        EmailVerification ev = emailRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorMessage.INVALID_VERIFICATION_CODE));

        if (!ev.getToken().equals(token)) {
            throw new CustomException(ErrorMessage.INVALID_VERIFICATION_CODE);
        }
        if (ev.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new CustomException(ErrorMessage.INVALID_TIME_VERIFICATION_CODE);
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorMessage.USER_NOT_FOUND));

        return new UserFindIdEmailCodeResponse(user.getLoginId());
    }

    // ──────────────────────────────────────────
    // 비밀번호 찾기
    // ──────────────────────────────────────────

    // 비밀번호 찾기 - 아이디+이메일 확인 후 인증코드 발송
    @Transactional
    public Boolean sendPasswordResetCode(PasswordResetSendRequest request) {
        User user = userRepository.findByLoginId(request.getLoginId())
                .orElseThrow(() -> new CustomException(ErrorMessage.USER_NOT_FOUND));

        if (!user.getEmail().equals(request.getEmail())) {
            throw new CustomException(ErrorMessage.INVALID_AUTH_INFO);
        }

        sendVerificationCode(request.getEmail());
        return true;
    }

    // 비밀번호 찾기 - 인증코드 확인 후 새 비밀번호로 변경
    @Transactional
    public Boolean resetPassword(PasswordResetConfirmRequest request) {
        EmailVerification ev = emailRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException(ErrorMessage.INVALID_VERIFICATION_CODE));

        if (!ev.getToken().equals(request.getToken())) {
            throw new CustomException(ErrorMessage.INVALID_VERIFICATION_CODE);
        }
        if (ev.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new CustomException(ErrorMessage.INVALID_TIME_VERIFICATION_CODE);
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException(ErrorMessage.USER_NOT_FOUND));

        user.changePassword(passwordEncoder.encode(request.getNewPassword()));
        return true;
    }

    // ──────────────────────────────────────────
    // 공통 내부 메서드
    // ──────────────────────────────────────────

    // 인증코드 발송 (쿨타임·횟수 제한 포함)
    private void sendVerificationCode(String email) {
        LocalDateTime now = LocalDateTime.now();
        String code = String.format("%06d", ThreadLocalRandom.current().nextInt(1000000));

        EmailVerification ev = emailRepository.findByEmail(email).orElse(null);
        if (ev == null) {
            ev = new EmailVerification(email, code, now.plusMinutes(7));
            emailRepository.save(ev);
        } else {
            // 쿨타임 체크 (10초)
            if (ev.getLastSendTime() != null &&
                    ev.getLastSendTime().plusSeconds(COOLDOWN_SECONDS).isAfter(now)) {
                throw new CustomException(ErrorMessage.EMAIL_SEND_COOLDOWN);
            }
            // 횟수 리셋 체크 (5분 경과 시 초기화)
            if (ev.getCountResetTime() != null && now.isAfter(ev.getCountResetTime())) {
                ev.resetCount(now, COUNT_RESET_MINUTES);
            }
            // 횟수 초과 체크
            if (ev.getSendCount() >= MAX_SEND_COUNT) {
                throw new CustomException(ErrorMessage.EMAIL_SEND_LIMIT_EXCEEDED);
            }
            ev.updateCode(code, now.plusMinutes(7));
        }

        ev.recordSend(now);
        smtpEmailSender.sendVerificationCode(email, code);
    }

    private <T> T getOrDefault(T value, T defaultValue) {
        return value != null ? value : defaultValue;
    }
}
   