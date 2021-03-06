package com.jack.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
@MapperScan("com.jack.admin.mapper") // 说明在com.jack.admin.mapper里面寻找map关系
public class ScmAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScmAdminApplication.class,args);
        //System.out.println(new BCryptPasswordEncoder().encode("123456"));
    }
}
