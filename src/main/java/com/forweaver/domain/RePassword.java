package com.forweaver.domain;

import java.io.Serializable;

/** <pre> 재발급할 비밀번호와 키를 담는 클래스
 * key - 인증키
 * password - 새로운 비밀번호 
 * </pre>
 */
public class RePassword implements Serializable {
	
	static final long serialVersionUID = 54224234L; 
	private String key;
	private String password;
	
	public RePassword(String key, String password) {
		super();
		this.key = key;
		this.password = password;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	
}
