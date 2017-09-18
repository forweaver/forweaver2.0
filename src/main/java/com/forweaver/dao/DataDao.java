package com.forweaver.dao;


import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.forweaver.domain.Data;
import com.mongodb.MongoException;

/** 자료 관리를 위한 DAO
 *
 */
@Repository
public class DataDao {

	private static final Logger logger =
			LoggerFactory.getLogger(DataDao.class);

	@Autowired 
	private MongoTemplate mongoTemplate;

	@Value("${data.repository.path}")
	private String dataPath;
	/** 자료 추가함.
	 * @param data
	 */
	public Data insert(Data data) {
		data.setFilePath(dataPath+data.getFilePath());
		try {
			mongoTemplate.insert(data);
			FileUtils.writeByteArrayToFile(
					new File(data.getFilePath()), data.getContent());
		}
		catch(MongoException e) {
			logger.error(e.getMessage());
			this.delete(data);
			return null;
		}catch(IOException e) {
			logger.error(e.getMessage());
			this.delete(data);
			return null;
		}
		return data;
	}

	/** 자료 가져오기
	 * @param dataID
	 * @return
	 */
	public Data get(String dataID) {
		Data data = null;
		try {
			Query query = new Query(Criteria.where("_id").is(dataID));
			data =  mongoTemplate.findOne(query, Data.class);
			data.setContent(FileUtils.readFileToByteArray(new File(data.getFilePath())));
		}
		catch(MongoException e) {
			logger.error(e.getMessage());
		}catch(IOException e) {
			logger.error(e.getMessage());
		}
		return data;
	}

	/** 자료 삭제하기
	 * @param data
	 */
	public boolean delete(Data data) {
		if(data == null)
			return false;
		try {
			mongoTemplate.remove(data);
			FileUtils.forceDelete(new File(data.getFilePath()));
		}
		catch(MongoException e) {
			logger.error(e.getMessage());
			return false;
		}catch(IOException e) {
			logger.error(e.getMessage());
			return false;
		}
		return true;
	}

}

