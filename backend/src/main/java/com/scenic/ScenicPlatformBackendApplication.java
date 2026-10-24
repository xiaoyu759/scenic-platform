package com.scenic;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.scenic.mapper")
public class ScenicPlatformBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScenicPlatformBackendApplication.class, args);
    }

}
