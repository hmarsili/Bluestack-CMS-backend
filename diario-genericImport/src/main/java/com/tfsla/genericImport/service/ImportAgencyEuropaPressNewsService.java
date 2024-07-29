package com.tfsla.genericImport.service;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.OpenCms;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.tfsla.genericImport.exception.ImportException;
import com.tfsla.genericImport.model.A_ImportServiceWires;

public class ImportAgencyEuropaPressNewsService extends A_ImportServiceWires{
	private String rootSite = null;
	
	public ImportAgencyEuropaPressNewsService(CmsObject cms,String rootSite ,String importDefinitionPath) throws CmsException{	
		super(cms, importDefinitionPath, true);
		this.rootSite = rootSite;

		importName = "EuropaPress";
		contentType = "XML";
		basePath = "/europapress/";
		campoNombreArchivo = "Nombre Archivo";
	}
	
	public void getXmlContent() throws  ImportException, ParserConfigurationException {
       
        BASE_FOLDER = getUploadPath();
        campoNombreArchivo = getFolderPath();
        destinationPath = getDestinationFolderPath();
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        		
        try {            
			builder = factory.newDocumentBuilder(); 
			
            if(campoNombreArchivo != null){
            	if(BASE_FOLDER.endsWith("/") )
              	  pathFilesContent = BASE_FOLDER + campoNombreArchivo;
              	else
              	  pathFilesContent = BASE_FOLDER + S + campoNombreArchivo;
            }
            
            XPathFactory xpathFactory = XPathFactory.newInstance(); 
            XPath xpath = xpathFactory.newXPath();
            
            List<String> articles = getListOfArticles(pathFilesContent);
            
            writeToLog("... Site de importación: " + rootSite);
            
            for(String item : articles){
            	Document document = null;            	
            	String  subFolder = null;       
            	
            	String itemPath = null;
            	
            	if(BASE_FOLDER.endsWith("/") )
            		itemPath = BASE_FOLDER + campoNombreArchivo + S + item;
                else
                	itemPath = BASE_FOLDER + S + campoNombreArchivo + S + item;
            	
            	File file = new File(itemPath);
            	
            	currentFileName = "";
				try {
					document = builder.parse(file);   
	        			
					String format = getFormat(document,xpath);
					
					subFolder = createSubFoldersByDate(document,xpath,format);     
					String resourceFolder = basePath + subFolder;
					
					createFolders(basePath, subFolder);						
					currentFileName = generateResourceName(resourceFolder);			
					
					CmsFile newFile = createResource(currentFileName);
					cms.writePropertyObject(currentFileName, new CmsProperty("oldRecordId",null,"" + item));
					cms.writePropertyObject(currentFileName, new CmsProperty("newsType",null,"agency"));
					cms.writePropertyObject(currentFileName, new CmsProperty("Agency",null,"EuropaPress"));
					
					CmsXmlContent newXmlContent = null;
					StringBuilder content = new StringBuilder();
					
					newXmlContent = CmsXmlContentFactory.unmarshal(cms, newFile);					
					mapCmsContentValues(newXmlContent, content, "", item, document, xpath, null,format);						
					newFile.setContents(newXmlContent.marshal());
										
					cms.writeFile(newFile);					
					cms.unlockResource(currentFileName);				
					
					moveFileAfterProcess(item);
					
					writeToLog("... Fin importacion recurso (" + item +"): " + currentFileName);
					
				}  catch (CmsIllegalArgumentException e) {
					LOG.error(this.getClass().getName() + " || Error ",e);
					writeToLog("... Error en importacion recurso (" + item +"): " + currentFileName);
					writeToLog("causa: " + e.getMessage());
				} catch (CmsException e) {
					LOG.error(this.getClass().getName() + " || Error ",e);
					writeToLog("... Error en importacion recurso (" + item +"): " + currentFileName);
					writeToLog("causa: " + e.getMessage());
				}  catch (SAXException e) {
					LOG.error("No puede parsear el archivo: " + item + " - Se elimina de la carpeta",e);
					moveFileAfterProcess(item);
				} 
            }
        } catch (IOException e) {
        	writeToLog("... Error en importacion recurso ");
			writeToLog("causa: " + e.getMessage());
        }finally {
			if (objWriter!=null) {
				try {
					objWriter.flush();
				} catch (IOException e) {
					writeToLog("... Error en importacion recurso ");
					writeToLog("causa: " + e.getMessage());
				}
			}

		}       
 
    }
	

	private void mapCmsContentValues(CmsXmlContent content, StringBuilder resContent, String path, String item, Document doc, XPath xpath, String valorEntrega, String format) throws ImportException {
		
		String elementoDestino = "/NOTICIA/TITULAR/text()";
		
		if(format.equals("NewsML"))
			elementoDestino = "/NewsML/NewsItem/NewsComponent/NewsComponent/NewsLines/HeadLine/text()";
		
		String element = getValueForItem(elementoDestino, doc, xpath);
		content.getValue("titulo", contentLocale).setStringValue(cms, element);	
		
		elementoDestino = "/NOTICIA/ANTETITULO/text()";
		
		if(format.equals("NewsML"))
			elementoDestino = "/NewsML/NewsItem/NewsComponent/NewsComponent/NewsLines/Slugline/text()";
		
		element = getValueForItem(elementoDestino, doc, xpath);
		content.getValue("volanta", contentLocale).setStringValue(cms, element);
		
		if(format.equals("datacast")){
			elementoDestino = "/NOTICIA/ENTRADILLA/text()";	
			element = getValueForItem(elementoDestino, doc, xpath);
			content.getValue("copete", contentLocale).setStringValue(cms, element);
		}
		
		elementoDestino = "/NOTICIA/CONTENIDO/text()";
		element = getValueForItem(elementoDestino, doc, xpath);
		
		if(format.equals("NewsML")){
			elementoDestino = "/NewsML/NewsItem/NewsComponent/NewsComponent/ContentItem/DataContent/text()";
			element = getValueForItem(elementoDestino, doc, xpath);
			
			if(element!= null && !element.equals("")){
			    String body = element.substring(element.indexOf("<body>") + 6, element.indexOf("</body"));
			    element = body;
			}
		}
		
		content.getValue("cuerpo", contentLocale).setStringValue(cms, element);		
		
		//fecha de ultima modificacion
		if (this.lastModificationDate != 0)	
			content.getValue("ultimaModificacion", contentLocale).setStringValue(cms, this.lastModificationDate.toString());
				
	}
	
	

