package com.forweaver.service;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import com.forweaver.domain.git.statistics.GitParentStatistics;
import com.forweaver.domain.vc.VCFileInfo;
import com.forweaver.domain.vc.VCLog;
import com.forweaver.domain.vc.VCSimpleFileInfo;
import com.forweaver.domain.vc.VCSimpleLog;
import com.forweaver.util.GitInfo;

public interface VCService {
	public VCFileInfo getFileInfo(String parentDirctoryName,String repositoryName,String commitID,String filePath);
	public VCFileInfo getFileInfoWithBlame(String parentDirctoryName,String repositoryName,String commitID,String filePath);
	public List<String> getBranchList(String parentDirctoryName,String repositoryName);
	public boolean existCommit(String parentDirctoryName,String repositoryName,String commit);
	public int getCommitListCount(String parentDirctoryName,String repositoryName,String commit);
	public List<VCSimpleFileInfo> getVCSimpleFileInfoList(String parentDirctoryName,String repositoryName,String commitID,String filePath);
	public List<VCSimpleLog> getVCCommitLogList(String parentDirctoryName,String repositoryName,String branchName,int page,int number) ;
	public VCLog getVCCommitLog(String parentDirctoryName,String repositoryName,String branchName) ;
	public void getProjectZip(String parentDirctoryName,String repositoryName,String commitName,String format,HttpServletResponse response);
	public GitParentStatistics loadStatistics(String parentDirctoryName,String repositoryName);
	public int[][] loadDayAndHour(String parentDirctoryName,String repositoryName);
	public GitInfo getVCInfo(String parentDirctoryName,String repositoryName,String branchName);
	public String getReadme(String creatorName,String projectName,String commit,List<VCSimpleFileInfo> gitFileInfoList);
}
