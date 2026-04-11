package com.hatoo.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
public class PasswordCheckRequest {

    @NotBlank(message = "비밀번호를 입력해주세요")
    private String password;
}