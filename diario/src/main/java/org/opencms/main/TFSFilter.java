package org.opencms.main;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class TFSFilter implements Filter {
	 
	private String encoding;
	
	public void init(FilterConfig arg0) throws ServletException {

		// Configuracion de acciones de tfs
		TfsContext.getInstance().setTimeBefore(new Integer(arg0.getInitParameter("timeBefore")));
		TfsContext.getInstance().setPurge(Boolean.valueOf(arg0.getInitParameter("purge")).booleanValue());
		
		encoding = arg0.getInitParameter( "requestEncoding" );
        if( encoding == null ) {
              encoding = "UTF-8";
        }
	}

	public void doFilter(ServletRequest arg0, ServletResponse arg1, FilterChain arg2) throws IOException,
			ServletException {

		arg0.setCharacterEncoding( encoding );
        arg1.setContentType("text/html; "+encoding);
        arg1.setCharacterEncoding(encoding);
        
		TfsContext.getInstance().setResponse((HttpServletResponse) arg1);
		TfsContext.getInstance().setRequest((HttpServletRequest) arg0);

		arg2.doFilter(arg0, arg1);

	}


	public void destroy() {
	}

}
