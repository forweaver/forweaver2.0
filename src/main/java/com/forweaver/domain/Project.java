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

/**<pre> 프로젝트 정보를 담은 클래스. 
 * name 프로젝트 이름 이게 기본 키
 * category  프로젝트 종류 값이 0이면 공개 프로젝트, 1이면 비공개 프로젝트, -1이면 파생 프로젝트, 3이면 과제 프로젝트
 * description  프로젝트 소개
 * openingDate 프로젝트 시작일
 * endDate  프로젝트 종료일
 * originalProject  원본 프로젝트 이름
 * creator 프로젝트 개설자 정보
 * push 프로젝트 추천수
 * childProjects 파생 프로젝트 모음
 * isJoin 프로젝트 가입 여부 0일떄 미가입 ,1일때 그냥 가입 ,2일때 관리자
 * tags 프로젝트 태그 모음
 * activeDate 활성화된 날짜
 * commitCount 커밋 갯수
 * adminWeavers 관리자들
 * joinWeavers 가입자들
 * </pre>
 */
@Document
public class Project implements Serializable {

	static final long serialVersionUID = 423123232123124234L;
	@Id
	private String name;
	private int category;
	private String description;
	private Date openingDate;
	private Date endDate;
	private String originalProjectName;
	private Date activeDate;
	private int commitCount;
	@DBRef
	private Weaver creator;
	private long push;
	@DBRef
	private List<Project> childProjects = new ArrayList<Project>();
	
	@Transient
	private int isJoin;
	
	private List<String> tags = new ArrayList<String>();
	
	@DBRef
	private List<Weaver> adminWeavers = new ArrayList<Weaver>();
	@DBRef
	private List<Weaver> joinWeavers = new ArrayList<Weaver>();
	
	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	
	
	public Project() {
		
	}
	
	public Project(String name, int category, String description, // 기본 생성자
			Weaver weaver,List<String> tagList) {
		super();
		this.name = weaver.getId()+"/"+name;
		this.name = this.name.toLowerCase();
		this.category = category;
		this.description = description;
		this.openingDate = new Date();
		this.creator = weaver;
		this.adminWeavers.add(weaver);
		this.tags = tagList;
		this.activeDate = this.openingDate;
	}
	
	public Project(String name,Weaver weaver,Project originalProject) { //포크할 때 생성자
		super();
		this.name = weaver.getId()+"/"+name;
		this.name = this.name.toLowerCase();
		this.category = -1;
		this.description = originalProject.getDescription();
		this.openingDate = new Date();
		this.activeDate = this.openingDate;
		this.creator = weaver;
		this.adminWeavers.add(weaver);
		if(originalProject.getOriginalProjectName() != null //이미 파생한 프로젝트를 또 파생할때
				&& originalProject.getOriginalProjectName().length() >0){
			this.tags.addAll(originalProject.getTags());
		}else{ // 원본 프로젝트를 파생할 때
			this.tags.add("@"+originalProject.getName());
			this.tags.addAll(originalProject.getTags());
		}
		originalProject.getChildProjects().add(this);
		this.originalProjectName = originalProject.getName();
		
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getCategory() {
		return category;
	}

	public void setCategory(int category) {
		this.category = category;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Date getOpeningDate() {
		return openingDate;
	}

	public void setOpeningDate(Date openingDate) {
		this.openingDate = openingDate;
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

	public String getOpeningDateFormat() {
		SimpleDateFormat df = new SimpleDateFormat("yy/MM/dd");
		return df.format(this.openingDate); 
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
	
	public String getChatRoomName(){
		return this.name.replace("/", "@");
	}

	public String getOriginalProjectName() {
		return originalProjectName;
	}

	public void setOriginalProjectName(String originalProjectName) {
		this.originalProjectName = originalProjectName;
	}

	public List<Project> getChildProjects() {
		return childProjects;
	}

	public void setChildProjects(List<Project> childProjects) {
		this.childProjects = childProjects;
	}


	public int getDDay() {
		if(this.endDate == null)
			return -2;
		 long left =  this.endDate.getTime() - System.currentTimeMillis();
		 int leftDay = (int)Math.floor(left/(1000*60*60*24)+1);
		 if(leftDay < 0)
			 return -1;
		 
		 return leftDay;
	}
	
	public boolean isForkProject(){
		if(this.originalProjectName != null && this.originalProjectName.length() > 0)
			return true;
		return false;
	}
	
	public List<Weaver> getWeavers() {
		List<Weaver> weavers = new ArrayList<Weaver>();
		weavers.addAll(this.joinWeavers);
		weavers.addAll(this.adminWeavers);
		return weavers;
	}
	
	public boolean isEducation(){
		return this.category == 3;
	}

	public Date getActiveDate() {
		return activeDate;
	}

	public void setActiveDate(Date activeDate) {
		this.activeDate = activeDate;
	}

	public int getCommitCount() {
		return commitCount;
	}

	public void setCommitCount(int commitCount) {
		this.commitCount = commitCount;
	}
	
	public boolean isPublic(){
		if(this.category == 0 || this.category ==-1)
			return true;
		return false;
	}
	
	public boolean isProjectWeaver(Weaver weaver){
		if(this.creator.equals(weaver))
			return true;
		for(Weaver joinWeaver:this.joinWeavers)
			if(joinWeaver.equals(weaver))
				return true;
		return false;
				
	}
	
	
	public boolean isForked(){
		if(this.category == -1)
			return true;
		
		if(this.originalProjectName == null || this.originalProjectName.length()  == 0)
			if(this.childProjects == null || this.childProjects.size() == 0)
				return false;
		
		return true;
	}
	
}
