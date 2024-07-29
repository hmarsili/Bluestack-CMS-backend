package com.tfsla.homeCreator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.main.CmsException;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import java.io.ByteArrayInputStream; 
import java.io.IOException;
import java.io.InputStream; 
import java.io.UnsupportedEncodingException;
import java.util.Locale;

/**
 * Actualiza la prioridad de la noticia en la home online tomando lo enviado por un xml.
 * @author Victor Podberezski (vpod@tfsla.com)
 *
 */
public class HomeNewsdesigner {

	private static final String HOME_ORDER = "prioridadhome";
	
	private CmsObject cms;
	
	public HomeNewsdesigner(CmsObject cms)
	{
		this.cms = cms;
	}
	
	public void order(String xml) throws DOMException, CmsException
	{
	    try {
			InputStream is = new ByteArrayInputStream(xml.getBytes("ISO-8859-1"));
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

			DocumentBuilder db = dbf.newDocumentBuilder();
	        Document dom = db.parse(is);

	        Element docEle = dom.getDocumentElement();  


	        NodeList items = docEle.getElementsByTagName("item");  
	        if (items != null && items.getLength() > 0) {  
	        	for (int i = 0; i < items.getLength(); i++) {  
	        		Element noticia = (Element) items.item(i);  
	        		Element path = (Element) noticia.getElementsByTagName("path").item(0);

	        		Element order = (Element) noticia.getElementsByTagName("order").item(0);
	        		
	        		changeNewsOrder(path.getFirstChild().getNodeValue(),order.getFirstChild().getNodeValue());
	        		

	        	}  

	        }  



	    } catch (ParserConfigurationException e) {
			e.printStackTrace();
	    } 
	    catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void changeNewsOrder(String path, String order) throws CmsException {
		
			cms.lockResource(path);
			
			CmsFile file = cms.readFile(path);

			CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, file);
			
			content.setAutoCorrectionEnabled(true);
			content.correctXmlStructure(cms);

			content.getValue(HOME_ORDER, Locale.ENGLISH).setStringValue(cms, order);
			file.setContents(content.marshal());
			cms.writeFile(file);

			cms.writePropertyObject(path, new CmsProperty("home.priority", order,null));

			cms.unlockResource(path);

		
	}
	
	
	public void changeNewsProperties(String xml) throws CmsException, ParserConfigurationException, SAXException, IOException
	{
			InputStream is = new ByteArrayInputStream(xml.getBytes("ISO-8859-1"));
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

			DocumentBuilder db = dbf.newDocumentBuilder();
	        Document dom = db.parse(is);

	        Element docEle = dom.getDocumentElement();  


	        NodeList items = docEle.getElementsByTagName("item");  
	        if (items != null && items.getLength() > 0) {  
	        	for (int i = 0; i < items.getLength(); i++) {  
	        		Element noticia = (Element) items.item(i);  

	        		Element path = (Element) noticia.getElementsByTagName("path").item(0);

	        		String pathNews = path.getFirstChild().getNodeValue();
	    			cms.lockResource(pathNews);	    			
	    			CmsFile file = cms.readFile(pathNews);

	    			CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, file);

	    			content.setAutoCorrectionEnabled(true);
	    			content.correctXmlStructure(cms);

	        		NodeList props = noticia.getChildNodes();
	        		for (int j= 1; j<props.getLength();j++)
	        		{
	        			Element prop = (Element) props.item(j);
	        			String propName = prop.getNodeName();
	        			String propNewValue = prop.getFirstChild().getNodeValue();

	        			content.getValue(propName, Locale.ENGLISH).setStringValue(cms, propNewValue);

	        			
	        		}
        			file.setContents(content.marshal());
        			cms.writeFile(file);


        			cms.unlockResource(pathNews);
	        		
	        		

	        	}  

	        }  



	}

}
