package gnu.project.pbl2.common.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
    servers = {
        @Server(url = "http://localhost:8080", description = "local 서버입니다.")
    }
)
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI OpenAPI() {
        String securitySchemeName = "bearerAuth";

        return new OpenAPI()
            .info(new Info()
                .title("GNU Product API")
                .description("GNU 프로젝트의 Product 관련 REST API 명세서")
                .version("v1.0.0")
            )
            .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
            .components(new Components().addSecuritySchemes(securitySchemeName,
                new SecurityScheme()
                    .name(securitySchemeName)
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
            ));
    }

}
