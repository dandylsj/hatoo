package com.hatoo.domain.token;


import com.hatoo.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "refresh_token")
public class RefreshToken extends BaseEntity {

    @Id
    @Column(name = "user_id", columnDefinition = "BINARY(16)")
    private UUID userId;

    @Column(name = "token", nullable = false)
    private String token;

    public RefreshToken(UUID userId, String token) {
        this.userId = userId;
        this.token = token;
    }

    public void updateToken(String token) {
        this.token = token;
    }
}
