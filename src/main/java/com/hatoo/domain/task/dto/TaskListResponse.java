package com.hatoo.domain.task.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
@Schema(description = "할 일 목록 조회 응답")
public class TaskListResponse {

    @Schema(description = "할 일 ID")
    private UUID id;

    @Schema(description = "할 일 제목")
    private String title;

    @Schema(description = "할 일 설명")
    private String description;

    @Schema(description = "할 일이 속한 그룹 ID")
    private UUID groupId;

    @Schema(description = "시작 일시")
    private String dueFrom;

    @Schema(description = "마감 일시")
    private String dueTo;

    @Schema(description = "담당자 전원 완료 여부")
    private Boolean allFinished;

    @Schema(description = "반복 생성된 원본 할 일 ID (반복 할 일인 경우)")
    private String recurringTaskId;

    @Schema(description = "담당자 목록")
    private List<AssigneeDto> assignees;

    @Schema(description = "할 일을 생성한 사용자 ID")
    private UUID creatorId;

    @Getter
    @AllArgsConstructor
    @Schema(description = "담당자 완료 현황")
    public static class AssigneeDto {

        @Schema(description = "담당자 사용자 ID")
        private UUID id;

        @Schema(description = "담당자 닉네임")
        private String nickname;

        @Schema(description = "완료 여부")
        private Boolean finished;
    }
}
