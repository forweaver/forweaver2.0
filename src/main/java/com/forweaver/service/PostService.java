package com.forweaver.service;

import java.util.ArrayList;
import java.util.List;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.forweaver.domain.Data;
import com.forweaver.domain.Pass;
import com.forweaver.domain.Post;
import com.forweaver.domain.RePost;
import com.forweaver.domain.Weaver;
import com.forweaver.mongodb.dao.DataDao;
import com.forweaver.mongodb.dao.PostDao;
import com.forweaver.mongodb.dao.RePostDao;
import com.forweaver.mongodb.dao.WeaverDao;

@Service
public class PostService {

	@Autowired private PostDao postDao;
	@Autowired private RePostDao rePostDao;
	@Autowired private DataDao dataDao;
	@Autowired private WeaverDao weaverDao;
	@Autowired private CacheManager cacheManager;

	/** 글을 생성함.
	 * @param post
	 * @param datas
	 * @return
	 */
	public int add(Post post,List<Data> datas) {

		if (post.getContent().length() > 4)
			post.setLong(true);
		else {
			post.setLong(false);
			post.setContent("");
		}
		if (post.getTitle().length() < 5)
			return -1;

		//만약 자료를 올렸다면.
		if(datas != null && datas.size() >0)
			for(Data data:datas){
				dataDao.insert(data);
				post.addData(data);
			}
		return postDao.insert(post);

	}

	/** 글 하나를 가져옴.
	 * @param postID
	 * @return
	 */
	public Post get(int postID) {
		return postDao.get(postID);
	}

	/** 글 추천하면 캐시에 저장하고 24시간 제한을 둠.
	 * @param post
	 * @param weaver
	 * @return
	 */
	public boolean push(Post post, Weaver weaver,String ip) {
		if (weaver != null && weaver.equals(post.getWriter()))
			return false;

		Cache cache = cacheManager.getCache("push"); // 중복 추천 방지!
		Element element = cache.get(post.getPostID()+"@@"+ip);

		if (element == null || (element != null && element.getValue() == null)) {
			post.push();
			postDao.update(post);
			Element newElement = new Element(post.getPostID()+"@@"+ip, ip);
			cache.put(newElement);
			return true;
		}
		return false;
	}

	public void update(Post post,List<Data> datas,String[] removeDataList) {
		if (post.getTitle().length() < 5)
			return;

		//만약 자료를 올렸다면.
		if(datas != null && datas.size() >0)
			for(Data data:datas){
				dataDao.insert(data);
				post.addData(dataDao.getLast());
			}

		if(removeDataList != null)
			for(String dataID: removeDataList){
				dataDao.delete(post.getData(dataID));
				post.deleteData(dataID);
			}
		postDao.update(post);
	}

	/** 글을 그냥 삭제 메서드
	 * @param post
	 */
	public void delete(Post post){
		
		
		for(RePost rePost:rePostDao.gets(post)) { // 관련 답변들을 전부 불러옴
			
			for(Data data:rePost.getDatas()) //자료 전부 삭제.
				dataDao.delete(data);
			
			rePostDao.delete(rePost); // 답변 차례대로 삭제
		}
		
		for(Data data:post.getDatas()) //자료 전부 삭제.
			dataDao.delete(data);
		
		
		postDao.delete(post); //실제 글 삭제.
	}

	/** 글을 삭제함.
	 * @param post
	 * @param weaver
	 * @return
	 */
	public boolean delete(Weaver weaver,Post post) {
		if(post == null || weaver == null)
			return false;

		if (weaver.isAdmin() || 	post.getWriter().getId().equals(weaver.getId())) { // 글쓴이 또는 관리자의 경우
			this.delete(post);
			return true;
		} else if (post.getKind() == 2) { // 글쓴이가 아니고 프로젝트 태그의 경우
			String projectString = "";
			for (String tag : post.getTags()) {
				if (tag.startsWith("@"))
					projectString = tag;
			}

			for (Pass pass : weaver.getPasses()) {
				if (projectString.equals("@" + pass.getJoinName())
						&& pass.getPermission() == 1) {
					this.delete(post);
					return true;
				}
			}
			return false;
		} else if (post.getKind() == 3) { // 메세지 태그의 경우

			if (post.getTags().size() == 1
					&& post.getTags().get(0).equals("@" + weaver.getId())) {
				this.delete(post);
				return true;
			} else {
				for (String tag : post.getTags()) {
					if (tag.equals("@" + weaver.getId())) {
						post.deleteTag(tag);
						postDao.update(post);
						return true;
					}
				}
				return false;
			}
		} else
			return false;
	}

