package com.boaglio;


import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Dia51 {

    public static void main(String[] args) {
        SpringApplication.run(Dia51.class, args);
    }

    @Bean
    public OpenAPI springShopOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("DIA51 API")
                        .description("Sabe tudo de MSX")
                        .version("v1.0"))
                .externalDocs(new ExternalDocumentation().description("GitHub").url("https://github.com/boaglio/DIA51"));
    }

}