	protected CmsFile createResource(String resourceName) throws CmsIllegalArgumentException, CmsException{
		LOG.debug(this.getClass().getName() + " || creating resource type '" + contentType + "': " + resourceName);
		writeToLog("Creando tipo de recurso '" + contentType + "': " + resourceName);
		writeToLog("RootSite " + rootSite + " Context site:"+cms.getRequestContext().getSiteRoot()+" Context uri: "+cms.getRequestContext().getUri());

		if(cms.getRequestContext().getSiteRoot().equals("") || cms.getRequestContext().getSiteRoot().equals("/"))
			cms.getRequestContext().setSiteRoot(rootSite);
		
		int typeResource = OpenCms.getResourceManager().getResourceType("noticia").getTypeId();
		CmsResource res = cms.createResource(resourceName,typeResource);
		return cms.readFile(res);
	}

	protected void createFolders(String baseFolder, String subFolder) throws CmsIllegalArgumentException, CmsException {
		
		String[] subFolders = subFolder.split("/");
		String folderName = baseFolder;
		
		if(cms.getRequestContext().getSiteRoot().equals("") || cms.getRequestContext().getSiteRoot().equals("/"))
			cms.getRequestContext().setSiteRoot(rootSite);
		
		if (!cms.existsResource(folderName)) {
			writeToLog("Creando carpeta " + folderName);
			writeToLog("RootSite " + rootSite + " Context site:"+cms.getRequestContext().getSiteRoot()+" Context uri: "+cms.getRequestContext().getUri());

			cms.createResource(folderName, CmsResourceTypeFolder.RESOURCE_TYPE_ID);
			try {
				OpenCms.getPublishManager().publishResource(cms, folderName); 
			} catch (Exception e) {
				writeToLog("Error al publicar la carpeta: " + folderName);
			}
		}
		
		int f = 0;
		String yearFolder = "";
		
		for (String subpath : subFolders){
			if(!subpath.isEmpty()){
				folderName += subpath + "/"; 
				if (!cms.existsResource(folderName)) {
					writeToLog("Creando carpeta " + folderName);
					writeToLog("RootSite " + rootSite + " Context site:"+cms.getRequestContext().getSiteRoot()+" Context uri: "+cms.getRequestContext().getUri());

					cms.createResource(folderName, CmsResourceTypeFolder.RESOURCE_TYPE_ID);
					cms.unlockResource(folderName);
					
					f++;
					if(f==1)
						yearFolder = folderName;
				}
			}
		} 
		
		if(!yearFolder.equals("")){
			try {
				OpenCms.getPublishManager().publishResource(cms, yearFolder); 
			} catch (Exception e) {
				writeToLog("Error al publicar la carpeta: " + yearFolder);
			}
		}
		
	}	
	
	private String getFormat(Document doc, XPath xpath){
		
		String format = "datacast";
		
		try {
			
			XPathExpression exprNewsML = xpath.compile("/NewsML/@Duid");
			String newsMLV = (String) exprNewsML.evaluate(doc, XPathConstants.STRING);
			
			if(newsMLV!=null && !newsMLV.trim().equals(""))
				format = "NewsML";
			
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
				
		return format;
		
	}
	
	protected String createSubFoldersByDate(Document doc, XPath xpath, String format) {	
    	Object valueFinal = null;
		try {
            XPathExpression expr = null;
            
            if(format.equals("datacast"))
              expr = xpath.compile("/NOTICIA/CODIGO/text()");
            
            if(format.equals("NewsML"))
              expr = xpath.compile("/NewsML/NewsEnvelope/DateAndTime/text()");
            
            String campoFecha = (String) expr.evaluate(doc, XPathConstants.STRING);
            Date date = null;
            
            try {
            	if (campoFecha != null  && !campoFecha.equals("")) {
            	   try {   
		            	SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		            	
		            	if(format.equals("datacast"))
		            		simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");  
		            	
		                date = simpleDateFormat.parse(campoFecha);
		            }catch (Exception ex) {
		            	date = new Date();
		            	LOG.info ("WIRES - Se genera error de parseo de fecha se usa la actual");
		            }
            	}else {
            		date = new Date();
            	}
                //para almacenar luego
                this.lastModificationDate = date.getTime();

                
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
                String today = formatter.format(date);
                valueFinal = today;
            } catch (Exception ex) {
                LOG.error(ex);
            }
            
        } catch (XPathExpressionException e) {
        	LOG.error("WIRES - Error de parseo de fechas: " , e);
        }	
		
		return valueFinal.toString();				
	}
	
	private String getFolderPath() {
		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		return config.getParam("", "", "importAgency", "importAgencyEuropaPress","");
	}
	

    
}
