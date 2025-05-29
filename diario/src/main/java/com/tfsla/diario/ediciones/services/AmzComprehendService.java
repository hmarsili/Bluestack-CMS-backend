package com.tfsla.diario.ediciones.services;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.jsoup.Jsoup;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.I_CmsXmlDocument;
import org.opencms.xml.content.CmsXmlContentFactory;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.services.comprehend.ComprehendClient;
import software.amazon.awssdk.services.comprehend.model.DetectKeyPhrasesRequest;
import software.amazon.awssdk.services.comprehend.model.DetectKeyPhrasesResponse;

import software.amazon.awssdk.services.comprehend.model.DetectEntitiesRequest;
import software.amazon.awssdk.services.comprehend.model.DetectEntitiesResponse;
import software.amazon.awssdk.services.comprehend.model.DetectSentimentRequest;
import software.amazon.awssdk.services.comprehend.model.DetectSentimentResponse;
import software.amazon.awssdk.services.comprehend.model.Entity;
import software.amazon.awssdk.services.comprehend.model.EntityType;
import software.amazon.awssdk.services.comprehend.model.KeyPhrase;
import com.tfsla.diario.terminos.data.PersonsDAO;
import com.tfsla.diario.terminos.data.TermsDAO;
import com.tfsla.diario.terminos.data.TermsTypesDAO;
import com.tfsla.diario.terminos.model.Persons;
import com.tfsla.diario.terminos.model.SearchOptions;
import com.tfsla.diario.terminos.model.Terms;
import com.tfsla.diario.terminos.model.TermsTypes;

import org.apache.lucene.analysis.es.SpanishLightStemmer;

public class AmzComprehendService {

	private static final int MAX_DISTANCE = 30;
	
	private static final Log LOG = CmsLog.getLog(AmzComprehendService.class);
	private static Map<String, AmzComprehendService> instances = new HashMap<String, AmzComprehendService>();


	protected CmsObject cmsObject = null;
	protected String siteName;
	protected String publication;
	
	private String amzAccessID;
	private String amzAccessKey;
	private String amzRegion;
	private String language;
	
	private boolean analizeResult=true;

	private String text;
	private static long termType;
	
	private ComprehendClient comprehendClient;
	
	public static AmzComprehendService getInstance(CmsObject cms) {
    	String siteName = OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot();
    	String publication = "0";
    	try {
			publication = String.valueOf(PublicationService.getCurrentPublicationWithoutSettings(cms).getId());
		} catch (Exception e) {
			LOG.error(e);
		}
    	
    	CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
    	
    	String parameterType = config.getParam(siteName, publication, "terms", "termsType","tags");
		
    	TermsTypesDAO ttDAO = new TermsTypesDAO ();
    	TermsTypes oTermTypes = null;
    	try {
    		oTermTypes = ttDAO.getTermType(parameterType);
    		termType = oTermTypes.getId_termType();
    	} catch (Exception e) {
    		termType = new Long(1);
    	}

    	String id = siteName + "||" + publication;
    	
    	AmzComprehendService instance = instances.get(id);
    	
    	if (instance == null) {
	    	instance = new AmzComprehendService(cms,siteName, publication);

	    	instances.put(id, instance);
    	}
    	
    	instance.cmsObject = cms;
    	
    	
        return instance;
    }

	public AmzComprehendService() {}
	
	public AmzComprehendService(CmsObject cmsObject, String siteName, String publication) {
		this.siteName = siteName;
		this.publication = publication;
	}

	protected String getModuleName() {
		return "comprehend";
	}

	public boolean isAmzComprehendEnabled() {
		return CmsMedios.getInstance().getCmsParaMediosConfiguration().getBooleanParam(siteName, publication, getModuleName(), "AmzComprehendEnabled", false);
	}

	private String getAmzAccessID() {
		return (amzAccessID!=null ? amzAccessID : 
			CmsMedios.getInstance().getCmsParaMediosConfiguration().getParam(siteName, publication, getModuleName(), "amzAccessID", "")
		);
	}

	private String getAmzAccessKey() {
		return (amzAccessKey!=null ? amzAccessKey : 
			CmsMedios.getInstance().getCmsParaMediosConfiguration().getParam(siteName, publication, getModuleName(), "amzAccessKey", "")
		);
	}
	
	private String getAmzRegion() {
		return (amzRegion!=null ? amzRegion : 
			CmsMedios.getInstance().getCmsParaMediosConfiguration().getParam(siteName, publication, getModuleName(), "amzRegion", "")
		);
	}
	
	private String getLanguage() {
		return (language!=null ? language : 
			CmsMedios.getInstance().getCmsParaMediosConfiguration().getParam(siteName, publication, getModuleName(), "language", "es"));
	}
	
