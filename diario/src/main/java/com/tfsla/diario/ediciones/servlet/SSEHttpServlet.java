package com.tfsla.diario.ediciones.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsRequestUtil;

import com.tfsla.diario.ediciones.model.ServerEvent;
import com.tfsla.diario.ediciones.services.SSEService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class SSEHttpServlet extends HttpServlet{

	private static final long serialVersionUID = 4551049459489821878L;

	private String userName;
	
	protected void doGet(HttpServletRequest req, HttpServletResponse res) {
		res.setContentType("text/event-stream");
		res.setCharacterEncoding("UTF-8");

		String token = getToken(req);
		String browserId = getBrowserId(req);
		
		SSEService service = SSEService.getInstance();
		
		 // check for special header for remote address
        String remoteAddr = req.getHeader(CmsRequestUtil.HEADER_X_FORWARDED_FOR);
        if (remoteAddr == null) {
            remoteAddr = req.getRemoteAddr();
        }
		
		try {
			CmsObject cms = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserExport());
			cms.loginUserByToken(token, browserId, remoteAddr);
			userName = cms.getRequestContext().currentUser().getName();
			
			service.subscribe(userName);
			
			welcomeMsg(res);
			sendEvents(res, service);
		}
		catch (CmsException e) {
			e.printStackTrace();
			
		}
		
	}

	private void welcomeMsg(HttpServletResponse res) {
		PrintWriter print = null;
		try {
			print = res.getWriter();
			print.print("event: init\n");
			print.print("data: " + userName + " welcome to sse service\n");
			print.print("\n");
			res.flushBuffer();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void sendEvents(HttpServletResponse res, SSEService service) {
		PrintWriter print = null;
		List<ServerEvent> events = null;
		while (true) {
			try {
				print = res.getWriter();
				
				events = service.getEvents(userName, 10);
				
				Iterator<ServerEvent> it = events.iterator();
				while (it.hasNext()) {
					ServerEvent event = it.next();
					print.print(event.toString());
					res.flushBuffer();
					it.remove();
				}
				
				res.flushBuffer();
				Random randGen = new Random();
				Thread.sleep(500 + randGen.nextInt(0, 300));
			
			} catch (IOException | InterruptedException e) {
				print.close();
				
				service.putBackEvents(userName, events);
				
				e.printStackTrace();
				
				
				break;
			}
			service.updateLastInteracion(userName);
		}
	}
	
	private String getToken(HttpServletRequest req) 
	{
		Cookie[] cookies = req.getCookies();
		for (int j=0; j<cookies.length; j++) {
			Cookie cookie = cookies[j];
			
			if (cookie.getName().equals("token"))
				return cookie.getValue();	
		}
		
		return null;
	}

	private String getBrowserId(HttpServletRequest req) 
	{
		Cookie[] cookies = req.getCookies();
		for (int j=0; j<cookies.length; j++) {
			Cookie cookie = cookies[j];
			//System.out.println(cookie.getName());
			if (cookie.getName().equals("browserId"))
				return cookie.getValue();	
		}
		
		return null;
	}
	
}
