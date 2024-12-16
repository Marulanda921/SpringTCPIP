package com.AQT.Aqutest.Application.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(title = "Gestor de Operaciones", version = "1.0", description = "Documentación api de administracion de operaciones"))
public class Swagger {
}
