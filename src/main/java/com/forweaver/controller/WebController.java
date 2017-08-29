package com.forweaver.controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.forweaver.domain.Weaver;
import com.forweaver.service.WeaverService;


@Controller
public class WebController {
	
	private static final Logger logger =
			LoggerFactory.getLogger(WebController.class);

	@Autowired 
	private WeaverService weaverService;

	@RequestMapping("/error500")
	public void error500() {}

	@RequestMapping("/error400")
	public void error400() {}

	@RequestMapping("/error404")
	public void error404() {
	}

	@RequestMapping("/errorUserNull")
	public void errorUserNull() {}

	@RequestMapping("/")
	public String front(Model model) {
		Weaver weaver = weaverService.getCurrentWeaver();
		if(weaver == null)
			return "redirect:/login?state=null";
		return "redirect:/"+weaver.getId()+"/repository";
	}
}
