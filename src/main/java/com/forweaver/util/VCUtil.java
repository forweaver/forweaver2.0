package com.forweaver.util;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import com.forweaver.domain.vc.VCBlame;
import com.forweaver.domain.vc.VCCommitLog;
import com.forweaver.domain.vc.VCFileInfo;
import com.forweaver.domain.vc.VCSimpleCommitLog;
import com.forweaver.domain.vc.VCSimpleFileInfo;

/** 실제 버젼 관리 작업을 구현을 정의한 인터페이스
 *
 */
public interface VCUtil {


	//저장소 생성함.
	public boolean createRepository();

	/// 저장소 디렉토리 삭제
	public boolean deleteRepository();

	/** 파일주소와 커밋아이디를 바탕으로 디렉토리인지 검사함.
	 * @param commitID
	 * @param filePath
	 * @return
	 */
	public boolean isDirectory(String commitID, String filePath);


	/** 프로젝트의 파일 정보를 가져옴
	 * @param commitID
	 * @param filePath
	 * @return
	 */
	public VCFileInfo getFileInfo(String commitID, String filePath);

	/** 저장소에서 커밋을 갖고 옴
	 * @param refName
	 * @return
	 */
	public VCSimpleCommitLog getVCCommit(String refName);
	
	/** 브랜치 혹은 태그의 커밋 갯수를 가져옴
	 * @param refName
	 * @return
	 */
	public int getCommitListCount(String refName);

	/** 프로젝트의 파일 정보들을 가져와 파일 브라우져를 보여줄 때 사용.
	 * @param commitID
	 * @param filePath
	 * @return
	 */
	public List<VCSimpleFileInfo> getGitFileInfoList(String commitID,String filePath);

	/** 프로젝트의 파일 목록을 커밋 아이디를 가지고 가져옴.
	 * @param commitID
	 * @return
	 */
	public List<String> getGitFileList(String commitID);

	/** 저장소에서 GIT 로그 정보를 가져옴
	 * @param commitID
	 * @return
	 */
	public VCCommitLog getCommitLog(String commitID);

	
	// 커밋 로그 목록를 가져옴
	public List<VCSimpleCommitLog> getCommitLogList(String branchName,int page, int number);

	/** 저장소에서 브랜치 정보를 가져옴
	 * @return
	 */
	public List<String> getBranchList();


	/** 브랜치 목록과 태그 목록을 가져옴
	 * @return
	 */
	public List<String> getSimpleBranchAndTagNameList();


	/** 커밋을 입력받으면 당시 파일들을 압축하여 사용자에게 보내줌.
	 * @param commitName
	 * @param format
	 * @param response
	 */
	public void getProjectZip(String commitName,String format, HttpServletResponse response);

	/** 일주일 24시간별로 커밋의 갯수를 측정하는 코드 빈도수 시각화에 쓰임
	 * @return
	 */
	public int[][] getDayAndHour();
	
	/** git blame기능을 구현함.
	 * @param filePath
	 * @param commitID
	 * @return
	 */
	public List<VCBlame> getBlame(String filePath, String commitID);

}
