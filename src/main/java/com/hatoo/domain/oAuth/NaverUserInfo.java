package com.hatoo.domain.oAuth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NaverUserInfo {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("properties")
    private NaverProperties properties;

    @JsonProperty("naver_account")
    private NaverAccount naverAccount;

    @Getter
    @NoArgsConstructor
    public static class NaverProperties {
        private String nickname;
    }

    @Getter
    @NoArgsConstructor
    public static class NaverAccount {
        private String email;
    }
}
