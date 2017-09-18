package com.forweaver.servlet.ftp;

import java.util.ArrayList;
import java.util.List;

import org.apache.ftpserver.ftplet.Authentication;
import org.apache.ftpserver.ftplet.AuthenticationFailedException;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.usermanager.PasswordEncryptor;
import org.apache.ftpserver.usermanager.UsernamePasswordAuthentication;
import org.apache.ftpserver.usermanager.impl.AbstractUserManager;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.ConcurrentLoginPermission;
import org.apache.ftpserver.usermanager.impl.TransferRatePermission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.stereotype.Component;

import com.forweaver.domain.Weaver;
import com.forweaver.service.WeaverService;


@Component
public class FTPUserManager extends AbstractUserManager {

	@Autowired
	private WeaverService weaverService;

	public FTPUserManager() {
	}

	@Override
	public User getUserByName(String userName) {
		Weaver weaver = weaverService.get(userName);
		if (weaver == null)
			return null;

		BaseUser user = new BaseUser();
		user.setName(weaver.getId());
		user.setPassword(weaver.getPassword());
		user.setEnabled(true);
		user.setHomeDirectory("/home/go/ftp/");		
		List<Authority> authorities = new ArrayList<>();
		authorities.add(new ConcurrentLoginPermission(0, 0));
		authorities.add(new TransferRatePermission(0, 0));
		user.setAuthorities(authorities);
		user.setMaxIdleTime(0);
		return user;
	}

	@Override
	public String[] getAllUserNames() throws FtpException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void delete(String arg0) throws FtpException {
		// TODO Auto-generated method stub

	}

	@Override
	public void save(User arg0) throws FtpException {
		// TODO Auto-generated method stub

	}
	@Override
	public boolean doesExist(String s) throws FtpException {
		return true;
	}

	public User authenticate(Authentication authentication) throws AuthenticationFailedException {

		if (authentication instanceof UsernamePasswordAuthentication) {
			UsernamePasswordAuthentication usernamePasswordAuthentication =
					(UsernamePasswordAuthentication) authentication;

			String user = usernamePasswordAuthentication.getUsername();
			String password = usernamePasswordAuthentication.getPassword();

			if (user == null || password == null) {
				throw new AuthenticationFailedException("Authentication failed");
			}

			User weaver = getUserByName(user);
			if (weaver == null || 
					!weaver.getPassword().equals(new ShaPasswordEncoder().encodePassword(password, null)) ) {
				throw new AuthenticationFailedException("Authentication failed");
			}	

			return weaver;
		}
		return null;
	}
}