package com.hatto.domain.user.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PasswordCheckRequest {

    @NotBlank(message = "비밀번호를 입력해주세요")
    private String password;
}