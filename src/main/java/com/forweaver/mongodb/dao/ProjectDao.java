package com.forweaver.mongodb.dao;

import java.util.List;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.forweaver.domain.Project;

/** 프로젝트 관리를 위한 DAO
 *
 */
@Repository
public class ProjectDao {
	
	@Autowired private MongoTemplate mongoTemplate;
	
	/** 프로젝트를 생성 메서드
	 * @param project
	 */
	public void insert(Project project) {
		mongoTemplate.insert(project);
	}
	
	/** 프로젝트명으로 가져옴
	 * @param projectName
	 * @return
	 */
	public Project get(String projectName) {
		Query query = new Query(Criteria.where("_id").is(projectName));
		return mongoTemplate.findOne(query, Project.class);
	}
	
	/** 프로젝트 삭제
	 * @param project
	 */
	public void delete(Project project) {
		mongoTemplate.remove(project);
	}
	
	/** 프로젝트 수정하기
	 * @param project
	 */
	public void update(Project project) {
		Query query = new Query(Criteria.where("_id").is(project.getName()));
		Update update = new Update();
		update.set("category", project.getCategory());
		update.set("description", project.getDescription());
		update.set("tags", project.getTags());
		update.set("push", project.getPush());
		update.set("adminWeavers", project.getAdminWeavers());
		update.set("joinWeavers", project.getJoinWeavers());
		update.set("childProjects", project.getChildProjects());
		update.set("activeDate", project.getActiveDate());
		update.set("commitCount", project.getCommitCount());
		mongoTemplate.updateFirst(query, update, Project.class);
	}
	
	/** 프로젝트를 검색하고 수를 셈.
	 * @param tags
	 * @param search
	 * @param creator
	 * @param sort
	 * @return
	 */
	public long countProjects(
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

		return mongoTemplate.count(new Query(criteria), Project.class);
	}
	
	/** 프로젝트를 검색
	 * @param tags
	 * @param search
	 * @param creator
	 * @param sort
	 * @param page
	 * @param size
	 * @return
	 */
	public List<Project> getProjects( 
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
		return mongoTemplate.find(query, Project.class);
	}
	
	/** 특정 회원의 프로젝트를 가져올 때 수를 셈.
	 * @param tags
	 * @param search
	 * @param creator
	 * @param sort
	 * @return
	 */
	public long countProjects(
			List<String> projectNames,
			List<String> tags,
			String sort) {
		Criteria criteria = new Criteria("name").in(projectNames);	
		
		if(tags != null)
			criteria.and("tags").all(tags);
			
		this.filter(criteria, sort);

		return mongoTemplate.count(new Query(criteria), Project.class);
	}
	
	/** 특정 회원의 프로젝트를 가져올 때 활용.
	 * @param projectNames
	 * @param tags
	 * @param sort
	 * @param page
	 * @param size
	 * @return
	 */
	public List<Project> getProjects(
			List<String> projectNames,
			List<String> tags,
			String sort,
			int page, 
			int size) {
		Criteria criteria = new Criteria("name").in(projectNames);
		if(tags != null)
			criteria.and("tags").all(tags);
		this.filter(criteria, sort);
		Query query = new Query(criteria);
		query.with(new PageRequest(page-1, size));
		
		return mongoTemplate.find(query, Project.class);
	}
	
	
	
	/** 검색할 때 필터를 적용.
	 * @param criteria
	 * @param sort
	 */
	public void filter(Criteria criteria,String sort){
		if (sort.equals("public")) {
			criteria.and("category").is(0);
		}else if (sort.equals("homework")) {
			criteria.and("category").is(3);
		}else if (sort.equals("private")) {
			criteria.and("category").is(1);
		}
	}
	
	/** 검색할 때 정렬함.
	 * @param query
	 * @param sort
	 */
	public void sorting(Query query,String sort){
		if (sort.equals("age-asc")) 
			query.with(new Sort(Sort.Direction.ASC, "openingDate"));
		else
			query.with(new Sort(Sort.Direction.DESC, "openingDate"));
	}

}
