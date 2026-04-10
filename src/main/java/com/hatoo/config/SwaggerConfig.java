package com.hatoo.config;

import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        String jwtSchemeName = "jwtAuth";

        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtSchemeName);

        Components components = new Components()
                .addSecuritySchemes(jwtSchemeName, new SecurityScheme()
                        .name(jwtSchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"));

        // 배포된 도메인 주소를 명시적으로 등록
        Server server = new Server();
        server.setUrl("http://lsjyahoo.synology.me"); // HTTPS를 적용했다면 https:// 로 변경해야 합니다.
        server.setDescription("Hatoo Production Server");

        return new OpenAPI()
                .info(apiInfo())
                .addServersItem(server) // 서버 정보 추가
                .addSecurityItem(securityRequirement)
                .components(components);
    }

    private Info apiInfo() {
        return new Info()
                .title("Hatoo API")
                .description("Hatoo API Document")
                .version("1.0.0");
    }
}