package com.tfsla.diario.newsCollector;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Date;
import java.util.Collections;
import java.util.regex.Pattern;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;

import com.tfsla.collections.CollectionFactory;
import com.tfsla.diario.ediciones.services.openCmsService;
import com.tfsla.diario.friendlyTags.TfsNoticiasListTag;
import com.tfsla.diario.newsCollector.comparators.CompositeComparator;
import com.tfsla.diario.newsCollector.comparators.PropertyComparator;
import com.tfsla.diario.newsCollector.order.OrderDirective;
import com.tfsla.diario.newsCollector.order.ResultOrderManager;
import com.tfsla.opencmsdev.CmsResourceExtended;
import com.tfsla.opencms.dev.collector.PriorityDynamicComparator;
import com.tfsla.workflow.QueryBuilder;
import com.tfsla.workflow.ResourceTaskSqlManager;
import com.tfsla.workflow.ResultSetProcessor;

public class EdicionOnlineHomeNewsCollector extends A_NewsCollector {

	private static Log LOG = LogFactory.getLog(EdicionOnlineHomeNewsCollector.class);

	private CmsResourceFilter resourceFilter =null;
	
	public EdicionOnlineHomeNewsCollector()
	{
		supportedOrders.add(OrderDirective.ORDER_BY_CREATIONDATE);
		supportedOrders.add(OrderDirective.ORDER_BY_MODIFICATIONDATE);
		supportedOrders.add(OrderDirective.ORDER_BY_SECTION);
		supportedOrders.add(OrderDirective.ORDER_BY_PRIORITY);
		supportedOrders.add(OrderDirective.ORDER_BY_USERMODIFICATIONDATE);
		supportedOrders.add(OrderDirective.ORDER_BY_ZONE);
		supportedOrders.add(OrderDirective.ORDER_BY_PRIORITY);
		supportedOrders.add(OrderDirective.ORDER_BY_PRIORITY_SECTION);
		
	}
	
	@Override
	public boolean canCollect(Map<String, Object> parameters) {

		if (paramValueIsEmpty(parameters.get(TfsNoticiasListTag.param_onmainpage)))
			return false;

		//Solo aplicar este collector si se debe tomar noticias de alguna homa de la edicion impresa (home o section).
		String[] values = getValues((String)parameters.get(TfsNoticiasListTag.param_onmainpage));
		if (values==null)
			return false;
		
		for (String value : values)
			if (value.trim().equals("anywhere") || value.trim().equals("none"))
				return false;

		int numParams = 1;

		
		if (parameters.get(TfsNoticiasListTag.param_filter)!=null)
			numParams++;

		if (parameters.get(TfsNoticiasListTag.param_size)!=null)
			numParams++;

		if (parameters.get(TfsNoticiasListTag.param_page)!=null)
			numParams++;
		
		if (parameters.get(TfsNoticiasListTag.param_order)!=null)
			numParams++;

		if (parameters.get(TfsNoticiasListTag.param_zone)!=null)
			numParams++;

		if (parameters.get(TfsNoticiasListTag.param_section)!=null)
			numParams++;

		if (parameters.get(TfsNoticiasListTag.param_from)!=null)
			numParams++;

		if (parameters.get(TfsNoticiasListTag.param_to)!=null)
			numParams++;

		if ((Integer)parameters.get(TfsNoticiasListTag.param_numberOfParamters)>numParams)
			return false;

		return true;
	}

	@Override
	protected List<CmsResource> sortNews(List<CmsResource> resources, Map<String, Object> parameters, CmsObject cms) {
		String order = (String)parameters.get(TfsNoticiasListTag.param_order);
		String onmainpage = (String)parameters.get(TfsNoticiasListTag.param_onmainpage);
		
		//OrderDirective prio = null;
		String target = "";
		
		if (onmainpage.contains("home"))
			target = "home";//prio = OrderDirective.ORDER_BY_PRIORITY;
		else
			target = "section";//prio = OrderDirective.ORDER_BY_PRIORITY_SECTION;		
			
		CompositeComparator<CmsResource> comp = new CompositeComparator<CmsResource>();
		
		List<OrderDirective> orderby = ResultOrderManager.getOrderConfiguration(order);
		
		for (OrderDirective od : orderby)		
		{
			if (od.equals(OrderDirective.ORDER_BY_PRIORITY)){
				//comp.addComparator(new PropertyComparator(cms,prio.getPropertyName(),prio.getType(),od.isAscending()));
				comp.addComparator(new PriorityDynamicComparator(cms, target + ".priority", !od.isAscending()));
			}
			else{				
				comp.addComparator(new PropertyComparator(cms,od.getPropertyName(),od.getType(),od.isAscending()));
			}
		}
		Collections.sort(resources, comp);
		
		return resources;
	}

