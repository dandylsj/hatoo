package com.hatoo.domain.activitylog;

import com.hatoo.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

// 할일이 나중에 삭제돼도 "누가 언제 등록했는지" 이력은 그대로 남도록 별도 테이블로 분리
@Entity
@Table(name = "task_creation_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TaskCreationLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(columnDefinition = "BINARY(16)")
    private UUID taskId;

    @Column(columnDefinition = "BINARY(16)")
    private UUID creatorId;

    @Column
    private String creatorNickname;

    @Column(columnDefinition = "BINARY(16)")
    private UUID groupId;

    @Column
    private String title;

    public TaskCreationLog(UUID taskId, UUID creatorId, String creatorNickname, UUID groupId, String title) {
        this.taskId = taskId;
        this.creatorId = creatorId;
        this.creatorNickname = creatorNickname;
        this.groupId = groupId;
        this.title = title;
    }
}
