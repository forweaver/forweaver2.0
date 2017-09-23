package com.forweaver.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLock;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNProperties;
import org.tmatesoft.svn.core.SVNProperty;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.io.ISVNEditor;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.io.diff.SVNDeltaGenerator;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNDiffClient;
import org.tmatesoft.svn.core.wc.SVNLogClient;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNWCClient;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import com.forweaver.config.Config;
import com.forweaver.domain.Repository;
import com.forweaver.domain.git.statistics.GitChildStatistics;
import com.forweaver.domain.git.statistics.SvnChildStatistics;
import com.forweaver.domain.git.statistics.SvnParentStatistics;
import com.forweaver.domain.vc.VCBlame;
import com.forweaver.domain.vc.VCFileInfo;
import com.forweaver.domain.vc.VCLog;
import com.forweaver.domain.vc.VCSimpleFileInfo;
import com.forweaver.domain.vc.VCSimpleLog;
import com.forweaver.domain.vc.VCSvnInfo;

@Component
public class SVNUtil implements VCUtil{
	private static final Logger logger =
			LoggerFactory.getLogger(SVNUtil.class);

	private String svnPath;
	private String svnreporootPath;
	private String path;
	private SVNRepository repository;
	private SVNURL svnURL;
	
	@Autowired
	AnnotationHandler annotationhandler;

	public SVNUtil(){
		this.svnPath = Config.svnPath;
		this.svnreporootPath = "file://"+this.svnPath; //svn의 저장소 주소//
	}

	public String getSvnPath() {
		return svnPath;
	}

	public String getSvnrepoPath() {
		return svnreporootPath;
	}

	public SVNRepository getRepository() {
		return repository;
	}

