package com.forweaver.mongodb.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.forweaver.domain.Code;
import com.forweaver.domain.Post;
import com.forweaver.domain.RePost;
import com.forweaver.domain.Weaver;

/** 답변 관리를 위한 DAO
 *
 */
@Repository
public class RePostDao {
	
	@Autowired private MongoTemplate mongoTemplate;

	/** 답변 추가하기
	 * @param rePost
	 */
	public void insert(RePost rePost) {
		if (!mongoTemplate.collectionExists(RePost.class)) {
			rePost.setRePostID(1);
			mongoTemplate.insert(rePost);
			return;
		}
		RePost lastRePost = getLast();
		if(lastRePost == null)
			rePost.setRePostID(1);
		else
			rePost.setRePostID(lastRePost.getRePostID() + 1);
		mongoTemplate.insert(rePost);
	}

	/** 답변 가져오기
	 * @param ID
	 * @param kind
	 * @param sort
	 * @return
	 */
	public List<RePost> gets(int ID,int kind, String sort) {

		Criteria criteria = new Criteria().orOperator(
				new Criteria().where("originalPost.$id").is(ID).and("kind").is(kind),
				new Criteria().where("originalCode.$id").is(ID).and("kind").is(kind));

		this.filter(criteria, sort);

		Query query = new Query(criteria);
		this.sorting(query, sort);
		return mongoTemplate.find(query, RePost.class);
	}
	
	/** 글에 달린 답변 가져오기
	 * @param post
	 * @return
	 */
	public List<RePost> gets(Post post) {
		Criteria criteria = 
				new Criteria().and("originalPost.$id").is(post.getPostID());
		Query query = new Query(criteria);
		return mongoTemplate.find(query, RePost.class);
	}
	
	/** 코드에 달린 답변 가져오기
	 * @param code
	 * @return
	 */
	public List<RePost> gets(Code code) {
		Criteria criteria = 
				new Criteria().and("originalCode.$id").is(code.getCodeID());
		Query query = new Query(criteria);
		return mongoTemplate.find(query, RePost.class);
	}
	
	/** 자신이 올린 답변 가져오기
	 * @param weaver
	 * @return
	 */
	public List<RePost> gets(Weaver weaver) {
		Criteria criteria = 
				new Criteria().and("writer.$id").is(weaver.getId());
		Query query = new Query(criteria);
		return mongoTemplate.find(query, RePost.class);
	}
	
	/** 내가 댓글을 단 답변 목록
	 * @param weaver
	 * @return
	 */
	public List<RePost> getsAsReply(Weaver weaver) {
		Criteria criteria = 
				new Criteria().and("replys.writer.$id").is(weaver.getId());
		Query query = new Query(criteria);
		return mongoTemplate.find(query, RePost.class);
	}

	/** 답변 가져오기
	 * @param rePostID
	 * @return
	 */
	public RePost get(int rePostID) {
		Query query = new Query(Criteria.where("_id").is(rePostID));
		return mongoTemplate.findOne(query, RePost.class);
	}

	/** 답변을 삭제합니다.
	 * @param rePost
	 */
	public void delete(RePost rePost) {
		mongoTemplate.remove(rePost);
	}

	public void update(RePost rePost) {
		Query query = new Query(Criteria.where("_id").is(
				rePost.getRePostID()));
		Update update = new Update();
		update.set("content", rePost.getContent());
		update.set("push", rePost.getPush());
		update.set("replys", rePost.getReplys());
		update.set("datas", rePost.getDatas());
		update.set("recentReplyDate", rePost.getRecentReplyDate());
		mongoTemplate.updateFirst(query, update, RePost.class);
	}

	public RePost getLast() {
		Query query = new Query().with(new Sort(Sort.Direction.DESC, "_id"));
		return mongoTemplate.findOne(query, RePost.class);
	}

	/** 필터링 메서드
	 * @param criteria
	 * @param sort
	 */
	public void filter(Criteria criteria, String sort) {
		if (sort.equals("push-desc")) {
			criteria.and("push").gt(0);
		} else if (sort.equals("reply-many")) {
			criteria.and("replys").not().size(0); 
		} else if (sort.equals("reply-desc")) {
			criteria.and("replys").not().size(0); 
		}
	}

	/** 정렬하기 메서드
	 * @param query
	 * @param sort
	 */
	public void sorting(Query query, String sort) {
		if (sort.equals("age-asc")) {
			query.with(new Sort(Sort.Direction.ASC, "_id"));
		} else if (sort.equals("push-desc")) {
			query.with(new Sort(Sort.Direction.DESC, "push"));
		} else if (sort.equals("reply-many")) {
			query.with(new Sort(Sort.Direction.DESC, "replysCount"));
		} else if (sort.equals("reply-desc")) {
			query.with(new Sort(Sort.Direction.DESC, "recentReplyDate"));
		} else
			query.with(new Sort(Sort.Direction.DESC, "_id"));
	}
}
