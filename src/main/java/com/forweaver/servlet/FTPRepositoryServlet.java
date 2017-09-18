package com.forweaver.servlet;

import javax.annotation.PostConstruct;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.listener.ListenerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.forweaver.servlet.ftp.FTPUserManager;
import com.forweaver.util.SVNUtil;

@Component
public class FTPRepositoryServlet {
	private static final Logger logger =
			LoggerFactory.getLogger(FTPRepositoryServlet.class);
	
	@Autowired
	FTPUserManager userManager;
	@Value("${ftp.repository.port}")
	int port;
	
	@PostConstruct
	public void start() {
		FtpServer server;
		FtpServerFactory serverFactory = new FtpServerFactory();

		ListenerFactory factory = new ListenerFactory();
		factory.setPort(port);
		serverFactory.addListener("default", factory.createListener());
		server = serverFactory.createServer();
		serverFactory.setUserManager(userManager);
		try {
			server.start();
		} catch (FtpException ex) {
			logger.error(ex.getMessage());
		}
	}
}
