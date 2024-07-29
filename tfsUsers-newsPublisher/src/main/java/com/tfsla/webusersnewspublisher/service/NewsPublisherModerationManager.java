package com.tfsla.webusersnewspublisher.service;

import java.rmi.RemoteException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.arabidopsis.ahocorasick.AhoCorasick;
import org.arabidopsis.ahocorasick.SearchResult;
import org.opencms.configuration.*;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.site.CmsSite;
import org.opencms.xml.CmsXmlEntityResolver;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import com.tfsla.diario.comentarios.dictionary.Dictionary;
import com.tfsla.diario.comentarios.dictionary.DictionaryPersistor;
import com.tfsla.diario.ediciones.data.SeccionDAO;
import com.tfsla.diario.ediciones.model.*;
import com.tfsla.diario.ediciones.services.*;
import com.tfsla.event.I_TfsEventListener;
import com.tfsla.planilla.herramientas.PlanillaFormConstants;
import com.tfsla.rankUsers.model.TfsUserRankResults;
import com.tfsla.rankUsers.service.RankService;
import com.tfsla.statistics.model.TfsUserStatsOptions;
import com.tfsla.webusersnewspublisher.model.ModerationReason;
import com.tfsla.webusersnewspublisher.model.ModerationResult;
import com.tfsla.webusersposts.common.PostDetails;
import com.tfsla.webusersposts.common.PostsNotificationType;
import com.tfsla.webusersposts.dataaccess.PostsDAO;
import com.tfsla.webusersposts.service.PostsMailingService;

public class NewsPublisherModerationManager {

	private static final Log LOG = CmsLog.getLog(NewsPublisherModerationManager.class);

	private NewsPublisherModerationManager(String siteName, String publication){
		loadProperties(siteName,publication);
	}

	private int tipoEdicion;
	private String siteName;
	private String publication;
	
	private float noModerationRankingLimit = 0;
	private Date noModerationRankingAge = null;
	
	private List<String> directPublishUserGroups = null;
	private List<String> moderatedPublishUserGroups = null;
	
	private Map<String,List<String>> allowedSectionsForGroup = new HashMap<String, List<String>>();
	private Map<String,List<String>> allowedCategoriesForGroup = new HashMap<String, List<String>>();
	private Map<String,List<String>> moderatorsForGroup = new HashMap<String, List<String>>();

	private boolean enablePreModeration = false;
	private boolean enablePostModeration = false;
	private boolean enableDictionary = false;
	private boolean enableSectionRestriction = false;
	private boolean enableCategoryRestriction = false;

	private int abuseReportThreshold = 5;
	private List<String> abuseTypeDescriptions = null;
	
	private static AhoCorasick forbiddenWordsTree =null;
	private static AhoCorasick moderatedWordsTree =null;
	private static byte[] forbiddenHash = null;
	private static byte[] moderatedHash = null;
	
