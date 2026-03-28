package com.hatoo.domain.groups;

import com.hatoo.common.exception.CustomException;
import com.hatoo.common.exception.ErrorMessage;
import com.hatoo.common.util.JwtUtil;
import com.hatoo.domain.groups.dto.*;

import com.hatoo.domain.user.User;
import com.hatoo.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;


    //내가 속한 그룹 조회
    @Transactional
    public MyGroupResponse myGroupInfoResponse(String accessToken) {

        jwtUtil.validateToken(accessToken);

        String loginId = jwtUtil.extractLoginId(accessToken);

        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new CustomException(ErrorMessage.USER_NOT_FOUND));

        // 2. 조회된 User 엔티티에서 Group 정보 가져오기
        Group group = user.getGroup();

        // 3. 유저가 그룹에 속해있지 않은 경우 예외 처리
        if (group == null) {
            throw new CustomException(ErrorMessage.USER_NOT_IN_GROUP); // ErrorMessage에 추가 필요
        }

        // 4. Group 엔티티를 DTO로 변환하여 반환
        return new MyGroupResponse(group);
    }

    //그룹 생성
    @Transactional
    public GroupCreateResponse groupCreateResponse(GroupCreateRequest request) {

        Group group = new Group(
                request.getName(),
                request.getDescription()
        );

        groupRepository.save(group);

        return new GroupCreateResponse(
                group.getId(),
                group.getName(),
                group.getDescription(),
                group.getAssignerId(),
                group.getCreatedAt(),
                group.getUpdatedAt()
        );
    }

    //그룹 멤버 리스트 조회
    @Transactional
    public GroupMemberListResponse groupMemberListResponse(UUID groupId) {

        groupRepository.findById(groupId)
                .orElseThrow(()-> new CustomException(ErrorMessage.GROUP_NOT_FOUND));

        // 1. 해당 그룹에 속한 유저 리스트 조회
        List<User> users = userRepository.findAllByGroupId(groupId);
        
        // 2. User 엔티티 리스트를 GroupMemberDto 리스트로 변환
        List<GroupMemberDto> memberDtos = users.stream()
                .map(GroupMemberDto::from)
                .collect(Collectors.toList());

        // 3. DTO에 감싸서 반환 (총 멤버 수와 함께 반환)
        return new GroupMemberListResponse(memberDtos.size(), memberDtos);
    }

    //그룹 참여
    @Transactional
    public boolean joinGroupApi(String accessToken, UUID groupId) {
        // 1. 토큰 검증 및 유저 로그인 ID 추출
        jwtUtil.validateToken(accessToken);
        String loginId = jwtUtil.extractLoginId(accessToken);

        // 2. 유저 조회
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new CustomException(ErrorMessage.USER_NOT_FOUND));

        // 3. 유저가 이미 다른 그룹에 속해 있는지 확인
        if (user.getGroup() != null) {
            if (user.getGroup().getId().equals(groupId)) {
                throw new CustomException(ErrorMessage.ALREADY_JOINED_GROUP); // 이미 이 그룹에 속해 있음
            } else {
                throw new CustomException(ErrorMessage.ALREADY_IN_ANOTHER_GROUP); // 이미 다른 그룹에 속해 있음
            }
        }

        // 4. 참여하려는 그룹 조회
        Group group = groupRepository.findById(groupId)
                .orElseThrow(()-> new CustomException(ErrorMessage.GROUP_NOT_FOUND));

        // 5. 유저를 그룹에 할당 (User 엔티티의 assignGroup 메서드 사용)
        // @Transactional 어노테이션 덕분에 user 엔티티의 변경 사항은 트랜잭션 커밋 시 자동으로 DB에 반영됩니다.
        return user.assignGroup(group);
    }

    //그룹 초대코드 생성
    @Transactional
    public GroupInviteCodeResponse inviteCodeAPi(GroupInviteCodeRequest request) {

        Group group = groupRepository.findById(request.getGroupId())
                .orElseThrow(() ->  new CustomException(ErrorMessage.GROUP_NOT_FOUND));
        
        // 그룹 초대 코드 생성 및 유효기간 설정
        String inviteCode = generateInviteCode();
        LocalDateTime expiryDate = LocalDateTime.now().plusDays(7);
        
        group.updateInviteCode(inviteCode, expiryDate);

        return new GroupInviteCodeResponse(inviteCode, expiryDate);
    }

    private String generateInviteCode() {
        int length = 4;
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder inviteCode = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            inviteCode.append(characters.charAt(random.nextInt(characters.length())));
        }
        return inviteCode.toString();
    }
}
