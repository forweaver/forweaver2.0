package com.forweaver.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.forweaver.domain.Weaver;
import com.forweaver.service.CodeService;
import com.forweaver.service.PostService;
import com.forweaver.service.RepositoryService;
import com.forweaver.service.TagService;
import com.forweaver.service.WeaverService;
import com.forweaver.util.WebUtil;

@Controller
public class WeaverController {

	private static final Logger logger =
			LoggerFactory.getLogger(WeaverController.class);

	@Autowired
	private WeaverService weaverService;
	@Autowired
	private PostService postService;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private CodeService codeService;
	@Autowired
	private TagService tagService;

	@RequestMapping(value = "/login",method = RequestMethod.GET)
	public String login(@RequestParam(value = "error", required = false) String error,
	        @RequestParam(value = "logout", required = false) String logout,Model model) {

		if(error != null)
			model.addAttribute("script", "alert('로그인 실패!!! 다시 로그인해주세요!')");

		return "/weaver/login";
	}



	@RequestMapping("/join")
	public String join() {
		return "/weaver/join";
	}

	@RequestMapping(value = "/join", method = RequestMethod.POST)
	public String join(@RequestParam("id") String id,
			@RequestParam("email") String email,
			@RequestParam("password") String password,
			@RequestParam("say") String say,
			@RequestParam("image") MultipartFile image,
			HttpServletRequest request,Model model) {

		if(!Pattern.matches("^[a-z]{1}[a-z0-9_]{4,14}$", id) || password.length()<4 ||
				say.length()>50 ||
				!Pattern.matches("[\\w\\~\\-\\.]+@[\\w\\~\\-]+(\\.[\\w\\~\\-]+)+",email)){
			model.addAttribute("say", "잘못 입력하셨습니다!");
			model.addAttribute("url", "/join");
			return "/alert";
		}

		if (weaverService.idCheck(id) || weaverService.idCheck(email)){
			model.addAttribute("say", "이미 존재하는 아이디 혹은 이메일입니다.");
			model.addAttribute("url", "/join");
			return "/alert";
		}
		Weaver weaver = new Weaver(id, password, email,say);
		weaverService.add(weaver,image);
		weaverService.autoLoginWeaver(weaver, request);
		return "redirect:/";
	}


	@RequestMapping("/weaver")
	public String weavers(HttpServletRequest request){
		return "redirect:"+request.getRequestURI() +"page:1";
	}

	@RequestMapping("/weaver/page:{page}")
	public String weavers(@PathVariable("page") String page,Model model) {
		int pageNum = WebUtil.getPageNumber(page);
		int size = WebUtil.getPageSize(page,0);

		long weaverCount = weaverService.countWeavers();
		List<Weaver> weavers = weaverService.getWeavers(pageNum, size);

		model.addAttribute("weavers", weavers);
		model.addAttribute("weaverCount", weaverCount);

		model.addAttribute("pageIndex", pageNum);
		model.addAttribute("number", size);
		model.addAttribute("pageUrl", "/weaver/page:");
		return "/weaver/weavers";
	}

	@RequestMapping("/weaver/tags:{tagNames}")
	public String weaversTags(HttpServletRequest request){
		return "redirect:"+request.getRequestURI() +"/page:1";
	}

	@RequestMapping("/weaver/tags:{tagNames}/page:{page}")
	public String weaversTags(Model model,@PathVariable("tagNames") String tagNames,
			@PathVariable("page") String page) {
		List<String> tagList = tagService.stringToTagList(tagNames);

		if(!tagService.isPublicTags(tagList))
			return "redirect:/weaver/page:1";

		int pageNum = WebUtil.getPageNumber(page);
		int size = WebUtil.getPageSize(page,0);

		List<Weaver> weavers= weaverService.getWeavers(tagList,pageNum,size);
		long weaverCount = weaverService.countWeavers(tagList);

		model.addAttribute("weavers", weavers);
		model.addAttribute("weaverCount", weaverCount);

		model.addAttribute("pageIndex", pageNum);
		model.addAttribute("number", size);
		model.addAttribute("pageUrl", "/weaver/tags:"+tagNames+"/page:");
		return "/weaver/weavers";
	}

	@RequestMapping({"/{id}","/{id}/code","/{id}/repository","/{id}/lecture"})
	public String home(@PathVariable("id") String id,HttpServletRequest request) {
		Weaver weaver = weaverService.get(id);
		if(weaver == null || weaver.isLeave())
			return "/errorUserNull";
		else
			return "redirect:" + request.getRequestURI() + "/sort:age-desc/page:1";

	}

