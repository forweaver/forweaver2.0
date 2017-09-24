package com.forweaver.service;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.forweaver.domain.Weaver;
import com.forweaver.domain.git.statistics.GitParentStatistics;
import com.forweaver.domain.git.statistics.SvnParentStatistics;
import com.forweaver.domain.vc.VCFileInfo;
import com.forweaver.domain.vc.VCLog;
import com.forweaver.domain.vc.VCSimpleFileInfo;
import com.forweaver.domain.vc.VCSimpleLog;
import com.forweaver.domain.vc.VCSvnInfo;
import com.forweaver.util.GitInfo;
import com.forweaver.util.SVNUtil;
import com.forweaver.util.SvnInfo;

@Service
public class SVNService implements VCService{
	private static final Logger logger =
			LoggerFactory.getLogger(SVNUtil.class);
	
	@Autowired
	SVNUtil svnUtil;
	@Autowired
	WeaverService weaverService;
	
	
	public VCFileInfo getFileInfo(String parentDirctoryName, String repositoryName, String commitID, String filePath) {
		//사용자 정보 출력(세션)//
		Weaver weaver = weaverService.getCurrentWeaver();
		
		logger.debug("==> Session id: " + weaver.getUsername());
		logger.debug("==> Session password: " + weaver.getPassword());
		
		//프로젝트 초기화//
		svnUtil.RepoInt(parentDirctoryName, repositoryName);
		
		/*//인증정보를 설정//
		ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(userid, userpassword);
	    repository.setAuthenticationManager(authManager);*/
	        
	    //commitID가 Long으로 들어온다는 가정//
		commitID = "-1";
		//svnUtil.isDirectory(commitID, filePath);
		//저장소 리스트를 출력//
		VCFileInfo svnFileInfo = svnUtil.getFileInfo(commitID, filePath);
			
		return svnFileInfo;
	}

	public VCFileInfo getFileInfoWithBlame(String parentDirctoryName, String repositoryName, String commitID,
			String filePath) {
		logger.debug("projectDirectoryName: " + parentDirctoryName);
		logger.debug("repositoryName: " + repositoryName);
		logger.debug("commitID: " + commitID);
		logger.debug("filePath: " + filePath);
		
		svnUtil.RepoInt(parentDirctoryName,repositoryName);

		VCFileInfo svnFileInfo = svnUtil.getFileInfo(commitID, filePath);
		if(!svnFileInfo.isDirectory()){
			logger.debug("blame set");
			svnFileInfo.setBlames(svnUtil.getBlame(filePath, commitID));
		}
		return svnFileInfo;
	}

	public List<String> getBranchList(String parentDirctoryName, String repositoryName) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean existCommit(String parentDirctoryName, String repositoryName, String commit) {
		svnUtil.RepoInt(parentDirctoryName,repositoryName);
		try{
			if(svnUtil.getVCCommit(commit) == null)
				return false;
			else
				return true;
		}catch(Exception e){
			return false;
		}
	}

	public int getCommitListCount(String parentDirctoryName, String repositoryName, String commit) {
		//프로젝트 초기화//
		svnUtil.RepoInt(parentDirctoryName, repositoryName);
		
		return svnUtil.getCommitListCount(commit);
	}

	public List<VCSimpleFileInfo> getVCSimpleFileInfoList(String parentDirctoryName, String repositoryName,
			String commitID, String filePath) {
		//프로젝트 초기화//
		svnUtil.RepoInt(parentDirctoryName, repositoryName);
		
		//파일의 내용을 불러온다.//
		List<VCSimpleFileInfo> svnFileInfoList = svnUtil.getVCFileInfoList(commitID,filePath);
		
		return svnFileInfoList;
	}

	public List<VCSimpleLog> getVCCommitLogList(String parentDirctoryName, String repositoryName,
			String commitID, int page, int number) {
		List<VCSimpleLog> svnCommitLogList = svnUtil.getCommitLogList(commitID,page,number);
		
		return svnCommitLogList;
	}

	public VCLog getVCCommitLog(String parentDirctoryName, String repositoryName, String commitID) {
		//프로젝트 초기화//
		svnUtil.RepoInt(parentDirctoryName, repositoryName);
		
		VCLog gitCommitLog = svnUtil.getCommitLog(commitID);
		
		return gitCommitLog;
	}

	public SvnParentStatistics loadStatistics_svn(String parentDirctoryName, String repositoryName) {
		// TODO Auto-generated method stub
		svnUtil.RepoInt(parentDirctoryName,repositoryName);
		return svnUtil.getCommitStatistics();
	}

	public int[][] loadDayAndHour(String parentDirctoryName, String repositoryName) {
		svnUtil.RepoInt(parentDirctoryName,repositoryName);	
		return svnUtil.getDayAndHour();
	}

	public VCSvnInfo getSvnInfo(String parentDirctoryName,
			String repositoryName,String branchName){
		svnUtil.RepoInt(parentDirctoryName, repositoryName);

		return svnUtil.getSvnInfo(branchName);
	}

	public String getReadme(String creatorName, String projectName, String commit,
			List<VCSimpleFileInfo> svnFileInfoList) {
		String readme = "";
		if(svnFileInfoList != null){ 
			for(VCSimpleFileInfo svnSimpleFileInfo:svnFileInfoList){// 파일들을 검색해서 리드미 파일을 찾아냄
				if(svnSimpleFileInfo.getName().toUpperCase().contains("README.MD")){
					readme = getFileInfo(
							creatorName, 
							projectName, 
							commit, 
							"/"+svnSimpleFileInfo.getName()).getContent();
				}
			}
		}
		
		return readme;
	}

	public GitInfo getVCInfo(String parentDirctoryName, String repositoryName, String branchName) {
		// TODO Auto-generated method stub
		return null;
	}

	public GitParentStatistics loadStatistics(String parentDirctoryName, String repositoryName) {
		// TODO Auto-generated method stub
		return null;
	}

	public void doLockservice(String parentDirctoryName, String repositoryName, String lockfilePath){
		svnUtil.RepoInt(parentDirctoryName, repositoryName);
		svnUtil.dolock(lockfilePath);
	}
	
	public void doUnLockservice(String parentDirctoryName, String repositoryName, String lockfilePath){
		svnUtil.RepoInt(parentDirctoryName, repositoryName);
		svnUtil.dounlock(lockfilePath);
	}

	public void getRepositoryZip(String parentDirctoryName, String repositoryName, String commitName, String format,
			HttpServletResponse response) {
		svnUtil.RepoInt(parentDirctoryName,repositoryName);
		svnUtil.getRepositoryZip(commitName,format,response);
	}
}
