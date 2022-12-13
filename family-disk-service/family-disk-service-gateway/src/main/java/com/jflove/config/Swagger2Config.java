package com.jflove.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Arrays;
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
                .securitySchemes(List.of(
                        new ApiKey(HttpConstantConfig.AUTHORIZATION, HttpConstantConfig.AUTHORIZATION, "header"),
                        new ApiKey(HttpConstantConfig.USE_SPACE_ID, HttpConstantConfig.USE_SPACE_ID, "header")
                ))//设置页面可以设置token到请求头部
                .securityContexts(List.of(securityContexts()))//设置哪些url要带上token请求
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

    private SecurityContext securityContexts() {
        return SecurityContext.builder()
                .securityReferences(defaultAuth())
                .build();
    }

    private List<SecurityReference> defaultAuth() {
        AuthorizationScope authorizationScope = new AuthorizationScope("xxax", "描述信息");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;
        return Arrays.asList(
                new SecurityReference(HttpConstantConfig.AUTHORIZATION, authorizationScopes),
                new SecurityReference(HttpConstantConfig.USE_SPACE_ID, authorizationScopes)
        );
    }
}
