package com.tfsla.workflow;

import java.util.HashMap;
import java.util.Map;

//import com.tfsla.opencmsdev.module.TfsConstants;



/**
 * Agrega las queries con las que extendemos el opencms.
 *
 * @author lgassman
 */
public class ResourceTaskSqlManager extends org.opencms.db.mysql.CmsSqlManager {

	public static final String TFS_RESOURCETASK_READ_BY_TASK = "TFS_RESOURCETASK_READ_BY_TASK";
	public static final String TFS_RESOURCETASK_READ_BY_RESOURCE = "TFS_RESOURCETASK_READ_BY_RESOURCE";
	public static final String TFS_RESOURCETASK_WRITE = "TFS_RESOURCETASK_WRITE";
	public static final String TFS_RESOURCETASK_DELETE = "TFS_RESOURCETASK_DELETE";

	public static final String TFS_INDEX_INSERT = "TFS_INDEX_INSERT";
	public static final String TFS_INDEX_DELETE = "TFS_INDEX_DELETE";

	public static final String TFS_STATISTIC_INSERT = "TFS_STATISTIC_INSERT";
	public static final String TFS_STATISTIC_DELETE = "TFS_STATISTIC_DELETE";
	public static final String TFS_STATISTIC_SELECT = "TFS_STATISTIC_SELECT";

	public static final String TFS_PASSWORD_UPDATE = "TFS_PASSWORD_UPDATE";
	public static final String TFS_PASSWORD_SELECT = "TFS_PASSWORD_SELECT";

	public static final String TFS_GET_NOTICIAS_MOSTRADAS = "TFS_GET_NOTICIAS_MOSTRADAS";
	public static final String TFS_GET_NOTICIAS_MOSTRADAS_2 = "TFS_GET_NOTICIAS_MOSTRADAS_2";

	public static final String TFS_GET_NOTICIAS_ALTOTRANSITO = "TFS_GET_NOTICIAS_ALTOTRANSITO";
	public static final String TFS_GET_NOTICIAS_ALTOTRANSITO_FULLSECTION = "TFS_GET_NOTICIAS_ALTOTRANSITO_FULLSECTION";
	public static final String TFS_GET_NOTICIAS_ALTOTRANSITO_FULLZONE = "TFS_GET_NOTICIAS_ALTOTRANSITO_FULLZONE";

	public static final String TFS_GET_NOTICIAS_SET_ALTOTRANSITO = "TFS_GET_SET_NOTICIAS_ALTOTRANSITO";
	public static final String TFS_GET_NOTICIAS_SET_ALTOTRANSITO_FULLSECTION = "TFS_GET_NOTICIAS_SET_ALTOTRANSITO_FULLSECTION";
	public static final String TFS_GET_NOTICIAS_SET_ALTOTRANSITO_FULLZONE = "TFS_GET_NOTICIAS_SET_ALTOTRANSITO_FULLZONE";

	private static final long serialVersionUID = 4934139232255215155L;

	private static Map<String, String> extraQuerys = new HashMap<String, String>();
	
	public ResourceTaskSqlManager() {
		super();
		loadQueryProperties(this.m_queries);
		 this.m_queries.putAll(extraQuerys);
	}

	public  static void addQueryProperty(String name, String query)
	 {
	  extraQuerys.put(name, query);
	 }
	
