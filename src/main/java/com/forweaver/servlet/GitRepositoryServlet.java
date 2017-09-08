package com.forweaver.servlet;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jgit.http.server.GitServlet;

import com.forweaver.config.Config;

@WebServlet(urlPatterns = { "/g/*" })
public class GitRepositoryServlet extends GitServlet {
	private static final long serialVersionUID = 1L;
	private String gitPath;

	@Override
	public void init(ServletConfig config) throws ServletException {
		gitPath = Config.gitPath;
		super.init(new ServletConfig() {


			public String getServletName() {
				// TODO Auto-generated method stub
				return "gitServlet";
			}

			public ServletContext getServletContext() {
				// TODO Auto-generated method stub
				return null;
			}

			public Enumeration<String> getInitParameterNames() {
				// TODO Auto-generated method stub
				return null;
			}

			public String getInitParameter(String arg0) {
				// TODO Auto-generated method stub
				if("base-path".equals(arg0))
					return gitPath;
				if("export-all".equals(arg0))
					return "1";
				return "";
			}
		});
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		super.service(req, res);
	}
}