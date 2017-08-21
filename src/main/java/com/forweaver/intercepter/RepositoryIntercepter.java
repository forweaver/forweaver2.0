package com.forweaver.intercepter;

import java.net.URLDecoder;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.forweaver.domain.Repository;
import com.forweaver.domain.Weaver;
import com.forweaver.service.RepositoryService;
import com.forweaver.service.TagService;
import com.forweaver.service.WeaverService;

/** 저장소와 관련하여 회원의 권한을 분석하여 제어하는 인터셉터
*
*/
public class RepositoryIntercepter extends HandlerInterceptorAdapter {
	@Autowired WeaverService weaverService;

	@Autowired TagService tagService;

	@Autowired RepositoryService repositoryService;
	public boolean preHandle(HttpServletRequest request, 
			HttpServletResponse response, Object handler)
					throws Exception {
		String uri = request.getRequestURI();
		
		String repositoryName = new String();
		if (uri.split("/").length>3){
			repositoryName= uri.split("/")[2]+"/"+uri.split("/")[3];
		}else
			return true;
		if(uri.endsWith("/join-cancel") || uri.endsWith("/join-ok") ||uri.endsWith("/join") || repositoryName.startsWith("sort:") || repositoryName.startsWith("tags:"))
			return true;
		Weaver weaver = weaverService.getCurrentWeaver();
		Repository repository = repositoryService.get(repositoryName);
		
		if(uri.contains("/tags:")){
			String tags = uri.substring(uri.indexOf("/tags:")+6);
			if(tags.contains("/"))
				tags = tags.substring(0, tags.indexOf("/"));
			tags = URLDecoder.decode(tags, "UTF-8");
			List<String> tagList = tagService.stringToTagList(tags);
			if(!tagService.validateTag(tagList, weaver)){
				response.sendError(400);
				return false;
			}
		}
		if(repository == null){
			response.sendError(404);
			return false;
		}
		
		if(repository.getAuthLevel() > 0){
			if(weaver == null){
				response.sendError(400);
				return false;
			}
			if(weaver.getPass(repositoryName) != null)
				return true;			
		}

		return true;
	}

}
