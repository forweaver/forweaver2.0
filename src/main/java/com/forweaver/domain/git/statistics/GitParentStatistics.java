package com.forweaver.domain.git.statistics;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

// GitChildStatistics를 직접 관리하는 클래스
public class GitParentStatistics implements Serializable {
	
	static final long serialVersionUID = 32222213L;
	
	private List<GitChildStatistics> gitChildStatistics; 
	private HashMap<String, GitTotalStatistics> userHashMap; 

	public GitParentStatistics() {
		super();
		this.gitChildStatistics = new ArrayList<GitChildStatistics>();
		this.userHashMap = new HashMap<String, GitTotalStatistics>();
	}

	public void addGitChildStatistics(GitChildStatistics gcs){
		if(gitChildStatistics.size() > 0){
			for(int i = 1 ; i <= gitChildStatistics.size() ; i++){
				GitChildStatistics lastGCS = gitChildStatistics.get(gitChildStatistics.size()-i);

				if(lastGCS.getUserEmail().equals(gcs.getUserEmail()) && 
						lastGCS.getDate().equals(gcs.getDate())){
					gcs.setAddLine(gcs.getAddLine()+lastGCS.getAddLine());
					gcs.setDeleteLine(gcs.getDeleteLine()+lastGCS.getDeleteLine());
					gitChildStatistics.remove(gitChildStatistics.size()-i);
				}else if(!lastGCS.getDate().equals(gcs.getDate())){
					break;
				}
			}
		}
		gitChildStatistics.add(gcs);
		GitTotalStatistics gts = this.userHashMap.get(gcs.getUserEmail());
		if(gts == null)
			this.userHashMap.put(gcs.getUserEmail(), 
					new GitTotalStatistics(gcs.getAddLine(), gcs.getDeleteLine(),gcs.getAddFile(),gcs.getDeleteFile(), 1));
		else{
			gts.setTotalAdd(gts.getTotalAdd()+gcs.getAddLine());
			gts.setTotalAddFile(gts.getTotalAddFile()+gcs.getAddFile());
			gts.setTotalDelete(gts.getTotalDelete()+gcs.getDeleteLine());
			gts.setTotalDeleteFile(gts.getTotalDeleteFile()+gcs.getDeleteFile());
			gts.setTotalCommit(gts.getTotalCommit()+1);
		}
	}

	public List<GitChildStatistics> getGitChildStatistics() {
		return gitChildStatistics;
	}

	public void setGitChildStatistics(
			List<GitChildStatistics> gitChildrenStatistics) {
		this.gitChildStatistics = gitChildrenStatistics;
	}

	public HashMap<String, GitTotalStatistics> getUserHashMap() {
		return userHashMap;
	}

	public void setUserHashMap(HashMap<String, GitTotalStatistics> userHashMap) {
		this.userHashMap = userHashMap;
	}

	public List<String> getDates(){
		List<String> dates = new ArrayList<String>();
		dates.add(gitChildStatistics.get(0).getDate());
		
		for(GitChildStatistics gcs:gitChildStatistics){
			if(!dates.get(dates.size()-1).equals(gcs.getDate()))
				dates.add(gcs.getDate());
		}
		return dates;
	}

}
