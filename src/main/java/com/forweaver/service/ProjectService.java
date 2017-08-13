package com.forweaver.service;

import java.util.ArrayList;
import java.util.List;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.forweaver.domain.Pass;
import com.forweaver.domain.Project;
import com.forweaver.domain.ProjectInvite;
import com.forweaver.domain.Weaver;
import com.forweaver.mongodb.dao.PostDao;
import com.forweaver.mongodb.dao.ProjectDao;
import com.forweaver.mongodb.dao.ProjectInviteDao;
import com.forweaver.mongodb.dao.WeaverDao;
import com.forweaver.util.GitUtil;

/** 프로젝트 관리 서비스
 *
 */
@Service
public class ProjectService{

	@Autowired private WeaverDao weaverDao;
	@Autowired private ProjectDao projectDao;
	@Autowired private CacheManager cacheManager;
	@Autowired private ProjectInviteDao waitJoinDao;
	@Autowired private PostDao postDao;
	@Autowired private GitUtil gitUtil;

	@Autowired @Qualifier("sessionRegistry")
	private SessionRegistry sessionRegistry;
	/** 프로젝트 추가하는 메서드
	 * @param project
	 * @param currentWeaver
	 */
	public void add(Project project,Weaver currentWeaver){
		// TODO Auto-generated method stub
		if(currentWeaver == null)
			return;

		gitUtil.Init(project);
		
		if(!gitUtil.createRepository())
			return;

		projectDao.insert(project);
		Pass pass = new Pass(project.getName(),2);
		currentWeaver.addPass(pass);
		weaverDao.updatePass(currentWeaver);
	}

	/** 회원 추가함.
	 * @param project
	 * @param currentWeaver
	 * @param joinWeaver
	 */
	public boolean addWeaver(Project project,Weaver joinWeaver){
		// TODO Auto-generated method stub
		if(project == null || joinWeaver == null)
			return false;

		Pass pass = new Pass(project.getName(), 1);
		project.addJoinWeaver(joinWeaver); //프로젝트 목록에 추가
		joinWeaver.addPass(pass);
		weaverDao.updatePass(joinWeaver);
		this.update(project);

		return true;
	}


	/** 프로젝트 이름으로 불러오기.
	 * @param projectName
	 * @return
	 */
	public Project get(String projectName) {
		// TODO Auto-generated method stub
		return projectDao.get(projectName);
	}


	public boolean delete(Weaver weaver,Project project){
		// TODO Auto-generated method stub
		if(weaver == null || project == null)
			return false;
		if(weaver.isAdmin() || weaver.equals(project.getCreator())){


			gitUtil.Init(project);
			if(!gitUtil.deleteRepository())
				return false;

			for(Weaver joinWeaver:project.getJoinWeavers()){
				joinWeaver.deletePass(project.getName());
				weaverDao.updatePass(joinWeaver);

				for (Object object : sessionRegistry.getAllPrincipals()) { //현재 로그인 중인 회원의 권한 삭제.
					Weaver tmpWeaver = ((Weaver) object);
					if (tmpWeaver.equals(joinWeaver))
						joinWeaver.deletePass(project.getName());
				}
			}
			for(ProjectInvite waitJoin:waitJoinDao.delete(project.getName())){ // 대기 중인 초대장 삭제.
				postDao.delete(postDao.get(waitJoin.getPostID())); //처음 보냈던 메세지 삭제.
				return true;
			}

			project.getCreator().deletePass(project.getName());
			weaverDao.updatePass(project.getCreator());
			projectDao.delete(project);
			return true;
		}
		return false;

	}


	public boolean deleteWeaver(Project project, Weaver currentWeaver,Weaver deleteWeaver) {
		// 프로젝트에 동료를 탈퇴시킴
		if(project == null || deleteWeaver == null || deleteWeaver.getPass(project.getName()) == null)
			return false;

		if(project.getCreator().equals(currentWeaver) ||  //관리자가 탈퇴시키거나
				currentWeaver.equals(deleteWeaver)){ //본인이 나가거나
			deleteWeaver.deletePass(project.getName());
			project.removeJoinWeaver(deleteWeaver);
			weaverDao.updatePass(deleteWeaver);
			projectDao.update(project);

			return true;
		}

		return false;

	}

