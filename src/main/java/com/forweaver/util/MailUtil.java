package com.forweaver.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;

/**<pre>메일을 보내기 위한 빈.
 * 참고 - http://www.mkyong.com/spring/spring-sending-e-mail-via-gmail-smtp-server-with-mailsender/</pre> 
 *b17100d39d057e62
 */
public class MailUtil {
	
	private static final Logger logger =
			LoggerFactory.getLogger(MailUtil.class);
	
	private MailSender mailSender;
    private String from; 
    
	/** 보내는 메일 설정
	 * @param mailSender
	 */
	public void setMailSender(MailSender mailSender) {
		this.mailSender = mailSender;
	}
 
	/** 메일로 메세지를 보냄.
	 * @param to
	 * @param subject
	 * @param msg
	 */
	public void sendMail(String to, String subject, String msg) {
		SimpleMailMessage message = new SimpleMailMessage();
 
		message.setFrom(from);
		message.setTo(to);
		message.setSubject(subject);
		message.setText(msg);
		mailSender.send(message);
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}
	
	
}
