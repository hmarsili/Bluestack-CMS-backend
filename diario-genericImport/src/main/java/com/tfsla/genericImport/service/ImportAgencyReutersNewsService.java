package com.tfsla.genericImport.service;

import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.axiom.util.namespace.MapBasedNamespaceContext;
import org.jsoup.Jsoup;
import org.jsoup.parser.Parser;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.tfsla.genericImport.exception.ImportException;
import com.tfsla.genericImport.model.A_ImportServiceWires;

public class ImportAgencyReutersNewsService extends A_ImportServiceWires {
	
	public ImportAgencyReutersNewsService(CmsObject cms, String importDefinitionPath) throws CmsException{	
		super(cms, importDefinitionPath, true);
	
		importName = "REUTERS";
		contentType = "XML";
		basePath = "/reuters/";
		campoNombreArchivo = "Nombre Archivo";
	}
	

	
	public void getXmlContent() throws  ImportException, ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        BASE_FOLDER = getUploadPath();
        campoNombreArchivo = getFolderPath();
        destinationPath = getDestinationFolderPath();
        DocumentBuilder builder = null;
        		
        try {            
			builder = factory.newDocumentBuilder();            
            if(campoNombreArchivo != null){
            	pathFilesContent = BASE_FOLDER + S + campoNombreArchivo;
            }
            
            XPathFactory xpathFactory = XPathFactory.newInstance(); 
            XPath xpath = xpathFactory.newXPath();
            
            List<String> articles = getListOfArticles(pathFilesContent);
            
            for(String item : articles){
            	Document document = null;            	
            	String subFolder = null;            	
        		currentFileName = "";
				try {
					document = builder.parse(BASE_FOLDER + campoNombreArchivo + S + item);   
	        		
					subFolder = createSubFoldersByDate(document, item, xpath);     
					String resourceFolder = basePath + subFolder;
					
					createFolders(basePath, subFolder);						
					currentFileName = generateResourceName(resourceFolder);			
					
					CmsFile newFile = createResource(currentFileName);
					cms.writePropertyObject(currentFileName, new CmsProperty("oldRecordId",null,"" + item));
					cms.writePropertyObject(currentFileName, new CmsProperty("newsType",null,"agency"));
					cms.writePropertyObject(currentFileName, new CmsProperty("Agency",null,"Reuters"));
					
					CmsXmlContent newXmlContent = null;
					StringBuilder content = new StringBuilder();
					
					newXmlContent = CmsXmlContentFactory.unmarshal(cms, newFile);					
					mapCmsContentValues(newXmlContent, content, "", item, document, xpath, null);						
					newFile.setContents(newXmlContent.marshal());
										
					cms.writeFile(newFile);					
					cms.unlockResource(currentFileName);				
					
					moveFileAfterProcess(item);
					
					writeToLog("... Fin importacion recurso (" + item +"): " + currentFileName);
					
				}  catch (CmsIllegalArgumentException e) {
					if (!currentFileName.equals(""))
						//deleteResources(currentFileName, "" + item);
					LOG.error(this.getClass().getName() + " || Error ",e);
					writeToLog("... Error en importacion recurso (" + item +"): " + currentFileName);
					writeToLog("causa: " + e.getMessage());
				} catch (CmsException e) {
					if (!currentFileName.equals(""))
						//deleteResources(currentFileName, "" + item);
					LOG.error(this.getClass().getName() + " || Error ",e);
					writeToLog("... Error en importacion recurso (" + item +"): " + currentFileName);
					writeToLog("causa: " + e.getMessage());
				} catch (SAXException e) {
					LOG.error("No puede parsear el archivo: " + item + " - Se elimina de la carpeta",e);
					moveFileAfterProcess(item);
				}
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
			if (objWriter!=null) {
				try {
					objWriter.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}       
 
    }

	
	private void mapCmsContentValues(CmsXmlContent content, StringBuilder resContent, String path, String item, Document doc, XPath xpath, String valorEntrega) throws ImportException {
		
		String elementoDestino = "/iptc:newsMessage/iptc:itemSet/iptc:newsItem/iptc:contentMeta/iptc:headline/text()";
		String element = getValueForItem(elementoDestino, doc, xpath);
		content.getValue("titulo", contentLocale).setStringValue(cms, element);	
		
		elementoDestino = "/iptc:newsMessage/iptc:itemSet/iptc:newsItem/iptc:contentMeta/iptc:description/text()";
		element = getValueForItem(elementoDestino, doc, xpath);
		content.getValue("copete", contentLocale).setStringValue(cms, element);
		
		elementoDestino = "/iptc:newsMessage/iptc:itemSet/iptc:newsItem/iptc:contentSet/iptc:inlineXML/node()";
		element = getValueForItemByNodes(elementoDestino, doc, xpath);
		content.getValue("cuerpo", contentLocale).setStringValue(cms, element);		
		
		//fecha de ultima modificacion
		if (this.lastModificationDate != 0)	
			content.getValue("ultimaModificacion", contentLocale).setStringValue(cms, this.lastModificationDate.toString());
				
	}
	
	private String getValueForItemByNodes(String path, Document doc, XPath xpath) {
		
		String campo = "";
		
		try {			
			
            XPathExpression expr =  xpath.compile(path);
            String campoBuscado = ""; 
            
            NodeList nodes = (NodeList) expr.evaluate( doc,XPathConstants.NODESET);
            
            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                StreamResult xmlOutput = new StreamResult(new StringWriter());
                
                Transformer transformer;
				try {
					transformer = TransformerFactory.newInstance().newTransformer();
					transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
					transformer.transform(new DOMSource(node), xmlOutput);
					
	                String nodeAsAString = xmlOutput.getWriter().toString();
	                campoBuscado += nodeAsAString;
	                
				} catch (TransformerConfigurationException e) {
					writeToLog("Error obteniendo el contenido del xml. "+e.getCause());
				} catch (TransformerFactoryConfigurationError e) {
					writeToLog("Error obteniendo el contenido del xml. "+e.getCause());
				} catch (TransformerException e) {
					writeToLog("Error obteniendo el contenido del xml. "+e.getCause());
				}
            }
            
            org.jsoup.nodes.Document saneHtml = Jsoup.parse(campoBuscado,"",Parser.xmlParser());
            campo = saneHtml.body().html();

        } catch (XPathExpressionException e) {
        	writeToLog("Error obteniendo el contenido del xml. "+e.getCause());
        }
		
		return campo;
	}
	
	

protected String createSubFoldersByDate(Document doc, String item, XPath xpath) {	
    	Object valueFinal = null;
		try {
            
            HashMap<String, String> prefMap = new HashMap<String, String>() {{
                put("iptc", "http://iptc.org/std/nar/2006-10-01/");
            }};
            MapBasedNamespaceContext nsContext = new MapBasedNamespaceContext(prefMap);
            xpath.setNamespaceContext(nsContext);
            XPathExpression expr = xpath.compile("/iptc:newsMessage/iptc:itemSet/iptc:newsItem/iptc:itemMeta/iptc:versionCreated/text()");//Xpath para leer fecha
            String campoFecha = (String) expr.evaluate(doc, XPathConstants.STRING);
            Date date = null;
            
            try {
            	if (campoFecha != null  && !campoFecha.equals("")) {
	      	    	try { 
	            		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-mm-dd'T'hh:mm:ss");
		                date = simpleDateFormat.parse(campoFecha);
	      	    	} catch (Exception ex) {
	      	    		date = new Date();
	      	    	}
	      	    } else {
            		date = new Date();
            	}
                //para almacenar luego
                this.setLastModificationDate(date.getTime());
           
                
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
                String today = formatter.format(date);
                valueFinal = today;
            }
            catch (Exception ex)
            {
                LOG.error("WIRES - Al buscar la fecha de la nota",ex);
            }
            //valueFinal = date; 
        } catch (XPathExpressionException e) {
            LOG.error("Error al buscar la fecha: " ,e);
        }	
		
		return valueFinal.toString();				
	}
	
	private String getFolderPath() {
		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		return config.getParam("", "", "importAgency", "importAgencyReuters","");
	}

	
}
