package com.forweaver.domain.git.statistics;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SvnParentStatistics implements Serializable {
	
	static final long serialVersionUID = 32222213L;
	
	private List<SvnChildStatistics> svnChildStatistics; 
	private HashMap<String, SvnTotalStatistics> userHashMap; 

	public SvnParentStatistics() {
		super();
		this.svnChildStatistics = new ArrayList<SvnChildStatistics>();
		this.userHashMap = new HashMap<String, SvnTotalStatistics>();
	}

	public void addSvnChildStatistics(SvnChildStatistics gcs){
		if(svnChildStatistics.size() > 0){
			for(int i = 1 ; i <= svnChildStatistics.size() ; i++){
				SvnChildStatistics lastGCS = svnChildStatistics.get(svnChildStatistics.size()-i);

				if(lastGCS.getUserEmail().equals(gcs.getUserEmail()) && 
						lastGCS.getDate().equals(gcs.getDate())){
					gcs.setAddLine(gcs.getAddLine()+lastGCS.getAddLine());
					gcs.setDeleteLine(gcs.getDeleteLine()+lastGCS.getDeleteLine());
					svnChildStatistics.remove(svnChildStatistics.size()-i);
				}else if(!lastGCS.getDate().equals(gcs.getDate())){
					break;
				}
			}
		}
		svnChildStatistics.add(gcs);
		SvnTotalStatistics gts = this.userHashMap.get(gcs.getUserEmail());
		if(gts == null)
			this.userHashMap.put(gcs.getUserEmail(), 
					new SvnTotalStatistics(gcs.getAddLine(), gcs.getDeleteLine(),gcs.getAddFile(),gcs.getDeleteFile(), 1));
		else{
			gts.setTotalAdd(gts.getTotalAdd()+gcs.getAddLine());
			gts.setTotalAddFile(gts.getTotalAddFile()+gcs.getAddFile());
			gts.setTotalDelete(gts.getTotalDelete()+gcs.getDeleteLine());
			gts.setTotalDeleteFile(gts.getTotalDeleteFile()+gcs.getDeleteFile());
			gts.setTotalCommit(gts.getTotalCommit()+1);
		}
	}

	public List<SvnChildStatistics> getSvnChildStatistics() {
		return svnChildStatistics;
	}

	public void setSvnChildStatistics(
			List<SvnChildStatistics> gitChildrenStatistics) {
		this.svnChildStatistics = gitChildrenStatistics;
	}

	public HashMap<String, SvnTotalStatistics> getUserHashMap() {
		return userHashMap;
	}

	public void setUserHashMap(HashMap<String, SvnTotalStatistics> userHashMap) {
		this.userHashMap = userHashMap;
	}

	public List<String> getDates(){
		List<String> dates = new ArrayList<String>();
		dates.add(svnChildStatistics.get(0).getDate());
		
		for(SvnChildStatistics gcs:svnChildStatistics){
			if(!dates.get(dates.size()-1).equals(gcs.getDate()))
				dates.add(gcs.getDate());
		}
		return dates;
	}

}