	/** 글의 수를 셈.
	 * @param weaver
	 * @param sort
	 * @return
	 */
	public long countPosts(Weaver weaver,String sort) {
		if(weaver == null) //로그인하지 않은 회원의 경우
			return postDao.countPostsWhenNotLogin(null, null, null, sort);

		if(sort.equals("my")) // 자기가 쓴 글과 볼수 있는 글 바탕으로 검색.
			return postDao.countMyPosts(null,null, weaver, null, sort);

		return postDao.countPostsWhenLogin(null,weaver.getPrivateAndMassageTags(),null,null, sort);

	}

	/** 글을 가져옴.
	 * @param weaver
	 * @param sort
	 * @param page
	 * @param size
	 * @return
	 */
	public List<Post> getPosts(Weaver weaver,String sort, int page, int size) {

		if(weaver == null) //로그인하지 않은 회원의 경우
			return postDao.getPostsWhenNotLogin(null, null, null, sort, page, size);

		if(sort.equals("my")) // 자기가 쓴 글과 볼수 있는 글 바탕으로 검색.
			return postDao.getMyPosts(null,null, weaver, null, sort,page,size);

		return postDao.getPostsWhenLogin(null,weaver.getPrivateAndMassageTags(),null,null, sort, page, size);
	}

	/** 태그와 정렬을 이용하여  글의 수를 셈
	 * @param weaver
	 * @param tags
	 * @param sort
	 * @return
	 */
	public long countPosts(Weaver weaver,List<String> tags,String sort) {

		if(weaver == null) //로그인하지 않은 회원의 경우
			return postDao.countPostsWhenNotLogin(tags, null, null, sort);

		if(sort.equals("my")) // 자기가 쓴 글과 볼수 있는 글 바탕으로 검색.
			return postDao.countMyPosts(tags,null, weaver, null, sort);

		if(this.isPublicTags(tags)) // 태그가 공개 태그일 경우.
			return postDao.countPostsWhenLogin(tags,weaver.getPrivateAndMassageTags(),null,null, sort);

		if(this.isPrivateTags(tags)) // 태그가 프로젝트 태그일 경우.
			return postDao.countPostsAsPrivateTags(tags, null, null, sort);

		if(this.isMassageTags(tags)) // 태그가 메세지 태그의 경우.
			return postDao.countPostsAsMassageTag( this.getOneMassageTag(weaver.getId(),tags), null, weaver, this.getOneMassageTag(weaver.getId(),tags).equals("$"+weaver.getId()), sort);

		return 0;
	}

	/** 태그와 정렬을 이용하여 글을 가져옴
	 * @param weaver
	 * @param tags
	 * @param sort
	 * @param page
	 * @param size
	 * @return
	 */
	public List<Post> getPosts(Weaver weaver,List<String> tags,String sort, int page, int size) {

		if(weaver == null) //로그인하지 않은 회원의 경우
			return postDao.getPostsWhenNotLogin(tags, null, null, sort, page, size);

		if(sort.equals("my")) // 자기가 쓴 글과 볼수 있는 글 바탕으로 검색.
			return postDao.getMyPosts(tags,null, weaver, null, sort, page, size);

		if(this.isPublicTags(tags)) // 태그가 공개 태그일 경우.
			return postDao.getPostsWhenLogin(tags,weaver.getPrivateAndMassageTags(),null,null, sort, page, size);

		if(this.isPrivateTags(tags)) // 태그가 프로젝트 태그일 경우.
			return postDao.getPostsAsPrivateTags(tags, null, null, sort, page, size);

		if(this.isMassageTags(tags))// 태그가 메세지 태그의 경우.
			return postDao.getPostsAsMassageTag( this.getOneMassageTag(weaver.getId(),tags), null, weaver, this.getOneMassageTag(weaver.getId(),tags).equals("$"+weaver.getId()), sort, page, size);

		return null;
	}

	/** 태그와 정렬 그리고 검색어를 이용하여 글을 수를 셈
	 * @param weaver
	 * @param tags
	 * @param search
	 * @param sort
	 * @return
	 */
	public long countPosts(Weaver weaver,
			List<String> tags,String search,String sort) {

		if(weaver == null) //로그인하지 않은 회원의 경우
			return postDao.countPostsWhenNotLogin(tags, search, null, sort);
		if(sort.equals("my")) // 자기가 쓴 글과 볼수 있는 글 바탕으로 검색.
			return postDao.countMyPosts(tags,null, weaver, search, sort);
		if(this.isPublicTags(tags)) // 태그가 공개 태그일 경우.
			return postDao.countPostsWhenLogin(tags,weaver.getPrivateAndMassageTags(),null,search, sort);
		if(this.isPrivateTags(tags)) // 태그가 프로젝트 태그일 경우.
			return postDao.countPostsAsPrivateTags(tags, search, null, sort);
		if(this.isMassageTags(tags)) // 태그가 메세지 태그의 경우.
			return postDao.countPostsAsMassageTag( this.getOneMassageTag(weaver.getId(),tags), search, weaver, this.getOneMassageTag(weaver.getId(),tags).equals("$"+weaver.getId()), sort);
		return 0;
	}

