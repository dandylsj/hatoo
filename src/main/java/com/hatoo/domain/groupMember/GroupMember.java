package com.hatoo.domain.groupMember;

import com.hatoo.common.BaseEntity;
import com.hatoo.domain.groups.Group;
import com.hatoo.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "group_members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroupMember extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group;

    @Column
    private String profileImg;

    public GroupMember(User user, Group group, String profileImg) {
        this.user = user;
        this.group = group;
        this.profileImg = profileImg;
    }

    public void updateProfileImg(String profileImg) {
        this.profileImg = profileImg;
    }
}