	@RequestMapping("/{id}/sort:{sort}/page:{page}")
	public String communityPage(@PathVariable("id") String id,
			@PathVariable("page") String page,
			@PathVariable("sort") String sort, Model model) {
		int pageNum = WebUtil.getPageNumber(page);
		int size = WebUtil.getPageSize(page,0);
		Weaver weaver = weaverService.get(id);

		if (weaver == null || weaver.isLeave())
			return "redirect:/";

		Weaver currentWeaver = weaverService.getCurrentWeaver();

		model.addAttribute("weaver", weaver);

		model.addAttribute("posts", postService.getPosts(
				currentWeaver, weaver, sort, pageNum, size));
		model.addAttribute("postCount",
				postService.countPosts(currentWeaver, weaver, sort));

		model.addAttribute("pageIndex", pageNum);
		model.addAttribute("number", size);
		model.addAttribute("pageUrl", "/"+id+"/sort:" + sort + "/page:");
		return "/weaver/mypage/community";
	}

	@RequestMapping("/{id}/code/sort:{sort}/page:{page}")
	public String codePage(@PathVariable("id") String id,
			@PathVariable("sort") String sort,
			@PathVariable("page") String page, Model model) {
		Weaver weaver = weaverService.get(id);
		int pageNum = WebUtil.getPageNumber(page);
		int size = WebUtil.getPageSize(page,0);

		if (weaver == null || weaver.isLeave())
			return "redirect:/";

		model.addAttribute("weaver", weaver);
		model.addAttribute("codes", codeService.getMyCodes(weaver, null, null, sort, pageNum, size));
		model.addAttribute("codeCount", codeService.countMyCodes(weaver, null, null, sort));
		model.addAttribute("pageIndex", pageNum);
		model.addAttribute("number", size);
		model.addAttribute("pageUrl", "/"+id+"/code/sort:" + sort + "/page:");
		return "/weaver/mypage/code";
	}

	@RequestMapping("/{id}/repository/sort:{sort}/page:{page}")
	public String repositoryPage(@PathVariable("id") String id,
			@PathVariable("sort") String sort,
			@PathVariable("page") String page, Model model) {
		Weaver currentWeaver = weaverService.getCurrentWeaver();
		Weaver weaver = weaverService.get(id);
		int pageNum = WebUtil.getPageNumber(page);
		int size = WebUtil.getPageSize(page,0);

		if (weaver == null || weaver.isLeave())
			return "redirect:/";

		model.addAttribute("weaver", weaver);
		model.addAttribute("repositorys", repositoryService.getRepositories(currentWeaver,weaver,null,sort, pageNum, size));
		model.addAttribute("repositoryCount", repositoryService.countRepositories(weaver, null, sort));
		model.addAttribute("pageIndex", pageNum);
		model.addAttribute("number", size);
		model.addAttribute("pageUrl", "/"+id+"/repository/sort:" + sort + "/page:");
		return "/weaver/mypage/repository";
	}

	@RequestMapping({"/{id}/tags:{tagNames}",
		"/{id}/code/tags:{tagNames}",
		"/{id}/repository/tags:{tagNames}"
		,"/{id}/lecture/tags:{tagNames}"})
	public String tags(HttpServletRequest request) {
		return "redirect:" + request.getRequestURI() + "/sort:age-desc/page:1";
	}

	@RequestMapping("/{id}/tags:{tagNames}/sort:{sort}/page:{page}")
	public String tagsWithPage(@PathVariable("tagNames") String tagNames,
			@PathVariable("id") String id, @PathVariable("page") String page,
			@PathVariable("sort") String sort, Model model) {
		List<String> tagList = tagService.stringToTagList(tagNames);

		int pageNum = WebUtil.getPageNumber(page);
		int size = WebUtil.getPageSize(page,0);

		Weaver weaver = weaverService.get(id);

		if (weaver == null || weaver.isLeave())
			return "redirect:/";

		Weaver currentWeaver = weaverService.getCurrentWeaver();

		if (!tagService.validateTag(tagList, currentWeaver))
			return "redirect:/";

		model.addAttribute("weaver", weaver);

		model.addAttribute("posts", postService.getPosts(
				currentWeaver, tagList, weaver, sort, pageNum, size));
		model.addAttribute("postCount", postService
				.countPosts(currentWeaver, tagList, weaver,
						sort));
		model.addAttribute("tagNames", tagNames);
		model.addAttribute("pageIndex", pageNum);
		model.addAttribute("number", size);
		model.addAttribute("pageUrl", "/"+id+"/tags:" + tagNames + "/sort:"+ sort + "/page:");

		return "/weaver/mypage/community";
	}

