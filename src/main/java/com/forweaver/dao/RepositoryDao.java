package com.forweaver.dao;

import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import com.forweaver.domain.Repository;
import com.forweaver.service.CodeService;

/** 저장소 관리를 위한 DAO
 *
 */
@org.springframework.stereotype.Repository
public class RepositoryDao {
	
	private static final Logger logger =
			LoggerFactory.getLogger(CodeService.class);
	
	@Autowired private MongoTemplate mongoTemplate;
	
	/** 저장소를 생성 메서드
	 * @param repository
	 */
	public void insert(Repository repository) {
		mongoTemplate.insert(repository);
	}
	
	/** 저장소명으로 가져옴
	 * @param repositoryName
	 * @return
	 */
	public Repository get(String repositoryName) {
		logger.debug("==> repository name: " + repositoryName);
		Query query = new Query(Criteria.where("_id").is(repositoryName));
		return mongoTemplate.findOne(query, Repository.class);
	}
	
	/** 저장소 삭제
	 * @param repository
	 */
	public void delete(Repository repository) {
		mongoTemplate.remove(repository);
	}
	
	/** 저장소 수정하기
	 * @param repository
	 */
	public void update(com.forweaver.domain.Repository repository) {
		Query query = new Query(Criteria.where("_id").is(repository.getName()));
		Update update = new Update();
		update.set("authLevel", repository.getAuthLevel());
		update.set("description", repository.getDescription());
		update.set("tags", repository.getTags());
		update.set("push", repository.getPush());
		update.set("adminWeavers", repository.getAdminWeavers());
		update.set("joinWeavers", repository.getJoinWeavers());
		mongoTemplate.updateFirst(query, update, Repository.class);
	}
	
	/** 저장소를 검색하고 수를 셈.
	 * @param tags
	 * @param search
	 * @param creator
	 * @param sort
	 * @return
	 */
	public long countRepositories(
			List<String> tags,
			String search,
			String sort) {
		Criteria criteria = new Criteria();
		
		if(search != null && search.length()>0)
			criteria.orOperator(new Criteria("name").regex(search),
					new Criteria("description").regex(search));
		
		if(tags != null)
			criteria.and("tags").all(tags);
			
		this.filter(criteria, sort);

		return mongoTemplate.count(new Query(criteria), Repository.class);
	}
	
	/** 저장소를 검색
	 * @param tags
	 * @param search
	 * @param creator
	 * @param sort
	 * @param page
	 * @param size
	 * @return
	 */
	public List<Repository> getRepositories( 
			List<String> tags,
			String search,
			String sort,
			int page, 
			int size) {
		Criteria criteria = new Criteria();
		
		if(search != null && search.length()>0)
			criteria.orOperator(new Criteria("name").regex(search),
					new Criteria("description").regex(search));
		
		if(tags != null)
			criteria.and("tags").all(tags);
		
		this.filter(criteria, sort);
		
		Query query = new Query(criteria);
		query.with(new PageRequest(page-1, size));

		this.sorting(query, sort);
		return mongoTemplate.find(query, Repository.class);
	}
	
	/** 특정 회원의 저장소를 가져올 때 수를 셈.
	 * @param tags
	 * @param search
	 * @param creator
	 * @param sort
	 * @return
	 */
	public long countRepositories(
			List<String> repositoryNames,
			List<String> tags,
			String sort) {
		Criteria criteria = new Criteria("name").in(repositoryNames);	
		
		if(tags != null)
			criteria.and("tags").all(tags);
			
		this.filter(criteria, sort);

		return mongoTemplate.count(new Query(criteria), Repository.class);
	}
	
	/** 특정 회원의 저장소를 가져올 때 활용.
	 * @param repositoryNames
	 * @param tags
	 * @param sort
	 * @param page
	 * @param size
	 * @return
	 */
	public List<Repository> getRepositories(
			List<String> repositoryNames,
			List<String> tags,
			String sort,
			int page, 
			int size) {
		Criteria criteria = new Criteria("name").in(repositoryNames);
		if(tags != null)
			criteria.and("tags").all(tags);
		this.filter(criteria, sort);
		Query query = new Query(criteria);
		query.with(new PageRequest(page-1, size));
		
		return mongoTemplate.find(query, Repository.class);
	}
	
	
	
	/** 검색할 때 필터를 적용.
	 * @param criteria
	 * @param sort
	 */
	public void filter(Criteria criteria,String sort){
		if (sort.equals("public")) {
			criteria.and("authLevel").is(0);
		}else if (sort.equals("homework")) {
			criteria.and("authLevel").is(3);
		}else if (sort.equals("private")) {
			criteria.and("authLevel").is(1);
		}
	}
	
	/** 검색할 때 정렬함.
	 * @param query
	 * @param sort
	 */
	public void sorting(Query query,String sort){
		if (sort.equals("age-asc")) 
			query.with(new Sort(Sort.Direction.ASC, "date"));
		else
			query.with(new Sort(Sort.Direction.DESC, "date"));
	}

}
