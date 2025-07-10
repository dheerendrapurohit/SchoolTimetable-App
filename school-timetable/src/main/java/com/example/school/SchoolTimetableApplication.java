package com.example.school;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(
        scanBasePackages = "com.example.school",
        exclude = { DataSourceAutoConfiguration.class } // 👈 disables SQL DB auto-config
)
@EnableTransactionManagement
@EntityScan("com.example.school.entity")
public class SchoolTimetableApplication {
    public static void main(String[] args) {
        SpringApplication.run(SchoolTimetableApplication.class, args);
    }
}
