package com.forweaver.controller;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.forweaver.domain.Data;
import com.forweaver.domain.Post;
import com.forweaver.domain.Project;
import com.forweaver.domain.RePost;
import com.forweaver.domain.Reply;
import com.forweaver.domain.Weaver;
import com.forweaver.service.DataService;
import com.forweaver.service.PostService;
import com.forweaver.service.ProjectService;
import com.forweaver.service.RePostService;
import com.forweaver.service.TagService;
import com.forweaver.service.WeaverService;
import com.forweaver.util.WebUtil;

@Controller
@RequestMapping("/community")

public class PostController {
	@Autowired 
	private PostService postService;
	@Autowired 
	private ProjectService projectService;
	@Autowired 
	private RePostService rePostService;
	@Autowired 
	private TagService tagService;
	@Autowired 
	private WeaverService weaverService;
	@Autowired 
	private DataService dataService;

	@RequestMapping("/")
	public String front(){
		return "redirect:/community/sort:age-desc/page:1";
	}
	/*
	@RequestMapping("/sort:{sort}/rss")
	@ResponseBody
	public String rss(@PathVariable("sort") String sort){ 
		String rss = "<?xml version='1.0' encoding='UTF-8'?><rss version='2.0'><channel>";

		rss +="<title>commutnity:"+sort+"</title>";
		rss +="<link>http://forweaver.com/community/sort:"+sort+"</link>";
		rss +="<description>commutnity:"+sort+"</description>";

		Weaver currentWeaver = weaverService.getCurrentWeaver();
		for(Post post:postService.getPosts(currentWeaver, sort, 1, 15)){ // 게시물을 15개 가져옴
			rss +="<item>";
			rss +="<author>"+post.getWriterName()+" ("+post.getWriterEmail()+")</author>";
			rss +="<title>"+post.getTitle()+"</title>";
			rss +="<link>http://forweaver.com/community/"+post.getPostID()+"</link>";
			rss +="<guid>http://forweaver.com/community/"+post.getPostID()+"</guid>";
			rss +="<description>"+post.getContent()+"</description>";
			rss +="<pubDate>"+post.getCreated()+"</pubDate>";
			rss +="<image>http://forweaver.com/"+post.getImgSrc()+"</image>";
			rss +="</item>";
		}
		return rss+"</channel></rss>";
	}
	 */

	@RequestMapping("/sort:{sort}/page:{page}")
	public String page(@PathVariable("page") String page,
			@PathVariable("sort") String sort,Model model){
		int pageNum = WebUtil.getPageNumber(page);
		int size = WebUtil.getPageSize(page,0);

		Weaver currentWeaver = weaverService.getCurrentWeaver();

		model.addAttribute("posts", 
				postService.getPosts(currentWeaver, sort, pageNum, size));
		model.addAttribute("postCount", 
				postService.countPosts(currentWeaver, sort));

		model.addAttribute("pageIndex", pageNum);
		model.addAttribute("number", size);
		model.addAttribute("pageUrl", "/community/sort:"+sort+"/page:");
		return "/post/front";
	}

	@RequestMapping("/tags:{tagNames}")
	public String tags(HttpServletRequest request){
		return "redirect:"+request.getRequestURI() +"/sort:age-desc/page:1";
	}

	@RequestMapping("/tags:{tagNames}/sort:{sort}/page:{page}")
	public String tagsWithPage(@PathVariable("tagNames") String tagNames,
			@PathVariable("page") String page,
			@PathVariable("sort") String sort,Model model){
		List<String> tagList = tagService.stringToTagList(tagNames);

		int pageNum = WebUtil.getPageNumber(page);
		int size = WebUtil.getPageSize(page,0);

		Weaver currentWeaver = weaverService.getCurrentWeaver();

		model.addAttribute("posts", 
				postService.getPosts(currentWeaver,tagList, sort, pageNum, size));
		model.addAttribute("postCount", 
				postService.countPosts(currentWeaver,tagList, sort));

		model.addAttribute("tagNames", tagNames);
		model.addAttribute("pageIndex", pageNum);
		model.addAttribute("number", size);
		model.addAttribute("pageUrl", "/community/tags:"+tagNames+"/sort:"+sort+"/page:");

		return "/post/front";
	}

	@RequestMapping("/tags:{tagNames}/search:{search}")
	public String tagsWithSearch(HttpServletRequest request){
		return "redirect:"+ request.getRequestURI() +"/sort:age-desc/page:1";
	}

