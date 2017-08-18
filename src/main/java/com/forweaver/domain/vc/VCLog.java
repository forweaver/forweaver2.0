package com.forweaver.domain.vc;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/** 버전 관리의 커밋 로그 정보를 담기 위한 클래스
 *
 */
public class VCLog implements Serializable {

	static final long serialVersionUID = 23434L;
	
	private String logID;
	private String shortMassage; // 커밋 축약 메세지
	private String fullMassage; // 커밋 메세지 전문
	private String commiterName;
	private String commiterEmail;
	private String diff; //diff의 모든 내용
	private String note; // git note 내용
	private Date commitDate;
	
	public VCLog(String logID, String shortMassage,String fullMassage,
			String commiterName, String commiterEmail,String diff,String note,
			int commitDate) {
		this.logID = logID;
		this.shortMassage = shortMassage;
		this.fullMassage = fullMassage;
		this.commiterName = commiterName;
		this.commiterEmail = commiterEmail;
		this.note = note;
		this.diff = diff;

		this.commitDate = new Date(commitDate*1000L);
	}
	
	
	public String getLogID() {
		return logID;
	}
	public void setLogID(String logID) {
		this.logID = logID;
	}
	public String getShortMassage() {
		return shortMassage;
	}
	public void setShortMassage(String shortMassage) {
		this.shortMassage = shortMassage;
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
	public String getCommitDate() {
	    SimpleDateFormat sdf = new SimpleDateFormat("yy/MM/dd HH:mm");
	    return sdf.format(this.commitDate);
	}
	public void setCommitDate(Date commitDate) {
		this.commitDate = commitDate;
	}
	
	public String getImgSrc(){
		return "/"+this.commiterEmail+"/img";
	}

	public String getFullMassage() {
		return fullMassage;
	}
	public void setFullMassage(String fullMassage) {
		this.fullMassage = fullMassage;
	}
	public String getDiff() {
		return diff;
	}
	public void setDiff(String diff) {
		this.diff = diff;
	}
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}	
	
}
