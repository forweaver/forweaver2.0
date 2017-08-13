package com.forweaver.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.forweaver.domain.Code;
import com.forweaver.domain.Data;
import com.forweaver.domain.RePost;
import com.forweaver.domain.Reply;
import com.forweaver.domain.SimpleCode;
import com.forweaver.domain.Weaver;
import com.forweaver.service.CodeService;
import com.forweaver.service.DataService;
import com.forweaver.service.RePostService;
import com.forweaver.service.TagService;
import com.forweaver.service.WeaverService;
import com.forweaver.util.WebUtil;


@Controller
@RequestMapping("/code")
public class CodeController {
	@Autowired 
	private CodeService codeService;
	@Autowired 
	private TagService tagService;
	@Autowired 
	private WeaverService weaverService;
	@Autowired 
	private RePostService rePostService;
	@Autowired 
	private DataService dataService;

	@RequestMapping("/")
	public String front(){
		return "redirect:/code/sort:age-desc/page:1";
	}


	@RequestMapping("/sort:{sort}/page:{page}")
	public String page(@PathVariable("page") String page,
			@PathVariable("sort") String sort,Model model){
		int pageNum = WebUtil.getPageNumber(page);
		int size = WebUtil.getPageSize(page,5);
		Weaver currentWeaver = weaverService.getCurrentWeaver();
		
		model.addAttribute("codes", 
				codeService.getCodes(currentWeaver, null, null, sort, pageNum, size));
		model.addAttribute("codeCount", 
				codeService.countCodes(currentWeaver, null, null, sort));

		model.addAttribute("pageIndex", pageNum);
		model.addAttribute("number", size);
		model.addAttribute("pageUrl", "/code/sort:"+sort+"/page:");
		return "/code/front";
	}

	@RequestMapping("/tags:{tagNames}")
	public String tags(HttpServletRequest request){
		return "redirect:"+request.getRequestURI() +"/sort:age-desc/page:1";
	}

	@RequestMapping("/tags:{tagNames}/sort:{sort}/page:{page}")
	public String tagsWithPage(@PathVariable("tagNames") String tagNames,
			@PathVariable("page") String page,
			@PathVariable("sort") String sort,Model model){
		List<String> tags = tagService.stringToTagList(tagNames);

		int pageNum = WebUtil.getPageNumber(page);
		int size = WebUtil.getPageSize(page,5);

		Weaver currentWeaver = weaverService.getCurrentWeaver();
		if(!tagService.validateTag(tags,currentWeaver)){
			return "redirect:/code/sort:age-desc/page:1";
		}

		model.addAttribute("codes", 
				codeService.getCodes(currentWeaver, tags, null, sort, pageNum, size));
		model.addAttribute("codeCount", 
				codeService.countCodes(currentWeaver,tags, null, sort));

		model.addAttribute("tagNames", tagNames);
		model.addAttribute("pageIndex", pageNum);
		model.addAttribute("number", size);
		model.addAttribute("pageUrl", "/code/tags:"+tagNames+"/sort:"+sort+"/page:");

		return "/code/front";
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
		List<String> tags = tagService.stringToTagList(tagNames);
		
		Weaver currentWeaver = weaverService.getCurrentWeaver();
		if(!tagService.validateTag(tags,currentWeaver)){
			return "redirect:/code/sort:age-desc/page:1";
		}
		
		int pageNum = WebUtil.getPageNumber(page);
		int size = WebUtil.getPageSize(page,5);

		model.addAttribute("codes", 
				codeService.getCodes(currentWeaver,tags, search, sort, pageNum, size));
		model.addAttribute("codeCount", 
				codeService.countCodes(currentWeaver,tags, search, sort));

		model.addAttribute("number", size);
		model.addAttribute("tagNames", tagNames);
		model.addAttribute("search", search);
		model.addAttribute("pageIndex", pageNum);
		model.addAttribute("pageUrl", "/tags:"+tagNames+"/search:"+search+"/sort:"+sort+"/page:");

		return "/code/front";
	}