	@Override
	public List<CmsResource> collectNews(Map<String, Object> parameters,
			CmsObject cms) {
		
		String[] secciones = getValues((String)parameters.get(TfsNoticiasListTag.param_section));
		String[] zonas = getValues((String)parameters.get(TfsNoticiasListTag.param_zone));
		String[] paginas = getValues((String)parameters.get(TfsNoticiasListTag.param_onmainpage));

		String filtro =  (String)parameters.get(TfsNoticiasListTag.param_filter);

		int size = Integer.MAX_VALUE;
		
		if (parameters.get(TfsNoticiasListTag.param_size)!=null)
			size = (Integer)parameters.get(TfsNoticiasListTag.param_size);

		int page = (Integer)parameters.get(TfsNoticiasListTag.param_page);

		resourceFilter = (CmsResourceFilter) parameters.get(TfsNoticiasListTag.param_resourcefilter);	
		if (resourceFilter == null)
			resourceFilter = CmsResourceFilter.DEFAULT;
		
		List<CmsResource> resourcesFiltradas = new ArrayList<CmsResource>();

		for (String pagina : paginas)
		{
			
			String target=pagina;
		
			List<CmsResource> resources;
			NoticiasMostradasResultSetProcessor proc = new NoticiasMostradasResultSetProcessor(cms);

			//String propertyName = target + ".zone";
			//String propertyDefIDAsString = getZonePropertyDefId(cms, propertyName);
			
			String site = openCmsService.getCurrentSite(cms);
			String publication = openCmsService.getPublicationNameFromContext(cms);

			QueryBuilder<List<CmsResource>> queryBuilder = null;
			if (secciones==null && zonas==null)
				queryBuilder = getQueryBuilder(cms, site, publication, pagina);
			else if (secciones==null && zonas.length==1)
				queryBuilder = getQueryBuilderAltoTransitoFullZona(cms, site, publication, target, (String)parameters.get(TfsNoticiasListTag.param_zone));
			else if (secciones==null && zonas.length>1)
				queryBuilder = getQueryBuilderSetAltoTransitoFullZona(cms, site, publication, target, (String)parameters.get(TfsNoticiasListTag.param_zone));
			else if (zonas==null && secciones.length==1)
				queryBuilder = getQueryBuilderAltoTransitoFullSection(cms, site, publication, target, (String)parameters.get(TfsNoticiasListTag.param_section));
			else if (zonas==null && secciones.length>1)
				queryBuilder = getQueryBuilderSetAltoTransitoFullSection(cms, site, publication, target, (String)parameters.get(TfsNoticiasListTag.param_section));
			else if (zonas.length==1 && secciones.length==1)
				queryBuilder = getQueryBuilderAltoTransito(cms, site, publication, target, (String)parameters.get(TfsNoticiasListTag.param_zone), (String)parameters.get(TfsNoticiasListTag.param_section));
			else if (zonas.length>=1 && secciones.length>=1)
				queryBuilder = getQueryBuilderSetAltoTransito(cms, site, publication, target, (String)parameters.get(TfsNoticiasListTag.param_zone), (String)parameters.get(TfsNoticiasListTag.param_section));
			else
				queryBuilder = getQueryBuilder(cms, site, publication, pagina);			

			resources = queryBuilder.execute(proc);
	
			String from = (String)parameters.get(TfsNoticiasListTag.param_from);
			String to = (String)parameters.get(TfsNoticiasListTag.param_to);

			Date desde = parseDateTime(from);
			Date hasta = parseDateTime(to);
			
			for (CmsResource res : resources)
			{
				LOG.debug(res.getRootPath());
				String sUltimaModif = propertyValue(res,"ultimaModificacion",cms);
				Date ultimaModificacion = new Date(Long.parseLong(sUltimaModif));
				
				if (desde!=null && desde.after(ultimaModificacion))
					continue;
						
				if (hasta!=null && hasta.before(ultimaModificacion))
					continue;
				
				//if (!isInFrontPage(res, cms, publication,pagina,secciones, zonas))
				//	continue;
				
				
				//if (!propertyValueInList(res,"seccion",secciones,false,cms))
				//	continue;
	
				//if (!propertyValueInList(res,propertyName,zonas,false, cms))
				//	continue;
	
				if (filtro!=null && !filtro.trim().equals("") && !propertyValueInList(res,"title",new String[] {filtro},true, cms))
					continue;
			
				if (!resourcesFiltradas.contains(res))
					resourcesFiltradas.add(res);
			}
		}
		
		resourcesFiltradas = sortNews(resourcesFiltradas,parameters,cms);
		
		//pagino los resultados.
		int fromNews = (page-1)*size;
		int toNews = page*size -1;
		
		if (fromNews>=resourcesFiltradas.size())
			return Collections.emptyList();
		
		if (toNews>=resourcesFiltradas.size())
			toNews = resourcesFiltradas.size()-1;
		
		return resourcesFiltradas.subList(fromNews, toNews+1);

		//return (size >= resourcesFiltradas.size()) ? resourcesFiltradas : resourcesFiltradas.subList(0, size);

	}

