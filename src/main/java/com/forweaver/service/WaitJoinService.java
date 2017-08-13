package com.forweaver.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.forweaver.domain.Project;
import com.forweaver.domain.ProjectInvite;
import com.forweaver.domain.Weaver;
import com.forweaver.mongodb.dao.PostDao;
import com.forweaver.mongodb.dao.ProjectInviteDao;

/** 초대장 관리 서비스
 *
 */
@Service
public class WaitJoinService {

	@Autowired private ProjectInviteDao waitJoinDao;

	/** 프로젝트 서비스와 관련하여 초대장을 생성 가능한지 검증함.
	 * @param project
	 * @param waitingWeaver
	 * @param proposer
	 * @return
	 */
	public boolean isCreateWaitJoin(Project project,Weaver waitingWeaver,Weaver proposer) {
		if(		project == null || waitingWeaver == null ||
				waitJoinDao.get(project.getName(),waitingWeaver.getId()) != null)
			return false;
		
		if(proposer.isAdmin(project.getName())|| // 강의 관리자이거나
				(proposer.equals(waitingWeaver)&& // 아니면 본인이 신청해야함
				!project.isProjectWeaver(waitingWeaver)) ) // 그리고 가입자가 아니어야함.
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
		waitJoinDao.add(new ProjectInvite(joinTeam, proposer, waitingWaver,postID));
	}


	/** 프로젝트의 초대장 삭제
	 * @param waitJoin
	 * @param project
	 * @param currentWeaver
	 * @return
	 */
	public boolean deleteWaitJoin(ProjectInvite waitJoin,Project project,Weaver currentWeaver){
		if(waitJoin == null || project == null)
			return false;
		
		if(project.getCreator().equals(currentWeaver) || //현재 위버가 프로젝트 관리자이거나
				waitJoin.getWaitingWeaver().equals(currentWeaver.getId())){		//현재 위버가 대기중인 위버일때
			waitJoinDao.delete(waitJoin);			
			return true;
		}			
		
		return false;
	}

	/** 초대장 수락 가능한지 여부 검증
	 * @param waitJoin
	 * @param adminWeaverName
	 * @param currentWeaver
	 * @return
	 */
	public boolean isOkJoin(ProjectInvite waitJoin,String adminWeaverName,Weaver currentWeaver){
		if(waitJoin == null)
			return false;

		if(waitJoin.getProposer().equals(waitJoin.getWaitingWeaver()) &&  //제안하는 사람과 대기중인 사람이 같고 현재 관리자가 평가할때
				!waitJoin.getWaitingWeaver().equals(currentWeaver)&&
				currentWeaver.getId().equals(adminWeaverName))
			return true; //관리자가 승인하는 경우

		if(waitJoin.getProposer().equals(adminWeaverName) && //가입자가 승인하는 경우
				waitJoin.getWaitingWeaver().equals(currentWeaver.getId()))
			return true;
		
		return false;
	}


	/** 초대장 가져오기
	 * @param joinTeam
	 * @param waitingWeaver
	 * @return
	 */
	public ProjectInvite get(String joinTeam,String waitingWeaver){
		return waitJoinDao.get(joinTeam,waitingWeaver);
	}
}