	public void loadProperties(String siteName, String publication) {
		
    	String module = "newsPublisher";
 		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
 		this.siteName = siteName;
 		this.publication = publication;

 		tipoEdicion = Integer.parseInt(publication);
 		enablePreModeration = config.getBooleanParam(siteName, publication, module, "enablePreModeration",false);
 		enablePostModeration = config.getBooleanParam(siteName, publication, module, "enablePostModeration",false);
 		enableDictionary = config.getBooleanParam(siteName, publication, module, "enableDictionary",false);
 		enableSectionRestriction = config.getBooleanParam(siteName, publication, module, "enableSectionRestriction",false);
 		enableCategoryRestriction = config.getBooleanParam(siteName, publication, module, "enableCategoryRestriction",false);
 		abuseReportThreshold = config.getIntegerParam(siteName, publication, module, "abuseReportThreshold",5);
 		abuseTypeDescriptions = config.getParamList(siteName, publication, module, "abuseTypeDescriptions");
 		directPublishUserGroups = config.getParamList(siteName, publication, module, "directPublishUserGroups");
 		moderatedPublishUserGroups = config.getParamList(siteName, publication, module, "moderatedPublishUserGroups");
 		
 		try {
	 		String _noModerationRankingLimit = config.getParam(siteName, publication, module, "noModerationRankingLimit");
	 		if (_noModerationRankingLimit!=null)
	 			noModerationRankingLimit = Float.parseFloat(_noModerationRankingLimit);
 		}
 		catch (Exception e) {
			e.printStackTrace();
		}
 		
 		try {
	 		String _moderationRankingAge = config.getParam(siteName, publication, module, "moderationRankingAge",null);
	 		noModerationRankingAge = parseDateTime(_moderationRankingAge);
 		}
 		catch (Exception e) {
 			e.printStackTrace();
		}
 		
 		for (String group : directPublishUserGroups) {
 			allowedSectionsForGroup.put(group,config.getListItempGroupParam(siteName, publication, module, group, "allowedSections"));
 			allowedCategoriesForGroup.put(group,config.getListItempGroupParam(siteName, publication, module, group, "allowedCategories"));
 			moderatorsForGroup.put(group,config.getListItempGroupParam(siteName, publication, module, group, "moderatorsForGroup"));
 		}
 		for (String group : moderatedPublishUserGroups) { 			
 			allowedSectionsForGroup.put(group,config.getListItempGroupParam(siteName, publication, module, group, "allowedSections"));
 			allowedCategoriesForGroup.put(group,config.getListItempGroupParam(siteName, publication, module, group, "allowedCategories"));
 		}
	}

    public static NewsPublisherModerationManager getInstance(String siteName, String publication) {
    	return new NewsPublisherModerationManager(siteName, publication);
    }
   
    public static NewsPublisherModerationManager getInstance(CmsObject cms) {
    	String siteName = OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot();
    	String publication = "0";
     	TipoEdicionBaseService tService = new TipoEdicionBaseService();
    	
     	try {
			TipoEdicion tEdicion = tService.obtenerTipoEdicion(cms, cms.getRequestContext().getUri());			
			if (tEdicion!=null)
				publication = "" + tEdicion.getId();
		} catch (Exception e) {
			e.printStackTrace();
		}

    	return getInstance(siteName, publication);
    }

	public List<String> getDirectPublishUserGroups() {
		return directPublishUserGroups;
	}

	public List<String> getModeratedPublishUserGroups() {
		return moderatedPublishUserGroups;
	}

	public float getNoModerationRankingLimit() {
		return noModerationRankingLimit;
	}

	public boolean hasBigRating(CmsUser user, String sitio) {
		String uid = user.getId().toString();
		TfsUserStatsOptions options = new TfsUserStatsOptions();
		options.setSitio(sitio);
		options.setUsuario(uid);
		options.setFrom(noModerationRankingAge);
		options.setTo(new Date());
		options.setRankMode(TfsUserStatsOptions.RANK_GENERAL);
		options.setShowGeneralRank(true);
		RankService rServiceUser = new RankService();
		TfsUserRankResults resUser;
		try {
			resUser = rServiceUser.getStatistics(options);
		
			float generalRank=0;
			if ( resUser != null && resUser.getRank() != null ) {
				generalRank = resUser.getRank()[0].getGeneralRank();
			}
		
			return generalRank>noModerationRankingLimit;
		} catch (RemoteException e) {
			return false;
		}
	}

