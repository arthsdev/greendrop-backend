package br.com.greendrop.backend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "GreenDrop API",
                version = "1.0",
                description = "API oficial da plataforma GreenDrop para gestão de usuários, autenticação, produtos e operações internas.",
                contact = @Contact(
                        name = "Equipe GreenDrop",
                        email = "support@greendrop.com"
                )
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Local Development Server")
        },
        security = {
                @SecurityRequirement(name = "bearerAuth")
        },
        tags = {
                @Tag(name = "Authentication", description = "Endpoints relacionados a registro, login, refresh e logout."),
                @Tag(name = "Users", description = "Gerenciamento de usuários do sistema."),
                @Tag(name = "Products", description = "Gerenciamento de produtos, categorias e estoque.")
        }
)
@SecurityScheme(
        name = "bearerAuth",
        description = "Insira seu JWT no formato: Bearer {token}",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT"
)
public class OpenAPIConfig {
}
