package com.forweaver.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.forweaver.domain.Pass;
import com.forweaver.domain.Weaver;

/** 태그 관리를 위한 서비스
 *
 */
@Service
public class TagService {

	/** 실수로 자신의 메시지 태그를 붙이면 지움.
	 * @param tagList
	 * @param weaver
	 * @return
	 */
	public List<String> removeMyMassageTag(List<String> tagList,Weaver weaver){
		 List<String> tags = new ArrayList<String>();
		 
		for (String tag : tagList)
			if(!tag.equals("$"+weaver.getId()))
				tags.add(tag);
		
		return tags;
	}
	
	public String getPrivateTag(List<String> tagList){
		for (String tag : tagList) 
			if (tag.startsWith("@"))
					return tag;
			
		return null;
	}
	
	public boolean validateTag(List<String> tagList,Weaver weaver) {
		System.out.println("validateTag");
		System.out.println(tagList);
		List<String> publicTags = new ArrayList<String>();
		List<String> privateTags = new ArrayList<String>();
		List<String> massageTags = new ArrayList<String>();
		boolean myTag = false;
		if (tagList.size() == 0)
			return false;

		for (String tag : tagList) {
			if(tag.equals("$")||tag.equals("@")||tag.contains(".")||tag.contains("?") || tag.contains("#"))
				return false;
			
			if (tag.startsWith("@")){
					privateTags.add(tag);
			}else if(tag.startsWith("$")){
				if(weaver == null) return false;
				
				if(tag.equals("$"+weaver.getId()))
					myTag = true;
				massageTags.add(tag);
			}
			else
				publicTags.add(tag);
		}

		if(weaver!= null && weaver.isAdmin())
			return true;
		
		if(this.isPublicTags(tagList))
			return true;
		
		if (privateTags.size() >= 2) // 권한을 가진 태그가 2개일때
			return false;
		
		if(massageTags.size() >= 3) //메세지 태그가 세개 이상일 때
			return false;
		
		if(!myTag && massageTags.size() == 2) //자신의 태그가 없고 메세지 태그가 두개 아닐 때
			return false;
		
		if (privateTags.size() >0 && massageTags.size() > 0) // 권한을 가진 태그가 있고 메세지 태그가 있을때
			return false;
		
		if(massageTags.size() > 0 && weaver != null)  // 메시지 태그가 있고 로그인한 사람의 경우
			return true;
		
		if (weaver == null) // 권한을 가진 태그가 있는데 로그인 안한 사람일때
			return false;
		
		for (Pass pass : weaver.getPasses()) // 권한 검증
			if (privateTags.size() > 0 && privateTags.get(0).equals("@" + pass.getJoinName())) 
				return true;
		
		return false;
	}

	public List<String> stringToTagList(String tagNames) {
		List<String> tags = new ArrayList<String>();

		if(tagNames == null)
			return tags;	

		if(tagNames.startsWith(","))
			tagNames = tagNames.substring(1);
		

		for (String tagName : tagNames.split(",")) {

			if(tagName.startsWith("\""))
				tagName = tagName.substring(1, tagName.length()-1);

			if(tagName.equals(" ") || tagName.length() <= 0)
				break;

			tagName = tagName.replace(">", "/");
			tags.add(tagName);
		}
		return tags;
	}

	public boolean isPublicTags(List<String> tags) {
		
		if (tags.size() == 0)
			return false; 
		
		for (String tag : tags) {
			if (tag.startsWith("@") || tag.startsWith("$") || tag.contains(".")||tag.contains("?") || tag.contains("#"))
				return false;
		}
		return true;
	}
	


}
