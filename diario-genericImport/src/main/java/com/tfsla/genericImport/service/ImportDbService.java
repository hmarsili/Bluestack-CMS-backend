package com.tfsla.genericImport.service;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.opencms.db.CmsDbPool;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.content.CmsXmlContentValueSequence;
import org.opencms.xml.types.I_CmsXmlContentValue;

import com.tfsla.genericImport.dao.QueryBuilder;
import com.tfsla.genericImport.dao.ResultSetProcessor;
import com.tfsla.genericImport.exception.DataTransformartionException;
import com.tfsla.genericImport.exception.ImportException;
import com.tfsla.genericImport.model.A_ImportService;
import com.tfsla.genericImport.model.DBColumn;

public class ImportDbService extends A_ImportService {

	private String dbPoolName=null;
	private String dbPoolType=null;
	private Map<String,Map<String,DBColumn>> tablesDefinition = new HashMap<String,Map<String,DBColumn>>();
	private String campoIdRecord;
	private String basePath;
	private String preffixName;
	private boolean isXmlContent;
	private Map<String,Integer> elementLastUsedIndex = new HashMap<String,Integer>();
	private String currentFileName;
	private String DBType;
	
	public ImportDbService(CmsObject cms, String importDefinitionPath) throws CmsException {
		super(cms, importDefinitionPath);
		
		importName = importDefinitionContent.getStringValue(cms, "Nombre", contentLocale);
		contentType = importDefinitionContent.getStringValue(cms, "TipoContenido", contentLocale);
		basePath = importDefinitionContent.getStringValue(cms, "BasePath", contentLocale);
		preffixName = importDefinitionContent.getStringValue(cms, "PreffixName", contentLocale);
		campoIdRecord = importDefinitionContent.getStringValue(cms, "RecordId", contentLocale);
		DBType = importDefinitionContent.getStringValue(cms, "DBType", contentLocale);

		ContentTypeService cTypeService = new ContentTypeService();
		isXmlContent = cTypeService.isXmlContentType(contentType);
	}
	
	@Override
	public void run() {
		ImportContent();
		writeToLog("// Proceso de importacion finalizado");
		closeLog();
	}

	public List<Map<String,Object>> getDataBaseRecords(String path,Map<String, Object> values ) throws ImportException {
		String query = "";
		if(DBType.equals("sqlServer")){
			query = getSQLDBQuery(path,values);
		}else{
			query = getDBQuery(path,values);
		}		
				
		List<Map<String,Object>> tableValues = new ArrayList<Map<String,Object>>();
		try {

			tableValues = getTableValues(query);
		} catch (Exception e) {
			String msg = "Error Obteniendo los registros de la tabla.";
			writeToLog(msg);
			writeToLog(e.getMessage());
			LOG.error(this.getClass().getName() + " || Error ",e);
			throw new ImportException(msg,e);
		}
		return tableValues;
	}
	
