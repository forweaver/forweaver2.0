package com.forweaver.domain.git.statistics;

import java.io.Serializable;

public class SvnTotalStatistics implements Serializable{
	
	static final long serialVersionUID = 2219L;

	private int totalAdd;
	private int totalDelete;
	private int totalAddFile;
	private int totalDeleteFile;
	private int totalCommit;
	public SvnTotalStatistics(int totalAdd, int totalDelete, int totalAddFile,
			int totalDeleteFile, int totalCommit) {
		super();
		this.totalAdd = totalAdd;
		this.totalDelete = totalDelete;
		this.totalAddFile = totalAddFile;
		this.totalDeleteFile = totalDeleteFile;
		this.totalCommit = totalCommit;
	}
	public int getTotalAdd() {
		return totalAdd;
	}
	public void setTotalAdd(int totalAdd) {
		this.totalAdd = totalAdd;
	}
	public int getTotalDelete() {
		return totalDelete;
	}
	public void setTotalDelete(int totalDelete) {
		this.totalDelete = totalDelete;
	}
	public int getTotalAddFile() {
		return totalAddFile;
	}
	public void setTotalAddFile(int totalAddFile) {
		this.totalAddFile = totalAddFile;
	}
	public int getTotalDeleteFile() {
		return totalDeleteFile;
	}
	public void setTotalDeleteFile(int totalDeleteFile) {
		this.totalDeleteFile = totalDeleteFile;
	}
	public int getTotalCommit() {
		return totalCommit;
	}
	public void setTotalCommit(int totalCommit) {
		this.totalCommit = totalCommit;
	}

}
