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
			Map<String, Object> diffinfo = doDiffInfo(repository);
			
			logger.debug("total add line count: " + diffinfo.get("addlinecount"));
			logger.debug("total remove line count: " + diffinfo.get("removelinecount"));
			logger.debug("total add file count: " + diffinfo.get("addfilecount"));
			logger.debug("total remove file count: " + diffinfo.get("removefilecount"));
			logger.debug("total modify file count: " + diffinfo.get("modifyfilecount"));
			
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
	public Map<String, Object> doDiffInfo(SVNRepository repository){
		Map<String, Object> diffinfo = new HashMap<String, Object>();
		
		//파일정보에 대한 정보를 저장하는 배열//
		List<String> fileinfolist = new ArrayList<String>();
		
		String diffresult = null;
		int totaladd_line = 0;
		int totalremove_line = 0;
		int totalfileadd_count = 0;
		int totalfileremove_count = 0;
		int totalfilemodify_count = 0;

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
			    diffresult = byteArrayOutputStream.toString();
			    
			    String parsediff[] = diffresult.split("\n");
			    
			    logger.debug("startrevesion: " + startrevesion + " / endrevesion: " + endrevesion);
			    
			    //diff 정보를 파싱//
			    for(int loop=0; loop<parsediff.length; loop++){
			    	logger.debug("diff info["+loop+"]: " + parsediff[loop]);
			    	
			    	//diff정보에서 사이즈가 1이라는 것은 diff 상태에 대한 플래그만 존재한다는 경우 (빈 공백 등이 이에 해당)//
			    	if(parsediff[loop].length() == 1){
			    		//따로 다시 파싱할 필요가 없는 경우.//
			    		//"+", "-"에 판단해서 라인추가, 삭제를 구별//
			    		if(parsediff[loop].equals("+")){
			    			totaladd_line ++;
			    		} else if(parsediff[loop].equals("-")){
			    			totalremove_line ++;
			    		}
			    	}
			    	
			    	//diff정보에서 사이즈가 1이상이라는 것은 diff 상태 플래그 이후에도 텍스트 정보가 존재한다는 의미//
			    	if(parsediff[loop].length() > 1){
			    		//1. diff 파일상태 : 파일 헤더정보일 경우 최소 길이가 3이상 ("+++ ", "--- " 은 정해진 규칙)
			    		//2. diff 라인상태 : 라인 정보에 대한 것은 최소 길이가 3이하일 수도 있다.//
			    		//3. 판단의 기준은 length 4를 기준으로 한다.//
			    		if(parsediff[loop].length() >= 4){
			    			String diffflag = parsediff[loop].substring(0, 4);
			    			
			    			if(diffflag.equals("+++ ") || diffflag.equals("--- ")){
			    				//파일 추가/삭제에 대한 정보//		
			    				//바로 비교가 불가하기에 정보를 먼저 배열에 저장//
			    				String fileinfo[] = parsediff[loop].split(" ");
			    				if(fileinfo.length == 2){
			    					fileinfolist.add(fileinfo[1]);	
			    				} else if(fileinfo.length == 3){
			    					fileinfolist.add(fileinfo[1] + fileinfo[2]);	
			    				}
			    			} else if(diffflag.equals("@@ -")){
			    				//logger.debug("file flag");
			    			} else{
			    				//라인을 판단하기 위해서 diff플래그 파싱//
			    				String diffflagline = parsediff[loop].substring(0, 1);
			    				
			    				if(diffflagline.equals("+")){
			    					totaladd_line ++;
			    				} else if(diffflagline.equals("-")){
			    					totalremove_line ++;
			    				}
			    			}
			    		} else if(parsediff[loop].length() < 4){
			    			String diffflag = parsediff[loop].substring(0, 1);
			    			
			    			if(diffflag.equals("+")){
			    				totaladd_line ++;
			    			} else if(diffflag.equals("-")){
			    				totalremove_line ++;
			    			}
			    		}
			    	}
			    }
			    
			    logger.debug("------------------");
		    }
		    
		    //파일정보 판단//
		    for(int i=0; i<fileinfolist.size(); i++){
		    	String fileinfo_compare_1 = fileinfolist.get(i).toString();
		    	String fileinfo_compare_2 = fileinfolist.get(i+1).toString();
		    	
		    	//diff 형식에 따르면 (~) 명으로 해당 파일에 상태를 추출한다.//
		    	int start_1_position = fileinfo_compare_1.indexOf('(');
		    	int end_1_position = fileinfo_compare_1.indexOf(')');
		    	//substring으로 파일부분만 추출한다.//
		    	String fileinfo_compare_1_status = fileinfo_compare_1.substring(start_1_position+1, end_1_position);
		    	
		    	//리비전이면 숫자에 따라 달라지기에 공통되는 부분으로 다시 파싱//
		    	if(fileinfo_compare_1_status.startsWith("revision")){
		    		fileinfo_compare_1_status = fileinfo_compare_1_status.substring(0,8);
		    	}
		    	
		    	int start_2_position = fileinfo_compare_2.indexOf('(');
		    	int end_2_position = fileinfo_compare_2.indexOf(')');
		    	String fileinfo_compare_2_status = fileinfo_compare_2.substring(start_2_position+1, end_2_position);
		    	
		    	if(fileinfo_compare_2_status.startsWith("revision")){
		    		fileinfo_compare_2_status = fileinfo_compare_2_status.substring(0,8);
		    	}
		    	
		    	//1. (nonexistent -> revision)이 설정되면 파일 추가//
				//2. (revision -> nonexistent)이 설정되면 파일 제거//
				//3. (revision -> revision)이 설정되면 파일 수정//
		    	if(fileinfo_compare_1_status.equals("nonexistent") && fileinfo_compare_2_status.equals("revision")){
		    		totalfileadd_count ++;
		    	} else if(fileinfo_compare_1_status.equals("revision") && fileinfo_compare_2_status.equals("nonexistent")){
		    		totalfileremove_count ++;
		    	} else if(fileinfo_compare_1_status.equals("revision") && fileinfo_compare_2_status.equals("revision")){
		    		totalfilemodify_count ++;
		    	}
		    	
		    	i = i+1; //2개씩 이동//
		    }
		    
		    
		    diffinfo.put("addlinecount", totaladd_line);
		    diffinfo.put("removelinecount", totalremove_line);
		    diffinfo.put("addfilecount", totalfileadd_count);
		    diffinfo.put("removefilecount", totalfileremove_count);
		    diffinfo.put("modifyfilecount", totalfilemodify_count);
		    
	        return diffinfo;
		} catch (SVNException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}

		return diffinfo;
	}
}
