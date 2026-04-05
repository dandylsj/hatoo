package com.hatoo.domain.kakao;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class KakaoUserInfo {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("properties")
    private KakaoProperties properties;

    @Getter
    @NoArgsConstructor
    public static class KakaoProperties {
        private String nickname;
    }
}
