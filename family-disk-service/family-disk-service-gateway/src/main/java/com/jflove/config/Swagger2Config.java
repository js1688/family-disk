package com.jflove.config;

import io.swagger.annotations.SwaggerDefinition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import springfox.documentation.service.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tanjun
 * @date 2022/12/6 16:10
 */
@Configuration
@EnableSwagger2
public class Swagger2Config {
    @Value("${spring.profiles.active:NA}")
    private String active;

    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .enable("dev".equalsIgnoreCase(active) || "local".equalsIgnoreCase(active))
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.jflove.controller"))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
        .title("家庭网盘后端服务 open api")
        .description("")
        .contact(new Contact("jflove", "https://www.jflove.cn", "woshitanjun@icloud.com"))
        .version("0.0.1-SNAPSHOT")
        .build();
    }
}