	public String setResource(CmsFile file, String[] contentPaths) throws CmsXmlException {
		I_CmsXmlDocument fileContent = CmsXmlContentFactory.unmarshal(cmsObject, file);

		Locale locale = cmsObject.getRequestContext().getLocale();
		
		StringBuilder sBuilder = new StringBuilder();
		for (String path : contentPaths) {
			
			if (path.contains("[x]")) {
				String value = null;
				int i=1;
				do {
					String pathNum = path;
					
					pathNum = pathNum.replace("[x]", "[" + i + "]");
					value = fileContent.getStringValue(cmsObject, pathNum, locale);
					LOG.debug(pathNum + ": " + value);
					if (value!=null) {
						sBuilder.append(Jsoup.parse(value).text());
						sBuilder.append("\\n ");
					}
					i++;
				}
				while (value!=null);
			}
			else {
				String value = fileContent.getStringValue(cmsObject, path, locale);
				value = (value != null ? value : "");
				
				sBuilder.append(Jsoup.parse(value).text());
				sBuilder.append("\\n ");
				
			}
			
		}
		text = sBuilder.toString();
		
		//Agregado para que no se pase el tamaño del texto al limite de comprehend
		try {
			if (text.getBytes("UTF8").length>=(5000)) {
				text =  new String(Arrays.copyOfRange(text.getBytes("UTF8"), 0, 5000));
				text = text.substring(0,text.lastIndexOf(" "));
			}
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			LOG.error("Error obteniendo el texto de la noticia ",e1);
		}
		
		return text;
	}
	

	public List<Entity> dectecEntities() {
        DetectEntitiesRequest detectEntitiesRequest = DetectEntitiesRequest.builder()
        		.text(text)
                .languageCode(getLanguage())
                .build();
        
        DetectEntitiesResponse detectEntitiesResult  = getComprehendClient().detectEntities(detectEntitiesRequest);

        ArrayList<Entity> entities = new ArrayList<>(detectEntitiesResult.entities());
        
        entities.sort((Entity e1, Entity e2) -> e2.score().compareTo(e1.score()));
        //detectEntitiesResult.getEntities().forEach(System.out::println);

        return entities;
        
	}

	public List<KeyPhrase> detectKeyPhrases() {
		DetectKeyPhrasesRequest detectKeyPhrasesRequest = DetectKeyPhrasesRequest.builder()
                .text(text)
                .languageCode("en")
                .build();

        DetectKeyPhrasesResponse detectKeyPhrasesResult = getComprehendClient().detectKeyPhrases(detectKeyPhrasesRequest);
        List<KeyPhrase> phraseList = detectKeyPhrasesResult.keyPhrases();
        phraseList.sort((KeyPhrase k1, KeyPhrase k2) -> k2.score().compareTo(k1.score()));
   
		return phraseList;

	}
	
	public DetectSentimentResponse dectedSentiment() {
        DetectSentimentRequest detectSentimentRequest = DetectSentimentRequest.builder()
        		.text(text)
                .languageCode(getLanguage())
                .build();

        return getComprehendClient().detectSentiment(detectSentimentRequest);

	}
	
	
	public List<Entity> dectecEntities(String text) {
        DetectEntitiesRequest detectEntitiesRequest = DetectEntitiesRequest.builder()
        		.text(text)
                .languageCode(getLanguage())
                .build();
        
        DetectEntitiesResponse detectEntitiesResult  = getComprehendClient().detectEntities(detectEntitiesRequest);

        detectEntitiesResult.entities().sort((Entity e1, Entity e2) -> e2.score().compareTo(e1.score()));
        //detectEntitiesResult.getEntities().forEach(System.out::println);

        return detectEntitiesResult.entities();
        
	}

	public List<KeyPhrase> detectKeyPhrases(String text) {
        DetectKeyPhrasesRequest detectKeyPhrasesRequest = DetectKeyPhrasesRequest.builder()
        		.text(text)
                .languageCode(getLanguage())
                .build();
		DetectKeyPhrasesResponse detectKeyPhrasesResult = getComprehendClient().detectKeyPhrases(detectKeyPhrasesRequest);
		detectKeyPhrasesResult.keyPhrases().sort((KeyPhrase k1, KeyPhrase k2) -> k2.score().compareTo(k1.score()));
		//detectKeyPhrasesResult.getKeyPhrases().forEach(System.out::println);
		
		return detectKeyPhrasesResult.keyPhrases();

	}
	
	public DetectSentimentResponse dectedSentiment(String text) {
        DetectSentimentRequest detectSentimentRequest = DetectSentimentRequest.builder()
        		.text(text)
                .languageCode(getLanguage())
                .build();

        return getComprehendClient().detectSentiment(detectSentimentRequest);

	}

	private ComprehendClient  getComprehendClient() {
		
		if (comprehendClient!=null)
			return comprehendClient;
		
		AwsBasicCredentials awsCreds = AwsBasicCredentials.create(getAmzAccessID(), getAmzAccessKey());

		
		ComprehendClient comClient = ComprehendClient.builder()
                .region(Region.of(getAmzRegion()))
                .build();
		
        comprehendClient = ComprehendClient.builder().credentialsProvider(StaticCredentialsProvider.create(awsCreds))
        		.region(Region.of(getAmzRegion()))
        		.build();
            
        return comprehendClient;
	}
	
	public class DocEntity {
		protected String name;
		protected String type;
		
		protected Float score;
		protected int count;
		
		protected boolean analyzed;
		protected boolean exists;
		protected long temsId;
		protected int aproved;
		protected boolean hasCandidates;
		protected String synonymous=null;
		
		
		public class EntityCandidate {
			public String name;
			public float score;
			
