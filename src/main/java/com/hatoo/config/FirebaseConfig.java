package com.hatoo.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

@Slf4j
@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void initFirebase() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                InputStream credentialsStream = resolveCredentials();
                if (credentialsStream == null) {
                    log.error("[Firebase] 초기화 실패 - FIREBASE_CREDENTIALS 환경변수 또는 서비스 계정 키 파일이 없습니다.");
                    return;
                }

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(credentialsStream))
                        .build();

                FirebaseApp.initializeApp(options);
                log.info("[Firebase] 초기화 완료");
            }
        } catch (IOException e) {
            log.error("[Firebase] 초기화 실패 - {}", e.getMessage());
        }
    }

    /**
     * Firebase 인증 정보를 아래 순서로 탐색
     * 1순위: FIREBASE_CREDENTIALS 환경변수 (Base64 인코딩된 JSON) → Docker 운영 환경
     * 2순위: classpath:firebase/service-account-key.json → 로컬 개발 환경
     */
    private InputStream resolveCredentials() {
        // 1순위: 환경변수 (Base64 디코딩)
        String base64Credentials = System.getenv("FIREBASE_CREDENTIALS");
        if (base64Credentials != null && !base64Credentials.isBlank()) {
            log.info("[Firebase] 환경변수 FIREBASE_CREDENTIALS 로 인증 정보 로드");
            byte[] decoded = Base64.getDecoder().decode(base64Credentials.trim());
            return new ByteArrayInputStream(decoded);
        }

        // 2순위: 로컬 파일 (개발 환경용)
        try {
            ClassPathResource resource = new ClassPathResource("firebase/service-account-key.json");
            if (resource.exists()) {
                log.info("[Firebase] 로컬 서비스 계정 키 파일로 인증 정보 로드");
                return resource.getInputStream();
            }
        } catch (IOException e) {
            log.warn("[Firebase] 로컬 키 파일 로드 실패 - {}", e.getMessage());
        }

        return null;
    }
}