	protected String readProperty(CmsResource resource,CmsObject cms, String propertyName) {
		String value;
		try {
			
			int index = ((CmsResourceExtended)resource).getIndexPublication(); 
			
			String correctedPropertyName = (index == 0 ? propertyName.replaceAll(Pattern.quote("{$1}"), "") : propertyName.replaceAll(Pattern.quote("{$1}"), "" + index));
			
			value = cms.readPropertyObject(resource, correctedPropertyName, false).getValue();
			
		} catch (CmsException e) {
			throw new RuntimeException(e);
		}
		return value != null ? value : "";
	}

	protected boolean isInFrontPage(CmsResource res, CmsObject cms, String publication,String page,String[] sections, String[] zones) {

		
		String propertyName = page + ".zone{$1}.hightraffic";
		String propValue = readProperty(res, cms, propertyName);
		String[] values = propValue.split("\\|");
		
		String propZone = "";
		String propSection = "";
		
		if (values.length==3){
			propZone = values[2];
			propSection = values[1];
		}
		else if (values.length==2){
			propZone = values[1];
			propSection = values[0];
		}

		if (zones==null || zones.length == 0 || ArrayUtils.contains(zones, propZone)){
			if (sections!=null){
				if (propSection!=null && propSection.trim().length()>0) {
					if (sections.length == 0 || ArrayUtils.contains(sections, propSection))
						return true;
				}
			}
			else {
				return true;
			}
			
		}
		
		return false;
	}
	
	protected String propertyValue(CmsResource res, String property, CmsObject cms)
	{
		
		CmsProperty prop = null;
		try {
			prop = cms.readPropertyObject(res, property, false);
		} catch (CmsException e) {
			e.printStackTrace();
			return null;
		}
		
		if (prop==null || prop.getValue()==null )
			return null;
		
		return prop.getValue();
		
	}
	
	protected boolean propertyValueInList(CmsResource res, String property, String[] values, boolean partial, CmsObject cms)
	{
		
		if (values==null)
			return true;
		
		CmsProperty prop = null;
		try {
			prop = cms.readPropertyObject(res, property, false);
		} catch (CmsException e) {
			e.printStackTrace();
			return false;
		}
		
		if (prop==null || prop.getValue()==null )
			return false;
		
		String resValue = prop.getValue();
				
		for (String value : values)
		{
			if (partial)
			{
				if (resValue.contains(value.trim()))
					return true;
			}
			else
			{
				if (resValue.equals(value.trim()))
					return true;
			}
		}
		return false;
	}
	
	/*
	private String getZonePropertyDefId(final CmsObject cms, String propertyName) {
		CmsPropertyDefinition propertyDef;

		try {
			propertyDef = cms.readPropertyDefinition(propertyName);
			if (propertyDef == null) {
				throw new RuntimeException("No se encontro una propertyDefinition de nombre [" + propertyName
						+ "]");
			}
		}
		catch (CmsException e) {
			throw new RuntimeException("error al intentar leer la propertyDefinition de [" + propertyName
					+ "]", e);
		}

		CmsUUID propertyDefId = propertyDef.getId();
		// no me gusta colgarme del toString pero no parece tener nada mas
		String propertyDefIDAsString = propertyDefId.toString();
		return propertyDefIDAsString;
	}
	*/

	private QueryBuilder<List<CmsResource>> getQueryBuilderSetAltoTransito(final CmsObject cms, String site, String publication, String target, String zona, String seccion) {
		QueryBuilder<List<CmsResource>> queryBuilder = new QueryBuilder<List<CmsResource>>(cms);
		queryBuilder.setQueryKeyInThisProject(ResourceTaskSqlManager.TFS_GET_NOTICIAS_SET_ALTOTRANSITO)
			.addParameter(site)
				.addParameter(publication)
					.addParameter(target)
						.addParameter(seccion.replaceAll(" ", ""))
							.addParameter(zona.replaceAll(" ", ""));

		return queryBuilder;
	}

	private QueryBuilder<List<CmsResource>> getQueryBuilderSetAltoTransitoFullZona(final CmsObject cms, String site, String publication, String target, String zona) {
		QueryBuilder<List<CmsResource>> queryBuilder = new QueryBuilder<List<CmsResource>>(cms);
		queryBuilder.setQueryKeyInThisProject(ResourceTaskSqlManager.TFS_GET_NOTICIAS_SET_ALTOTRANSITO_FULLZONE)
			.addParameter(site)
				.addParameter(publication)
					.addParameter(target)
							.addParameter(zona.replaceAll(" ", ""));

		return queryBuilder;
	}
	
