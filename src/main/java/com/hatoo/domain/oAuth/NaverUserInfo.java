package com.hatoo.domain.oAuth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NaverUserInfo {

    @JsonProperty("resultcode")
    private String resultcode;

    @JsonProperty("message")
    private String message;

    @JsonProperty("response")
    private NaverResponse response;

    @Getter
    @NoArgsConstructor
    public static class NaverResponse {

        @JsonProperty("id")
        private String id;

        @JsonProperty("nickname")
        private String nickname;

        @JsonProperty("email")
        private String email;

        @JsonProperty("name")
        private String name;
    }
}
