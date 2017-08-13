package com.forweaver.domain;

import java.io.Serializable;
import java.util.Date;

import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class ProjectInvite implements Serializable {

	static final long serialVersionUID = 54354364334L;
	private String joinTeam;
	private String proposer;
	private String waitingWeaver;
	private int postID;
	private Date created;
	
	public ProjectInvite(){}

	public ProjectInvite(String joinTeam, String proposer, String waitingWeaver,int postID) {
		this.joinTeam = joinTeam;
		this.proposer = proposer;
		this.waitingWeaver = waitingWeaver;
		this.created = new Date();
		this.postID = postID;
	}


	public String getJoinTeam() {
		return joinTeam;
	}
	public void setJoinTeam(String joinTeam) {
		this.joinTeam = joinTeam;
	}
	public String getProposer() {
		return proposer;
	}
	public void setProposer(String proposer) {
		this.proposer = proposer;
	}

	public String getWaitingWeaver() {
		return waitingWeaver;
	}

	public void setWaitingWeaver(String waitingWeaver) {
		this.waitingWeaver = waitingWeaver;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public int getPostID() {
		return postID;
	}

	public void setPostID(int postID) {
		this.postID = postID;
	}	
	
	
}