	private QueryBuilder<List<CmsResource>> getQueryBuilderSetAltoTransitoFullSection(final CmsObject cms, String site, String publication, String target, String seccion) {
		QueryBuilder<List<CmsResource>> queryBuilder = new QueryBuilder<List<CmsResource>>(cms);
		queryBuilder.setQueryKeyInThisProject(ResourceTaskSqlManager.TFS_GET_NOTICIAS_SET_ALTOTRANSITO_FULLSECTION)
			.addParameter(site)
				.addParameter(publication)
					.addParameter(target)
						.addParameter(seccion.replaceAll(" ", ""));

		return queryBuilder;
	}


	private QueryBuilder<List<CmsResource>> getQueryBuilderAltoTransito(final CmsObject cms, String site, String publication, String target, String zona, String seccion) {
		QueryBuilder<List<CmsResource>> queryBuilder = new QueryBuilder<List<CmsResource>>(cms);
		queryBuilder.setQueryKeyInThisProject(ResourceTaskSqlManager.TFS_GET_NOTICIAS_ALTOTRANSITO)
			.addParameter(site)
				.addParameter(publication)
					.addParameter(target)
						.addParameter(seccion)
							.addParameter(zona);

		return queryBuilder;
	}

	private QueryBuilder<List<CmsResource>> getQueryBuilderAltoTransitoFullZona(final CmsObject cms, String site, String publication, String target, String zona) {
		QueryBuilder<List<CmsResource>> queryBuilder = new QueryBuilder<List<CmsResource>>(cms);
		queryBuilder.setQueryKeyInThisProject(ResourceTaskSqlManager.TFS_GET_NOTICIAS_ALTOTRANSITO_FULLZONE)
			.addParameter(site)
				.addParameter(publication)
					.addParameter(target)
							.addParameter(zona);

		return queryBuilder;
	}
	
	private QueryBuilder<List<CmsResource>> getQueryBuilderAltoTransitoFullSection(final CmsObject cms, String site, String publication, String target, String seccion) {
		QueryBuilder<List<CmsResource>> queryBuilder = new QueryBuilder<List<CmsResource>>(cms);
		queryBuilder.setQueryKeyInThisProject(ResourceTaskSqlManager.TFS_GET_NOTICIAS_ALTOTRANSITO_FULLSECTION)
			.addParameter(site)
				.addParameter(publication)
					.addParameter(target)
						.addParameter(seccion);

		return queryBuilder;
	}

	
	
	private QueryBuilder<List<CmsResource>> getQueryBuilder(final CmsObject cms, String site, String publication, String target) {
		QueryBuilder<List<CmsResource>> queryBuilder = new QueryBuilder<List<CmsResource>>(cms);
		queryBuilder.setQueryKeyInThisProject(ResourceTaskSqlManager.TFS_GET_NOTICIAS_MOSTRADAS_2)
			.addParameter(site)
				.addParameter(publication)
					.addParameter(target);		

		return queryBuilder;
	}
	
	private final class NoticiasMostradasResultSetProcessor implements ResultSetProcessor<List<CmsResource>> {
		private final CmsObject cms;

		private List<CmsResource> list = CollectionFactory.createList();

		private NoticiasMostradasResultSetProcessor(CmsObject cms) {
			this.cms = cms;
		}

		public void processTuple(ResultSet rs) {
			String url = null;
			try {
				url = rs.getString("RESOURCE_PATH");

				String siteName = OpenCms.getSiteManager().getCurrentSite(this.cms).getTitle();
				url = url.substring(siteName.length(), url.length());
				if (!url.contains("~") && url.contains("contenidos/")) {
					
					String propertyName = rs.getString("PROPERTYDEF_NAME");
					int indexPublication = 0;
					
					try{
						//Obtengo el ultimo caracter para buscar el indice de publicacion, donde se guardo la prioridad
						indexPublication = Integer.parseInt(propertyName.substring(propertyName.length()-1));
					}
					catch(Exception ex){
						indexPublication = 0;
					}
					
					CmsResource resource = this.cms.readResource(url,resourceFilter);
					this.list.add(CmsResourceExtended.getInstance(resource, indexPublication));
					
					//this.list.add(this.cms.readResource(url));
				}
			}
			catch (CmsException e) {
				//NO ES UN ERROR
				//log.error("No se pudo leer la noticia " + url, e);
			}
			catch (SQLException e) {
				LOG.error("No se pudo leer la noticia " + url, e);
			}
		}

		public List<CmsResource> getResult() {
			return this.list;
		}
		
	}



}
