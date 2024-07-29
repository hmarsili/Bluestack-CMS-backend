package org.opencms.search;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.report.CmsLogReport;
import org.opencms.report.I_CmsReport;
import org.opencms.scheduler.I_CmsScheduledJob;
import org.opencms.search.fields.CmsSearchField;

public class TfsUpdateIndexSourceJob  implements I_CmsScheduledJob {

	private static final Log LOG = CmsLog.getLog(TfsUpdateIndexSourceJob.class);

	private String resultados = "";
	
	private final String FORWARD = "forward"; 
	private final String REMOVE = "remove"; 
	private final String ADD = "add";
	
	@Override
	public String launch(CmsObject cms, Map parameters) throws Exception {
		
		String indexSource = (String) parameters.get("indexSource");
		String typeNews = (String) parameters.get("typeNews");
		
		String _buildHistorical = (String) parameters.get("buildHistorical");
		boolean buildHistorical=false;
		if (_buildHistorical!=null)
			buildHistorical = Boolean.parseBoolean(_buildHistorical);
			
		
		resultados = "Ejecutando actualizacion de recursos de indexsource " + indexSource;
		
		if (indexSource==null || indexSource.trim().equals("")) {
			return resultados+=" - SIN EFECTO: IndexSource no especificado";
		}
		String sitePath = cms.getRequestContext().getSiteRoot();
		
		ExtendedIndexManager eindexManager = new ExtendedIndexManager();
		
		String[] types = typeNews.split(";");
		
		for (String type : types) {

			Map<Integer, List<String>> folderToHistorical = new HashMap<Integer,List<String>>();
			Set<Integer> yearInHistorical = new HashSet<Integer>();
		
			String basePath = (String) parameters.get(type + "_basePath");
			if (basePath==null)	basePath = type;
			
			String config = (String) parameters.get(type + "_configuration");
			if (config==null) config = "QWERTYUIOP";
			
			String subFolderFormat = getParamValue(parameters,"subFolderFormat",type,config);
			String action = getParamValue(parameters,"action",type,config);
			String unit = getParamValue(parameters,"unit",type,config);
			String range = getParamValue(parameters,"range",type,config);
						
			String path = sitePath + "/" + basePath;

			int field = getDateUnit(unit);

			if (action==null || action.equals(""))
				action = FORWARD;
			
			action = action.trim().toLowerCase();
			
			if (!action.equals(ADD) && !action.equals(FORWARD) && !action.equals(REMOVE))
				resultados+="\nAction " + action + " no reconocida para tipo " + type;
			
			if (action.equals(ADD) || action.equals(FORWARD)) {
				
				String folder = getFolderToAdd(subFolderFormat, path, field);

				resultados+="\nAgregando carpeta " + folder + " a " + indexSource;

				try {
				eindexManager.addFolderToSource(indexSource, folder);
				}
				catch (Exception e)
				{
					resultados+="\n failed: " + e.getMessage();
				}
			}

			if (action.equals(REMOVE) || action.equals(FORWARD)) {
				String folder = getFolderToRemove(subFolderFormat, range, path, field);
				
				if (buildHistorical) {
					
					int year = getYearInRemove(range,field);
					
					List<String> resources = folderToHistorical.get(year);
					if (resources==null)
						resources = new ArrayList<String>();
					
					resources.add(folder);
					folderToHistorical.put(year, resources);
					
					yearInHistorical.add(year);
				}
				
				resultados+="\nQuitando carpeta " + folder + " a " + indexSource;
				
				try {
					eindexManager.removeFolderFromSource(indexSource, folder);
				}
				catch (Exception e)
				{
					resultados+="\n failed: " + e.getMessage();
				}
			}
			eindexManager.updateIndexDefinition();
			if (buildHistorical) {
				CmsSearchIndexSource idxSource = OpenCms.getSearchManager().getIndexSource(indexSource);
				
				List<CmsSearchIndexSource> affectedIndexSources = new ArrayList<CmsSearchIndexSource>();
				for (Integer year : yearInHistorical) {
					
					List<String> resources = folderToHistorical.get(year);
					
					//Verifico si tengo que crear los index sources historicos
					String sourceHistoricalName = indexSource + "_HISTORICAL_" + year;
					CmsSearchIndexSource historicalSource = OpenCms.getSearchManager().getIndexSource(sourceHistoricalName);
					if (historicalSource==null) {
						historicalSource = eindexManager.createIndexSource(sourceHistoricalName, 
								resources, 
								idxSource.getDocumentTypes(), 
								idxSource.getIndexerClassName());

						resultados+="\nCreando el index source " + sourceHistoricalName + " con carpetas " + String.join(",", resources);

						eindexManager.updateIndexDefinition();
					}
					else {
						// Si ya existe agrego sus recursos.
						resultados+="\nAgregando al index source " + sourceHistoricalName + " con carpetas " + String.join(",", resources);

						for (String folder : resources) {
						try {
							eindexManager.addFolderToSource(sourceHistoricalName, folder);
							}
							catch (Exception e)
							{
								resultados+="\n failed: " + e.getMessage();
							}
						}
					}
					
					affectedIndexSources.add(historicalSource);
				}
				
				LOG.debug("Por actualizar indices...");
				List<String> indexNamesModified = new ArrayList<String>();
				//Obtengo los indices que tienen el index source actualizado
				List<CmsSearchIndex> allIndexes = new ArrayList<CmsSearchIndex>(OpenCms.getSearchManager().getSearchIndexes());
				for (CmsSearchIndex idx : allIndexes) { 
					LOG.debug("indice " + idx.getName() + ": historical=" + idx.isBuildHistorical());
					if (!idx.isBuildHistorical())
						continue;
					boolean hasSource = false; 
					for (String source : idx.getSourceNames()) {
						if (source.equals(indexSource)) {
							hasSource = true;
							indexNamesModified.add(idx.getName());
						}
					}	
					LOG.debug("indice " + idx.getName() + ": has source " + indexSource + "=" + hasSource);
					if (hasSource) {
						for (Integer year : yearInHistorical) {
							String sourceHistoricalName = indexSource + "_HISTORICAL_" + year;
							String IndexHistoricalName = idx.getName() + "_HISTORICAL_" + year;
							
							CmsSearchIndex idxHistorical = OpenCms.getSearchManager().getIndex(IndexHistoricalName);
							if (idxHistorical==null)
							{
								resultados+="\nCreando el index " + IndexHistoricalName + " con index source " + sourceHistoricalName;
 
									//No existe el indice historico, lo genero y lo indexo.
								List<String> sources = new ArrayList<String>();
								sources.add(sourceHistoricalName);
								idxHistorical = eindexManager.creteIndex(IndexHistoricalName, 
										idx.getProject(), 
										CmsSearchIndex.REBUILD_MODE_MANUAL, 
										idx.getFieldConfigurationName(), 
										sources, 
										idx.getConfiguration(),
										idx.getLocale());
								
								eindexManager.updateIndexDefinition();
								
								List<String> idxs = new ArrayList<>();
								idxs.add(IndexHistoricalName);
								I_CmsReport report = new CmsLogReport(Locale.ENGLISH, getClass());
								OpenCms.getSearchManager().rebuildIndexes(idxs, report);
								
							}
							else {
								//ya existe, solo copio los recursos de un indice al otro.
								resultados+="\nAgregando al index " + IndexHistoricalName + " los recursos historicos";

								List<String> resources = folderToHistorical.get(year);
								for (String folder : resources) {
									eindexManager.copyResourcesFromIndexes(cms, idx.getName(),idxHistorical.getName(),CmsSearchField.FIELD_NAME + ":\"" + folder + "\"",true,true);
								}
							}

							resultados+="\nQuitando del index " + idx.getName() + " los recursos historicos";

							//Quita las noticias del indice original
							List<String> resources = folderToHistorical.get(year);
							for (String folder : resources) {
								eindexManager.removeResourcesFromIndex(idx.getName(),CmsSearchField.FIELD_NAME + ":\"" + folder + "\"",true,true);
							}

						}
					}
				
				}
			}
			
			eindexManager.updateIndexDefinition();
		}
		
		
				
		return resultados;
	}