	@RequestMapping("/tags:{tagNames}/search:{search}/sort:{sort}/page:{page}")
	public String tagsWithSearch(@PathVariable("tagNames") String tagNames,
			@PathVariable("search") String search,
			@PathVariable("sort") String sort,
			@PathVariable("page") String page,Model model){
		List<String> tagList = tagService.stringToTagList(tagNames);
		int pageNum = WebUtil.getPageNumber(page);
		int size = WebUtil.getPageSize(page,0);

		Weaver currentWeaver = weaverService.getCurrentWeaver();

		model.addAttribute("posts", 
				postService.getPosts(currentWeaver,tagList,search, sort, pageNum, size));
		model.addAttribute("postCount", 
				postService.countPosts(currentWeaver,tagList,search, sort));

		model.addAttribute("number", size);
		model.addAttribute("tagNames", tagNames);
		model.addAttribute("search", search);
		model.addAttribute("pageIndex", page);
		model.addAttribute("pageUrl", "/tags:"+tagNames+"/search:"+search+"/sort:"+sort+"/page:");

		return "/post/front";
	}

	@RequestMapping(value = "/add", method = RequestMethod.POST)
	public String add(final HttpServletRequest request,Model model) throws UnsupportedEncodingException {
		final MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
		final Map<String, MultipartFile> files = multiRequest.getFileMap();
		ArrayList<Data> datas = new ArrayList<Data>();

		String tags = request.getParameter("tags");
		String title = request.getParameter("title");
		String content = request.getParameter("content");

		if(tags == null || title == null || title.length() < 5 || title.length() > 200
				|| (content.length() >0 && content.length() < 5)){ // 검증함
			model.addAttribute("say", "잘못 입력하셨습니다!!!");
			model.addAttribute("url", "/community/");
			return "/alert";
		}
		List<String> tagList = tagService.stringToTagList(tags);
		Weaver weaver = weaverService.getCurrentWeaver();

		tagList = tagService.removeMyMassageTag(tagList,weaver);//실수로 자신의 메세지 태그를 붙이면 지움

		if(!tagService.validateTag(tagList,weaver)){ // 태그에 권한이 없을때
			model.addAttribute("say", "태그에 권한이 없습니다!!!");
			model.addAttribute("url", "/community/");
			return "/alert";
		}

		for (MultipartFile file : files.values())
			if(!file.isEmpty()){
				String fileID= dataService.getObjectID(file.getOriginalFilename(), weaver);
				if(!fileID.equals(""))
					datas.add(new Data(fileID,file,weaver));
			}

		Post post = new Post(weaver,title, 
				content, 
				tagList);

		postService.add(post,datas);

		if(postService.isMassageTags(tagList))
			return "redirect:"+"/community/tags:"+"$"+weaver.getId();

		return "redirect:"+"/community/";
	}


	@RequestMapping("/{postID}")
	public String view(@PathVariable("postID") int postID){
		return "redirect:/community/"+postID+"/sort:age-asc";
	}

	@RequestMapping("/{postID}/sort:{sort}")
	public String view(Model model, @PathVariable("postID") int postID,
			@PathVariable("sort") String sort) {
		Post post = postService.get(postID);
		Weaver weaver = weaverService.getCurrentWeaver();
				
		if(!post.getWriter().equals(weaver) && !tagService.validateTag(post.getTags(), weaver)){
			if(post.getKind() == 2){
				String projectName = tagService.getPrivateTag(post.getTags());
				projectName = projectName.substring(1);
				Project project = projectService.get(projectName);
				
				if(project == null || !project.isPublic())
					return "redirect:/community/";
			}else
				return "redirect:/community/";
		}
			
		model.addAttribute("post", post);
		model.addAttribute("rePosts", rePostService.gets(postID,post.getKind(),sort));

		return "/post/viewPost";
	}

	@RequestMapping(value = "/{postID}/add-repost", method = RequestMethod.POST)
	public String addRepost(@PathVariable("postID") int postID,HttpServletRequest request,Model model) throws UnsupportedEncodingException {

		final MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;

		final Map<String, MultipartFile> files = multiRequest.getFileMap();	

		String content = request.getParameter("content");
		Post post = postService.get(postID);
		Weaver weaver = weaverService.getCurrentWeaver();


		if(!post.getWriter().equals(weaver) && !tagService.validateTag(post.getTags(),weaver) || 
				weaver == null || post == null || content.equals("")) {
			model.addAttribute("say", "잘못 입력하셨습니다!!!");
			model.addAttribute("url", "/community/"+postID);
			return "/alert";
		}

		ArrayList<Data> datas = new ArrayList<Data>();
		for (MultipartFile file : files.values()) 
			if(!file.isEmpty()){
				String fileID= dataService.getObjectID(file.getOriginalFilename(), weaver);
				if(!fileID.equals(""))
					datas.add(new Data(fileID,file,weaver));
			}

		RePost rePost = new RePost(post,weaver,content);
		post.setRecentRePostDate(rePost.getCreated());
		post.addRePostCount();	
		postService.update(post,null, null);
		rePostService.add(rePost,datas);


		return "redirect:/community/"+postID;
	}