	/**
	 * Agrega al mapa de queries las quieries necesarias para el ResourceTask
	 * Primero lo iba a poner en un archivo de properties como est?n hechos todos los demas
	 * Pero como es algo recontra fijo (de hecho los demas est?n metidos dentro del jar)
	 * No encuentro ning?n sentido de tenerlo oculto en un .properties, y lo dejo el .java
	 *
	 */
	@SuppressWarnings("unchecked")
	public static void loadQueryProperties(Map<String, String> queryMap) {
		queryMap.put(TFS_RESOURCETASK_READ_BY_TASK,
		"SELECT TASK_ID, RESOURCE, STATE " +
		"FROM TFS_RESOURCETASK T " +
		"WHERE T.TASK_ID = ?");

		queryMap.put(TFS_RESOURCETASK_READ_BY_RESOURCE,
				"SELECT TASK_ID, RESOURCE, STATE " +
				"FROM TFS_RESOURCETASK T " +
				"WHERE T.RESOURCE = ?");

		queryMap.put(TFS_RESOURCETASK_WRITE,
				"INSERT INTO TFS_RESOURCETASK "+
			    "(TASK_ID, RESOURCE, STATE) " +
			    "VALUES (?,?,?)");

		queryMap.put(TFS_RESOURCETASK_DELETE,
				"DELETE FROM TFS_RESOURCETASK " +
				"WHERE TASK_ID = ?");


		queryMap.put(TFS_INDEX_INSERT,
				"INSERT INTO TFS_INDEX "+
			    "(WORD, UBICATION, RESOURCE) " +
			    "VALUES (?,?,?)");

		queryMap.put(TFS_INDEX_DELETE,
				"DELETE FROM TFS_INDEX "+
			    "WHERE RESOURCE = ? AND UBICATION = ?");



		queryMap.put(TFS_STATISTIC_INSERT,
							"insert into TFS_STATISTICS (URL,SITE,TIPO_EDICION,EDICION) " +
							"values (?,?,?,?)");
		queryMap.put(TFS_STATISTIC_DELETE,
							"delete from TFS_STATISTICS " +
							"where TIME < ?;");

		queryMap.put(TFS_STATISTIC_SELECT,
								"select URL, COUNT(*) as CANT " +
								"from TFS_STATISTICS " +
								"where (TIME >= ?) " +
								"group by URL " +
								"order by CANT desc " +
								"limit ?");

		queryMap.put(TFS_PASSWORD_SELECT,
			"select PASSWORD " +
			"from TFS_PASSWORD " +
			"where PASSWORD = ?");

		queryMap.put(TFS_PASSWORD_UPDATE,
			"UPDATE TFS_PASSWORD " +
			"set PASSWORD = ?" +
			"where PASSWORD = ?");

/*		queryMap.put(TFS_GET_NOTICIAS_MOSTRADAS, "SELECT S.RESOURCE_PATH " +
				"FROM CMS_${PROJECT}_PROPERTIES P, CMS_${PROJECT}_STRUCTURE S " +
				"WHERE P.PROPERTYDEF_ID = ? " +
				"AND P.PROPERTY_MAPPING_ID = S.STRUCTURE_ID " +
				"AND P.PROPERTY_VALUE <> '" + TfsConstants.NO_MOSTRAR_VALUE+ "'");
*/
		queryMap.put(TFS_GET_NOTICIAS_MOSTRADAS, "SELECT RESOURCE_PATH, PROPERTYDEF_NAME " +
				"FROM TFS_${PROJECT}_NEWS_ZONE  " +
				"WHERE SITE = ? " +
				"AND PUBLICATION = ? " +
				"AND PAGE = ? " +
				"AND PROPERTY_VALUE = ?");
		
		queryMap.put(TFS_GET_NOTICIAS_MOSTRADAS_2, "SELECT RESOURCE_PATH, PROPERTYDEF_NAME " +
				"FROM TFS_${PROJECT}_NEWS_ZONE  " +
				"WHERE SITE = ? " +
				"AND PUBLICATION = ? " +
				"AND PAGE = ? ");
		
		queryMap.put(TFS_GET_NOTICIAS_ALTOTRANSITO, "SELECT RESOURCE_PATH, PROPERTYDEF_NAME " +
				"FROM TFS_${PROJECT}_NEWS_ZONE  " +
				"WHERE SITE = ? " +
				"AND PUBLICATION = ? " +
				"AND PAGE = ? " +
				"AND SECTION = ? " +
				"AND PROPERTY_VALUE = ?");		

		queryMap.put(TFS_GET_NOTICIAS_ALTOTRANSITO_FULLSECTION, "SELECT RESOURCE_PATH, PROPERTYDEF_NAME " +
				"FROM TFS_${PROJECT}_NEWS_ZONE  " +
				"WHERE SITE = ? " +
				"AND PUBLICATION = ? " +
				"AND PAGE = ? " +
				"AND SECTION = ? ");		

		queryMap.put(TFS_GET_NOTICIAS_ALTOTRANSITO_FULLZONE, "SELECT RESOURCE_PATH, PROPERTYDEF_NAME " +
				"FROM TFS_${PROJECT}_NEWS_ZONE  " +
				"WHERE SITE = ? " +
				"AND PUBLICATION = ? " +
				"AND PAGE = ? " +
				"AND PROPERTY_VALUE = ? ");		
		
		queryMap.put(TFS_GET_NOTICIAS_SET_ALTOTRANSITO, "SELECT RESOURCE_PATH, PROPERTYDEF_NAME " +
				"FROM TFS_${PROJECT}_NEWS_ZONE  " +
				"WHERE SITE = ? " +
				"AND PUBLICATION = ? " +
				"AND PAGE = ? " +
				"AND FIND_IN_SET(SECTION, ? ) > 0 " +
				"AND FIND_IN_SET(PROPERTY_VALUE, ?) > 0 ");		

		queryMap.put(TFS_GET_NOTICIAS_SET_ALTOTRANSITO_FULLSECTION, "SELECT RESOURCE_PATH, PROPERTYDEF_NAME " +
				"FROM TFS_${PROJECT}_NEWS_ZONE  " +
				"WHERE SITE = ? " +
				"AND PUBLICATION = ? " +
				"AND PAGE = ? " +
				"AND FIND_IN_SET(SECTION, ? ) > 0");		

		queryMap.put(TFS_GET_NOTICIAS_SET_ALTOTRANSITO_FULLZONE, "SELECT RESOURCE_PATH, PROPERTYDEF_NAME " +
				"FROM TFS_${PROJECT}_NEWS_ZONE  " +
				"WHERE SITE = ? " +
				"AND PUBLICATION = ? " +
				"AND PAGE = ? " +
				"AND FIND_IN_SET(PROPERTY_VALUE, ? ) > 0");		
		
		

	}

}
