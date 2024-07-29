package com.tfsla.opencms.webusers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsUUID;

import com.tfsla.collections.CollectionFactory;
import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.TipoEdicionBaseService;
import com.tfsla.opencms.exceptions.ProgramException;
import com.tfsla.opencms.webusers.params.A_Param;
import com.tfsla.opencms.webusers.params.A_SimpleParam;
import com.tfsla.opencms.webusers.params.FilterParam;
import com.tfsla.opencms.webusers.params.RangePrimaryParams;
import com.tfsla.workflow.QueryBuilder;
import com.tfsla.workflow.ResultSetProcessor;

public class SearchUsers {
	
	public static final String CMS_USERS = "CMS_USERS";
	public static final String CMS_USERDATA = "CMS_USERDATA";
	public static final String USER_NAME = "USER_NAME";
	public static final String USER_OU = "USER_OU";
	public static final String USER_FIRSTNAME = "USER_FIRSTNAME";
	public static final String USER_LASTNAME = "USER_LASTNAME";
	public static final String USER_EMAIL = "USER_EMAIL";
	public static final String USER_ID = "USER_ID";
	public static final String DATA_KEY = "DATA_KEY";
	public static final String DATA_VALUE = "DATA_VALUE";
	public static final String USER_COUNTRY = "USER_COUNTRY";
	public static final String USER_PENDING = "USER_PENDING"; 
	
	static final String defaultUserOU = "/webUser/";
	private String ou;
		
	private static Map<String, SearchUsers> instances = new HashMap<String, SearchUsers>();

	public synchronized static SearchUsers getInstance(CmsObject cms) {
		
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

    	String id = siteName + "||" + publication;

    	SearchUsers instance = instances.get(id);
		
		if (instance == null) {
			
			instance = new SearchUsers(cms,siteName, publication);
	    	instances.put(id, instance);
	    	
		}

		return instance;
	}

	private SearchUsers(CmsObject cms,String siteName, String publication) {
		
		String module = "webusers";
 		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
 		
		ou =  config.getParam(siteName, publication, module, "usersOu",defaultUserOU);

	}
	
	public List<CmsUser> Search(CmsObject cms, List<A_Param> params, int pageSize, int pageNumber) {
		String query = "SELECT "+CMS_USERS+"."+USER_ID+" FROM "+CMS_USERS+" ";
		if (params.size()>0) {
			query += " WHERE ";
		
			for (A_Param param : params) {
				query += " AND " + param.generateSubClause() ;
			}
			
			query = query.replaceFirst(" AND ", "");
		}
		query += " LIMIT " + pageSize + " OFFSET "
				+ (new Integer(pageNumber) - 1) * pageSize+"";
		
		QueryBuilder<List<CmsUser>> queryBuilder = new QueryBuilder<List<CmsUser>>(cms);
		queryBuilder.setSQLQuery(query);

		for (A_Param param : params)
			for (Object value : param.getParams())
				queryBuilder.addParameter(value);
					
		ResultSetProcessor<List<CmsUser>> proc = new getUserIdListProcessor(cms);

		return queryBuilder.execute(proc);
				
	}
	
	public List<CmsUser> SearchPlus(CmsObject cms, List<A_Param> params, int pageSize, int pageNumber, String order) {
		return this.SearchPlus(cms, params, pageSize, pageNumber, order, false);
	}
	
	public List<CmsUser> SearchPlus(CmsObject cms, List<A_Param> params, int pageSize, int pageNumber, String order, Boolean andFilter) {
		
		String selectFieldOrder = "";
		
		if (order!= null && !order.equals("")){
			
			selectFieldOrder = order;
			selectFieldOrder = selectFieldOrder.replace("desc", "");
			selectFieldOrder = selectFieldOrder.replace("asc", "");
			selectFieldOrder = selectFieldOrder.replace("DESC", "");
			selectFieldOrder = selectFieldOrder.replace("ASC", "");
			selectFieldOrder = "," + selectFieldOrder;
		}
		
		String query = "SELECT DISTINCT "+CMS_USERS+"."+USER_ID+" "+selectFieldOrder+" FROM "+CMS_USERS+" INNER JOIN "+CMS_USERDATA+" ON "+CMS_USERS+"."+USER_ID+"="+CMS_USERDATA+"."+USER_ID;
		
		if (params.size()>0) {
			query += " WHERE ";
			int count=0;
			
			for (A_Param param : params) {
				if(andFilter) {
					if(param instanceof A_SimpleParam && !(param instanceof RangePrimaryParams) && !(param instanceof FilterParam)) {
						Object value = ((A_SimpleParam)param).getValue();
						if(value == null || value.equals("")) continue;
					}
				}
				if (count==0) {
					query += " " + param.generateSubClause() ;
				} else {
					if(!param.getOperator().equals("LIKE") || andFilter) {
						query += " AND " + param.generateSubClause() ;
					} else {
						query += " OR " + param.generateSubClause() ;
					}
				}
				count++;
			}
		}
	
		if (!order.equals("")){
			query = query + " ORDER By " + order;
		}
		
		query += " LIMIT " + pageSize + " OFFSET "
				+ (new Integer(pageNumber) - 1) * pageSize+"";
		
		QueryBuilder<List<CmsUser>> queryBuilder = new QueryBuilder<List<CmsUser>>(cms);
		queryBuilder.setSQLQuery(query);
		
		for (A_Param param : params) {
			for (Object value : param.getParams()) {
				if(param instanceof A_SimpleParam && !(param instanceof RangePrimaryParams) && !(param instanceof FilterParam)) {
					if(value == null || value.equals("")) continue;
				}
				queryBuilder.addParameter(value);
			}
		}
		
		ResultSetProcessor<List<CmsUser>> proc = new getUserIdListProcessor(cms);

		return queryBuilder.execute(proc);
	}
	
