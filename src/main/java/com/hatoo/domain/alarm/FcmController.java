package com.hatoo.domain.alarm;

import com.hatoo.common.model.response.GlobalResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Alarm", description = "FCM 알림 관련 API")
@RestController
@RequestMapping("/alarm")
@RequiredArgsConstructor
public class FcmController {

    private final FcmService fcmService;

}