	/** 태그와 정렬 그리고 검색어를 이용하여  글들을 가져옴.
	 * @param weaver
	 * @param tags
	 * @param search
	 * @param sort
	 * @param page
	 * @param size
	 * @return
	 */
	public List<Post> getPosts(Weaver weaver,
			List<String> tags,String search,String sort, int page, int size) {

		if(weaver == null) //로그인하지 않은 회원의 경우
			return postDao.getPostsWhenNotLogin(tags, search, null, sort, page, size);
		if(sort.equals("my")) // 자기가 쓴 글과 볼수 있는 글 바탕으로 검색.
			return postDao.getMyPosts(tags,null, weaver, search, sort, page, size);
		if(this.isPublicTags(tags)) // 태그가 공개 태그일 경우.
			return postDao.getPostsWhenLogin(tags,weaver.getPrivateAndMassageTags(),null,search, sort, page, size);
		if(this.isPrivateTags(tags)) // 태그가 프로젝트 태그일 경우.
			return postDao.getPostsAsPrivateTags(tags, search, null, sort, page, size);
		if(this.isMassageTags(tags)) // 태그가 메세지 태그의 경우.
			return postDao.getPostsAsMassageTag( this.getOneMassageTag(weaver.getId(),tags), search, weaver, this.getOneMassageTag(weaver.getId(),tags).equals("$"+weaver.getId()), sort, page, size);

		return null;

	}

	/** 다른 회원의 글의 수를 셈.
	 * @param loginWeaver
	 * @param writer
	 * @param sort
	 * @return
	 */
	public long countPosts(Weaver loginWeaver,Weaver writer,String sort) {

		if(loginWeaver == null) //로그인하지 않은 회원의 경우
			return postDao.countPostsWhenNotLogin(null, null, writer, sort);

		if(loginWeaver.equals(writer))
			if(sort.equals("my"))
				return postDao.countMyPosts(null,null, writer, null, sort);
			else
				return postDao.countMyPosts(null,loginWeaver.getPrivateAndMassageTags(), writer, null, sort);
		else
			return postDao.countPostsAsWriter(null, 
					loginWeaver.getPrivateAndMassageTags(),
					writer, loginWeaver, null, sort);
	}

	/** 다른 회원의 글 목록을 가져옴.
	 * @param loginWeaver
	 * @param writer
	 * @param sort
	 * @param page
	 * @param size
	 * @return
	 */
	public List<Post> getPosts(Weaver loginWeaver,
			Weaver writer,String sort, int page, int size) {

		if(loginWeaver == null) //로그인하지 않은 회원의 경우
			return postDao.getPostsWhenNotLogin(null, null, writer, sort,page,size);

		if(loginWeaver.equals(writer))
			if(sort.equals("my"))
				return postDao.getMyPosts(null,null, writer, null, sort,page,size);
			else
				return postDao.getMyPosts(null,loginWeaver.getPrivateAndMassageTags(), writer, null, sort,page,size);
		else
			return postDao.getPostsAsWriter(null, 
					loginWeaver.getPrivateAndMassageTags(), 
					writer, loginWeaver, null, sort, page, size);
	}


	/** 다른 회원의 글을 태그와 정렬을 조합하여 가져오고 수를 셈.
	 * @param loginWeaver
	 * @param tags
	 * @param writer
	 * @param sort
	 * @return
	 */
	public long countPosts(Weaver loginWeaver,List<String> tags,Weaver writer,String sort) {

		if(loginWeaver == null) //로그인하지 않은 회원의 경우
			return postDao.countPostsWhenNotLogin(tags, null, writer, sort);

		if(loginWeaver.equals(writer))
			if(sort.equals("my"))
				return postDao.countMyPosts(tags,null, writer, null, sort);
			else
				return postDao.countMyPosts(tags,loginWeaver.getPrivateAndMassageTags(), writer, null, sort);
		else
			return postDao.countPostsAsWriter(tags, 
					loginWeaver.getPrivateAndMassageTags(),
					writer, loginWeaver, null, sort);
	}


