package com.forweaver.mongodb.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.forweaver.domain.ProjectInvite;

/** 프로젝트 및 강의의 초대 관리 DAO
 *
 */
@Repository
public class ProjectInviteDao {

	@Autowired private MongoTemplate mongoTemplate;
	
	/** 초대 추가하기
	 * @param waitJoin
	 */
	public void add(ProjectInvite waitJoin) {
		mongoTemplate.insert(waitJoin);
	}

	/** 초대 가져오기
	 * @param joinTeam
	 * @param waitingWeaver
	 * @return
	 */
	public ProjectInvite get(String joinTeam,String waitingWeaver) { 
		Query query = new Query(Criteria.where("joinTeam").is(joinTeam).and("waitingWeaver").is(waitingWeaver));
		return mongoTemplate.findOne(query, ProjectInvite.class);
	}
	
	/** 초대 삭제하기
	 * @param waitJoin
	 */
	public void delete(ProjectInvite waitJoin) {
		Query query = new Query(Criteria.where("joinTeam").is(waitJoin.getJoinTeam()).and("waitingWeaver").is(waitJoin.getWaitingWeaver()));
		mongoTemplate.remove(query, ProjectInvite.class);
	}
	
	/** 팀 이름으로 삭제.
	 * @param joinTeam
	 * @return
	 */
	public List<ProjectInvite> delete(String joinTeam) {
		Query query = new Query(Criteria.where("joinTeam").is(joinTeam));
		List<ProjectInvite> waitJoins = mongoTemplate.find(query, ProjectInvite.class);
		mongoTemplate.remove(query, ProjectInvite.class);
		return waitJoins;
	}
}
