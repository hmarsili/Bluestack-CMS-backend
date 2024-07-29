package com.tfsla.genericImport.service;

import java.io.Writer;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.content.CmsXmlContentValueSequence;
import org.opencms.xml.types.I_CmsXmlContentValue;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.tfsla.genericImport.exception.DataTransformartionException;
import com.tfsla.genericImport.exception.ImportException;
import com.tfsla.genericImport.model.A_ImportService;

public class ImportXMLService extends A_ImportService{
	
	private static Writer objWriter = null;
	private String importName;
	private CmsObject cms;
	private static String BASE_FOLDER = "/var/lib/tomcat7/webapps/ROOT/importFiles";
	private String S = "/";

	private CmsFile importDefinitionFile;
	private CmsXmlContent importDefinitionContent;
	private Locale contentLocale;

	private String contentType;
	private String campoIdRecord;
	private String basePath;
	private String preffixName;
	private boolean isXmlContent;
	private String currentFileName;
	private String executeFileName;
	private String campoNombreArchivo;
	private String campoNombreCarpeta;
	private String campoDocImportacion;
	boolean isFotogaleria = false;
	boolean importFromFolder = false;
	
	public String getOffset() {
		return offset;
	}

	public void setOffset(String offset) {
		this.offset = offset;
	}

	private String offset;
	
	public ImportXMLService(CmsObject cms, String importDefinitionPath) throws CmsException{	
		super(cms, importDefinitionPath);
		this.importDefinitionPath = importDefinitionPath;
		this.cms = cms;
		
		importDefinitionFile = cms.readFile(cms.getRequestContext().removeSiteRoot(importDefinitionPath));		
		importDefinitionContent = CmsXmlContentFactory.unmarshal(cms, importDefinitionFile);
		
		if (getLocale()==null)
	    	setLocale(cms.getRequestContext().getLocale());
		
		if(contentLocale == null)
			contentLocale = cms.getRequestContext().getLocale();

		importName = importDefinitionContent.getStringValue(cms, "Nombre", contentLocale);
		contentType = importDefinitionContent.getStringValue(cms, "TipoContenido", contentLocale);
		basePath = importDefinitionContent.getStringValue(cms, "BasePath", contentLocale);
		preffixName = importDefinitionContent.getStringValue(cms, "PreffixName", contentLocale);
		
		if(preffixName==null || (preffixName!=null && preffixName.equals("")))
			preffixName = contentType; 
		
		campoNombreArchivo = importDefinitionContent.getStringValue(cms, "NombreArchivo", contentLocale);
		campoNombreCarpeta = importDefinitionContent.getStringValue(cms, "NombreCarpeta", contentLocale);
		campoDocImportacion = importDefinitionContent.getStringValue(cms, "DocumentoImportacion", contentLocale);

		ContentTypeService cTypeService = new ContentTypeService();
		isXmlContent = cTypeService.isXmlContentType(contentType);
	}
	