	public void ImportContent() {
		Map<String, Object> values = new HashMap<String, Object>();
		List<Map<String, Object>> tableValues;
		try {
			tableValues = getDataBaseRecords("", values );
			String msg = "Se cargaron " + tableValues.size() + " registros.";
			LOG.info(this.getClass().getName() + " || " + msg);
			writeToLog(msg);
			int currRecord = 0;
			int por = tableValues.size() * 5 / 100;
			for (Map<String,Object> record : tableValues) {
				String subFolder = generateSubFolder(record);
				String resourceFolder = basePath + subFolder;
				
				Object idRecord = record.get(campoIdRecord);
				if(record.get(campoIdRecord) == null){
					campoIdRecord = campoIdRecord.replace(importDefinitionContent.getStringValue(cms,"Tabla", contentLocale), "");
					campoIdRecord = campoIdRecord.startsWith(".") ? campoIdRecord.substring(1) : campoIdRecord;
					idRecord = record.get(campoIdRecord);
	        	}
				currRecord++;
				por = checkProcessAdvance(tableValues, currRecord, por);
				
				currentFileName = "";
				try {
					elementLastUsedIndex = new HashMap<String,Integer>();
					
					createFolders(basePath, subFolder);
				
					
					currentFileName = generateResourceName(resourceFolder,record);
					CmsFile newFile = createResource(currentFileName);
					cms.writePropertyObject(currentFileName, new CmsProperty("oldRecordId",null,"" + idRecord));
					
					CmsXmlContent newXmlContent = null;
					StringBuilder content = new StringBuilder();
					
					if (isXmlContent)
						newXmlContent = CmsXmlContentFactory.unmarshal(cms, newFile);
					
					mapValues(newXmlContent, content, "", record);
					
					if (isXmlContent)	
						newFile.setContents(newXmlContent.marshal());
					else
						newFile.setContents(content.toString().getBytes());
					
					cms.writeFile(newFile);
					
					cms.unlockResource(currentFileName);
					writeToLog("... Fin importacion recurso (" + idRecord +"): " + currentFileName);
					
				}  catch (CmsIllegalArgumentException e) {
					if (!currentFileName.equals(""))
						deleteResources(currentFileName, "" + idRecord);
					LOG.error(this.getClass().getName() + " || Error ",e);
					writeToLog("... Error en importacion recurso (" + idRecord +"): " + currentFileName);
					writeToLog("causa: " + e.getMessage());
				} catch (CmsException e) {
					if (!currentFileName.equals(""))
						deleteResources(currentFileName, "" + idRecord);
					LOG.error(this.getClass().getName() + " || Error ",e);
					writeToLog("... Error en importacion recurso (" + idRecord +"): " + currentFileName);
					writeToLog("causa: " + e.getMessage());
				} catch (ImportException e) {
					if (!currentFileName.equals(""))
						deleteResources(currentFileName, "" + idRecord);
					writeToLog("... Error en importacion recurso (" + idRecord +"): " + currentFileName);
					writeToLog("causa: " + e.getMessage());
				} 
			}
		} catch (ImportException e1) {
			e1.printStackTrace();
		} finally {
			if (objWriter!=null) {
				try {
					objWriter.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
	}

	private int checkProcessAdvance(List<Map<String, Object>> tableValues,
			int currRecord, int por) {
		if ((100*currRecord/tableValues.size())>por)
		{
			writeToLog("// Avance de proceso " + por + "%");
			por += 5 * tableValues.size() / 100;
			try {
				objWriter.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		return por;
	}

	private void mapValues(CmsXmlContent content, StringBuilder resContent, String path, Map<String,Object> record) throws ImportException {
		CmsXmlContentValueSequence elementSequence = importDefinitionContent.getValueSequence(path + "Mapping", contentLocale);
		int elementCount = elementSequence.getElementCount();
		
        // loop through elements
        for (int j = 0; j < elementCount; j++) {
            //I_CmsXmlContentValue value = elementSequence.getValue(j);
        	
        	String elementoDestino = importDefinitionContent.getStringValue(cms,path + "Mapping[" + (j+1) + "]/Elemento", contentLocale);
        	String propiedadDestino = importDefinitionContent.getStringValue(cms,path + "Mapping[" + (j+1) + "]/Property", contentLocale);

        	
    		CmsXmlContentValueSequence campoSequence = importDefinitionContent.getValueSequence(path + "Mapping[" + (j+1) + "]/Campo", contentLocale);
    		int campoCount = campoSequence.getElementCount();

        	String valorManual = importDefinitionContent.getStringValue(cms,path + "Mapping[" + (j+1) + "]/Valor", contentLocale);
        	

    		int valorManualCargado = (valorManual!=null ? 1 : 0); 
    		Object[] value = new Object[campoCount + valorManualCargado];
    		if (valorManual!=null) {
    			value[0] = valorManual;
    		}
    		for (int i = 0; i < campoCount; i++) {
            	String campoColumn = importDefinitionContent.getStringValue(cms,path + "Mapping[" + (j+1) + "]/Campo[" + (i+1) + "]", contentLocale);
    			//LOG.debug(path + "Mapping[" + (j+1) + "]/Campo[" + (i+1) + "]" + " -> " + campoColumn);
            	value[i+valorManualCargado] = record.get(campoColumn);
            	if(record.get(campoColumn) == null){
            		campoColumn = campoColumn.substring(campoColumn.lastIndexOf("."));
            		/*if(path.contains("SubTabla")){
            			campoColumn = campoColumn.replace(importDefinitionContent.getStringValue(cms, path + "Tabla", contentLocale), "");
            		}else{
            			campoColumn = campoColumn.replace(importDefinitionContent.getStringValue(cms,"Tabla", contentLocale), "");
            		}*/	        		
	        		value[i] = record.get(campoColumn);
	        	}
    		}

    		Object valueFinal = null;
    		try {
    			valueFinal = transform(path + "Mapping[" + (j+1) + "]/",value);
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
	        		else
		        		LOG.debug(this.getClass().getName() + " || omitiendo valor en propiedad " + propiedadDestino + " por ser nulo.");

				} catch (CmsException e) {
					LOG.error(this.getClass().getName() + " || Error ",e);
				}
        	}
        	
        	String elementCeatedPreffix = "";
        	String elementoCrear = "";
        	if (isXmlContent && valueFinal!=null) {
	        	elementoCrear = importDefinitionContent.getStringValue(cms,path + "Mapping[" + (j+1) + "]/Crear", contentLocale);
	        	
	        	
	        	if (elementoCrear!=null) {
	        		LOG.debug("elemento a Crear:" + elementoCrear);
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
	        		
	        		//Ver caso de elemento con minimo diferente a cero (precargados) para no crearlos si no es necesario.
	        		if (content.getValueSequence(elementPosition, contentLocale).getMinOccurs()>0) {
	        			Integer lastIndexUsed = elementLastUsedIndex.get(elementPosition);
	        			if (lastIndexUsed!=null)
	        				newIndex = lastIndexUsed +1;
	        			else
	        				newIndex = 1;
	        			
	        			elementLastUsedIndex.put(elementPosition,newIndex);
	        			
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
        	
        	if (elementoDestino!=null) {
        		String elementPosition = "";

    			if (isXmlContent) {
    				if (elementoCrear!=null && !elementoCrear.equals("") && elementoDestino.startsWith(elementoCrear)) {
	        			LOG.debug(elementoDestino + " -- " + elementoCrear );
    					elementoDestino = elementoDestino.substring(elementoCrear.length());
    					if (elementoDestino.startsWith("/"))
    						elementoDestino = elementoDestino.substring(1);
	        			//LOG.debug(elementoDestino);

    					elementPosition = elementCeatedPreffix;
    					if (elementoDestino.length()>0)
    						elementPosition += "/";
    					
    					//LOG.debug(elementoDestino);
    				}
    				if (!elementoDestino.trim().equals("")) {
		        		String[] elementParts = elementoDestino.split("/");
		        		for (int i=0;i<elementParts.length;i++) {
		        			elementPosition += elementParts[i]; 
		        			//LOG.debug(elementPosition + " -- " + content.getValueSequence(elementPosition, contentLocale) );
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
        		else
	        		LOG.debug(this.getClass().getName() + " || omitiendo valor en " + elementPosition + " por ser nulo.");

        	}
        }
        getSubTable(content, resContent, path, record);
	}
	
	private void getSubTable(CmsXmlContent newXmlContent, StringBuilder resContent, String path, Map<String,Object> record) throws ImportException {
		CmsXmlContentValueSequence elementSequence = importDefinitionContent.getValueSequence(path + "SubTabla", contentLocale);
		int elementCount = elementSequence.getElementCount();
        // loop through elements
        for (int j = 0; j < elementCount; j++) {
    		String newPath = path + "SubTabla[" + (j+1) + "]/"; 
    		List<Map<String,Object>> tableValues = getDataBaseRecords(newPath,record ); 
    		for (Map<String,Object> subRecord : tableValues) {
    			// agregar a tableValues lo que tiene record 
    			subRecord.putAll(record);
    			mapValues(newXmlContent, resContent, newPath, subRecord);
    		}
        }
	}
	
	private void createFolders(String baseFolder, String subFolder) throws CmsIllegalArgumentException, CmsException {

		LOG.debug(this.getClass().getName() + " || creating folder: " + baseFolder + subFolder );

		String[] subFolders = subFolder.split("/");
		String folderName = baseFolder;
		for (String subpath : subFolders){
			
			folderName += subpath + "/"; 
			if (!cms.existsResource(folderName)) {
				writeToLog("Creando carpeta " + folderName);
				cms.createResource(folderName, CmsResourceTypeFolder.RESOURCE_TYPE_ID);
				cms.unlockResource(folderName);
			}
		} 

	}
	
	private String generateSubFolder(Map<String,Object> record) throws DataTransformartionException {
		//LOG.debug("Obteniendo para la carpeta del subcarpeta ");

		CmsXmlContentValueSequence campoSequence = importDefinitionContent.getValueSequence("CampoFechaContenido", contentLocale);
		int campoCount = campoSequence.getElementCount();
		if (campoCount>0) {
			Object[] value = new Object[campoCount];
			for (int i = 0; i < campoCount; i++) {
	        	String campoColumn = importDefinitionContent.getStringValue(cms,"CampoFechaContenido[" + (i+1) + "]", contentLocale);
				//LOG.debug(path + "Mapping[" + (j+1) + "]/Campo[" + (i+1) + "]" + " -> " + campoColumn);
	        	value[i] = record.get(campoColumn);
	        	if(record.get(campoColumn) == null){
	        		campoColumn = campoColumn.substring(campoColumn.lastIndexOf("."));
	        		//campoColumn = campoColumn.replace(importDefinitionContent.getStringValue(cms,"Tabla", contentLocale), "");
	        		value[i] = record.get(campoColumn);
	        		LOG.debug("campoColumn " + campoColumn + ": " + value[i]);
	        	}
	        	if(value[i] == null){
	        		campoColumn = campoColumn.replace(importDefinitionContent.getStringValue(cms,"SubTabla/Tabla", contentLocale), "");
	        		value[i] = record.get(campoColumn);
	        	}
	        	
				LOG.debug("generateSubFolder -> " + campoColumn + " (value[" + i + "]) :" + value[i] + "  <-- type " + value.getClass().toString() );
			}
	
				Object valueFinal = transform("",value,"Transformacion");
				
				return valueFinal.toString();
				
			
		}
		return "";
	}
	
	private String generateResourceName(String vfsPath, Map<String,Object> record) throws CmsException, DataTransformartionException {
		CmsXmlContentValueSequence campoSequence = importDefinitionContent.getValueSequence("CampoNombreContenido", contentLocale);
		int campoCount = campoSequence.getElementCount();
		String fileName;
		
		if (campoCount > 0) {
			Object[] value = new Object[campoCount];
			for (int i = 0; i < campoCount; i++) {
	        	String campoColumn = importDefinitionContent.getStringValue(cms,"CampoNombreContenido[" + (i+1) + "]", contentLocale);
				//LOG.debug(path + "Mapping[" + (j+1) + "]/Campo[" + (i+1) + "]" + " -> " + campoColumn);
	        	value[i] = record.get(campoColumn);
	        	if(record.get(campoColumn) == null){
	        		campoColumn = campoColumn.replace(importDefinitionContent.getStringValue(cms,"Tabla", contentLocale), "");
	        		value[i] = record.get(campoColumn);
	        	}
	        	if(value[i] == null){
	        		campoColumn = campoColumn.replace(importDefinitionContent.getStringValue(cms,"SubTabla/Tabla", contentLocale), "");
	        		value[i] = record.get(campoColumn);
	        	}
			}
			Object valueFinal = transform("",value,"TransformacionNombre");
			
			if (!vfsPath.endsWith("/"))
				vfsPath += "/";
			fileName = vfsPath + valueFinal.toString();
			return fileName;
		}
		
		int maxNewsValue  = 0;
		List<CmsResource> cmsFiles = cms.getResourcesInFolder(vfsPath, CmsResourceFilter.ALL);
		for (CmsResource resource : cmsFiles) {
			fileName = resource.getName();
			if (fileName.matches(".*" + preffixName + "_[0-9]{4}.html")) {
				String auxFileName =fileName.substring(fileName.indexOf(preffixName + "_"));
				int newsValue = Integer.parseInt(auxFileName.replace(preffixName + "_","").replace(".html",""));
				if (maxNewsValue<newsValue)
					maxNewsValue=newsValue;
			}
		}

		DecimalFormat df = new DecimalFormat("0000");
		if (!vfsPath.endsWith("/"))
			vfsPath += "/";
		fileName = vfsPath + preffixName + "_" + df.format(maxNewsValue+1) + ".html";
		return fileName;
	}

	public CmsFile createResource(String resourceName) throws CmsIllegalArgumentException, CmsException{
		LOG.debug(this.getClass().getName() + " || creating resource type '" + contentType + "': " + resourceName);
		writeToLog("Creando tipo de recurso '" + contentType + "': " + resourceName);

		int typeResource = OpenCms.getResourceManager().getResourceType(contentType).getTypeId();
		CmsResource res = cms.createResource(resourceName,typeResource);
		return cms.readFile(res);
	}

	public void deleteResources(String resourceName, String idRecord) {
		LOG.debug(this.getClass().getName() + " || deleting resource type '" + contentType + "': " + resourceName);
		writeToLog("eliminando recurso " + resourceName + " identificado por " + idRecord);
		try {
			cms.deleteResource(resourceName,CmsResource.DELETE_PRESERVE_SIBLINGS );
		} catch (CmsException e) {
			LOG.error(this.getClass().getName() + " || Error borrando recurso " + resourceName,e);
		}
		
	}
	
	public List<Map<String,Object>> getTableValues(String query) throws Exception {

		QueryBuilder<List<Map<String,Object>>> queryBuilder = new QueryBuilder<List<Map<String,Object>>>(getDbPoolName());
		queryBuilder.setSQLQuery(query);
		
		ResultSetProcessor<List<Map<String,Object>>> proc = new ResultSetProcessor<List<Map<String,Object>>>() {

			private List<Map<String,Object>> rows = new ArrayList<Map<String,Object>>();

			private java.sql.ResultSetMetaData metaData = null;
			public void processTuple(ResultSet rs) throws Exception {
				try {
					
					if (metaData==null)
						metaData = rs.getMetaData();
					
					Map<String,Object> col = new HashMap<String,Object>();
					for (int j=1;j<=metaData.getColumnCount();j++)
					{
						String colName = metaData.getColumnName(j);
						String tabla = metaData.getTableName(j);
						String colType = metaData.getColumnTypeName(j);

						LOG.debug("Obteniendo columna " + tabla + "." + colName + " / tipo " + colType + " > "+ (rs.getObject(j) !=null ? rs.getObject(j).toString() : "NULL"));

						Object colValue = null;
						if (colType.toUpperCase().contains("VARCHAR")) {
							colValue = rs.getString(j);
		        		}
						else if (colType.toUpperCase().contains("TEXT")) {
							colValue = rs.getString(j);
		        		}
						else if (colType.toUpperCase().contains("BIGINT")) {
		        			colValue = rs.getFloat(j);
		        		}
		        		else if (colType.toUpperCase().contains("INT")) {
		        			colValue = rs.getInt(j);
		        		}
		        		else if (colType.toUpperCase().contains("TIMESTAMP")) {
		        			try{
			        			if (rs.getTimestamp(j)!=null)
			        					colValue = new Date(rs.getTimestamp(j).getTime());
			        			else
		        					colValue = new Date(1970,01,01);
		        			}catch(SQLException e){
		        				colValue = new Date(1970,01,01);
		        			}
		        		}
		        		else if (colType.toUpperCase().contains("DATE") || colType.toUpperCase().contains("DATETIME")) {
		        			try{
			        			if (rs.getTimestamp(j)!=null)
			        				colValue = new Date(rs.getTimestamp(j).getTime());
			        			else
		        					colValue = new Date(1970,01,01);
			        		}catch(SQLException e){
		        				colValue = new Date(1970,01,01);
			        		}
		        		}
						
						LOG.debug("Valor: " + colValue + ". (" + (colValue!=null ? colValue.getClass().toString() : "NULL") + ")");
						col.put(tabla + "." + colName, colValue);
					}
						
					rows.add(col);
				}
				catch (SQLException e) {
					LOG.error(this.getClass().getName() + " || Error ",e);
					throw new Exception("Error al ejecutar la consulta para la importacion",e);
				}
			}

			public List<Map<String,Object>> getResult() {
				return rows;
			}
		};

		return queryBuilder.execute(proc);
		
	}

	private String getDbPoolName() {
		if (dbPoolName!=null)
			return dbPoolName;
		
		dbPoolName = DbService.getEntryDBName(cms);
		if (dbPoolName==null || dbPoolName.equals(""))
			dbPoolName = CmsDbPool.OPENCMS_DEFAULT_POOL_NAME;
		
		return dbPoolName;
	}
	
	private String getDbPoolType() {
		if (dbPoolType!=null)
			return dbPoolType;		
		
		dbPoolType = DbService.getEntryDBType(cms);	
		
		return dbPoolType;
	}
	
	private Map<String,DBColumn> getTableDescription(String tabla) {
		
		Map<String,DBColumn> tableDefinition = tablesDefinition.get(tabla);
		if (tableDefinition==null) {
			String dbPoolName = getDbPoolName();
			String dbPoolType = getDbPoolType();
			
			DbService dbService = new DbService(dbPoolName, dbPoolType);
			try {
				tableDefinition = dbService.getMapTableDescription(tabla);
				tablesDefinition.put(tabla,tableDefinition);
			} catch (Exception e) {
				LOG.error(this.getClass().getName() + " || Error ",e);
			}
		}
		return tableDefinition;
	}

	private Map<String,DBColumn>[] getTablesDescriptions(String[] tablas) {
		Map<String,DBColumn>[] tables = (Map<String,DBColumn>[]) new Map[tablas.length];
		int j=0;
		for (String tabla : tablas) {
			Map<String,DBColumn> tableDefinition = tablesDefinition.get(tabla);
			if (tableDefinition==null) {
				String dbPoolName = getDbPoolName();
				String dbPoolType = getDbPoolType();
				
				DbService dbService = new DbService(dbPoolName, dbPoolType);
				try {
					tableDefinition = dbService.getMapTableDescription(tabla);
					tablesDefinition.put(tabla,tableDefinition);
				} catch (Exception e) {
					LOG.error(this.getClass().getName() + " || Error ",e);
				}
			}
			tables[j] = tableDefinition;
		}
		return tables;
	}

	public String getTableNames(String path) {
		CmsXmlContentValueSequence elementSequence = importDefinitionContent.getValueSequence(path + "Tabla", contentLocale);
		int elementCount = elementSequence.getElementCount();
        // loop through elements
		String tableNames = importDefinitionContent.getStringValue(cms, path + "Tabla[1]", contentLocale);
        for (int j = 1; j < elementCount; j++) {
        	tableNames += "," + importDefinitionContent.getStringValue(cms,path + "Tabla[" + (j+1) + "]", contentLocale);
        }
        return tableNames;
	};

	public String[] getTablesNames(String path) {
		CmsXmlContentValueSequence elementSequence = importDefinitionContent.getValueSequence(path + "Tabla", contentLocale);
		int elementCount = elementSequence.getElementCount();
        
		String[] tablesNames = new String[elementCount];
		
        for (int j = 0; j < elementCount; j++) {
        	tablesNames[j] += importDefinitionContent.getStringValue(cms,path + "Tabla[" + (j+1) + "]", contentLocale);
        }
        return tablesNames;
	}
	
	public String getDBQuery(String path, Map<String,Object> values) {
		
		String tabla = getTableNames(path);
		
		String query = "SELECT * FROM " + tabla;
		
		String whereCondition = getWhereClause(path, values,tabla.split(","));
        
        if (!whereCondition.equals("")) {
        	query += " WHERE " + whereCondition.replaceFirst(" AND ", "") ;
        }
        
        String orderClause = getOrderClause(path, values);
        if (!orderClause.equals("")) {
        	query += " ORDER BY " + orderClause.replaceFirst(",", "") ;
        }        
        
        if (!path.equals("")) {
			cantidad = importDefinitionContent.getStringValue(cms, path + "Cantidad", contentLocale);
			offset = importDefinitionContent.getStringValue(cms, path + "Offset", contentLocale);
        }
		if (!cantidad.trim().equals("")) {
			query += " LIMIT ";
			
			if (!offset.trim().equals("")) {
				query += offset + ", ";
			}
			
			query += cantidad;
			
		}
		
		LOG.debug(this.getClass().getName() + " || Query to execute: " + query );
		return query;
	}
	
	public String getSQLDBQuery(String path, Map<String,Object> values) {
		
		String tabla = getTableNames(path);
		
		String query = "SELECT * FROM " + tabla;
		
		String whereCondition = getSQLWhereClause(path, values,tabla.split(","), tabla);
        
        if (!whereCondition.equals("")) {
        	query += " WHERE " + whereCondition.replaceFirst(" AND ", "") ;
        }
        
        String orderClause = getOrderClause(path, values);
        if (!orderClause.equals("")) {
        	query += " ORDER BY " + orderClause.replaceFirst(",", "") ;
        }        
        
        if (!path.equals("")) {
			cantidad = importDefinitionContent.getStringValue(cms, path + "Cantidad", contentLocale);
			offset = importDefinitionContent.getStringValue(cms, path + "Offset", contentLocale);
        }
                
		if (!cantidad.trim().equals("")) {
			
			if (!offset.trim().equals("")) {
				query += " OFFSET " + offset + " ROWS ";
			}
			
			query += " FETCH NEXT " + cantidad + " ROWS ONLY";

			
		}
		
		LOG.debug(this.getClass().getName() + " || Query to execute: " + query );
		return query;
		
		/*
		String tabla = getTableNames(path);
		
		if (!path.equals("")) {
			cantidad = importDefinitionContent.getStringValue(cms, path + "Cantidad", contentLocale);
			offset = importDefinitionContent.getStringValue(cms, path + "Offset", contentLocale);
        }		
	
		String query = "";
		if (!cantidad.trim().equals("")) {
			if(!offset.trim().equals("")){
				query = "SELECT TOP "+ cantidad +" t.* FROM " + 
					//"( SELECT *, ROW_NUMBER() over ( order by "+ campoIdRecord +" ) as RowNum from "+ tabla +" )as t";
					"( SELECT *, ROW_NUMBER() over ( order by "+ campoIdRecord +" ) as RowNum from "+ tabla +" ";
			}else{
				query = "SELECT TOP "+ cantidad +" * FROM " + tabla;
			}
		}else{
			if(!offset.trim().equals("")){
				query = "SELECT t.* FROM " + 
					//"( SELECT *, ROW_NUMBER() over ( order by "+ campoIdRecord +" ) as RowNum from "+ tabla +" )as t";
					"( SELECT *, ROW_NUMBER() over ( order by "+ campoIdRecord +" ) as RowNum from "+ tabla +" ";
			}else{
				query = "SELECT * FROM " + tabla;
			}
		}
		
		String whereCondition = "";
        
		//PLEASE CHECK HERE----------------------------------------------------------
		if(!offset.trim().equals("")){
			//whereCondition = getSQLWhereClause(path, values,tabla.split(","), "t", offset, tabla, true);
			whereCondition = getSQLWhereClause(path, values,tabla.split(","), "t", offset, tabla, false);
		}else{
			//whereCondition = getWhereClause(path, values,tabla.split(","));
			whereCondition = getSQLWhereClause(path, values,tabla.split(","), "t", offset, tabla, false);
		}
		
        if (!whereCondition.equals("")) {
        	query += " WHERE " + whereCondition.replaceFirst(" AND ", "") ;
        }
        */
		
        /*esto es nuevo*/
    	/*
		if(!offset.trim().equals("")){
    		query += " )as t WHERE RowNum between "+offset+" AND (select count(*) from "+tabla+") ";
    	} 
    	
        
        String orderClause = getOrderClause(path, values);
        if (!whereCondition.equals("")) {
        	orderClause = orderClause.replaceFirst(",", "");
        	if(!offset.trim().equals("")){
        		orderClause = orderClause.replace(tabla, "t");
        	}        	
        	query += " ORDER BY " + orderClause;
        }     
		
		LOG.debug(this.getClass().getName() + " || Query to execute: " + query );
		return query;
		*/
	}

	private String getOrderClause(String path, Map<String, Object> values) {
		String orderBy = "";
		CmsXmlContentValueSequence elementSequence = importDefinitionContent.getValueSequence(path + "Order", contentLocale);
		int elementCount = elementSequence.getElementCount();
        // loop through elements
        for (int j = 0; j < elementCount; j++) {
            //I_CmsXmlContentValue value = elementSequence.getValue(j);
        	String orderColumn = importDefinitionContent.getStringValue(cms,path + "Order[" + (j+1) + "]/Campo", contentLocale);
        	String orderDirection = importDefinitionContent.getStringValue(cms,path + "Order[" + (j+1) + "]/Direccion", contentLocale);
        	orderBy = "," + orderColumn + " " + orderDirection;
        }
        
        return orderBy;
	}
	
	private String getWhereClause(String path, Map<String, Object> values, String[] queryTables) {
		String whereCondition = "";
		CmsXmlContentValueSequence elementSequence = importDefinitionContent.getValueSequence(path + "Filtro", contentLocale);
		int elementCount = elementSequence.getElementCount();
        // loop through elements
        for (int j = 0; j < elementCount; j++) {
            //I_CmsXmlContentValue value = elementSequence.getValue(j);
        	
        	
        	whereCondition += " AND ";

        	String wherePartCondition = "";
        	
        	I_CmsXmlContentValue campoOrigenContent = importDefinitionContent.getValue(path + "Filtro[" + (j+1) + "]/CampoOrigen", contentLocale);
        	if (campoOrigenContent!=null) {
        		String campoOrigen = campoOrigenContent.getStringValue(cms);
        		String[] tableColumn = campoOrigen.split("\\.");
            	
        		if (ArrayUtils.contains(queryTables, tableColumn[0]))
        			wherePartCondition += campoOrigen;
        		else {
	        		Object origenValue = values.get(campoOrigen);
	        		
	        		Map<String,DBColumn> table = getTableDescription(tableColumn[0]);
	        		DBColumn column = table.get(tableColumn[1]);
	        		
	        
	        		
	        		
	        		
	        		if (column.getType().toLowerCase().contains("varchar") || column.getType().toLowerCase().contains("text")) {
	        			wherePartCondition += "'" + (String)origenValue + "'";
	        		}
	        		else if (column.getType().toLowerCase().contains("int")) {
	        			wherePartCondition += (String)origenValue.toString();
	        		}
	        		else if (column.getType().toLowerCase().contains("timestamp")) {
	        			wherePartCondition += "'" + sdf.format((Date)origenValue) + "'";
	        		}
	        		else if (column.getType().toLowerCase().contains("date")) {
	        			wherePartCondition += "'" + sdf.format((Date)origenValue) + "'";
	        		}        		
        		}
        	}
        	
        	String comparator = getComparatorInFilter(path, j);
        	wherePartCondition += comparator;
    		
        	I_CmsXmlContentValue campoDestinoContent = importDefinitionContent.getValue(path + "Filtro[" + (j+1) + "]/CampoDestino", contentLocale);
        	if (campoDestinoContent!=null) {
        		String campoDestino = campoDestinoContent.getStringValue(cms);
        		String[] tableColumn = campoDestino.split("\\.");
        	
        		if (ArrayUtils.contains(queryTables, tableColumn[0]))
        			wherePartCondition += campoDestino;
        		else {
	        		Object destinoValue = values.get(campoDestino);
	        		
	        		Map<String,DBColumn> table = getTableDescription(tableColumn[0]);
	        		DBColumn column = table.get(tableColumn[1]);
	        		
	        		if (column.getType().toLowerCase().contains("varchar") || column.getType().toLowerCase().contains("text")) {
	        			wherePartCondition += "'" + (String)destinoValue + "'";
	        		}
	        		else if (column.getType().toLowerCase().contains("int")) {
	        			wherePartCondition += (String)destinoValue.toString();
	        		}
	        		else if (column.getType().toLowerCase().contains("timestamp")) {
	        			wherePartCondition += "'" + sdf.format((Date)destinoValue) + "'";
	        		}
	        		else if (column.getType().toLowerCase().contains("date")) {
	        			wherePartCondition += "'" + sdf.format((Date)destinoValue) + "'";
	        		}        		
        		}
        		whereCondition += wherePartCondition;
        	}
        	

    		CmsXmlContentValueSequence valorSequence = importDefinitionContent.getValueSequence(path + "Filtro[" + (j+1) + "]/Valor", contentLocale);
    		int valorCount = valorSequence.getElementCount();
            if (valorCount==1) {
            	whereCondition += wherePartCondition + importDefinitionContent.getStringValue(cms,path + "Filtro[" + (j+1) + "]/Valor[1]", contentLocale);
            }
            else if (valorCount>1) {
	    		// loop through elements
            	whereCondition += "( ";
	            for (int i = 0; i < valorCount; i++) {
	            	whereCondition += wherePartCondition + importDefinitionContent.getStringValue(cms,path + "Filtro[" + (j+1) + "]/Valor[" + (i+1) + "]", contentLocale);
	            	if (i<valorCount-1)
	            		if (comparator.equals(" <> "))
	            			whereCondition += " AND ";
	            		else
	            			whereCondition += " OR ";
	            }
	            whereCondition += ")";
            }

        }
		return whereCondition;
	}
	
	private String getSQLWhereClause(String path, Map<String, Object> values, String[] queryTables, String tableName) {
		String whereCondition = "";
/*		if(offset){
			whereCondition = " AND " + alias + "." + "RowNum between "+offSet+" AND (select count(*) from "+tableName+") ";
		}else{
			whereCondition = "";
		}
*/		
		
		CmsXmlContentValueSequence elementSequence = importDefinitionContent.getValueSequence(path + "Filtro", contentLocale);
		int elementCount = elementSequence.getElementCount();
        // loop through elements
        for (int j = 0; j < elementCount; j++) {
            //I_CmsXmlContentValue value = elementSequence.getValue(j);
        	
        	
        	whereCondition += " AND ";

        	String wherePartCondition = "";
        	
        	I_CmsXmlContentValue campoOrigenContent = importDefinitionContent.getValue(path + "Filtro[" + (j+1) + "]/CampoOrigen", contentLocale);
        	if (campoOrigenContent!=null) {
        		String campoOrigen = campoOrigenContent.getStringValue(cms);
        		String[] tableColumn = campoOrigen.split("\\.");
            	
        		if (ArrayUtils.contains(queryTables, tableColumn[0])){
        			//if(offset){
        			//	campoOrigen = campoOrigen.replace(tableName, alias);
        			//}
        			wherePartCondition += campoOrigen;
        		}
        		else {
	        		Object origenValue = values.get(campoOrigen);
	        		if(origenValue == null){
	        			campoOrigen = campoOrigen.replace(importDefinitionContent.getStringValue(cms,"Tabla", contentLocale), "");
	        			origenValue = values.get(campoOrigen);
	        		}
	        		
	        		
	        		Map<String,DBColumn> table = getTableDescription(tableColumn[0]);
	        		
	        		
	        		DBColumn column = table.get(tableColumn[1]);		
	        		
	        		LOG.debug("getSqlWhereClause: campoOrigen: " + origenValue + " > " + column.getType().toLowerCase() );
	        		if (column.getType().toLowerCase().contains("varchar") || column.getType().toLowerCase().contains("text")) {
	        			//if(offset){
	        			//	wherePartCondition += alias + "." +"'" + (String)origenValue + "'";
	        			//}else{
	        				wherePartCondition += "'" + (String)origenValue + "'";
	        			//}
	        		}
	        		else if (column.getType().toLowerCase().contains("int")) {
	        			//if(offset){
	        				wherePartCondition += (String)origenValue.toString();
	        			//}else{
	        			//	wherePartCondition += (String)origenValue.toString();
	        			//}	        			
	        		}
	        		else if (column.getType().toLowerCase().contains("timestamp")) {
	        			//if(offset){
	        			//	wherePartCondition += alias + "." +"'" + sdf.format((Date)origenValue) + "'";
	        			//}else{
	        				wherePartCondition += "'" + sdf.format((Date)origenValue) + "'";
	        			//}	        			
	        		}
	        		else if (column.getType().toLowerCase().contains("date")) {
	        			//if(offset){
	        			//	wherePartCondition += "'" + sdf.format((Date)origenValue) + "'";
	        			//}else{
	        				wherePartCondition += "'" + sdf.format((Date)origenValue) + "'";
	        			//}	        			
	        		}        		
        		}
        	}
        	
        	String comparator = getComparatorInFilter(path, j);
        	wherePartCondition += comparator;
    		
        	I_CmsXmlContentValue campoDestinoContent = importDefinitionContent.getValue(path + "Filtro[" + (j+1) + "]/CampoDestino", contentLocale);
        	if (campoDestinoContent!=null) {
        		String campoDestino = campoDestinoContent.getStringValue(cms);
        		String[] tableColumn = campoDestino.split("\\.");
        	
        		if (ArrayUtils.contains(queryTables, tableColumn[0])){
        			//if(offset){
        			//	campoDestino = campoDestino.replace(tableName, alias);
        			//}        			
        			wherePartCondition += campoDestino;
        		}
        		else {
        			
        			Object destinoValue = values.get(campoDestino);
	        		if(destinoValue == null){
	        			campoDestino = campoDestino.replace(importDefinitionContent.getStringValue(cms,"Tabla", contentLocale), "");
	        			destinoValue = values.get(campoDestino);
	        		}
        			
	        		
	        		Map<String,DBColumn> table = getTableDescription(tableColumn[0]);
	        		DBColumn column = table.get(tableColumn[1]);
	
	        		LOG.debug("getSqlWhereClause: campoOrigen: " + destinoValue + " > " + column.getType().toLowerCase() );
	        		
	        		if (column.getType().toLowerCase().contains("varchar") || column.getType().toLowerCase().contains("text")) {
	        			wherePartCondition += "'" + (String)destinoValue + "'";
	        		}
	        		else if (column.getType().toLowerCase().contains("int")) {
	        			wherePartCondition += (String)destinoValue.toString();
	        		}
	        		else if (column.getType().toLowerCase().contains("timestamp")) {
	        			wherePartCondition += "'" + sdf.format((Date)destinoValue) + "'";
	        		}
	        		else if (column.getType().toLowerCase().contains("date")) {
	        			wherePartCondition += "'" + sdf.format((Date)destinoValue) + "'";
	        		}        		
        		}
        		whereCondition += wherePartCondition;
        	}
        	
    		CmsXmlContentValueSequence valorSequence = importDefinitionContent.getValueSequence(path + "Filtro[" + (j+1) + "]/Valor", contentLocale);
    		int valorCount = valorSequence.getElementCount();
            if (valorCount==1) {
            	whereCondition += wherePartCondition + importDefinitionContent.getStringValue(cms,path + "Filtro[" + (j+1) + "]/Valor[1]", contentLocale);
            }
            else if (valorCount>1) {
	    		// loop through elements
            	whereCondition += "( ";
	            for (int i = 0; i < valorCount; i++) {
	            	String valor = importDefinitionContent.getStringValue(cms,path + "Filtro[" + (j+1) + "]/Valor[" + (i+1) + "]", contentLocale);
	            	//if(valor.equals("OLDRECORDID")){
	            	//	valor = campoIdRecord;
	            	//}	            		
	            	whereCondition += wherePartCondition + importDefinitionContent.getStringValue(cms,path + "Filtro[" + (j+1) + "]/Valor[" + (i+1) + "]", contentLocale);
	            	if (i<valorCount-1)
	            		if (comparator.equals(" <> "))
	            			whereCondition += " AND ";
	            		else
	            			whereCondition += " OR ";
	            }
	            whereCondition += ")";
            }

        }
		return whereCondition;
	}

	private String getComparatorInFilter(String path, int j) {
		String comparador = importDefinitionContent.getStringValue(cms, path + "Filtro[" + (j+1) + "]/Comparador", contentLocale);
		if (comparador.equals("equal")) {
			return " = ";
		}
		else if (comparador.equals("greater")) {
			return " > ";       		
		}
		else if (comparador.equals("greater or equal")) {
			return " >= ";        		
		}
		else if (comparador.equals("lower")) {
			return " < ";
		}
		else if (comparador.equals("lower or equal")) {
			return " <= ";
		}
		else if (comparador.equals("distinct")) {
			return " <> ";
		}
		else if (comparador.equals("like")) {
			return " LIKE ";
		} else if (comparador.equals("not like")) {
			return " NOT LIKE ";
		} else if (comparador.equals("in")) {
			return " IN ";
		}
		
		return "";
	}

	/** * This method ensures that the output String has only
	    * valid XML unicode characters as specified by the
	    * XML 1.0 standard. For reference, please see
	    * <a href="http://www.w3.org/TR/2000/REC-xml-20001006#NT-Char">the     * standard</a>. This method will return an empty     * String if the input is null or empty.     *     * @param in The String whose non-valid characters we want to remove.     * @return The in String, stripped of non-valid characters.     */
	public String stripNonValidXMLCharacters(String in) {
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
}
