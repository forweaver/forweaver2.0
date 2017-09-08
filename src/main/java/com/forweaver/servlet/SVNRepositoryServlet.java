package com.forweaver.servlet;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.tmatesoft.svn.core.internal.server.dav.DAVServlet;

@WebServlet("/s/*")
public class SVNRepositoryServlet extends DAVServlet{
	private static final long serialVersionUID = 1L;
	@Value("svn.path")
	private String svnPath;

	@Override
	public void init(ServletConfig config) throws ServletException {
		// TODO Auto-generated method stub
		super.init(new ServletConfig() {

			public String getServletName() {
				// TODO Auto-generated method stub
				return "svnServlet";
			}

			public ServletContext getServletContext() {
				// TODO Auto-generated method stub
				return null;
			}

			public Enumeration<String> getInitParameterNames() {
				// TODO Auto-generated method stub
				return null;
			}

			public String getInitParameter(String name) {
				// TODO Auto-generated method stub
				if("SVNParentPath".equals(name))
					return svnPath;
				return null;
			}
		});
	}

	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		super.service(request, response);

}

}