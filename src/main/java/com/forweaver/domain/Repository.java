package com.forweaver.domain;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

/**<pre> 저장소 정보를 담은 클래스. 
 * name 저장소 이름 이게 기본 키.
 * authLevel  저장소 공개 범위 값이 0이면 일반 저장소, 1이면 비공개 저장소, 3이면 공개 저장소.
 * type  저장소 종류 값이 1이면 git, 2 svn, 3 ftp.
 * description  저장소 소개
 * creator 저장소 개설자 정보
 * push 저장소 추천수
 * isJoin 저장소 가입 여부 0일떄 미가입 ,1일때 그냥 가입 ,2일때 관리자
 * tags 저장소 태그 모음
 * date 활성화된 날짜
 * adminWeavers 관리자들
 * joinWeavers 가입자들
 * </pre>
 */
@Document
public class Repository implements Serializable {

	static final long serialVersionUID = 423123232123124234L;
	@Id
	private String name;
	private int authLevel;
	private String description;
	private int type;
	private Date date;
	@DBRef
	private Weaver creator;
	private long push;
	
	@Transient
	private int isJoin;
	
	private List<String> tags = new ArrayList<String>();
	
	@DBRef
	private List<Weaver> adminWeavers = new ArrayList<Weaver>();
	@DBRef
	private List<Weaver> joinWeavers = new ArrayList<Weaver>();
	
	public Repository() {
		
	}
	
	public Repository(String name, int authLevel, int type, String description, // 기본 생성자
			Weaver weaver,List<String> tagList) {
		super();
		this.name = weaver.getId()+"/"+name;
		this.name = this.name.toLowerCase();
		this.authLevel = authLevel;
		this.type = type;
		this.description = description;
		this.creator = weaver;
		this.adminWeavers.add(weaver);
		this.tags = tagList;
		this.date = new Date();
	}
	
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getAuthLevel() {
		return authLevel;
	}

	public void setAuthLevel(int authLevel) {
		this.authLevel = authLevel;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	
	
	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}
	
	public String getDateFormat() {
		SimpleDateFormat df = new SimpleDateFormat("yy/MM/dd");
		return df.format(this.date); 
	}

	public String getCreatorName() {
		return this.creator.getId();
	}

	public String getCreatorEmail() {
		return this.creator.getEmail();
	}
	
	public Weaver getCreator() {
		return creator;
	}

	public void setCreator(Weaver creator) {
		this.creator = creator;
	}

	public void addAdminWeaver(Weaver weaver){
		this.adminWeavers.add(weaver);
	}
	
	public void addJoinWeaver(Weaver weaver){
		this.joinWeavers.add(weaver);
	}
	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}


	public long getPush() {
		return push;
	}

	public void setPush(long push) {
		this.push = push;
	}
	
	public void push(){
		this.push +=1;
	}

	public List<Weaver> getAdminWeavers() {
		return adminWeavers;
	}

	public void setAdminWeavers(List<Weaver> adminWeavers) {
		this.adminWeavers = adminWeavers;
	}

	public List<Weaver> getJoinWeavers() {
		return joinWeavers;
	}

	public void setJoinWeavers(List<Weaver> joinWeavers) {
		this.joinWeavers = joinWeavers;
	}
	
	public int isJoin() {
		return isJoin;
	}

	public void setJoin(int isJoin) {
		this.isJoin = isJoin;
	}
	public String getImgSrc(){
		return this.creator.getImgSrc();
	}
	
	public void removeJoinWeaver(Weaver weaver){
		int index = -1;
		
		for(int i = 0;i<this.joinWeavers.size();i++)
			if(joinWeavers.get(i).getId().equals(weaver.getId()))
				index = i;
		
		
		if(index >= 0)
			this.joinWeavers.remove(index);
	}
	
	public void removeAdminWeaver(Weaver weaver){
		int index = -1;
		
		for(int i = 0;i<this.adminWeavers.size();i++)
			if(adminWeavers.get(i).getId().equals(weaver.getId()))
				index = i;
		
		if(index >= 0)
			this.adminWeavers.remove(index);
	}

	public List<Weaver> getWeavers() {
		List<Weaver> weavers = new ArrayList<Weaver>();
		weavers.addAll(this.joinWeavers);
		weavers.addAll(this.adminWeavers);
		return weavers;
	}
	
	public boolean isPublic(){
		if(this.authLevel == 0 || this.authLevel ==-1)
			return true;
		return false;
	}
	
	public boolean isJoinWeaver(Weaver weaver){
		if(this.creator.equals(weaver))
			return true;
		for(Weaver joinWeaver:this.joinWeavers)
			if(joinWeaver.equals(weaver))
				return true;
		return false;
				
	}

	public int getIsJoin() {
		return isJoin;
	}

	public void setIsJoin(int isJoin) {
		this.isJoin = isJoin;
	}
	
	public String getRepoName() {
		return this.name.substring(this.name.indexOf("/"),this.name.length());
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
	
	
}
