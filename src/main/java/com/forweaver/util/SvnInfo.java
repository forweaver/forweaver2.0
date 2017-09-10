package com.forweaver.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.io.SVNRepository;

import com.forweaver.domain.Weaver;
import com.forweaver.service.WeaverService;

public class SvnInfo {
	private static final Logger logger =
			LoggerFactory.getLogger(SvnInfo.class);
	
	public void run(SVNRepository repository, String start) throws IOException {
		try {
			logger.debug("==> repository lastest revesion: " + repository.getLatestRevision());
			logger.debug("==> repository address: " + repository.getRepositoryRoot(false).getPath());
			logger.debug("==> info revesion: " + start);
			
			//SVN의 여러가지 서비스들을 호출//
			//1. 커밋터 정보//
			Map<String, Object> commitinfo = doPrintRepoLog(repository); //전체 커밋정보 리스트//
			
			@SuppressWarnings("unchecked")
			List<Object> authorlist = (List<Object>) commitinfo.get("authorlist");
		
			for(int i=0; i<authorlist.size(); i++){
				logger.debug("author: " + authorlist.get(i));
			}
			//해당 정보를 통합저장 클래스에 저장//
			
			//2. 날짜 정보//
			List<Object> datelist = (List<Object>) commitinfo.get("datelist");
			
			for(int i=0; i<datelist.size(); i++){
				logger.debug("date: " + datelist.get(i));
			}
			
			//3. 커밋수(날짜별 커밋수)
			List<HashMap<String, Object>> datecommitinfo = doPrintHitLogDate(repository, commitinfo);
			
			for(int i=0; i<datecommitinfo.size(); i++){
				logger.debug("date: " + datecommitinfo.get(i).get("date["+i+"]") + ", hitcount: " + datecommitinfo.get(i).get("logcount["+i+"]"));
			}
			
			//4. 
			
		} catch (SVNException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Map<String, Object> doPrintRepoLog(SVNRepository repository){
		//SVNRepository repository = null;
		Collection logEntries = null;
		
		//媛� 濡쒓렇�뿉�꽌 �븘�슂�븳 �뜲�씠�꽣瑜� ���옣�븷 �닔 �엳�뒗 �옄猷뚭뎄議� �꽑�뼵//
		List<Object>revesionlist = new ArrayList<Object>();
		List<Object>authorlist = new ArrayList<Object>();
		List<Object>datelist = new ArrayList<Object>();
		List<Object>logmessagelist = new ArrayList<Object>();
		List<Object>changepathlist = new ArrayList<Object>();
		
		//醫낇빀�젙蹂대�� 媛�吏� 由ъ뒪�듃//
		Map<String, Object>loglist = new HashMap<String, Object>();
		
		long startRevision = 0;
		long endRevision = -1; //HEAD (the latest) revision
		
		int logcount = 0; //key濡� �솢�슜//
		
		StringBuffer str_paths_buffer = new StringBuffer();
		
		try {
			//repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(repourl));
			
			/*//인증정보를 설정//
			ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(userid, userpassword);
	        repository.setAuthenticationManager(authManager);*/
	       
			logEntries = repository.log(new String[] { "" }, null, startRevision, endRevision, true, true);

			for (Iterator entries = logEntries.iterator(); entries.hasNext();) {
				SVNLogEntry logEntry = (SVNLogEntry) entries.next();
	
				revesionlist.add(logEntry.getRevision());
				authorlist.add(logEntry.getAuthor());
				datelist.add(logEntry.getDate().toString());
				logmessagelist.add(logEntry.getMessage());

				if (logEntry.getChangedPaths().size() > 0){
					Set changedPathsSet = logEntry.getChangedPaths().keySet();
					for (Iterator changedPaths = changedPathsSet.iterator(); changedPaths.hasNext();) {
						SVNLogEntryPath entryPath = (SVNLogEntryPath) logEntry.getChangedPaths().get(changedPaths.next());

						str_paths_buffer.append(entryPath.getType());
						str_paths_buffer.append(" ");
						str_paths_buffer.append(entryPath.getPath());
						str_paths_buffer.append(" ");
						str_paths_buffer.append(" ");
						str_paths_buffer.append((entryPath.getCopyPath() != null)
								? " (from " + entryPath.getCopyPath() + " revision " + entryPath.getCopyRevision() + ")"
								: "");
						str_paths_buffer.append("\n");
					}

					changepathlist.add(str_paths_buffer.toString());

					str_paths_buffer.setLength(0); // 珥덇린�솕//
				}
				
				logcount++;
			}
			
			loglist.put("revesionlist", revesionlist);
			loglist.put("authorlist", authorlist);
			loglist.put("datelist", datelist);
			loglist.put("logmessagelist", logmessagelist);
			loglist.put("changepathlist", changepathlist);
			loglist.put("count", ""+logcount);
			
			return loglist;
		}
		catch (SVNException e) {
			e.printStackTrace();
		}
		
		return loglist;
	}
	
	public List<HashMap<String, Object>> doPrintHitLogDate(SVNRepository repository, Map<String, Object> commitinfo){
		//SVNRepository repository = null;
		Collection logEntries = null;
		
		//醫낇빀�젙蹂대�� 媛�吏� 由ъ뒪�듃//
		//List<Object> authorlist = (List<Object>) commitinfo.get("authorlist");
		List<Object> datelist = (List<Object>) commitinfo.get("datelist");
		
		List<HashMap<String, Object>>loghitinfolist = new ArrayList<HashMap<String, Object>>();
		
		HashMap<String, Object>loglist = new HashMap<String, Object>();
		
		long startRevision = 0;
		long endRevision = -1; //HEAD (the latest) revision
		
		int logcount = 0; //key濡� �솢�슜//
		
		try {
			//repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(repourl));
			
			/*//인증정보를 설정//
			ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(userid, userpassword);
	        repository.setAuthenticationManager(authManager);*/
	       
			logEntries = repository.log(new String[] { "" }, null, startRevision, endRevision, true, true);

			//전체 횟수는 모든 커밋에 대한 날짜정보를 기반//
			for(int i=0; i<datelist.size(); i++){
				//년,월,일을 비교하기 위해서 파싱//
				String datearray[] = datelist.get(i).toString().split(" ");
				logger.debug("ori date: " + datearray[0]+","+datearray[1]+","+datearray[2]);
				//키값을 대입//
				loglist.put("date["+i+"]", datelist.get(i).toString());
				
				for (Iterator entries = logEntries.iterator(); entries.hasNext();) {
					SVNLogEntry logEntry = (SVNLogEntry) entries.next();
					
					//년,월,일을 비교하기 위해서 파싱//
					String comparedatearray[] = logEntry.getDate().toString().split(" ");
					logger.debug("comp date: " + comparedatearray[0]+","+comparedatearray[1]+","+comparedatearray[2]);
					//날짜(년,월,일)가 일치하는지 판단//
					if(datearray[0].equals(comparedatearray[0]) && 
							datearray[1].equals(comparedatearray[1]) && 
							datearray[2].equals(comparedatearray[2])){
						logcount++;
						logger.debug("ok" + logcount);
					} else{
						logger.debug("not ok" + + logcount);
					}
				}
				
				loghitinfolist.add(loglist); //리스트에 대입//
				
				loglist.put("logcount["+i+"]", ""+logcount);
				logcount = 0;
				
				logger.debug("---------------------------------");
			}
			
			return loghitinfolist;
		}
		catch (SVNException e) {
			e.printStackTrace();
		}
		
		return loghitinfolist;
	}
}
