package com.tfsla.rankViews.collector;

import java.text.ParseException;

import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.commons.logging.Log;
import org.opencms.file.CmsDataAccessException;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.collectors.A_CmsResourceCollector;
import org.opencms.file.types.CmsResourceTypeXmlContent;

import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import com.tfsla.rankViews.model.TfsRankResults;
import com.tfsla.rankViews.service.RankService;
import com.tfsla.statistics.model.TfsStatisticsOptions;
import com.tfsla.statistics.model.TfsHitPage;

public abstract class A_ServerStatisticsCollector extends A_CmsResourceCollector {

	private static final Log LOG = CmsLog.getLog(A_ServerStatisticsCollector.class);

	protected final String FECHACREACION = "FECHACREACION";
	protected final String FECHAMODIFICACION = "FECHAMODIFICACION";
	protected final String FECHAULTIMAMODIFICACION = "FECHAULTIMAMODIFICACION";

	protected Calendar desdeHorasVisitas = null;
	protected Calendar hastaHorasVisitas = new GregorianCalendar();


	protected String sitio = null;
	protected String seccion = null;
	protected String campo = null;
	protected int number = 20;
	protected int pagina = 1;
	protected int edicion = 0;
	protected int tipoEdicion = -1;
	protected int horasVisitas=24;
	protected int horasNoticia=-1;
	protected String tipoContenido = null;
	protected String autor = null;
	protected String[] tags = null;

	public String getCreateLink(CmsObject cms, String collectorName, String param) throws CmsException, CmsDataAccessException {
		return getCreateInFolder(cms, param);
	}

	public String getCreateParam(CmsObject cms, String collectorName,
			String param) throws CmsDataAccessException {
		return param;
	}

	protected void parseParam(String params,CmsObject cms) throws RuntimeException, CmsException
	{
		
		desdeHorasVisitas = null;
		hastaHorasVisitas = new GregorianCalendar();

		sitio = null;
		seccion = null;
		campo = null;
		number = 20;
		edicion = 0;
		tipoEdicion = -1;
		horasVisitas=24;
		horasNoticia=-1;
		tipoContenido = null;
		autor = null;
		tags = null;
		
		
		hastaHorasVisitas.add(Calendar.DAY_OF_MONTH, 1);

		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm");
		String[] values = params.split("\\|");
		int i;
		seccion=null;
		for (i=0; i<values.length;i++)
		{
			String[] param = values[i].split(":");
			if (param[0].equalsIgnoreCase("seccion"))
			{
				seccion = param[1];
			}
			else if (param[0].equalsIgnoreCase("sitio"))
			{
				if (!param[1].trim().equals("*"))
					sitio = param[1];
			}
			else if (param[0].equalsIgnoreCase("cantidad"))
			{
				number = Integer.parseInt(param[1]);
			}
			else if (param[0].equalsIgnoreCase("pagina"))
			{
				pagina = Integer.parseInt(param[1]);
			}
			else if (param[0].equalsIgnoreCase("edicion") /*&& masVisitadosConfiguration.getInstance().getUsarGestorEdiciones()*/)
			{
				edicion = Integer.parseInt(param[1]);
			}
			else if (param[0].equalsIgnoreCase("tipoEdicion") /*&& masVisitadosConfiguration.getInstance().getUsarGestorEdiciones()*/)
			{
				tipoEdicion = Integer.parseInt(param[1]);
			}
			else if (param[0].equalsIgnoreCase("tipoContenido"))
			{
				tipoContenido = param[1]; 
			}
			else if (param[0].equalsIgnoreCase("autor"))
			{
				autor = param[1]; 
			}
			else if (param[0].equalsIgnoreCase("tags"))
			{
				tags = param[1].split(","); 
			}
			else if (param[0].equalsIgnoreCase("horasVisitas"))
			{
				horasVisitas = Integer.parseInt(param[1]);
			}
			else if (param[0].equalsIgnoreCase("horasCreacion"))
			{
				horasNoticia = Integer.parseInt(param[1]);
				campo = FECHACREACION;
			}
			else if (param[0].equalsIgnoreCase("horasModificacion"))
			{
				horasNoticia = Integer.parseInt(param[1]);
				campo = FECHAMODIFICACION;
			}
			else if (param[0].equalsIgnoreCase("horasUltimaModificacion"))
			{
				horasNoticia = Integer.parseInt(param[1]);
				campo = FECHAULTIMAMODIFICACION;
			}
			else if (param[0].equalsIgnoreCase("desdeHorasVisita"))
			{
				desdeHorasVisitas = new GregorianCalendar();
				try {
					desdeHorasVisitas.setTime(sdf.parse(param[1]));
				} catch (ParseException e) {
					LOG.error(e);
				}
				
			}
			else if (param[0].equalsIgnoreCase("hastaHorasVisitas"))
			{
				try {
					hastaHorasVisitas.setTime(sdf.parse(param[1]));
				} catch (ParseException e) {
					LOG.error(e);
				}				
			}
			
		}
		// Si el sitio no llego le paso el sitio actual.
		if (sitio==null)
			sitio = getSiteName(cms);
			
	}

