package com.forweaver.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.forweaver.domain.Data;
import com.forweaver.domain.Post;
import com.forweaver.domain.Repository;
import com.forweaver.domain.Invite;
import com.forweaver.domain.Weaver;
import com.forweaver.domain.vc.VCLog;
import com.forweaver.domain.vc.VCFileInfo;
import com.forweaver.domain.vc.VCSimpleFileInfo;
import com.forweaver.service.DataService;
import com.forweaver.service.GitService;
import com.forweaver.service.PostService;
import com.forweaver.service.RepositoryService;
import com.forweaver.service.TagService;
import com.forweaver.service.InviteService;
import com.forweaver.service.WeaverService;
import com.forweaver.util.WebUtil;

@Controller
@RequestMapping("/repository")
public class RepositoryController {

	@Autowired 
	private InviteService invateService;
	@Autowired 
	private WeaverService weaverService;
	@Autowired 
	private RepositoryService repositoryService;
	@Autowired 
	private PostService postService;
	@Autowired 
	private GitService gitService;
	@Autowired 
	private TagService tagService;
	@Autowired 
	private DataService dataService;

	@RequestMapping("/")
	public String repositorys() {
		return "redirect:/repository/sort:age-desc/page:1";
	}

	@RequestMapping(value = {"/{creatorName}/{repositoryName}/{logName}/{download}.zip"
			,"/{creatorName}/{repositoryName}/{logName}/{download}.tar"
	})
	public void getRepositoryZip(@PathVariable("repositoryName") String repositoryName,
			@PathVariable("creatorName") String creatorName,
			@PathVariable("logName") String logName,
			HttpServletRequest request,
			HttpServletResponse response) {

		if(!gitService.existCommit(creatorName, repositoryName, logName))
			return;
		String url = request.getRequestURI();
		String format = url.substring(url.lastIndexOf(".")+1);
		gitService.getRepositoryZip(creatorName, repositoryName, logName, format,response);

		return;
	}

	@RequestMapping("/sort:{sort}/page:{page}")
	public String repositorysWithPage(@PathVariable("page") String page,
			@PathVariable("sort") String sort,Model model) {
		int pageNum = WebUtil.getPageNumber(page);
		int size = WebUtil.getPageSize(page,0);

		Weaver currentWeaver = weaverService.getCurrentWeaver();
		model.addAttribute("repositorys", 
				repositoryService.getRepositories(currentWeaver, null, "", sort, pageNum, size));
		model.addAttribute("repositoryCount", repositoryService.countRepositories(currentWeaver,null, "", sort));
		model.addAttribute("pageIndex", pageNum);
		model.addAttribute("number", size);
		model.addAttribute("pageUrl", "/repository/sort:"+sort+"/page:");
		return "/repository/repositories";
	}



	@RequestMapping("/tags:{tagNames}")
	public String repositorysWithTags(HttpServletRequest request){
		return "redirect:"+request.getRequestURI() +"/sort:age-desc/page:1";
	}

	@RequestMapping("/tags:{tagNames}/sort:{sort}/page:{page}")
	public String repositorysWithTags(@PathVariable("tagNames") String tagNames,
			@PathVariable("page") String page,
			@PathVariable("sort") String sort,Model model) {
		List<String> tagList = tagService.stringToTagList(tagNames);
		int pageNum = WebUtil.getPageNumber(page);
		int size = WebUtil.getPageSize(page,0);

		Weaver currentWeaver = weaverService.getCurrentWeaver();
		model.addAttribute("repositorys", 
				repositoryService.getRepositories(currentWeaver, tagList, null, sort, pageNum, size));
		model.addAttribute("repositoryCount", repositoryService.countRepositories(currentWeaver,tagList, null, sort));
		model.addAttribute("pageIndex", pageNum);
		model.addAttribute("number", size);
		model.addAttribute("pageUrl", "/repository/tags:"+tagNames+"/sort:"+sort+"/page:");
		return "/repository/repositories";
	}

	@RequestMapping("/tags:{tagNames}/search:{search}")
	public String tagsWithSearch(HttpServletRequest request){
		return "redirect:"+request.getRequestURI() +"/sort:age-desc/page:1";
	}

	@RequestMapping("/tags:{tagNames}/search:{search}/sort:{sort}/page:{page}")
	public String tagsWithSearch(@PathVariable("tagNames") String tagNames,
			@PathVariable("page") String page,
			@PathVariable("search") String search,
			@PathVariable("sort") String sort,Model model) {
		List<String> tagList = tagService.stringToTagList(tagNames);
		int pageNum = WebUtil.getPageNumber(page);
		int size = WebUtil.getPageSize(page,0);

		Weaver currentWeaver = weaverService.getCurrentWeaver();
		model.addAttribute("repositorys", repositoryService.getRepositories(currentWeaver,tagList,search,sort, pageNum, size));
		model.addAttribute("repositoryCount", repositoryService.countRepositories(currentWeaver,tagList,search,sort));
		model.addAttribute("pageIndex", pageNum);
		model.addAttribute("number", size);
		model.addAttribute("pageUrl", "/repository/tags:"+tagNames+"/search:"+search+"/sort:"+sort+"/page:");
		return "/repository/repositories";
	}

