package com.hatoo.domain.groups;

import com.hatoo.common.BaseEntity;
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

    @ManyToMany(mappedBy = "groups")
    private List<User> users = new ArrayList<>();

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
    }

    public void updateInviteCode(String inviteCode, LocalDateTime expiryDate) {
        this.inviteCode = inviteCode;
        this.inviteCodeExpiryDate = expiryDate;
    }

    public Collection<User> getMembers() {
        return users;
    }
}
