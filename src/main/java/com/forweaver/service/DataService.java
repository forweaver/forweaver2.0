package com.forweaver.service;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.forweaver.domain.Data;
import com.forweaver.domain.Weaver;
import com.forweaver.mongodb.dao.DataDao;

@Service
public class DataService {

	private static final Logger logger =
			LoggerFactory.getLogger(DataService.class);
	
	@Autowired DataDao dataDao;
	@Autowired private CacheManager cacheManager;

	public Data get(String dataID) {
		Cache tmpCache = cacheManager.getCache("tmp");
		if(tmpCache.get(dataID) != null)
			return (Data) tmpCache.get(dataID).getValue();
		else 
			return dataDao.get(dataID);
	}

	public void delete(Data data) {
		dataDao.delete(data);
	}

	public void addTemp(Data data){
		Cache tmpCache = cacheManager.getCache("tmp");
		Cache tmpNameCache = cacheManager.getCache("tmpName");
		tmpCache.put(new Element(data.getId(), data));
		tmpNameCache.put(new Element(data.getWeaver().getId()+"/"+data.getName(), data.getId()));
	}

	public String getObjectID(String dataName,Weaver weaver){
		Cache tmpNameCache = cacheManager.getCache("tmpName");
		dataName = dataName.replace(" ", "_");
		dataName = dataName.replace("?", "_");
		dataName = dataName.replace("#", "_");
		dataName = dataName.trim();

		Element element = tmpNameCache.get(weaver.getId()+"/"+dataName);

		if(element == null)
			return "";

		return (String)element.getValue();
	}

}