package com.tfsla.diario.friendlyTags;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspTagException;

import org.apache.commons.logging.Log;
import org.opencms.file.CmsObject;
import org.opencms.flex.CmsFlexController;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.xml.CmsXmlException;

import com.tfsla.diario.VODCollector.A_VODCollector;
import com.tfsla.diario.VODCollector.LuceneVODCollector;
import com.tfsla.diario.model.TfsListaVods;
import com.tfsla.diario.model.TfsVOD;

public class TfsVODListTag extends A_XmlContentTag implements I_TfsNoticia, I_TfsCollectionListTag {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1984143153049823965L;

		private static final Log LOG = CmsLog.getLog(TfsVODListTag.class);
		TfsVOD previousVOD = null;
		TfsListaVods previousListaVods= null;

		//static private List<A_VODCollector> vODCollectors = new ArrayList<A_VODCollector>();

		public static final String param_zone="zone";
				
		public static final String param_category="category";
		public static final String param_tags="tags";
		public static final String param_personas = "people";

		public static final String param_size="size";
		public static final String param_page="page";
		public static final String param_order="order";
		public static final String param_title ="title";
		public static final String param_type ="type";
		
		public static String param_longDescription  = "cuerpo";
		
		public static final String param_numberOfParamters = "params-count";
		public static final String param_advancedFilter="advancedfilter";
		public static final String param_searchIndex="searchIndex";
		public static final String param_publication="publication";

		public static final String param_fromDate="fromDate";
		public static final String param_toDate="toDate";
		public static final String param_orderVOD = "orderVOD";
		public static final String param_classification = "calificacion";
		
		
		public static final String param_showtemporal="showtemporal";

		/*static {
			vODCollectors.add(new LuceneVODCollector());
		}*/
		
		CmsObject cms = null;
		private String url = null;
		
		private String showresult="";
		
		private String zone= null;
		
		private String tags=null;
		private String persons=null;
		
		private String category=null;
		private String name=null;
		
		private int size=0;
		private int page=1;
		private String order=null;

		private String advancedfilter=null;
		private String searchindex = null;
		private String publication=null;
		
		private String fromDate=null;
		private String toDate=null;
		private String orderVOD=null;
		private String type =null;
		private String classification = null;
		
		

		private Boolean showtemporal = null;
		
		public String getShowtemporal() {
			return showtemporal.toString();
		}

		public void setShowtemporal(String showtemporal) {
			if (showtemporal==null || showtemporal.trim().length()==0)
				this.showtemporal = null;
			else
				this.showtemporal = Boolean.parseBoolean(showtemporal);
		}

		private List<String> vods=null;
		private int index = 0;

		
		public CmsObject getCms() {
			return cms;
		}