	/** 프로젝트를 추천하면 캐시에 저장하고 24시간 제한을 둠.
	 * @param project
	 * @param weaver
	 * @param ip
	 * @return
	 */
	public boolean push(Project project, Weaver weaver,String ip) {
		if(project == null || project.getCategory() > 0 || 
				(weaver == null &&  project.isProjectWeaver(weaver)))
			return false;

		Cache cache = cacheManager.getCache("push");
		Element element = cache.get(project.getName()+"@@"+ip);
		if (element == null || (element != null && element.getValue() == null)) {
			project.push();
			projectDao.update(project);
			Element newElement = new Element(project.getName()+"@@"+ip, ip);
			cache.put(newElement);
			return true;
		}
		return false;
	}


	public long countProjects(Weaver currentWeaver,List<String> tags,String search,String sort){
		if(currentWeaver != null && sort.equals("my"))
			return projectDao.countProjects(currentWeaver.getProjects(),tags,sort);

		return projectDao.countProjects(tags, search, sort);
	}

	public List<Project> getProjects(Weaver currentWeaver,List<String> tags,
			String search,String sort, int pageNumber,int lineNumber){
		List<Project> projects;

		if(currentWeaver != null && sort.equals("my"))
			projects = projectDao.getProjects(currentWeaver.getProjects(),tags,sort, pageNumber, lineNumber);
		else
			projects=  projectDao.getProjects(tags, search, sort, pageNumber, lineNumber);

		if(currentWeaver != null)
			for(Project project:projects){
				Pass pass = currentWeaver.getPass(project.getName());
				if(pass != null)
					project.setJoin(pass.getPermission());
				else
					project.setJoin(0);
			}
		return projects;
	}


	public long countProjects(Weaver weaver,List<String> tags,String sort){
		if(sort.equals("join"))
			return projectDao.countProjects(weaver.getJoinProjects(),tags,sort);
		else if(sort.equals("admin"))
			return projectDao.countProjects(weaver.getAdminProjects(),tags,sort);
		else
			return projectDao.countProjects(weaver.getProjects(),tags,sort);
	}

	/** 회원을 기준으로 프로젝트를 검색함.
	 * @param weaver
	 * @param tags
	 * @param sort
	 * @param page
	 * @param size
	 * @return
	 */
	public List<Project> getProjects(Weaver currentWeaver,Weaver weaver,List<String> tags,String sort,int page,int size){
		List<Project> projects=  new ArrayList<Project>();

		if(sort.equals("join"))
			projects = projectDao.getProjects(weaver.getJoinProjects(),tags,sort, page, size);
		else if(sort.equals("admin"))
			projects = projectDao.getProjects(weaver.getAdminProjects(),tags,sort, page, size);
		else
			projects = projectDao.getProjects(weaver.getProjects(),tags,sort, page, size);

		if(currentWeaver != null)
			for(Project project:projects){
				Pass pass = currentWeaver.getPass(project.getName());
				if(pass != null)
					project.setJoin(pass.getPermission());
				else
					project.setJoin(0);
			}
		return projects;
	}

	/** 압축파일을 올리면 자동으로 커밋함.
	 * @param project
	 * @param weaver
	 * @param branchName
	 * @param message
	 * @param zip
	 */
	public boolean uploadFile(Project project,Weaver weaver,String branchName,String message,String path,MultipartFile file){
		if(message==null || message.length() < 5 ||weaver  == null || 
				weaver.getPass(project.getName()) == null)
			return false;

		gitUtil.Init(project);

		if(file.getOriginalFilename().toUpperCase().endsWith(".ZIP"))
			gitUtil.uploadZip(weaver.getId(), weaver.getEmail(), branchName, message, file);
		else
			gitUtil.uploadFile(weaver.getId(), weaver.getEmail(), branchName, message, path, file);


		return true;
	}

	/** 파일을 수정하면 자동으로 커밋함.
	 * @param project
	 * @param weaver
	 * @param branchName
	 * @param message
	 * @param zip
	 */
	public boolean updateFile(Project project,Weaver weaver,String branchName,String message,String path,String code){
		if(message==null || message.length() < 5 ||weaver  == null || 
				weaver.getPass(project.getName()) == null)
			return false;

		gitUtil.Init(project);
		gitUtil.updateFile(weaver.getId(), weaver.getEmail(), branchName, message, path, code);

		return true;
	}

	// 프로젝트 초기화
	public boolean reSetRepository(Project project,Weaver weaver){
		if(!project.getCreator().equals(weaver))
			return false;
		gitUtil.Init(project);
		try{
			gitUtil.deleteRepository();
			gitUtil.createRepository();
			return true;
		}catch(Exception e){
			return false;
		}


	}

	public void update(Project project) {
		projectDao.update(project);
	}

}
