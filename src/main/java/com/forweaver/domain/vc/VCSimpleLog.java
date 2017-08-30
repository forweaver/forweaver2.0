package com.forweaver.domain.vc;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.jgit.revwalk.RevCommit;

/** 커밋 브라우져에서 볼 수 있도록 리스트에 담길 커밋 로그 클래스
 *
 */
public class VCSimpleLog implements Serializable {

	static final long serialVersionUID = 23434L;
	
	private String logID;
	private String shortMassage;
	private String commiterName;
	private String commiterEmail;
	private Date commitDate;
	private int commitDateInt;
	
	public VCSimpleLog(String logID, String shortMassage,
			String commiterName, String commiterEmail,
			int commitDate) {
		this.logID = logID;
		this.shortMassage = shortMassage;
		this.commiterName = commiterName;
		this.commiterEmail = commiterEmail;
		this.commitDate = new Date(commitDate*1000L);
	}
	
	public VCSimpleLog(String commitLogID, String shortMassage,
			String commiterName, String commiterEmail,
			Date commitDate) {
		this.logID = commitLogID;
		this.shortMassage = shortMassage;
		this.commiterName = commiterName;
		this.commiterEmail = commiterEmail;
		this.commitDate = commitDate;
	}
	
	public VCSimpleLog(RevCommit revCommit){
		if(revCommit == null)
			return;
		this.logID = revCommit.getName();
		this.shortMassage = revCommit.getShortMessage();
		this.commiterName = revCommit.getAuthorIdent().getName();
		this.commiterEmail =  revCommit.getAuthorIdent().getEmailAddress();
		this.commitDate = new Date(revCommit.getCommitTime()*1000L);
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


	public int getCommitDateInt() {
		return commitDateInt;
	}

	public void setCommitDateInt(int commitDateInt) {
		this.commitDateInt = commitDateInt;
	}
	
}