	//Puede postear pero moderado de acuerdo a los permisos del usuario, el ranking y la precencia de palabras moderadas.
	public List<ModerationResult> moderatePost(CmsObject cms, CmsUser user, String sectionName, String category, String content) throws CmsException {
		List<CmsGroup> groups = cms.getGroupsOfUser(user.getName(), false, true);
		List<ModerationResult> ret = new ArrayList<ModerationResult>();
		
		String sitePath = getSiteName(cms);
		String siteName = sitePath.replaceFirst("/sites/", "");
		boolean hasBigRating = hasBigRating(user, siteName);

		if(isSectionModerateForGroups(sectionName, groups) && !hasBigRating) {
			ret.add(ModerationResult.getInstance(ModerationReason.SECTION_MODERATED, sectionName));
		}
		if(isCategoryModerateForGroups(category, groups) && !hasBigRating) {
			ret.add(ModerationResult.getInstance(ModerationReason.CATEGORY_MODERATED, sectionName));
		}
		
		LOG.debug("hasBigRating: " + hasBigRating);
		
		List<String> moderated = getModeratedWordsInContent(cms, content);
		List<String> forbidden = getForbiddenWordsInContent(cms, content);
		boolean hasModeratedWords = isEnableDictionary() && (moderated.size() > 0 || forbidden.size() > 0);
		if(hasModeratedWords) {
			moderated.addAll(forbidden);
			ret.add(ModerationResult.getInstance(ModerationReason.HAS_MODERATED_WORDS, moderated));
		}
		
		return ret;
	}
	
	private String getSiteName(CmsObject cms) {
		CmsSite site = OpenCms.getSiteManager().getCurrentSite(cms);
		String siteName = site.getSiteRoot(); 
		return siteName;
	}
	
	private List<String> forbidden = null;
	//Puede postear de acuerdo a los permisos del usuario y la ausencia de palabras prohibidas.
	public boolean canPost(CmsObject cms, String username, String content) {
		if (!canPost(cms, username))
			return false;
		
		if (!isEnableDictionary())
			return true;
		
		forbidden = getForbiddenWordsInContent(cms, content);
		return forbidden.size() == 0;
	}
	
	public List<String> getForbiddenWordsFound() {
		return forbidden;
	}
	
	//Puede postear de acuerdo a los permisos del usuario.
	@SuppressWarnings("unchecked")
	public boolean canPost(CmsObject cms, String username) {
		try {
			List<CmsGroup> groups = cms.getGroupsOfUser(username, false, true);
			
			for (CmsGroup group : groups) {
				if (moderatedPublishUserGroups.contains(group.getName()))
					return true;
				
				if (directPublishUserGroups.contains(group.getName()))
					return true;
			}
		
		} catch (CmsException e) {
			e.printStackTrace();
		}
		
		return false;
	}

	public boolean isModeratedPost(String sectionName, String category, List<CmsGroup> groups) {
		return isSectionModerateForGroups(sectionName, groups) || isCategoryModerateForGroups(category, groups);
	}

	public boolean isUnmoderatedPost(String sectionName, String category, List<CmsGroup> groups) {
		return isSectionUnmoderatedForGroups(sectionName, groups) && isCategoryUnmoderatedForGroups(category, groups);
	}

	public boolean isSectionModerateForGroups(String sectionName, List<CmsGroup> groups) {
		boolean isModerated = false;
		
		if (!enableSectionRestriction)
			return false;
		
		for (CmsGroup group : groups) {
			if (moderatedPublishUserGroups.contains(group.getName())) {
				List<String> sectionNameModeratedAllowed = allowedSectionsForGroup.get(group.getName());
				
				if (sectionNameModeratedAllowed.size()==1 && sectionNameModeratedAllowed.get(0).equals("*"))
					isModerated = true;
				else if (sectionNameModeratedAllowed.contains(sectionName))
					isModerated = true;
			}
		}
		
		return isModerated;
	}

	public boolean isSectionUnmoderatedForGroups(String sectionName, List<CmsGroup> groups) {
		boolean isUnmoderated = false;

		if (!enableSectionRestriction)
			return true;
		
		for (CmsGroup group : groups) {
			if (directPublishUserGroups.contains(group.getName())) {
				List<String> sectionNameUnmoderatedAllowed = allowedSectionsForGroup.get(group.getName());
				
				if (sectionNameUnmoderatedAllowed.size()==1 && sectionNameUnmoderatedAllowed.get(0).equals("*"))
					isUnmoderated = true;
				else if (sectionNameUnmoderatedAllowed.contains(sectionName))
					isUnmoderated = true;
			}
		}
		
		return isUnmoderated;
	}

