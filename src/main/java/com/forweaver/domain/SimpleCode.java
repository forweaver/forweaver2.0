package com.forweaver.domain;

import java.io.Serializable;

import com.forweaver.util.WebUtil;

public class SimpleCode  implements Serializable {

	static final long serialVersionUID = 5229134L;
	private String fileName;
	private String content;
	
	public SimpleCode(String fileName, String content) {
		super();
		this.fileName = fileName;
		this.fileName = this.fileName.replace(" ", "_");
		this.fileName = this.fileName.replace("#", "_");
		this.fileName = this.fileName.replace("?", "_");	
		this.fileName = this.fileName.trim();
		this.content = content;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	
	public boolean isImgFile(){
		return WebUtil.isImageName(this.fileName);
	}
	
}
