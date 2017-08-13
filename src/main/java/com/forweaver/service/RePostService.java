package com.forweaver.service;

import java.util.List;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.forweaver.domain.Code;
import com.forweaver.domain.Data;
import com.forweaver.domain.Post;
import com.forweaver.domain.RePost;
import com.forweaver.domain.Reply;
import com.forweaver.domain.Weaver;
import com.forweaver.mongodb.dao.CodeDao;
import com.forweaver.mongodb.dao.DataDao;
import com.forweaver.mongodb.dao.PostDao;
import com.forweaver.mongodb.dao.RePostDao;
import com.forweaver.mongodb.dao.WeaverDao;

/** 답변 관리 서비스
 *
 */
@Service
public class RePostService {
	@Autowired private RePostDao rePostDao;
	@Autowired private PostDao postDao;
	@Autowired private DataDao dataDao;
	@Autowired private CodeDao codeDao;
	@Autowired private WeaverDao weaverDao;
	@Autowired private CacheManager cacheManager;

	/** 답변을 추가함.
	 * @param rePost
	 * @param datas
	 * @return
	 */
	public boolean add(RePost rePost,List<Data> datas) {
		if(rePost == null)
			return false;
		if(datas != null)
			for(Data data:datas){
				dataDao.insert(data);
				rePost.addData(data);
			}

		weaverDao.update(rePost.getWriter());
		rePostDao.insert(rePost);
		return true;
	}


	/** 댓글을 추가함.
	 * @param rePost
	 * @param reply
	 * @return
	 */
	public boolean addReply(RePost rePost,Reply reply) {
		if(rePost == null || reply == null || reply.getContent() == null)
			return false;
		rePost.addReply(reply);
		rePostDao.update(rePost);
		return true;
	}

	/** 댓글을 삭제함.
	 * @param rePost
	 * @param reply
	 * @return
	 */
	public boolean deleteReply(RePost rePost,Weaver weaver,int number) {

		if(rePost == null || weaver == null)
			return false;

		Weaver replyWriter = rePost.getReplyWriter(number);

		if(replyWriter == null || !replyWriter.equals(weaver))
			return false;

		if(!rePost.removeReply(weaver, number))
			return false;

		rePostDao.update(rePost);
		return true;
	}

	/** 댓글을 전부 삭제함.
	 * @param rePost
	 * @param reply
	 * @return
	 */
	public boolean deleteAllReply(Weaver currentWeaver,Weaver weaver) {

		if(weaver == null || currentWeaver == null)
			return false;

		if(currentWeaver.isAdmin() || weaver.equals(currentWeaver)){
			for(RePost rePost:rePostDao.getsAsReply(weaver)){
				rePost.deleteAllReply(weaver);
				rePostDao.update(rePost);
			}
			return true;
		}
		return false;
	}

	/** 답변들을 가져옴
	 * @param ID
	 * @param kind
	 * @param sort
	 * @return
	 */
	public List<RePost> gets(int ID,int kind,String sort) {
		return rePostDao.gets(ID,kind,sort);
	}
	
	public List<RePost> gets(Weaver weaver) {
		return rePostDao.gets(weaver);
	}

	public RePost get(int rePostID) {
		return rePostDao.get(rePostID);
	}
	


	/** 답변을 추천하면 캐시에 저장하고 24시간 제한을 둠.
	 * @param rePost
	 * @param weaver
	 * @param ip
	 * @return
	 */
	public boolean push(RePost rePost, Weaver weaver,String ip) {
		if(rePost == null || (weaver != null && weaver.equals(rePost.getWriter())))
			return false;
		rePost.push();
		Cache cache = cacheManager.getCache("push");
		Element element = cache.get("re"+rePost.getRePostID()+"@@"+ip);
		if (element == null || (element != null && element.getValue() == null)) {
			rePostDao.update(rePost);
			Element newElement = new Element("re"+rePost.getRePostID()+"@@"+ip, ip);
			cache.put(newElement);
			weaverDao.update(rePost.getWriter());
			return true;
		}
		return false;
	}

	public boolean update(RePost rePost,List<Data> datas,String[] removeDataList){
		if(rePost == null)
			return false;

		//만약 자료를 올렸다면.
		if(datas != null && datas.size() >0)
			for(Data data:datas){
				dataDao.insert(data);
				rePost.addData(dataDao.getLast());
			}

		if(removeDataList != null)
			for(String dataID: removeDataList){
				dataDao.delete(rePost.getData(dataID));
				rePost.deleteData(dataID);
			}
		rePostDao.update(rePost);
		return true;
	}

	/** 글에 쓴 답변을 삭제할 때
	 * @param post
	 * @param rePost
	 * @param weaver
	 * @return
	 */
	public boolean delete(Post post,RePost rePost,Weaver weaver){

		if(post == null ||rePost == null || weaver == null)
			return false;

		if(rePost.getWriterName().equals(weaver.getId()) 
				||  weaver.isAdmin()){

			post.rePostCountDown();
			postDao.update(post);
			this.delete(rePost);
			return true;
		}
		return false;
	}

	/** 코드에 단 답변을 삭제할 때
	 * @param code
	 * @param rePost
	 * @param weaver
	 * @return
	 */
	public boolean delete(Code code,RePost rePost,Weaver weaver){

		if(code == null ||rePost == null || weaver == null)
			return false;

		if(rePost.getWriterName().equals(weaver.getId()) ||  weaver.isAdmin()){

			code.rePostCountDown();
			codeDao.update(code);
			this.delete(rePost);
			return true;
		}
		return false;
	}
	

	/** 그냥 답변을 삭제함.
	 * @param rePost
	 */
	public void delete(RePost rePost){
		
		for(Data data:rePost.getDatas())
			dataDao.delete(data);
		
		rePostDao.delete(rePost);
	}

}