	public boolean isCategoryModerateForGroups(String category, List<CmsGroup> groups) {
		boolean isModerated = false;
		
		if (!enableCategoryRestriction)
			return false;

		for (CmsGroup group : groups) {
			if (moderatedPublishUserGroups.contains(group.getName())) {
				List<String> categoryModeratedAllowed = allowedCategoriesForGroup.get(group.getName());
				
				if (categoryModeratedAllowed.size()==1 && categoryModeratedAllowed.get(0).equals("*"))
					isModerated = true;
				else if (categoryModeratedAllowed.contains(category))
					isModerated = true;
			}
		}
		
		return isModerated;
	}

	public boolean isCategoryUnmoderatedForGroups(String category, List<CmsGroup> groups) {
		boolean isUnmoderated = false;
		
		if (!enableCategoryRestriction)
			return true;

		for (CmsGroup group : groups) {
			if (directPublishUserGroups.contains(group.getName())) {
				List<String> categoryUnmoderatedAllowed = allowedCategoriesForGroup.get(group.getName());
				
				if (categoryUnmoderatedAllowed.size()==1 && categoryUnmoderatedAllowed.get(0).equals("*"))
					isUnmoderated = true;
				else if (categoryUnmoderatedAllowed.contains(category))
					isUnmoderated = true;
			}
		}
		
		return isUnmoderated;
	}

	@SuppressWarnings("unchecked")
	public List<String> getGroupModerators(List<CmsGroup> groups) {
		List<String> moderators = new ArrayList<String>();
		
		for (CmsGroup group : groups) {
			List<String> groupModerators =  moderatorsForGroup.get(group.getName());
			if(groupModerators == null) continue;
			moderators = (List<String>) CollectionUtils.union(moderators, groupModerators);
		}
		return moderators;
	}

	@SuppressWarnings("unchecked")
	public List<String> getAllowedCategoriesForGroups(List<CmsGroup> groups) {
		List<String> allowedCategories = new ArrayList<String>();
		for (CmsGroup group : groups) {
			List<String> categAllowed = allowedCategoriesForGroup.get(group.getName());
			allowedCategories = (List<String>) CollectionUtils.union(allowedCategories,categAllowed);
		}
		
		return allowedCategories;
	}

