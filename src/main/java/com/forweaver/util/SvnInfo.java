package com.forweaver.util;

import java.io.ByteArrayOutputStream;
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
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNDiffClient;
import org.tmatesoft.svn.core.wc.SVNRevision;

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
			
			//3. 라인추가 개수(저장소 전체 커밋 리스트 -> diff파싱))//
			Map<String, Object> lineinfo = doDiffLineInfo(repository);
			
			logger.debug("total add line count: " + lineinfo.get("addlinecount"));
			logger.debug("total remove line count: " + lineinfo.get("removelinecount"));
			
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
	
	/** SVN 라인정보 출력
	 *
	 * @param revesionone
	 * @param revesiontwo
	 * @return
	 */
	public Map<String, Object> doDiffLineInfo(SVNRepository repository){
		Map<String, Object> lineinfo = new HashMap<String, Object>();
		
		String diffresult = null;
		int totaladd_line = 0;
		int totalremove_line = 0;

		try {
			SVNURL svnURL = repository.getRepositoryRoot(false);
			long latestrevesion = repository.getLatestRevision();
			
			logger.debug("latest revesion: " + latestrevesion);

			// Get diffClient.
		    SVNClientManager clientManager = SVNClientManager.newInstance();
		    SVNDiffClient diffClient = clientManager.getDiffClient();

		    // Using diffClient, write the changes by commitId into
		    // byteArrayOutputStream, as unified format.
		    for(int i=0; i<Integer.parseInt(""+latestrevesion); i++){
		    	long startrevesion = i;
		    	long endrevesion = i+1;
		    	
		    	if(i > Integer.parseInt(""+latestrevesion)){
		    		endrevesion = latestrevesion;
		    	}
		    	ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			    diffClient.doDiff(svnURL, null, SVNRevision.create(startrevesion), SVNRevision.create(endrevesion), SVNDepth.INFINITY, true, byteArrayOutputStream);
			    //diffClient.doDiff(new File(repourl), SVNRevision.UNDEFINED, SVNRevision.create(revesionone), SVNRevision.create(revesiontwo), true, true, byteArrayOutputStream);
			    diffresult = byteArrayOutputStream.toString();
			    
			    String parsediff[] = diffresult.split("\n");
			    
			    logger.debug("startrevesion: " + startrevesion + " / endrevesion: " + endrevesion);
			    
			    //diff 정보를 파싱//
			    for(int loop=0; loop<parsediff.length; loop++){
			    	logger.debug("diff info["+loop+"]: " + parsediff[loop]);
			    }
			    
			    logger.debug("------------------");
		    }
		    
		    lineinfo.put("addlinecount", totaladd_line);
		    lineinfo.put("removelinecount", totalremove_line);
		    
	        return lineinfo;
		} catch (SVNException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}

		return lineinfo;
	}
}
