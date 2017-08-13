package com.forweaver.domain;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import com.forweaver.util.WebUtil;

/**<pre> 글 정보를 담는 클래스. 
 * postID 글 아이디
 * title 글 제목
 * content 글 내용
 * isLong 단문인지 장문인지 여부
 * kind 글 종류 1이면 공개 2이면 프로젝트 글 3이면 메시지 글
 * created 생성일
 * recentRePostDate 최근 답변일
 * isNotice 시스템에서 자동으로 생성하는 알림 글 여부
 * writer 글쓴이
 * push 추천수
 * rePostCount 답변수
 * tags 태그들
 * datas 자료들
 * diffs 바뀐 정보들
 *</pre>
 */
@Document
public class Post implements Serializable {

	static final long serialVersionUID = 11666661134L;
	@Id
	private int postID;
	private String title;
	private String content;
	private boolean isLong;
	private int kind;
	private Date created;
	private Date recentRePostDate;
	private boolean isNotice;
	@DBRef
	private Weaver writer;
	private int push;
	private int rePostCount;
	private List<String> tags = new ArrayList<String>();
	@DBRef
	private List<Data> datas = new ArrayList<Data>();
	
	public Post(){}
		
	public Post(Weaver weaver,
			String title, String content,List<String> tags) {
		this.writer = weaver;
		this.title = title;
		this.content = content;
		this.created = new Date();
		this.tags = tags;
		this.kind = getKind(this.tags);
	}
	
	public Post(Weaver weaver,
			String title, String content,List<String> tags,boolean isNotice) {
		this.writer = weaver;
		this.title = title;
		this.content = content;
		this.created = new Date();
		this.tags = tags;
		this.isNotice = isNotice;
		this.kind = getKind(this.tags);
	}

	public int getPostID() {
		return postID;
	}
	public void setPostID(int postID) {
		this.postID = postID;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public Date getCreated() {
		return created;
	}
	public void setCreated(Date created) {
		this.created = created;
	}
	
	public String getFormatCreated() {
		SimpleDateFormat df = new SimpleDateFormat("yy/MM/dd HH:mm:ss");
		return df.format(created); 
	}
	

	public Weaver getWriter() {
		return writer;
	}

	public void setWriter(Weaver writer) {
		this.writer = writer;
	}
	
	public String getWriterName() {
		return writer.getId();
	}
	
	public String getWriterEmail() {
		return writer.getEmail();
	}

	public int getPush() {
		return push;
	}
	public void setPush(int push) {
		this.push = push;
	}
	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public boolean isLong() {
		return isLong;
	}
	public void setLong(boolean isLong) {
		this.isLong = isLong;
	}

	
	public void addTag(String tag){
		this.tags.add(tag);
	}

	public int getRePostCount() {
		return rePostCount;
	}

	public void setRePostCount(int rePostCount) {
		this.rePostCount = rePostCount;
	}
	
	public void rePostCountDown() {
		this.rePostCount -=1;
	}


	public Date getRecentRePostDate() {
		return recentRePostDate;
	}

	public void setRecentRePostDate(Date recentRePostDate) {
		this.recentRePostDate = recentRePostDate;
	}

	public String getImgSrc(){
		return this.writer.getImgSrc();	
	}

	public void push(){
		this.push +=1;
	}
	
	public void addRePostCount(){
		this.rePostCount +=1;
	}

	public int getKind() {
		return kind;
	}

	public void setKind(int kind) {
		this.kind = kind;
	}

	public void deleteTag(String tag){
		int i=0;
		for(String tmpTag : this.tags){
			if(tmpTag.equals(tag))
				this.tags.remove(i);
		}
	}

	public void addData(Data data){
		this.datas.add(data);
	}
	
	public void deleteData(String id){
		for(int i = 0 ; i< this.datas.size() ; i++){
			if(this.datas.get(i).getId().equals(id)){
				this.datas.remove(i);
				return;
			}
		}
		
	}
	
	public Data getData(String id){
		for(Data data:this.datas)
			if(data.getId().equals(id))
				return data;
		return null;
	}

	public List<Data> getDatas() {
		return datas;
	}

	public void setDatas(List<Data> datas) {
		this.datas = datas;
	}

	public boolean isNotice() {
		return isNotice;
	}

	public void setNotice(boolean isNotice) {
		this.isNotice = isNotice;
	}
	
	private int getKind(List<String> tags){
		for (String tag :tags)
			if (tag.startsWith("@"))
				return 2;
			else if (tag.startsWith("$")) 
				return 3;
		
		return 1;
	}
	
	public String getFirstImageURL(){
		for(Data data:this.datas)
			if(WebUtil.isImageName(data.getName()))
				return "/data/"+data.getId()+"/"+data.getName();
		return "";
	}
}
