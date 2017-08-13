package com.forweaver.mongodb.dao;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.forweaver.domain.Data;
import com.forweaver.domain.Weaver;

/** 자료 관리를 위한 DAO
 *
 */
@Repository
public class DataDao {

	@Autowired private MongoTemplate mongoTemplate;

	/** 자료 추가함.
	 * @param data
	 */
	public void insert(Data data) {
		if (!mongoTemplate.collectionExists(Data.class)) {
			mongoTemplate.createCollection(Data.class);
		}
		mongoTemplate.insert(data);
		return;
	}

	/** 자료 가져오기
	 * @param dataID
	 * @return
	 */
	public Data get(String dataID) {
		Query query = new Query(Criteria.where("_id").is(dataID));
		return mongoTemplate.findOne(query, Data.class);
	}
	
	/** 위버가 올린 자료를 다 가져옴
	 * @param weaver
	 * @return
	 */
	public List<Data> gets(Weaver weaver) {
		Query query = new Query(Criteria.where("weaver.$id").is(weaver.getId()));
		return mongoTemplate.find(query, Data.class);
	}


	/** 자료 삭제하기
	 * @param data
	 */
	public void delete(Data data) {
		if(data == null)
			return;
		mongoTemplate.remove(data);
	}

	/** 마지막 자료 가져오기
	 * @return
	 */
	public Data getLast() {
		Query query = new Query().with(new Sort(Sort.Direction.DESC, "_id"));
		return mongoTemplate.findOne(query, Data.class);
	}
}

