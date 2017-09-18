package com.forweaver.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@SpringBootApplication(
		scanBasePackages={"com.forweaver.config",
				"com.forweaver.controller","com.forweaver.service","com.forweaver.util","com.forweaver.dao","com.forweaver.servlet"})
@ServletComponentScan(basePackages="com.forweaver.servlet")
public class ForweaverApplication {

	public static void main(String[] args) {
		SpringApplication.run(ForweaverApplication.class, args);
	}
}
