package com.tfsla.opencmsdev.encuestas;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.opencms.file.CmsObject;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.widgets.CmsSelectWidgetOption;

import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.diario.ediciones.services.openCmsService;
import com.tfsla.exceptions.ApplicationException;
import com.tfsla.workflow.QueryBuilder;
import com.tfsla.workflow.ResultSetProcessor;

public class ModuloEncuestas implements EncuestasSQLConstants {

	public static final String ENCUESTAS_PATH = "/encuestas";
	public static final String COOKIE_NAME = "yaVoto";

	// ***************************
	// ** CREAR ENCUESTA
	// ***************************
	public static List<CmsSelectWidgetOption> getGruposParaCombo(CmsObject cms) {
		return new GetGruposParaComboProcess().execute(cms);
	}
	
	public static List<CmsSelectWidgetOption> getGrupos(CmsObject cms) {
		return new GetGruposProcess().execute(cms);
	}

	public static void insertOrUpdateEncuesta(CmsObject cms, String encuestaURL, Encuesta encuesta) {
		if (encuesta.isTransient()) {
			// por ahora solo hacemos insert de la encuesta desde el edit, y no update
			new InsertEncuestaProcess().execute(cms, encuestaURL, encuesta);
		}
	}
	
	public static List<CmsSelectWidgetOption> getCategoriesParaCombo(CmsObject cms) {
		
		return new GetCategoriesParaComboProcess().execute(cms);
	}
	

	// ***************************
	// ** VER ENCUESTA
	// ***************************
	/**
	 * @return la URL de la encuesta que esta visible (porque esta Activa o bien porque fue la m�s
	 *         recientemente cerrada)
	 */
	public static EncuestaBean getEncuestaMostrableURLs(CmsObject cms, String grupo, String orden) {
		return new GetEncuestaMostrableURLProcess().execute(cms, grupo, orden);
	}

	// ***************************
	// ** VOTACION
	// ***************************
	public static boolean yaVoto(HttpServletRequest request,CmsObject cms, Encuesta encuesta, String Username) {
		return new YaVotoProcess().execute(request,cms ,encuesta, Username);
	}
	
	public static boolean yaVoto(HttpServletRequest request,CmsObject cms, String encuestaURL, String Username) {
		return new YaVotoProcess().execute(request,cms ,encuestaURL, Username);
	}
	
	public static boolean yaVotoIP(HttpServletRequest request , CmsObject cms, Encuesta encuesta) {
		return new YaVotoIP_Process().execute(request, cms , encuesta);
	}
	
	@Deprecated
	public static boolean yaVotoIP(HttpServletRequest request , CmsObject cms, String encuestaURL) {
		return new YaVotoIP_Process().execute(request, cms , encuestaURL);
	}

	public static boolean yaVotoUsuario(HttpServletRequest request , CmsObject cms, Encuesta encuesta, String Username) {
		return new YaVotoUsuario_Process().execute(request, cms , encuesta, Username);
	}

	
	@Deprecated
	public static boolean yaVotoUsuario(HttpServletRequest request , CmsObject cms, String encuestaURL, String Username) {
		return new YaVotoUsuario_Process().execute(request, cms , encuestaURL, Username);
	}

	public static boolean votar(CmsObject cms, HttpServletRequest request ,Encuesta encuesta, String respuesta, String Username ) {
		List<String> respuestas = new ArrayList<String>();
		respuestas.add(respuesta);
		boolean registraVoto = votar(cms,request,encuesta, respuestas, Username);
		
		return registraVoto;
	}