	@SuppressWarnings("unchecked")
	public List<Seccion> getAllowedSectionsForGroups(List<CmsGroup> groups) {
		List<Seccion> allowedSections = new ArrayList<Seccion>();
		try {
			SeccionDAO sDAO = new SeccionDAO();
			List<Seccion> sections = sDAO.getSeccionesByTipoEdicionId(tipoEdicion);

			if (!enableSectionRestriction)
				return sections;
			
			List<String> sectionsNames = new ArrayList<String>();
			
			for (CmsGroup group : groups) {
				List<String> sectionNameAllowed = allowedSectionsForGroup.get(group.getName());
				if (sectionNameAllowed.size()==1 && sectionNameAllowed.get(0).equals("*")) {
					allowedSections = sections;
					return allowedSections;
				}
				
				sectionsNames = (List<String>) CollectionUtils.union(sectionsNames, sectionNameAllowed);
			}

			for (Seccion seccion : sections) {
				if (sectionsNames.contains(seccion.getName()))
					allowedSections.add(seccion);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return allowedSections;
	}
	
	protected Date parseDateTime(String value) {
		if (value==null)
			return null;
		
		if (value.matches("\\d{8}")) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			try {
				return sdf.parse(value);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
	
		if (value.matches("\\d{8}\\s\\d{4}")) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd hhmm");
			try {
				return sdf.parse(value);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
	
		if (value.matches("\\d+h")) {
			value = value.replace("h", "");
			int hours = Integer.parseInt(value);
			
			Calendar cal = new GregorianCalendar();
			cal.add(Calendar.HOUR, -1* hours);
			return cal.getTime();
		}
	
		if (value.matches("\\d+d")) {
			value = value.replace("d", "");
			int days = Integer.parseInt(value);
			
			Calendar cal = new GregorianCalendar();
			cal.add(Calendar.DAY_OF_YEAR, -1* days);
			return cal.getTime();
		}
	
		if (value.matches("\\d+M")) {
			value = value.replace("M", "");
			int month = Integer.parseInt(value);
			
			Calendar cal = new GregorianCalendar();
			cal.add(Calendar.MONTH, -1* month);
			return cal.getTime();
		}
	
		if (value.matches("\\d+y")) {
			value = value.replace("y", "");
			int year = Integer.parseInt(value);
			
			Calendar cal = new GregorianCalendar();
			cal.add(Calendar.YEAR, -1* year);
			return cal.getTime();
		}
	
		return null;
	}

	public List<String>  getForbiddenWordsInContent(CmsObject cms, String content) {
		loadTrees(cms);
		String parsedContent = preproc(content);
		return findWords(forbiddenWordsTree, parsedContent);
	}

	public List<String>  getModeratedWordsInContent(CmsObject cms, String content) {
		loadTrees(cms);
		String parsedContent = preproc(content);
		return findWords(moderatedWordsTree, parsedContent);
	}

	//Funciones de reporte de abuso.
	public void addAbuseReport(CmsObject cms, CmsResource resource, CmsUser user, String abuseType, String userMessage) {
		try {
			int abuseReportNumber = Integer.parseInt(cms.readPropertyObject(resource, "abuseReportCount", false).getValue("0"));
			abuseReportNumber++;
			
			try{
				cms.lockResource(cms.getSitePath(resource));
				cms.writePropertyObject(cms.getSitePath(resource), new CmsProperty("abuseReportCount","" + abuseReportNumber,"" + abuseReportNumber, true));
				cms.unlockResource(cms.getSitePath(resource));
			}catch(Exception ex){
				
			}		

			Map<String, Object> eventData = new HashMap<String, Object>();
	        eventData.put(I_TfsEventListener.KEY_RESOURCE, resource);
	        eventData.put(I_TfsEventListener.KEY_ABUSETYPE, abuseType);
	        eventData.put(I_TfsEventListener.KEY_USERMESSAGE, userMessage);
	        eventData.put(I_TfsEventListener.KEY_COMMENT, "ReporteAbuso");
	        eventData.put(I_TfsEventListener.KEY_USER_NAME, user.getName());
	        
	        OpenCms.fireCmsEvent(new CmsEvent(I_TfsEventListener.EVENT_POST_REPORTED, eventData));

			//enviar a dashboard informe de moderacion.
			if (isEnablePostModeration()){
				if (abuseReportNumber==abuseReportThreshold) {
					setRevisionPost(cms, resource);
				}
			}
		} catch (CmsException e) {
			e.printStackTrace();
		}
	}

	public void setRevisionPost(CmsObject cms, CmsResource resource) {
		this.setRevisionPost(cms, resource, true);
	}
	
	public void setRevisionPost(CmsObject cms, CmsResource resource, Boolean publish) {
		Map<String, Object> eventData;
		//cambiar el estado a pendiente de moderacion.
		changeState(cms, cms.getRequestContext().getSitePath(resource),PlanillaFormConstants.PENDIENTE_MODERACION_VALUE);

		if(publish) {
			try {
				OpenCms.getPublishManager().publishResource(cms, cms.getRequestContext().getSitePath(resource));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		//enviar a dashboard a moderadores.
		eventData = new HashMap<String, Object>();
		eventData.put(I_TfsEventListener.KEY_RESOURCE, resource);
		eventData.put(I_TfsEventListener.KEY_USER_NAME, getAuthor(cms,resource));

		OpenCms.fireCmsEvent(new CmsEvent(I_TfsEventListener.EVENT_POST_REVISION, eventData));
	}
	protected String getAuthor(CmsObject cmsObject, CmsResource resource) {
		CmsFile file;
		try {
			file = cmsObject.readFile(resource);
		
			CmsXmlContent xmlContent = CmsXmlContentFactory.unmarshal(cmsObject, file);
			
			List<Locale> locales = xmlContent.getLocales();
			if (locales.size() == 0) {
				locales = OpenCms.getLocaleManager().getDefaultLocales(cmsObject,cmsObject.getSitePath(resource));
			}
	
			Locale locale = OpenCms.getLocaleManager().getBestMatchingLocale(CmsLocaleManager.getLocale(""),
			OpenCms.getLocaleManager().getDefaultLocales(cmsObject, cmsObject.getSitePath(resource)),locales);
				
			return xmlContent.getValue("autor/internalUser",locale).getStringValue(cmsObject);			
		} catch (CmsException e) {
			e.printStackTrace();
		}
		
		return "";
	}
	
	protected String getSection(CmsObject cmsObject, CmsResource resource) {
		CmsFile file;
		try {
			file = cmsObject.readFile(resource);
		
			CmsXmlContent xmlContent = CmsXmlContentFactory.unmarshal(cmsObject, file);
			
			List<Locale> locales = xmlContent.getLocales();
			if (locales.size() == 0) {
				locales = OpenCms.getLocaleManager().getDefaultLocales(cmsObject,cmsObject.getSitePath(resource));
			}
	
			Locale locale = OpenCms.getLocaleManager().getBestMatchingLocale(CmsLocaleManager.getLocale(""),
			OpenCms.getLocaleManager().getDefaultLocales(cmsObject, cmsObject.getSitePath(resource)),locales);
				
			return xmlContent.getValue("seccion",locale).getStringValue(cmsObject);			
		} catch (CmsException e) {
			e.printStackTrace();
		}
		
		return "";
	}
	
	protected String getCategory(CmsObject cmsObject, CmsResource resource) {
		CmsFile file;
		try {
			file = cmsObject.readFile(resource);
		
			CmsXmlContent xmlContent = CmsXmlContentFactory.unmarshal(cmsObject, file);
			
			List<Locale> locales = xmlContent.getLocales();
			if (locales.size() == 0) {
				locales = OpenCms.getLocaleManager().getDefaultLocales(cmsObject,cmsObject.getSitePath(resource));
			}
	
			Locale locale = OpenCms.getLocaleManager().getBestMatchingLocale(CmsLocaleManager.getLocale(""),
			OpenCms.getLocaleManager().getDefaultLocales(cmsObject, cmsObject.getSitePath(resource)),locales);
			if(xmlContent.hasValue("Categorias", locale, 0)) {
				if(xmlContent.getValue("Categorias[0]",locale) != null) {
					return xmlContent.getValue("Categorias[0]",locale).getStringValue(cmsObject);
				} else {
					return "";
				}
			} else {
				return "";
			}
		} catch (CmsException e) {
			e.printStackTrace();
		}
		
		return "";
	}
	
	public List<ModerationResult> premoderation(CmsObject cms, CmsResource resource, CmsUser user, String content) {
		return this.premoderation(cms, resource, user, content, true);
	}
	
	public List<ModerationResult> premoderation(CmsObject cms, CmsResource resource, CmsUser user, String content, Boolean publish) {
		List<ModerationResult> moderationResults = new ArrayList<ModerationResult>();
		if (enablePreModeration) {
			String sectionName = getSection(cms,resource);
			String category = getCategory(cms,resource);

			try {
				moderationResults = moderatePost(cms, user, sectionName, category, content);
				//if (moderationResults.size() > 0)
				//	setRevisionPost(cms, resource, publish);
			} catch (CmsException e) {
				//setRevisionPost(cms, resource, publish);
				LOG.error("Error moderando post - " + e.getMessage(), e);
				e.printStackTrace();
			}
		}
		return moderationResults;
	}
	
	public void aprovePost(CmsObject cms, CmsResource resource, CmsUser user) {
		//cambiar el estado a publicado (aceptado).
		try {
			changeState(cms,cms.getRequestContext().getSitePath(resource),PlanillaFormConstants.PUBLICADA_VALUE);

			lockTheFile(cms, cms.getRequestContext().getSitePath(resource));
			cms.writePropertyObject(cms.getSitePath(resource), new CmsProperty("abuseReportCount","0","0", true));
			if(!cms.getLock(resource).isUnlocked())
				cms.unlockResource(cms.getRequestContext().getSitePath(resource));

			try {
				OpenCms.getPublishManager().publishResource(cms, cms.getRequestContext().getSitePath(resource));
			} catch (Exception e) {
				e.printStackTrace();
			}

			Map<String, Object> eventData = new HashMap<String, Object>();
	        eventData.put(I_TfsEventListener.KEY_RESOURCE, resource);
	        eventData.put(I_TfsEventListener.KEY_USER_NAME, user.getName());
	        
	        OpenCms.fireCmsEvent(new CmsEvent(I_TfsEventListener.EVENT_POST_ACEPTED, eventData));
	        PostsMailingService.sendMail(user, resource, cms, siteName, publication, PostsNotificationType.APPROVED);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void rejectPost(CmsObject cms, CmsResource resource, CmsUser user) {
		//cambiar el estado a rechazado.
		changeState(cms,cms.getRequestContext().getSitePath(resource),PlanillaFormConstants.RECHAZADA_VALUE);
		
		try {
			OpenCms.getPublishManager().publishResource(cms, cms.getRequestContext().getSitePath(resource));
			PostsMailingService.sendMail(user, resource, cms, siteName, publication, PostsNotificationType.REJECTED);
		} catch (Exception e) {
			e.printStackTrace();
		}

		Map<String, Object> eventData = new HashMap<String, Object>();
        eventData.put(I_TfsEventListener.KEY_RESOURCE, resource);
        eventData.put(I_TfsEventListener.KEY_USER_NAME, user.getName());
        
        OpenCms.fireCmsEvent(new CmsEvent(I_TfsEventListener.EVENT_POST_REJECTED, eventData));
	}
	
	public int getAbuseReportCount(String resource) {
		int ret = 0;
		PostsDAO dao = new PostsDAO();
		dao.openConnection();
		try {
			ret = dao.getPostAbuseReportCount(resource);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			dao.closeConnection();
		}
		return ret;
	}
	
	public ArrayList<PostDetails> getPostDetails(String resource) {
		ArrayList<PostDetails> ret = new ArrayList<PostDetails>();
		PostsDAO dao = new PostsDAO();
		dao.openConnection();
		try {
			ret = dao.getPostDetailsByResource(resource, tipoEdicion);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			dao.closeConnection();
		}
		return ret;
	}
	
	//Funciones para la moderacion del post por contenido.
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void loadTrees(CmsObject cms) {
		
		String forbiddenWords = "";
		String moderatedWords ="";
		
		List<Dictionary> dictionaries = DictionaryPersistor.getDicionary(cms);
		if (dictionaries.size() > 0) {
			moderatedWords = dictionaries.get(0).getPalabrasModeradas().toLowerCase();
			forbiddenWords = dictionaries.get(0).getPalabrasProhibidas().toLowerCase();
		}
		byte[] newModeratedHash = getHash(moderatedWords);
		byte[] newForbiddenHash = getHash(forbiddenWords);
		
		if (moderatedWordsTree ==null || !Arrays.equals(moderatedHash, newForbiddenHash))  {
			List<String> terms = new ArrayList(Arrays.asList(moderatedWords.split(",")));
			moderatedWordsTree = createTree(terms);
			moderatedHash = newForbiddenHash;
		}
		if (forbiddenWordsTree ==null || !Arrays.equals(forbiddenHash, newModeratedHash)) {
			List<String> terms = new ArrayList(Arrays.asList(forbiddenWords.split(",")));
			forbiddenWordsTree = createTree(terms);
			forbiddenHash = newModeratedHash;
		}
	}
	
	private byte[] getHash(String termsList) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(termsList.getBytes());
			 
			return md.digest();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}

	private AhoCorasick createTree(List<String> terms) {
		AhoCorasick tree = new AhoCorasick();
		for (String term : terms)
			tree.add(term.getBytes(), term);
		
		tree.prepare();
		return tree;
	}

	@SuppressWarnings("rawtypes")
	private List<String> findWords(AhoCorasick tree, String information) {
		List<String> findwords = new ArrayList<String>();	
		int size = information.length();

		Iterator searcher = tree.search(information.getBytes());
	       while (searcher.hasNext()) {
	           SearchResult result = (SearchResult) searcher.next();
	           
	           boolean isFullWord = true;
	           if (result.getLastIndex()<size) {
	        	   if (Character.isLetterOrDigit(information.charAt(result.getLastIndex())))
	        		   isFullWord = false;
	           }
	           int startAt = result.getLastIndex()- ((HashSet)result.getOutputs()).iterator().next().toString().length();
	           if (startAt>0) {
	        	   if (Character.isLetterOrDigit(information.charAt(startAt-1)))
	        		   isFullWord = false;	        	   
	           }
	           
	           if (isFullWord)
	        	   findwords.add(((HashSet)result.getOutputs()).iterator().next().toString());
	       }
	       return findwords;
	}

	private String preproc(String information) {
		String text = information.toLowerCase();
		return text;
	}


	public boolean isEnablePreModeration() {
		return enablePreModeration;
	}

	public boolean isEnableDictionary() {
		return enableDictionary;
	}
	
	public boolean isEnablePostModeration() {
		return enablePostModeration;
	}

	private void changeState(CmsObject cmsObject, String fileName, String newState) {
		try {
			lockTheFile(cmsObject, fileName);
		
			CmsFile file = cmsObject.readFile(fileName);
			CmsXmlContent xmlContent = CmsXmlContentFactory.unmarshal(cmsObject, file);
			try {
				xmlContent.validateXmlStructure(new CmsXmlEntityResolver(cmsObject));
			} catch (CmsXmlException eXml) {
				xmlContent.setAutoCorrectionEnabled(true);
				xmlContent.correctXmlStructure(cmsObject);
	        }
			 
			List<Locale> locales = xmlContent.getLocales();
			if (locales.size() == 0) {
				locales = OpenCms.getLocaleManager().getDefaultLocales(cmsObject,fileName);
			}
	
			Locale locale = OpenCms.getLocaleManager().getBestMatchingLocale(
				CmsLocaleManager.getLocale(""),
				OpenCms.getLocaleManager().getDefaultLocales(cmsObject, fileName),
				locales
			);
	
			xmlContent.getValue("estado",locale).setStringValue(cmsObject, newState);
			
			file.setContents(xmlContent.marshal());
	
			cmsObject.writeFile(file);
			
			if(!cmsObject.getLock(file).isUnlocked())
				cmsObject.unlockResource(fileName);
			
			LOG.info(String.format("Changing state of file %s to %s", fileName, newState));
		}
		catch (Exception e) {
			e.printStackTrace();
			LOG.error(String.format("Error changing state of file %s to %s", fileName, newState), e);
		}
	}
	
	private void lockTheFile(CmsObject cmsObject,String file) throws CmsException {
		if (cmsObject.getLock(file).isUnlocked()) {
			cmsObject.lockResource(file);
		} else {
        	try {
        		cmsObject.unlockResource(file);
        		cmsObject.lockResource(file);
        	} catch (Exception e) {
        		cmsObject.changeLock(file);
        	}
        }
	}

	public List<String> getAbuseTypeDescriptions() {
		return abuseTypeDescriptions;
	}	
}