	public List<String> Search(CmsObject cms, String texto, String pais, List<String> atributosAdicionales,List<String> valoresAdicionales, int PageSize, int pageNumber) {
		return Search(cms, ou, true, texto, pais, atributosAdicionales,valoresAdicionales, PageSize, pageNumber);
	}
	
	//TODO: seguir para que onlyactivos se tome por parametro
	public List<String> Search(CmsObject cms, String ou, boolean onlyActivos, String texto, String pais, List<String> atributosAdicionales,List<String> valoresAdicionales, int PageSize, int pageNumber) {
		
		if (ou==null) ou = defaultUserOU;
		
		String QrTexto = "";
		
		if(texto!=null && !texto.equals("")){
			QrTexto = " AND ( "+USER_NAME+" like '%"+texto+"%' OR "+USER_FIRSTNAME+" like '%"+texto+"%' OR "+USER_LASTNAME+" like '%"+texto+"%' OR "+USER_EMAIL+" like '%"+texto+"%')";
		}
		
		String QrPaisWhere = "";
		String QrPaisFrom = "";
		if(pais!=null) {
			QrPaisFrom = ", (SELECT DISTINCT("+CMS_USERDATA+"."+USER_ID+") FROM "+CMS_USERDATA+" WHERE "+CMS_USERDATA+"."+DATA_KEY+"='"+USER_COUNTRY+"' AND "+CMS_USERDATA+"."+DATA_VALUE+"= '"+pais+"') AS USER_COUNTRY";
			QrPaisWhere = " AND "+CMS_USERS+"."+USER_ID+" = "+USER_COUNTRY+"."+USER_ID+" ";
		}
		
		String QrExtrasFrom ="";
		String QrExtrasWhere = "";
		int i =0;
		for (String atributoAdicional : atributosAdicionales) {
			QrExtrasFrom = QrExtrasFrom +" ,(SELECT DISTINCT(CMS_USERDATA.USER_ID) FROM CMS_USERDATA WHERE "+CMS_USERDATA+"."+DATA_KEY+"='"+atributoAdicional+"' AND "+CMS_USERDATA+"."+DATA_VALUE+" ='"+valoresAdicionales.get(i)+"') as "+atributoAdicional+" ";
			QrExtrasWhere = QrExtrasWhere+" AND CMS_USERS.USER_ID = "+atributoAdicional+".USER_ID ";
			i++;
		}	
		
		String Limit = "";
		if(PageSize > 0) {
			Limit = "LIMIT " + PageSize + " OFFSET "
				+ (new Integer(pageNumber) - 1) * PageSize+"";
		}
		
		String query = "SELECT DISTINCT("+CMS_USERS+"."+USER_ID+"), "+CMS_USERS+"."+USER_NAME+" FROM "+CMS_USERS+", "+CMS_USERDATA+" ";
		
		if (onlyActivos)
			query +=",(SELECT DISTINCT("+CMS_USERDATA+"."+USER_ID+") FROM "+CMS_USERDATA+" WHERE "+CMS_USERDATA+"."+DATA_KEY+"='"+USER_PENDING+"' AND "+CMS_USERDATA+"."+DATA_VALUE+"= 'false') as ACTIVOS ";
		
		query +=
				QrPaisFrom +
				QrExtrasFrom +
				" WHERE "+USER_OU+"='"+ou+"' " ;
		if (onlyActivos)
			query += "AND "+CMS_USERS+"."+USER_ID+" = ACTIVOS."+USER_ID;
		query += " AND "+CMS_USERS+"."+USER_ID+" = "+CMS_USERDATA+"."+USER_ID+" " +
				QrTexto +
				QrPaisWhere +
				QrExtrasWhere + 
				" ORDER BY "+USER_NAME+" DESC  " + Limit;
		
		CmsLog.getLog(this).debug(query);
		
		QueryBuilder<List<String>> queryBuilder = new QueryBuilder<List<String>>(cms);
		queryBuilder.setSQLQuery(query);
		
		ResultSetProcessor<List<String>> proc = this.getUsuariosListProcessor();

		return queryBuilder.execute(proc);
	}

