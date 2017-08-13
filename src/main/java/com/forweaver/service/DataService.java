package com.forweaver.service;

import java.util.List;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.forweaver.domain.Data;
import com.forweaver.domain.Weaver;
import com.forweaver.mongodb.dao.DataDao;

@Service
public class DataService {

	@Autowired DataDao dataDao;
	@Autowired private CacheManager cacheManager;
	
	public Data get(String dataID) {
		Cache tmpCache = cacheManager.getCache("tmp");
		if(tmpCache.get(dataID) != null)
			return (Data) tmpCache.get(dataID).getValue();
		else 
			return dataDao.get(dataID);
	}
	
	public List<Data> gets(Weaver weaver) {
		return dataDao.gets(weaver);
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