	protected String getSiteName(CmsObject cms)
	{
		String siteName = OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot();
		siteName = siteName.replace("/sites/", "");
		return siteName;
	}
	
	public List<CmsResource>  getResources(TfsStatisticsOptions options, CmsObject cms)
	{
		List<CmsResource> resourcesInFolder = new ArrayList<CmsResource>();

       

		//try {
			
			RankService rService = new RankService();
			TfsRankResults result = rService.getStatistics(cms, options);
			
			//TfsRankingViewsStub rank = new TfsRankingViewsStub();
			//GetStatistics getStatistics = new GetStatistics();
			//getStatistics.setOptions(options);
			
			//TfsRankResults result = rank.getStatistics(getStatistics).get_return();
        
			if (result.getRank()!=null)
			{
				for (TfsHitPage masVisitado : result.getRank()) {
		
					try
					{
						CmsResource res = cms.readResource(cms.getRequestContext().removeSiteRoot(masVisitado.getURL()));
						if (CmsResourceTypeXmlContent.isXmlContent(res))
							resourcesInFolder.add(res);
					}
					catch (Exception e) {
						LOG.error("Error al agregar la pagina al ranking '" + masVisitado.getURL() + "' (" + cms.getRequestContext().getSiteRoot() + ") - solicitado como '" + cms.getRequestContext().removeSiteRoot(masVisitado.getURL()) + "'" , e);
						e.printStackTrace();
					}
				}
			}

		//} catch (RemoteException e1) {
		//	LOG.error(e1);
		//	e1.printStackTrace();
		//}

		return resourcesInFolder;
	}
	
	protected TfsStatisticsOptions getOptions(int rankMode)
	{
        TfsStatisticsOptions options = new TfsStatisticsOptions();

        options.setRankMode(rankMode);
        
        if (edicion!=0)
        	options.setEdicion(edicion);
        if (tipoEdicion!=-1)
        	options.setTipoEdicion(tipoEdicion);
        if (seccion!=null)
        	options.setSeccion(seccion);
        
        if (sitio!=null)
        	options.setSitio(sitio);
        
        if (tipoContenido!=null)
        	options.setTipoContenido(tipoContenido);
        
        if (autor!=null)
        	options.setAutor(autor);
        
        if (tags==null)
        	tags = new String[0];
        
        options.setTags(tags);

        Calendar to = new GregorianCalendar();

        if (campo!=null)
        {
            Calendar fromRecurso = new GregorianCalendar();
            fromRecurso.add(Calendar.HOUR, -1 * horasNoticia);

            options.setFromDateRecurso(fromRecurso.getTime());
            options.setToDateRecurso(to.getTime());

        }
       
        options.setCount(number);
        options.setPage(pagina);

        if (desdeHorasVisitas==null)
        {
	        Calendar from = new GregorianCalendar();
	        from.add(Calendar.HOUR, -1* horasVisitas);
	        
	        
	        options.setFrom(from.getTime());
	        options.setTo(to.getTime());
        }
        else
        {
	        options.setFrom(desdeHorasVisitas.getTime());
	        options.setTo(hastaHorasVisitas.getTime());        	
        }

        return options;
	}
}
