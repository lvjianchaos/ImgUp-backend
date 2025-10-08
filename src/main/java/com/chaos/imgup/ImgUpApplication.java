package com.chaos.imgup;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.chaos.imgup.mapper")
@SpringBootApplication
public class ImgUpApplication {

    public static void main(String[] args) {
        SpringApplication.run(ImgUpApplication.class, args);
    }

}