	public void decompress(String compressedFile,String destination) {
		 try {
		     ZipFile zipFile = new ZipFile(compressedFile);		     
		     zipFile.extractAll(destination);
		 } catch (ZipException e) {
		     e.printStackTrace();
		     writeToLog("... Error en descompresion del archivo "+compressedFile);
		 }
		 
		 //System.out.println("File Decompressed");
		}

	
	public void getXmlContent() throws SAXException, ImportException, ParserConfigurationException {
		
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        BASE_FOLDER = getUploadPath();
        DocumentBuilder builder = null;
        Document doc = null;
        ArrayList<String> i = new ArrayList<String>();
        
        ContentTypeService cTypeService = new ContentTypeService();
        
		try {
			isXmlContent = cTypeService.isXmlContentType(contentType);
		} catch (CmsLoaderException e1) {
			e1.printStackTrace();
		}
		
        try {            
			builder = factory.newDocumentBuilder();  
			
			List<String> articles = null;
			
			XPathFactory xpathFactory = XPathFactory.newInstance(); 
            XPath xpath = xpathFactory.newXPath();
			
			if(campoDocImportacion!=null && !campoDocImportacion.equals(""))
			{
	            if(campoNombreArchivo != null && !campoNombreArchivo.equals("")){
	            	doc = builder.parse(BASE_FOLDER + S + campoNombreArchivo + S + campoDocImportacion);		
	            }else
	            	doc = builder.parse(BASE_FOLDER + S + campoDocImportacion);	
	     
	            articles = getListOfArticles(doc, xpath);   
			}else{
				
				 importFromFolder = true;
				
				 String pathSource = "";
				 
				 if(campoNombreArchivo != null && !campoNombreArchivo.equals("")){
					pathSource = BASE_FOLDER +"process"+ S + campoNombreArchivo;		
		         }else
		            pathSource = BASE_FOLDER +"process";	
				
				 articles = getListOfArticlesbyFolder(pathSource);
				
			}
            
            for(String item : articles){
            	Document document = null;
            	String filtroBusquedaElemento = importDefinitionContent.getStringValue(cms, "FiltroBusquedaElemento", contentLocale);
            	String subFolder = null;
            	
            	String baseFolder = "";

        		if(importFromFolder)
        			baseFolder = BASE_FOLDER + "process/";
        		else
        			baseFolder = BASE_FOLDER;
            	
            	if(filtroBusquedaElemento !=null && filtroBusquedaElemento != ""){
            		String itemUbication = getItemUbication(doc, xpath, item, filtroBusquedaElemento);
            		String  documentPath = null;
            		
	            	if(campoNombreArchivo!=null && !campoNombreArchivo.equals("") )
	            			documentPath = baseFolder + campoNombreArchivo + S + itemUbication;
	            	else
	            			documentPath = baseFolder  + itemUbication;
	            	
	            	try{	
	            		document = builder.parse(documentPath);
            		}catch (SAXParseException e){
            			writeToLog("... No se puede leer el archivo "+documentPath);
    				}
            		
            		subFolder = generateSubFolder(document, item, xpath);
            	}else{
            		
            		String documentPath = null;
            		
            		if(campoNombreArchivo!=null && !campoNombreArchivo.equals(""))
            			documentPath = baseFolder  + campoNombreArchivo + S + item;
            		else
            			documentPath = baseFolder  + item;
            		
            		try{	
	            		document = builder.parse(documentPath);
            		}catch (SAXParseException e){
            			writeToLog("... No se puede leer el archivo "+documentPath);
    				}
            		
            		subFolder = generateSubFolder(document, item, xpath);
            	}
            	
				String resourceFolder = basePath + subFolder;
				currentFileName = "";
				try {
					createFolders(basePath, subFolder);				
					if(document == null){
						currentFileName = generateResourceName(resourceFolder, doc, item, xpath);
					}else{
						currentFileName = generateResourceName(resourceFolder, document, item, xpath);					
					}
					CmsFile newFile = createResource(currentFileName);
					cms.writePropertyObject(currentFileName, new CmsProperty("oldRecordId",null,"" + item));
					
					CmsXmlContent newXmlContent = null;
					StringBuilder content = new StringBuilder();
					
					if (isXmlContent)
						newXmlContent = CmsXmlContentFactory.unmarshal(cms, newFile);
					
					if(document == null){
						try{
							mapValues(newXmlContent, content, "", item, doc, xpath, null);
						}catch(ImportException e){
							writeToLog("... Error en el mapeo de "+item);
							writeToLog("... Causa: "+e.getMessage());
						}
					}else{
						try{
							mapValues(newXmlContent, content, "", item, document, xpath, null);	
						}catch(ImportException e){
							writeToLog("... Error en el mapeo de "+item);
							writeToLog("... Causa: "+e.getMessage());
						}
					}
					
					
					if (isXmlContent)	
						newFile.setContents(newXmlContent.marshal());
					else
						newFile.setContents(content.toString().getBytes());
					
					cms.writeFile(newFile);
					
					cms.unlockResource(currentFileName);
					writeToLog("... Fin importacion recurso (" + item +"): " + currentFileName);
					
					if(importFromFolder)
						moveFileAfterProcess(item);
					
				}  catch (CmsIllegalArgumentException e) {
					
					if (!currentFileName.equals(""))
						deleteResources(currentFileName, "" + item);
					
					LOG.error(this.getClass().getName() + " || Error ",e);
					writeToLog("... Error en importacion recurso (" + item +"): " + currentFileName);
					writeToLog("causa: " + e.getMessage());
				} catch (CmsException e) {
					if (!currentFileName.equals(""))
						deleteResources(currentFileName, "" + item);
					
					LOG.error(this.getClass().getName() + " || Error ",e);
					writeToLog("... Error en importacion recurso (" + item +"): " + currentFileName);
					writeToLog("causa: " + e.getMessage());
				}
            }
        } catch (IOException e) {
            e.printStackTrace();
            writeToLog("... Error no se pudo leer el archivo: " + campoNombreArchivo);
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
	
	private void deleteResources(String resourceName, String item) {		
		writeToLog("eliminando recurso " + resourceName + " identificado por " + item);
		try {
			cms.deleteResource(resourceName,CmsResource.DELETE_PRESERVE_SIBLINGS );
		} catch (CmsException e) {
			LOG.error(this.getClass().getName() + " || Error borrando recurso " + resourceName,e);
		}		
	}
	
	/**
     * Mapea todos los elementos de la galeria de imagenes buscando en subnodos
     * 
     * @param content Contenido estructurado de la noticia
     * @param doc  Documento de origen de la importacion
     * @param path xpath de los subnodos con los valores a importar
     * @param elementoDestino Campo de destino de la importacion
     * @param elemntMap Elemento de la definicion de importacion que tiene declarada la imagenGaleria
     */
	private void mapValuesImageGallery( CmsXmlContent content, Document doc, XPath xpath, String path, String elementoDestino, String elemntMap) throws ImportException {
		
		try {
			XPathExpression expr =  xpath.compile(path);
            
            NodeList nodes = (NodeList) expr.evaluate( doc,XPathConstants.NODESET);
            
            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                String campoBuscado = node.getTextContent();
                
                Object[] value = new Object[1];
                value[0] = campoBuscado;  
                
                Object valueFinal = null;
                
        		try {
        			valueFinal = transform(elemntMap, value);
        		}catch (DataTransformartionException e) {
        			writeToLog("Error aplicando transformacion en galerias " + e.getMessage());
        			throw e;
        		}

                String finalValueString = "";
                
                if(valueFinal!=null)
                	finalValueString = valueFinal.toString();
                
                int index = i + 1;
                String subelemento = elementoDestino.substring(("imagenesFotogaleria/").length());
                String destino = "imagenesFotogaleria["+index+"]/"+subelemento+"[1]";
                
                I_CmsXmlContentValue field = content.getValue(destino, contentLocale);
                
                if(field==null){
                	
                	content.addValue(cms,"imagenesFotogaleria", contentLocale, index-1);
                	content.getValue(destino, contentLocale).setStringValue(cms, finalValueString);
                }else{
                	
                	if(field.getStringValue(cms).equals("")){
	                	content.getValue(destino, contentLocale).setStringValue(cms, finalValueString);
                	}else{
	                	index = index + 1;
	                	destino = "imagenesFotogaleria["+index+"]/"+subelemento+"[1]";
	                	content.getValue(destino, contentLocale).setStringValue(cms, finalValueString);
                	}
                }
            }
             
        } catch (XPathExpressionException e) {
        	writeToLog("Error en el mapeo de la imagenGaleria "+e.getMessage());
            e.printStackTrace();
        }
	}

	private void mapValues(CmsXmlContent content, StringBuilder resContent, String path, String item, Document doc, XPath xpath, String valorEntrega) throws ImportException {
		CmsXmlContentValueSequence elementSequence = importDefinitionContent.getValueSequence(path + "Mapping", contentLocale);
		int elementCount = elementSequence.getElementCount();
		
        for (int j = 0; j < elementCount; j++) {
        	
        	isFotogaleria = false;
        	
        	String elementoDestino = importDefinitionContent.getStringValue(cms,path + "Mapping[" + (j+1) + "]/Elemento", contentLocale);
        	String propiedadDestino = importDefinitionContent.getStringValue(cms,path + "Mapping[" + (j+1) + "]/Property", contentLocale);
        	
    		CmsXmlContentValueSequence campoSequence = importDefinitionContent.getValueSequence(path + "Mapping[" + (j+1) + "]/XPath", contentLocale);
    		int campoCount = campoSequence.getElementCount();
    		String multiXpathValue = null;
        	String valorManual = importDefinitionContent.getStringValue(cms,path + "Mapping[" + (j+1) + "]/Valor", contentLocale); 
        	int valorManualCargado = (valorManual!=null ? 1 : 0); 
    		
        	Object[] value = new Object[campoCount + valorManualCargado];
    		if (valorManual!=null) {
    			value[0] = valorManual;    			
    		}
    		if (valorEntrega!=null && valorManual == null) {
    			value[0] = valorEntrega;    			
    		}
    		if(valorManual == null || valorManual == ""){
	    		for (int i = 0; i < campoCount; i++) {
	            	String campoColumn = importDefinitionContent.getStringValue(cms,path + "Mapping[" + (j+1) + "]/XPath[" + (i+1) + "]", contentLocale);
	            	if(campoColumn.contains("REPLACE")){
	            		campoColumn = campoColumn.replace("REPLACE", multiXpathValue);
	    			}
	            	if(campoColumn.contains("REPITEM")){
	            		campoColumn = campoColumn.replace("REPITEM", item);
	    			}
	            	value[0] = campoColumn;
	            	multiXpathValue= getValueForItem(value, doc, xpath);
	    		}
    		
    			value[0] = multiXpathValue;
    		}
    		Object valueFinal = null;
    		try {
    			if(value!=null && !value[0].equals(""))
    				valueFinal = transform(path + "Mapping[" + (j+1) + "]/", value);
    		}
    		catch (DataTransformartionException e) {
    			writeToLog("Error aplicando transformacion para " + (elementoDestino!=null ? " '" + elementoDestino + "' " : "")  + (propiedadDestino!=null ? " '" + propiedadDestino + "' " : "") + " :" + e.getMessage());
    			throw e;
    		}
    		
			boolean adjuntar = false;
			String separador = ", ";
        	String valorAdjuntar = importDefinitionContent.getStringValue(cms,path + "Mapping[" + (j+1) + "]/Adjuntar", contentLocale);
        	if (valorAdjuntar!=null) {
        		adjuntar = true;
        		separador = valorAdjuntar;
        	}
			
        	if (propiedadDestino!=null) {
	        	try {
	        		
	        		if (valueFinal!=null) {
		        		String finalValueString = ""; 
		        		if (adjuntar) {
		        			String previewsValue = "";
		        			try {
		        				CmsProperty property = cms.readPropertyObject(currentFileName,propiedadDestino,false);
		        				if (property!=null && property.getValue()!=null) {
		        					previewsValue = property.getValue();
		        				}
		        			} catch (CmsException e) {
		        				LOG.error(this.getClass().getName() + " || Error ",e);
		        				writeToLog("Error aplicando transformacion para " + (elementoDestino!=null ? " '" + elementoDestino + "' " : "")  + (propiedadDestino!=null ? " '" + propiedadDestino + "' " : "") + " :" + e.getMessage());
		        			}
		        			
		        			if (previewsValue!=null && previewsValue.trim().length()>0) {
		        				finalValueString = previewsValue + separador + valueFinal.toString();
		        			}
		        			else {
		        				finalValueString = valueFinal.toString();
		        			}
		        		}
		        		else
		        			finalValueString = valueFinal.toString();
		
		        		LOG.debug(this.getClass().getName() + " || poniendo valor '" + finalValueString + "' a propiedad " + propiedadDestino);
		        		cms.writePropertyObject(currentFileName, new CmsProperty(propiedadDestino,null,finalValueString));
	        		}
	        		else{
		        		LOG.debug(this.getClass().getName() + " || omitiendo valor en propiedad " + propiedadDestino + " por ser nulo.");
		        		writeToLog("... omitiendo valor en propiedad " + propiedadDestino + " por ser nulo.");
	        		}
				} catch (CmsException e) {
					writeToLog("Error aplicando transformacion para " + (elementoDestino!=null ? " '" + elementoDestino + "' " : "")  + (propiedadDestino!=null ? " '" + propiedadDestino + "' " : "") + " :" + e.getMessage());
					LOG.error(this.getClass().getName() + " || Error ",e);
				}
        	}
        	
        	String elementCeatedPreffix = "";
        	String elementoCrear = "";
        	if (isXmlContent && valueFinal!=null) {
	        	elementoCrear = importDefinitionContent.getStringValue(cms,path + "Mapping[" + (j+1) + "]/Crear", contentLocale);
	        	
	        	if (elementoCrear!=null) {
	        		if(elementoCrear.equals("imagenesFotogaleria"))
	        		{
	        			isFotogaleria = true;
	        			
	        		}else{
	        			String[] elementParts = elementoCrear.split("/");
		        		String elementPosition = "";
		        		int newIndex = 1;
		        		for (int i=0;i<elementParts.length;i++) {
		        			elementPosition += elementParts[i];
		        			if (i<elementParts.length-1) {
		        				elementPosition += "[" + content.getValueSequence(elementPosition, contentLocale).getElementCount() + "]/";
		        			}
		        			else {	            					
	        					newIndex = content.getValueSequence(elementPosition, contentLocale).getElementCount();	        					        				    				
		        			}		        			
		        		}
		        		
		        		if (content.getValueSequence(elementPosition, contentLocale).getMinOccurs()>0) {  
		        			try {
		        			if (newIndex > content.getValueSequence(elementPosition, contentLocale).getMinOccurs())
		        				content.addValue(cms,elementPosition,contentLocale,newIndex-1);
		        			}
		        			catch (CmsRuntimeException e) {
		        				throw new ImportException("Error al agregar nuevo elemento '" + e.getMessage());
		        			}
		        			catch (Exception e) {
		        				throw new ImportException("Error al agregar nuevo elemento '" + e.getMessage());
							}
		        		}
		        		else {
		        			try {
		        				content.addValue(cms,elementPosition,contentLocale,newIndex);
		        			}
		        			catch (CmsRuntimeException e) {
		        				throw new ImportException("Error al agregar nuevo elemento '" + e.getMessage());
		        			}
		        			catch (Exception e) {
		        				throw new ImportException("Error al agregar nuevo elemento '" + e.getMessage());
							}
	
		        			newIndex++;
		        		}
		        		elementCeatedPreffix = elementPosition + "[" + newIndex + "]";
	        		}
	        	}
        	}
        	
        	if (elementoDestino!=null) {
        		String elementPosition = "";

    			if (isXmlContent) { 	
    				if (elementoCrear!=null && !elementoCrear.equals("") && elementoDestino.startsWith(elementoCrear)) {
    					if(elementoCrear.equals("imagenesFotogaleria"))
    	        		{
    						isFotogaleria = true;
    						
    						String campoXpath = importDefinitionContent.getStringValue(cms,path + "Mapping[" + (j+1) + "]/XPath[1]", contentLocale);
    		            	
    						String elementMap = path + "Mapping[" + (j+1) + "]/";
    						mapValuesImageGallery(content,doc, xpath, campoXpath, elementoDestino, elementMap);

    	        		}else{
    	        			elementoDestino = elementoDestino.substring(elementoCrear.length());
	    					if (elementoDestino.startsWith("/"))
	    						elementoDestino = elementoDestino.substring(1);
	
	    					elementPosition = elementCeatedPreffix;
	    					if (elementoDestino.length()>0)
	    						elementPosition += "/";
    	        		}
    				}
    				if (!elementoDestino.trim().equals("")) {
		        		String[] elementParts = elementoDestino.split("/");
		        		for (int i=0;i<elementParts.length;i++) {
		        			elementPosition += elementParts[i]; 
		        			elementPosition += "[" + content.getValueSequence(elementPosition, contentLocale).getElementCount() + "]";
		        			if (i<elementParts.length-1) {
		        				elementPosition += "/";
		        			}
		        		}
    				}	        		
    			}
    			else {
    				elementPosition = "content";
    			}
    			
        		if (valueFinal!=null) {
        			if (!isXmlContent) 
        				resContent.append(valueFinal.toString());
        			else {
        			
        		        if(!isFotogaleria){		
			        		String finalValueString = ""; 
			        		if (adjuntar) {
			        			String previewsValue = content.getValue(elementPosition, contentLocale).getStringValue(cms);
			        			if (previewsValue!=null && previewsValue.trim().length()>0) {
			        				finalValueString = previewsValue + separador + stripNonValidXMLCharacters(valueFinal.toString());
			        			}
			        			else
			        				finalValueString = stripNonValidXMLCharacters(valueFinal.toString());
			        		}
			        		else
			        			finalValueString = stripNonValidXMLCharacters(valueFinal.toString());
			        		
			        		LOG.debug(this.getClass().getName() + " || poniendo valor '" + finalValueString + "' a " + elementPosition);
			        		content.getValue(elementPosition, contentLocale).setStringValue(cms, finalValueString);
        		        }
        			}
        		
        		}
        		else
	        		LOG.debug(this.getClass().getName() + " || omitiendo valor en " + elementPosition + " por ser nulo.");

        	}
        }
        getSubTable(content, resContent, path, item, doc, xpath);
		
	}
	
	
	private void getSubTable(CmsXmlContent newXmlContent, StringBuilder resContent, String path, String item, Document doc, XPath xpath) throws ImportException {
		
		CmsXmlContentValueSequence elementSequence = importDefinitionContent.getValueSequence(path + "SubTabla", contentLocale);
		
		if(elementSequence != null){
			
			int elementCount = elementSequence.getElementCount();
			String valorEntrega = null;
			boolean filtroEncontrado = false;
			
	        for (int j = 0; j < elementCount; j++) {
	        	
	    		String newPath = path + "SubTabla[" + (j+1) + "]/"; 
	    		CmsXmlContentValueSequence eSequence = importDefinitionContent.getValueSequence(newPath + "Filtro", contentLocale);
	    		int eCount = eSequence.getElementCount();	    		
	    		String valorComparar = null;
	    		
	    		for (int i = 0; i < eCount; i++) {
	    			Object[] value = new Object[1];
	    			String comparator = "";
	    			String xpathValue = null;
	    			comparator = getComparatorInFilter(newPath, i);
	    			String[] filterValues = new String[100];
	    			String multiXpathValue = null;
	    			
	    			CmsXmlContentValueSequence xpathSequence = importDefinitionContent.getValueSequence(newPath + "Mapping[" + (i+1) + "]/XPath", contentLocale);
	        		int xpathCount = xpathSequence.getElementCount();
	        		if(xpathCount > 1){
		        		for (int x = 0; x < xpathCount; x++) {
		                	String campoXpath = importDefinitionContent.getStringValue(cms,newPath + "Mapping[" + (i+1) + "]/XPath[" + (x+1) + "]", contentLocale);
		        			if(campoXpath.contains("replace")){
		        				campoXpath = campoXpath.replace("replace", multiXpathValue);
		        			}
		                	value[x] = campoXpath;
		                	multiXpathValue = getValueForItem(value, doc, xpath);
		        		}
		        		valorComparar = multiXpathValue;
	        		}
	    			xpathValue = importDefinitionContent.getStringValue(cms,newPath + "Filtro[" + (i+1) + "]/XPath", contentLocale);
	    			if(xpathValue != null){
	    			value[0] = xpathValue;
	    				valorComparar = getValueForItem(value, doc, xpath);
	    			}
	        					
	    			
	    			CmsXmlContentValueSequence valorSequence = importDefinitionContent.getValueSequence(newPath + "Filtro[" + (i+1) + "]/Valor", contentLocale);
	        		int valorCount = valorSequence.getElementCount();
	        		//Object[] val = new Object[valorCount];
	                if (valorCount==1) {
	                	for (int a = 0; a < eCount; a++) {
	                		filterValues[0] = importDefinitionContent.getStringValue(cms,newPath + "Filtro[" + (a+1) + "]/Valor[" + (a+1) + "]", contentLocale);
	                	}
	                	if(comparator.equals("Replace")){
	                		valorEntrega = filterValues[0];
	                	}
	                }	                
	                
	                CmsXmlContentValueSequence valSequence = importDefinitionContent.getValueSequence(newPath + "Filtro[" + (i+1) + "]/Valores", contentLocale);
	        		int vCount = valSequence.getElementCount();
	        		Object[] v = new Object[vCount];
	        		
	                if (vCount==1) {
	                	for (int h = 0; h < eCount; h++) {
	                		v[0] = importDefinitionContent.getStringValue(cms,newPath + "Filtro[" + (h+1) + "]/Valores[" + (h+1) + "]", contentLocale);
	                	}	
	                	String separador = getSeparatorInFilter(newPath, i);
	                	filterValues = ((String) v[0]).split(separador);
	                }
	                
	                if(filterValues.length > 0){
	                	for(String separatedvalue : filterValues){
	                		if(separatedvalue != null){
		                		if(getCompraratorValue(separatedvalue, valorComparar, comparator))
		                			valorEntrega = separatedvalue;
	                		}
	                	}
	                }
	                
	                if(valorEntrega == null || valorEntrega == ""){
	                	filtroEncontrado = false;
	                }else{
	                	filtroEncontrado = true;
	                }
	                
	    		}	
	    		
	    		if(filtroEncontrado){
	    			mapValues(newXmlContent, resContent, newPath, item, doc, xpath, valorEntrega);
	    			valorEntrega = null;
	    		}
	        }
		}
	}

	private boolean getCompraratorValue(String value,
			String valueCompare, String comparator) {
		if (comparator.equals("equal")) {
			if(value.equals(valueCompare))
				return true;
		}		
		else if (comparator.equals("like")) {
			if(value.contains(valueCompare))
				return true;
		}
		
		return false;
	}

	private String stripNonValidXMLCharacters(String in) {
		
		StringBuffer out = new StringBuffer();        
		char current;        
	    if (in == null || ("".equals(in))) return "";
	    
	    for (int i = 0; i < in.length(); i++) {
	          current = in.charAt(i); 
	          // NOTE: No IndexOutOfBoundsException caught here; it should not happen.
	          if (
	        		  (current == 0x9) ||
	        		  (current == 0xA) ||
	        		  (current == 0xD) ||
	        		  ((current >= 0x20) && (current <= 0xD7FF)) ||
	        		  ((current >= 0xE000) && (current <= 0xFFFD)) ||
	        		  ((current >= 0x10000) && (current <= 0x10FFFF)))
	        	  		out.append(current);
    	}
	    
	    return out.toString();	
	}
	
	private String getComparatorInFilter(String path, int j) {
		String comparador = importDefinitionContent.getStringValue(cms, path + "Filtro[" + (j+1) + "]/Comparador", contentLocale);
		return comparador;
		
	}
	
	private String getSeparatorInFilter(String path, int j) {
		String comparador = importDefinitionContent.getStringValue(cms, path + "Filtro[" + (j+1) + "]/Separador", contentLocale);
		return comparador;		
	}

	private String getValueForItem(Object[] value, Document doc, XPath xpath) {
		String campo = "";
		String path = "";
		
		//String valorBuscado = (String) value[0];
		for(Object v : value){
			if((String)v != null && (String)v != ""){
				path = (String)v;
			}
		}
		try {
            XPathExpression expr =
                xpath.compile(path);// + "/@" + valorBuscado );
            String campoBuscado = (String) expr.evaluate(doc, XPathConstants.STRING);
            
            campo = campoBuscado; 
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
		return campo;
	}

	private CmsFile createResource(String resourceName) throws CmsIllegalArgumentException, CmsException{
		LOG.debug(this.getClass().getName() + " || creating resource type '" + contentType + "': " + resourceName);
		writeToLog("Creando tipo de recurso '" + contentType + "': " + resourceName);

		int typeResource = OpenCms.getResourceManager().getResourceType(contentType).getTypeId();
		CmsResource res = cms.createResource(resourceName,typeResource);
		return cms.readFile(res);
	}

	private String generateResourceName(String vfsPath, Document doc, String item, XPath xpath) throws CmsException, DataTransformartionException {
		CmsXmlContentValueSequence campoSequence = importDefinitionContent.getValueSequence("CampoNombreContenido", contentLocale);
		int campoCount = campoSequence.getElementCount();
		String fileName;
		
		if (campoCount > 0) {
			Object[] value = new Object[campoCount];
			for (int i = 0; i < campoCount; i++) {
	        	String campoColumn = importDefinitionContent.getStringValue(cms,"CampoNombreContenido[" + (i+1) + "]", contentLocale);
	        	if(campoColumn.contains("REPLACE")){
	        		campoColumn = campoColumn.replace("REPLACE", item);
	    		}
	        	try {
	                XPathExpression expr =
	                    xpath.compile(campoColumn);
	                String campoFecha = (String) expr.evaluate(doc, XPathConstants.STRING);
	                value[i] = campoFecha; 
	            } catch (XPathExpressionException e) {
	                e.printStackTrace();
	            }
			}
	
			Object valueFinal = transform("",value,"TransformacionNombre");
			
			if (!vfsPath.endsWith("/"))
				vfsPath += "/";
			fileName = vfsPath + valueFinal.toString();
			return fileName;
		}
		
		int maxNewsValue  = 0;
		String extension = "html";
		
		if(contentType.equals("video-youtube") || contentType.equals("video-embedded"))
			extension = "lnk";
		
		List<CmsResource> cmsFiles = cms.getResourcesInFolder(vfsPath, CmsResourceFilter.ALL);
		for (CmsResource resource : cmsFiles) {
			fileName = resource.getName();
			if (fileName.matches(".*" + preffixName + "_[0-9]{4}."+extension)) {
				String auxFileName =fileName.substring(fileName.indexOf(preffixName + "_"));
				int newsValue = Integer.parseInt(auxFileName.replace(preffixName + "_","").replace("."+extension,""));
				if (maxNewsValue<newsValue)
					maxNewsValue=newsValue;
			}
		}

		DecimalFormat df = new DecimalFormat("0000");
		if (!vfsPath.endsWith("/"))
			vfsPath += "/";
		fileName = vfsPath + preffixName + "_" + df.format(maxNewsValue+1) + "."+extension;
		return fileName;
	}

	private void createFolders(String baseFolder, String subFolder) throws CmsIllegalArgumentException, CmsException {
		String[] subFolders = subFolder.split("/");
		String folderName = baseFolder;
		for (String subpath : subFolders){
			if(!subpath.isEmpty()){
				folderName += subpath + "/"; 
				if (!cms.existsResource(folderName)) {
					writeToLog("Creando carpeta " + folderName);
					cms.createResource(folderName, CmsResourceTypeFolder.RESOURCE_TYPE_ID);
					cms.unlockResource(folderName);
				}
			}
		} 
		
	}

	private String generateSubFolder(Document doc, String item, XPath xpath) {
		CmsXmlContentValueSequence campoSequence = importDefinitionContent.getValueSequence("CampoFechaContenido", contentLocale);
		int campoCount = campoSequence.getElementCount();
		
		if (campoCount>0) {
			Object[] value = new Object[campoCount];
			for (int i = 0; i < campoCount; i++) {
	        	String campoColumn = importDefinitionContent.getStringValue(cms,"CampoFechaContenido[" + (i+1) + "]", contentLocale);
	        	if(campoColumn.contains("REPLACE")){
	        		campoColumn = campoColumn.replace("REPLACE", item);
	    		}
	        	try {
	                XPathExpression expr = xpath.compile(campoColumn);
	                String campoFecha = (String) expr.evaluate(doc, XPathConstants.STRING);
	                if(campoFecha!=null) 
	                	campoFecha = campoFecha.trim();
	                		
	                Date date = null;
	                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/mm/yyyy");
	                try
	                {
	                    date = simpleDateFormat.parse(campoFecha);	                    
	                }
	                catch (Exception ex)
	                {
	                    ex.getMessage();
	                }
	                value[i] = date; 
	            } catch (XPathExpressionException e) {
	                e.printStackTrace();
	            }
			}
	
			Object valueFinal = null;
			try {
					valueFinal = transform("",value,"Transformacion");
			} catch (DataTransformartionException e) {
					e.printStackTrace();
			}
				
			return valueFinal.toString();		
		}
		
		CmsXmlContentValueSequence campoSeq = importDefinitionContent.getValueSequence("CampoCarpetaContenido", contentLocale);
		int campoC = campoSeq.getElementCount();
		if (campoC>0) {
			Object[] value = new Object[campoC];
			for (int i = 0; i < campoC; i++) {
	        	String campoColumn = importDefinitionContent.getStringValue(cms,"CampoCarpetaContenido[" + (i+1) + "]", contentLocale);
	        	if(campoColumn.contains("REPLACE")){
	        		campoColumn = campoColumn.replace("REPLACE", item);
	    		}
	        	try {
	                XPathExpression expr = xpath.compile(campoColumn);
	                String campoCarpeta = (String) expr.evaluate(doc, XPathConstants.STRING);	
	                if(campoCarpeta!=null) 
	                	campoCarpeta = campoCarpeta.trim();
	                
	                value[i] = campoCarpeta; 
	            } catch (XPathExpressionException e) {
	                e.printStackTrace();
	            }
			}
	
			Object valueFinal = null;
			try {
				valueFinal = transform("",value,"Transformacion");
			} catch (DataTransformartionException e) {
				e.printStackTrace();
			}
				
			return valueFinal.toString();		
		}
		return "";
	}
	
	public void setFileName(String fileName) {
		this.executeFileName = fileName;
	}

	private String getItemUbication(Document doc, XPath xpath, String item, String Xpath) {
		String extension = importDefinitionContent.getStringValue(cms, "FiltroExtension", contentLocale);
		String ubication = null;
		if(Xpath.contains("REPLACE")){
			Xpath = Xpath.replace("REPLACE", item);
		}
        try {
            XPathExpression expr =
                xpath.compile(Xpath);
            
            String folder = (String)expr.evaluate(doc, XPathConstants.STRING);
            
            ubication = folder + "/" + item + "." + extension;
            
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
 
        return ubication;
	}
	
	@SuppressWarnings("unchecked")
	private List<String> getListOfArticles(Document doc, XPath xpath) {
        String name = null;
        List<String> list = new ArrayList();
        String filtroNoticias = importDefinitionContent.getStringValue(cms, "FiltroElemento", contentLocale);
        try {
            XPathExpression expr =
                xpath.compile(filtroNoticias);//"/Export/Article/@id"
            
            Object result = expr.evaluate(doc, XPathConstants.NODESET);
            NodeList nodes = (NodeList) result;
            for (int i = 0; i < nodes.getLength(); i++) {
            	list.add(nodes.item(i).getNodeValue());
            	writeToLog(nodes.item(i).getNodeValue()); 
            }
            
            if(nodes.getLength()==0 && this.executeFileName !=null && !this.executeFileName.equals("")){
            	list.add(this.executeFileName);
            	writeToLog(this.executeFileName);
            }
            
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
 
        return list;
    }
	
	private List<String> getListOfArticlesbyFolder(String path) {
		
        List<String> list = new ArrayList();
        
        try {
        	File folder = new File(path);
        	FileFilter fileFilter = new WildcardFileFilter("*.xml");
        	File[] listOfFiles = folder.listFiles(fileFilter);

        	    for (int i = 0; i < listOfFiles.length; i++) {
        	      if (listOfFiles[i].isFile()) {
        	  			list.add(listOfFiles[i].getName());
                	    writeToLog("File " + listOfFiles[i].getName());
        	      } 
        	    }
        } catch (Exception e) {
            writeToLog("ERROR - No se pudo obtener la lista de archivos para importar. "+e.getMessage());
        }
 
        return list;
    }

    public String getImportName() {
		return importName;
	}
    
    public String getContentType() {
		return contentType;
	}
      
    private String getUploadPath() {
		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		return config.getParam("", "", "importManager", "imporFilesPath","");
	}
    
    private void moveFileAfterProcess(String fileName){			
    	try{
    		String destFolder = BASE_FOLDER + "processed/" + campoNombreArchivo;
    		File afile =new File(BASE_FOLDER + "process/" + campoNombreArchivo + "/" + fileName);
    		boolean success = false;
    		
    		File df = new File(destFolder);
    		if(!df.exists()){
	    		success = (new File(destFolder)).mkdirs();
	    		if (success) {
	    			if(afile.renameTo(new File(destFolder + "/" + afile.getName()))){
	    	    		afile.delete();
	    	    	}
	    		}	    	   	 
    		}else{
    			if(afile.renameTo(new File(destFolder + "/" + afile.getName()))){
    	    		afile.delete();
    	    	}
    		}
    	    	    	    
    	}catch(Exception e){
			writeToLog("... Error copiando recurso a processed (" + fileName +"): ");
			writeToLog("causa: " + e.getMessage());
    	}
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			
			if(this.executeFileName !=null && !this.executeFileName.equals("")){
				
				if(this.executeFileName.endsWith(".zip"))
	        	{   
	        		String compressFile = getUploadPath() + this.executeFileName;
	        		
	        		if(campoNombreArchivo != null && !campoNombreArchivo.equals("")){
	        			decompress(compressFile, BASE_FOLDER + campoNombreArchivo + S );	
	        		}else{
	        			decompress(compressFile, BASE_FOLDER);
	        		}	
	        	}else
	        		campoDocImportacion =  this.executeFileName;
			}
			
			getXmlContent();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DataTransformartionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ImportException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		writeToLog("// Proceso de importacion finalizado");
		closeLog();
	}
}

