package com.npst.spring_boot_project_generator;


import java.io.IOException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;

@Slf4j
@EnableAsync
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
public class SpringBootProjectGeneratorApplication {

	public static void main(String[] args) throws IOException {
		SpringApplication.run(SpringBootProjectGeneratorApplication.class, args);

		log.info("🚀 STARTING the SpringBootProjectGeneratorApplication...");

    }

}
