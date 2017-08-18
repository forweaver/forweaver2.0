package com.forweaver.domain.vc;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;


/** 파일 브라우져에서 간단하게 볼 수 있도록 리스트에 담길 파일 정보 클래스
 * @author go
 *
 */
public class VCSimpleFileInfo implements Serializable {

	static final long serialVersionUID = 3333333333L;
	
	private String name;
	private String path;
	private boolean isDirectory;
	private String commitID;
	private String simpleLog;
	private Date commitDate;
	private int commitDateInt;
	private String commiterName;
	private String commiterEmail;
		
	public VCSimpleFileInfo(String name, String path,
			boolean isDirectory, String commitID, String simpleLog,
			int commitDateInt,String commiterName,
			String commiterEmail) {
		this.name = name;
		this.path = "/"+path;
		this.isDirectory = isDirectory;
		this.commitID = commitID;
		this.simpleLog = simpleLog;
		this.commitDateInt = commitDateInt;
		this.commitDate = new Date(commitDateInt*1000L);
		this.commiterName =commiterName;
		this.commiterEmail = commiterEmail;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public boolean getIsDirectory() {
		return isDirectory;
	}
	public void setDirectory(boolean isDirectory) {
		this.isDirectory = isDirectory;
	}
	public String getCommitID() {
		return commitID;
	}
	public void setCommitID(String commitID) {
		this.commitID = commitID;
	}
	public String getSimpleLog() {
		return simpleLog;
	}
	public void setSimpleLog(String simpleLog) {
		this.simpleLog = simpleLog;
	}
	public String getCommitDate() {
	    SimpleDateFormat sdf = new SimpleDateFormat("yy/MM/dd HH:mm");
	    return sdf.format(this.commitDate);
	}
	public void setCommitDate(Date commitDate) {
		this.commitDate = commitDate;
	}
	public int getCommitDateInt() {
		return commitDateInt;
	}
	public void setCommitDateInt(int commitDateInt) {
		this.commitDateInt = commitDateInt;
	}
	public String getCommiterName() {
		return commiterName;
	}
	public void setCommiterName(String commiterName) {
		this.commiterName = commiterName;
	}
	public String getCommiterEmail() {
		return commiterEmail;
	}
	public void setCommiterEmail(String commiterEmail) {
		this.commiterEmail = commiterEmail;
	}
	
	
	
}
