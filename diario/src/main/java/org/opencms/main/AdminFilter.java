package org.opencms.main;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opencms.file.CmsObject;
import org.opencms.security.CmsRole;


public class AdminFilter extends TFSFilter {
@Override

	public void doFilter(ServletRequest arg0, ServletResponse arg1, FilterChain arg2) throws IOException, ServletException {
		if (arg0 instanceof HttpServletRequest) {
			TfsContext.getInstance().setRequest((HttpServletRequest) arg0);
			TfsContext.getInstance().setResponse((HttpServletResponse) arg1);
		}
	
		CmsObject object = TfsContext.getInstance().getCmsObject();
		if(object.hasRole(CmsRole.ADMINISTRATOR)) {
			arg2.doFilter(arg0, arg1);
		}
		else {
			arg1.setContentType("text/html;charset=UTF-8");
			arg1.getOutputStream().print("<html><body><b>Debe loguearse como un usuario administrador para acceder a este recurso</b></body></html>");
		}
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
	}
}
