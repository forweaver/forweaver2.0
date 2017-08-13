package com.forweaver.domain;

import java.io.Serializable;
import java.util.Date;

import org.springframework.security.core.GrantedAuthority;

/**<pre> 회원의 권한을 보관하는 클래스
 * joinName 권한 이름
 * permission 권한 수준
 * joinDate 권한 부여일
 *</pre>
 */
public class Pass implements GrantedAuthority, Serializable {

	static final long serialVersionUID = 11111161134L;
	private String joinName;
	private int permission;
	private Date joinDate;
	
	public Pass(){
		
	}
	
	public Pass(String joinName) {
		this.joinName = joinName;
		this.joinDate = new Date();
	}
	
	public Pass(String joinName,int permission) {
		this.permission = permission;
		this.joinName = joinName;
		this.joinDate = new Date();
	}
	
	public int getPermission() {
		return permission;
	}
	public void setPermission(int permission) {
		this.permission = permission;
	}
	public String getJoinName() {
		return joinName;
	}
	public void setJoinName(String joinName) {
		this.joinName = joinName;
	}
	public Date getJoinDate() {
		return joinDate;
	}
	public void setJoinDate(Date joinDate) {
		this.joinDate = joinDate;
	}

	
	public String getAuthority() {
		// TODO Auto-generated method stub
		return this.joinName;
	}
}