	@RequestMapping(value = "/add", method = RequestMethod.POST)
	public String add(@RequestParam Map<String, String> params,Model model) {
		Weaver currentWeaver = weaverService.getCurrentWeaver();
		List<String> tagList = tagService.stringToTagList(params.get("tags"));
		int authLevel = 0;
		if(params.get("authLevel") != null)
			authLevel = Integer.parseInt(params.get("authLevel"));

		int type = 1;
		if(params.get("type") != null)
			type = Integer.parseInt(params.get("type"));
		
		if(!Pattern.matches("^[a-z]{1}[a-z0-9_]{4,14}$", params.get("name")) || 
				params.get("name").length() <5 || 
				params.get("description").length() <5 || 
				params.get("description").length() > 50 || 
				!tagService.isPublicTags(tagList)){
			model.addAttribute("say", "잘못 입력하셨습니다!!!");
			model.addAttribute("url", "/repository/");
			return "/alert";
		}
		Repository repository = new Repository(params.get("name"), 
				authLevel,
				type,
				params.get("description"), 
				currentWeaver,
				tagList);

		repositoryService.add(repository,currentWeaver);
		return "redirect:/repository/";//+repository.getName();
	}


	@RequestMapping("/{creatorName}/{repositoryName}/delete")
	public String delete(Model model,
			@PathVariable("creatorName") String creatorName,
			@PathVariable("repositoryName") String repositoryName) {
		Repository repository = repositoryService.get(creatorName+"/"+repositoryName);
		Weaver currentWeaver = weaverService.getCurrentWeaver();
		List<String> tags = new ArrayList<String>();
		tags.add("@"+repository.getName());
		
		if(repositoryService.delete(currentWeaver, repository))
			for(Post post:postService.getPosts(tags, null, null, "", 1, Integer.MAX_VALUE)) // 저장소에 쓴 글 모두 삭제
				postService.delete(post);
			
		else{
			model.addAttribute("say", "삭제하지 못하였습니다!!!");
			model.addAttribute("url", "/repository/"+creatorName+"/"+repositoryName);
			return "/alert";
		}

		return "redirect:/repository/";
	}


	@RequestMapping(value = {"/{creatorName}/{repositoryName}/data/log:{log}/**"})
	public void data(HttpServletRequest request,@PathVariable("repositoryName") String repositoryName,
			@PathVariable("creatorName") String creatorName,
			@PathVariable("log") String log,HttpServletResponse res) throws IOException {
		String uri = URLDecoder.decode(request.getRequestURI(),"UTF-8");
		String filePath = uri.substring(uri.indexOf("filepath:")+9);
		filePath = filePath.replace(",jsp", ".jsp");

		log = uri.substring(uri.indexOf("/log:")+5);
		log = log.substring(0, log.indexOf("/"));

		VCFileInfo gitFileInfo = gitService.getFileInfo(creatorName, repositoryName, log, filePath);

		if (gitFileInfo == null) {
			return;
		} else {
			byte[] imgData = gitFileInfo.getData();

			res.reset();
			res.setContentType("application/octet-stream");
			String filename = new String(gitFileInfo.getName().getBytes("UTF-8"), "8859_1");
			res.setHeader("Content-Disposition", "attachment; filename = " + filename);
			res.setContentType(WebUtil.getFileExtension(gitFileInfo.getName()));
			OutputStream o = res.getOutputStream();
			o.write(imgData);
			o.flush();
			o.close();
			return;
		} 

	}

	@RequestMapping(value=
		{	"/{creatorName}/{repositoryName}", 
		"/{creatorName}/{repositoryName}/browser"}
			)
	public String browser(@PathVariable("repositoryName") String repositoryName,
			@PathVariable("creatorName") String creatorName) {		
		List<String> gitBranchList = gitService.getBranchList(creatorName, repositoryName);
		return "redirect:/repository/"+creatorName+"/"+repositoryName+"/browser/log:"+gitBranchList.get(0)+"/filepath:/";
	}

	@RequestMapping("/{creatorName}/{repositoryName}/browser/log:{log}")
	public String fileBrowser(HttpServletRequest request){
		return "redirect:"+request.getRequestURI()+"/filepath:/"; 
	}

	@RequestMapping("/{creatorName}/{repositoryName}/browser/log:{log}/**")
	public String fileBrowser(HttpServletRequest request,@PathVariable("repositoryName") String repositoryName,
			@PathVariable("creatorName") String creatorName,
			@PathVariable("log") String log,Model model) throws UnsupportedEncodingException  {
		Repository repository = repositoryService.get(creatorName+"/"+repositoryName);
		String uri = URLDecoder.decode(request.getRequestURI(),"UTF-8");
		String filePath = uri.substring(uri.indexOf("filepath:")+9);
		filePath = filePath.replace(",jsp", ".jsp");
		
		log = uri.substring(uri.indexOf("/log:")+5);
		log = log.substring(0, log.indexOf("/"));

		VCFileInfo gitFileInfo = gitService.getFileInfo(creatorName, repositoryName, log, filePath);


		if(gitFileInfo ==null || gitFileInfo.isDirectory()){ // 만약에 주소의 파일이 디렉토리라면
			List<VCSimpleFileInfo> gitFileInfoList = 
					gitService.getGitSimpleFileInfoList(creatorName, repositoryName,log,filePath);

			List<String> gitBranchList = gitService.getBranchList(creatorName, repositoryName);
			gitBranchList.remove(log);

			model.addAttribute("repository", repository);
			model.addAttribute("gitFileInfoList", gitFileInfoList);

			model.addAttribute("gitBranchList", gitBranchList);
			model.addAttribute("selectBranch",log);
			model.addAttribute("readme",gitService.getReadme(creatorName, repositoryName,log,gitFileInfoList));
			model.addAttribute("filePath",filePath);
			model.addAttribute("log",log);

			return "/repository/browser";
		}else{ // 파일이라면
			model.addAttribute("repository", repository);
			model.addAttribute("fileName", gitFileInfo.getName());
			if(!WebUtil.isCodeName(gitFileInfo.getName()))
				gitFileInfo.setContent("이 파일은 화면에 표시할 수 없습니다!");
			if(gitFileInfo.getContent() != null)
				model.addAttribute("fileContent", new String(gitFileInfo.getContent().getBytes(Charset.forName("EUC-KR")),Charset.forName("CP949")));
			model.addAttribute("gitLogList", gitFileInfo.getLogList());
			model.addAttribute("selectCommitIndex", gitFileInfo.getSelectCommitIndex());
			model.addAttribute("gitLog",gitFileInfo.getSelectLog());
			model.addAttribute("filePath",filePath);
			model.addAttribute("isCodeName",WebUtil.isCodeName(filePath));
			model.addAttribute("isImageName",WebUtil.isImageName(filePath));
			return "/repository/fileViewer";
		}


	}

