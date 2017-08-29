package com.forweaver.mongodb.dao;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.forweaver.domain.Invite;
import com.forweaver.service.CodeService;

/** 저장소 및 강의의 초대 관리 DAO
 *
 */
@Repository
public class InviteDao {

	private static final Logger logger =
			LoggerFactory.getLogger(InviteDao.class);
	
	@Autowired private MongoTemplate mongoTemplate;
	
	/** 초대 추가하기
	 * @param invate
	 */
	public void add(Invite invate) {
		mongoTemplate.insert(invate);
	}

	/** 초대 가져오기
	 * @param joinTeam
	 * @param waitingWeaver
	 * @return
	 */
	public Invite get(String joinTeam,String waitingWeaver) { 
		Query query = new Query(Criteria.where("joinTeam").is(joinTeam).and("waitingWeaver").is(waitingWeaver));
		return mongoTemplate.findOne(query, Invite.class);
	}
	
	/** 초대 삭제하기
	 * @param invate
	 */
	public void delete(Invite invate) {
		Query query = new Query(Criteria.where("joinTeam").is(invate.getJoinTeam()).and("waitingWeaver").is(invate.getWaitingWeaver()));
		mongoTemplate.remove(query, Invite.class);
	}
	
	/** 팀 이름으로 삭제.
	 * @param joinTeam
	 * @return
	 */
	public List<Invite> delete(String joinTeam) {
		Query query = new Query(Criteria.where("joinTeam").is(joinTeam));
		List<Invite> invates = mongoTemplate.find(query, Invite.class);
		mongoTemplate.remove(query, Invite.class);
		return invates;
	}
}
