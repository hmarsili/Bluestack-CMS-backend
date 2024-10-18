package com.tfsla.opencmsdev.encuestas;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import org.opencms.file.CmsObject;

public class YaVotoProcess extends AbstractEncuestaProcess {

	public boolean execute(HttpServletRequest request, CmsObject cms, Encuesta encuesta, String Username) {
		
		boolean yaVoto = false;
		
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (int i = 0; i < cookies.length; i++) {
				Cookie cookie = cookies[i];
				if (cookie.getName().equals(encuesta.getEncuestaURL()) && ModuloEncuestas.COOKIE_NAME.equals(cookie.getValue())) {
					yaVoto = true;
				}
			}
		}
		
		// me fijo si viene con nombre de usuario
		if(!yaVoto && Username !=null){
			boolean yaVotoUser = ModuloEncuestas.yaVotoUsuario(request, cms, encuesta, Username);
			
			if(yaVotoUser){
				yaVoto = true;
			}else{
				yaVoto = false;
			}
		}

		if(!yaVoto && Username==null ){
			// Si las cookies son nulas revisa por ip
		    boolean yaVotoIP = ModuloEncuestas.yaVotoIP(request,cms,encuesta);
		    
		    if(yaVotoIP){
		    	yaVoto = true;
		    }else{
		    	yaVoto = false;
		    }
		}

		return yaVoto;
	}

	@Deprecated
	public boolean execute(HttpServletRequest request, CmsObject cms, String encuestaURL, String Username) {
		
		boolean yaVoto = false;
		
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (int i = 0; i < cookies.length; i++) {
				Cookie cookie = cookies[i];
				if (cookie.getName().equals(encuestaURL) && ModuloEncuestas.COOKIE_NAME.equals(cookie.getValue())) {
					yaVoto = true;
				}
			}
		}
		
		// me fijo si viene con nombre de usuario
		if(!yaVoto && Username !=null){
			boolean yaVotoUser = ModuloEncuestas.yaVotoUsuario(request, cms, encuestaURL, Username);
			
			if(yaVotoUser){
				yaVoto = true;
			}else{
				yaVoto = false;
			}
		}

		if(!yaVoto && Username==null ){
			// Si las cookies son nulas revisa por ip
		    boolean yaVotoIP = ModuloEncuestas.yaVotoIP(request,cms,encuestaURL);
		    
		    if(yaVotoIP){
		    	yaVoto = true;
		    }else{
		    	yaVoto = false;
		    }
		}

		return yaVoto;
	}

}