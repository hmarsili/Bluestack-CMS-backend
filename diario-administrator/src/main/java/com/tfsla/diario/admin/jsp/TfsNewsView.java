package com.tfsla.diario.admin.jsp;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.jsp.PageContext;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResourceFilter;
import org.opencms.flex.CmsFlexController;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.content.CmsXmlContentValueSequence;
import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.OpenCmsBaseService;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.opencms.search.documents.NoticiacontentExtrator;

public class TfsNewsView {
	
	private String state="";
	private String order="";
	private String onmainpage="";
	private String zone="";
	private String section="";
	private String size="";
	private String category="";
	private String filterText="";
	private String tags="";
	private String from="";
	private String to="";
	private String publication = "";
	private String advancedfilter = "";
	private String author = "";
	private String newscreator = "";
	private String group = "";
	private String showtemporal = "";
	private String searchinhistory = "";
	private String newstype = "";

	private String var="newsView";
	private int filterCount = 0;

	private boolean useCurrentPublication = false;
	
	private Map<String,String> variables = new HashMap<String,String>();

	private static final String FILTER = "filter";

	private static final String ON_HOME_INDEX = "enIndexHome[1]";
	private static final String ON_SECTION_INDEX = "enIndexSection[1]";
	private static final String NO_INDEX = "noIndex[1]";
	private static final String FILTERTEXT = "filterText[1]";
	private static final String ADVANCEDFILTER = "advancedfilter[1]";
	private static final String TAGS = "tags[1]";
	private static final String SIZE = "size[1]";
	private static final String PUBLICATION = "publication[1]";
	private static final String CATEGORIES = "Categorias";
	private static final String SECTIONS = "seccion";
	private static final String ZONES_HOME = "zonahome";
	private static final String ZONES_SECCION = "zonaSeccion";
	private static final String FROM = "desdeUltimaModificacion";
	private static final String TO = "hastaUltimaModificacion";
	private static final String STATE = "state";
	private static final String ORDER = "orden";
	private static final String CAMPO = "campo";
	private static final String DIRECCION = "direccion";
	private static final String AUTHOR = "author";
	private static final String NEWSCREATOR = "newscreator";
	private static final String GROUP = "group";
	private static final String SHOWTEMPORAL = "showtemporal[1]";
	private static final String FINDHISTORICAL = "findhistorical[1]";
	private static final String NEWSTYPE = "newstype[1]";
	
    private CmsFlexController m_controller;
    private HttpSession m_session;
    String siteName;
    private TipoEdicion currentPublication;
    private PageContext m_context;
    private CmsFile m_file;
    private CmsXmlContent m_content;
	private Locale locale;

    
    public CmsObject getCmsObject() {
        return m_controller.getCmsObject();
    }
    
	public TfsNewsView(PageContext context, HttpServletRequest req, HttpServletResponse res) throws Exception
    {
		m_controller = CmsFlexController.getController(req);
		m_context = context;
        m_session = req.getSession();

    	siteName = OpenCms.getSiteManager().getCurrentSite(getCmsObject()).getSiteRoot();
    	
    	currentPublication = (TipoEdicion) m_session.getAttribute("currentPublication");

    	if (currentPublication==null) {
        	TipoEdicionService tService = new TipoEdicionService();

    		currentPublication = tService.obtenerEdicionOnlineRoot(siteName);
    		m_session.setAttribute("currentPublication",currentPublication);
    	}

    }
	