		public void setCms(CmsObject cms) {
			this.cms = cms;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public String getShowresult() {
			return showresult;
		}

		public void setShowresult(String showresult) {
			this.showresult = showresult;
		}

		public String getTags() {
			return tags;
		}

		public void setTags(String tags) {
			if (tags!=null && tags.equals(""))
				tags=null;
			this.tags = tags;
		}

		public String getPersons() {
			return persons;
		}

		public void setPersons(String persons) {
			if (persons!=null && persons.equals(""))
				persons=null;
		
			this.persons = persons;
		}

		public String getCategory() {
			return category;
		}

		public void setCategory(String category) {
			if (category!=null && category.equals(""))
				category=null;
		
			this.category = category;
		}

		public int getSize() {
			return size;
		}

		public void setSize(int size) {
			this.size = size;
		}

		public int getPage() {
			return page;
		}

		public void setPage(int page) {
			this.page = page;
		}

		public String getOrder() {
			return order;
		}

		public void setOrder(String order) {
			this.order = order;
		}

		public String getAdvancedfilter() {
			return advancedfilter;
		}

		public void setAdvancedfilter(String advancedfilter) {
			if (advancedfilter != null && advancedfilter.equals(""))
				advancedfilter = null;
			this.advancedfilter = advancedfilter;
		}

		public String getSearchindex() {
			return searchindex;
		}

		public void setSearchindex(String searchindex) {
			this.searchindex = searchindex;
		}

		public String getPublication() {
			return publication;
		}

		public void setPublication(String publication) {
			this.publication = publication;
		}

		public String getOrderVOD() {
			return orderVOD;
		}

		public void setOrderVOD(String orderVOD) {
			this.orderVOD = orderVOD;
		}
		
		public List<String> getVODs() {
			return vods;
		}

		public void setVODs(List<String> vods) {
			this.vods = vods;
		}

		public void setIndex(int index) {
			this.index = index;
		}

		public String getCalification() {
			return classification;
		}

		public void setCalification(String calification) {
			this.classification = calification;
		}
		
		/*
		static private A_VODCollector getVODCollector(Map<String,Object> parameters, String order) {
			A_VODCollector bestMatchCollector = null;
			
			for (A_VODCollector  collector : vODCollectors) {
				if (collector.canCollect(parameters)) {
					if (collector.canOrder(order))
						return collector;
					else if (bestMatchCollector==null)
						bestMatchCollector = collector;
				}
			}
			return bestMatchCollector;
		}
		*/
		
		public void exposeVOD() {
			TfsVOD vOD = new TfsVOD(m_cms,m_content,m_contentLocale,pageContext);
			pageContext.getRequest().setAttribute("vod", vOD);
			
			TfsListaVods listaVods = new TfsListaVods(this.vods.size(),this.index+1,this.size, this.page);
			listaVods.setCurrentPriorityZone(vOD.getZonePriority());
			listaVods.setCurrentZone(vOD.getZone());
	
			pageContext.getRequest().setAttribute("vodList", listaVods);
		}
		
		
		public String getType() {
			return type;
		}

		public void setType(String type) {
			if (type != null && type.equals(""))
				this.type = null;
			else
				this.type = type;
		}

		@Override
		public int doStartTag() throws JspException {
				
			index = 0;
			
		    cms = CmsFlexController.getCmsObject(pageContext.getRequest());

	    	findVODs();
	    				
			if (index<vods.size()) {
				init(vods.get(index));
				exposeVOD();
				return EVAL_BODY_INCLUDE;
			}
	    return SKIP_BODY;
		}
		
		
		@Override
		public int doAfterBody() throws JspException {

			index++;

			if (index==vods.size())
				restoreVODs();
			
			if (index<vods.size()) {
				init(vods.get(index));
				exposeVOD();
				return EVAL_BODY_AGAIN;
			}
			
			if (OpenCms.getSystemInfo().getServletContainerSettings().isReleaseTagsAfterEnd()) {
				release();
			}
			return SKIP_BODY;
		}

		   protected void restoreVODs() {
		    	pageContext.getRequest().setAttribute("vod", previousVOD);
		      	pageContext.getRequest().setAttribute("vodList", previousListaVods );
		      	 
		    }
		
		@Override
		public int doEndTag() {
			
			restoreVODs();

			if (OpenCms.getSystemInfo().getServletContainerSettings().isReleaseTagsAfterEnd()) {
				release();
			}
			return EVAL_PAGE;
		}
		
		protected void findVODs() {
			
			vods = null;
			index=0;
			Map<String,Object> parameters = createParameterMap();
			A_VODCollector collector = new LuceneVODCollector(); //getVODCollector(parameters,order);
			
		 	previousVOD = (TfsVOD) pageContext.getRequest().getAttribute("vod");
	    	pageContext.getRequest().setAttribute("vod",null);

			if (collector!=null)
				vods = collector.collectEvent(parameters,cms);

		}
		
		protected Map<String,Object> createParameterMap() {
			
			if (size==0)
				size=100;
			
			Map<String,Object> parameters = new HashMap<String,Object>();
			
			parameters.put(param_zone,zone);
			parameters.put(param_category,category);
			parameters.put(param_size,size);
			parameters.put(param_page,page);
			parameters.put(param_order,order);
			parameters.put(param_advancedFilter,advancedfilter);
			parameters.put(param_searchIndex,searchindex);
			parameters.put(param_tags,tags);
			parameters.put(param_title,name);
			parameters.put(param_orderVOD,orderVOD);
			parameters.put(param_publication,publication);
			parameters.put(param_fromDate,fromDate);
			parameters.put(param_toDate,toDate);
			parameters.put(param_personas,persons);
			parameters.put(param_type, type);
			parameters.put(param_showtemporal, showtemporal);
			parameters.put(param_classification, classification);
			
			int paramsWithValues =
				(zone!=null ? 1 : 0) +
				(category!=null ? 1 : 0) +
				1  + //size
				1  + //page
				(order!=null ? 1 : 0) +
				(advancedfilter!=null ? 1 : 0) +
				(searchindex!=null ? 1 : 0) +
				(publication!=null ? 1 : 0) +

				(fromDate!=null ? 1 : 0) +
				(toDate!=null ? 1 : 0) +
				(orderVOD!=null ? 1 : 0) +
				(type!=null ? 1 : 0) +
				
				(showtemporal!=null ? 1 : 0) +
				
				(tags!=null ? 1 : 0) +
				(persons!=null ? 1 : 0) +
				(classification!=null ? 1 : 0) +
				(name!=null ? 1 : 0);

			parameters.put(param_numberOfParamters,(Integer)paramsWithValues);

			return parameters;
		}

		public int getIndex() {
			return index;
		}

		public boolean isLast() {
			return (index==vods.size()-1);
		}

		
		
		public String getCollectionValue(String name) throws JspTagException {
			try {
				return getXmlDocument().getStringValue(m_cms, name, m_locale);
			} catch (CmsXmlException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return "";
		}
		
		
		public int getCollectionIndexSize(String name, boolean isCollectionPart) {
			return getXmlDocument().getValues(name, m_locale).size();
		}

		public String getCollectionIndexValue(String name, int index) {
			try {
				return getXmlDocument().getStringValue(m_cms, name, m_locale, index);
			} catch (CmsXmlException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return "";
		}

		public String getCollectionPathName() {
			return "";
		}

		public String getFromDate() {
			return fromDate;
		}

		public void setFromDate(String fromDate) {
			if (fromDate != null && fromDate.equals(""))
				fromDate=null;
			this.fromDate = fromDate;
		}

		public String getToDate() {
			return toDate;
		}

		public void setToDate(String toDate) {
			if (toDate != null && toDate.equals(""))
				toDate=null;
		
			this.toDate = toDate;
		}
		
		public String getZone() {
			return zone;
		}

		public void setZone(String zone) {
			if (zone==null || zone.trim().length()==0)
				this.zone = null;
			else
				this.zone = zone;
		}
		
				
		
}