	@RequestMapping("/{id}/code/tags:{tagNames}/sort:{sort}/page:{page}")
	public String codeTagsWithPage(@PathVariable("tagNames") String tagNames,
			@PathVariable("id") String id, @PathVariable("page") String page,
			@PathVariable("sort") String sort, Model model) {
		List<String> tags = tagService.stringToTagList(tagNames);

		int pageNum = WebUtil.getPageNumber(page);
		int size = WebUtil.getPageSize(page,0);

		Weaver weaver = weaverService.get(id);

		if (weaver == null || weaver.isLeave())
			return "redirect:/";

		Weaver currentWeaver = weaverService.getCurrentWeaver();

		if (!tagService.validateTag(tags, currentWeaver))
			return "redirect:/";

		model.addAttribute("weaver", weaver);
		model.addAttribute("codes", codeService
				.getMyCodes(weaver, tags, null, sort, pageNum, size));
		model.addAttribute("codeCount", codeService
				.countMyCodes(weaver, tags, null, sort));
		model.addAttribute("tagNames", tagNames);
		model.addAttribute("pageIndex", page);
		model.addAttribute("number", size);
		model.addAttribute("pageUrl", "/"+id+"/code/tags:" + tagNames+ "/sort:" + sort + "/page:");

		return "/weaver/mypage/code";
	}

	@RequestMapping("/{id}/repository/tags:{tagNames}/sort:{sort}/page:{page}")
	public String repositoryTagsWithPage(@PathVariable("tagNames") String tagNames,
			@PathVariable("id") String id, @PathVariable("page") String page,
			@PathVariable("sort") String sort, Model model) {
		List<String> tags = tagService.stringToTagList(tagNames);

		int pageNum = WebUtil.getPageNumber(page);
		int size = WebUtil.getPageSize(page,0);


		Weaver weaver = weaverService.get(id);

		if (weaver == null || weaver.isLeave())
			return "redirect:/";

		Weaver currentWeaver = weaverService.getCurrentWeaver();

		if (!tagService.validateTag(tags, currentWeaver))
			return "redirect:/";

		model.addAttribute("weaver", weaver);
		model.addAttribute("repositorys", repositoryService
				.getRepositories(currentWeaver, weaver, tags, sort, pageNum, size));
		model.addAttribute("repositoryCount", repositoryService
				.countRepositories(weaver, tags, sort));
		model.addAttribute("tagNames", tagNames);
		model.addAttribute("pageIndex", page);
		model.addAttribute("number", size);
		model.addAttribute("pageUrl", "/"+id+"/code/tags:" + tagNames+ "/sort:" + sort + "/page:");

		return "/weaver/mypage/repository";
	}

	@RequestMapping({"/{id}/tags:{tagNames}/search:{search}",
	"/{id}/code/tags:{tagNames}/search:{search}"})
	public String tagsWithSearch(HttpServletRequest request) {
		return "redirect:" + request.getRequestURI() + "/sort:age-desc/page:1";
	}

	@RequestMapping("/{id}/tags:{tagNames}/search:{search}/sort:{sort}/page:{page}")
	public String tagsWithSearch(@PathVariable("tagNames") String tagNames,
			@PathVariable("id") String id,
			@PathVariable("search") String search,
			@PathVariable("sort") String sort,
			@PathVariable("page") String page, Model model) {
		List<String> tagList = tagService.stringToTagList(tagNames);
		int pageNum = WebUtil.getPageNumber(page);
		int size = WebUtil.getPageSize(page,0);
		Weaver weaver = weaverService.get(id);

		if (weaver == null || weaver.isLeave())
			return "redirect:/";

		Weaver currentWeaver = weaverService.getCurrentWeaver();

		if (!tagService.validateTag(tagList, currentWeaver))
			return "redirect:/";

		model.addAttribute("weaver", weaver);

		model.addAttribute("posts", postService
				.getPosts(currentWeaver,
						tagList, weaver, search, sort, pageNum, size));
		model.addAttribute("postCount", postService
				.countPosts(currentWeaver,
						tagList, weaver, search, sort));
		model.addAttribute("tagNames", tagNames);
		model.addAttribute("search", search);
		model.addAttribute("pageIndex", page);
		model.addAttribute("pageUrl", "/"+id+"/tags:" + tagNames + "/search:" + search
				+ "/sort:" + sort + "/page:");

		return "/weaver/mypage/community";
	}