	private final class getUserIdListProcessor implements ResultSetProcessor<List<CmsUser>> {
		private final CmsObject cms;

		private List<CmsUser> results = CollectionFactory.createList();

		private getUserIdListProcessor(CmsObject cms) {
			this.cms = cms;
		}

		public void processTuple(ResultSet rs) {

			try {
				
				String userid = rs.getString(USER_ID);
				try {
					this.results.add(cms.readUser(new CmsUUID(userid)));
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (CmsException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			catch (SQLException e) {
				throw ProgramException.wrap("error al intentar obtener los usuarios de la base", e);
			}
		}

		public List<CmsUser> getResult() {
			return this.results;
		}
	}

	private ResultSetProcessor<List<String>> getUsuariosListProcessor() {
		ResultSetProcessor<List<String>> proc = new ResultSetProcessor<List<String>>() {

		private List<String> results = CollectionFactory.createList();

		public void processTuple(ResultSet rs) {

				try {
					String userName = rs.getString(USER_NAME);

					this.results.add(userName);
				}
				catch (SQLException e) {
					throw ProgramException.wrap("error al intentar obtener los usuarios de la base", e);
				}
			}

			public List<String> getResult() {
				return this.results;
			}

		};
		return proc;
	}
	
	public int SearchUserCount(CmsObject cms, String texto, String pais, List<String> atributosAdicionales,List<String> valoresAdicionales){
		
		String QrTexto = "";
		
		if(!texto.equals("") && texto!=null){
			QrTexto = " AND ( "+USER_NAME+" like '%"+texto+"%' OR "+USER_FIRSTNAME+" like '%"+texto+"%' OR "+USER_LASTNAME+" like '%"+texto+"%' OR "+USER_EMAIL+" like '%"+texto+"%')";
		}
		
		String QrPaisWhere = "";
		String QrPaisFrom = "";
		if(pais!=null){
			QrPaisFrom = ", (SELECT DISTINCT("+CMS_USERDATA+"."+USER_ID+") FROM "+CMS_USERDATA+" WHERE "+CMS_USERDATA+"."+DATA_KEY+"='"+USER_COUNTRY+"' AND "+CMS_USERDATA+"."+DATA_VALUE+"= '"+pais+"') AS USER_COUNTRY";
			QrPaisWhere = " AND "+CMS_USERS+"."+USER_ID+" = "+USER_COUNTRY+"."+USER_ID+" ";
		}
		
		String QrExtrasFrom ="";
		String QrExtrasWhere = "";
		int i =0;
		for (String atributoAdicional : atributosAdicionales) {
			QrExtrasFrom = QrExtrasFrom +" ,(SELECT DISTINCT(CMS_USERDATA.USER_ID) FROM CMS_USERDATA WHERE "+CMS_USERDATA+"."+DATA_KEY+"='"+atributoAdicional+"' AND "+CMS_USERDATA+"."+DATA_VALUE+" ='"+valoresAdicionales.get(i)+"') as "+atributoAdicional+" ";
			QrExtrasWhere = QrExtrasWhere+" AND CMS_USERS.USER_ID = "+atributoAdicional+".USER_ID ";
			i++;
		}	
		
		String query = "SELECT COUNT(DISTINCT("+CMS_USERS+"."+USER_ID+")) FROM "+CMS_USERS+", "+CMS_USERDATA+" " +
		",(SELECT DISTINCT("+CMS_USERDATA+"."+USER_ID+") FROM "+CMS_USERDATA+" WHERE "+CMS_USERDATA+"."+DATA_KEY+"='"+USER_PENDING+"' AND "+CMS_USERDATA+"."+DATA_VALUE+"= 'false') as ACTIVOS "+ 
		QrPaisFrom +
		QrExtrasFrom +
		" WHERE "+USER_OU+"='"+ou+"' AND "+CMS_USERS+"."+USER_ID+" = ACTIVOS."+USER_ID+" AND "+CMS_USERS+"."+USER_ID+" = "+CMS_USERDATA+"."+USER_ID+" " +
		QrTexto +
		QrPaisWhere +
		QrExtrasWhere;
		
		CmsLog.getLog(this).debug(query);
		
		QueryBuilder<Integer> queryBuilder = new QueryBuilder<Integer>(cms);
		queryBuilder.setSQLQuery(query);
		
		ResultSetProcessor<Integer> proc = new ResultSetProcessor<Integer>() {

			private int count = 0;

			public void processTuple(ResultSet rs) {

				try {
					this.count = rs.getInt(1);
				}
				catch (SQLException e) {
					throw ProgramException.wrap(
							"error al intentar recuperar la cantidad de usuarios de la base", e);
				}
			}

			public Integer getResult() {
				return this.count;
			}
		};

		return queryBuilder.execute(proc);
	}

}
