package com.forweaver.domain.git.statistics;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

//날짜별로 유저의 커밋을 분석하고 정보를 담는 클래스.
public class GitChildStatistics implements Serializable {
	
	static final long serialVersionUID = 323232313L;
	
	private String userEmail;
	private int addLine;
	private int deleteLine;
	private int addFile;
	private int deleteFile;
	private Date commitDate;
	
	public GitChildStatistics(String userEmail, int addLine, int deleteLine,
			int addFile, int deleteFile, Date commitDate) {
		super();
		this.userEmail = userEmail;
		this.addLine = addLine;
		this.deleteLine = deleteLine;
		this.addFile = addFile;
		this.deleteFile = deleteFile;
		this.commitDate = commitDate;
	}

	public String getUserEmail() {
		return userEmail;
	}

	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}

	public int getAddLine() {
		return addLine;
	}

	public void setAddLine(int addLine) {
		this.addLine = addLine;
	}

	public int getDeleteLine() {
		return deleteLine;
	}

	public void setDeleteLine(int deleteLine) {
		this.deleteLine = deleteLine;
	}

	public Date getCommitDate() {
		return commitDate;
	}

	public void setCommitDate(Date commitDate) {
		this.commitDate = commitDate;
	}
	
	public String getDate(){
		SimpleDateFormat formatter = new SimpleDateFormat ( "yyyy/MM/dd");
		return formatter.format ( this.commitDate );
	}
	
	public int getTotal(){
		return this.addLine+this.deleteLine;
	}
	
	public int getTotalFile(){
		return this.addFile+this.deleteFile;
	}

	public int getAddFile() {
		return addFile;
	}

	public void setAddFile(int addFile) {
		this.addFile = addFile;
	}

	public int getDeleteFile() {
		return deleteFile;
	}

	public void setDeleteFile(int deleteFile) {
		this.deleteFile = deleteFile;
	}
	
	
	
	
}