	@RequestMapping("/{id}/code/tags:{tagNames}/search:{search}/sort:{sort}/page:{page}")
	public String codeTagsWithSearch(@PathVariable("tagNames") String tagNames,
			@PathVariable("id") String id,
			@PathVariable("search") String search,
			@PathVariable("sort") String sort,
			@PathVariable("page") String page, Model model) {
		List<String> tags = tagService.stringToTagList(tagNames);
		int pageNum = WebUtil.getPageNumber(page);
		int size = WebUtil.getPageSize(page,0);
		Weaver weaver = weaverService.get(id);

		if (weaver == null || weaver.isLeave())
			return "redirect:/";

		Weaver currentWeaver = weaverService.getCurrentWeaver();

		if (!tagService.validateTag(tags, currentWeaver))
			return "redirect:/";

		model.addAttribute("weaver", weaver);
		model.addAttribute("codes", codeService
				.getMyCodes(weaver, tags, search, sort, pageNum, size));
		model.addAttribute("codeCount", codeService
				.countMyCodes(weaver, tags, search, sort));
		model.addAttribute("tagNames", tagNames);
		model.addAttribute("search", search);
		model.addAttribute("pageIndex", page);
		model.addAttribute("pageUrl", "/"+id+"/code/tags:" + tagNames + "/search:" + search
				+ "/sort:" + sort + "/page:");

		return "/weaver/mypage/code";
	}


	@RequestMapping(value = "/edit")
	public String editWeaver(Model model) {
		Weaver weaver = weaverService.getCurrentWeaver();
		if (weaver == null) // 로그인하지 않은 경우
			return "redirect:/";
		model.addAttribute("weaver", weaver);
		return "/weaver/edit";
	}

	@RequestMapping(value = "/edit", method = RequestMethod.POST)
	public String editWeaver(
			@RequestParam("password") String password,
			@RequestParam("newpassword") String newpassword,
			@RequestParam("say") String say,
			@RequestParam("image") MultipartFile image,Model model) {
		Weaver weaver = weaverService.getCurrentWeaver();
		weaverService.update(weaver, password, newpassword, say, image);

		model.addAttribute("say", "정보를 수정하였습니다!");
		model.addAttribute("url", "/edit");
		return "/alert";

	}

	@RequestMapping(value = "/{id}/img")
	public void img(@PathVariable("id") String id, HttpServletResponse res)
			throws IOException {
		Weaver weaver = weaverService.get(id);
		byte[] imgData = null;

		if(weaver.getData() != null) {
			res.setHeader("Content-Disposition", "attachment; filename = "+weaver.getData().getName());
			res.setContentType(weaver.getData().getType());
			imgData = weaver.getData().getContent();
		}

		if (imgData == null) {
			imgData = WebUtil.downloadFile("http://www.gravatar.com/avatar/" + WebUtil.convertMD5(weaver.getEmail()) + ".jpg");
			res.setHeader("Content-Disposition", "attachment; filename = icon.jpg");
			res.setContentType("image/jpeg");
		}
		if(imgData == null) {
			res.sendRedirect("/resources/forweaver/img/null.png");
			return;
		}

		OutputStream o = res.getOutputStream();
		o.write(imgData);
		o.flush();
		o.close();
	}

	@RequestMapping(value = "/check", method = RequestMethod.POST)
	@ResponseBody
	public boolean nickNameCheck(HttpServletRequest req) {
		return weaverService.idCheck(req.getParameter("id"));
	}

/*	@RequestMapping(value = "/repassword/{email}/{key}")
	public String repassword(@PathVariable("email") String email,@PathVariable("key") String key) {

		if(weaverService.changePassword(email, key))
			return "/weaver/repassword";
		else
			return "/error500";
	}*/
/*
	@RequestMapping(value = "/repassword", method = RequestMethod.POST)
	@ResponseBody
	public boolean repasswordCheck(@RequestParam("email") String email) {
		return weaverService.sendRepassword(email);
	}*/


	@RequestMapping(value = "/del")
	public String delete() {
		Weaver weaver = weaverService.getCurrentWeaver();

		if (weaver == null) // 로그인하지 않은 경우
			return "redirect:/";
		return "/weaver/del";
	}
/*
	@RequestMapping(value = "/del", method = RequestMethod.POST)
	public String delete(Model model,@RequestParam("password") String password) {
		Weaver weaver = weaverService.getCurrentWeaver();

		if(!weaverService.validPassword(weaver, password)){
			model.addAttribute("say", "비밀번호가 일치하지 않습니다!");
			model.addAttribute("url", "/del");
			return "/alert";
		}
		model.addAttribute("say", "성공적으로 탈퇴 처리됐습니다!");
		model.addAttribute("url", "/j_spring_security_logout");
		return "/alert";
	}*/




}