	private String getParamValue(Map parameters, String paramName, String type, String config) {
		String value = (String) parameters.get(type + "_" + paramName);
		if (value==null)
			value = (String) parameters.get("config_" + config + "_" + paramName);
		
		return value;
	}

	private int getYearInRemove(String range, int field) {
		Date date = new Date();
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		
		int amount = -1 * Integer.parseInt(range);
		calendar.add(field, amount);
		
		return calendar.get(Calendar.YEAR);
	}

	private String getFolderToRemove(String subFolderFormat, String range, String path, int field) {
		Date date = new Date();
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		
		int amount = -1 * Integer.parseInt(range);
		calendar.add(field, amount);
		SimpleDateFormat format = new SimpleDateFormat(subFolderFormat);
		String folderDate = format.format(calendar.getTime());
		
		String folder = path + "/" + folderDate;
		return folder;
	}

	private String getFolderToAdd(String subFolderFormat, String path, int field) {
		Date date = new Date();
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		
		calendar.add(field, 1);
		SimpleDateFormat format = new SimpleDateFormat(subFolderFormat);
		String folderDate = format.format(calendar.getTime());
		
		String folder = path + "/" + folderDate;
		return folder;
	}

	private int getDateUnit(String unit) {
		int field = Calendar.YEAR;
		if (unit!=null) {
			if (unit.equals("y"))
				field = Calendar.YEAR;
			else if (unit.equals("M"))
				field = Calendar.MONTH;
			else if (unit.equals("d"))
				field = Calendar.DAY_OF_YEAR;

		}
		return field;
	}

}
