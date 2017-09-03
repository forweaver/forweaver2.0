package com.forweaver.service;

import java.util.ArrayList;
import java.util.List;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.forweaver.domain.Pass;
import com.forweaver.domain.Repository;
import com.forweaver.domain.Invite;
import com.forweaver.domain.Weaver;
import com.forweaver.mongodb.dao.InviteDao;
import com.forweaver.mongodb.dao.PostDao;
import com.forweaver.mongodb.dao.RepositoryDao;
import com.forweaver.mongodb.dao.WeaverDao;
import com.forweaver.util.GitUtil;
import com.forweaver.util.SVNUtil;

/** 저장소 관리 서비스
 *
 */
@Service
public class RepositoryService{

	private static final Logger logger =
			LoggerFactory.getLogger(RepositoryService.class);
	
	@Autowired private WeaverDao weaverDao;
	@Autowired private RepositoryDao repositoryDao;
	@Autowired private CacheManager cacheManager;
	@Autowired private InviteDao invateDao;
	@Autowired private PostDao postDao;
	@Autowired private GitUtil gitUtil;
	@Autowired private SVNUtil svnUtil;

	@Autowired @Qualifier("sessionRegistry")
	private SessionRegistry sessionRegistry;
	/** 저장소 추가하는 메서드
	 * @param repository
	 * @param currentWeaver
	 */
	public void add(Repository repository,Weaver currentWeaver){
		//System.out.println("- project type: " + repository.getType());
		// TODO Auto-generated method stub
		if(repository.getType() == 1){
			logger.debug("git project init");
			if(currentWeaver == null)
				return;

			gitUtil.Init(repository);
			
			if(!gitUtil.createRepository())
				return;
		} else if(repository.getType() == 2){
			logger.debug("svn project init");
			logger.debug("<<weaver info>>");
			logger.debug("id: " + currentWeaver.getId());
			logger.debug("password: " + currentWeaver.getPassword());
			logger.debug("username: " + currentWeaver.getUsername());
			logger.debug("email: " + currentWeaver.getEmail());
			
			logger.debug("<<Repository info>>");
			logger.debug("Category: " + repository.getType());
			logger.debug("Category name: " + repository.getCreatorName());
			logger.debug("Category email: " + repository.getCreatorEmail());
			logger.debug("Category description : " + repository.getDescription());
			logger.debug("Category projectname: " + repository.getName());
			
			if(currentWeaver == null)
				return;
			
			svnUtil.Init(repository);
				
			if(!svnUtil.createRepository())
				return;
		}
		
		repositoryDao.insert(repository);
		Pass pass = new Pass(repository.getName(),2);
		currentWeaver.addPass(pass);
		weaverDao.updatePass(currentWeaver);
	}

	/** 회원 추가함.
	 * @param repository
	 * @param currentWeaver
	 * @param joinWeaver
	 */
	public boolean addWeaver(Repository repository,Weaver joinWeaver){
		// TODO Auto-generated method stub
		if(repository == null || joinWeaver == null)
			return false;

		Pass pass = new Pass(repository.getName(), 1);
		repository.addJoinWeaver(joinWeaver); //저장소 목록에 추가
		joinWeaver.addPass(pass);
		weaverDao.updatePass(joinWeaver);
		this.update(repository);

		return true;
	}


	/** 저장소 이름으로 불러오기.
	 * @param repositoryName
	 * @return
	 */
	public Repository get(String repositoryName) {
		// TODO Auto-generated method stub
		return repositoryDao.get(repositoryName);
	}


	public boolean delete(Weaver weaver,Repository repository){
		// TODO Auto-generated method stub
		if(weaver == null || repository == null)
			return false;
		if(weaver.isAdmin() || weaver.equals(repository.getCreator())){

			gitUtil.Init(repository);
			if(!gitUtil.deleteRepository())
				return false;

			for(Weaver joinWeaver:repository.getJoinWeavers()){
				joinWeaver.deletePass(repository.getName());
				weaverDao.updatePass(joinWeaver);

				for (Object object : sessionRegistry.getAllPrincipals()) { //현재 로그인 중인 회원의 권한 삭제.
					Weaver tmpWeaver = ((Weaver) object);
					if (tmpWeaver.equals(joinWeaver))
						joinWeaver.deletePass(repository.getName());
				}
			}
			for(Invite invate:invateDao.delete(repository.getName())){ // 대기 중인 초대장 삭제.
				postDao.delete(postDao.get(invate.getPostID())); //처음 보냈던 메세지 삭제.
				return true;
			}

			repository.getCreator().deletePass(repository.getName());
			weaverDao.updatePass(repository.getCreator());
			repositoryDao.delete(repository);
			return true;
		}
		return false;

	}


	public boolean deleteWeaver(Repository repository, Weaver currentWeaver,Weaver deleteWeaver) {
		// 저장소에 동료를 탈퇴시킴
		if(repository == null || deleteWeaver == null || deleteWeaver.getPass(repository.getName()) == null)
			return false;

		if(repository.getCreator().equals(currentWeaver) ||  //관리자가 탈퇴시키거나
				currentWeaver.equals(deleteWeaver)){ //본인이 나가거나
			deleteWeaver.deletePass(repository.getName());
			repository.removeJoinWeaver(deleteWeaver);
			weaverDao.updatePass(deleteWeaver);
			repositoryDao.update(repository);

			return true;
		}

		return false;

	}

