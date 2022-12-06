package com.jflove;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import springfox.documentation.oas.annotations.EnableOpenApi;

@SpringBootApplication
@EnableOpenApi
public class FamilyDiskServiceGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(FamilyDiskServiceGatewayApplication.class, args);
    }

}
