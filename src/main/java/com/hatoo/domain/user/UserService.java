package com.hatoo.domain.user;

import com.hatoo.common.exception.CustomException;
import com.hatoo.common.exception.ErrorMessage;
import com.hatoo.common.util.JwtUtil;
import com.hatoo.domain.alarmUserAgree.AlarmUserAgree;
import com.hatoo.domain.alarmUserAgree.AlarmUserAgreeRepository;
import com.hatoo.domain.groupMember.GroupMember;
import com.hatoo.domain.groupMember.GroupMemberRepository;
import com.hatoo.domain.groups.Group;
import com.hatoo.domain.groups.GroupRepository;
import com.hatoo.domain.task.Task;
import com.hatoo.domain.task.TaskRepository;
import com.hatoo.domain.user.dto.AlarmAgreeRequest;
import com.hatoo.domain.user.dto.UserAgreeRequest;
import com.hatoo.domain.user.dto.UserInfoModifyRequest;
import com.hatoo.domain.user.dto.UserInfoModifyResponse;
import lombok.RequiredArgsConstructor;
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

    // 알림 동의 저장/수정 (설정에서 언제든 변경 가능)
    @Transactional
    public Boolean saveAlarmAgree(String accessToken, AlarmAgreeRequest request) {
        jwtUtil.validateToken(accessToken);
        String loginId = jwtUtil.extractLoginId(accessToken);

        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new CustomException(ErrorMessage.USER_NOT_FOUND));

        // AlarmUserAgree 없으면 새로 생성, 있으면 업데이트
        AlarmUserAgree alarmAgree = alarmUserAgreeRepository.findByUserId(user.getId())
                .orElse(null);

        if (alarmAgree == null) {
            alarmAgree = new AlarmUserAgree(
                    request.getIsChoreNotiAllowed() != null ? request.getIsChoreNotiAllowed() : false,
                    request.getIsMarketingNotiAllowed() != null ? request.getIsMarketingNotiAllowed() : false,
                    user
            );
            alarmUserAgreeRepository.save(alarmAgree);
        } else {
            alarmAgree.update(request.getIsChoreNotiAllowed(), request.getIsMarketingNotiAllowed());
        }

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

            // 6. 프로필 이미지가 변경된 경우, profileImg가 null인 GroupMember에도 반영
            if (request.getProfileImg() != null) {
                List<GroupMember> groupMembers = groupMemberRepository.findByUserId(user.getId());
                for (GroupMember gm : groupMembers) {
                    gm.updateProfileImg(request.getProfileImg());
                }
            }

            // 7. 응답 DTO 생성 반환
            return new UserInfoModifyResponse(user.getId(), user.getNickname(), user.getProfileImg());
    }

    //이전 비밀번호 확인
    @Transactional(readOnly = true)
    public boolean prePasswordVerification(String accessToken, String password) {
        try {
            // 1. 토큰 검증
            jwtUtil.validateToken(accessToken);

            // 2. 토큰에서 로그인 아이디 추출
            String loginId = jwtUtil.extractLoginId(accessToken);

            // 3. 유저 조회
            User user = userRepository.findByLoginId(loginId)
                    .orElseThrow(() -> new CustomException(ErrorMessage.USER_NOT_FOUND));

            // 4. 비밀번호 일치 여부 확인.
            return passwordEncoder.matches(password, user.getPassword());
        } catch (Exception e) {
            return false;
        }
    }

    //비밀번호 변경
    @Transactional
    public boolean changePassword(String accessToken, String password) {
        try {
            //1.토큰 검증
            jwtUtil.validateToken(accessToken);

            //2. 토큰에서 로그인 아이디 추출 하기;
            String loginId = jwtUtil.extractLoginId(accessToken);

            //3. 유저 조회
            User user = userRepository.findByLoginId(loginId)
                    .orElseThrow(() -> new CustomException(ErrorMessage.USER_NOT_FOUND));

            //4. 이전 비밀번호와 같을경우 예외
            if(passwordEncoder.matches(password, user.getPassword())) {
                return false;
            }
            //5. 비밀번호 암호화
            String encodedPassword = passwordEncoder.encode(password);

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

        // 4. 유저가 속한 모든 그룹 처리
        List<GroupMember> myGroupMembers = groupMemberRepository.findByUserId(user.getId());

        for (GroupMember gm : myGroupMembers) {
            Group group = gm.getGroup();

            // 해당 그룹에서 나를 제외한 다른 멤버 목록 (가입 순서 오름차순)
            List<GroupMember> otherMembers = groupMemberRepository.findByGroupIdOrderByCreatedAtAsc(group.getId())
                    .stream()
                    .filter(m -> !m.getUser().getId().equals(user.getId()))
                    .toList();

            boolean isAssigner = group.getAssignerId().equals(user.getId());

            if (isAssigner && otherMembers.isEmpty()) {
                // 내가 방장이고 혼자인 그룹 → 그룹의 모든 할일 삭제 후 그룹 삭제
                List<Task> groupTasks = taskRepository.findByGroupsId(group.getId());
                for (Task task : groupTasks) {
                    task.getAssignees().clear();
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

        // 6. 유저 하드 딜리트
        userRepository.delete(user);

        return true;
    }

    // 특정 그룹에서 유저가 담당인 할일 삭제
    private void deleteMyTasksInGroup(UUID userId, UUID groupId) {
        List<Task> myTasksInGroup = taskRepository.findByAssigneesIdAndGroupsId(userId, groupId);
        for (Task task : myTasksInGroup) {
            task.getAssignees().clear();
            task.getGroups().clear();
            taskRepository.delete(task);
        }
    }

}
