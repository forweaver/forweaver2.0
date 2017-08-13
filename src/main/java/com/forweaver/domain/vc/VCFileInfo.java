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
	private List<VCSimpleCommitLog> commitLogList = new ArrayList<VCSimpleCommitLog>();
	private int selectCommitIndex;
	private boolean isDirectory;
	private List<VCBlame> gitBlames = new ArrayList<VCBlame>();


	public VCFileInfo(){
	}

	public VCFileInfo(String name, String content,byte[] data,
			List<VCSimpleCommitLog> commitLogList,int selectCommitIndex,boolean isDirectory) {
		this.name = name;
		this.content = content;
		this.data = data;
		this.commitLogList = commitLogList;
		this.selectCommitIndex = selectCommitIndex;
		this.isDirectory = isDirectory;
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

	public List<VCSimpleCommitLog> getCommitLogList() {
		return commitLogList;
	}

	public void setCommitLogList(List<VCSimpleCommitLog> commitLogList) {
		this.commitLogList = commitLogList;
	}

	public int getSelectCommitIndex() {
		return selectCommitIndex;
	}
	public void setSelectCommitIndex(int selectCommitIndex) {
		this.selectCommitIndex = selectCommitIndex;
	}

	public VCSimpleCommitLog getSelectCommitLog() {
		if(this.commitLogList.size() <= 0){
			return null;
		}else if(this.commitLogList.size() == 1){
			return this.commitLogList.get(0);
		}else{
			if(this.getSelectCommitIndex() >= this.commitLogList.size())
				return this.commitLogList.get(this.commitLogList.size()-1);
			else
				return this.commitLogList.get(this.getSelectCommitIndex());
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




}