	@RequestMapping(value = "/{postID}/{rePostID}/add-reply", method = RequestMethod.POST)
	public String addReply(@PathVariable("postID") int postID,
			@PathVariable("rePostID") int rePostID,
			HttpServletRequest request,Model model) throws UnsupportedEncodingException {
		String content = request.getParameter("content");
		RePost rePost = rePostService.get(rePostID);
		Post post = postService.get(postID);
		Weaver weaver = weaverService.getCurrentWeaver();

		if(!post.getWriter().equals(weaver) && !tagService.validateTag(post.getTags(),weaver)) {// 권한 검사.
			model.addAttribute("say", "태그에 권한이 없습니다!!!");
			model.addAttribute("url", "/community/"+rePost.getOriginalPost().getPostID());
			return "/alert";
		}
		
		if(post ==  null || rePost == null || content.length() < 5 || content.length() > 200) {// 입력을 제대로 했는지 검사
			model.addAttribute("say", "잘못 입력하셨습니다!!!");
			model.addAttribute("url", "/community/"+rePost.getOriginalPost().getPostID());
			return "/alert";
		}
		rePostService.addReply(rePost,new Reply(weaver, content));	

		return "redirect:/community/"+rePost.getOriginalPost().getPostID();
	}

	@RequestMapping(value="/{postID}/{rePostID}/{number}/delete")
	public String deleteReply(@PathVariable("postID") int postID, 
			@PathVariable("rePostID") int rePostID,
			@PathVariable("number") int number,
			HttpServletRequest request,Model model) {		

		Post post = postService.get(postID);
		RePost rePost = rePostService.get(rePostID);
		Weaver weaver = weaverService.getCurrentWeaver();

		if(!post.getWriter().equals(weaver) && !tagService.validateTag(post.getTags(),weaver)) {// 권한 검사.
			model.addAttribute("say", "태그에 권한이 없습니다!!!");
			model.addAttribute("url", "/community/"+rePost.getOriginalPost().getPostID());
			return "/alert";
		}

		rePostService.deleteReply(rePost, weaver, number);

		return "redirect:/community/"+rePost.getOriginalPost().getPostID();
	}


	@RequestMapping("/{postID}/push")
	public String push(Model model, @PathVariable("postID") int postID) {


		Post post = postService.get(postID);
		
		postService.push(post,weaverService.getCurrentWeaver(),weaverService.getUserIP());

		return "redirect:/community/sort:age-desc/page:1";
	}

	@RequestMapping("/{postID}/{repostID}/push")
	public String rePostPush(Model model, @PathVariable("postID") int postID, @PathVariable("repostID") int repostID) {
		RePost rePost = rePostService.get(repostID);
		rePostService.push(rePost,weaverService.getCurrentWeaver(),weaverService.getUserIP());

		return "redirect:/community/"+postID+"/";
	}

	@RequestMapping("/{postID}/update")
	public String update(Model model, @PathVariable("postID") int postID) {		
		Post post = postService.get(postID);
		Weaver weaver = weaverService.getCurrentWeaver();
		
		if(post == null || weaver == null || !post.getWriter().equals(weaver)){
			model.addAttribute("say", "권한이 없습니다!!!");
			model.addAttribute("url", "/community/"+postID+"/");
			return "/alert";
		}
		
		model.addAttribute("post", post);

		return "/post/updatePost";
	}

