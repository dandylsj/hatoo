package com.hatoo.domain.user.dto;

import jakarta.validation.constraints.Email;
import lombok.Getter;

@Getter
public class EmailVerifiRequest {

    private String email;

    private String token;
}
