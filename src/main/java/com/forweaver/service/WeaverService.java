package com.forweaver.service;

import java.io.File;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.forweaver.domain.Data;
import com.forweaver.domain.Pass;
import com.forweaver.domain.RePassword;
import com.forweaver.domain.Weaver;

import com.forweaver.mongodb.dao.WeaverDao;
import com.forweaver.util.GitUtil;
import com.forweaver.util.MailUtil;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

@Service("userDetailsService")
public class WeaverService implements UserDetailsService {

	protected Log log = LogFactory.getLog(WeaverService.class);
	@Autowired 
	private WeaverDao weaverDao;
	@Autowired 
	private PasswordEncoder passwordEncoder;
	@Autowired @Qualifier("sessionRegistry")
	private SessionRegistry sessionRegistry;
	@Autowired 
	private CacheManager cacheManager;
	@Autowired 
	private MailUtil mailUtil;
	@Autowired 
	private GitUtil gitUtil;

	public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {
		// TODO Auto-generated method stub
		Weaver weaver = weaverDao.get(id);

		if(weaver == null || weaver.isLeave())
			return null;

		log.info("================ "+weaver.getId()+"  login   ================");
		return weaver;
	}

	public boolean idCheck(String id) { // 이름 중복 체크
		id = id.toLowerCase();
		if(id.equals("tracker") || id.equals("lecture") || id.equals("repassword") || id.equals("community") ||
				id.equals("forweaver") || id.equals("weaver") || id.startsWith("rule_") || id.equals("resources") ||
				id.startsWith("error") || id.equals("login") || id.equals("admin") || id.equals("check") || id.equals("chat") || 
				weaverDao.get(id) != null)
			return true;

		return false;
	}

	public Weaver getCurrentWeaver() {
		// TODO Auto-generated method stub
		Authentication auth = SecurityContextHolder.getContext()
				.getAuthentication();
		if (auth.getName().equals("anonymousUser"))
			return null;
		return (Weaver) auth.getPrincipal();
	}

	public String getUserIP() {
		// TODO Auto-generated method stub
		WebAuthenticationDetails details = (WebAuthenticationDetails)SecurityContextHolder.getContext().getAuthentication().getDetails();
		return details.getRemoteAddress();
	}

	public void add(Weaver weaver) { // 회원 추가 서비스
		Pass pass;
		if(weaverDao.existsWeaver())
			pass = new Pass("ROLE_USER"); 
		else
			pass = new Pass("ROLE_ADMIN"); // 최초 회원 가입시 운영자 지위
		weaver.addPass(pass);
		weaver.setPassword(passwordEncoder.encodePassword(weaver.getPassword(), null));
		weaverDao.insert(weaver);
		File file = new File(gitUtil.getGitPath() + weaver.getId());
		file.mkdir();
	}

	public void update(Weaver weaver,String password,String newpassword,List<String> tags,String studentID,String say,MultipartFile image) { // 회원 수정
		// TODO Auto-generated method stub
		if(image != null && image.getSize() > 0)
			weaver.setImage(new Data(image));

		if(this.validPassword(weaver,password) && newpassword != null && newpassword.length() > 3)
			weaver.setPassword(passwordEncoder.encodePassword(newpassword, null));

		if(say != null && !say.equals(""))
			weaver.setSay(say);

		weaverDao.update(weaver);
	}

	public void update(Weaver weaver) { // 회원 수정
		// TODO Auto-generated method stub

		weaverDao.update(weaver);
	}

	public Weaver get(String id) { // 회원이름으로 회원 불러오기
		Weaver weaver = this.getLoginWeaver(id);
		if (weaver == null)
			weaver = weaverDao.get(id);
		return weaver;
	}

	public Weaver getLoginWeaver(String id) {

		for (Object object : sessionRegistry.getAllPrincipals()) {
			Weaver weaver = ((Weaver) object);
			if (weaver.getId().equals(id))
				return weaver;
		}
		return null;
	}


	// 프로젝트 삭제시 로그인 된 위버의 pass 삭제
	public void deletePass(String passName) {
		for (Weaver weaver : weaverDao.searchPassName(passName)) {
			weaver.deletePass(passName);
			weaverDao.update(weaver);

			Weaver currentWeaver = getLoginWeaver(weaver.getId()); //만약 로그인한 회원이라면
			if (currentWeaver != null)
				currentWeaver.deletePass(passName);
		}

	}


	public boolean delete(Weaver weaver) { //위버 삭제
		// TODO Auto-generated method stub

		if(weaver == null)
			return false;

		try {
			FileUtils.deleteDirectory(new File(gitUtil.getGitPath() + weaver.getId()));
			weaverDao.delete(weaver);
		} catch (Exception e) {
			return false;
		}

		return true;
	}

