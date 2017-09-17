package com.forweaver.domain.vc;

import java.util.List;
import java.util.Map;

public class VCSvnInfo {
	//SVN 정보//
	private List<Object> authorlist; //커밋터 정보//
	private List<Object> datelist; //날짜정보//
	private Map<String, Object> diffinfo; //라인, 파일 관련 추가/삭제/수정 수//
	
	public List<Object> getAuthorlist() {
		return authorlist;
	}
	public void setAuthorlist(List<Object> authorlist) {
		this.authorlist = authorlist;
	}
	public List<Object> getDatelist() {
		return datelist;
	}
	public void setDatelist(List<Object> datelist) {
		this.datelist = datelist;
	}
	public Map<String, Object> getDiffinfo() {
		return diffinfo;
	}
	public void setDiffinfo(Map<String, Object> diffinfo) {
		this.diffinfo = diffinfo;
	}
}
