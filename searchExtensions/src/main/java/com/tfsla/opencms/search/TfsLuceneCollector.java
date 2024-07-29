package com.tfsla.opencms.search;

import java.util.List;
import java.util.ArrayList;

import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.opencms.file.CmsDataAccessException;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.collectors.A_CmsResourceCollector;
import org.opencms.main.CmsException;
import org.opencms.search.CmsSearchResult;
import org.opencms.search.TfsAdvancedSearch;

public class TfsLuceneCollector  extends A_CmsResourceCollector{

	protected String query;
	protected String searchIndex="DIARIO_CONTENIDOS_ONLINE";
	protected int maxResults=100;
	protected int size=10;
	protected int page = 1;
	
	protected String sOrderBy = "";
	protected Sort Order=null;
	
	public List getCollectorNames() {
		List<String> nombres = new ArrayList<String>();
		nombres.add("TfsLuceneCollector");
		return nombres;
	}

	public String getCreateLink(CmsObject cms, String collectorName,
			String param) throws CmsException, CmsDataAccessException {
		return getCreateInFolder(cms, param);
	}

	public String getCreateParam(CmsObject cms, String collectorName, String param) throws CmsDataAccessException {
		return param;
	}

	public List getResults(CmsObject cms, String collectorName, String param) throws CmsDataAccessException, CmsException {

		parseParam(param,cms);
		
		List<CmsResource> noticias = new ArrayList<CmsResource>();

		TfsAdvancedSearch adSearch = new TfsAdvancedSearch();
		adSearch.init(cms);
		adSearch.setQuery(query);
		adSearch.setMaxResults(maxResults);
		adSearch.setMatchesPerPage(size);
		adSearch.setIndex(searchIndex);
		adSearch.setPage(page);
		
		SortField[] camposOrden = null;
		
		if (sOrderBy!=null && sOrderBy.trim().length()>0)
		{
			String[] orderParts = sOrderBy.split(",");
			camposOrden = new SortField[orderParts.length];
			int pos = 0;
			for (String part : orderParts)
			{
				String[] a = part.trim().split(" ");
				if (a.length==1)
					camposOrden[pos] = new SortField(a[0], SortField.Type.STRING);
				else
					camposOrden[pos] = new SortField(a[0], SortField.Type.STRING,a[1].trim().toLowerCase().equals("asc"));
				pos++;
			}
		}
		else
		{
			camposOrden = new SortField[1];
			camposOrden[0] = new SortField("ultimaModificacion",SortField.Type.INT,true);
		}

		adSearch.setSortOrder(new Sort(camposOrden));

		List<CmsSearchResult> resultados = adSearch.getSearchResult();
		
		if (resultados!=null)
			for (CmsSearchResult resultado : resultados)
			{
	 			String path = cms.getRequestContext().removeSiteRoot(resultado.getPath());
	 			try {
	 				noticias.add(cms.readResource(path));
				
	 			} catch (CmsException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		
		return noticias;
	}

	private void parseParam(String params,CmsObject cms) throws RuntimeException, CmsException
	{
		String[] values = params.split("\\|");

		int i;
		for (i=0; i<values.length;i++)
		{
			String[] param = values[i].split(":");
			if (param[0].equalsIgnoreCase("searchindex"))
				searchIndex = param[1];
			else if (param[0].equalsIgnoreCase("query"))
				query =  values[i].replaceAll("query:", "");
			else if (param[0].equalsIgnoreCase("size"))
				size = Integer.parseInt(param[1]);
			else if (param[0].equalsIgnoreCase("page"))
				page = Integer.parseInt(param[1]);
			else if (param[0].equalsIgnoreCase("maxresults"))
				maxResults = Integer.parseInt(param[1]);
			else if (param[0].equalsIgnoreCase("order"))
				sOrderBy = param[1];
		}
	}
}
