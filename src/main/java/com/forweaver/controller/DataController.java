package com.forweaver.controller;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.forweaver.domain.Data;
import com.forweaver.domain.Weaver;
import com.forweaver.service.DataService;
import com.forweaver.service.WeaverService;

@Controller
@RequestMapping("/data")
public class DataController {
	@Autowired 
	private DataService dataService;
	@Autowired 
	private WeaverService weaverService;

	@RequestMapping(value = {"/{dataID}","/{dataID}/**"})
	public void data(@PathVariable("dataID") String dataID, HttpServletResponse res)
			throws IOException {
		Data data = dataService.get(dataID);
		if (data == null) {
			res.sendRedirect("http://www.gravatar.com/avatar/a.jpg");
			return;
		} else {
			byte[] imgData = data.getContent();
			res.reset();
			res.setContentType("application/octet-stream");
			String Encoding = new String(data.getName().getBytes("UTF-8"), "8859_1");
			res.setHeader("Content-Disposition", "attachment; filename = " + Encoding);
			res.setContentType(data.getType());
			OutputStream o = res.getOutputStream();
			o.write(imgData);
			o.flush();
			o.close();
			return;
		} 

	}

	@RequestMapping(value = "/tmp",method = RequestMethod.POST) // 임시로 파일을 저장함
	public void tmp(@RequestParam("objectID") String objectID,
			@RequestParam("file") MultipartFile file){
		Weaver currentWeaver = weaverService.getCurrentWeaver();
		Data data = new Data(objectID, file, currentWeaver);
		dataService.addTemp(data);
	}
}
