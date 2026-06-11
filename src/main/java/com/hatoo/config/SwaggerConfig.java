package com.hatoo.config;

import com.hatoo.common.model.response.ErrorResponse;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        String jwtSchemeName = "jwtAuth";

        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtSchemeName);

        // ErrorResponse 스키마 자동 등록
        Map<String, Schema> errorSchemas = ModelConverters.getInstance()
                .read(ErrorResponse.class);

        Components components = new Components()
                .addSecuritySchemes(jwtSchemeName, new SecurityScheme()
                        .name(jwtSchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"));

        errorSchemas.forEach(components::addSchemas);

        // 로컬 개발 서버
        Server localServer = new Server();
        localServer.setUrl("http://localhost:8080");
        localServer.setDescription("Local Development Server");

        // NAS 서버
        Server productionServer = new Server();
        productionServer.setUrl("https://lsjyahoo.synology.me");
        productionServer.setDescription("Hatoo Production Server (NAS)");

        // AWS EC2 서버
        Server awsServer = new Server();
        awsServer.setUrl("https://43.203.92.102");
        awsServer.setDescription("Hatoo AWS EC2 Server");

        // Ubuntu 서버
        Server ubuntuServer = new Server();
        ubuntuServer.setUrl("https://dandyhomelab.uk");
        ubuntuServer.setDescription("Hatoo Ubuntu Server");

        return new OpenAPI()
                .info(apiInfo())
                .addServersItem(localServer)
                .addServersItem(productionServer)
                .addServersItem(awsServer)
                .addServersItem(ubuntuServer)
                .addSecurityItem(securityRequirement)
                .components(components);
    }

    // 모든 엔드포인트에 400 / 401 에러 응답을 자동으로 추가
    @Bean
    public OperationCustomizer globalErrorResponseCustomizer() {
        Schema<?> errorSchema = new Schema<>().$ref("#/components/schemas/ErrorResponse");
        Content errorContent = new Content()
                .addMediaType("application/json", new MediaType().schema(errorSchema));

        return (operation, handlerMethod) -> {
            ApiResponses responses = operation.getResponses();
            if (responses == null) {
                responses = new ApiResponses();
                operation.setResponses(responses);
            }
            responses.addApiResponse("400", new ApiResponse()
                    .description("Bad Request - 잘못된 요청 (유효성 검사 실패, 파라미터 오류 등)")
                    .content(errorContent));
            responses.addApiResponse("401", new ApiResponse()
                    .description("Unauthorized - 토큰이 없거나 만료/위조된 경우")
                    .content(errorContent));
            return operation;
        };
    }

    private Info apiInfo() {
        return new Info()
                .title("Hatoo API")
                .description("Hatoo API Document")
                .version("1.0.0");
    }
}