	@RequestMapping("/{creatorName}/{repositoryName}/edit/log:{log}/**")
	public String fileEdit(HttpServletRequest request,@PathVariable("repositoryName") String repositoryName,
			@PathVariable("creatorName") String creatorName,
			@PathVariable("log") String log,Model model) throws UnsupportedEncodingException  {
		Repository repository = repositoryService.get(creatorName+"/"+repositoryName);
		String uri = URLDecoder.decode(request.getRequestURI(),"UTF-8");
		String filePath = uri.substring(uri.indexOf("filepath:")+9);
		filePath = filePath.replace(",jsp", ".jsp");

		if(!WebUtil.isCodeName(filePath)) //소스코드만 수정 가능함.
			return "redirect:/repository/"+creatorName+"/"+repositoryName+"/browser/log:"+log+"/filepath:"+filePath;

		log = uri.substring(uri.indexOf("/log:")+5);
		log = log.substring(0, log.indexOf("/"));

		VCFileInfo gitFileInfo = gitService.getFileInfo(creatorName, repositoryName, log, filePath);
		model.addAttribute("repository", repository);
		model.addAttribute("fileName", gitFileInfo.getName());
		if(gitFileInfo.getContent() != null)
			model.addAttribute("fileContent", new String(gitFileInfo.getContent().getBytes(Charset.forName("EUC-KR")),Charset.forName("CP949")));
		model.addAttribute("gitLogList", gitFileInfo.getLogList());
		model.addAttribute("selectCommitIndex", gitFileInfo.getSelectCommitIndex());
		model.addAttribute("gitLog",gitFileInfo.getSelectLog());
		model.addAttribute("filePath",filePath);
		model.addAttribute("log",log);

		return "/repository/fileEdit";
	}

	@RequestMapping(value="/{creatorName}/{repositoryName}/file-edit",method = RequestMethod.POST )
	public String fileEdit(@PathVariable("repositoryName") String repositoryName,
			@PathVariable("creatorName") String creatorName,
			@RequestParam("log") String log,
			@RequestParam("message") String message,
			@RequestParam("path") String path,
			@RequestParam("code") String code,Model model)  {
		Repository repository = repositoryService.get(creatorName+"/"+repositoryName);
		Weaver currentWeaver = weaverService.getCurrentWeaver();

		if(!repositoryService.updateFile(repository, currentWeaver,log, message,path, code)){
			model.addAttribute("say", "업로드 실패! 저장소에 가입되어 있는지 혹은 최신 커밋의 파일인지 확인해보세요!");
			model.addAttribute("url", "/repository/"+creatorName+"/"+repositoryName+"/edit/log:"+log+"/filepath:/"+path);
			return "/alert";
		}
		return "redirect:/repository/"+creatorName+"/"+repositoryName+"/browser/log:"+log+"/filepath:/"+path;
	}

	@RequestMapping("/{creatorName}/{repositoryName}/blame/log:{log}/**")
	public String blame(HttpServletRequest request, @PathVariable("repositoryName") String repositoryName,
			@PathVariable("creatorName") String creatorName,
			@PathVariable("log") String log,Model model) throws UnsupportedEncodingException{
		Repository repository = repositoryService.get(creatorName+"/"+repositoryName);
		String uri = URLDecoder.decode(request.getRequestURI(),"UTF-8");
		String filePath = uri.substring(uri.indexOf("filepath:")+9);
		filePath = filePath.replace(",jsp", ".jsp");

		if(!WebUtil.isCodeName(filePath)) //소스코드만 추적 가능함.
			return "redirect:/repository/"+creatorName+"/"+repositoryName+"/browser/log:"+log+"/filepath:"+filePath;

		log = uri.substring(uri.indexOf("/log:")+5);
		log = log.substring(0, log.indexOf("/"));		
		VCFileInfo gitFileInfo = gitService.getFileInfoWithBlame(creatorName, repositoryName, log, filePath);

		if(gitFileInfo==null || gitFileInfo.isDirectory()) // 디렉토리의 경우 blame 기능을 이용할 수 없어 저장소 메인으로 돌려보냄.
			return "redirect:/repository/"+creatorName+"/"+repositoryName;

		model.addAttribute("repository", repository);
		model.addAttribute("fileName", gitFileInfo.getName());
		if(gitFileInfo.getContent() != null)
			model.addAttribute("fileContent", gitFileInfo.getContent());
		model.addAttribute("gitLogList", gitFileInfo.getLogList());
		model.addAttribute("gitBlameList", gitFileInfo.getBlames());
		model.addAttribute("selectCommitIndex", gitFileInfo.getSelectCommitIndex());
		model.addAttribute("gitLog", gitFileInfo.getSelectLog());
		return "/repository/blame";
	}

