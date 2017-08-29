package com.forweaver.mongodb.dao;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.forweaver.domain.Weaver;
import com.forweaver.service.CodeService;
import com.mongodb.DBObject;

/** 회원(Weaver) 관리를 위한 DAO
 *
 */
@Repository
public class WeaverDao {
	
	private static final Logger logger =
			LoggerFactory.getLogger(WeaverDao.class);
	
	@Autowired 
	private MongoTemplate mongoTemplate;
	
	/**  회원 존재 여부
	 * @return 존재 여부
	 */
	public boolean existsWeaver() {
		return mongoTemplate.collectionExists(Weaver.class);
	}
	
	/** 회원 추가하기
	 * @param weaver
	 */
	public void insert(Weaver weaver) {
		mongoTemplate.insert(weaver);
	}

	/** 회원 정보 갖고오기
	 * @param id
	 * @return 회원
	 */
	public Weaver get(String id) {
		Query query = new Query(new Criteria()	.orOperator(Criteria.where("_id").is(id),
				Criteria.where("email").is(id)));
		return mongoTemplate.findOne(query,Weaver.class);
	}


	
	/** 회원 정보 리스트를 가져옴
	 * @param page
	 * @param size
	 * @return 회원 목록
	 */
	public List<Weaver> getWeavers(int page, int size) {
		Criteria criteria = new Criteria().and("isLeave").is(false);
		Query query = new Query(criteria);
		query.with(new PageRequest(page - 1, size));
		return mongoTemplate.find(query, Weaver.class);
	}
	
	/** 회원 정보를 셈.
	 * @return 회원수
	 */
	public long countWeavers() {
		Criteria criteria = new Criteria().and("isLeave").is(false);
		return mongoTemplate.count(new Query(criteria), Weaver.class);
	}
	
	/** 태그를 이용하여 위버를 검색함.
	 * @param tags
	 * @param page
	 * @param size
	 * @return 회원 목록
	 */
	public List<Weaver> getWeavers(List<String> tags, int page,int size) {
		Criteria criteria = new Criteria("tags").all(tags).and("isLeave").is(false);

		Query query = new Query(criteria);
		query.with(new PageRequest(page - 1, size));

		return mongoTemplate.find(query, Weaver.class);
	}
	/** 태그를 이용하여 위버를 검색하고 숫자를 셈.
	 * @param tags
	 * @param page
	 * @param size
	 * @return 회원수
	 */
	public long countWeavers(List<String> tags) {
		Criteria criteria = new Criteria("tags").all(tags).and("isLeave").is(false);
		return mongoTemplate.count(new Query(criteria), Weaver.class);
	}

	/** 회원 삭제
	 * @param weaver
	 */
	public void delete(Weaver weaver) {
		mongoTemplate.remove(weaver);
	}

	/** 저장소에 가입한 회원들 가져오기
	 * @param passName
	 * @return 회원 목록
	 */
	public List<Weaver> searchPassName(String passName) { //특정 패스의 회원들을 검색
		Query query = new Query(Criteria.where("passes").in(passName).and("isLeave").is(false));
		return mongoTemplate.find(query, Weaver.class);
	}

	/** 회원 정보 수정
	 * @param weaver
	 */
	public void update(Weaver weaver) {
		Query query = new Query(Criteria.where("_id").is(weaver.getId()));
		Update update = new Update();
		update.set("password", weaver.getPassword());
		update.set("data", weaver.getData());
		update.set("say", weaver.getSay());
		update.set("isLeave", weaver.isLeave());
		mongoTemplate.updateFirst(query, update, Weaver.class);     
	}
	
	/** 회원 권한 수정
	 * @param weaver
	 */
	public void updatePass(Weaver weaver) {
		Query query = new Query(Criteria.where("_id").is(weaver.getId()));
		Update update = new Update();
		update.set("passes", weaver.getPasses());
		mongoTemplate.updateFirst(query, update, Weaver.class);     
	}

	
	/** 회원의 게시글 정보를 aggregation을 활용하여 가져옴
	 * @param weaver
	 * @return 회원의 커뮤니티 활동 내역
	 */
	public DBObject getWeaverInfosInPost(Weaver weaver){
		Criteria criteria = 	Criteria.where("writer.$id").is(weaver.getId());
		AggregationOperation match = Aggregation.match(criteria);
		
		AggregationOperation group = Aggregation. group("writer").count().as("postCount").sum("push").as("push").sum("rePostCount").as("rePostCount");
		Aggregation agg = newAggregation(match, group);

		return mongoTemplate.aggregate(agg, "post", DBObject.class).getUniqueMappedResult();
	}
	
	/** 회원의 답변 정보를 aggregation을 활용하여 가져옴
	 * @param weaver
	 * @return 회원의 답변 활동 내역
	 */
	public DBObject getWeaverInfosInRePost(Weaver weaver){
		Criteria criteria = 	Criteria.where("writer.$id").is(weaver.getId());
		AggregationOperation match = Aggregation.match(criteria);
		
		AggregationOperation group = Aggregation. group("writer").count().as("myRePostCount").sum("push").as("rePostPush").push("replys").as("replys");
		Aggregation agg = newAggregation(match, group);

		return mongoTemplate.aggregate(agg, "rePost", DBObject.class).getUniqueMappedResult();
	}
	
	/** 회원의 저장소 정보를 aggregation을 활용하여 가져옴
	 * @param weaver
	 * @return 회원의 저장소 활동 내역
	 */
	public DBObject getWeaverInfosInRepository(Weaver weaver){
		Criteria criteria = 	Criteria.where("creator.$id").is(weaver.getId());
		AggregationOperation match = Aggregation.match(criteria);
		
		AggregationOperation group = Aggregation.group("creator").sum("push").as("repositoryPush");
		Aggregation agg = newAggregation(match, group);

		return mongoTemplate.aggregate(agg, "repository", DBObject.class).getUniqueMappedResult();
	}
	
	/** 회원의 강의 정보를 aggregation을 활용하여 가져옴
	 * @param weaver
	 * @return 회원의 강의 활동 내역
	 */
	public DBObject getWeaverInfosInLecture(Weaver weaver){
		Criteria criteria = 	Criteria.where("creator.$id").is(weaver.getId());
		AggregationOperation match = Aggregation.match(criteria);
		
		AggregationOperation group = Aggregation. group("creator").push("joinWeavers").as("joinWeavers").push("repos").as("repos");
		Aggregation agg = newAggregation(match, group);

		return mongoTemplate.aggregate(agg, "lecture", DBObject.class).getUniqueMappedResult();
	}
	
	/** 회원의 코드 정보를 aggregation을 활용하여 가져옴
	 * @param weaver
	 * @return 회원의 코드 활동 내역
	 */
	public DBObject getWeaverInfosInCode(Weaver weaver){
		Criteria criteria = 	Criteria.where("writer.$id").is(weaver.getId());
		AggregationOperation match = Aggregation.match(criteria);
		
		AggregationOperation group = Aggregation. group("writer").count().as("codeCount").sum("downCount").as("downCount");
		Aggregation agg = newAggregation(match, group);

		return mongoTemplate.aggregate(agg, "code", DBObject.class).getUniqueMappedResult();
	}
	
	

}