	/** 저장소를 추천하면 캐시에 저장하고 24시간 제한을 둠.
	 * @param repository
	 * @param weaver
	 * @param ip
	 * @return
	 */
	public boolean push(Repository repository, Weaver weaver,String ip) {
		if(repository == null || repository.getAuthLevel() > 0 || 
				(weaver == null &&  repository.isJoinWeaver(weaver)))
			return false;

		Cache cache = cacheManager.getCache("push");
		Element element = cache.get(repository.getName()+"@@"+ip);
		if (element == null || (element != null && element.getValue() == null)) {
			repository.push();
			repositoryDao.update(repository);
			Element newElement = new Element(repository.getName()+"@@"+ip, ip);
			cache.put(newElement);
			return true;
		}
		return false;
	}


	public long countRepositories(Weaver currentWeaver,List<String> tags,String search,String sort){
		if(currentWeaver != null && sort.equals("my"))
			return repositoryDao.countRepositories(currentWeaver.getRepositories(),tags,sort);

		return repositoryDao.countRepositories(tags, search, sort);
	}

	public List<Repository> getRepositories(Weaver currentWeaver,List<String> tags,
			String search,String sort, int pageNumber,int lineNumber){
		List<Repository> repositorys;

		if(currentWeaver != null && sort.equals("my"))
			repositorys = repositoryDao.getRepositories(currentWeaver.getRepositories(),tags,sort, pageNumber, lineNumber);
		else
			repositorys=  repositoryDao.getRepositories(tags, search, sort, pageNumber, lineNumber);

		if(currentWeaver != null)
			for(Repository repository:repositorys){
				Pass pass = currentWeaver.getPass(repository.getName());
				if(pass != null)
					repository.setJoin(pass.getPermission());
				else
					repository.setJoin(0);
			}
		return repositorys;
	}


	public long countRepositories(Weaver weaver,List<String> tags,String sort){
		if(sort.equals("join"))
			return repositoryDao.countRepositories(weaver.getJoinRepositories(),tags,sort);
		else if(sort.equals("admin"))
			return repositoryDao.countRepositories(weaver.getAdminRepositories(),tags,sort);
		else
			return repositoryDao.countRepositories(weaver.getRepositories(),tags,sort);
	}

	/** 회원을 기준으로 저장소를 검색함.
	 * @param weaver
	 * @param tags
	 * @param sort
	 * @param page
	 * @param size
	 * @return
	 */
	public List<Repository> getRepositories(Weaver currentWeaver,Weaver weaver,List<String> tags,String sort,int page,int size){
		List<Repository> repositorys=  new ArrayList<Repository>();

		if(sort.equals("join"))
			repositorys = repositoryDao.getRepositories(weaver.getJoinRepositories(),tags,sort, page, size);
		else if(sort.equals("admin"))
			repositorys = repositoryDao.getRepositories(weaver.getAdminRepositories(),tags,sort, page, size);
		else
			repositorys = repositoryDao.getRepositories(weaver.getRepositories(),tags,sort, page, size);

		if(currentWeaver != null)
			for(Repository repository:repositorys){
				Pass pass = currentWeaver.getPass(repository.getName());
				if(pass != null)
					repository.setJoin(pass.getPermission());
				else
					repository.setJoin(0);
			}
		return repositorys;
	}

	/** 압축파일을 올리면 자동으로 커밋함.
	 * @param repository
	 * @param weaver
	 * @param branchName
	 * @param message
	 * @param zip
	 */
	public boolean uploadFile(Repository repository,Weaver weaver,String branchName,String message,String path,MultipartFile file){
		if(message==null || message.length() < 5 ||weaver  == null || 
				weaver.getPass(repository.getName()) == null)
			return false;

		gitUtil.Init(repository);

		if(file.getOriginalFilename().toUpperCase().endsWith(".ZIP"))
			gitUtil.uploadZip(weaver.getId(), weaver.getEmail(), branchName, message, file);
		else
			gitUtil.uploadFile(weaver.getId(), weaver.getEmail(), branchName, message, path, file);


		return true;
	}

	/** 파일을 수정하면 자동으로 커밋함.
	 * @param repository
	 * @param weaver
	 * @param branchName
	 * @param message
	 * @param zip
	 */
	public boolean updateFile(Repository repository,Weaver weaver,String branchName,String message,String path,String code){
		logger.debug("==> repository type: " + repository.getType());
		logger.debug("==> commit: " + branchName);
		logger.debug("==> message: " + message);
		logger.debug("==> path: " + path);
		logger.debug("==> code: " + code);
		
		if(message==null || message.length() < 5 ||weaver  == null || 
				weaver.getPass(repository.getName()) == null)
			return false;

		if(repository.getType() == 1){
			gitUtil.Init(repository);
			gitUtil.updateFile(weaver.getId(), weaver.getEmail(), branchName, message, path, code);
		} else if(repository.getType() == 2){
			svnUtil.Init(repository);
			svnUtil.updateFile(weaver.getId(), weaver.getEmail(), branchName, message, path, code);
		}

		return true;
	}

	// 저장소 초기화
	public boolean reSetRepository(Repository repository,Weaver weaver){
		if(!repository.getCreator().equals(weaver))
			return false;
		gitUtil.Init(repository);
		try{
			gitUtil.deleteRepository();
			gitUtil.createRepository();
			return true;
		}catch(Exception e){
			return false;
		}


	}

	public void update(Repository repository) {
		repositoryDao.update(repository);
	}

}