	@RequestMapping(value = "/add", method = RequestMethod.POST)
	public String add(final HttpServletRequest request,Model model) throws UnsupportedEncodingException {
		final MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;

		MultipartFile file = multiRequest.getFile("file");
		MultipartFile output = multiRequest.getFile("output");
		String tags = request.getParameter("tags");
		String content = request.getParameter("content");
		String url = request.getParameter("url");
		String name = file.getOriginalFilename();
		name = name.substring(0, name.indexOf('.'));
		if(tags == null || content.length() < 5 || content.length() >50 || file == null){ // 태그가 없을 때
			model.addAttribute("say", "잘못 입력하셨습니다!!!");
			model.addAttribute("url", "/code/");
			return "/alert";
		}
		List<String> tagList = tagService.stringToTagList(tags);
		Weaver weaver = weaverService.getCurrentWeaver();
		if(!codeService.add(new Code(weaver, name, content,url, tagList), file,output)){ // 태그가 없을 때
			model.addAttribute("say", "코드 업로드에 실패하였습니다! 압축파일을 확인하시거나 제대로된 소스파일인지 확인해주세요.");
			model.addAttribute("url", "/code/");
			return "/alert";
		}
		return "redirect:/code/";
	}

	@RequestMapping("/{codeID}")
	public String view(@PathVariable("codeID") int codeID){
		return "redirect:/code/"+codeID+"/sort:age-desc";
	}

	@RequestMapping("/{codeID}/delete")
	public String delete(@PathVariable("codeID") int codeID,Model model){
		Weaver currentWeaver = weaverService.getCurrentWeaver();
		Code code = codeService.get(codeID,true);

		if(!codeService.delete(currentWeaver,code)){
			model.addAttribute("say", "코드를 삭제하지 못했습니다. 권한을 확인해보세요!");
			model.addAttribute("url", "/code/");
			return "/alert";
		}
		return "redirect:/code/";
	}

	@RequestMapping("/{codeID}/sort:{sort}")
	public String view(Model model, @PathVariable("codeID") int codeID,
			@PathVariable("sort") String sort) {
		Code code = codeService.get(codeID,true);
		
		model.addAttribute("code", code);
		model.addAttribute("rePosts", rePostService.gets(codeID,4,sort));

		return "/code/viewCode";
	}
	
	@RequestMapping("/{codeID}/**")
	public void viewFile(@PathVariable("codeID") int codeID, 
			HttpServletRequest request,HttpServletResponse res) throws IOException {
		Code code = codeService.get(codeID,false);
		String uri = URLDecoder.decode(request.getRequestURI(),"UTF-8");
		String filePath = uri.substring(uri.indexOf("/code/")+6+(""+codeID).length());		
		SimpleCode simpleCode = code.getSimpleCode(filePath);

		if (simpleCode == null) {
			res.sendRedirect("http://www.gravatar.com/avatar/a.jpg");
			return;
		}
		byte[] imgData;
		
		
		if(WebUtil.isCodeName(filePath))
			imgData = simpleCode.getContent().getBytes("EUC-KR");
		else
			imgData = simpleCode.getContent().getBytes("8859_1");
		
			res.reset();
			res.setContentType("application/octet-stream");
			String filename = new String(simpleCode.getFileName().getBytes("UTF-8"), "8859_1");
			res.setHeader("Content-Disposition", "attachment; filename = " + filename);
			
			OutputStream o = res.getOutputStream();
			o.write(imgData);
			o.flush();
			o.close();
			return;

	}

	@RequestMapping("/{codeID}/{codeName}.zip")
	public void download(HttpServletResponse res, 
			@PathVariable("codeID") int codeID,
			@PathVariable("codeName") String codeName) throws IOException {
		Code code = codeService.get(codeID,false);
		res.reset();
		res.setContentType("application/octet-stream");
		res.setHeader("Content-Disposition", "attachment; filename = " + codeName+".zip");
		res.setContentType(WebUtil.getFileExtension(codeName+".zip"));
		codeService.dowloadCode(code, res.getOutputStream());
	}


	@RequestMapping(value = "/{codeID}/add-repost", method = RequestMethod.POST)
	public String addRepost(@PathVariable("codeID") int codeID,
			HttpServletRequest request,Model model) throws UnsupportedEncodingException {

		final MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;

		final Map<String, MultipartFile> files = multiRequest.getFileMap();	

		String content = request.getParameter("content");
		Weaver weaver = weaverService.getCurrentWeaver();
		Code code = codeService.get(codeID,true);

		if(code == null || weaver == null || content.equals("")) {
			model.addAttribute("say", "잘못 입력하셨습니다!!!");
			model.addAttribute("url", "/code/"+codeID);
			return "/alert";
		}

		ArrayList<Data> datas = new ArrayList<Data>();
		for (MultipartFile file : files.values()) {
			if(!file.isEmpty())
				datas.add(new Data(dataService.getObjectID(file.getOriginalFilename(), weaver),file,weaver));
		}

		RePost rePost = new RePost(code,
				weaver,
				content);
		rePostService.add(rePost,datas);
		code.setRePostCount(code.getRePostCount()+1);
		code.setRecentRePostDate(rePost.getCreated());
		codeService.update(code);

		return "redirect:/code/"+codeID;
	}
	