	/** 프로젝트 초기화 메서드
	 * @param pro
	 */
	public void Init(Repository pro) {
		try {
			this.path = svnPath + pro.getName();
			logger.debug("path: " + this.path);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

	/** 프로젝트 저장소 정보 설정(Repository Load) 메서드
	 *
	 * @param parentDirctoryName
	 * @param repositoryName
	 * @return
	 */
	public void RepoInt(String parentDirctoryName, String repositoryName) {
		try {
			SVNRepository repository = SVNRepositoryFactory.create( SVNURL.parseURIEncoded(this.svnreporootPath+"/"+parentDirctoryName+"/"+repositoryName));
			this.repository = repository;
		
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

	public boolean createRepository() {
		try {
			SVNURL tgtURL = SVNRepositoryFactory.createLocalRepository( new File( this.path ), true , false );

			logger.debug("repo URL: " + tgtURL);
		} catch (SVNException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return true;
	}

	public boolean deleteRepository() {
		try {
			FileUtils.deleteDirectory(new File(this.path)); //파일제거//
		}catch(Exception e) {
			logger.error(e.getMessage());
			return false;
		}
		return true;
	}

	/** 파일주소와 커밋아이디(Revesion)를 바탕으로 디렉토리인지 검사함.
	 * @param commitID
	 * @param filePath
	 * @return
	 */
	public boolean isDirectory(String commitID, String filePath) {
		//해당 filepath만 검증//
		logger.debug("==> isDirectory filePath: " + filePath);
		SVNDirEntry dirEntry=null;

		try {
		    dirEntry=this.repository.info(filePath,Long.parseLong(commitID));

		    if(dirEntry.getKind().toString().equals("dir")){
		    	logger.debug("==> [true directory]");
            	return true;
            } else{
            	logger.debug("==> [false directory]");
            	return false;
            }
		} catch (  SVNException e) {
			e.printStackTrace();

			return false;
		}
	}

	/** 프로젝트의 파일 정보를 가져옴
	 * @param commitID
	 * @param filePath
	 * @return
	 */
	@SuppressWarnings("finally")
	public VCFileInfo getFileInfo(String commitID, String filePath) {

		//파일내용, 커밋로그 2개를 call//
		List<VCSimpleLog> commitLogList = new ArrayList<VCSimpleLog>();

		//저장소의 로그기록을 가져온다.//
		Collection logEntries = null;

		int selectCommitIndex = 0;
		int endRevesion = Integer.parseInt(commitID); //HEAD (the latest) revision

		try {
			logEntries = this.repository.log(new String[] { filePath }, null, selectCommitIndex, endRevesion, true, true);

			for (Iterator entries = logEntries.iterator(); entries.hasNext();) {
				SVNLogEntry logEntry = (SVNLogEntry) entries.next();
				//lock/unlock 상태 추출//
              
				commitLogList.add(new VCSimpleLog(
						""+logEntry.getRevision(),
						logEntry.getMessage(),
						logEntry.getAuthor(),
						"",
						logEntry.getDate()));
			}

			//해당 파일에 대해서 로그가 일치하는 부분에서 종료//
			for(;selectCommitIndex<commitLogList.size();selectCommitIndex++)
				if(commitLogList.get(selectCommitIndex).getLogID().equals(endRevesion))
					break;
		} catch (SVNException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch(Exception e){
			e.printStackTrace();
		} finally{
			//파일의 내용을 불러온다.(String, byte[])//
			String stringContent = doPrintFileStringcontent(filePath);
			byte[] byteContent = doPrintFileBytecontent(filePath);
			
			//lock/unlock 상태 추출//
            SVNLock lock = null;
            boolean isLockflag = false;
            String lockdate = "";
            String lockAuthor = "";
            String lockcomment = "";
            
			try {
				lock = repository.getLock(filePath);
		
	            if (lock == null) {
					isLockflag = false;
					logger.debug(filePath + "file is not lock");
	            }

				else if(lock != null){
					isLockflag = true;
					lockdate = ""+lock.getCreationDate();
					lockAuthor = "" + lock.getOwner();
					lockcomment = "" + lock.getComment();
					logger.debug(filePath + "file is lock");
				}
			} catch (SVNException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return new VCFileInfo(filePath, stringContent, byteContent,
					commitLogList, selectCommitIndex,isDirectory(commitID,filePath), isLockflag, lockdate, lockAuthor, lockcomment);
		}
	}

	/** 파일내용 String으로 출력
	 *
	 * @param filename
	 * @return
	 */
	public String doPrintFileStringcontent(String filename){
		logger.debug("--<file content>--");
		logger.debug("==> filename: " + filename);

		String filecontent = "";

		SVNRepository repository = null;

		try {
			repository = this.repository;

			/*//인증정보를 설정//
			ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(userid, userpassword);
			repository.setAuthenticationManager(authManager);*/

			SVNNodeKind nodeKind = repository.checkPath(filename, -1);

			if (nodeKind == SVNNodeKind.NONE || nodeKind == SVNNodeKind.FILE) {
				SVNProperties fileProperties = new SVNProperties();
				ByteArrayOutputStream baos = new ByteArrayOutputStream();

				repository.getFile(filename, -1, fileProperties, baos);

				String mimeType = fileProperties.getStringValue(SVNProperty.MIME_TYPE);
				boolean isTextType = SVNProperty.isTextMimeType(mimeType);

				Iterator iterator = fileProperties.nameSet().iterator();

				while (iterator.hasNext()) {
					String propertyName = (String) iterator.next();
					String propertyValue = fileProperties.getStringValue(propertyName);
				}

				if (isTextType) {
					logger.debug("==> "+filename +" File contents:");

					filecontent = baos.toString();

					logger.debug(filecontent);
					logger.debug("------------------");
					return filecontent;
				} else {
					logger.debug(filename + " Not a text file.");
					logger.debug("------------------");
					return filecontent;
				}
			} else if (nodeKind == SVNNodeKind.DIR) {
				logger.debug(filename + " is Directory");
				logger.debug("------------------");
				return filecontent;
			}


		} catch (SVNException e) {
			e.printStackTrace();
			logger.debug("------------------");
			return filecontent;
		}

		return filecontent;
	}

	/** 파일내용 byte[]로 출력
	 *
	 * @param filename
	 * @return
	 */
	public byte[] doPrintFileBytecontent(String filename){
		logger.debug("--<file content>--");
		logger.debug("==> filename: " + filename);

		byte[] content = null;

		SVNRepository repository = null;

		try {
			repository = this.repository;

			/*//인증정보를 설정//
			ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(userid, userpassword);
			repository.setAuthenticationManager(authManager);*/

			SVNNodeKind nodeKind = repository.checkPath(filename, -1);

			if (nodeKind == SVNNodeKind.NONE || nodeKind == SVNNodeKind.FILE) {
				SVNProperties fileProperties = new SVNProperties();
				ByteArrayOutputStream baos = new ByteArrayOutputStream();

				repository.getFile(filename, -1, fileProperties, baos);

				String mimeType = fileProperties.getStringValue(SVNProperty.MIME_TYPE);
				boolean isTextType = SVNProperty.isTextMimeType(mimeType);

				Iterator iterator = fileProperties.nameSet().iterator();

				while (iterator.hasNext()) {
					String propertyName = (String) iterator.next();
					String propertyValue = fileProperties.getStringValue(propertyName);
				}

				if (isTextType) {
					logger.debug("==> "+filename +" File contents:");
					content = baos.toByteArray();

					logger.debug(""+content);

					return content;
				} else {
					logger.debug(filename + " Not a text file.");
					logger.debug("------------------");
					return content;
				}
			} else if (nodeKind == SVNNodeKind.DIR) {
				logger.debug(filename + " is Directory");
				logger.debug("------------------");
				return content;
			}


		} catch (SVNException e) {
			e.printStackTrace();
			logger.debug("------------------");
			return content;
		}

		return content;
	}

	public int getCommitListCount(String commitID) {
		int logcount = 0;

		//저장소의 로그기록을 가져온다.//
		Collection logEntries = null;

		int selectCommitIndex = 0;
		int endRevesion = Integer.parseInt(commitID); //HEAD (the latest) revision

		try {
			logEntries = this.repository.log(new String[] { "" }, null, selectCommitIndex, endRevesion, true, true);

			for (Iterator entries = logEntries.iterator(); entries.hasNext();) {
				SVNLogEntry logEntry = (SVNLogEntry) entries.next();
				logcount++;
			}
		} catch (SVNException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch(Exception e){
			e.printStackTrace();
		}

		logger.debug("==> log count: " + logcount);

		return logcount;
	}

	// 디렉터리일 경우 정보 리스트
	public List<VCSimpleFileInfo> getVCFileInfoList(String commitID, String filePath) {
		List<VCSimpleFileInfo> svnFileInfoList = new ArrayList<VCSimpleFileInfo>();

		SVNRepository repository = this.repository;

		try {
			svnFileInfoList = listEntries(repository, filePath, commitID); //파일내의 정보를 불러온다.//
		} catch (SVNException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return svnFileInfoList;
	}

	//파일 리스트 정보를 가져온다.//
	private List<VCSimpleFileInfo> listEntries(SVNRepository repository, String path, String commitID) throws SVNException {
		List<VCSimpleFileInfo> svnFileInfoList = new ArrayList<VCSimpleFileInfo>();

		boolean isLockflag = false;
		logger.debug("==> filepath: " + path);
		logger.debug("==> commitID: " + commitID);

        try
        {
        	Collection entries = repository.getDir(path, -1, null, (Collection) null);

            Iterator iterator = entries.iterator();

        	while (iterator.hasNext()) {
                SVNDirEntry entry = (SVNDirEntry) iterator.next();
                
                //lock/unlock 상태 추출//
                SVNLock lock = repository.getLock(path+"/"+entry.getName());
                
                if (lock == null) {
    				isLockflag = false;
    				logger.debug(path+"/"+entry.getName() + "file is not lock");
                }

    			else if(lock != null){
    				isLockflag = true;
    				logger.debug(path+"/"+entry.getName() + "file is lock");
    			}
                
                //디렉터리 출력 형식에 맞게 가져온다.//
                VCSimpleFileInfo svnFileInfo = new VCSimpleFileInfo(
                		entry.getName(), path+"/"+entry.getName(),
						isDirectory(commitID, path+"/"+entry.getName()),
						""+entry.getRevision(), entry.getCommitMessage().toString(),
						entry.getDate(),
						entry.getAuthor(),
						isLockflag);

                
                svnFileInfoList.add(svnFileInfo);
    			
    			/*//락 테스트//
				//File Lock//
				SVNWCClient wcclient = clientManager.getWCClient();
				SVNURL svnURLs[] = new SVNURL[1];
				SVNURL svnURLlock = repository.getRepositoryRoot(false).appendPath(filePath, false);
	
				String lockPath = filePath;
				SVNLock lock = repository.getLock(lockPath);
	
				if (lock == null) {
					svnURLs[0] = svnURLlock;
					
					wcclient.doLock(svnURLs, false, "fff");
	            }
	
				else if(lock != null){
					svnURLs[0] = svnURLlock;
					
					wcclient.doUnlock(svnURLs, false);
				}
				
				//락/언락 확인//
				lockPath = filePath;
				lock = repository.getLock(lockPath);
	
				if (lock == null) {
	                logger.debug(filePath + " isn't lock");
	            }
	
				else if(lock != null){
					logger.debug(filePath + " is lock");
				} */
            }

        	return svnFileInfoList;
        } catch(SVNException e){
        	e.printStackTrace();
        }

        return svnFileInfoList;
    }

	public List<String> getVCFileList(String commitID) {
		// TODO Auto-generated method stub
		return null;
	}

	public VCLog getCommitLog(String commitID) {
		VCLog svnCommitLog = null;
		String diffStr = new String();

		String revesion = "";
		String commitMessage = "";
		String fullMessage = "";
		String commiterName = "";
		String commiterEmail = "";
		Date commitdate = null;

		//diff와 로그정보를 가져온다.//
		//저장소의 로그기록을 가져온다.//
		Collection logEntries = null;

		int selectCommitIndex = Integer.parseInt(commitID);
		int endRevesion = Integer.parseInt(commitID); //HEAD (the latest) revision

		try{
			logEntries = this.repository.log(new String[] { "" }, null, selectCommitIndex, endRevesion, true, true);

			for (Iterator entries = logEntries.iterator(); entries.hasNext();) {
				SVNLogEntry logEntry = (SVNLogEntry) entries.next();

				revesion = ""+logEntry.getRevision();
				commitMessage = logEntry.getMessage();
				fullMessage = logEntry.getMessage();
				commiterName = logEntry.getAuthor();
				commitdate = logEntry.getDate();
			}

			//diff정보를 가져온다.(선택된 커밋과 하나 이전 커밋과의 비교)//
			diffStr = doDiff(selectCommitIndex-1, selectCommitIndex);

			logger.debug("==> Diff result:");
			logger.debug(diffStr);

			//로그객체를 생성//
			svnCommitLog = new VCLog(revesion,
					commitMessage, fullMessage, commiterName, commiterEmail,
					diffStr, null,commitdate);

		} catch (SVNException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch(Exception e){
			e.printStackTrace();
		}

		return svnCommitLog;
	}

	/** SVN Diff 수행
	 *
	 * @param revesionone
	 * @param revesiontwo
	 * @return
	 */
	public String doDiff(long revesionone, long revesiontwo){
		String diffresult = null;
		SVNRepository repository = null;

		try {
			repository = this.getRepository(); //저장소를 불러온다.//

			SVNURL svnURL = repository.getRepositoryRoot(false);

			// Get diffClient.
		    SVNClientManager clientManager = SVNClientManager.newInstance();
		    SVNDiffClient diffClient = clientManager.getDiffClient();

		    // Using diffClient, write the changes by commitId into
		    // byteArrayOutputStream, as unified format.
		    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		    diffClient.doDiff(svnURL, null, SVNRevision.create(revesionone), SVNRevision.create(revesiontwo), SVNDepth.INFINITY, true, byteArrayOutputStream);
		    //diffClient.doDiff(new File(repourl), SVNRevision.UNDEFINED, SVNRevision.create(revesionone), SVNRevision.create(revesiontwo), true, true, byteArrayOutputStream);
		    diffresult = byteArrayOutputStream.toString();
		    
	        return diffresult;
		} catch (SVNException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}

		return diffresult;
	}

	public List<VCSimpleLog> getCommitLogList(String commitID, int page, int number) {
		//파일내용, 커밋로그 2가를 call//
		List<VCSimpleLog> svncommitLogList = new ArrayList<VCSimpleLog>();
		//page카운트 변수//
		int pageCount = 0;

		//저장소의 로그기록을 가져온다.//
		Collection logEntries = null;

		int selectCommitIndex = 0;
		int endRevesion = Integer.parseInt(commitID); //HEAD (the latest) revision

		try{
			//페이징 처리//
			//1페이지인 경우는 그대로 간다.//
			if(page == 1){
				logEntries = this.repository.log(new String[] { "" }, null, selectCommitIndex, endRevesion, true, true);

				for (Iterator entries = logEntries.iterator(); entries.hasNext();) {
					//System.out.println("pageCount: " + pageCount);

					if(pageCount > number){
						break;
					}

					SVNLogEntry logEntry = (SVNLogEntry) entries.next();

					//repotreelist_commitmessage.add(logEntry.getMessage());
					svncommitLogList.add(new VCSimpleLog(
							""+logEntry.getRevision(),
							logEntry.getMessage(),
							logEntry.getAuthor(),
							"",
							logEntry.getDate()));


					pageCount++;
				}
			} else if(page > 1){
				//1페이지 이상부터는 루프의 범위가 달라진다.//
				int startNumber = number+1;
				int endNumber = page * number;

				logEntries = this.repository.log(new String[] { "" }, null, startNumber, endRevesion, true, true);

				for (Iterator entries = logEntries.iterator(); entries.hasNext();) {
					//해당 범위에 들어왔을 때 로그를 추출//
					if(pageCount > startNumber){
						//System.out.println("pageCount: " + pageCount);

						SVNLogEntry logEntry = (SVNLogEntry) entries.next();

						//repotreelist_commitmessage.add(logEntry.getMessage());
						svncommitLogList.add(new VCSimpleLog(
								""+logEntry.getRevision(),
								logEntry.getMessage(),
								logEntry.getAuthor(),
								"not email svn",
								logEntry.getDate()));
					} if(pageCount > endNumber){
						break;
					}

					pageCount++;
				}
			}
		} catch (SVNException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch(Exception e){
			e.printStackTrace();
		}

		pageCount = 0;

		//정렬순서를 내림차순으로 변경//
		Descending descending = new Descending();
		Collections.sort(svncommitLogList, descending);

		return svncommitLogList;
	}

	public List<String> getBranchList() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<String> getSimpleBranchAndTagNameList() {
		// TODO Auto-generated method stub
		return null;
	}

	public void getProjectZip(String commitName, String format, HttpServletResponse response) {
		// TODO Auto-generated method stub

	}

	public int[][] getDayAndHour() {
		//로그정보를 넘기면 요일 숫자를 뽑아내는 메소드//
		//로그정보를 넘기면 시간정보를 숫자로 뽑아내는 메소드//
		int[][] array = new int[7][24];

		//로그뽑아오기//
		Map<String, Object> commitinfo = doPrintRepoLog(repository); //전체 커밋정보 리스트//
		
		List<Object> datelist = (List<Object>) commitinfo.get("datelist");
		
		for(int i=0; i<datelist.size(); i++){
			logger.debug("date: " + datelist.get(i));
			
			//파싱 후 시각화 배열에 적용//
			String day_time[] = datelist.get(i).toString().split("/");
			
			array[Integer.parseInt(day_time[0])]
					[Integer.parseInt(day_time[1])]++;
		}

		return array;
	}

	public List<VCBlame> getBlame(String filePath, String commitID) {
		logger.debug("===== Blame set...");
		logger.debug("filePath: " + filePath);
		logger.debug("commitID: " + commitID);

		long startRevesion = 0;
		long endRevesion = Long.parseLong(commitID);

		logger.debug("start Revesion: " + startRevesion);
		logger.debug("end Revesion: " + endRevesion);

		List<VCBlame> gitBlames = new ArrayList<VCBlame>();
		List<Map<String, Object>>blameinfolist = new ArrayList<Map<String, Object>>();

		SVNRepository repository = null;
		//블렘을 수행하는 핸들러 호출//
		try {
			//Get LogClient//
			SVNClientManager clientManager = SVNClientManager.newInstance();
			SVNLogClient logClient = clientManager.getLogClient();

			boolean includeMergedRevisions = false;

			annotationhandler.setInit(includeMergedRevisions, true, logClient.getOptions());

			repository = this.getRepository(); //저장소를 불러온다.//

			SVNURL svnURL = repository.getRepositoryRoot(false).appendPath(filePath, false);

			logger.debug("repo address: " + repository.getRepositoryRoot(false).getPath());

			logClient.doAnnotate(svnURL, SVNRevision.UNDEFINED, SVNRevision.create(startRevesion), SVNRevision.create(endRevesion), annotationhandler);

			blameinfolist = annotationhandler.getResult();

			logger.debug("blame info size: " + blameinfolist.size());

			for(int i=0; i<blameinfolist.size(); i++){
				gitBlames.add(new VCBlame(
						blameinfolist.get(i).get("commitID").toString(),
						blameinfolist.get(i).get("userName").toString(),
						blameinfolist.get(i).get("userEmail").toString(),
						blameinfolist.get(i).get("commitTime").toString()));
			}

			return gitBlames;
		} catch (SVNException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch(Exception e){
			e.printStackTrace();
		}

		return null;
	}

	/** 파일을 수정 시 commit까지 바로 진행
	 * @param name
	 * @param email
	 * @param branchName
	 * @param message
	 * @param zip
	 */
	public void updateFile(String name,String email,String branchName,String message,String path,String code){
		logger.debug("-> name: " + name);
		logger.debug("-> email: " + email);
		logger.debug("-> message: " + message);
		logger.debug("-> path: " + path);
		logger.debug("-> code: " + code);

		//SVN modify commit//
		SVNRepository repository = null;

		try {
			repository = this.getRepository(); //저장소를 불러온다.//

			byte[] oldcontents = code.getBytes();
			byte[] updatecontents = code.getBytes();

			SVNNodeKind nodeKind = repository.checkPath("", -1);
			logger.debug("-> status: " + nodeKind);

			long latestRevision = repository.getLatestRevision();
			logger.debug("Repository latest revision (before committing): " + latestRevision);

	        ISVNEditor editor = repository.getCommitEditor(message, null);

	        logger.debug("midify path: " + path);
	        SVNCommitInfo commitInfo = modifyFile(editor, path, path, oldcontents, updatecontents);
	        logger.debug("The file was changed: " + commitInfo);

	        //수정되었는지 확인//
	        if(commitInfo != null){
	        	logger.debug("==> edit result: success...");
	        } else if(commitInfo == null){
	        	logger.debug("==> edit result: fail...");
	        }
		} catch(SVNException e){
			e.printStackTrace();
		}
	}

	private static SVNCommitInfo modifyFile(ISVNEditor editor, String dirPath, String filePath, byte[] oldData, byte[] newData) throws SVNException {
        editor.openRoot(-1);
        editor.openDir(dirPath, -1);
        editor.openFile(filePath, -1);
        editor.applyTextDelta(filePath, null);

        SVNDeltaGenerator deltaGenerator = new SVNDeltaGenerator();
        String checksum = deltaGenerator.sendDelta(filePath, new ByteArrayInputStream(oldData), 0, new ByteArrayInputStream(newData), editor, true);

        editor.closeFile(filePath, checksum);
        editor.closeDir();

        return editor.closeEdit();
    }

	public VCSimpleLog getVCCommit(String refName) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<VCSimpleFileInfo> getGitFileInfoList(String commitID, String filePath) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<String> getGitFileList(String commitID) {
		// TODO Auto-generated method stub
		return null;
	}

	public VCLog getLog(String commitID) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<VCSimpleLog> getLogList(String branchName, int page, int number) {
		// TODO Auto-generated method stub
		return null;
	}

	public void getRepositoryZip(String commitName, String format, HttpServletResponse response) {
		// TODO Auto-generated method stub

	}

	public void dolock(String lockfilepath){
		System.out.println("repository info: " + this.repository.getLocation() + "filepath: " + lockfilepath);

		try {

			//File Lock//
			SVNClientManager clientManager = SVNClientManager.newInstance();
			SVNWCClient wcclient = clientManager.getWCClient();

			SVNURL svnURLs[] = new SVNURL[1];
			SVNURL svnURL = this.repository.getLocation().appendPath(lockfilepath, false);

			svnURLs[0] = svnURL;

			//wcclient.doLock(lockfilepath, true, "file lock");
			wcclient.doLock(svnURLs, false, "file lock");
			
			//락 확인//
			String lockPath = lockfilepath;
			SVNLock lock = repository.getLock(lockPath);

			if (lock == null) {
                System.out.println(lockfilepath + " isn't lock");
                System.out.println(lock.getOwner());
                System.out.println(lock.getID());
            }

			else if(lock != null){
				System.out.println(lockfilepath + " is lock");
				System.out.println(lock.getOwner());
                System.out.println(lock.getID());
			}
		} catch (SVNException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void dounlock(String lockfilepath){
		System.out.println("repository info: " + this.repository.getLocation() + "filepath: " + lockfilepath);

		try {
			String lockPath = lockfilepath;
			SVNLock lock = repository.getLock(lockPath);

			if (lock == null) {
                System.out.println(lockfilepath + " isn't lock");
                System.out.println(lock.getOwner());
                System.out.println(lock.getID());
            }

			else if(lock != null){
				System.out.println(lockfilepath + " is lock");
				System.out.println(lock.getOwner());
                System.out.println(lock.getID());
			}
			//File Lock//
			SVNClientManager clientManager = SVNClientManager.newInstance();
			SVNWCClient wcclient = clientManager.getWCClient();

			SVNURL svnURLs[] = new SVNURL[1];
			SVNURL svnURL = this.repository.getLocation().appendPath(lockfilepath, false);

			svnURLs[0] = svnURL;

			//wcclient.doLock(lockfilepath, true, "file lock");
			wcclient.doUnlock(svnURLs, false);
			
			//락 확인//
		} catch (SVNException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/** 저장소 정보를 가져옴
	 * @param svnRepository 
	 * @param branchName
	 * @return
	 */
	public VCSvnInfo getSvnInfo(String branchName){
		SvnInfo svnInfo = new SvnInfo();
		VCSvnInfo info = null;
		SVNRepository repository = this.repository;
		
		try{
			info = svnInfo.run(repository, branchName);
		}catch(Exception e){
			System.err.println(e.getMessage());
		}
		return info;
	}
	
	public Map<String, Object> doPrintRepoLog(SVNRepository repository){
		//SVNRepository repository = null;
		Collection logEntries = null;
		
		//媛� 濡쒓렇�뿉�꽌 �븘�슂�븳 �뜲�씠�꽣瑜� ���옣�븷 �닔 �엳�뒗 �옄猷뚭뎄議� �꽑�뼵//
		List<Object>datelist = new ArrayList<Object>();
		
		//醫낇빀�젙蹂대�� 媛�吏� 由ъ뒪�듃//
		Map<String, Object>loglist = new HashMap<String, Object>();
		
		long startRevision = 0;
		long endRevision = -1; //HEAD (the latest) revision
		
		int logcount = 0; //key濡� �솢�슜//
		
		try {
			//repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(repourl));
			
			/*//인증정보를 설정//
			ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(userid, userpassword);
	        repository.setAuthenticationManager(authManager);*/
	       
			logEntries = repository.log(new String[] { "" }, null, startRevision, endRevision, true, true);

			for (Iterator entries = logEntries.iterator(); entries.hasNext();) {
				SVNLogEntry logEntry = (SVNLogEntry) entries.next();
				
				String parsingresult = "";
				//파싱//
				String dateinfo[] = logEntry.getDate().toString().split(" ");
				String timeinfo[] = dateinfo[3].split(":");
				
				if(dateinfo[0].equals("Sun")){
					parsingresult = "0";
				} else if(dateinfo[0].equals("Mon")){
					parsingresult = "1";
				} else if(dateinfo[0].equals("Tue")){
					parsingresult = "2";
				} else if(dateinfo[0].equals("Wed")){
					parsingresult = "3";
				} else if(dateinfo[0].equals("Thu")){
					parsingresult = "4";
				} else if(dateinfo[0].equals("Fri")){
					parsingresult = "5";
				} else if(dateinfo[0].equals("Sat")){
					parsingresult = "6";
				}
				
				parsingresult += "/" + timeinfo[0];
				
				datelist.add(parsingresult);
				
				logcount++;
			}
			
			loglist.put("datelist", datelist);
			loglist.put("count", ""+logcount);
			
			return loglist;
		}
		catch (SVNException e) {
			e.printStackTrace();
		}
		
		return loglist;
	}
	
	/** 각 유저가 날짜별로 커밋을 한 정보를 취합함.
	 * @return
	 */
	public SvnParentStatistics getCommitStatistics(){
		logger.debug("--> svn statistic info");
		
		SvnParentStatistics svnParentStatistics = new SvnParentStatistics();
		
		//리비전 당 비교 정보들을 SvnChildStatistics 맞춰 넣어준다.//
		//필요한 정보 : 커밋터 계정(이메일), 추가라인 수, 삭제 라인 수, 추가파일 수, 제거 파일 수, 커밋날짜 -> 해당 케이스를 리스트로 관리//
		List<String>commiterlist = new ArrayList<String>();
		List<String>addlinelist = new ArrayList<String>();
		List<String>deletelinelist = new ArrayList<String>();
		List<String>addfilelist = new ArrayList<String>();
		List<String>deletefilelist = new ArrayList<String>();
		List<Date>datelist = new ArrayList<Date>();
		
		String diffresult = null;
		int add_line = 0;
		int remove_line = 0;
		
		//파일정보에 대한 정보를 저장하는 배열//
		List<String> fileinfolist = new ArrayList<String>();
		
		//diff와 로그정보를 가져온다.//
		//저장소의 로그기록을 가져온다.//
		Collection logEntries = null;

		int selectCommitIndex = Integer.parseInt("0");
		int endRevesion = Integer.parseInt("-1"); //HEAD (the latest) revision

		try{
			logEntries = this.repository.log(new String[] { "" }, null, selectCommitIndex, endRevesion, true, true);

			for (Iterator entries = logEntries.iterator(); entries.hasNext();) {
				SVNLogEntry logEntry = (SVNLogEntry) entries.next();

				if(logEntry.getAuthor() == null){
					commiterlist.add("not commiter");
				} else{
					commiterlist.add(logEntry.getAuthor());
				}
				datelist.add(logEntry.getDate());
			}
			
			//맨 첫번째 정보는 무의미 하므로 제외//
			commiterlist.remove(0);
			datelist.remove(0);
			
			//diff정보를 기반//
			SVNURL svnURL = this.repository.getRepositoryRoot(false);
			long latestrevesion = this.repository.getLatestRevision();
			
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
			    			add_line ++;
			    		} else if(parsediff[loop].equals("-")){
			    			remove_line ++;
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
			    					add_line ++;
			    				} else if(diffflagline.equals("-")){
			    					remove_line ++;
			    				}
			    			}
			    		} else if(parsediff[loop].length() < 4){
			    			String diffflag = parsediff[loop].substring(0, 1);
			    			
			    			if(diffflag.equals("+")){
			    				add_line ++;
			    			} else if(diffflag.equals("-")){
			    				remove_line ++;
			    			}
			    		}
			    	}
			    }
			    
			    logger.debug("------------------");
			    
			    //초기화(다음 리비전 비교를 위해)//
		    	
		    	//저장//
		    	addlinelist.add(""+add_line);
		    	deletelinelist.add(""+remove_line);
		    	
		    	//파일비교//
		    	Map<String, Object> fileinfo = getFileinfo(fileinfolist);
		    	
		    	addfilelist.add(""+fileinfo.get("addcount"));
		    	deletefilelist.add(""+fileinfo.get("removecount"));
		    	
		    	logger.debug("<semi result>");
		    	logger.debug("add line count: " + add_line);
		    	logger.debug("remove line count: " + remove_line);
		    	logger.debug("file add count: " + fileinfo.get("addcount"));
				logger.debug("file remove count: " + fileinfo.get("removecount"));
				
		    	add_line= 0;
		    	remove_line = 0;
		    	fileinfolist.clear();
		    }
		    
		    logger.debug("datesize: " + datelist.size());
		    logger.debug("commitersize: " + commiterlist.size());
		    logger.debug("addlinecount: " + addlinelist.size());
		    logger.debug("deletelinecount: " + deletelinelist.size());
		    logger.debug("addfilecount: " + addfilelist.size());
		    logger.debug("deletefilecount: " + deletefilelist.size());
		    
		    //Child를 만들어준다.//
		    for(int i=0; i<datelist.size(); i++){
		    	svnParentStatistics.addSvnChildStatistics(
						new SvnChildStatistics(
								commiterlist.get(i),
								Integer.parseInt(addlinelist.get(i).toString()),
								Integer.parseInt(deletelinelist.get(i).toString()),
								Integer.parseInt(addfilelist.get(i).toString()),
								Integer.parseInt(deletefilelist.get(i).toString()),
								datelist.get(i)
								)); // 날짜
		    }
		} catch (SVNException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch(Exception e){
			e.printStackTrace();
		}
		
		return svnParentStatistics;
	}
	
	private Map<String, Object> getFileinfo(List<String>fileinfolist){
		Map<String, Object> fileinfo = new HashMap<String,Object>();
		
		int fileadd_count = 0;
		int fileremove_count = 0;
		
		for (int i = 0; i < fileinfolist.size(); i++) {
			String fileinfo_compare_1 = fileinfolist.get(i).toString();
			String fileinfo_compare_2 = fileinfolist.get(i + 1).toString();

			// diff 형식에 따르면 (~) 명으로 해당 파일에 상태를 추출한다.//
			int start_1_position = fileinfo_compare_1.indexOf('(');
			int end_1_position = fileinfo_compare_1.indexOf(')');
			// substring으로 파일부분만 추출한다.//
			String fileinfo_compare_1_status = fileinfo_compare_1.substring(start_1_position + 1, end_1_position);

			// 리비전이면 숫자에 따라 달라지기에 공통되는 부분으로 다시 파싱//
			if (fileinfo_compare_1_status.startsWith("revision")) {
				fileinfo_compare_1_status = fileinfo_compare_1_status.substring(0, 8);
			}

			int start_2_position = fileinfo_compare_2.indexOf('(');
			int end_2_position = fileinfo_compare_2.indexOf(')');
			String fileinfo_compare_2_status = fileinfo_compare_2.substring(start_2_position + 1, end_2_position);

			if (fileinfo_compare_2_status.startsWith("revision")) {
				fileinfo_compare_2_status = fileinfo_compare_2_status.substring(0, 8);
			}

			// 1. (nonexistent -> revision)이 설정되면 파일 추가//
			// 2. (revision -> nonexistent)이 설정되면 파일 제거//
			// 3. (revision -> revision)이 설정되면 파일 수정//
			if (fileinfo_compare_1_status.equals("nonexistent") && fileinfo_compare_2_status.equals("revision")) {
				fileadd_count ++;
			} else if (fileinfo_compare_1_status.equals("revision") && fileinfo_compare_2_status.equals("nonexistent")) {
				fileremove_count ++;
			} 

			i = i + 1; // 2개씩 이동//
		}
		
		fileinfo.put("addcount", fileadd_count);
		fileinfo.put("removecount", fileremove_count);
		
		return fileinfo;
	}
}