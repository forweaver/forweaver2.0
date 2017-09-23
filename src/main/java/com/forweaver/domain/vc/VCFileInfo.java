package com.forweaver.domain.vc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.revwalk.RevCommit;

/** 버전 관리의 파일 정보를 담기 위한 클래스
 *
 */
public class VCFileInfo implements Serializable {

	static final long serialVersionUID = 39311473L;

	private String name;
	private String content;
	private byte[] data;
	private List<VCSimpleLog> logList = new ArrayList<VCSimpleLog>();
	private int selectCommitIndex;
	private boolean isDirectory;
	private List<VCBlame> gitBlames = new ArrayList<VCBlame>();
	private boolean isLock;
	private String LockDate;
	private String LockAuth = "";
	private String LockComment = "";
	
	public VCFileInfo(String name, String content,byte[] data,
			List<VCSimpleLog> logList,int selectCommitIndex,boolean isDirectory) {
		this.name = name;
		this.content = content;
		this.data = data;
		this.logList = logList;
		this.selectCommitIndex = selectCommitIndex;
		this.isDirectory = isDirectory;
	}

	public VCFileInfo(String name, String content,byte[] data,
			List<VCSimpleLog> logList,int selectCommitIndex,boolean isDirectory, boolean isLock, String LockDate, String LockAuth, String LockComment) {
		this.name = name;
		this.content = content;
		this.data = data;
		this.logList = logList;
		this.selectCommitIndex = selectCommitIndex;
		this.isDirectory = isDirectory;
		this.isLock = isLock;
		this.LockDate = LockDate;
		this.LockAuth = LockAuth;
		this.LockComment = LockComment;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}

	public List<VCSimpleLog> getLogList() {
		return logList;
	}

	public void setLogList(List<VCSimpleLog> logList) {
		this.logList = logList;
	}

	public int getSelectCommitIndex() {
		return selectCommitIndex;
	}
	public void setSelectCommitIndex(int selectCommitIndex) {
		this.selectCommitIndex = selectCommitIndex;
	}

	public VCSimpleLog getSelectLog() {
		if(this.logList.size() <= 0){
			return null;
		}else if(this.logList.size() == 1){
			return this.logList.get(0);
		}else{
			if(this.getSelectCommitIndex() >= this.logList.size())
				return this.logList.get(this.logList.size()-1);
			else
				return this.logList.get(this.getSelectCommitIndex());
		}
	}

	public List<VCBlame> getBlames() {
		return gitBlames;
	}

	public void setBlames(List<VCBlame> gitBlames) {
		this.gitBlames = gitBlames;
	}

	public boolean isDirectory() {
		return isDirectory;
	}

	public void setDirectory(boolean isDirectory) {
		this.isDirectory = isDirectory;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public boolean isLock() {
		return isLock;
	}

	public void setLock(boolean isLock) {
		this.isLock = isLock;
	}

	public String getLockAuth() {
		return LockAuth;
	}

	public void setLockAuth(String lockAuth) {
		LockAuth = lockAuth;
	}

	public String getLockComment() {
		return LockComment;
	}

	public void setLockComment(String lockComment) {
		LockComment = lockComment;
	}
	
	public String getLockDate() {
		return LockDate;
	}

	public void setLockDate(String lockDate) {
		LockDate = lockDate;
	}


	public VCFileInfo(){
	}
}
