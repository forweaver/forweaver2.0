package com.forweaver.controller;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@SpringBootApplication(
		scanBasePackages={"com.forweaver.config",
				"com.forweaver.controller","com.forweaver.service","com.forweaver.util","com.forweaver.dao"})
@ServletComponentScan(basePackages="com.forweaver.servlet")
public class SpringbootJspTestApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringbootJspTestApplication.class, args);
	}
}
