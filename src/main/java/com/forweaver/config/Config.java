package com.forweaver.config;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

@Component()
public class Config {
	public static String gitPath;
	public static String svnPath;

	@PostConstruct
	private void init() {
		gitPath = "/home/vc/";
		svnPath = "/Users/macbook/project/svn/";
	}
}
