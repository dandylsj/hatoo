package com.hatoo.domain.task;

import com.hatoo.common.exception.GlobalExceptionHandler;
import com.hatoo.common.model.response.GlobalResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Task", description = "Task 관련 API")
@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskContoroller {

    private final TaskRepository taskRepository;

//    @Operation(summary = "할 일 추가", description = "할 일을 추가 합니다.")
//    @PostMapping
//    public ResponseEntity<GlobalResponse> addToDo()
}
