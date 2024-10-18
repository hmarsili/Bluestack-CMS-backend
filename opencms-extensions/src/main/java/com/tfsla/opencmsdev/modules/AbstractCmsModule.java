package com.tfsla.opencmsdev.modules;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import com.tfsla.opencms.exceptions.ProgramException;

public abstract class AbstractCmsModule {

	protected final String readFileContents(String fileName) {
		try {
			InputStream mailTemplate = this.getClass().getResource(fileName).openStream();
			ProgramException.assertTrue("file " + fileName + " not found", mailTemplate != null);

			StringBuffer fileData = new StringBuffer(1000);
			InputStreamReader reader = new InputStreamReader(mailTemplate);
			char[] buf = new char[1024];
			int numRead = 0;
			while ((numRead = reader.read(buf)) != -1) {
				String readData = String.valueOf(buf, 0, numRead);
				fileData.append(readData);
				buf = new char[1024];
			}
			reader.close();
			String fileContents = fileData.toString();

			return fileContents;
		}
		catch (IOException e) {
			throw ProgramException.wrap("Error al intentar leer el archivo [" + fileName + "]", e);
		}
	}
	
	/**
	 * @return la cookie de nombre cookieName, o null si dicha cookie no esta presente.
	 */
	protected Cookie getCookie(HttpServletRequest request, String cookieName) {
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (int i = 0; i < cookies.length; i++) {
				Cookie cookie = cookies[i];
				if (cookie.getName().equals(cookieName)) {
					return cookie;
				}
			}
		}

		return null;
	}

}