			public String getName() {
				return name;
			}

			public float getScore() {
				return score;
			}

			public EntityCandidate(String name, float score) {
				this.name = name;
				this.score= score;
			}
			
			@Override
			public String toString() {
				return "EntityCandidate [name=" + name + ", score=" + score + "]";
			}
		}
		
		protected LinkedHashMap<String,EntityCandidate> candidates;
		
		
		public String getSynonymous() {
			return synonymous;
		}

		public void setSynonymous(String synonymous) {
			this.synonymous = synonymous;
		}

		public Float getScore() {
			return score;
		}
		
		public boolean isExists() {
			return exists;
		}
		
		public void setExists(boolean exists) {
			this.exists = exists;
			analyzed=true;
		}
		
		public void setTemsId(long id) {
			this.temsId = id;
		}
		
		public void setAproved(int id) {
			this.aproved = id;
		}
		
		public long getTemsId() {
			return this.temsId ;
		}
		
		public int getAproved() {
			return aproved;
		}
		
		public boolean isHasCandidates() {
			return hasCandidates;
		}
		
		
		@Override
		public String toString() {
			return "DocEntity [name=" + name + ", type=" + type + ", score=" + score + ", count=" + count + (analyzed ? ", exists=" + exists + ", candidates=" + getCandidates().toString() : "") + "]";
		} 

		public String getName() {
			return name;
		}

		public String getType()  {
			return type;
		}
		
		public List<EntityCandidate> getCandidates() {
			Collection<EntityCandidate> val = candidates.values();
			List<EntityCandidate> list = new ArrayList<EntityCandidate>(val);

			list.sort((e1,e2) -> ((Float)e2.score).compareTo((float)e1.score));
			
			return list;
		}

	}
	
	public class DocEntityImpl extends DocEntity {
		
		private int minPosition;
		private int sumPosition;
		
		private float maxScore;
		private float sumScore;
		
		
		public DocEntityImpl() {
			count = 0;
			sumScore = 0;
			sumPosition = 0;
			minPosition = Integer.MAX_VALUE;
			maxScore = 0;
			exists=false;
			
			analyzed=false;
			hasCandidates=false;
			candidates = new LinkedHashMap<String,EntityCandidate>();
		}
		
		public void calculateCustomScore2(
				float scoreMinimoModificador, 
				int maximoOffset, 
				int topeOffset,
				int InitPenalizationOffset) {
			
				float modificadorOffsetsLejanos = Float.min( 1 - ((float)minPosition-(float)topeOffset + (float)InitPenalizationOffset)/(float)maximoOffset,1);
				float modificadorOffsetsCercanos = (1-scoreMinimoModificador) * Float.max(((float)minPosition - (float)InitPenalizationOffset )/ (float)maximoOffset, 0 );
				float modificadorScoreTermino = modificadorOffsetsLejanos - modificadorOffsetsCercanos;
	
				score = Float.max(0,getMaxScore()*modificadorScoreTermino);
			
			
		}
		
		public void calculateCustomScore(int docLength) {
			score = (float)Math.pow( Math.min((Float)(getMaxScore()*
					(float)(Math.log10(getCont()+1))*
					((docLength+1-minPosition)/(float)docLength)
					),1),0.3f);
			
			
		}
		
		public void addEntityAparition(String stemm, Entity entity) {
			count++;
			sumScore +=entity.score();
			sumPosition += entity.beginOffset();
			
			if (entity.score()>maxScore) {
				maxScore = entity.score();
				name = entity.text();
			}
			if (entity.beginOffset()<minPosition) minPosition = entity.beginOffset();
			
			type = entity.typeAsString();
		} 
		
		public int getCont() {
			return count;
		}
		
		public Float getAvgScore() {
			return (count!=0 ? sumScore / count : 0);
		}
		
		public Float getMaxScore() {
			return maxScore;
		}
		
		public int getMinPosition() {
			return minPosition;
		}
		
		public int getAvgPosition() {
			return (count!=0 ? sumPosition / count : 0);
		}


		public void addCandidate(List<String> newCandidates, String text) {
			for (String candidate : newCandidates) {
				EntityCandidate c = candidates.get(candidate);
				
				int dist = StringUtils.getLevenshteinDistance(text,candidate);
				if (dist>MAX_DISTANCE) dist=MAX_DISTANCE;
				float scoreL = 1 - dist/(float)MAX_DISTANCE;
				if (c==null) {
					c = new EntityCandidate(candidate, scoreL);
				}
				else if (c.score<scoreL)
					c.score = scoreL;
				
				candidates.put(candidate, c);
			}
			analyzed=true;
			
		}
	
	}
	
	private String stemm(String text) {
		SpanishLightStemmer stemmer = new SpanishLightStemmer();
		String textStemmed = text.substring(0, stemmer.stem(text.toCharArray(), text.length()));
		return textStemmed.toLowerCase();
	}

