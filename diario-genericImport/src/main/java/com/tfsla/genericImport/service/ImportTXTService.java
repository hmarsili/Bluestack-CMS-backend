package com.tfsla.genericImport.service;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLDecoder;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.xml.CmsXmlEntityResolver;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.content.CmsXmlContentValueSequence;
import org.xml.sax.SAXException;

import com.tfsla.diario.ediciones.services.ImagenService;
import com.tfsla.genericImport.exception.DataTransformartionException;
import com.tfsla.genericImport.exception.ImportException;
import com.tfsla.genericImport.model.A_ImportService;
import com.tfsla.utils.CmsResourceUtils;


public class ImportTXTService extends A_ImportService{
	
	private String pathFilesContent;
	private CmsObject cms;
	private static String BASE_FOLDER = null;
	private String destinationPath = null;
	private String S = "/";
	private Locale contentLocale;
	private String basePath;
	private String siteBasePath;
	private String currentFileName;
	private String fileName;
	private String campoNombreArchivo;
	boolean isFotogaleria = false;
	private boolean publishFolders = false;
	List<String> articles;
	Map<String, List<String>> newsImages = new HashMap<String, List<String>>();
	
	public String getOffset() {
		return offset;
	}

	public void setOffset(String offset) {
		this.offset = offset;
	}

	private String offset;
	
	public void setPublishFolders(boolean publishFolders) {
		this.publishFolders = publishFolders;
	}
	
	public ImportTXTService(CmsObject cms, String importDefinitionPath) throws CmsException{	
		super(cms, importDefinitionPath, false);
		this.importDefinitionPath = importDefinitionPath;
		this.cms = cms;

		if (getLocale()==null)
	    	setLocale(cms.getRequestContext().getLocale());
		
		if(contentLocale == null)
			contentLocale = cms.getRequestContext().getLocale();
		
		importName = importDefinitionContent.getStringValue(cms, "Nombre", contentLocale);
		contentType = importDefinitionContent.getStringValue(cms, "TipoContenido", contentLocale);
		basePath = importDefinitionContent.getStringValue(cms, "BasePath", contentLocale);
		campoNombreArchivo = importDefinitionContent.getStringValue(cms, "CampoNombreImportacion", contentLocale);

		if(basePath!=null && !basePath.equals("")){
			String[] basePathParts = basePath.split("\\/");
			siteBasePath = "/"+basePathParts[1]+"/"+basePathParts[2]+"/";
		}
		
	}
	
