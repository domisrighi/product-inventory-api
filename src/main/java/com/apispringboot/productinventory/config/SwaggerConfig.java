package com.apispringboot.productinventory.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.*;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI ProductInventoryOpenAPI() {
        return new OpenAPI().info(new Info()
           .title("Product Inventory API")
           .description("This API was developed for an assessment project for the course Development for Servers II")
           .version("v0.0.1")
           .contact(new Contact()
             .name("Dominike Righi, Kauan Coli e Rodrigo Oliveira"))
           .license(new License()
             .name("Apache 2.0").url("http://springdoc.org")));
     }
    
}
