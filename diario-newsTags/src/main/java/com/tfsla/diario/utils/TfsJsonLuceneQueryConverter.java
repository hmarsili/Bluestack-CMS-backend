package com.tfsla.diario.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.jsoup.Jsoup;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsUUID;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.I_CmsXmlDocument;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;

import com.tfsla.diario.friendlyTags.I_TfsNoticia;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class TfsJsonLuceneQueryConverter {
	
	final static Log LOG = CmsLog.getLog(TfsJsonLuceneQueryConverter.class);
	
	
	final static String OnlineSerarchIndexName = "DIARIO_CONTENIDOS_ONLINE";
	final static String OfflineSerarchIndexName = "DIARIO_CONTENIDOS_OFFLINE";
	String serarchIndexName = OnlineSerarchIndexName;
	final static String OR_OPERATOR = " OR ";
	final static String AND_OPERATOR = " AND ";
	private I_TfsNoticia news;
	private CmsObject m_cms = null;
	
	public String convertLuceneQueryByJson(String data, CmsObject cms){
		//JSON data
		JSONObject jsonRequest = JSONObject.fromObject(data);		

		Condition condition = new Condition();
		createCondition(jsonRequest, condition);		
		
		String query = "";		
		
		query += getValues(condition);
				
		query = query.replaceFirst(" AND ", "");
		return query;
	}
	
	public String convertLuceneQueryByJsonAndFile(CmsObject cms,  String file, I_TfsNoticia noticia){
		news = noticia;
		m_cms = cms;
		String data = "";
		String query = "";	
		
		//Obtengo los valores del file		
		try {
			CmsXmlContent resourceDocument = CmsXmlContentFactory.unmarshal(cms, cms.readFile(file));
			I_CmsXmlContentValue elementValue = resourceDocument.getValue("advancedQuery", cms.getRequestContext().getLocale());
		
			data = elementValue.getStringValue(cms);
		} catch (CmsException e) {
			e.printStackTrace();
		} 
		if(data != null && data != ""){
			try {
				JSONObject jsonRequest = JSONObject.fromObject(data);		
				Condition condition= new Condition();
				createCondition(jsonRequest, condition, true);		
				
				query += getValues(condition);	
				
			} catch (Exception e) {
				LOG.error("QueryBuilder/ error al armar la query",e);
			} 
		}
		return query;
	}
	
	private void createCondition(JSONObject jObject,Condition condition){
		createCondition(jObject, condition, false);
	}
	
	private void createCondition(JSONObject jObject, Condition condition, boolean obtainValue) {
		
		//Condition condition = new Condition();		
		List<Rule> rules = new ArrayList<Rule>();		
		
		String conditionValue = jObject.getString("condition");
		JSONArray array= jObject.getJSONArray("rules");
		int index = 0;
		
		for(Object o : array){
			JSONObject jsonLineItem = (JSONObject) o;
			index++;
			if(!jsonLineItem.containsKey("condition")){
				if(obtainValue)
					rules.add(getRule(jsonLineItem, true));
				else
					rules.add(getRule(jsonLineItem));					
				
				if(!hasMoreItems(index, array.size())){
					condition.setConector(conditionValue);
					condition.setRules(rules);					
				}
			}else{	
				condition.setConector(conditionValue);
				condition.setRules(rules);					
				condition.setCondition(new Condition()); 
				createCondition(jsonLineItem, condition.getCondition());			
			}			
		}		
	}

	private boolean hasMoreItems(int index, int size) {
		if(index != size){
			return true;
		}
		return false;
	}
	
	private Rule getRule(JSONObject jObject){
		return getRule(jObject, false);
	}

	private Rule getRule(JSONObject jObject, boolean obtainValue){
		Rule rule = new Rule();		
		
		rule.setID(jObject.getString("id").equals(TfsXmlContentNameProvider.getInstance().getTagName("news.title.home")) ? TfsXmlContentNameProvider.getInstance().getTagName("news.title") : jObject.getString("id")); ;
		rule.setField( jObject.getString("field"));
		rule.setType( jObject.getString("type"));
		rule.setInput( jObject.getString("input"));
		rule.setOperator( jObject.getString("operator"));
		
		if (jObject.getString("field").equals("personas")) {
			rule.setID( "people");
		}
		
		if (jObject.getString("field").equals("antiguedad")) {
				Date date = parseDateTime(jObject.getString("value"));
				rule.setValue( date!=null? "" + date.getTime():"");
		} else if (jObject.getString("value").equals("newscreator")) {
			rule.setValue(getNewsCreator());
			rule.setID("newscreator");
		}else if(jObject.getString("value").contains(".")){
			rule.setValue( getNewsValue(m_cms, news, jObject.getString("value").replace("manual-", "")));
		}else{
			rule.setValue( jObject.getString("value").replace("manual-", ""));
		}
		
		return rule;		
	}

	
	private String getNewsCreator() {
		try {
			return m_cms.readUser(news.getXmlDocument().getFile().getUserCreated()).getName();
		} catch (CmsException e) {
			LOG.error("QueryBuilder = No logra levantar el newsCreator" ,e);
			return "";
		}
	}

	protected String getQueryClause(String[] values, String categoryName)
	{		
		String clauseLucene = "";
		if (values!=null && values[0] != null && values[1] != null)
		{
			String operator = values[0];
			clauseLucene = categoryName + ":(";
			//for (String value : values)
			clauseLucene += " " + values[1] + operator ;
			
			clauseLucene = clauseLucene.substring(0,clauseLucene.length()-operator.length());
			clauseLucene +=")";
			
			clauseLucene = " " + operator + clauseLucene;
		}
		
		return clauseLucene;
	}
	
	protected String getCategoryQueryClause(String[] values,String categoryName)
	{
		String clauseLucene = "";
		if (values!=null)
		{
			String operator = values[0];
			clauseLucene = categoryName + ":(";
				if (values[1].charAt(0)=='+')
    			{
					values[1] = values[1].substring(1);
					operator = AND_OPERATOR;
    			}
				
				String itemValue = "\"";
				for (String part : values[1].split("/"))
				{
					if (part.trim().length()>0)
					{
						itemValue += part.replaceAll("[-_]", "") + " ";
					}
				}
				if (!itemValue.equals("\""))
					clauseLucene += " " + itemValue + "\"" + operator ;
			clauseLucene = clauseLucene.substring(0,clauseLucene.length()-operator.length());
			clauseLucene +=")";
			
		clauseLucene = " AND " + clauseLucene;
		}
		
		return clauseLucene;
	}


	protected String getGroupQueryClause(String[] values,String categoryName, CmsObject cms)
	{
		String clauseLucene = "";
		if (values!=null && values[0] != null && values[1] != null)
		{
			String operator = values[0];
			clauseLucene = categoryName + ":(";
				try {
					CmsGroup group = cms.readGroup(new CmsUUID(values[1]));
					if (group!=null)
						values[1] = group.getName();
				} catch (NumberFormatException e) {
				} catch (CmsException e) {}
				
				clauseLucene += " " + values[1] + operator ;
		
			clauseLucene = clauseLucene.substring(0,clauseLucene.length()-operator.length());
			clauseLucene +=")";
			
			clauseLucene = " AND " + clauseLucene;
		}
		
		return clauseLucene;
		
	}
	
	protected String getNewsValue(CmsObject cms, I_TfsNoticia noticia, String tag) {
		
		String tagElement = tag.replaceAll("\"","");
		boolean isKeywords = false;
		tagElement = tagElement.replace("[","");
		tagElement = tagElement.replace("]","");
		if(tagElement.equals("news.keywords")){
			isKeywords = true;
		}
		String mapper =  "";
		try {
			mapper = tagElement.substring(0,tagElement.indexOf("."));
		} catch (Exception ex) {}
		
		if(mapper.equals("property")){
			tagElement = tagElement.replace(mapper + ".", "");
			String elementFromTag = "";
			
			try{
				elementFromTag = TfsXmlContentNameProvider.getInstance().getTagName(tagElement);
				if(elementFromTag == null){
					elementFromTag = tagElement;
				}
			} catch(Exception ex){}
			
			if(!elementFromTag.equals("") && !elementFromTag.equals(null)){
				tagElement = elementFromTag;
			}
			CmsFile file = noticia.getXmlDocument().getFile();
			String value = "";
			try {
				value = cms.readPropertyObject(file, tagElement, false).getValue();
			} catch (CmsException e) {				
				e.printStackTrace();
			}
			if(value==null){
				I_CmsXmlDocument xmlContent = noticia.getXmlDocument();
				try {
					value = xmlContent.getStringValue(
							cms,
							tagElement,
							noticia.getXmlDocumentLocale());
				} catch (CmsXmlException e) {
					LOG.error("QueryBuilder / no encuentra elemento en xsd de la nota: " + tagElement, e);
				}
			}
			if (value !=null)
				value = value.replaceAll("[&|!^~*:#'\"]", "");
			else {
				value="";
				LOG.debug("No se encontro el valor para:" +tagElement);
			}
			return value;
		}

		I_CmsXmlDocument xmlContent = noticia.getXmlDocument();
		
		String newsValue = "";
		
		try {
			if(mapper.equals("news")){
				//evaluar que pasa con titulohome/tituloseccion
				if (tagElement.contains("news.title.")) {
					int index = Integer.parseInt(xmlContent.getStringValue( cms,
									TfsXmlContentNameProvider.getInstance().getTagName(tagElement),
									noticia.getXmlDocumentLocale()));
					newsValue = xmlContent.getStringValue(
							cms,
							TfsXmlContentNameProvider.getInstance().getTagName("news.title")+ "[" + index  + "]" ,
							noticia.getXmlDocumentLocale());
					
				} else 
					if (!tagElement.contains("authors")) {
						newsValue = xmlContent.getStringValue(
						cms,
						TfsXmlContentNameProvider.getInstance().getTagName(tagElement),
						noticia.getXmlDocumentLocale());
					} else {
						int index = 1; 
						boolean hasMoreElements = true;
						String autorValue ="";
						while (hasMoreElements){
							try {
								newsValue = xmlContent.getStringValue(cms,
								TfsXmlContentNameProvider.getInstance().getTagName(tagElement) + "["+ index +"]/" + TfsXmlContentNameProvider.getInstance().getTagName(tagElement + ".opencmsuser")
								,noticia.getXmlDocumentLocale());
								if (newsValue == null || newsValue.equals("") ) {
									newsValue = xmlContent.getStringValue(cms,
											TfsXmlContentNameProvider.getInstance().getTagName(tagElement) + "["+ index +"]/" + TfsXmlContentNameProvider.getInstance().getTagName(tagElement + ".name")
											,noticia.getXmlDocumentLocale());
								}
								autorValue +=  newsValue!=null? " " +  newsValue:""; 
								if (newsValue == null || newsValue.trim().equals("") ) 
									hasMoreElements=false;
								index++;
							} catch (Exception ex) {
								hasMoreElements = false;
							}
						}
						newsValue = autorValue;
							
					}
			}else{
				newsValue = xmlContent.getStringValue(
						cms,
						tagElement,
						noticia.getXmlDocumentLocale());
			}			
			
		} catch (CmsXmlException e) {
			LOG.error("Error al buscar el elemento en la nota:" + tagElement,e);
		}
		
		if (newsValue != null) {
			newsValue = newsValue.replaceAll("[&|!^~*:#'\"]", "");
			String newValue = "";
			//Cambio momentaneo para RM8
			if(newsValue.contains(",")){
				String[] values = newsValue.split(",");
				
				for(int i=0; i < values.length; i++){
					try{
						String tagValue = values[i];
						newValue += tagValue + ",";
					}catch(Exception ex){}			
				}
			}else{
				newValue = newsValue.trim();
			}
		
			if(isKeywords){
				newsValue = newValue;
			}
		} else {
			LOG.debug("El valor de comparacion que entrega para " + tagElement + "  es nulo");
			newsValue="";
		}
		return newsValue;
	}
	
	protected String getValues(Condition condition) {
		if (condition==null)
			return null;
		
		String clausulaReglas = getLuceneQuery(condition);
		if (clausulaReglas.trim().indexOf("NOT")==0	) //se agrega porque lucene no contempla querys que comiencen con not
			clausulaReglas = " created: [100000000 TO 95647402800000] " +clausulaReglas ; 
		return clausulaReglas;
	}

	private String getLuceneQuery (Condition condition) {
		String operator = condition.getConector();
		
		String clauseLuceneFinal =  " ";
		String clausePartial = "";
		
		int i=0;
		for(Rule rule : condition.getRules()){					
			String validate = rule.getOperator();						
			String clauseLucene = "";
					
			if (rule.getID().equals("antiguedad")) {
				if (rule.getOperator().contains("greater"))
					clauseLucene += getRangeQueryClause(rule.getValue(), "" + (new Date()).getTime(), "ultimaModificacion");
				else if (rule.getOperator().contains("less")) {
					clauseLucene += getRangeQueryClause("100000000",rule.getValue(), "ultimaModificacion");
				}
			} else {
				clauseLucene += rule.getID() + ":(";
				rule.setValue(replaceSpecialCharacters(Jsoup.parse(rule.getValue()).text()));
				clauseLucene += " " +  getValueTransformed(rule.getValue(),rule.getID())  ;
				clauseLucene +=")";								
				
				if(!validate.equals("equal")){
					clauseLucene = " NOT (" + clauseLucene + ")";
				}else{
					clauseLucene = " " + clauseLucene;
				}
			}
			LOG.debug("clausula: " + rule.getValue() + " para id " + rule.getID());
			if(!rule.getValue().trim().isEmpty()) {
				LOG.debug("agrega la clusula: "+ clauseLucene);
				if (clausePartial.equals("") )
					clausePartial += clauseLucene;
				else {
					clausePartial += " " + operator + " " + clauseLucene;
				}
			}
			i++;
			
			
		}
		clauseLuceneFinal +=    clausePartial ;
		if (condition.getCondition() != null) {
			clauseLuceneFinal += operator  + " ( " + getLuceneQuery(condition.getCondition())  + " ) ";
		}
		return " ( " + clauseLuceneFinal + " ) " ;
		
	}
	
	private String getValueTransformed (String value, String id) {
		value = replaceSpecialCharacters(Jsoup.parse(value).text());
		if (id.equals("categoria")  || id.equals("category")) {
			String itemValue = String.valueOf('"'); //"\"";
			String aux="";
			if ( value.split("/").length >4 ) {
				for (int i=0;i<3; i++) {
					if (value.split("/")[i].equals("")) {
						aux="/";
					} else 
						aux+=value.split("/")[i] + "/";
				}
			}
			value = value.replace(aux, "");
			for (String part : value.split("/")) {
				if (part.trim().length()>0) {
					itemValue += part.replaceAll("[-_]", "") + " ";
				}
				
			}
			if (!itemValue.equals( String.valueOf('"'))) {
				itemValue += String.valueOf('"') ;
			}
			return itemValue;
		} else
			return QueryParser.escape(value.trim());
	}
	
	private String replaceSpecialCharacters (String value) {
		return value.replace("/", " ").
				replace ("\\"," ");
	}
	
	protected Date parseDateTime(String value) {
		if (value==null)
			return null;
		
		if (value.matches("\\d{8}"))
		{
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			try {
				return sdf.parse(value);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
	
		if (value.matches("\\d{8}\\s\\d{4}"))
		{
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd hhmm");
			try {
				return sdf.parse(value);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
	
		if (value.matches("\\d+h"))
		{
			value = value.replace("h", "");
			int hours = Integer.parseInt(value);
			
			Calendar cal = new GregorianCalendar();
			cal.add(Calendar.HOUR, -1* hours);
			return cal.getTime();
		}
	
		if (value.matches("\\d+d"))
		{
			value = value.replace("d", "");
			int days = Integer.parseInt(value);
			
			Calendar cal = new GregorianCalendar();
			cal.add(Calendar.DAY_OF_YEAR, -1* days);
			return cal.getTime();
		}
	
		if (value.matches("\\d+M"))
		{
			value = value.replace("M", "");
			int month = Integer.parseInt(value);
			
			Calendar cal = new GregorianCalendar();
			cal.add(Calendar.MONTH, -1* month);
			return cal.getTime();
		}
	
		if (value.matches("\\d+y"))
		{
			value = value.replace("y", "");
			int year = Integer.parseInt(value);
			
			Calendar cal = new GregorianCalendar();
			cal.add(Calendar.YEAR, -1* year);
			return cal.getTime();
		}
	
		return null;
	}
	
	protected String getRangeQueryClause(String from, String to, String fieldName) {
		if (from!=null || to!=null) {
			return " " + fieldName + ":[" + from + " TO " + to + "]";
		}

		return "";
	}

}


class Condition{
	private String conector;
	private List<Rule> rules;
	private Condition condition = null;
	
	public Condition(){}

	public String getConector() {
		return conector;
	}

	public void setConector(String conector) {
		this.conector = conector;
	}

	public List<Rule> getRules() {
		return rules;
	}

	public void setRules(List<Rule> rules) {
		this.rules = rules;
	}

	public Condition getCondition() {
		return condition;
	}

	public void setCondition(Condition condition) {
		this.condition = condition;
	}
	
	
	
	
	
}

class Rule{
	private String ID;
	private String Field;
	private String Type;
	private String Input;
	private String Operator;
	private String Value;
	
	public Rule(){}

	public String getID() {
		return ID;
	}

	public void setID(String iD) {
		ID = iD;
	}

	public String getField() {
		return Field;
	}

	public void setField(String field) {
		Field = field;
	}

	public String getType() {
		return Type;
	}

	public void setType(String type) {
		Type = type;
	}

	public String getInput() {
		return Input;
	}

	public void setInput(String input) {
		Input = input;
	}

	public String getOperator() {
		return Operator;
	}

	public void setOperator(String operator) {
		Operator = operator;
	}

	public String getValue() {
		return Value;
	}

	public void setValue(String value) {
		Value = value;
	}
	

	
}
