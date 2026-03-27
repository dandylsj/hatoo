package com.hatoo.domain.groups;

import com.hatoo.common.exception.CustomException;
import com.hatoo.common.exception.ErrorMessage;
import com.hatoo.common.util.JwtUtil;
import com.hatoo.domain.groups.dto.GroupCreateRequest;
import com.hatoo.domain.groups.dto.GroupCreateResponse;
import com.hatoo.domain.groups.dto.MyGroupResponse;
import com.hatoo.domain.user.User;
import com.hatoo.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

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
}
