package com.tfsla.diario.ediciones.services;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jakarta.servlet.http.Cookie;

import org.apache.commons.logging.Log;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.main.TfsContext;
import org.opencms.report.CmsLogReport;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import com.tfsla.diario.ediciones.exceptions.UndefinedTipoEdicion;
import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.opencms.dev.collector.DateFolder;
import com.tfsla.trivias.data.TfsTriviasDAO;
import com.tfsla.trivias.model.TfsTrivia;
import com.tfsla.utils.CmsResourceUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class TriviasService extends baseService {

	
	public static final String TRIVIAS_PATH = "trivias";
	private static String COOKIE_NAME = "TRIVIATFS";
	
	private static final Log LOG = CmsLog.getLog(TriviasService.class);

	
	/**
	 * Crea una trivia en el directorio indicado con la numeracion correspondiente.
	 * @param obj
	 * @param location
	 * @return Path y nombre de la trivia creada.
	 * @throws CmsIllegalArgumentException
	 * @throws CmsException
	 */
	public String crearTrivia(CmsObject cms,String location) throws CmsIllegalArgumentException, CmsException
	{
		int shift=1;
		String fileName = "";
		boolean done = false;
		while (!done && shift <=10) {
			try {
				fileName = getNextTriviaName(cms,location,shift);

				int triviaType = OpenCms.getResourceManager().getResourceType("trivia").getTypeId();
				cms.createResource(fileName,triviaType);
		
				done=true;
				
			}
			catch (CmsException ex) {
				LOG.error("Intento " + shift + " - Error al crear la trivia " + fileName,ex);
				if (shift>=10)
					throw ex;
			}
			shift+=2;
		}

		return fileName;
	}
	
	public String crearTrivia(CmsObject cms, int tipoEdicion, int numeroEdicion ) throws CmsIllegalArgumentException, CmsException, UndefinedTipoEdicion {
		TipoEdicionService tService = new TipoEdicionService();
		TipoEdicion tEdicion = tService.obtenerTipoEdicion(tipoEdicion);
		
		String path = tEdicion.getBaseURL().replace("/contenidos/", "");
		
		path = cms.getRequestContext().removeSiteRoot(path);
		
		path +=  TRIVIAS_PATH + "/";
		
		return crearTrivia(cms, path);
	}
	
	private String getNextTriviaName(CmsObject obj,String location,int shift) throws CmsException {
		String fileName="";
		
		String[] niveles = location.split("/");
		
		//Solo si el contenido es de publicaciones de tipo ONLINE y ONLINE ROOT
		if (niveles.length <= 3)
			location = new DateFolder(location, true).getTodayFolder(obj);

		fileName = createTriviaName(obj, location,"trivia", shift);

		return fileName;
	}

	private String createTriviaName(CmsObject obj, String location, String fileNamePreffix, int shift)throws CmsException {
		
		String fileName;
		int maxNewsValue  = 0;
		List cmsFiles = obj.getResourcesInFolder(location, CmsResourceFilter.ALL);
		
		for (Iterator it = cmsFiles.iterator(); it.hasNext();) {
			CmsResource resource = (CmsResource) it.next();
			fileName = resource.getName();
			if (fileName.matches(".*" + fileNamePreffix + "_[0-9]{4}.html")) {
				String auxFileName =fileName.substring(fileName.indexOf(fileNamePreffix + "_"));
				int newsValue = Integer.parseInt(auxFileName.replace(fileNamePreffix + "_","").replace(".html",""));
				if (maxNewsValue<newsValue)
					maxNewsValue=newsValue;
			}
		}

		DecimalFormat df = new DecimalFormat("0000");
		fileName = location + fileNamePreffix + "_" + df.format(maxNewsValue+shift) + ".html"; 
		return fileName;
	}
	
	public String publishTrivias(CmsObject cms,List trivias){
		
		String msg = "";
		
		try {	
			
			List<CmsResource> publishList = new ArrayList<CmsResource>();
            Iterator it =  trivias.iterator();
            
            while(it.hasNext()) {
				String trivia = (String)it.next();
				
				if (!cms.getLock(trivia).isUnlocked()) {
				     if(!cms.getLock(trivia).isOwnedBy(cms.getRequestContext().currentUser())){
					      cms.changeLock(trivia);
				    }
				} else {
				     cms.lockResource(trivia);
				}
				
				CmsResource  resource = cms.readResource(trivia);
				publishList.add(resource);
			}
			
			OpenCms.getPublishManager().publishProject(cms, new CmsLogReport(Locale.getDefault(), this.getClass()), OpenCms.getPublishManager().getPublishList(cms,publishList, false));
			
		} catch (CmsException e1) {
			CmsLog.getLog(this).error("Error al publicar Trivias: "+e1.getMessage());
			msg = "ERROR: "+e1.getMessage();
		}
		
		return msg;
	}
	
	public String changeStatusTrivia(CmsObject cms,String triviaPath, String newStatus){
		
		String msg = "";
		
		try {
			if (!cms.getLock(triviaPath).isUnlocked()) {
			     if(!cms.getLock(triviaPath).isOwnedBy(cms.getRequestContext().currentUser())){
				      cms.changeLock(triviaPath);
			    }
			} else {
			     cms.lockResource(triviaPath);
			}
			
			CmsFile file = cms.readFile(triviaPath,CmsResourceFilter.ALL);
			
			CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, file);
			content.setAutoCorrectionEnabled(true);
			content.correctXmlStructure(cms);
			
			content.getValue("status", Locale.ENGLISH).setStringValue(cms, newStatus);
			
			file.setContents(content.marshal());
			cms.writeFile(file);	
			
		} catch (CmsException e1) {
			CmsLog.getLog(this).error("Error al cambiar el estado de una trivia: "+e1.getMessage());
			msg = "ERROR: "+e1.getMessage();
		}
		
		return msg;
		
	}
	
	public void saveTrivia(CmsObject cms, TfsTrivia trivia)throws CmsException {
		
		if(trivia.getPath()!=null && !trivia.getPath().equals("") && trivia.getPath().indexOf("~")<0){
			
			TfsTriviasDAO triviaDAO = new TfsTriviasDAO();
			int idTrivia = 0;
			
			try {
				idTrivia = triviaDAO.getIDTrivia(trivia.getPath(), trivia.getSite(), trivia.getPublication());
				
				if(idTrivia>0){
					trivia.setIdTrivia(idTrivia);
					triviaDAO.updateCantUsersTrivia(idTrivia);
				}else{
					idTrivia = triviaDAO.insertTrivia(trivia);
					trivia.setIdTrivia(idTrivia);
				}
				
				if(trivia.getResultName()!=null && !trivia.getResultName().equals("")){
					
					boolean existTriviaResult = triviaDAO.existTriviaResult(idTrivia, trivia.getResultName());
					
					if(existTriviaResult)
						triviaDAO.updateCantUsersTriviaResults(idTrivia, trivia.getResultName());
					else
						triviaDAO.insertTriviaResults(idTrivia, trivia.getResultName());
				}
				
				if(trivia.getStoreResults()){		
					Date currentDate = new Date();
					long dateModified = currentDate.getTime();
					
					String remoteIp  = cms.getRequestContext().getRemoteAddress();
					
					triviaDAO.insertUserResults(trivia, dateModified, remoteIp);
				}
				
		       int cookieMaxAge = 172800;  // 2 dias
				
				Cookie cookie = new Cookie(trivia.getPath(), COOKIE_NAME );
				cookie.setMaxAge(cookieMaxAge);  
				cookie.setPath("/"); 
				TfsContext.getInstance().getResponse().addCookie(cookie);

				/*CmsResourceUtils.forceLockResource(cms,trivia.getPath());
				
				int cantUsers = triviaDAO.getCantUsers(trivia.getPath());
		
				CmsProperty prop = new CmsProperty();
				 prop.setName("triviaUsers");
				 
				 prop.setValue(String.valueOf(cantUsers), CmsProperty.TYPE_INDIVIDUAL);
				cms.writePropertyObject(trivia.getPath(), prop);
					
				 CmsResourceUtils.unlockResource(cms,trivia.getPath(), false);*/
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public int getCantUsers (String triviaPath) {
		TfsTriviasDAO triviaDAO = new TfsTriviasDAO();
		
		try {
			return triviaDAO.getCantUsers(triviaPath);
		} catch (Exception e) {
			LOG.error("Error al buscar cantidad de usuarios en trivias: trivia: " + triviaPath, e);
		}
		return 0;
	}
	
	public boolean canPlayTrivia(CmsObject cms, TfsTrivia trivia){
		
		boolean canPlay = true;
		
		if(!trivia.getMultipleGame()){ 
			
			if(trivia.getStoreResults()){
				
				TfsTriviasDAO triviaDAO = new TfsTriviasDAO();
				
				if(trivia.getRegisteredUser()){
					
					    try {
					    	int idTrivia = triviaDAO.getIDTrivia(trivia.getPath(),trivia.getSite(),trivia.getPublication());
							canPlay = !triviaDAO.existUserTriviaResult(idTrivia, trivia.getUserId());
						} catch (Exception e) {
							e.printStackTrace();
						}
					
				}else{
					
					try {
				    	int idTrivia = triviaDAO.getIDTrivia(trivia.getPath(),trivia.getSite(),trivia.getPublication());
				    	String remoteIp  = cms.getRequestContext().getRemoteAddress();
						canPlay = !triviaDAO.existIPTriviaResult(idTrivia, remoteIp);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}else{
				
				Cookie[] cookies = TfsContext.getInstance().getRequest().getCookies();
			 	   
		        if (cookies != null){
				    for (int i = 0; i < cookies.length; i++) {
					 Cookie cookie = cookies[i];
					 
					 if (cookie.getName().equals(trivia.getPath()) && COOKIE_NAME.equals(cookie.getValue()))
					    	canPlay = false;
				    }
		        }
			}
		}
		
		return canPlay;

	}	
	
	public boolean canPlayTrivia(CmsObject cms, String triviaPath, boolean registeredUser, boolean multipleGame, boolean storeResults, String userID, String site, int publication){
		
		boolean canPlay = true;
		
		if(!multipleGame){ 
			
			if(storeResults){
				
				TfsTriviasDAO triviaDAO = new TfsTriviasDAO();
				
				if(registeredUser){
					
					    try {
					    	int idTrivia = triviaDAO.getIDTrivia(triviaPath,site,publication);
							canPlay = !triviaDAO.existUserTriviaResult(idTrivia, userID);
						} catch (Exception e) {
							e.printStackTrace();
						}
					
				}else{
					
					try {
				    	int idTrivia = triviaDAO.getIDTrivia(triviaPath,site,publication);
				    	String remoteIp  = cms.getRequestContext().getRemoteAddress();
						canPlay = !triviaDAO.existIPTriviaResult(idTrivia, remoteIp);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}else{
				
				Cookie[] cookies = TfsContext.getInstance().getRequest().getCookies();
			 	   
		        if (cookies != null){
				    for (int i = 0; i < cookies.length; i++) {
					 Cookie cookie = cookies[i];
					 
					 if (cookie.getName().equals(triviaPath) && COOKIE_NAME.equals(cookie.getValue()))
					    	canPlay = false;
				    }
		        }
			}
		}
		
		return canPlay;
	}
	
	public static String getRemoteIP(String remoteAddress){
		String ip = "";
		
		String[] remoteAddrArray = remoteAddress.split(",");
		int size = remoteAddrArray.length;
		
		for(int a=0; a<size;a++){
			try{
				String ipStr = remoteAddrArray[a].trim();
				InetAddress address = InetAddress.getByName(ipStr);
				
				if(!address.isSiteLocalAddress())
				{
					ip = ipStr;
					a = size + 1;
				}
				
			}catch( UnknownHostException e){
				//CmsLog.getLog(this).error("Error determinando si la ip es interna en trivias: "+e.getMessage());
			}
		}
		return ip;
	}
	
	
	/*entrega el listado de elementos de la tabla MyList */
	public JSONObject getTriviasResults (Map<String, String> parameters, CmsObject cms) {
		JSONObject listado = new JSONObject();
	
		TfsTriviasDAO  tDAO = new TfsTriviasDAO();
		
		List<TfsTrivia> lista = null;
		try {
			 lista = tDAO.getResultsByUser(parameters);
		} catch (Exception e) {
			LOG.error("Error al buscar el listado de trivias", e);
			return listado;
		}
		try {
			JSONArray arraySources = new JSONArray();
			
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss", new Locale("es","ES"));
			for (TfsTrivia trivia : lista) {
				
				String triviaTitle = "";
				try {
					CmsProperty triviaTitleProp=cms.readPropertyObject(trivia.getPath(), "title", false);
					triviaTitle = triviaTitleProp.getValue();
				} catch (Exception ex) {
					LOG.debug("No puede obtener la propiedad title de la trivia",ex);
				}
				JSONObject element = new  JSONObject();
				element.put("count", 1);
				element.put("publication", trivia.getPublication());
				element.put("trivia_id", trivia.getIdTrivia());
				element.put("trivia_path", trivia.getPath());
				element.put("trivia_title", triviaTitle);
				element.put("close_date",trivia.getCloseDate()!= null? sdf.format(trivia.getCloseDate()):"");
				element.put("user_id", trivia.getUserId());
				element.put("user_username", trivia.getUserName());
				element.put("user_name", trivia.getUser());
				element.put("result_date",trivia.getResultDate() != null? sdf.format(trivia.getResultDate()):"");
				element.put("result_type", trivia.getResultType());
				element.put("result_name", trivia.getResultName());
				element.put("result_points", trivia.getResultPoints());
				element.put("time_resolution", trivia.getTimeResolution()/1000);
				arraySources.add(element);
			}
			listado.put("items", arraySources);
		} catch (Exception e) {
			LOG.error("Error al armar el json del listado de mylist", e);
		}
		return listado;
	}
	
	public List<TfsTrivia> getTriviasByUser(String siteName,int publication, String userId){
		
		return getTriviasByUser(siteName,publication, userId,-1,-1,null);
	}
	
    public List<TfsTrivia> getTriviasByUser(String siteName,int publication, String userId, int size, int page, String resultsType){
		
		return getTriviasByUser(siteName,publication,userId,size,page,resultsType,null,null,null,null);
	}
	
	public List<TfsTrivia> getTriviasByUser(String siteName,int publication, String userId, int size, int page, String resultsType, String path, String from, String to, String order){
		
		TfsTriviasDAO  tDAO = new TfsTriviasDAO();
		
		List<TfsTrivia> trivias = null;
		try {
			 trivias = tDAO.getTriviasByUser(siteName,publication,userId,size,page,resultsType,path,from,to,order);
		} catch (Exception e) {
			LOG.error("Error al buscar el listado de trivias del usuario", e);
		}
		
		return trivias;
	}
	
	public int getCountTriviasUsers(String siteName,int publication, String userId){
		return getCountTriviasUsers(siteName,publication,userId,null );
	}
	
	public int getCountTriviasUsers(String siteName,int publication, String userId, String resultsType ){
		
		TfsTriviasDAO  tDAO = new TfsTriviasDAO();
		
		int triviasCount = 0;
		try {
			triviasCount = tDAO.getTriviasByUserCount(siteName,publication,userId,resultsType);
		} catch (Exception e) {
			LOG.error("Error al buscar la cantidad de trivias del usuario", e);
		}
		
		return triviasCount;
	}
	
	public int getCountAllResultsUsers(String siteName,int publication, String userId, String resultsType){
		
		TfsTriviasDAO  tDAO = new TfsTriviasDAO();
		
		int triviasCount = 0;
		try {
			triviasCount = tDAO.getAllTriviasByUserCount(siteName,publication,userId, resultsType);
		} catch (Exception e) {
			LOG.error("Error al buscar la cantidad de trivias del usuario", e);
		}
		
		return triviasCount;
	}
	
	public String getStatisticsString (String triviaPath,String type) {
		return getStatistics(triviaPath, type).toString();
	}
	
	
	public JSONObject getStatistics (String triviaPath,String type) {
		JSONObject result = new JSONObject();
		
	 	JSONArray errorsJS = new JSONArray();
		TfsTriviasDAO tDAO = new TfsTriviasDAO ();
	 	if (triviaPath!=null) {
	 		try {
	 			 JSONArray triviasArray = new JSONArray();
	 	  		int cantUsers = tDAO.getCantUsers(triviaPath);
	 			result.put ("cantUsers",cantUsers);
	 			List<TfsTrivia> trivias = null;
	 			if (!type.equals("scale"))	{
	 				trivias = tDAO.getCantUsersByClassification(triviaPath);
	 				for (TfsTrivia trivia :trivias){
			  			JSONObject tJson = new JSONObject();
						tJson.put ("name",trivia.getResultName());
						tJson.put ("value",trivia.getCantUsers());
		 	  			triviasArray.add(tJson)	;	
		 	  		}
	 			} else {
	 					trivias = tDAO.getTopTenByResultType(triviaPath,type,String.valueOf(10));
	 	 				for (TfsTrivia trivia :trivias){
	 			  			JSONObject tJson = new JSONObject();
	 						tJson.put ("name",trivia.getUser());
	 						tJson.put ("value",Integer.valueOf(trivia.getResultPoints()));
	 		 	  			tJson.put("timeResolution", Integer.valueOf(trivia.getTimeResolution()));
	 		 	  			tJson.put ("resultName",trivia.getResultName());
	 		 	  			SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
	 		 	  			String date = "";
	 		 	  			if (trivia.getResultDate() != null)
	 		 	  				date = dateFormat.format(trivia.getResultDate());
	 		 	  			tJson.put ("resultDate",date);
	 		 	  			triviasArray.add(tJson)	;	
	 		 	  		}
	 			}
	 			result.put("resultados",triviasArray);
	 		} catch (Exception e) {
	 			result.put("status", "error");
		 	  	JSONObject error = new JSONObject();
		            	error.put("element", "");
		            	error.put("message", e.getMessage());
		            	errorsJS.add(error);
					result.put("errors", errorsJS );
	 		}
	 	}
	 	return result;
	}
	
}