	public void getXmlContent() throws SAXException, ImportException, ParserConfigurationException {   
		
        BASE_FOLDER = getUploadPath();
        
        if(BASE_FOLDER ==null || BASE_FOLDER.equals("") ){
        	writeToLog("Falta configurar el parámetro imporFilesPath del módulo importAutomatic");
        	return;
        }	
         
        destinationPath = getDestinationFolderPath();
        
        if(destinationPath == null || destinationPath.equals("")){
        	writeToLog("Falta configurar el parámetro imporFilesDestinationPath del módulo importAutomatic");
        	return;
        }
        			
        try {            
			           
            if(campoNombreArchivo != null){
            	if(BASE_FOLDER.endsWith("/") )
            	  pathFilesContent = BASE_FOLDER + campoNombreArchivo;
            	else
            	  pathFilesContent = BASE_FOLDER + S + campoNombreArchivo;
            }
                        
            articles = getListOfArticles(pathFilesContent);
            
            if(articles.size()==0)
            	writeToLog("No se encontraon noticias para importar en "+pathFilesContent);
            
            for(String item : articles){
            	   	
            	String subFolder = null;            	
        		String ElementToRead = BASE_FOLDER + campoNombreArchivo + S + item;
				currentFileName = "";
				
				try {
					subFolder = createSubFoldersByDate(item);     
					String resourceFolder = basePath + subFolder;
					
					createFolders(basePath, subFolder);						
					currentFileName = generateResourceName(resourceFolder);			
					
					CmsFile newFile = createResource(currentFileName);
					cms.writePropertyObject(currentFileName, new CmsProperty("oldRecordId",null,"" + item));
					cms.writePropertyObject(currentFileName, new CmsProperty("newsType",null,"news"));
					
					CmsXmlContent newXmlContent = null;
					
					newXmlContent = CmsXmlContentFactory.unmarshal(cms, newFile);			
					mapValues(newXmlContent, ElementToRead, item, "",pathFilesContent,currentFileName);						
					
					newFile.setContents(newXmlContent.marshal());
										
					cms.writeFile(newFile);					
					cms.unlockResource(currentFileName);				
					
					moveFileAfterProcess(item);
					
					writeToLog("... Fin importacion recurso (" + item +"): " + currentFileName);
					
				}  catch (CmsIllegalArgumentException e) {
					writeToLog("... Error en importacion recurso (" + item +"): " + currentFileName);
					writeToLog("causa: " + e.getMessage());
				} catch (CmsException e) {
					writeToLog("... Error en importacion recurso (" + item +"): " + currentFileName);
					writeToLog("causa: " + e.getMessage());
				} 
            }
            
            uploadImages();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
 
    }
	
	private void moveFileAfterProcess(String fileName){			
    	try{
    		String destFolder = destinationPath + campoNombreArchivo;
    		File afile =new File(BASE_FOLDER + campoNombreArchivo + "/" + fileName);
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
	
	private void mapValues(CmsXmlContent content, String path, String item, String valorEntrega, String pathFilesContent, String newsVfsPath) throws ImportException {
		
		CmsXmlContentValueSequence elementSequence = importDefinitionContent.getValueSequence("" + "Mapping", contentLocale);
		int elementCount = elementSequence.getElementCount();
		
		String strFile = getStrFile(path);
		
        for (int j = 0; j < elementCount; j++){
        	
        	String  elementoDestino = importDefinitionContent.getStringValue(cms,"" + "Mapping[" + (j+1) + "]/Elemento", contentLocale);
        	String propiedadDestino = importDefinitionContent.getStringValue(cms,"" + "Mapping[" + (j+1) + "]/Property", contentLocale);
        	        	
    		CmsXmlContentValueSequence campoSequence = importDefinitionContent.getValueSequence("" + "Mapping[" + (j+1) + "]/Buscar", contentLocale);
    		int 						  campoCount = campoSequence.getElementCount();
    		
    		String multiXpathValue = "";
        	String     valorManual = importDefinitionContent.getStringValue(cms,"" + "Mapping[" + (j+1) + "]/Valor", contentLocale); 
        	int valorManualCargado = (valorManual!=null ? 1 : 0); 
    		
        	Object[] value = new Object[campoCount + valorManualCargado];
        	
    		if (valorManual!=null) 
    			value[0] = valorManual;    			
    		
    		if (valorEntrega!=null && valorManual == null)
    			value[0] = valorEntrega;    			
    		
    		if(valorManual == null || valorManual == ""){
	    		for (int i = 0; i < campoCount; i++) {
	            	String campoColumn = importDefinitionContent.getStringValue(cms,"" + "Mapping[" + (j+1) + "]/Buscar[" + (i+1) + "]", contentLocale);
	            	   multiXpathValue = getValueForItem(campoColumn,true,strFile);
	    		}
    			value[0] = multiXpathValue;
    		}
    		
    		Object valueFinal = null;
    		
    		try {
    			valueFinal = transform("Mapping[" + (j+1) + "]/",value);
    		}catch (DataTransformartionException e) {
    			writeToLog("Error aplicando transformacion para " + (elementoDestino!=null ? " '" + elementoDestino + "' " : "")  + (propiedadDestino!=null ? " '" + propiedadDestino + "' " : "") + " :" + e.getMessage());
    			throw e;
    		}
    		        	
        	String elementCeatedPreffix = "";
        	String        elementoCrear = "";
        	
        	if (valueFinal!=null){
        		
	        	elementoCrear = importDefinitionContent.getStringValue(cms,"" + "Mapping[" + (j+1) + "]/Crear", contentLocale);
	        	
	        	if (elementoCrear!=null){
	        		
	        		if(elementoCrear.equals("imagenesFotogaleria"))
	        		{
	        			if(valueFinal!=""){
			        		String[]  elementParts = elementoCrear.split("/");
			        		String elementPosition = "";
			        		int           newIndex = 1;
			        		
			        		for (int i=0;i<elementParts.length;i++)
			        		{
			        			elementPosition += elementParts[i];
			        			if (i<elementParts.length-1) {
			        				elementPosition += "[" + content.getValueSequence(elementPosition, contentLocale).getElementCount() + "]/";
			        			}
			        			else{	        				
		        					if(isFotogaleria && elementPosition.equals("imagenesFotogaleria"))
		        						newIndex = content.getValueSequence(elementPosition, contentLocale).getElementCount() + 1;
		        					else{
		        						newIndex = content.getValueSequence(elementPosition, contentLocale).getElementCount();
		        					}	        				    				
			        			}
			        			
			        		}
			        		
			        		if (content.getValueSequence(elementPosition, contentLocale).getMinOccurs()>0){  
			        			try {
				        			if (newIndex > content.getValueSequence(elementPosition, contentLocale).getMinOccurs())
				        				content.addValue(cms,elementPosition,contentLocale,newIndex-1);
			        			}catch (CmsRuntimeException e) {
			        				throw new ImportException("Error al agregar nuevo elemento '" + e.getMessage());
			        			}catch (Exception e1) {
			        				throw new ImportException("Error al agregar nuevo elemento '" + e1.getMessage());
								}
			        		}else{
			        			try {
			        				content.addValue(cms,elementPosition,contentLocale,newIndex);
			        			}catch (CmsRuntimeException e) {
			        				throw new ImportException("Error al agregar nuevo elemento '" + e.getMessage());
			        			}catch (Exception e1) {
			        				throw new ImportException("Error al agregar nuevo elemento '" + e1.getMessage());
								}
		
			        			newIndex++;
			        		}
			        		elementCeatedPreffix = elementPosition + "[" + newIndex + "]";
	        			}
	        		}else{
	        			String[]  elementParts = elementoCrear.split("/");
		        		String elementPosition = "";
		        		int           newIndex = 1;
		        		
		        		for (int i=0;i<elementParts.length;i++){
		        			elementPosition += elementParts[i];
		        			if (i<elementParts.length-1)
		        				elementPosition += "[" + content.getValueSequence(elementPosition, contentLocale).getElementCount() + "]/";
		        			else 	            					
	        					newIndex = content.getValueSequence(elementPosition, contentLocale).getElementCount();	        					        				    					        			
		        		}
		        		
		        		if (content.getValueSequence(elementPosition, contentLocale).getMinOccurs()>0) {  
		        			try {
			        			if (newIndex > content.getValueSequence(elementPosition, contentLocale).getMinOccurs())
			        				content.addValue(cms,elementPosition,contentLocale,newIndex-1);
		        			}catch (CmsRuntimeException e) {
		        				writeToLog("Error al agregar nuevo elemento '" + e.getMessage());
		        			}catch (Exception e) {
		        				writeToLog("Error al agregar nuevo elemento '" + e.getMessage());
		        			}
		        		}
		        		else {
		        			try {
		        				content.addValue(cms,elementPosition,contentLocale,newIndex);
		        			}catch (CmsRuntimeException e) {
		        				writeToLog("Error al agregar nuevo elemento '" + e.getMessage());
		        			}catch (Exception e) {
		        				writeToLog("Error al agregar nuevo elemento '" + e.getMessage());
		        			}
	
		        			newIndex++;
		        		}
		        		elementCeatedPreffix = elementPosition + "[" + newIndex + "]";
	        		}
	        	}
        	}
        	
        	if (elementoDestino!=null){
        		
        		String elementPosition = "";
	
    			if (elementoCrear!=null && !elementoCrear.equals("") && elementoDestino.startsWith(elementoCrear)){
    					
    				if(elementoCrear.equals("imagenesFotogaleria"))
    	        	{
    	        			if(valueFinal!=""){
		    					elementoDestino = elementoDestino.substring(elementoCrear.length());
		    					
		    					if (elementoDestino.startsWith("/"))
		    						elementoDestino = elementoDestino.substring(1);
		
		    					elementPosition = elementCeatedPreffix;
		    					
		    					if (elementoDestino.length()>0)
		    						elementPosition += "/";
    	        			}
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
		        			
		        			if (i<elementParts.length-1)
		        				elementPosition += "/";
		        			
		        		}
    			}	        		
    			
    			
        		if (valueFinal!=null){
        			
        			String finalValueString = ""; 
	        		
	        		
        			if(elementoDestino.trim().equals("imagen") || elementPosition.indexOf("/imagen[")>-1)
	        		{
        				String imgFileName = valueFinal.toString();
        				
        				if(!imgFileName.equals("")){
        					addImage(imgFileName, pathFilesContent,newsVfsPath,elementPosition);
        				}
	        		}else
	        			finalValueString = stripNonValidXMLCharacters(valueFinal.toString());
        			
	        		try{
	        		  content.getValue(elementPosition, contentLocale).setStringValue(cms, finalValueString);
	        		}catch (Exception e) {
	        			writeToLog("Error No se , no se puede setear el campo "+elementPosition+" con el valor "+finalValueString+". Error:"+e.getMessage());
	        		}
        		}
        		else
	        	    writeToLog("Omitiendo valor en " + elementPosition + " por ser nulo.");
	        	
        	}
        }
		
	}
	
	// convert from UTF-8 -> internal Java String format
    public static String convertFromUTF8(String s) {
        String out = null;
        try {
            out = new String(s.getBytes("ISO-8859-1"), "UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            return null;
        }
        return out;
    }
    
 
    public static String convertToUTF8(String s) {
        String out = null;
        try {
        	out = URLDecoder.decode(s, "UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            return null;
        }
        return out;
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
	
	private String getStrFile(String filePath){
		
		String strFile = "";
		
		FileInputStream fstream;
		
		try {
			fstream = new FileInputStream(filePath);
			
			int valor = fstream.read();
			
            while(valor!=-1){
                strFile += (char)valor;
                valor = fstream.read();
            }
			
		} catch (FileNotFoundException e) {
			writeToLog("Error en la importación, no se puede leer el archivo "+filePath+" Error:"+e.getMessage());
		} catch (IOException e) {
			writeToLog("Error en la importación, no se puede leer el archivo "+filePath+" Error:"+e.getMessage());
		}
		
		return strFile;
		
	}
	
	private String getValueForItem(String elementPath, Boolean firstOnly,String strFile) {
		
		String newFieldValue = "";
		
		if(elementPath.startsWith("NOT:")){
			String notElements = elementPath.replace("NOT:", "");
			String[] parts = notElements.split(",");
			
			newFieldValue = strFile;
			
			for(int i=0; i<parts.length;i++){
				if(!parts[i].trim().equals(""))
				    newFieldValue = newFieldValue.replaceAll("\\<"+parts[i].trim()+"\\>\\s*(.*?)\\s*\\<\\/"+parts[i].trim()+"\\>", "");
			}
			
		}else{
		
			Pattern p_strFilePart = Pattern.compile("\\<"+elementPath+"\\>\\s*(.*?)\\s*\\<\\/"+elementPath+"\\>", Pattern.DOTALL);
			Matcher m_strFilePart = p_strFilePart.matcher(strFile);
			
			if(firstOnly){
				  if(m_strFilePart.find())
				     newFieldValue = m_strFilePart.group(1);
			}else{
			
				while(m_strFilePart.find())
				{
					newFieldValue = m_strFilePart.group(1);
				}
			}
		}
		
		return newFieldValue;
		
	}
	
	private CmsFile createResource(String resourceName) throws CmsIllegalArgumentException, CmsException{
		
		writeToLog("Creando tipo de recurso '" + contentType + "': " + resourceName);

		int typeResource = OpenCms.getResourceManager().getResourceType("noticia").getTypeId();
		CmsResource res = cms.createResource(resourceName,typeResource);
		return cms.readFile(res);
	}

	private String generateResourceName(String vfsPath) throws CmsException, DataTransformartionException {
		
		int maxNewsValue  = 0;
		List<CmsResource> cmsFiles = cms.getResourcesInFolder(vfsPath, CmsResourceFilter.ALL);
		for (CmsResource resource : cmsFiles) {
			fileName = resource.getName();
			if (fileName.matches(".*" + "noticia" + "_[0-9]{4}.html")) {
				String auxFileName =fileName.substring(fileName.indexOf("noticia" + "_"));
				int newsValue = Integer.parseInt(auxFileName.replace("noticia" + "_","").replace(".html",""));
				if (maxNewsValue<newsValue)
					maxNewsValue=newsValue;
			}
		}

		DecimalFormat df = new DecimalFormat("0000");
		if (!vfsPath.endsWith("/"))
			vfsPath += "/";
		fileName = vfsPath + "noticia" + "_" + df.format(maxNewsValue+1) + ".html";
		return fileName;
	}

	private void createFolders(String baseFolder, String subFolder) throws CmsIllegalArgumentException, CmsException {
		String[] subFolders = subFolder.split("/");
		String folderName = baseFolder;
		
		String publishLevel = "";
		if (!cms.existsResource(folderName)) {
			writeToLog("Creando carpeta " + folderName);
			cms.createResource(folderName, CmsResourceTypeFolder.RESOURCE_TYPE_ID);
			cms.unlockResource(folderName);
			
			publishLevel = folderName;
		}
		for (String subpath : subFolders){
			if(!subpath.isEmpty()){
				folderName += subpath + "/"; 
				if (!cms.existsResource(folderName)) {
					writeToLog("Creando carpeta " + folderName);
					cms.createResource(folderName, CmsResourceTypeFolder.RESOURCE_TYPE_ID);
					cms.unlockResource(folderName);
					
					if (publishLevel.equals(""))
							publishLevel = folderName;
				}
			}
		} 
		
		if (publishFolders && !publishLevel.equals(""))
		{
			try {
				OpenCms.getPublishManager().publishResource(cms,publishLevel);
			}
			catch (Exception e) {
				//Si no pudo publicarlarla, puede ser que este usuario no tenga permisos de publicacion, y entonces, no se tiene que romper
				//queda la carpeta sin publicarla.
				writeToLog("Error al publicar la carpeta " + publishLevel);
			}

		}
		
	}	
	
	private String createSubFoldersByDate(String item) {	
    	Object valueFinal = null;
    	
            try
            {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
                String today = formatter.format(new Date());
                valueFinal = today;
            }
            catch (Exception ex)
            {
                ex.getMessage();
            }
           
		return valueFinal.toString();				
	}
	
	private List<String> getListOfArticles(String path) {
		
        List<String> list = new ArrayList();
        
        try {
        	File folder = new File(path);
        	FileFilter fileFilter = new WildcardFileFilter("*.txt");
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
		return config.getParam("", "", "importAutomatic", "imporFilesPath",null);
	}
    
    private String getDestinationFolderPath() {
		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		return config.getParam("", "", "importAutomatic", "imporFilesDestinationPath",null);
	}
    
    private void addImage(String imgName, String folderPath,String newsVfsPath, String xPath){
    	
        List<String> valSetImg = new ArrayList<String>();
        valSetImg.add(folderPath+"|"+newsVfsPath+"|"+xPath);

        
        newsImages.put(imgName, valSetImg);
		
    	return;
    }
    
    private void uploadImages(){
    	
    	Locale locale = cms.getRequestContext().getLocale();
    	
    	for (Map.Entry<String, List<String>> entry : newsImages.entrySet()) {
            String key = entry.getKey();
            List<String> values = entry.getValue();
            
            String imgFileName = key;
            
            String imgValues = values.get(0);
            
            String[] imageParts = imgValues.split("\\|");
            String imgSourceFolder = imageParts[0];
            String newsPath = imageParts[1];
            String xpath = imageParts[2];
            
            String vfsPath = "";
            String seccion = null;
            
             try{
            	CmsResourceUtils.forceLockResource(cms, newsPath);
            	 
	            CmsFile contentFile = cms.readFile(newsPath);
	           	CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, contentFile);
	
	           	CmsXmlUtils.validateXmlStructure(contentFile.getContents(), new CmsXmlEntityResolver(cms));
	           	content.setAutoCorrectionEnabled(true);
	           	content.correctXmlStructure(cms);
	            
	            seccion = content.getValue("seccion", locale).getStringValue(cms);
	            
	            vfsPath = uploadImage(imgFileName,imgSourceFolder,seccion);
	            
	            content.getValue(xpath, locale).setStringValue(cms, vfsPath);
	            contentFile.setContents(content.marshal());
				cms.writeFile(contentFile);
				
				CmsResourceUtils.unlockResource(cms, newsPath, false);
				CmsResourceUtils.unlockResource(cms, vfsPath, false);
	            
         	} catch (CmsException e) {
         		 writeToLog("ERROR - No se pudo agregar la imagen a la noticia. "+e.getMessage());
         	}
            
        }
    	
    }
    
    private String uploadImage(String imgName, String folderPath, String section)
    {
    	String vfsPath = "";
    	String uploadTo = "server";
    	
    	CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
    	uploadTo = config.getParam("", "", "imageUpload", "defaultUploadDestination","server");
		
		try {
			
			if(uploadTo.equals("server"))
				vfsPath = uploadImageRFS(folderPath,imgName,section);
			else if(uploadTo.equals("amz"))
				vfsPath = uploadImageAMZ(folderPath,imgName,section);
			else if(uploadTo.equals("ftp"))
				vfsPath = uploadImageFTP(folderPath,imgName,section);
			else if(uploadTo.equals("vfs"))
				vfsPath = uploadImageVFS(folderPath,imgName,section);
			
			moveFileAfterProcess(imgName);
			
		} catch (Exception e) {
				writeToLog("ERROR - No se pudo subir la imagen. "+e.getMessage());
		}
    	
    	return vfsPath;
    }
    
    protected String uploadImageVFS(String imgPath,String fileName,String section) throws Exception{
		
		Map<String,String> parameters = new HashMap<String,String>();
		
		if(section!=null && section.trim().equals(""))
			section = "import";
		
		parameters.put("section",section);
		
		CmsObject cmsObject = OpenCms.initCmsObject(this.cms);
				  cmsObject.getRequestContext().setSiteRoot(siteBasePath);
		
		String path = siteBasePath + ImagenService.getInstance(cmsObject).getDefaultVFSUploadFolder(parameters);
	
		String vfsImgPath = path + fileName;
		
		if(!imgPath.endsWith("/"))
			imgPath = imgPath + "/";
		
		FileInputStream			  in = new FileInputStream(imgPath + fileName );
		ByteArrayOutputStream outVfs = new ByteArrayOutputStream();
		byte[] 					 buf = new byte[1024];
		int                        n = 0;
		        
		while (-1 != (n = in.read(buf))) {
			outVfs.write(buf, 0, n);
		}
		     
		outVfs.close();
		in.close();
		byte[] buffer = outVfs.toByteArray();
		
		cms.createResource(vfsImgPath, OpenCms.getResourceManager().getResourceType("image").getTypeId(),buffer,null);
		
		return vfsImgPath;
	}
	
	protected String uploadImageAMZ(String imgPath, String fileName, String section) throws Exception {
		
		Map<String,String> parameters = new HashMap<String,String>();
		
		if(section!=null && section.trim().equals(""))
			section = "import";
		
		parameters.put("section",section);
		
		CmsObject cmsObject = OpenCms.initCmsObject(this.cms);
				  cmsObject.getRequestContext().setSiteRoot(siteBasePath);
		
		String path = siteBasePath + ImagenService.getInstance(cmsObject).getDefaultVFSUploadFolder(parameters);
	
		if(!imgPath.endsWith("/"))
			imgPath = imgPath + "/";
		
		BufferedInputStream buffIn = null;
        buffIn = new BufferedInputStream(new FileInputStream(imgPath + fileName ));
		
		String lcFileName = fileName.toLowerCase();
		String imageVFS = ImagenService.getInstance(cms).uploadAmzFile(path,lcFileName,parameters,buffIn);
		
		buffIn.close();
		
		return imageVFS;
		
	}
	
	protected String uploadImageRFS(String imgPath,String fileName, String section) throws Exception{
		
		Map<String,String> parameters = new HashMap<String,String>();
		
		if(section!=null && section.trim().equals(""))
			section = "import";
		
		parameters.put("section",section);
		
		CmsObject cmsObject = OpenCms.initCmsObject(this.cms);
				  cmsObject.getRequestContext().setSiteRoot(siteBasePath);
		
		String path = siteBasePath + ImagenService.getInstance(cmsObject).getDefaultVFSUploadFolder(parameters);
	
		if(!imgPath.endsWith("/"))
			imgPath = imgPath + "/";
		
		BufferedInputStream buffIn = null;
        buffIn = new BufferedInputStream(new FileInputStream(imgPath + fileName ));
		
		String lcFileName = fileName.toLowerCase();
		String imageVFS = ImagenService.getInstance(cms).uploadRFSFile(path,lcFileName,parameters,buffIn);
		
		buffIn.close();
		
		return imageVFS;
	}
	
	protected String uploadImageFTP(String imgPath,String fileName, String section) throws Exception{
		
		Map<String,String> parameters = new HashMap<String,String>();
		
		if(section!=null && section.trim().equals(""))
			section = "import";
		
		parameters.put("section",section);
		
		CmsObject cmsObject = OpenCms.initCmsObject(this.cms);
				  cmsObject.getRequestContext().setSiteRoot(siteBasePath);
		
		String path = siteBasePath + ImagenService.getInstance(cmsObject).getDefaultVFSUploadFolder(parameters);
		
		if(!imgPath.endsWith("/"))
			imgPath = imgPath + "/";
		
		BufferedInputStream buffIn = null;
        buffIn = new BufferedInputStream(new FileInputStream(imgPath + fileName));
	    
	    String lcFileName = fileName.toLowerCase();
		String imageFTP = ImagenService.getInstance(cms).uploadFTPFile(path,lcFileName,parameters,buffIn);
		
		buffIn.close();
		
		return imageFTP;
	}

	@Override
	public void run() {
		
		try {
			getXmlContent();
		} catch (SAXException e) {
			e.printStackTrace();
			writeToLog("Erro en proceso de importacion: "+e.getMessage());
		} catch (DataTransformartionException e1) {
			e1.printStackTrace();
			writeToLog("Erro en proceso de importacion: "+e1.getMessage());
		} catch (ImportException e2) {
			e2.printStackTrace();
			writeToLog("Erro en proceso de importacion: "+e2.getMessage());
		} catch (ParserConfigurationException e3) {
			e3.printStackTrace();
			writeToLog("Erro en proceso de importacion: "+e3.getMessage());
		}
		writeToLog("// Proceso de importacion finalizado");
		closeLog();
	}
}
