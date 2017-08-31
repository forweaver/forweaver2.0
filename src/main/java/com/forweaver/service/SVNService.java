package com.forweaver.service;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;

import com.forweaver.domain.Weaver;
import com.forweaver.domain.git.statistics.GitParentStatistics;
import com.forweaver.domain.vc.VCFileInfo;
import com.forweaver.domain.vc.VCLog;
import com.forweaver.domain.vc.VCSimpleFileInfo;
import com.forweaver.domain.vc.VCSimpleLog;
import com.forweaver.util.GitInfo;
import com.forweaver.util.SVNUtil;

public class SVNService implements VCService{
	@Autowired
	SVNUtil svnUtil;
	@Autowired
	WeaverService weaverService;
	
	
	public VCFileInfo getFileInfo(String parentDirctoryName, String repositoryName, String commitID, String filePath) {
		//사용자 정보 출력(세션)//
		Weaver weaver = weaverService.getCurrentWeaver();
		
		System.out.println("==> Session id: " + weaver.getUsername());
		System.out.println("==> Session password: " + weaver.getPassword());
		
		//프로젝트 초기화//
		svnUtil.RepoInt(parentDirctoryName, repositoryName);
		
		/*//인증정보를 설정//
		ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(userid, userpassword);
	    repository.setAuthenticationManager(authManager);*/
	        
	    //commitID가 Long으로 들어온다는 가정//
		commitID = "-1";
		//svnUtil.isDirectory(commitID, filePath);
		//저장소 리스트를 출력//
		VCFileInfo gitFileInfo = svnUtil.getFileInfo(commitID, filePath);
			
		return gitFileInfo;
	}

	public VCFileInfo getFileInfoWithBlame(String parentDirctoryName, String repositoryName, String commitID,
			String filePath) {
		System.out.println("projectDirectoryName: " + parentDirctoryName);
		System.out.println("repositoryName: " + repositoryName);
		System.out.println("commitID: " + commitID);
		System.out.println("filePath: " + filePath);
		
		svnUtil.RepoInt(parentDirctoryName,repositoryName);

		VCFileInfo svnFileInfo = svnUtil.getFileInfo(commitID, filePath);
		if(!svnFileInfo.isDirectory()){
			System.out.println("blame set");
			svnFileInfo.setBlames(svnUtil.getBlame(filePath, commitID));
		}
		return svnFileInfo;
	}

	public List<String> getBranchList(String parentDirctoryName, String repositoryName) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean existCommit(String parentDirctoryName, String repositoryName, String commit) {
		// TODO Auto-generated method stub
		return false;
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

	public void getProjectZip(String parentDirctoryName, String repositoryName, String commitName, String format,
			HttpServletResponse response) {
		// TODO Auto-generated method stub
		
	}

	public GitParentStatistics loadStatistics(String parentDirctoryName, String repositoryName) {
		// TODO Auto-generated method stub
		return null;
	}

	public int[][] loadDayAndHour(String parentDirctoryName, String repositoryName) {
		// TODO Auto-generated method stub
		return null;
	}

	public GitInfo getVCInfo(String parentDirctoryName, String repositoryName, String branchName) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getReadme(String creatorName, String projectName, String commit,
			List<VCSimpleFileInfo> svnFileInfoList) {
		String readme = "";
		if(svnFileInfoList != null) 
			for(VCSimpleFileInfo svnSimpleFileInfo:svnFileInfoList)// 파일들을 검색해서 리드미 파일을 찾아냄
				if(svnSimpleFileInfo.getName().toUpperCase().contains("README.md"))
					readme = getFileInfo(
							creatorName, 
							projectName, 
							commit, 
							"/"+svnSimpleFileInfo.getName()).getContent();
		
		System.out.println("readme info: " + readme);
		return readme;
	}

}