	@RequestMapping("/{creatorName}/{repositoryName}/community")
	public String community(HttpServletRequest request) {
		return "redirect:"+request.getRequestURI() +"/sort:age-desc/page:1";
	}

	@RequestMapping("/{creatorName}/{repositoryName}/community/sort:{sort}/page:{page}")
	public String community(@PathVariable("repositoryName") String repositoryName,
			@PathVariable("sort") String sort,
			@PathVariable("creatorName") String creatorName,
			@PathVariable("page") String page,Model model) {
		int pageNum = WebUtil.getPageNumber(page);
		int size = WebUtil.getPageSize(page,0);
		
		Repository repository = repositoryService.get(creatorName+"/"+repositoryName);
		List<String> tags = new ArrayList<String>();
		tags.add("@"+repository.getName());
		
		model.addAttribute("repository", repository);
		model.addAttribute("posts", 
				postService.getPosts(tags, null, null, sort, pageNum, size));
		model.addAttribute("postCount", 
				postService.countPosts(tags, null, null, sort));
		model.addAttribute("pageIndex", pageNum);
		model.addAttribute("pageUrl", "/repository/"+creatorName+"/"+repositoryName+"/community/sort:"+sort+"/page:");
		return "/repository/community";
	}

	@RequestMapping("/{creatorName}/{repositoryName}/community/tags:{tagNames}")
	public String tags(HttpServletRequest request){
		return "redirect:"+request.getRequestURI() +"/sort:age-desc/page:1";
	}

	@RequestMapping("/{creatorName}/{repositoryName}/community/tags:{tagNames}/sort:{sort}/page:{page}")
	public String tags(@PathVariable("repositoryName") String repositoryName,
			@PathVariable("creatorName") String creatorName,
			@PathVariable("tagNames") String tagNames,
			@PathVariable("sort") String sort,
			@PathVariable("page") String page,Model model){
		int pageNum = WebUtil.getPageNumber(page);
		int size = WebUtil.getPageSize(page,0);	

		Repository repository = repositoryService.get(creatorName+"/"+repositoryName);
		List<String> tagList = tagService.stringToTagList(tagNames);
		tagList.add("@"+repository.getName());
		
		model.addAttribute("repository", repository);
		model.addAttribute("posts", 
				postService.getPosts(tagList, null, null, sort, pageNum, size));
		model.addAttribute("postCount", 
				postService.countPosts(tagList, null, null, sort));

		model.addAttribute("pageIndex", pageNum);
		model.addAttribute("pageUrl", 
				"/repository/"+repositoryName+"/community/tags:"+tagNames+"/sort:"+sort+"/page:");
		return "/repository/community";
	}

	@RequestMapping(value = "/{creatorName}/{repositoryName}/community/add", method = RequestMethod.POST)
	public String addPost(@PathVariable("repositoryName") String repositoryName,
			@PathVariable("creatorName") String creatorName,
			HttpServletRequest request,Model model) {
		final MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
		final Map<String, MultipartFile> files = multiRequest.getFileMap();
		ArrayList<Data> datas = new ArrayList<Data>();

		String tags = request.getParameter("tags");
		String title = request.getParameter("title");
		String content = request.getParameter("content");

		if(title.length() < 5 || title.length() > 200
				|| (content.length() >0 && content.length() < 5)){ // 검증함
			model.addAttribute("say", "잘못 입력하셨습니다!!!");
			model.addAttribute("url", "/repository/"+creatorName+"/"+repositoryName+"/community/");
			return "/alert";
		}

		List<String> tagList = tagService.stringToTagList(tags);
		tagList.add(new String("@"+creatorName+"/"+repositoryName));
		Weaver weaver = weaverService.getCurrentWeaver();

		if(!tagService.validateTag(tagList,weaver)){ // 태그에 권한이 없을때
			model.addAttribute("say", "태그에 권한이 없습니다!!!");
			model.addAttribute("url", "/repository/"+creatorName+"/"+repositoryName+"/community/");
			return "/alert";
		}

		for (MultipartFile file : files.values())
			if(!file.isEmpty()){
				String fileID= dataService.getObjectID(file.getOriginalFilename(), weaver);
				if(!fileID.equals(""))
					datas.add(new Data(fileID,file,weaver));
			}

		Post post = new Post(weaver,title,content,tagList);

		postService.add(post,datas);
		return "redirect:/repository/"+creatorName+"/"+repositoryName+"/community/";
	}