	public boolean autoLoginWeaver(Weaver weaver, HttpServletRequest request) {
		boolean result = true;

		try {
			request.getSession();
			Authentication auth = new UsernamePasswordAuthenticationToken(
					weaver, null, weaver.getAuthorities());
			SecurityContextHolder.getContext().setAuthentication(auth);
		} catch (Exception e) {

			result = false;
		}

		return result;
	}


	/** 비밀번호를 재발급을 위한 메서드
	 * @param email
	 * @return 성공여부
	 */
	public boolean sendRepassword(String email){
		Cache rePasswordCache = cacheManager.getCache("repassword");
		Object object = rePasswordCache.get(email);

		if(object != null ||  weaverDao.get(email) == null) //등록된 이메일이 없을 경우.
			return false;
		String password = KeyGenerators.string().generateKey().substring(0, 7);
		String key = passwordEncoder.encodePassword(password, null);
		RePassword rePassword = new RePassword(key, password);

		mailUtil.sendMail(email,"[forweaver] 비밀번호 재발급",
				"링크 - http://forweaver.com/repassword/"+email+"/"+key+"\n"+
						"변경된 비밀번호 - "+password+"\n"+		
				"\n링크에 5분이내에 접속하시고 나서 변경된 비밀번호로 로그인해주세요!");
		Element newElement = new Element(email, rePassword);
		rePasswordCache.put(newElement);

		return true;
	}

	/** 인증된 키를 통해 재발급된 비밀번호로 변경하는 메서드
	 * @param email
	 * @param key
	 * @return 성공여부
	 */
	public boolean changePassword(String email,String key){
		Cache rePasswordCache = cacheManager.getCache("repassword");
		Element element = rePasswordCache.get(email);
		if(element == null)
			return false;
		RePassword rePassword =  (RePassword)element.getValue();

		if(rePassword.getKey().equals(key)){
			Weaver weaver = weaverDao.get(email);
			weaver.setPassword(passwordEncoder.encodePassword(rePassword.getPassword(),null));	
			weaverDao.update(weaver);
		}
		return true;
	}

	/** 회원의 원래 비밀번호와 입력한 비밀번호가 같은지 비교하는 메서드.
	 * @param weaver
	 * @param password
	 * @return
	 */
	public boolean validPassword(Weaver weaver,String password){
		if(password != null && password.length()>3 && 
				weaver.getPassword().equals(passwordEncoder.encodePassword(password, null)))
			return true;
		return false;
	}


	public void getWeaverInfos(Weaver weaver){
		BasicDBObject basicDB = new BasicDBObject();
		DBObject tempDB = weaverDao.getWeaverInfosInPost(weaver);
		System.out.println(weaver.getId());
		tempDB = weaverDao.getWeaverInfosInPost(weaver);
		if(tempDB != null){
			basicDB.put("postCount", tempDB.get("postCount"));
			basicDB.put("push", tempDB.get("push"));
			basicDB.put("rePostCount", tempDB.get("rePostCount"));
		}
		tempDB = weaverDao.getWeaverInfosInRePost(weaver);
		if(tempDB != null){
			basicDB.put("myRePostCount", tempDB.get("myRePostCount"));
			basicDB.put("rePostPush", tempDB.get("rePostPush"));
		}
		tempDB = weaverDao.getWeaverInfosInProject(weaver);
		if(tempDB != null){
			basicDB.put("projectPush", tempDB.get("projectPush"));
			basicDB.put("childProjects", tempDB.get("childProjects"));
		}
		tempDB = weaverDao.getWeaverInfosInLecture(weaver);
		if(tempDB != null){
			basicDB.put("repos", tempDB.get("repos"));
			basicDB.put("joinWeavers", tempDB.get("joinWeavers"));
		}
		tempDB = weaverDao.getWeaverInfosInCode(weaver);
		if(tempDB != null){
			basicDB.put("codeCount", tempDB.get("codeCount"));
			basicDB.put("downCount", tempDB.get("downCount"));
		}
		weaver.setWeaverInfo(basicDB);
	}

	public long countWeavers(){
		return weaverDao.countWeavers();
	}

	public List<Weaver> getWeavers(int page, int size) {
		List<Weaver> weavers = weaverDao.getWeavers(page, size);
		for(Weaver weaver : weavers)
			this.getWeaverInfos(weaver);
		return weavers;
	}

	/** 태그를 가지고 위버를 검색하고 활동내역도 검색함.
	 * @param tags
	 * @param page
	 * @param size
	 * @return
	 */
	public List<Weaver> getWeavers(List<String> tags,int page, int size ){
		List<Weaver> weavers = weaverDao.getWeavers(tags,page, size);
		for(Weaver weaver : weavers)
			this.getWeaverInfos(weaver);
		return weavers;
	}


	/** 태그를 가지고 위버를 검색하고 숫자를 셈.
	 * @param tags
	 * @return
	 */
	public long countWeavers(List<String> tags){
		return weaverDao.countWeavers(tags);
	}


}
