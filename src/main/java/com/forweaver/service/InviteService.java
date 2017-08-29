package com.forweaver.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.forweaver.domain.Repository;
import com.forweaver.domain.Invite;
import com.forweaver.domain.Weaver;
import com.forweaver.mongodb.dao.InviteDao;

/** 초대장 관리 서비스
 *
 */
@Service
public class InviteService {

	private static final Logger logger =
			LoggerFactory.getLogger(InviteService.class);
	
	@Autowired private InviteDao invateDao;

	/** 저장소 서비스와 관련하여 초대장을 생성 가능한지 검증함.
	 * @param repository
	 * @param waitingWeaver
	 * @param proposer
	 * @return
	 */
	public boolean isCreateWaitJoin(Repository repository,Weaver waitingWeaver,Weaver proposer) {
		if(		repository == null || waitingWeaver == null ||
				invateDao.get(repository.getName(),waitingWeaver.getId()) != null)
			return false;
		
		if(proposer.isAdmin(repository.getName())|| // 강의 관리자이거나
				(proposer.equals(waitingWeaver)&& // 아니면 본인이 신청해야함
				!repository.isJoinWeaver(waitingWeaver)) ) // 그리고 가입자가 아니어야함.
			return true;
		
		 return false;
	}

	/** 초대장 만듬.
	 * @param joinTeam
	 * @param proposer
	 * @param waitingWaver
	 * @param postID
	 */
	public void createWaitJoin(String joinTeam,String proposer,String waitingWaver,int postID){
		invateDao.add(new Invite(joinTeam, proposer, waitingWaver,postID));
	}


	/** 저장소의 초대장 삭제
	 * @param invate
	 * @param repository
	 * @param currentWeaver
	 * @return
	 */
	public boolean deleteWaitJoin(Invite invate,Repository repository,Weaver currentWeaver){
		if(invate == null || repository == null)
			return false;
		
		if(repository.getCreator().equals(currentWeaver) || //현재 위버가 저장소 관리자이거나
				invate.getWaitingWeaver().equals(currentWeaver.getId())){		//현재 위버가 대기중인 위버일때
			invateDao.delete(invate);			
			return true;
		}			
		
		return false;
	}

	/** 초대장 수락 가능한지 여부 검증
	 * @param invate
	 * @param adminWeaverName
	 * @param currentWeaver
	 * @return
	 */
	public boolean isOkJoin(Invite invate,String adminWeaverName,Weaver currentWeaver){
		if(invate == null)
			return false;

		if(invate.getProposer().equals(invate.getWaitingWeaver()) &&  //제안하는 사람과 대기중인 사람이 같고 현재 관리자가 평가할때
				!invate.getWaitingWeaver().equals(currentWeaver)&&
				currentWeaver.getId().equals(adminWeaverName))
			return true; //관리자가 승인하는 경우

		if(invate.getProposer().equals(adminWeaverName) && //가입자가 승인하는 경우
				invate.getWaitingWeaver().equals(currentWeaver.getId()))
			return true;
		
		return false;
	}


	/** 초대장 가져오기
	 * @param joinTeam
	 * @param waitingWeaver
	 * @return
	 */
	public Invite get(String joinTeam,String waitingWeaver){
		return invateDao.get(joinTeam,waitingWeaver);
	}
}