	@RequestMapping("/{creatorName}/{repositoryName}/log")
	public String log(@PathVariable("repositoryName") String repositoryName,
			@PathVariable("creatorName") String creatorName,Model model) {
		Repository repository = repositoryService.get(creatorName+"/"+repositoryName);

		List<String> gitBranchList = gitService.getBranchList(creatorName, repositoryName);

		model.addAttribute("gitBranchList", gitBranchList.subList(1, gitBranchList.size()));
		model.addAttribute("selectBranch",gitBranchList.get(0));
		model.addAttribute("repository", repository);
		model.addAttribute("pageIndex",1);
		model.addAttribute("gitCommitListCount", 
				gitService.getCommitListCount(creatorName, repositoryName,gitBranchList.get(0)));
		model.addAttribute("gitCommitList", 
				gitService.getGitLogList(creatorName, repositoryName,gitBranchList.get(0),1,15));

		return "/repository/log";
	}
	/*
	@RequestMapping("/{creatorName}/{repositoryName}/rss") 
	@ResponseBody
	public String rss(@PathVariable("repositoryName") String repositoryName,
			@PathVariable("creatorName") String creatorName,Model model) {
		List<String> gitBranchList = gitService.getBranchList(creatorName, repositoryName);

		String rss = "<?xml version='1.0' encoding='UTF-8'?><rss version='2.0'><channel>";

		rss +="<title>repository:"+creatorName+"/"+repositoryName+"</title>";
		rss +="<link>http://forweaver.com/repository/"+creatorName+"/"+repositoryName+"</link>";
		rss +="<description>repository:"+creatorName+"/"+repositoryName+"</description>";

		for(GitSimpleLog log:gitService.getGitLogList(creatorName, repositoryName,gitBranchList.get(0),1,10)){
			rss +="<item>";
			rss +="<author>"+log.getCommiterName()+" ("+log.getCommiterEmail()+")</author>";
			rss +="<title>"+log.getShortMassage()+"</title>";
			rss +="<link>http://forweaver.com/repository/"+creatorName+"/"+repositoryName+"</link>";
			rss +="<description>"+log.getShortMassage()+"</description>";
			rss +="<pubDate>"+log.getCommitDate()+"</pubDate>";
			rss +="<image>http://forweaver.com/"+log.getImgSrc()+"</image>";
			rss +="</item>";
		}
		return rss+"</channel></rss>";
	}

	 */

	@RequestMapping("/{creatorName}/{repositoryName}/log/log:{log}")
	public String log(@PathVariable("repositoryName") String repositoryName,
			@PathVariable("creatorName") String creatorName,
			@PathVariable("log") String log,HttpServletRequest request,Model model) {
		Repository repository = repositoryService.get(creatorName+"/"+repositoryName);
		String uri = request.getRequestURI();
		log = uri.substring(uri.indexOf("/log:")+5);
		List<String> gitBranchList = gitService.getBranchList(creatorName, repositoryName);
		gitBranchList.remove(log);
		model.addAttribute("gitBranchList", gitBranchList);
		model.addAttribute("selectBranch",log);
		model.addAttribute("repository", repository);
		model.addAttribute("pageIndex",1);
		model.addAttribute("gitCommitListCount", 
				gitService.getCommitListCount(creatorName, repositoryName,log));
		model.addAttribute("gitCommitList", 
				gitService.getGitLogList(creatorName, repositoryName,log,1,15));
		return "/repository/log";
	}

	@RequestMapping("/{creatorName}/{repositoryName}/edit")
	public String edit(@PathVariable("repositoryName") String repositoryName,
			@PathVariable("creatorName") String creatorName,Model model) {
		Repository repository = repositoryService.get(creatorName+"/"+repositoryName);
		Weaver currentWeaver = weaverService.getCurrentWeaver();
		if(!repository.getCreator().equals(currentWeaver))
			return "redirect:/repository/"+repository.getName()+"/";
		model.addAttribute("repository", repository);
		return "/repository/edit";
	}

	@RequestMapping(value = "/{creatorName}/{repositoryName}/edit", method = RequestMethod.POST)
	public String edit(@PathVariable("repositoryName") String repositoryName,
			@PathVariable("creatorName") String creatorName,@RequestParam Map<String, String> params,Model model) {
		Weaver currentWeaver = weaverService.getCurrentWeaver();
		Repository repository = repositoryService.get(creatorName+"/"+repositoryName);
		List<String> tagList = tagService.stringToTagList(params.get("tags"));
		int authLevel = 0;

		if(params.get("authLevel") != null)
			authLevel = Integer.parseInt(params.get("authLevel"));

		if(!repository.getCreator().equals(currentWeaver) || params.get("description") == null || !tagService.isPublicTags(tagList)){
			model.addAttribute("say", "잘못 입력하셨습니다!!!");
			model.addAttribute("url", "/repository/"+repository.getName()+"/edit");
			return "/alert";
		}

		repository.setDescription(params.get("description"));
		repository.setAuthLevel(authLevel);

		repository.setTags(tagList);

		repositoryService.update(repository);
		return "redirect:/repository/"+repository.getName()+"/edit";
	}

	@RequestMapping("/{creatorName}/{repositoryName}/log/log:{log}/page:{page}")
	public String log(@PathVariable("repositoryName") String repositoryName,
			@PathVariable("creatorName") String creatorName,
			@PathVariable("log") String log,
			@PathVariable("page") String page,
			HttpServletRequest request,Model model) {
		Repository repository = repositoryService.get(creatorName+"/"+repositoryName);
		String uri = request.getRequestURI();
		log = uri.substring(uri.indexOf("/log:")+5);
		log = log.substring(0, log.indexOf("/"));
		int pageNum = WebUtil.getPageNumber(page);
		int size = WebUtil.getPageSize(page,0);
		List<String> gitBranchList = gitService.getBranchList(creatorName, repositoryName);

		gitBranchList.remove(log);
		model.addAttribute("gitBranchList", gitBranchList);
		model.addAttribute("selectBranch",log);
		model.addAttribute("repository", repository);
		model.addAttribute("pageIndex",page);
		model.addAttribute("gitCommitListCount", 
				gitService.getCommitListCount(creatorName, repositoryName,log));
		model.addAttribute("gitCommitList", 
				gitService.getGitLogList(creatorName, repositoryName,log,pageNum,size));
		return "/repository/log";
	}

