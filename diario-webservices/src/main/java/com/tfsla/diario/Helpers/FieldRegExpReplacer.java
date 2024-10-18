package com.tfsla.diario.Helpers;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsLog;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.tfsla.diario.webservices.ImagesAddWebService;
import com.tfsla.diario.webservices.core.SessionManager;
import com.tfsla.utils.CmsObjectUtils;

import jakarta.servlet.http.HttpServletRequest;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class FieldRegExpReplacer {


	private static final Log LOG = CmsLog.getLog(FieldRegExpReplacer.class);

	private String site;
	private String publication;
	private String ruleName;
	private String findPattern;
	private String replacePattern;
	private String imageImportBaseDomain = "";
	private String defaultImageSection="";
	private Date folderDate=null;

	private String report;
	
	private HttpServletRequest request;
	private CmsObject cms;
	
	
	public CmsObject getCms() {
		return cms;
	}

	public void setCms(CmsObject cms) {
		this.cms = cms;
	}

	public HttpServletRequest getRequest() {
		return request;
	}

	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}

	public String getSite() {
		return site;
	}

	public void setSite(String site) {
		this.site = site;
	}

	public String getPublication() {
		return publication;
	}

	public void setPublication(String publication) {
		this.publication = publication;
	}

	public String getDefaultImageSection() {
		return defaultImageSection;
	}

	public void setDefaultImageSection(String defaultImageSection) {
		this.defaultImageSection = defaultImageSection;
	}

	public int getTotalReplacements() {
		return totalReplacements;
	}

	public String getRuleName() {
		return ruleName;
	}

	public void setRuleName(String ruleName) {
		this.ruleName = ruleName;
	}

	public String getImageImportBaseDomain() {
		return imageImportBaseDomain;
	}

	public void setImageImportBaseDomain(String imageImportBaseDomain) {
		this.imageImportBaseDomain = imageImportBaseDomain;
	}

	private int totalReplacements = 0;
	
	public void getRule(String fileName) {
		// Instantiate the Factory
	      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

	      try {

	          // optional, but recommended
	          // process XML securely, avoid attacks like XML External Entities (XXE)
	          dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

	          // parse XML file
	          DocumentBuilder db = dbf.newDocumentBuilder();

	          
	          
	          Document doc = db.parse(this.getClass()
			            .getClassLoader()
			            .getResourceAsStream(fileName));
	          
	          
	          doc.getDocumentElement().normalize();

	          LOG.info("Leyendo regla: " + fileName);
	          
	          ruleName = doc.getDocumentElement().getElementsByTagName("name").item(0).getTextContent();
	          
	          LOG.info("Nombre de la regla: " + ruleName);

	          findPattern = doc.getDocumentElement().getElementsByTagName("findPattern").item(0).getTextContent().trim();
	          replacePattern = doc.getDocumentElement().getElementsByTagName("replacePattern").item(0).getTextContent().trim();
	         
	      } catch (ParserConfigurationException ex ) {
	          ex.printStackTrace();
	      } catch (SAXException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	  }
	
	public String replaceUniqueId(String fieldContent) {
		
		for (int nro=1;nro<=totalReplacements; nro++) {
			
			fieldContent = fieldContent.replaceFirst("#ID_UNICO", "" + nro);
		  
		}
		return fieldContent;
		
	}
	
	public String importImage(String fieldContent) {
		Pattern patt = Pattern.compile("#IMPORT_IMAGE\\(([^\\)]*)\\)");
		Matcher m = patt.matcher(fieldContent);
		StringBuffer sb = new StringBuffer();
		
		
		String vfsFilename = "";
		while (m.find()) {
			  String path = m.group(1);
			  //System.out.println(path);
			  String fileName = FilenameUtils.getName(path);
			  //System.out.println(fileName);
			  
			  String fullpath = "";
			  URI u;
			try {
				u = new URI(m.group(1));
				
				 if (u.isAbsolute())
					 fullpath = path;  
				  else 
					fullpath = imageImportBaseDomain + path;
			
				  report += " Importando imagen de " + fullpath;

				  try {
						final InputStream is = new URL(fullpath).openStream();
						
						CmsObject cmsClone = CmsObjectUtils.getClone(cms);
						SessionManager.switchToProject(cmsClone, SessionManager.PROJECT_OFFLINE);
						
						ImagesAddWebService addImageService = new ImagesAddWebService(request);
						vfsFilename = addImageService.uploadFile(cmsClone, is, fileName, folderDate);
						if (!vfsFilename.startsWith("/"))
							vfsFilename = "/" + vfsFilename;
						LOG.debug("Agregado de la imagen " + fileName + " + en vfs: " + vfsFilename);
						
						report += " a  " + vfsFilename + "\r\n";

						is.close();
					} catch (MalformedURLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (Throwable e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				
				 
				 m.appendReplacement(sb, vfsFilename);
				 
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			   
		
			
			  
			  
			  
		} 
		  m.appendTail(sb);
		  
		  fieldContent = sb.toString();
		  //System.out.println("quitado de #IMPORT_IMAGE");
			// System.out.println(fieldContent);
			 
		return fieldContent;
	}
	
	public String replaceAll(String fieldContent) {
		return replaceAll(fieldContent, findPattern, replacePattern);
		
	}
		
	public String replaceAll(String fieldContent, String findPattern, String replacePattern) {

		report = "";
		report += "Aplicando regla " + this.ruleName;
		
		LOG.debug("Aplicando regla " + this.ruleName);
		LOG.debug("Contenido :" + fieldContent);
		
		totalReplacements = 0;
		
		Pattern patt = Pattern.compile(findPattern);
		Matcher m = patt.matcher(fieldContent);
		int start = 0;
		  while (m.find(start)) {
			start =  m.start();
			String text = m.group();
			String replacement = text.replaceAll(findPattern, replacePattern);
			
			LOG.debug("Encontrado el patron en posicion :" + start);
			LOG.debug("Valor encontrado:");
			LOG.debug(text);
			LOG.debug("Reemplazado a:");
			LOG.debug(replacement);
			
			totalReplacements++;
			
		    start = m.end();
		  }
		  
		  report += " .... " + totalReplacements + " reemplazos realizados\r\n";
		  
		  //System.out.println("Todo el resultado:");
		  
		    
		  
		  fieldContent = fieldContent.replaceAll(findPattern, replacePattern);
		  
		  //System.out.println(fieldContent);
		  //System.out.println("------------------------------------------------");
		  fieldContent = replaceUniqueId(fieldContent);
		  //System.out.println(fieldContent);
		  //System.out.println("------------------------------------------------");
		  fieldContent = importImage(fieldContent);
		  
		  //System.out.println(fieldContent);
		  //System.out.println("------------------------------------------------");
		 
		  
		  return fieldContent;
		  
	}
	
	public static void main(String[] args) {
		FieldRegExpReplacer replacer = new FieldRegExpReplacer();
		
		replacer.setImageImportBaseDomain("https://vader.news");
		  

		replacer.getRule("ReplaceRules/image2RL.xml");
		
		/*
		// instagramRL.xml
		String fieldContent =  "<blockquote class=\"instagram-media\" data-instgrm-captioned=\"\" data-instgrm-permalink=\"https://www.instagram.com/p/CRmW85dLG4Y/?utm_source=ig_embed&amp;utm_campaign=loading\" data-instgrm-version=\"13\" style=\"background-color:#FFF;border-radius:3px;border:0;box-shadow:0 0 1px 0 rgba(0,0,0,0.5),0 1px 10px 0 rgba(0,0,0,0.15);margin:1px;max-width:540px;min-width:326px;padding:0;width:calc(100% - 2px);\">    <svg height=\"50px\" version=\"1.1\" viewbox=\"0 0 60 60\" width=\"50px\" xmlns=\"https://www.w3.org/2000/svg\"><g fill=\"none\" fill-rule=\"evenodd\" stroke=\"none\" stroke-width=\"1\"><g fill=\"#000000\" transform=\"translate(-511.000000, -20.000000)\"><g><path d=\"M556.869,30.41 C554.814,30.41 553.148,32.076 553.148,34.131 C553.148,36.186 554.814,37.852 556.869,37.852 C558.924,37.852 560.59,36.186 560.59,34.131 C560.59,32.076 558.924,30.41 556.869,30.41 M541,60.657 C535.114,60.657 530.342,55.887 530.342,50 C530.342,44.114 535.114,39.342 541,39.342 C546.887,39.342 551.658,44.114 551.658,50 C551.658,55.887 546.887,60.657 541,60.657 M541,33.886 C532.1,33.886 524.886,41.1 524.886,50 C524.886,58.899 532.1,66.113 541,66.113 C549.9,66.113 557.115,58.899 557.115,50 C557.115,41.1 549.9,33.886 541,33.886 M565.378,62.101 C565.244,65.022 564.756,66.606 564.346,67.663 C563.803,69.06 563.154,70.057 562.106,71.106 C561.058,72.155 560.06,72.803 558.662,73.347 C557.607,73.757 556.021,74.244 553.102,74.378 C549.944,74.521 548.997,74.552 541,74.552 C533.003,74.552 532.056,74.521 528.898,74.378 C525.979,74.244 524.393,73.757 523.338,73.347 C521.94,72.803 520.942,72.155 519.894,71.106 C518.846,70.057 518.197,69.06 517.654,67.663 C517.244,66.606 516.755,65.022 516.623,62.101 C516.479,58.943 516.448,57.996 516.448,50 C516.448,42.003 516.479,41.056 516.623,37.899 C516.755,34.978 517.244,33.391 517.654,32.338 C518.197,30.938 518.846,29.942 519.894,28.894 C520.942,27.846 521.94,27.196 523.338,26.654 C524.393,26.244 525.979,25.756 528.898,25.623 C532.057,25.479 533.004,25.448 541,25.448 C548.997,25.448 549.943,25.479 553.102,25.623 C556.021,25.756 557.607,26.244 558.662,26.654 C560.06,27.196 561.058,27.846 562.106,28.894 C563.154,29.942 563.803,30.938 564.346,32.338 C564.756,33.391 565.244,34.978 565.378,37.899 C565.522,41.056 565.552,42.003 565.552,50 C565.552,57.996 565.522,58.943 565.378,62.101 M570.82,37.631 C570.674,34.438 570.167,32.258 569.425,30.349 C568.659,28.377 567.633,26.702 565.965,25.035 C564.297,23.368 562.623,22.342 560.652,21.575 C558.743,20.834 556.562,20.326 553.369,20.18 C550.169,20.033 549.148,20 541,20 C532.853,20 531.831,20.033 528.631,20.18 C525.438,20.326 523.257,20.834 521.349,21.575 C519.376,22.342 517.703,23.368 516.035,25.035 C514.368,26.702 513.342,28.377 512.574,30.349 C511.834,32.258 511.326,34.438 511.181,37.631 C511.035,40.831 511,41.851 511,50 C511,58.147 511.035,59.17 511.181,62.369 C511.326,65.562 511.834,67.743 512.574,69.651 C513.342,71.625 514.368,73.296 516.035,74.965 C517.703,76.634 519.376,77.658 521.349,78.425 C523.257,79.167 525.438,79.673 528.631,79.82 C531.831,79.965 532.853,80.001 541,80.001 C549.148,80.001 550.169,79.965 553.369,79.82 C556.562,79.673 558.743,79.167 560.652,78.425 C562.623,77.658 564.297,76.634 565.965,74.965 C567.633,73.296 568.659,71.625 569.425,69.651 C570.167,67.743 570.674,65.562 570.82,62.369 C570.966,59.17 571,58.147 571,50 C571,41.851 570.966,40.831 570.82,37.631\"> </path></g></g></g></svg><a href=\"https://www.instagram.com/p/CRmW85dLG4Y/?utm_source=ig_embed&amp;utm_campaign=loading\" target=\"_blank\">Ver esta publicaci&oacute;n en Instagram</a>     <a href=\"https://www.instagram.com/p/CRmW85dLG4Y/?utm_source=ig_embed&amp;utm_campaign=loading\" target=\"_blank\">Una publicaci&oacute;n compartida por Happiest (@gohappiest)</a></blockquote>\r\n" + 
				"<script src=\"//platform.instagram.com/en_US/embeds.js\" async=\"\"> </script>" + 
				"hola";
		*/
		
		/*
		// image1RL.xml
		String fieldContent =  
		"<p class=\"image-align-center\"><img alt=\"\" height=\"400\" src=\"/u/fotografias/m/2020/11/2/f768x400-1372_45275_5050.jpg\" width=\"768\" /></p>\r\n";
		*/
		
		// image2RL.xml
		String fieldContent = "<div class=\"image-align-center\">\r\n" + 
				"<figure class=\"image\"><img alt=\"ibrahimovic\" height=\"377\" src=\"/__export/1631648163573/sites/gadgets/img/2021/09/14/big-bang-theory_x1x.jpg_186469708.jpg\" width=\"688\" />\r\n" + 
				"<figcaption>Zlatan admired Messi.</figcaption>\r\n" + 
				"</figure>\r\n" + 
				"</div>";
		
		//String findPattern = "<b>([^<]*)</b>";
		//String replacePattern = "$1";
		
		replacer.replaceAll(fieldContent);
		
		//replacer.replaceAll(fieldContent,findPattern, replacePattern);
	}

	public void setFolderDate(Date folderDate) {
		this.folderDate = folderDate;
	}
	
	public String getReport() {
		return report;
	}

	public FieldRegExpReplacer getClone() {
		FieldRegExpReplacer clone = new FieldRegExpReplacer();
		clone.defaultImageSection = this.defaultImageSection;
		clone.imageImportBaseDomain = this.imageImportBaseDomain;
		clone.ruleName = this.ruleName;
		clone.cms = this.cms;
		clone.publication = this.publication;
		clone.site = this.site;
		clone.folderDate = this.folderDate;
		clone.request = this.request;
		clone.replacePattern = this.replacePattern;
		clone.findPattern = this.findPattern;
		return null;
	}
}
