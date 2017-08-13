package com.forweaver.mongodb.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.forweaver.domain.Code;
import com.forweaver.domain.Weaver;

/** 코드 서비스의 DAO
 *
 */
@Repository
public class CodeDao {
	
	@Autowired private MongoTemplate mongoTemplate;
	
	/** 코드 추가하기
	 * @param code
	 */
	public void insert(Code code) { 

		if (!mongoTemplate.collectionExists(Code.class)) {
			mongoTemplate.createCollection(Code.class);
			code.setCodeID(1);
			mongoTemplate.insert(code);
			return;
		}
		Code lastCode = getLast();
		if(lastCode == null)
			code.setCodeID(1);
		else
		code.setCodeID(lastCode.getCodeID() + 1);
		mongoTemplate.insert(code);
	}

	/** 코드 가져오기
	 * @param codeID
	 * @return
	 */
	public Code get(int codeID) {
		Query query = new Query(Criteria.where("_id").is(codeID));
		return mongoTemplate.findOne(query, Code.class);
	}
	
	/** 코드 가져오기
	 * @param code
	 */
	public void delete(Code code) {
		mongoTemplate.remove(code);
	}

	/** 코드 수정하기
	 * @param code
	 */
	public void update(Code code) {
		Query query = new Query(Criteria.where("_id").is(code.getCodeID()));
		Update update = new Update();
		update.set("content", code.getContent());
		update.set("downCount", code.getDownCount());
		update.set("readme", code.getReadme());
		update.set("tags", code.getTags());
		update.set("rePostCount", code.getRePostCount());
		update.set("recentReCodeDate", code.getRecentRePostDate());
		mongoTemplate.updateFirst(query, update, Code.class);
	}
	
	
	/** 검색한 코드의 갯수를 셈.
	 * @param tags
	 * @param search
	 * @param writer
	 * @param sort
	 * @return
	 */
	public long countCodes(
			Weaver writer,
			List<String> tags,
			String search,
			String sort) {
		Criteria criteria = new Criteria();
		
		if(search != null && search.length()>0)
			criteria.orOperator(new Criteria("name").regex(search),
					new Criteria("content").regex(search),
					new Criteria("readme").regex(search));
		
		if(tags != null)
			criteria.and("tags").all(tags);
		
		if(writer != null)
			criteria.and("writer").is(writer);
			
		this.filter(criteria, sort);

		return mongoTemplate.count(new Query(criteria), Code.class);
	}
	
	/** 코드를 검색함.
	 * @param tags
	 * @param search
	 * @param writer
	 * @param sort
	 * @param page
	 * @param size
	 * @return
	 */
	public List<Code> getCodes(
			List<String> tags,
			String search,
			Weaver writer,
			String sort,
			int page, 
			int size) {
		Criteria criteria = new Criteria();
		
		if(search != null && search.length()>0)
			criteria.orOperator(new Criteria("name").regex(search),
					new Criteria("content").regex(search),
					new Criteria("readme").regex(search));
		
		if(tags != null)
			criteria.and("tags").all(tags);
		if(writer != null)
			criteria.and("writer").is(writer);
		
		this.filter(criteria, sort);
		
		Query query = new Query(criteria);
		query.with(new PageRequest(page-1, size));

		this.sorting(query, sort);
		return mongoTemplate.find(query, Code.class);
	}
	
	/** 검색할 때 필터링함.
	 * @param criteria
	 * @param sort
	 */
	public void filter(Criteria criteria,String sort){
		if (sort.equals("download-desc")) {
			criteria.and("downCount").gt(0);
		} else if (sort.equals("repost-many")) {
			criteria.and("rePostCount").gt(0);
		} else if (sort.equals("repost-desc")) {
			criteria.and("rePostCount").gt(0);
		} else if (sort.equals("repost-null")) {
			criteria.and("rePostCount").is(0);
		}
	}
	
	/** 정렬 메서드
	 * @param query
	 * @param sort
	 */
	public void sorting(Query query,String sort){
		if (sort.equals("age-asc")) {
			query.with(new Sort(Sort.Direction.ASC, "_id"));
		} else if (sort.equals("download-desc")) {
			query.with(new Sort(Sort.Direction.DESC, "downCount"));
		} else if (sort.equals("repost-desc")) {
			query.with(new Sort(Sort.Direction.DESC, "recentRePostDate"));
		} else if (sort.equals("repost-many")) {
			query.with(new Sort(Sort.Direction.DESC, "rePostCount"));
		} else
			query.with(new Sort(Sort.Direction.DESC, "_id"));
	}

	/** 마지막 코드를 가져옴
	 * @return
	 */
	public Code getLast() {
		Query query = new Query().with(new Sort(Sort.Direction.DESC, "_id"));
		return mongoTemplate.findOne(query, Code.class);
	}
}
