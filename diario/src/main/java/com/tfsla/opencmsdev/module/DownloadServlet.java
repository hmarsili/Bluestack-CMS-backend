package com.tfsla.opencmsdev.module;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

public class DownloadServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static Comparator<File> comparator = makeComparator();

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

		String file = getFileName(req);
		// Podrán ser dos servlets distintos, pero no quiero configurar mucho
		if (file != null) {
			this.download(res, file);
		}
		else {
			res.setContentType("text/html;charset=ISO-8859-1");
			this.getFiles(req, "modules", "../packages/modules");
			this.getFiles(req, "config", "../config");
			this.getFiles(req, "logs", "../logs");
			this.forwardJSP(req, res);
		}
	}
	private void forwardJSP(HttpServletRequest req, HttpServletResponse res) throws IOException {
		res.getOutputStream().println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3c.org/TR/1999/REC-html401-19991224/loose.dtd\">");
		res.getOutputStream().println("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
		res.getOutputStream().println("<head>");
		res.getOutputStream().println("<title>File download</File>");
		res.getOutputStream().println("</head>");
		res.getOutputStream().println("		<html>");
		res.getOutputStream().println(this.printTable(req, "config"));
		res.getOutputStream().println(this.printTable(req, "modules"));
		res.getOutputStream().println(this.printTable(req, "logs"));
		res.getOutputStream().println("		</html>");
	}
	private String getLink(HttpServletRequest req, File file) {
		return "<a href=\"" + req.getRequestURI() +  "?url=" + file.getAbsolutePath() + "\">" + file.getName() + "</a>";
	}

	private String printCollection(HttpServletRequest req, String collectionName) {
		return printCollection(req, (Collection) req.getAttribute(collectionName));
	}
	
	private String printTable(HttpServletRequest req, String collectionName) {
		StringBuffer strBuffer = new StringBuffer();
		strBuffer.append("\t\t\t<b>" + StringUtils.capitalize(collectionName) + "</b><br />\n");
		strBuffer.append("\t\t\t<table>\n");
		strBuffer.append("\t\t\t\t<tr>\n");
		printColumnValue(strBuffer, "<b>archivo</b>");
		printColumnValue(strBuffer, "<b>&uacute;ltima modificaci&oacute;n</b>");
		printColumnValue(strBuffer, "<b>size</b>");		
		strBuffer.append("\t\t\t\t</tr>\n");
		strBuffer.append(printCollection(req, collectionName));
		strBuffer.append("\t\t\t</table>\n");
		strBuffer.append("\t\t<br />\n");
		return strBuffer.toString();
	}

	private String printCollection(HttpServletRequest req, Collection collection) {
		StringBuffer str = new StringBuffer();
		for (Iterator iter = collection.iterator(); iter.hasNext();) {
			File file = (File) iter.next();
			str.append("\t\t\t\t<tr>\n");
			printColumnValue(str, getLink(req, file));
			printColumnValue(str, String.valueOf(new SimpleDateFormat("dd-MM-yyyy hh:mm").format(new Date(file.lastModified()))));
			printColumnValue(str, 	file.length() > 1000 ? String.valueOf(file.length()/1000) + " kb" : String.valueOf(file.length()) + " b"); 
			str.append("\t\t\t\t</tr>\n");
		}
		return str.toString();
	}
	private void printColumnValue(StringBuffer str, String value) {
		str.append("\t\t\t\t\t<td>");
		str.append(value);
		str.append("</td>\n");
	}

	private void getFiles(HttpServletRequest req, String name, String string) {
		File folder;
		try {
			folder = new File(this.getClass().getClassLoader().getResource(string).toURI());
		}
		catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
		Set<File> set = new TreeSet<File>(comparator);
		for (File file : folder.listFiles()) {
			if (file.isFile()) {
				set.add(file);
			}
		}
		req.setAttribute(name, set);
	}

	private static Comparator<File> makeComparator() {
		return new Comparator<File>() {
			public int compare(File o1, File o2) {
				int ret =  new Date(o2.lastModified()).compareTo(new Date(o1.lastModified()));
				//si tienen la misma fecha de modificacion, entonces le dejo al filesystem que ordene
				return (ret == 0) ?  o1.compareTo(o2) : ret; 
			}
		};
	}

	private void download(HttpServletResponse res, String file) throws IOException {
		javax.servlet.ServletOutputStream out = res.getOutputStream();
		res.setContentType("application/x-download");
		res.setHeader("Content-Disposition", "attachment; filename=" + file.substring(file.lastIndexOf('/') + 1));
		printResourceInResponse(out, file);
	}

	private void printResourceInResponse(javax.servlet.ServletOutputStream out, String file) throws IOException {
		InputStream in = new FileInputStream(file);
		byte[] buf = new byte[4 * 1024]; // 4K buffer
		int bytesRead;
		try {
			
			while ((bytesRead = in.read(buf)) != -1) {
				out.write(buf, 0, bytesRead);
			}
		}
		finally {
			in.close();
		}
	}

	private String getFileName(HttpServletRequest req) {
		return req.getParameter("url");
	}

	@Override
	protected void doGet(HttpServletRequest arg0, HttpServletResponse arg1) throws ServletException, IOException {
		this.doPost(arg0, arg1);
	}

}
