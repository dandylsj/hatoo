package com.hatoo.domain.ai;

import com.hatoo.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "ai_chat_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiChatHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(columnDefinition = "BINARY(16)", nullable = false)
    private UUID userId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String userMessage;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String aiResponse;

    @Column
    private int tokensUsed;

    public AiChatHistory(UUID userId, String userMessage, String aiResponse, int tokensUsed) {
        this.userId = userId;
        this.userMessage = userMessage;
        this.aiResponse = aiResponse;
        this.tokensUsed = tokensUsed;
    }
}