	public void loadData(String viewConfigFile){
	
		CmsObject cmsObject = getCmsObject(); //TfsAdminCmsMediosModuleAction.getAdminCmsObject();

		try {
			m_file = cmsObject.readFile(viewConfigFile, CmsResourceFilter.ALL);
			m_content = CmsXmlContentFactory.unmarshal(cmsObject, m_file);
			
			locale = cmsObject.getRequestContext().getLocale();
			
			CmsXmlContentValueSequence elementSequence = m_content.getValueSequence(FILTER, locale);
			
			if (elementSequence==null)
				return ;
			//veo cuantos filtros tiene puesto.
			filterCount = elementSequence.getElementCount();
			
			String proyecto = OpenCmsBaseService.getCurrentSite(cmsObject);
			//determino que publicacion utilizar.
			if (useCurrentPublication)
				publication = "" + currentPublication.getId();
			else {
				publication = m_content.getStringValue(cmsObject,PUBLICATION, locale);
				
				if (publication == null || publication.length()==0)
					publication = "" + currentPublication.getId();
				else {
		        	TipoEdicionService tService = new TipoEdicionService();
		        	publication = "" +tService.obtenerTipoEdicion(publication,proyecto).getId();
		        	

				}
			}
			
			
			
		} catch (CmsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public void loadFilter(int number) {
		CmsObject cmsObject = getCmsObject(); //TfsAdminCmsMediosModuleAction.getAdminCmsObject();

		String path = FILTER + "[" + number + "]/";
		
		//Determino si tengo que poner noticias de portada de la home o de seccion.
		String onHome = m_content.getStringValue(cmsObject,path + ON_HOME_INDEX, locale);
		if (onHome.equals("true"))
			onmainpage = "home";

		String onSection = m_content.getStringValue(cmsObject,path + ON_SECTION_INDEX, locale);
		if (onSection.equals("true")) {
			if (onmainpage.length()>0)
				onmainpage += ",";
			onmainpage += "section";
		}
		
		//filto por palabras
		filterText = m_content.getStringValue(cmsObject,path + FILTERTEXT, locale);
		
		//filtro avanzado.
		advancedfilter = m_content.getStringValue(cmsObject,path + ADVANCEDFILTER, locale);
		advancedfilter = replaceVariables(advancedfilter);
		
		//tags
		tags = m_content.getStringValue(cmsObject,path + TAGS, locale);

		//cantidad de elementos a retornar
		size = m_content.getStringValue(cmsObject,path + SIZE, locale);
		if (new Scanner(size).hasNextInt())
			size = "" + new Scanner(size).nextInt();
		else 
			size= "100";

		//categorias.
		category = getValues(cmsObject, m_content, path + CATEGORIES, locale);

		//zonas.
		zone = getValues(cmsObject, m_content, path + ZONES_HOME, locale);
		String zone_section = getValues(cmsObject, m_content, path + ZONES_SECCION, locale);
		if (zone_section!=null && zone_section.length() > 0) {
			if (zone!=null && zone.length()>0)
				zone = "," + zone_section;
			else
				zone = zone_section;
		}

		//secciones.
		section = getValues(cmsObject, m_content, path + SECTIONS, locale);

		from = m_content.getStringValue(cmsObject,path + FROM, locale);
		if (from==null)
			from = "";
		to = m_content.getStringValue(cmsObject,path + TO, locale);
		if (to==null)
			to = "";

		//ocultar mostrar temporal.
		showtemporal = m_content.getStringValue(cmsObject,path + SHOWTEMPORAL, locale);
		
		searchinhistory = m_content.getStringValue(cmsObject,path + FINDHISTORICAL, locale);
		
		//tipo de noticia
		newstype = m_content.getStringValue(cmsObject,path + NEWSTYPE, locale);
		
		//autores.
		author = getUsers(cmsObject, m_content, path + AUTHOR, locale);
		
		//newscreator
		newscreator = getUsers(cmsObject, m_content, path + NEWSCREATOR, locale);
		
		//grupos.
		group = getUsers(cmsObject, m_content, path + GROUP, locale);

		//estados.
		state = getValues(cmsObject, m_content, path + STATE, locale);

		order = getSubValues(cmsObject,m_content,path + ORDER,new String[] {CAMPO,DIRECCION},locale);
		
		String noIndex = m_content.getStringValue(cmsObject,path + NO_INDEX, locale);
		if (noIndex.equals("true")) {
        	TipoEdicionService tService = new TipoEdicionService();
        	String nombrePublicacion = tService.obtenerTipoEdicion(Integer.parseInt(publication)).getNombre();
        	String filter = "NOT hightraffic: ( pub" + NoticiacontentExtrator.VALUE_SEPARATOR + NoticiacontentExtrator.escapeValue(nombrePublicacion) + ")";
        	
        	if (advancedfilter.length()>0)
        		advancedfilter += " AND " + filter;

		}

		m_context.getRequest().setAttribute(var, this);

	}
	
	private String replaceVariables(String text) {
		Pattern pattern = Pattern.compile("%[^%]*%");
		Matcher matcher = pattern.matcher(text);
		List<String> presentVariables = new ArrayList<String>();
		while (matcher.find())
			if (!presentVariables.contains(matcher.group()))
				presentVariables.add(matcher.group());
		for (String var : presentVariables)
		{
			String value = variables.get(var);
			if (value!=null)
				text.replaceAll(var, value);
		}
		
		return text;
	}

	private String getValues(CmsObject cmsObject, CmsXmlContent m_content,
			String path, Locale locale) {
		String value = "";
		CmsXmlContentValueSequence elementSequence = m_content.getValueSequence(path, locale);
		
		if (elementSequence==null)
			return "";
		
		int elementCount = elementSequence.getElementCount();
		for (int j=0;j<elementCount;j++)
			value += "," + elementSequence.getValue(j).getStringValue(cmsObject);
		if (value.length()>0)
			value = value.substring(1);
		
		return value;
	}
	
	private String getSubValues(CmsObject cmsObject, CmsXmlContent m_content,
			String path, String[] itemValues,  Locale locale) {
		String value = "";
		CmsXmlContentValueSequence elementSequence = m_content.getValueSequence(path, locale);
		
		if (elementSequence==null)
			return "";
		
		int elementCount = elementSequence.getElementCount();
		for (int j=0;j<elementCount;j++) {
			value += ",";
			for (String item : itemValues) {
				value += m_content.getValue( path + "[" + (j+1) + "]/" + item + "[1]", locale).getStringValue(cmsObject) + " ";
			}
			elementSequence.getValue(j);
			
		}
		if (value.length()>0)
			value = value.substring(1);
		
		return value;
	}

	private String getUsers(CmsObject cmsObject, CmsXmlContent m_content,
			String path, Locale locale) {
		String value = "";
		CmsXmlContentValueSequence elementSequence = m_content.getValueSequence(path, locale);
		
		if (elementSequence==null)
			return "";
		
		int elementCount = elementSequence.getElementCount();
		for (int j=0;j<elementCount;j++) {
			String userName = elementSequence.getValue(j).getStringValue(cmsObject);
			if (userName.trim().toLowerCase().equals("me") || userName.trim().toLowerCase().equals("current")){
				userName = cmsObject.getRequestContext().currentUser().getName();
			}
			value += "," + userName;
		}
		if (value.length()>0)
			value = value.substring(1);
		
		return value;
	}


	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getOrder() {
		return order;
	}

	public void setOrder(String order) {
		this.order = order;
	}

	public String getOnmainpage() {
		return onmainpage;
	}

	public void setOnmainpage(String onmainpage) {
		this.onmainpage = onmainpage;
	}

	public String getZone() {
		return zone;
	}

	public void setZone(String zone) {
		this.zone = zone;
	}

	public String getSection() {
		return section;
	}

	public void setSection(String section) {
		this.section = section;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getFilterText() {
		return filterText;
	}

	public void setFilterText(String filterText) {
		this.filterText = filterText;
	}

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getPublication() {
		return publication;
	}

	public void setPublication(String publication) {
		this.publication = publication;
	}

	public String getAdvancedfilter() {
		return advancedfilter;
	}

	public void setAdvancedfilter(String advancedfilter) {
		this.advancedfilter = advancedfilter;
	}
	
	public boolean isUseCurrentPublication() {
		return useCurrentPublication;
	}

	public void setUseCurrentPublication(boolean useCurrentPublication) {
		this.useCurrentPublication = useCurrentPublication;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getNewscreator() {
		return newscreator;
	}

	public void setNewscreator(String newscreator) {
		this.newscreator = newscreator;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getVar() {
		return var;
	}

	public void setVar(String var) {
		this.var = var;
	}
	
	public void addVariable(String key, String value)
	{
		variables.put("%" + key + "%", value);
	}

	public int getFilterCount() {
		return filterCount;
	}

	public void setFilterCount(int filterCount) {
		this.filterCount = filterCount;
	}

	public String getSearchinhistory() {
		return searchinhistory;
	}

	public void setSearchinhistory(String searchinhistory) {
		this.searchinhistory = searchinhistory;
	}
	
	public String getShowtemporal() {
		return showtemporal;
	}

	public void setShowtemporal(String showtemporal) {
		this.showtemporal = showtemporal;
	}

	public String getNewstype() {
		return newstype;
	}

	public void setNewstype(String newstype) {
		this.newstype = newstype;
	}
}