	@RequestMapping(value="/{postID}/update", method = RequestMethod.POST)
	public String update(@PathVariable("postID") int postID,HttpServletRequest request,Model model) throws UnsupportedEncodingException {		

		final MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
		final Map<String, MultipartFile> files = multiRequest.getFileMap();
		ArrayList<Data> datas = new ArrayList<Data>();
		
		Post post = postService.get(postID);
		String tags = request.getParameter("tags");
		String remove = request.getParameter("remove");
		String title = request.getParameter("title");
		String content = request.getParameter("content");
		Weaver weaver = weaverService.getCurrentWeaver();
		for (MultipartFile file : files.values())
			if(!file.isEmpty()){
				String fileID= dataService.getObjectID(file.getOriginalFilename(), weaver);
				if(!fileID.equals(""))
					datas.add(new Data(fileID,file,weaver));
			}
		
		if(post == null || tags == null || title == null || title.length() < 5 || title.length() > 200){ // 태그가 없을 때
			model.addAttribute("say", "잘못 입력하셨습니다!!!");
			model.addAttribute("url", "/community/"+postID);
			return "/alert";
		}

		List<String> tagList = tagService.stringToTagList(tags);


		if(!tagService.validateTag(tagList,weaver) && !post.getWriter().equals(weaver)){ // 권한이 없을때
			model.addAttribute("say", "권한이 없습니다!!!");
			model.addAttribute("url", "/community/"+postID);
			return "/alert";
		}
		post.setTitle(title);
		post.setContent(content);
		post.setTags(tagList);
		postService.update(post,datas,remove.split("@"));
		
		return "redirect:/community/"+postID;	
	}


	@RequestMapping("/{postID}/{rePostID}/update")
	public String update(Model model, @PathVariable("postID") int postID, @PathVariable("rePostID") int rePostID) {		
		Post post = postService.get(postID);
		RePost rePost = rePostService.get(rePostID);
		Weaver weaver = weaverService.getCurrentWeaver();
		if(post == null || rePost == null || weaver == null || !rePost.getWriter().equals(weaver) ||
				rePost.getOriginalPost().getPostID() != post.getPostID()){
			model.addAttribute("say", "권한이 없습니다!!!");
			model.addAttribute("url", "/community/"+postID);
			return "/alert";
		}
		model.addAttribute("post", post);
		model.addAttribute("rePost", rePost);

		return "/post/updateRePost";
	}

	@RequestMapping(value="/{postID}/{rePostID}/update", method = RequestMethod.POST)
	public String update(@PathVariable("postID") int postID, @PathVariable("rePostID") int rePostID,HttpServletRequest request,Model model) throws UnsupportedEncodingException {		

		final MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
		final Map<String, MultipartFile> files = multiRequest.getFileMap();
		ArrayList<Data> datas = new ArrayList<Data>();
		Post post = postService.get(postID);
		RePost rePost = rePostService.get(rePostID);
		String content = request.getParameter("content");
		Weaver weaver = weaverService.getCurrentWeaver();
		String remove = request.getParameter("remove");
		
		if(post == null || rePost == null || content.length() < 5 ||
				rePost.getOriginalPost().getPostID() != post.getPostID()){ // 태그가 없을 때
			model.addAttribute("say", "잘못 입력하셨습니다!!!");
			model.addAttribute("url", "/community/"+postID);
			return "/alert";
		}	

		if(!rePost.getWriter().equals(weaver) &&
				!tagService.validateTag(post.getTags(),weaver)){ // 태그에 권한이 없을때
			model.addAttribute("say", "권한이 없습니다!!!");
			model.addAttribute("url", "/community/"+postID);
			return "/alert";
		}	
		
		for (MultipartFile file : files.values())
			if(!file.isEmpty()){
				String fileID= dataService.getObjectID(file.getOriginalFilename(), weaver);
				if(!fileID.equals(""))
					datas.add(new Data(fileID,file,weaver));
			}
		
		rePost.setContent(content);
		rePostService.update(rePost,datas,remove.split("@"));

		return "redirect:/community/"+postID;	
	}

	@RequestMapping("/{postID}/delete")
	public String deletePost(Model model, @PathVariable("postID") int postID) {
		if(!postService.delete(weaverService.getCurrentWeaver(),postService.get(postID))){
			model.addAttribute("say", "삭제하지 못하였습니다!!!");
			model.addAttribute("url", "/community/"+postID);
			return "/alert";
		}	

		return "redirect:/community/";
	}

	@RequestMapping("/{postID}/{rePostID}/delete")
	public String deleteRePost(Model model, @PathVariable("postID") int postID, @PathVariable("rePostID") int rePostID) {
		Post post = postService.get(postID);
		RePost rePost = rePostService.get(rePostID);
		Weaver weaver =weaverService.getCurrentWeaver();

		if(!rePostService.delete(post,rePost,weaver)){
			model.addAttribute("say", "삭제하지 못하였습니다!!!");
			model.addAttribute("url", "/community/"+postID);
			return "/alert";
		}	

		return "redirect:/community/"+postID;	
	}

}