	private List<Persons> getPeopleCandidates(String name) {
		List<Persons> people = new ArrayList<Persons>();	
		PersonsDAO dao= new PersonsDAO();
		
		try {
			people = dao.getPersonasByWord(name,10,"");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return people;
	}
	
	private List<Terms> getTagsCandidates(String name) {
		List<Terms> terminos=null;
		TermsDAO dao = new TermsDAO();
		SearchOptions options = new SearchOptions();
		options.setText(name);
		options.setCount(10);
		options.setStatus(1); //aprobados
		options.setOrderBy("TYPE");
		try {
			terminos = dao.getTerminos(options);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
		return terminos;
	}
	
	private boolean personExists(String name) {
		PersonsDAO dao = new PersonsDAO();
		
		try {
			long existe = dao.existePersonaByName(name);
			return (existe!=0);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	private String getSynonymousPersonOf(String name) {
		PersonsDAO pao = new PersonsDAO();
		
		try {
			List<Persons> terminos = pao.getPersonasBySynonym(name, true);
			if (terminos.size()>0 && terminos.get(0).getApproved()==1)
				return terminos.get(0).getName();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private String getSynonymousTagOf(String name) {
		TermsDAO dao = new TermsDAO();
		
		try {
			List<Terms> terminos = dao.getTerminoBySynonym(termType,name,true);
			if (terminos.size()>0 && terminos.get(0).getIsFullTag())
				return terminos.get(0).getName();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
		
	private String tagExists(String name) {
		TermsDAO dao = new TermsDAO();
		try {
			List<Terms> terminos = dao.getTerminosByNames(new String[]{name}, 1, termType);
			if (!terminos.get(0).getIsFullTag())
				return null;
			//LOG.error("Tag existe! AMZ: " + name + " --> Tagsbase: " + terminos.get(0).getName());
			return terminos.get(0).getName();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		return null;
	}
	
	private Terms tagExist(String name) {
		TermsDAO dao = new TermsDAO();
		try {
			List<Terms> terminos = dao.getTerminosByNames(new String[]{name}, 1, termType);
			if (!terminos.get(0).getIsFullTag())
				return null;
			return terminos.get(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		return null;
	}
	
	
	private boolean isTag(Entity entity) {
		
		return entity.type().equals(EntityType.ORGANIZATION) 
				|| entity.type().equals(EntityType.EVENT) 
				|| entity.type().equals(EntityType.OTHER) 
				|| entity.type().equals(EntityType.TITLE);
	}
	
	public List<DocEntity> processEntities(List<Entity> entities) {
		LinkedHashMap<String, DocEntity> docEntities = new LinkedHashMap<String, DocEntity>();
		
		for (Entity entity : entities) {
			String stemm = stemm(entity.text());
			DocEntityImpl docEntity = (DocEntityImpl)docEntities.get(stemm);
			if (docEntity== null) {
				docEntity = new DocEntityImpl();
			}
			docEntity.addEntityAparition(stemm,entity);
			
			if (analizeResult) {
				//LOG.error("analizando " + entity.text());
				processEntityIfPerson(entity, docEntity);				
				processEntityIfTag(entity, docEntity);
			}
			
			docEntities.put(stemm, docEntity);
						
		}
	
		List<DocEntity> valueList = new ArrayList<>(docEntities.values());
		valueList.removeIf(e -> e.getType().equals("QUANTITY") || e.getType().equals("DATE"));
		
		//Calculo de maximos para el ordenamiento
		int maxPos=0;
		for( DocEntity e : valueList) {
			if (((DocEntityImpl)e).getMinPosition()>maxPos) 
				maxPos = ((DocEntityImpl)e).getMinPosition();
		}
		
		for( DocEntity e : valueList) { 
			((DocEntityImpl)e).calculateCustomScore(maxPos);
		}
		
		//Ordenamiento por relevancia
		valueList.sort(
				(DocEntity e1, DocEntity e2) -> 
						
					e2.getScore().compareTo(
							e1.getScore()
							)
			);

		return valueList;
	}
	
	/**
	 * Se procesan las entidades, eliminndo  todo lo que no esta seteado dentro del cmsmedios.xml (tipo y score)
	 * Por cada entidad validamos si existe en la base de datos y su estado.
	 * */
	
	public List<DocEntity> processEntitiesCMS(List<Entity> entities, String enableEntitiesType, String enableEntitiesScore) {
		LinkedHashMap<String, DocEntity> docEntities = new LinkedHashMap<String, DocEntity>();
		
		LOG.debug("processEntitiesCMS entities ENTRADA " + entities );
		
		
		int maxOffset = 0;
		for (Entity entity : entities) {
			if (maxOffset<entity.beginOffset())
				maxOffset =entity.beginOffset();
		}
		
		List<Entity> entitiesList = new ArrayList<>(entities);
		
		//entitiesList.removeIf(e -> e.getScore() < Float.parseFloat(enableEntitiesScore));
		entitiesList.removeIf(e -> enableEntitiesType.indexOf(e.typeAsString()) < 0);
		
		LOG.debug("processEntitiesCMS entities HABILITADAS " + entitiesList );
		
		for (Entity entity : entitiesList) {
			String stemm = stemm(entity.text());
			DocEntityImpl docEntity = (DocEntityImpl)docEntities.get(stemm);
			if (docEntity== null) {
				docEntity = new DocEntityImpl();
			}
			
			docEntity.addEntityAparition(stemm,entity);
			
			processEntityIfExitTag(entity, docEntity);
		
			docEntities.put(stemm, docEntity);
		}
		
		LOG.debug("processEntitiesCMS entities PROCESADAS 1" + docEntities );
		
		
		List<DocEntity> valueList = new ArrayList<>(docEntities.values());
		


		float scoreMinimoModificador = 0.6f; // Valor 
		
		int initPenalizationOffset = Integer.min(500,maxOffset); // Antes de esta posicion no penaliza
		int topeOffset = Integer.max(initPenalizationOffset,maxOffset/2); 		// A partir de esta posicion penalizar encuentros de tags		

		for (DocEntity value : valueList) {
			DocEntityImpl docEntity = (DocEntityImpl) value;
			docEntity.calculateCustomScore2(
					scoreMinimoModificador,
					maxOffset,
					topeOffset,
					initPenalizationOffset
					);
		}
		
//		LOG.debug("processEntitiesCMS entities salida " + docEntities );
//		
//		LOG.debug("enableEntitiesType - " + enableEntitiesType);
//		
//		List<DocEntity> valueList = new ArrayList<>(docEntities.values());
//		
//		LOG.debug("valueList  - " + valueList);
//		valueList.removeIf(e -> enableEntitiesType.indexOf(e.getType()) < 0);
//		
//		LOG.debug("removeIf valueList " + valueList);
//		
//		LOG.debug("enableEntitiesScore - " + enableEntitiesScore);
		
		valueList.removeIf(e -> e.getScore() < Float.parseFloat(enableEntitiesScore));
		
		valueList.sort((t1,t2) -> ((Float)t2.getScore()).compareTo((float)t1.getScore()));

		for (DocEntity value : valueList) {
			LOG.debug(value);
		}
		
		
		return valueList;
	}

	private void processEntityIfPerson(Entity entity, DocEntityImpl docEntity) {
		if (entity.type().equals(EntityType.PERSON)) {
			boolean personExists = personExists(entity.text());
			if (personExists) {
				docEntity.setExists(true);
			}
			else {
				String synon = getSynonymousPersonOf(entity.text());
				if (synon!=null) {
					docEntity.setSynonymous(synon);
				}
				else {
				List<Persons> people = getPeopleCandidates(entity.text());
				List<String> candidates = new ArrayList<String>();
				people.forEach(p -> candidates.add(p.getName())); 
				docEntity.addCandidate(candidates,entity.text());
				}
			}
		}
	}
	
	private void processEntityIfExitTag(Entity entity, DocEntityImpl docEntity) {
		
		Terms tag = tagExist(entity.text());
	
		//LOG.debug("extiste tag para esa entidad" + entity );
		
		if (tag!=null) {
			docEntity.setExists(true);
			docEntity.name = tag.getName();
			docEntity.setTemsId(tag.getId_term());
			docEntity.setAproved(tag.getApproved());
			//LOG.debug("EXISTE" + docEntity + " TAG " + tag);
		}
		else {
			String synon = getSynonymousTagOf(entity.text());
			if (synon!=null) {
				docEntity.setSynonymous(synon);
				//LOG.debug("EXISTE synon" + synon );
			} else {
				List<Terms> terms = getTagsCandidates(entity.text());
				List<String> candidates = new ArrayList<String>();
				terms.forEach(t -> candidates.add(t.getName())); 
				docEntity.addCandidate(candidates,entity.text());
			}
		}
		
		//LOG.debug("retorna docEntity" + docEntity );

	}
	
	private void processEntityIfTag(Entity entity, DocEntityImpl docEntity) {
		if (isTag(entity)) {
			String tagExists = tagExists(entity.text());
			
			if (tagExists!=null) {
				docEntity.setExists(true);
				docEntity.name = tagExists;
			}
			else {
				String synon = getSynonymousTagOf(entity.text());
				if (synon!=null) {
					docEntity.setSynonymous(synon);
				}
				else {
					List<Terms> terms = getTagsCandidates(entity.text());
					List<String> candidates = new ArrayList<String>();
					terms.forEach(t -> candidates.add(t.getName())); 
					docEntity.addCandidate(candidates,entity.text());
				}
			}
			
		}
	}
	
	
	public List<DocEntity> getPeople(List<DocEntity> entities) {
		 List<DocEntity> people = new ArrayList<>();
		 entities.forEach( 
		         docEntity -> people.add( docEntity )
		  );
		 people.removeIf(e -> !e.type.equals("PERSON"));

		 return people;
	}
	
	/*
	 PERSON | LOCATION | ORGANIZATION | COMMERCIAL_ITEM | EVENT | DATE | QUANTITY | TITLE | OTHER
	 */
	public List<DocEntity> getTags(List<DocEntity> entities) {
		 List<DocEntity> tags = new ArrayList<>();
		 entities.forEach( 
		         docEntity -> tags.add( docEntity )
		  );
		 tags.removeIf(e -> !e.type.equals("ORGANIZATION") && !e.type.equals("EVENT") && !e.type.equals("OTHER") && !e.type.equals("TITLE") );

		 
		 return tags;
	}
	
	public List<DocEntity> getPlaces(List<DocEntity> entities) {
		 List<DocEntity> people = new ArrayList<>();
		 entities.forEach( 
		         docEntity -> people.add( docEntity )
		  );
		 people.removeIf(e -> !e.type.equals("LOCATION"));

		 return people;
	}
	
	/*
	 Segun configuracion del cmsmedios.xml 
	 PERSON | LOCATION | ORGANIZATION | COMMERCIAL_ITEM | EVENT | DATE | QUANTITY | TITLE | OTHER
	 */
	public List<DocEntity> getEntities(List<DocEntity> entities, String enableEntitiesType) {
		 List<DocEntity> tags = new ArrayList<>();
		 entities.forEach( 
		         docEntity -> tags.add( docEntity )
		  );
		
		// tags.removeIf(e -> !e.type.equals("ORGANIZATION") && !e.type.equals("EVENT") && !e.type.equals("OTHER") && !e.type.equals("TITLE") );
		 tags.removeIf(e -> enableEntitiesType.indexOf(e.type) == 0);

		 return tags;
	}
	
	
	public boolean isAnalizeResult() {
		return analizeResult;
	}

	public void setAnalizeResult(boolean analizeResult) {
		this.analizeResult = analizeResult;
	}
	
	public static void main(String [] args)
	{
		
		
		//System.out.println((float)StringUtils.getLevenshteinDistance("Sergio Torres Félix","Torres"));

		
		String text = 
				"La tierra del Chapo en la que ni Diego Maradona pudo vencer la supremacía del béisbol " +
				"A Culiacán llegó uno de los astros del fútbol mundial más mediáticos de siempre, pero Culiacán, apasionada por el béisbol y asediada por el narcotráfico, se resistió a sus encantos " +
				"El rumor era inverosímil y sólo la confirmación del acuerdo le sacó de encima el rótulo de fantasía: Diego Maradona, uno de los astros del fútbol, había acordado dirigir a los Dorados de Sinaloa, un discreto equipo de la segunda división de la Liga mexicana. " +
				"Tras el anuncio llegaron la conmoción y la expectativa, como en todo lo que rodea al ex futbolista argentino. Maradona arribaría de manera inminente a Culiacán: una ciudad íntimamente relacionada con las altas temperaturas, el béisbol y el narcotráfico. " +
				"La urbe, en el noroeste de México, es el epicentro del Cártel de Sinaloa, fundado por Joaquín “El Chapo” Guzmán, en su momento uno de los criminales más buscados del mundo y que ahora cumple una cadena perpetua en Estados Unidos. " +
				"Netflix recupera la historia íntima de Maradona en México con una serie documental de siete capítulos en los que queda retratado el paso del campeón del mundo de 1986 en el Gran Pez y también su vida personal en Culiacán. " +
				"En el mando deportivo, por su parte, Culiacán siempre ha sido una de las plazas más tradicionales del béisbol mexicano, cuya tradición está impregnada sobre todo en los países norteños del país, más cercanos a Estados Unidos. Tan sólo Sinaloa alberga la localía de tres equipos de la Liga del Pacífico: los Tomateros de Culiacán, los Cañeros de Los Mochis y los Venados de Mazatlán. " +
				"Maradona, una de las figuras mediáticas más importantes del fútbol mundial, llegaba a uno de los reductos de un deporte diferente y a una ciudad donde el constante enfrentamiento entre el organizaciones criminales es el pan de cada día.  " +
				"Maradona llegó a Culiacán junto a el ex portero Luis Islas como auxiliar técnico. El interés que despertó entre los medios locales e internacionales, sin embargo, se quedó un tanto corto con el que le esperaba de los aficionados locales. A pesar de un nutrido recibimiento en el aeropuerto, esa sería la constante de su paso por el Gran Pez. " +
				"Ya en Culiacán, se ubicó en un hotel, donde viviría durante su estancia en Sinaloa. De acuerdo con medios locales, un grupo de vecinos del exclusivo fraccionamiento “La Primavera” se opuso a la excentricidad de tener de vecino a Maradona, que, en su opinión, importunaría la tranquilidad del lugar. " +
				"El aficionado futbolero se energizó, adquirió abonos para la temporada y el promedio de asistencia mejoró a partir de entonces en el estadio del equipo culichi y ahí a donde fuera el Dorados. Pero, al fin y al cabo, el deporte se mantuvo con el status minoritario que siempre ha tenido. " +
				"Culiacán no cambió sus rutinas ni se vio alterada por Maradona. A pesar de que el equipo comenzó a vender camisetas con el número 10, la franela de los Tomateros superaba en las calles y las plazas a las de Dorados. La curiosidad mediática no empataba con la de la gente común. " +
				"Un aficionado local resumió el tema en una sentencia: “Me gusta el fútbol, pero si juega Dorados y juega Tomateros, prefiero ir a ver a los Tomateros”. Y es que, además de la popularidad del béisbol, los equipos locales han sido exitosos en los últimos tiempos. " +
				"El argentino, con un discreto currículum en los banquillos, que contrastaba con su extraordinaria aunque irregular carrera como jugador, llegó a Dorados en un momento delicado del equipo, que en el inicio del Apertura 2018 de la Segunda División había empezado con varios tropiezos, lo que obligó a la directiva a despedir a Paco Ramírez como entrenador. " +
				"Sin embargo, las cosas cambiaron inmediatamente de la mano de Maradona. Después de una buena racha de inicio, el equipo volvió a la zona de Liguilla, de donde nunca más salió, y aquel semestre el equipo culminó séptimo y consiguió su boleto a las finales por el título. " +
				"Mientras tanto, Maradona era un fenómeno por sí mismo en las canchas en las que ya no jugaba: los jugadores, incluidos los rivales, se le rendían a los pies. Los más chicos, que nunca lo vieron jugar, le pedían una foto o un autógrafo. Los entrenadores se acercaban gustosos a saludarlo al inicio y al final de los partidos. " +
				"Los estadios a los que viajaba el Dorados solían tener una mejor asistencia cuando Maradona los visitaba. La gente se arremolinaba en los hoteles donde el equipo se concentraba. Los televidentes argentinos buscaban una forma de ver los partidos del ascenso mexicano desde su país. Y los resultados lo respaldaron. " +
				"En su primer torneo, Maradona llevó a la final a los Dorados. En su camino, se deshizo del primero y el segundo clasificados. Sin embargo, en la final, los Dorados caerían ante el Atlético San Luis, la filial del Atlético de Madrid en México que había inyectado dinero para consolidar el proyecto. " +
				"Sin embargo, las sensaciones fueron positivas. Maradona, a pesar de las dudas, se mantuvo para el torneo de Clausura 2018. El equipo, sin su liderazgo, tuvo un dubitativo inicio, pero cuando el argentino volvió, todo cambió, una vez más, como cada vez que, para bien o para mal, aparece Diego. " +
				"El Gran Pez volvió a calsificarse a la Liguilla, un logro que pocos técnicos primerizos en el fútbol mexicano consiguen. En las finales, los pupilos de Maradona volvieron a deshacerse de dos equipos mejor ubicados en la tabla, pero volvieron a encontrarse en la final con el Atlético, que repitió la dosis del semestre anterior. " +
				"\"La continuidad de Maradona volvió a ponerse en duda durante la pausa de verano (boreal) en el fútbol mexicano. Pero, en este caso, la decisión del argentino fue la de no continuar un tercer semestre en Culiacán. El ex capitán de la selección argentina tomó la decisión poniendo su salud en primer plano. " +
				"“Diego Maradona decidió no continuar en la dirección técnica de Dorados. Por consejo médico le dedicará tiempo a su salud y se someterá a dos operaciones: de hombro y de rodilla. Agradecidos a toda la familia de Dorados y continuaremos juntos el sueño más adelante”, confirmó en junio su abogado, Matías Morla. " +
				"A pesar de que no pudo cumplir con el objetivo del ascenso, Maradona dejó un buen sabor de boca en Culiacán, aunque las dos finales perdidas pesaron sobre el equipo, que hoy pelea en mitad de tabla y que se encuentra en busca de un técnico, ya que José Guadalupe Cruz, el sucesor del argentino, fue despedido por los malos resultados. " +
				"Maradona, por su parte, no pudo revolucionar Culiacán como lo ha hecho en infinidad de ciudades a lo largo y ancho del mundo, pero su trabajo mitigó las críticas que recibió a su llegada al noroeste de México, cuando se pronosticaba un rotundo fracaso por su falta de currículum. " +
				"Ahora, con 59 años, dirige a Gimnasia y Esgrima La Plata, en la Superliga Argentina, donde el de Villa Fiorito ha revivido a un club que parecía condenado al descenso y hoy pelea por salvar la categoría.";
/*
				"MOSUL, Irak (AP) — Entre 9.000 y 11.000 personas murieron en los nueve meses de batalla para liderar a la ciudad iraquí de Mosul del grupo extremista Estado Islámico, según determinó una investigación de Associated Press. Es una cifra de víctimas civiles unas diez veces mayor de lo que se había informado en un principio." +
				" Ni la coalición internacional, ni el gobierno iraquí ni el autoproclamado califato del grupo EI reconocen esa cifra de víctimas." +
				" Las fuerzas iraquíes o de la coalición son responsables de al menos 3.200 muertes civiles en ataques aéreos, fuego de artillería o rondas de mortero entre octubre de 2016 y la caída del grupo EI en julio de 2017, según la investigación de AP, que cruzó datos de la morgue y varias bases de datos de organizaciones no gubernamentales. La mayoría de esas víctimas aparecen simplemente como “aplastadas” en reportes del Ministerio de Salud." +
				" La coalición, que no envió a nadie a Mosul para investigar, sólo admite la responsabilidad de 326 de las muertes." +
				" Además de la base de datos de Airwars, AP analizó información de Amnistía Internacional, Iraq Body Count y un reporte de Naciones Unidas. AP también obtuvo una lista de 9.606 nombres de personas fallecidas durante la campaña elaborada por la morgue de Mosul. Se cree que cientos de civiles muertos siguen sepultados bajo los escombros." +
		" De las casi 10.000 muertes documentadas por AP, en torno a un tercio ocurrió en bombardeos de fuerzas iraquíes o la coalición que lidera Estados Unidos. Otro tercio se debió al último frenesí de violencia de los milicianos del grupo EI. Y no pudo determinarse qué bando fue el responsable de las demás muertes." +
		" Sin embargo, el total de la morgue podría superar con creces los recuentos oficiales." +
		" El primer ministro de Irak, Haidar al-Abadi, dijo a AP que 1.260 civiles murieron en los combates. La coalición que lidera Estados Unidos no ha ofrecido una cifra total. La coalición basa sus investigaciones en imágenes grabadas por dron, videos de cámaras colocadas en equipo armamentístico y observaciones de los pilotos." +
		" Los estadounidenses dijeron no tener recursos para enviar un equipo a Mosul. Debido a lo que la coalición considera información insuficiente, la mayoría de las acusaciones de bajas civiles se consideran “no creíbles” antes siquiera de iniciar una investigación." +
		" La coalición ha defendido sus decisiones operativas, afirmando que fue el grupo EI quien puso a los civiles en peligro al aferrarse al poder." +
		" Lo que está claro de las estimaciones es que conforme la coalición y las fuerzas del gobierno iraquí intensificaban su ofensiva, subió la tasa de civiles muertos a manos de sus liberadores." +
		" Antes de la batalla para expulsar al grupo EI, en Mosul vivían más de un millón de personas. Temiendo una enorme crisis humanitaria, el gobierno iraquí lanzó panfletos desde el aire y pidió a los soldados que avisaran a las familias de que se escondieran ante el inicio de la batalla final, a finales de 2016." +
		" Cuando los combates cruzaron al oeste del río Tigris el pasado invierno, los combatientes del grupo EI se llevaron a miles de civiles con ellos en su retirada. Hacinaron a cientos de familias en escuelas y edificios del gobierno." +
		" Esperaban que esa estrategia disuadiera de ataques aéreos y de artillería. Se equivocaban." +
		" Cuando las fuerzas iraquíes vieron su avance estancado a finales de diciembre, el Pentágono ajustó las normas sobre el uso de potencia aérea, permitiendo que comandantes con menos supervisión en la cadena de mando ordenaran ataques aéreos." +
		" En febrero y principios de marzo, los reportes de muertes civiles empezaron a dominar las reuniones de planificación militar en Bagdad, según un diplomático occidental de alto nivel que estaba presente pero no tenía autorización para comentarlo de forma pública." +
		" Cuando aparecieron las acusaciones sobre que un único ataque de la coalición había matado a cientos de civiles en el barrio al-Jadidah de Mosul el 17 de marzo, todos los combates se paralizaron tres semanas." +
		" Bajo una intensa presión internacional, la coalición envió por primera vez un equipo a la ciudad, que terminó concluyendo que la bomba de 500 libras (226 kilos) que mató a 105 personas estaba justificada para matar a dos francotiradores del grupo EI." +
		" Las fuerzas especiales iraquíes recibieron instrucciones de no pedir ataques aéreos sobre edificios. En su lugar, se les indicó que pidieran bombardeos de la coalición sobre jardines y carreteras contiguos a objetivos del grupo EI." +
		" Un grupo de WhatsApp compartido por asesores de la coalición y fuerzas iraquíes para coordinar ataques aéreos, antes llamado “matando a daesh 24/7”, se cambió con ironía a “asustando a daesh 24/7”. Daesh es el acrónimo en árabe para el grupo EI." +
		" Pero sobre el terreno, agentes de las fuerzas especiales iraquíes dijeron que tras la pausa, volvieron a combatir igual que antes.";
*/		
		
		String limitedText = text;
		//Agregado para que no se pase el tamaño del texto al limite de comprehend
		if (limitedText.length()>=(5000-115)) {
			limitedText = limitedText.substring(0, (5000-125));
			limitedText = limitedText.substring(0,limitedText.lastIndexOf(" "));
		}
		
		String amzAccessID = "";
		String amzAccessKey = "";
		String amzRegion = "US_EAST_1".toLowerCase().replaceAll("_","-");
		
		
		AmzComprehendService service = new AmzComprehendService();
		service.amzAccessID = amzAccessID;
		service.amzAccessKey = amzAccessKey;
		service.amzRegion = amzRegion;
		service.language = "es";
		
		service.setAnalizeResult(false);
		
		System.out.println("Calling DetectEntities");
		//service.dectecEntities(text).forEach(System.out::println);	
		
		List<DocEntity> entities =service.processEntities(service.dectecEntities(limitedText));
		entities.forEach(System.out::println);
		System.out.println("End of DetectEntities\n");
		
		System.out.println("Personas:");
		service.getPeople(entities).forEach(System.out::println);
		
		System.out.println("Tags:");
		service.getTags(entities).forEach(System.out::println);
		
		System.out.println("Lugares:");
		service.getPlaces(entities).forEach(System.out::println);
		

/*		
		System.out.println("Calling DetectKeyPhrases");
		service.detectKeyPhrases(text).forEach(System.out::println);
        System.out.println("End of DetectKeyPhrases\n");

		System.out.println("Calling DetectSentiment");
		System.out.println(service.dectedSentiment(text));
        System.out.println("End of DetectSentiment\n");
        System.out.println( "Done" );
      */
	}
}
