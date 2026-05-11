package com.hatoo.domain.oAuth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class GoogleTokenInfo {

    // Google 유저 고유 ID
    private String sub;

    private String email;

    @JsonProperty("email_verified")
    private String emailVerified;

    private String name;

    @JsonProperty("given_name")
    private String givenName;

    @JsonProperty("family_name")
    private String familyName;

    private String picture;
}