	@RequestMapping("/{creatorName}/{repositoryName}/log-viewer/log:{log}")
	public String logViewer(@PathVariable("repositoryName") String repositoryName,
			@PathVariable("creatorName") String creatorName,
			@PathVariable("log") String log,
			HttpServletRequest request,Model model) {
		Repository repository = repositoryService.get(creatorName+"/"+repositoryName);
		String uri = request.getRequestURI();
		log = uri.substring(uri.indexOf("/log:")+5);
		VCLog gitLog = gitService.getGitLog(creatorName, repositoryName, log);
		if(gitLog == null)
			return "redirect:/repository/"+ creatorName+"/"+repositoryName+"/log";
		model.addAttribute("repository", repository);
		model.addAttribute("gitLog",gitLog);
		return "/repository/logViewer";
	}


	@RequestMapping("/{creatorName}/{repositoryName}/weaver")
	public String manageWeaver(@PathVariable("repositoryName") String repositoryName,
			@PathVariable("creatorName") String creatorName,Model model) {
		Repository repository = repositoryService.get(creatorName+"/"+repositoryName);
		model.addAttribute("repository", repository);
		return "/repository/manageWeaver";
	}

	@RequestMapping(value = "/{creatorName}/{repositoryName}/weaver/{weaverName}/delete") // 회원 삭제용
	public String deleteWeaver(@PathVariable("repositoryName") String repositoryName,
			@PathVariable("creatorName") String creatorName,
			@PathVariable("weaverName") String weaverName,Model model) {
		Repository repository = repositoryService.get(creatorName+"/"+repositoryName);
		Weaver currentWeaver = weaverService.getCurrentWeaver();
		Weaver deleteWeaver = weaverService.get(weaverName);

		if(repositoryService.deleteWeaver(repository, currentWeaver,deleteWeaver)){
			Post post;
			if(currentWeaver.equals(repository.getCreator())){//관리자가 탈퇴시킬시에 메세지
				post = new Post(currentWeaver, 
						deleteWeaver.getId()+"님을 탈퇴 처리하였습니다.", "", 
						tagService.stringToTagList("@"+repository.getName()+",탈퇴"));//저장소에 메세지 보냄
				postService.add(post,null);
				post = new Post(currentWeaver, 
						deleteWeaver.getId()+"님이 저장소명:"+repository.getName()+"에서 탈퇴 당하셨습니다.", "", 
						tagService.stringToTagList("$"+deleteWeaver.getId()));//저장소에 메세지 보냄
				postService.add(post, null);
				model.addAttribute("url", "/repository/"+creatorName+"/"+repositoryName+"/weaver/");

			}else{//사용자가 탈퇴할시에 메세지
				post = new Post(currentWeaver, 
						deleteWeaver.getId()+"님이 탈퇴하셨습니다.", "", 
						tagService.stringToTagList("@"+repository.getName()+",탈퇴"));//저장소에 메세지 보냄
				postService.add(post, null);
				model.addAttribute("url", "/");
			}

			model.addAttribute("say", "탈퇴 처리가 성공하였습니다!");
			return "/alert";

		}
		model.addAttribute("url", "/");
		model.addAttribute("say", "탈퇴 처리가 실패하였습니다!");
		return "/alert";

	}

	@RequestMapping("/{creatorName}/{repositoryName}/weaver/{weaverName}/add-weaver")
	public String addWeaver(	@PathVariable("repositoryName") String repositoryName,
			@PathVariable("creatorName") String creatorName,
			@PathVariable("weaverName") String weaverName,Model model) {
		Repository repository = repositoryService.get(creatorName+"/"+repositoryName);
		Weaver waitingWeaver = weaverService.get(weaverName);
		Weaver proposer = weaverService.getCurrentWeaver();


		if(weaverService.get(weaverName) == null){
			model.addAttribute("url", "/repository/"+creatorName+"/"+repositoryName+"/weaver/");
			model.addAttribute("say", "회원이 존재하지 않습니다!");
			return "/alert";		
		}


		if(!waitingWeaver.equals(proposer) && invateService.isCreateWaitJoin(repository, waitingWeaver, proposer)){
			Weaver repositoryCreator = weaverService.get(repository.getCreatorName());
			String title ="저장소명:"+creatorName+"/"+repositoryName+"에 가입 초대를 </a><a href='/repository/"+creatorName+"/"+repositoryName+"/weaver/"+waitingWeaver.getId()+"/join-ok'>승락하시겠습니까?</a> "
					+ "아니면 <a href='/repository/"+creatorName+"/"+repositoryName+"/weaver/"+waitingWeaver.getId()+"/join-cancel'>거절하시겠습니까?</a><a>";

			Post post = new Post(repositoryCreator,
					title, 
					"", 
					tagService.stringToTagList("$"+waitingWeaver.getId()),true);
			invateService.createWaitJoin(
					repository.getName(), 
					proposer.getId(), 
					waitingWeaver.getId(), 
					postService.add(post,null));
			model.addAttribute("url", "/repository/"+creatorName+"/"+repositoryName+"/weaver/");
			model.addAttribute("say", "회원에게 초대장을 보냈습니다!");
			return "/alert";	
		}
		model.addAttribute("url", "/repository/"+creatorName+"/"+repositoryName+"/weaver/");
		model.addAttribute("say", "회원을 추가하지 못했습니다!");
		return "/alert";		

	}