	/** 다른 회원의 글을 태그와 정렬을 조합하여 글을 가져옴.
	 * @param loginWeaver
	 * @param tags
	 * @param writer
	 * @param sort
	 * @param page
	 * @param size
	 * @return
	 */
	public List<Post> getPosts(Weaver loginWeaver,List<String> tags,
			Weaver writer,String sort, int page, int size) {

		if(loginWeaver == null) //로그인하지 않은 회원의 경우
			return postDao.getPostsWhenNotLogin(tags, null, writer, sort,page,size);

		if(loginWeaver.equals(writer))
			if(sort.equals("my"))
				return postDao.getMyPosts(tags,null, writer, null, sort,page,size);
			else
				return postDao.getMyPosts(tags,loginWeaver.getPrivateAndMassageTags(), writer, null, sort,page,size);
		else
			return postDao.getPostsAsWriter(tags, 
					loginWeaver.getPrivateAndMassageTags(), 
					writer, loginWeaver, null, sort, page, size);
	}

	/** 다른 회원이 쓴 글의 수를 셈.
	 * @param loginWeaver
	 * @param tags
	 * @param writer
	 * @param search
	 * @param sort
	 * @return
	 */
	public long countPosts(Weaver loginWeaver,List<String> tags,Weaver writer,String search,String sort) {

		if(loginWeaver == null) //로그인하지 않은 회원의 경우
			return postDao.countPostsWhenNotLogin(tags, search, writer, sort);

		if(loginWeaver.equals(writer))
			if(sort.equals("my"))
				return postDao.countMyPosts(tags,null, writer, search, sort);
			else
				return postDao.countMyPosts(tags,loginWeaver.getPrivateAndMassageTags(), writer, search, sort);
		else
			return postDao.countPostsAsWriter(tags, 
					loginWeaver.getPrivateAndMassageTags(),
					writer, loginWeaver, search, sort);
	}


	/**  다른 회원이 쓴 글의 목록을 가져옴.
	 * @param loginWeaver
	 * @param tags
	 * @param writer
	 * @param search
	 * @param sort
	 * @param page
	 * @param size
	 * @return
	 */
	public List<Post> getPosts(Weaver loginWeaver,List<String> tags,
			Weaver writer,String search,String sort, int page, int size) {

		if(loginWeaver == null) //로그인하지 않은 회원의 경우
			return postDao.getPostsWhenNotLogin(tags, search, writer, sort,page,size);

		if(loginWeaver.equals(writer))
			if(sort.equals("my"))
				return postDao.getMyPosts(tags,null, writer, search, sort,page,size);
			else
				return postDao.getMyPosts(tags,loginWeaver.getPrivateAndMassageTags(), writer, search, sort,page,size);
		else
			return postDao.getPostsAsWriter(tags, 
					loginWeaver.getPrivateAndMassageTags(), 
					writer, loginWeaver, search, sort, page, size);
	}


	/** 메세지 태그인지 확인함.
	 * @param tags
	 * @return
	 */
	public boolean isMassageTags(List<String> tags) {
		return tags.get(0).startsWith("$") && !tags.get(0).contains("/");
	}

	/** 프로젝트 태그인지 확인함.
	 * @param tags
	 * @return
	 */
	public boolean isPrivateTags(List<String> tags) {
		for (String tag : tags) 
			if (tag.startsWith("@"))
				return true;
		return false;
	}

	/** 일반 공개 태그인지 확인함.
	 * @param tags
	 * @return
	 */
	public boolean isPublicTags(List<String> tags) {

		for (String tag : tags)
			if (tag.startsWith("@") || tag.startsWith("$"))
				return false;

		return true;
	}

	/** 자신의 메세지 태그가 붙어 있는지 확인함.
	 * @param nickName
	 * @param tags
	 * @return
	 */
	public String getOneMassageTag(String nickName, List<String> tags) {

		if(tags.size() == 1)
			return tags.get(0);

		if (tags.get(0).equals("$" + nickName)) 
			return tags.get(1);
		else 
			return "$" + nickName;
	}
	
	/** 권한 상관 없이 조건에 따라 글을 가져오고 수를 셈.
	 * @param tags
	 * @param writer
	 * @param search
	 * @param sort
	 * @return
	 */
	public long countPosts(List<String> tags,String search,Weaver writer,String sort) {
		return postDao.countPostsAsAdmin(tags, search, writer, sort);
	}


	/**  권한 상관 없이 조건에 따라 글을 가져옴.
	 * @param tags
	 * @param writer
	 * @param search
	 * @param sort
	 * @param page
	 * @param size
	 * @return
	 */
	public List<Post> getPosts(List<String> tags,String search,Weaver writer,String sort, int page, int size) {
		return postDao.getPostsAsAdmin(tags, search, writer, sort,page,size);
	}



}