	public static boolean votar(CmsObject cms, HttpServletRequest request, Encuesta encuesta, List<String> respuestas,String Username) {
        
		boolean validaxUsuario = false;
	    boolean registraVoto = false;
	    
		if(encuesta.isUsuariosRegistrados()){
			validaxUsuario = true;
		}
					
		if(validaxUsuario && Username==null){
			return false;
		}
		
		boolean yavoto = false;
		
		if(validaxUsuario){
		   yavoto = yaVoto(request, cms , encuesta, Username);
		}
		
		if(!validaxUsuario){
		   yavoto = yaVoto(request, cms , encuesta, null);
		}
		
		if(!yavoto){
		   new VotarProcess().execute(cms, encuesta, respuestas, Username);
		   registraVoto = true;
		}
		
		return registraVoto;
	}

	
	@Deprecated
	public static boolean votar(CmsObject cms, HttpServletRequest request ,String encuestaURL, String respuesta, String Username ) {
		boolean validaxUsuario = false;
		
		try {
			Encuesta encuesta = Encuesta.getEncuestaFromURL(cms, encuestaURL);
			
			if(encuesta.isUsuariosRegistrados()){
				validaxUsuario = true;
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(validaxUsuario && Username==null){
			return false;
		}
		
		List<String> respuestas = new ArrayList<String>();
		respuestas.add(respuesta);
		boolean registraVoto = votar(cms,request,encuestaURL, respuestas, Username);
		
		return registraVoto;
	}

	@Deprecated
	public static boolean votar(CmsObject cms, HttpServletRequest request, String encuestaURL, List<String> respuestas,String Username) {
        
		boolean validaxUsuario = false;
	    boolean registraVoto = false;
	    
		try {
			Encuesta encuesta = Encuesta.getEncuestaFromURL(cms, encuestaURL);
			
			if(encuesta.isUsuariosRegistrados()){
				validaxUsuario = true;
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(validaxUsuario && Username==null){
			return false;
		}
		
		boolean yavoto = false;
		
		if(validaxUsuario && Username !=null){
		   yavoto = yaVoto(request, cms , encuestaURL, Username);
		}
		
		if(!validaxUsuario){
		   yavoto = yaVoto(request, cms , encuestaURL, null);
		}
		
		if(!yavoto){
		   new VotarProcess().execute(cms, encuestaURL, respuestas, Username);
		   registraVoto = true;
		}
		
		return registraVoto;
	}

	// ***************************
	// ** VER RESULTADOS
	// ***************************
	public static ResultadoEncuestaBean getResultado(CmsObject cms, String encuestaURL) {
		return new GetResultadosProcess().executeOnline(cms, encuestaURL);
	}

	// ***************************
	// ** PUBLICACION
	// ***************************
	public static void publicarEncuesta(CmsJspActionElement jsp, String encuestaURL) {
		new PublicarEncuestaProcess().execute(jsp, encuestaURL);
	}

	// ***************************
	// ** ACTIVACION
	// ***************************
	public static void activarEncuesta(CmsJspActionElement jsp, String encuestaURL, String grupo) {
		new ActivarEncuestaProcess().execute(jsp, encuestaURL, grupo);
		new PublicarEncuestaProcess().execute(jsp, encuestaURL);
	}

	// ***************************
	// ** CIERRE
	// ***************************
	public static void cerrarEncuesta(CmsJspActionElement jsp, String encuestaURL) {
		PurgarVotosEncuesta(jsp.getCmsObject(), encuestaURL);
		new CerrarEncuestaProcess().execute(jsp, encuestaURL);
		new PublicarEncuestaProcess().execute(jsp, encuestaURL);
	}

	// ***************************
	// ** DESPUBLICAR
	// ***************************
	public static void despublicarEncuesta(CmsJspActionElement jsp, String encuestaURL, String siteURL) {
		CmsObject cms = jsp.getCmsObject();
		new DespublicarEncuestasProgramadasProcess().updateDateDespublicada(cms, encuestaURL);
		new DespublicarEncuestasProgramadasProcess().despublicarSiHaceFalta(cms, siteURL , encuestaURL, false);
		new PublicarEncuestaProcess().execute(jsp, encuestaURL);
	}

	
	// **********************************
	// ** Encuestas Anteriores/Cerradas
	// **********************************
	/**
	 * Obtiene las encuestas en estado cerrada. Busca en la tabla correspondiente al proyecto actual (online u
	 * offline).
	 * 
	 * @param cms
	 * @param limit
	 * @param grupo
	 * @return la lista de urls de encuestas cerradas
	 */
	public static List getEncuestasCerradas(CmsObject cms, String limit, String grupo) {
		return new GetEncuestasCerrradasProcess().execute(cms, limit, grupo);
	}
	
	public static List getEncuestasActivas(CmsObject cms, String limit, String grupo) {
		return new GetEncuestasActivasProcess().execute(cms, limit, grupo);
	}

	// *********************************
	// ** Eventos Programados
	// *********************************
	public static List<String> despublicarEncuestasProgramadas(String siteURL, CmsObject cms, boolean fixContent, int batchSize, int sleepTime) {
		return new DespublicarEncuestasProgramadasProcess().execute(siteURL, cms, fixContent, batchSize, sleepTime );
	}
	
	public static List<String> despublicarEncuestasProgramadas(String siteURL, CmsObject cms) {
		return new DespublicarEncuestasProgramadasProcess().execute(siteURL, cms, false,0,0);
	}

	public static List<String> cerrarEncuestasProgramadas(String siteURL, CmsObject cms) {
		return new CerrarEncuestasProgramadasProcess().execute(siteURL, cms, false,0,0);
	}
	
	public static List<String> cerrarEncuestasProgramadas(String siteURL, CmsObject cms, boolean fixContent, int batchSize, int sleepTime) {
		return new CerrarEncuestasProgramadasProcess().execute(siteURL, cms, fixContent, batchSize, sleepTime);
	}
	
	public static void PurgarVotosEncuestasCerradas(CmsObject cms){
		
		String purgaVotosCerradasQuery = "delete "+ TABLA_ENCUESTA_VOTOS +" from "+TABLA_ENCUESTA_VOTOS+","+TFS_ENCUESTA_ONLINE+" where "+TABLA_ENCUESTA_VOTOS+"."+ID_ENCUESTA+" = "+TFS_ENCUESTA_ONLINE+"."+ID_ENCUESTA+" AND "+TFS_ENCUESTA_ONLINE+"."+ESTADO_PUBLICACION+" = '" + Encuesta.CERRADA+ "'";
		new QueryBuilder<String>(cms).setSQLQuery(purgaVotosCerradasQuery).execute();
			
		String purgaVotosDespublicadasQuery = "delete "+ TABLA_ENCUESTA_VOTOS +" from "+TABLA_ENCUESTA_VOTOS+","+TFS_ENCUESTA_ONLINE+" where "+TABLA_ENCUESTA_VOTOS+"."+ID_ENCUESTA+" = "+TFS_ENCUESTA_ONLINE+"."+ID_ENCUESTA+" AND "+TFS_ENCUESTA_ONLINE+"."+ESTADO_PUBLICACION+" = '" + Encuesta.DESPUBLICADA+ "'";
		new QueryBuilder<String>(cms).setSQLQuery(purgaVotosDespublicadasQuery).execute();
		
	}
	
	public static void PurgarVotosEncuesta(CmsObject cms, String encuestaURL){
		
		String purgaVotosCerradasQuery = "delete "+ TABLA_ENCUESTA_VOTOS +" from "+TABLA_ENCUESTA_VOTOS+","+TFS_ENCUESTA_ONLINE+" where "+TABLA_ENCUESTA_VOTOS+"."+ID_ENCUESTA+" = "+TFS_ENCUESTA_ONLINE+"."+ID_ENCUESTA+" AND " +TFS_ENCUESTA_ONLINE+"."+URL_ENCUESTA + " = '" + encuestaURL+"'";
		new QueryBuilder<String>(cms).setSQLQuery(purgaVotosCerradasQuery).execute();
	}
	
	/**
	 * Obtiene las encuestas en estado cerrada.
	 * 
	 * @param cms
	 * @param tableName la tabla de encuestas donde buscar (online u offline)
	 * @param limit puede ser null
	 * @param grupo puede ser null
	 */
	public static List getEncuestasCerradas(CmsObject cms, String tableName, String limit, String grupo) {
		return new GetEncuestasCerrradasProcess().execute(cms, tableName, limit, grupo);
	}
	
	public static List getEncuestas(CmsObject cms, String grupo, String state, String limit, String order){
		return new GetEncuestasProcess().execute(cms, grupo, state, limit, order);
	}

	public static List getEncuestas(CmsObject cms, String sitio, String publicacion, String grupo, String state, String limit, String order){
		return new GetEncuestasProcess().execute(cms, sitio, publicacion, grupo, state, limit, order);
	}
	// ***************************
	// ** Helpers
	// ***************************
	/**
	 * @return el id de la encuesta en el offline
	 */
	public static int getEncuestaID(CmsObject cms, String encuestaURL) {
		return getEncuestaIDFromTable(cms, encuestaURL, TFS_ENCUESTA);
	}

	/**
	 * @param cms
	 * @param encuestaURL
	 * @return el id de la encuesta en la tabla del online
	 */
	public static int getEncuestaIDFromOnline(CmsObject cms, String encuestaURL) {
		return getEncuestaIDFromTable(cms, encuestaURL, TFS_ENCUESTA_ONLINE);
	}

	private static int getEncuestaIDFromTable(CmsObject cms, String encuestaURL, String encuestaTableName) {
		
		
		String getEncuestaSQL = "SELECT " + ID_ENCUESTA + " FROM " + encuestaTableName + " WHERE "
				+ URL_ENCUESTA + " = '" + encuestaURL + "' AND " + SITIO  + " = '" + openCmsService.getCurrentSite(cms) + "'";

		Integer id_encuesta = new QueryBuilder<Integer>(cms).setSQLQuery(getEncuestaSQL).execute(
				new ResultSetProcessor<Integer>() {

					private Integer encuestaId;

					public void processTuple(ResultSet rs) {
						try {
							this.encuestaId = new Integer(rs.getInt(ID_ENCUESTA));
						}
						catch (SQLException e) {
							throw new ApplicationException("No se pudo leer la columna " + ID_ENCUESTA, e);
						}
					}

					public Integer getResult() {
						return this.encuestaId;
					}
				});

		return id_encuesta == null ? -1 : id_encuesta;
	}
	
	public static String getEncuestaPublicacion(CmsObject cms, String encuestaURL) {
		
		String getEncuestaSQL = "SELECT " + PUBLICACION + " FROM TFS_ENCUESTA WHERE "
				+ URL_ENCUESTA + " = '" + encuestaURL + "'";

		String publicacion = new QueryBuilder<String>(cms).setSQLQuery(getEncuestaSQL).execute(
				new ResultSetProcessor<String>() {

					private String publicacion;

					public void processTuple(ResultSet rs) {
						try {
							if(rs.getString(PUBLICACION)!=null)
							    this.publicacion = new String(rs.getString(PUBLICACION));
							else
								this.publicacion = null;
						}
						catch (SQLException e) {
							throw new ApplicationException("No se pudo leer la columna " + PUBLICACION, e);
						}
					}

					public String getResult() {
						return this.publicacion;
					}
				});

		return publicacion == null ? "1" : publicacion;
	}
	

	/**
	 * Dado un nombre de archivo retorna el nombre calificado con la carpeta de encuestas. Puse este metodo
	 * para centralizar eso y si se quiere cambiar tocar en un solo lugar.
	 */
	@Deprecated
	public static String getCompletePath(String encuestaFileURL) {
		return ENCUESTAS_PATH + "/" + encuestaFileURL;
	}

	public static String getEncuestaPath(CmsObject cms, int publicacion) {
		String encuestasPath = "";
		
		TipoEdicionService tService = new TipoEdicionService();
		TipoEdicion tEdicion = tService.obtenerTipoEdicion(publicacion);
		encuestasPath = tEdicion.getBaseURL().replace("contenidos/", "");
		encuestasPath = cms.getRequestContext().removeSiteRoot(encuestasPath);
		encuestasPath = encuestasPath.substring(0, encuestasPath.length()-1);
					
		encuestasPath = encuestasPath + ModuloEncuestas.ENCUESTAS_PATH;
		
		return encuestasPath;
	}
	
	// ***************************
	// ** Depuracion de la base
	// ***************************
	/**
	 * Toda encuesta debe estar tanto creada en la carpeta de encuestas en el cms, como ingresada en las
	 * tablas del modulo de encuestas.
	 * 
	 * Dado que el openCMS no es transaccional, este metodo sirve para corregir las inconsistencias que
	 * pudieran darse al producirse un error en el openCMS.
	 */
	public static void sincronizarBDyContenidos(CmsObject cms) {
		new SincronizarBDProcess().execute(cms);
	}

	public static void getEncuestaIDAndPublicationFromOnline(CmsObject cms, Encuesta encuesta) {
			
			
			String getEncuestaSQL = "SELECT " + ID_ENCUESTA + ", " + PUBLICACION + " FROM " + TFS_ENCUESTA_ONLINE + " WHERE "
					+ URL_ENCUESTA + " = '" + encuesta.getEncuestaURL() + "' AND " + SITIO  + " = '" + openCmsService.getCurrentSite(cms) + "'";

			Encuesta encuestaDummy = new QueryBuilder<Encuesta>(cms).setSQLQuery(getEncuestaSQL).execute(
					new ResultSetProcessor<Encuesta>() {

						private Encuesta encuesta = new Encuesta();

						public void processTuple(ResultSet rs) {
							try {
								this.encuesta.setIDEncuesta(rs.getInt(ID_ENCUESTA));
								this.encuesta.setPublicacion(rs.getString(PUBLICACION));
							}
							catch (SQLException e) {
								throw new ApplicationException("No se pudo leer la columna " + ID_ENCUESTA, e);
							}
						}

						public Encuesta getResult() {
							return this.encuesta;
						}
					});

			encuesta.setPublicacion(encuestaDummy.getPublicacion());
			encuesta.setIDEncuesta(encuestaDummy.getIdEncuesta());
	}
}