	@RequestMapping("/{creatorName}/{repositoryName}/join") //본인이 직접 신청
	public String join(	@PathVariable("repositoryName") String repositoryName,
			@PathVariable("creatorName") String creatorName,Model model) {
		Repository repository = repositoryService.get(creatorName+"/"+repositoryName);
		Weaver waitingWeaver = weaverService.getCurrentWeaver();

		if(invateService.isCreateWaitJoin(repository, waitingWeaver, waitingWeaver)){
			String title = waitingWeaver.getId()+"님이 저장소명:"+creatorName+"/"+repositoryName+"에 가입 신청을 </a><a href='/repository/"+creatorName+"/"+repositoryName+"/weaver/"+waitingWeaver.getId()+"/join-ok'>승락하시겠습니까?</a> "
					+ "아니면 <a href='/repository/"+creatorName+"/"+repositoryName+"/weaver/"+waitingWeaver.getId()+"/join-cancel'>거절하시겠습니까?</a><a>";
			Post post = new Post(waitingWeaver,
					title, 
					"", 
					tagService.stringToTagList("$"+repository.getCreatorName()),true);
			invateService.createWaitJoin(
					repository.getName(), 
					waitingWeaver.getId(), 
					waitingWeaver.getId(), 
					postService.add(post,null));
			model.addAttribute("url", "/");
			model.addAttribute("say", "가입 부탁 메시지를 보냈습니다!");
			return "/alert";	
		}
		model.addAttribute("url", "/");
		model.addAttribute("say", "가입 부탁 메시지를 보내지 못했습니다!");
		return "/alert";		

	}


	@RequestMapping("/{creatorName}/{repositoryName}/weaver/{weaver}/join-ok") // 저장소 가입 승인
	public String joinOK(@PathVariable("repositoryName") String repositoryName,
			@PathVariable("creatorName") String creatorName,@PathVariable("weaver") String weaver,Model model) {
		Repository repository = repositoryService.get(creatorName+"/"+repositoryName);
		Weaver currentWeaver = weaverService.getCurrentWeaver();
		Weaver waitingWeaver = weaverService.get(weaver);
		Invite invate = invateService.get(creatorName+"/"+repositoryName, weaver);

		if(invateService.isOkJoin(invate, repository.getCreatorName(), currentWeaver) //요청자가 쪽지를 보내고 관리자가 승인을 하는 경우
				&& repository.getCreator().equals(currentWeaver)
				&& invateService.deleteWaitJoin(invate, repository, waitingWeaver)){
			postService.delete( waitingWeaver,postService.get(invate.getPostID()));	
			repositoryService.addWeaver(repository, waitingWeaver);
			Post post = new Post(waitingWeaver, 
					"관리자 "+repository.getCreatorName()+"님의 승인으로 저장소명:"+
							creatorName+"/"+repositoryName+
							"에 가입이 승인되었습니다!", 
							"", 
							tagService.stringToTagList("@"+repository.getName()+",가입"),true); //@저장소명,가입 태그를 걸어줌

			postService.add(post,null);

			return "redirect:/repository/"+creatorName+"/"+repositoryName+"/weaver";

		}else if(repository != null //관리자가 쪽지를 보내고 가입자가 승인을 하는 경우
				&& invateService.isOkJoin(invate, repository.getCreatorName(), currentWeaver)
				&& !repository.getCreator().equals(currentWeaver)
				&& invateService.deleteWaitJoin(invate, repository, currentWeaver)){
			postService.delete(repository.getCreator(),postService.get(invate.getPostID()));	
			repositoryService.addWeaver(repository, waitingWeaver);

			Post post = new Post(currentWeaver, //가입자가 관리자에게 보내는 메세지
					currentWeaver.getId()+"님이 저장소명:"+creatorName+"/"+repositoryName+
					"를 가입 초대를 수락하셨습니다!", 
					"", 
					tagService.stringToTagList("@"+repository.getName()+",가입"),true); //@저장소명,가입 태그를 걸어줌

			postService.add(post,null);

			return "redirect:/repository/"+creatorName+"/"+repositoryName+"/weaver";
		}

		model.addAttribute("url", "/");
		model.addAttribute("say", "권한이 없습니다!");
		return "/alert";		
	}