	@RequestMapping("/{codeID}/{rePostID}/update")
	public String update(Model model, @PathVariable("codeID") int codeID, @PathVariable("rePostID") int rePostID) {		
		Code code = codeService.get(codeID,true);
		RePost rePost = rePostService.get(rePostID);
		Weaver weaver = weaverService.getCurrentWeaver();
		if(code == null || rePost == null || weaver == null || !rePost.getWriter().equals(weaver) ||
				rePost.getOriginalCode().getCodeID() != code.getCodeID()){
			model.addAttribute("say", "권한이 없습니다!!!");
			model.addAttribute("url", "/code/"+codeID);
			return "/alert";
		}
		model.addAttribute("code", code);
		model.addAttribute("rePost", rePost);

		return "/code/updateRePost";
	}

	@RequestMapping(value="/{codeID}/{rePostID}/update", method = RequestMethod.POST)
	public String update(@PathVariable("codeID") int codeID, @PathVariable("rePostID") int rePostID,HttpServletRequest request,Model model) throws UnsupportedEncodingException {		

		final MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
		final Map<String, MultipartFile> files = multiRequest.getFileMap();
		ArrayList<Data> datas = new ArrayList<Data>();
		Code code = codeService.get(codeID,true);
		RePost rePost = rePostService.get(rePostID);
		String content = request.getParameter("content");
		Weaver weaver = weaverService.getCurrentWeaver();
		String remove = request.getParameter("remove");
		if(code == null || rePost == null || content.length() < 5 ||  
				rePost.getOriginalCode().getCodeID() != code.getCodeID()){
			model.addAttribute("say", "잘못 입력하셨습니다!!!");
			model.addAttribute("url", "/code/"+codeID);
			return "/alert";
		}	

		if(!rePost.getWriter().equals(weaver) && 
				!tagService.validateTag(code.getTags(),weaver)){ // 태그에 권한이 없을때
			model.addAttribute("say", "권한이 없습니다!!!");
			model.addAttribute("url", "/code/"+codeID);
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

		return "redirect:/code/"+codeID;
	}

	@RequestMapping(value = "/{codeID}/{rePostID}/add-reply", method = RequestMethod.POST)
	public String addReply(@PathVariable("codeID") int codeID,
			@PathVariable("rePostID") int rePostID,
			HttpServletRequest request,Model model) throws UnsupportedEncodingException {
		String content = request.getParameter("content");
		RePost rePost = rePostService.get(rePostID);
		Weaver weaver = weaverService.getCurrentWeaver();

		if(!rePostService.addReply(rePost,new Reply(weaver, content))){
			model.addAttribute("say", "댓글을 추가히지 못했습니다. 권한을 확인해보세요!");
			model.addAttribute("url", "/code/"+codeID);
			return "/alert";
		}
		return "redirect:/code/"+codeID;	
	}

	@RequestMapping(value="/{codeID}/{rePostID}/{number}/delete")
	public String deleteReply(@PathVariable("codeID") int codeID, 
			@PathVariable("rePostID") int rePostID,
			@PathVariable("number") int number,
			HttpServletRequest request,Model model) {		

		RePost rePost = rePostService.get(rePostID);
		Weaver weaver = weaverService.getCurrentWeaver();

		if(!rePostService.deleteReply(rePost, weaver, number)){		
			model.addAttribute("say", "삭제하지 못했습니다. 권한을 확인해보세요!");
			model.addAttribute("url", "/code/"+codeID);
			return "/alert";
		}
		return "redirect:/code/"+codeID;	
	}
	
	@RequestMapping("/{codeID}/{repostID}/push")
	public String rePostPush(Model model, @PathVariable("codeID") int codeID, @PathVariable("repostID") int repostID) {
		RePost rePost = rePostService.get(repostID);
		rePostService.push(rePost,weaverService.getCurrentWeaver(),weaverService.getUserIP());

		return "redirect:/code/"+codeID+"/";
	}
	@RequestMapping("/{codeID}/{rePostID}/delete")
	public String deleteRePost(Model model, @PathVariable("codeID") int codeID, @PathVariable("rePostID") int rePostID) {
		Code code = codeService.get(codeID,true);
		RePost rePost = rePostService.get(rePostID);
		Weaver weaver =weaverService.getCurrentWeaver();

		if(!rePostService.delete(code,rePost,weaver)){
			model.addAttribute("say", "삭제하지 못했습니다. 권한을 확인해보세요!");
			model.addAttribute("url", "/code/"+codeID);
			return "/alert";
		}
		return "redirect:/code/"+codeID;	

	}
}
