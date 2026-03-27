package com.hatoo.domain.groups;

import com.hatoo.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.hatoo.domain.user.User;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "`groups`") // groups는 MySQL의 예약어이므로 백틱(`)으로 감싸야 합니다.
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Group extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;

    @Column
    private String name;

    @Column
    private String description;

    @Column
    private String assignerId;

    @Column
    private Integer stock;

    @Column
    private String status;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<User> users = new ArrayList<>();


    public Group(String name, String description, String assignerId) {
        this.name = name;
        this.description = description;
        this.assignerId = assignerId;
    }

    public Group(String name, String description, String assignerId, Integer stock, String status) {
        this.name = name;
        this.description = description;
        this.assignerId = assignerId;
        this.stock = stock;
        this.status = status;
    }

    public Group(String aDefault, String 기본_그룹) {
        super();
    }

    public Group(String name, String description, Long assignerId) {
        this.name = name;
        this.description = description;
        this.assignerId = String.valueOf(assignerId);
    }
}