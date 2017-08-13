package com.forweaver.intercepter;

import java.net.URLDecoder;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.forweaver.domain.Weaver;
import com.forweaver.service.TagService;
import com.forweaver.service.WeaverService;

/** 커뮤니티와 관련하여 회원의 권한을 분석하여 제어하는 인터셉터
 *
 */
public class CommunityIntercepter extends HandlerInterceptorAdapter {
	@Autowired WeaverService weaverService;
	@Autowired TagService tagService;

	public boolean preHandle(HttpServletRequest request, 
			HttpServletResponse response, Object handler)
					throws Exception {
		String uri = request.getRequestURI();
		Weaver weaver = weaverService.getCurrentWeaver();
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
		return true;
	}

}