	@RequestMapping("/{creatorName}/{repositoryName}/weaver/{weaver}/join-cancel") // 저장소 가입 승인 취소
	public String joinCancel(@PathVariable("repositoryName") String repositoryName,
			@PathVariable("creatorName") String creatorName,@PathVariable("weaver") String weaver,Model model) {
		Repository repository = repositoryService.get(creatorName+"/"+repositoryName);
		Weaver currentWeaver = weaverService.getCurrentWeaver();
		Invite invate = invateService.get(repository.getName(), weaver);
		Weaver waitingWeaver = weaverService.get(weaver);

		if(repository != null //요청자가 쪽지를 보내고 관리자가 승인을 하는 경우
				&& invateService.isOkJoin(invate, repository.getCreatorName(), currentWeaver)
				&& repository.getCreator().equals(currentWeaver)
				&& invateService.deleteWaitJoin(invate, repository, currentWeaver)){
			postService.delete(waitingWeaver,postService.get(invate.getPostID()));	
			Post post = new Post(currentWeaver,  //관리자가 가입자에게 보내는 메세지
					"관리자 "+repository.getCreatorName()+"님의 저장소명:"+
					creatorName+"/"+repositoryName+
					"에 가입이 거절되었습니다.", 
					"", 
					tagService.stringToTagList("$"+waitingWeaver.getId()),true);

			postService.add(post,null);

			return "redirect:/repository/"+creatorName+"/"+repositoryName+"/weaver";

		}else if(repository != null //관리자가 쪽지를 보내고 가입자가 거절 하는 경우
				&& invateService.isOkJoin(invate, repository.getCreatorName(), currentWeaver)
				&& !repository.getCreatorName().equals(currentWeaver.getId())
				&& invateService.deleteWaitJoin(invate, repository, currentWeaver)){
			postService.delete(repository.getCreator(),postService.get(invate.getPostID()));	
			Post post = new Post(currentWeaver, //가입자가 관리자에게 보내는 메세지
					currentWeaver.getId()+"님이 저장소명:"+creatorName+"/"+repositoryName+
					"를 가입 초대를 거절하셨습니다.", 
					"", 
					tagService.stringToTagList("$"+repository.getCreatorName()),true);

			postService.add(post,null);

			return "redirect:/";
		}

		model.addAttribute("url", "/");
		model.addAttribute("say", "권한이 없습니다!");
		return "/alert";		
	}

	@RequestMapping(value="/{creatorName}/{repositoryName}/{branchName}/upload" , method=RequestMethod.POST)
	public String upload(@PathVariable("repositoryName") String repositoryName,
			@PathVariable("creatorName") String creatorName,
			@PathVariable("branchName") String branchName,
			@RequestParam("message") String message,
			@RequestParam("path") String path,
			@RequestParam("zip") MultipartFile zip,Model model) {
		Repository repository = repositoryService.get(creatorName+"/"+repositoryName);
		Weaver currentWeaver = weaverService.getCurrentWeaver();
		if(!repositoryService.uploadFile(repository, currentWeaver,branchName, message,path, zip)){
			model.addAttribute("say", "업로드 실패! 저장소에 가입되어 있는지 혹은 압축파일을 다시 확인해주세요!");
			model.addAttribute("url", "/repository/"+creatorName+"/"+repositoryName);
			return "/alert";
		}
		return "redirect:/repository/"+creatorName+"/"+repositoryName;
	}
	
	@RequestMapping(value="/{creatorName}/{repositoryName}/reset")
	public String reset(@PathVariable("repositoryName") String repositoryName,
			@PathVariable("creatorName") String creatorName,Model model) {
		Repository repository = repositoryService.get(creatorName+"/"+repositoryName);
		Weaver currentWeaver = weaverService.getCurrentWeaver();
		if(!repositoryService.reSetRepository(repository, currentWeaver)){
			model.addAttribute("say", "초기화 실패! 관리자만이 초기화 가능합니다!");
			model.addAttribute("url", "/repository/"+creatorName+"/"+repositoryName);
			return "/alert";
		}
		return "redirect:/repository/"+creatorName+"/"+repositoryName;
	}

	@RequestMapping("/{creatorName}/{repositoryName}/push")
	public String push(@PathVariable("repositoryName") String repositoryName,
			@PathVariable("creatorName") String creatorName) {
		Repository repository = repositoryService.get(creatorName+"/"+repositoryName);
		repositoryService.push(repository, weaverService.getCurrentWeaver(),weaverService.getUserIP());

		return "redirect:/repository/";
	}

	// 저장소 정보 불러오기
	@RequestMapping("/{creatorName}/{repositoryName}/info")
	public String info(@PathVariable("repositoryName") String repositoryName,
			@PathVariable("creatorName") String creatorName, Model model){
		Repository repository = repositoryService.get(creatorName+"/"+repositoryName);
		model.addAttribute("repository", repository);
		model.addAttribute("gitInfo", gitService.getGitInfo(creatorName, repositoryName, "HEAD"));
		return "/repository/info";
	}

	// 저장소 스트림 시각화
	@RequestMapping("/{creatorName}/{repositoryName}/info:stream")
	public String stream(@PathVariable("repositoryName") String repositoryName,
			@PathVariable("creatorName") String creatorName, Model model){
		Repository repository = repositoryService.get(creatorName+"/"+repositoryName);

		model.addAttribute("repository", repository);
		model.addAttribute("gps", gitService.loadStatistics(creatorName, repositoryName));
		return "/repository/stream";
	}

	// 저장소 빈도수 시각화
	@RequestMapping("/{creatorName}/{repositoryName}/info:frequency")
	public String punchcard(@PathVariable("repositoryName") String repositoryName,
			@PathVariable("creatorName") String creatorName, Model model){
		Repository repository = repositoryService.get(creatorName+"/"+repositoryName);

		model.addAttribute("repository", repository);
		model.addAttribute("dayAndHour", gitService.loadDayAndHour(creatorName, repositoryName));
		return "/repository/frequency";
	}

}
