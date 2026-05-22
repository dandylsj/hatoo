package com.hatoo.domain.groups;

import com.hatoo.common.BaseEntity;
import com.hatoo.domain.groupMember.GroupMember;
import com.hatoo.domain.task.Task;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.hatoo.domain.user.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "`groups`")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Group extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column
    private String name;

    @Column
    private String description;

    @Column(columnDefinition = "BINARY(16)")
    private UUID assignerId;

    @Column
    private String inviteCode;

    @Column
    private LocalDateTime inviteCodeExpiryDate;

    @Column(nullable = false)
    private boolean isPersonal = false;

    @OneToMany(mappedBy = "group")
    private List<GroupMember> groupMembers = new ArrayList<>();

    @ManyToMany
    private List<Task> tasks = new ArrayList<>();

    public Group(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public Group(String name, String description, UUID assignerId) {
        this.name = name;
        this.description = description;
        this.assignerId = assignerId;
        this.isPersonal = false;
    }

    public Group(String name, String description, UUID assignerId, boolean isPersonal) {
        this.name = name;
        this.description = description;
        this.assignerId = assignerId;
        this.isPersonal = isPersonal;
    }

    public void updateInviteCode(String inviteCode, LocalDateTime expiryDate) {
        this.inviteCode = inviteCode;
        this.inviteCodeExpiryDate = expiryDate;
    }

    // 그룹 이름 수정
    public void updateName(String name) {
        this.name = name;
    }

    // 방장 변경
    public void changeAssigner(UUID newAssignerId) {
        this.assignerId = newAssignerId;
    }

    public Collection<GroupMember> getMembers() {
        return groupMembers;
    }

    public Group getGroup() {
        return this;
    }
}
