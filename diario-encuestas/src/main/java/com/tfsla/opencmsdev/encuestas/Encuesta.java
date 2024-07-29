package com.tfsla.opencmsdev.encuestas;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;

import com.tfsla.opencmsdev.encuestas.util.XMLContentUtils;
import com.tfsla.utils.DateUtils;

public class Encuesta {

	public static final int TYPE_ID = 553;

	public static final String INACTIVA = "Inactiva";
	public static final String ACTIVA = "Activa";
	public static final String CERRADA = "Cerrada";
	public static final String DESPUBLICADA = "Despublicada";

	public static final int MIN_RESPUESTAS = 2;

	/** recordar que este valor tambien esta en el Encuesta.xsd :( **/
	public static final int MAX_RESPUESTAS = 20;
	
	public static final int MIN_CATEGORIAS = 2;
	public static final int MAX_CATEGORIAS = 20;
	
	public static final String ORDEN_CARGA = "carga";
	public static final String ORDEN_RANKING = "ranking";
	public static final String ORDEN_ALFABETO = "alfabetico";

	// *******************************
	// ** Optimizacion de performance
	// *******************************
	private static final String PROPERTY_SEPARATOR = "-";

	public static final String ESTADO_GRUPO_PROPERTY = "estadoYGrupo";

	// *******************************
	// ** Campos que carga el usuario
	// *******************************
	private String pregunta;
	private String grupo;
	private String fechaCierre;
	private String fechaDespublicacion;
	private String imagenAMostrar;
	private String textoAclaratorio;
	private boolean respuestaExcluyente = true;
	private boolean usuariosRegistrados = false;
	private String[][] respuestas = null;
	private List<String> categorias = new ArrayList<String>();
	private String tags;
	private String publicacion;
	private String ordenOpciones;
	private boolean usarCaptcha = false;
	private String estilo = "";
	

	// ***************************
	// ** Campos que maneja el sistema
	// ***************************
	private String estado;
	private String fechaCreacion;
	private String usuarioPublicador;
	private String fechaPublicacion;
	private String encuestaURL;
	private boolean isValidating = true;

	/** campo usado para validar que una vez construida no se pueda variar la cantidad de respuestas * */
	private int cantRespuestasOriginal;

	// ***************************
	// ** Campos para persistencia
	// ***************************
	private int idEncuesta;

	// ***************************
	// ** Constructores
	// ***************************
	public Encuesta() {
		this.estado = INACTIVA;
		this.fechaCreacion = DateUtils.today();
	}

	public Encuesta(CmsXmlContent content, CmsObject cms, String encuestaURL, boolean online) {
		try {
			this.isValidating = false;
			// deshabilito las validaciones para los setters ya que lo est�
			// construyendo el sistema y
			// no se puede asegurar el orden en el que se llama a los setters ni
			// nada.
			this.encuestaURL = encuestaURL;
			XMLContentUtils.setPropertyFromXML(this, "pregunta", content, cms);
			XMLContentUtils.setPropertyFromXML(this, "textoAclaratorio", content, cms);			
			XMLContentUtils.setPropertyFromXML(this, "grupo", content, cms);
			XMLContentUtils.setDatePropertyFromXML(this, "fechaCierre", content, cms);
			XMLContentUtils.setDatePropertyFromXML(this, "fechaDespublicacion", content, cms);
			XMLContentUtils.setPropertyFromXML(this, "imagenAMostrar", content, cms);
			XMLContentUtils.setBooleanPropertyFromXML(this, "respuestaExcluyente", content, cms);
			XMLContentUtils.setBooleanPropertyFromXML(this, "usuariosRegistrados", content, cms);
			XMLContentUtils.setPropertyFromXML(this, "ordenOpciones", content, cms,ORDEN_CARGA);
			
			//this.cantRespuestasOriginal = this.filterEmptyRespuestas(
			//		XMLContentUtils.setListFromXML(this, "respuesta", "respuestas", content, cms)).size();
			
			this.cantRespuestasOriginal = setRespuestasFromXML(content, cms).length;
			
			XMLContentUtils.setPropertyFromXML(this, "estado", content, cms);
			XMLContentUtils.setDatePropertyFromXML(this, "fechaCreacion", content, cms);
			XMLContentUtils.setPropertyFromXML(this, "usuarioPublicador", content, cms);
			XMLContentUtils.setDatePropertyFromXML(this, "fechaPublicacion", content, cms);
			XMLContentUtils.setPropertyFromXML(this, "tags", content, cms);
			XMLContentUtils.setListFromXML(this,"categorias", "categorias", content, cms);
			XMLContentUtils.setPropertyFromXML(this, "estilo", content, cms);
			
			XMLContentUtils.setBooleanPropertyFromXML(this, "usarCaptcha", content, cms,false);

			if (!online) {
				setIdEncuesta(cms,encuestaURL);
				setPublicacionEncuesta(cms,encuestaURL);
			}
			else {
				setIdAndPublicationEncuesta(this, cms);
			} 
		}
		catch (Exception e) {
			throw new RuntimeException("No se pudo construir una Encuesta a partir del contenido xml", e);
		}
		finally {
			this.isValidating = true;
		}
	}
	
	@Deprecated
	public Encuesta(CmsXmlContent content, CmsObject cms, String encuestaURL) {
		try {
			this.isValidating = false;
			// deshabilito las validaciones para los setters ya que lo est�
			// construyendo el sistema y
			// no se puede asegurar el orden en el que se llama a los setters ni
			// nada.
			this.encuestaURL = encuestaURL;
			XMLContentUtils.setPropertyFromXML(this, "pregunta", content, cms);
			XMLContentUtils.setPropertyFromXML(this, "textoAclaratorio", content, cms);			
			XMLContentUtils.setPropertyFromXML(this, "grupo", content, cms);
			XMLContentUtils.setDatePropertyFromXML(this, "fechaCierre", content, cms);
			XMLContentUtils.setDatePropertyFromXML(this, "fechaDespublicacion", content, cms);
			XMLContentUtils.setPropertyFromXML(this, "imagenAMostrar", content, cms);
			XMLContentUtils.setBooleanPropertyFromXML(this, "respuestaExcluyente", content, cms);
			XMLContentUtils.setBooleanPropertyFromXML(this, "usuariosRegistrados", content, cms);
			XMLContentUtils.setPropertyFromXML(this, "ordenOpciones", content, cms,ORDEN_CARGA);
			
			//this.cantRespuestasOriginal = this.filterEmptyRespuestas(
			//		XMLContentUtils.setListFromXML(this, "respuesta", "respuestas", content, cms)).size();
			
			this.cantRespuestasOriginal = setRespuestasFromXML(content, cms).length;
			
			XMLContentUtils.setPropertyFromXML(this, "estado", content, cms);
			XMLContentUtils.setDatePropertyFromXML(this, "fechaCreacion", content, cms);
			XMLContentUtils.setPropertyFromXML(this, "usuarioPublicador", content, cms);
			XMLContentUtils.setDatePropertyFromXML(this, "fechaPublicacion", content, cms);
			XMLContentUtils.setPropertyFromXML(this, "tags", content, cms);
			XMLContentUtils.setListFromXML(this,"categorias", "categorias", content, cms);
			XMLContentUtils.setPropertyFromXML(this, "estilo", content, cms);
			
			XMLContentUtils.setBooleanPropertyFromXML(this, "usarCaptcha", content, cms,false);
			
			setIdEncuesta(cms,encuestaURL);
			setPublicacionEncuesta(cms,encuestaURL);
		}
		catch (Exception e) {
			throw new RuntimeException("No se pudo construir una Encuesta a partir del contenido xml", e);
		}
		finally {
			this.isValidating = true;
		}
	}
	
	public String[][] setRespuestasFromXML(CmsXmlContent content, CmsObject cms)
            throws IllegalAccessException, InvocationTargetException {
		
		Map<Integer, String> options = new HashMap<Integer, String>();
		Map<Integer, String>  images = new HashMap<Integer, String>();
		Map<Integer, String> descriptions = new HashMap<Integer, String>();
		
		int nro = 1;
		String xmlName ="respuesta[" + nro + "]";
		I_CmsXmlContentValue value = content.getValue(xmlName, CmsLocaleManager.getDefaultLocale());
		
		while (value!=null)
		{
			I_CmsXmlContentValue  texto = (I_CmsXmlContentValue) content.getValue("respuesta["+nro+"]/texto", CmsLocaleManager.getDefaultLocale());
	    	I_CmsXmlContentValue imagen = (I_CmsXmlContentValue) content.getValue("respuesta["+nro+"]/imagen", CmsLocaleManager.getDefaultLocale());
	    	I_CmsXmlContentValue descripcion = (I_CmsXmlContentValue) content.getValue("respuesta["+nro+"]/descripcion", CmsLocaleManager.getDefaultLocale());
	    	
	    	options.put(nro, texto.getStringValue(cms));
	    	if (descripcion!=null && descripcion.getStringValue(cms)!=null)
	    		descriptions.put(nro, descripcion.getStringValue(cms));
	    	else
	    		descriptions.put(nro, "");
	    	
	    	if(imagen!=null)
	    	   images.put(nro,imagen.getStringValue(cms));
	    	else
	    	   images.put(nro,"");
	    	
			nro++;
			xmlName ="respuesta[" + nro + "]";
			value = content.getValue(xmlName, Locale.ENGLISH);

		}
		
    	int count = options.size();
    	
    	String[][] values = new String[count][3];
    	int ind = -1;
    	
    	for(int i=1;i<=count;i++)
    	{
    		if(!options.get(i).trim().equals("") || !images.get(i).trim().equals("") ){
    			ind++;
    			values[ind][0] = options.get(i);
    			values[ind][1] = images.get(i);
    			values[ind][2] = descriptions.get(i);
    		}
    	}
    	
    	this.respuestas = values;
    
        return values;
    }

	// *****************************************
	// ** validacion de cantidad de respuestas
	// *****************************************
	public int getCantRespuestasOriginal() {
		return this.cantRespuestasOriginal;
	}

	// ***************************
	// ** Optimizacion de performance
	// ***************************
	/**
	 * este campo guarda un string con el estado y el grupo. Es para poder buscar la encuesta activa haciendo
	 * cms.readResourcesWithProperty en lugar de pegarle a la base.
	 */
	public String getEstadoYGrupo() {
		return getEstadoYGrupoIdentifier(this.estado, this.grupo);
	}

	public static String getEstadoYGrupoIdentifier(String estado, String grupo) {
		return estado + PROPERTY_SEPARATOR + grupo;
	}

	public void setEstadoYGrupo() {
		// por compatiblidad
	}

	// ***************************
	// ** Persistence methods
	// ***************************
	public void setIDEncuesta(int int1) {
		this.idEncuesta = int1;
	}

	public void setIdAndPublicationEncuesta(Encuesta encuesta, CmsObject cms) {
		ModuloEncuestas.getEncuestaIDAndPublicationFromOnline(cms, encuesta);
	}
	
    public void setIdEncuesta(CmsObject cms, String encuestaURL){
		
		boolean isOnlineProject = cms.getRequestContext().currentProject().isOnlineProject();
		
		if(isOnlineProject){
		   this.idEncuesta = ModuloEncuestas.getEncuestaIDFromOnline(cms, encuestaURL);
		}else{
		   this.idEncuesta = ModuloEncuestas.getEncuestaID(cms, encuestaURL);
		}
	}
    
    public void setPublicacionEncuesta(CmsObject cms, String encuestaURL){
    	this.publicacion = ModuloEncuestas.getEncuestaPublicacion(cms, encuestaURL);
    }
	
	public int getIdEncuesta() {
		return this.idEncuesta;
	}

	public void setEncuestaURL(String encuestaURL) {
		this.encuestaURL = encuestaURL;
	}

	public void setValidating(boolean isValidating) {
		this.isValidating = isValidating;
	}

	public static Encuesta getEncuestaFromURL(CmsObject cms, String encuestaURL) throws Exception {
		CmsResource resource = cms.readResource(encuestaURL);
		CmsFile file = cms.readFile(resource);

		CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, file);

		Encuesta encuesta = new Encuesta(content, cms, encuestaURL);
		return encuesta;
	}
	
	public static Encuesta getEncuestaFromURL(CmsObject cms, String encuestaURL, Boolean online) throws Exception {
		CmsResource resource = cms.readResource(encuestaURL);
		CmsFile file = cms.readFile(resource);

		CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, file);

		Encuesta encuesta = new Encuesta(content, cms, encuestaURL,online);
		return encuesta;
	}

	// ***************************
	// ** Public accessors
	// ***************************
	public boolean isTransient() {
		return this.encuestaURL == null;
	}

	public String getFechaCierre() {
		return this.fechaCierre;
	}

	public void setFechaCierre(String fechaCierre) {
		// TODO ver de validar genericamente las fechas
		if (this.isValidating) {
			if (!DateUtils.isEmptyOrNullOrZero(fechaCierre)) {
				if (DateUtils.esAntesDeHoy(fechaCierre)) {
					throw new CmsIllegalArgumentException(Messages.get().container(
							"FECHA_CIERRE_ANTERIOR_ACTUAL"));
				}

				if (!DateUtils.isEmptyOrNullOrZero(this.fechaDespublicacion)) {
					if (DateUtils.esAnterior(this.fechaDespublicacion, fechaCierre)) {
						throw new CmsIllegalArgumentException(Messages.get().container(
								"FECHA_DESPUBLICACION_ANTERIOR_CIERRE"));
					}
				}
			}
		}
		this.fechaCierre = fechaCierre;
	}

	public boolean isUsarCaptcha() {
		return usarCaptcha;
	}

	public void setUsarCaptcha(boolean usarCaptcha) {
		this.usarCaptcha = usarCaptcha;
	}

	public String getFechaDespublicacion() {
		return this.fechaDespublicacion;
	}

	public void setFechaDespublicacion(String fechaDespublicacion) {
		if (this.isValidating) {
			if (!DateUtils.isEmptyOrNullOrZero(fechaDespublicacion)) {
				if (DateUtils.esAntesDeHoy(fechaDespublicacion)) {
					throw new CmsIllegalArgumentException(Messages.get().container(
							"FECHA_DESPUBLICACION_ANTERIOR_ACTUAL"));
				}

				if (!DateUtils.isEmptyOrNullOrZero(this.fechaCierre)) {
					if (DateUtils.esAnterior(fechaDespublicacion, this.fechaCierre)) {
						throw new CmsIllegalArgumentException(Messages.get().container(
								"FECHA_DESPUBLICACION_ANTERIOR_CIERRE"));
					}
				}
			}
		}
		this.fechaDespublicacion = fechaDespublicacion;
	}

	public String getImagenAMostrar() {
		return this.imagenAMostrar;
	}

	public void setImagenAMostrar(String imagenAMostrar) {
		this.imagenAMostrar = imagenAMostrar;
	}
	
	public String getTags() {
		return this.tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}
	
	public List<String>  getcategorias() {
		return this.categorias;
	}

	public void setcategorias(List categorias) {
		this.categorias = this.validateCategoria(categorias);
	}

	private List<String> validateCategoria(List categorias) {
		List<String> categoriasToSet = this.filterEmptyCategoria(categorias);
		
		return categoriasToSet;
	}

	private List<String> filterEmptyCategoria(List categorias) {
		List<String> filteredCategorias = new ArrayList<String>();
		for (Iterator it = categorias.iterator(); it.hasNext();) {
			String categoria = (String) it.next();
			if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(categoria)) {
				filteredCategorias.add(categoria);
			}
		}

		return filteredCategorias;
	}

	public String getPregunta() {
		return this.pregunta;
	}

	public void setPregunta(String pregunta) {
		if (CmsStringUtil.isEmptyOrWhitespaceOnly(pregunta)) {
			throw new CmsIllegalArgumentException(Messages.get()
					.container(Messages.ERR_PREGUNTA_VALIDATION_0));
		}

		this.pregunta = pregunta;
	}
	
	
	public String getEstilo() {
		return (this.estilo!=null ? this.estilo : "");
	}

	public void setEstilo(String estilo) {
		this.estilo = estilo;
	}

	public boolean isRespuestaExcluyente() {
		return this.respuestaExcluyente;
	}

	public void setRespuestaExcluyente(boolean respuestaExcluyente) {
		this.respuestaExcluyente = respuestaExcluyente;
	}
	
	public boolean isUsuariosRegistrados() {
		return this.usuariosRegistrados;
	}

	public void setUsuariosRegistrados(boolean VotanusuariosRegistrados) {
		this.usuariosRegistrados = VotanusuariosRegistrados;
	}

	public String[][] getRespuestas() {
		return this.respuestas;
	}

	public void setRespuestas(String[][] respuestas) {
		this.respuestas = this.validateRespuestas(respuestas);
	}

	public String getOrdenOpciones() {
		return ordenOpciones;
	}

	public void setOrdenOpciones(String ordenOpciones) {
		this.ordenOpciones = ordenOpciones;
	}
	
	//private List<String> validateRespuestas(List respuestas) {
	//	List<String> respuestasToSet = this.filterEmptyRespuestas(respuestas);

	//	if (respuestasToSet.size() < MIN_RESPUESTAS || respuestasToSet.size() > MAX_RESPUESTAS) {
	//		throw new CmsIllegalArgumentException(Messages.get().container(
	//				Messages.ERR_RESPUESTAS_VALIDATION_0,
	//				new Object[] { new Integer(MIN_RESPUESTAS), new Integer(MAX_RESPUESTAS) }));
	//	}

	//	return respuestasToSet;
	//}
	
	private String[][] validateRespuestas(String[][] respuestas) {
		
		int cantRespuestas = this.respuestas.length;

		if (cantRespuestas < MIN_RESPUESTAS || cantRespuestas > MAX_RESPUESTAS) {
			throw new CmsIllegalArgumentException(Messages.get().container(
					Messages.ERR_RESPUESTAS_VALIDATION_0,
					new Object[] { new Integer(MIN_RESPUESTAS), new Integer(MAX_RESPUESTAS) }));
		}

		return respuestas;
	}

	private List<String> filterEmptyRespuestas(List respuestas) {
		List<String> filteredRespuestas = new ArrayList<String>();
		for (Iterator it = respuestas.iterator(); it.hasNext();) {
			String respuesta = (String) it.next();
			if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(respuesta)) {
				filteredRespuestas.add(respuesta);
			}
		}

		return filteredRespuestas;
	}

	public String getTextoAclaratorio() {
		return this.textoAclaratorio;
	}

	public void setTextoAclaratorio(String textoAclaratorio) {
		this.textoAclaratorio = textoAclaratorio;
	}

	//public void addRespuesta(String string) {
	//	this.respuestas.add(string);
	//}

	public void setEstado(String string) {
		this.estado = string;
	}

	public void setFechaPublicacion(String date) {
		this.fechaPublicacion = date;
	}

	public String getFechaPublicacion() {
		return this.fechaPublicacion;
	}

	public String getEstado() {
		return this.estado;
	}

	public String toString() {
		return "Pregunta [" + this.pregunta + "]";
	}

	public String getFechaCreacion() {
		return this.fechaCreacion;
	}

	public void setFechaCreacion(String fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}

	public String getEncuestaURL() {
		return this.encuestaURL;
	}

	public String getGrupo() {
		return this.grupo;
	}

	public void setGrupo(String grupo) {
		if (CmsStringUtil.isEmptyOrWhitespaceOnly(grupo)) {
			throw new CmsIllegalArgumentException(Messages.get().container(Messages.ERR_GRUPO_VALIDATION_0));
		}

		this.grupo = grupo;
	}

	public String getUsuarioPublicador() {
		return this.usuarioPublicador;
	}

	public void setUsuarioPublicador(String usuarioPublicador) {
		this.usuarioPublicador = usuarioPublicador;
	}

	public void publicar(CmsJspActionElement jsp) {
		this.setFechaPublicacion(DateUtils.today());
		this.setUsuarioPublicador(jsp.getRequestContext().currentUser().getName());
	}

	public void activar() {
		this.setEstado(Encuesta.ACTIVA);
	}

	public void cerrar() {
		boolean wasValidating = this.isValidating;
		this.isValidating = false;

		this.setEstado(Encuesta.CERRADA);
		this.setFechaCierre(DateUtils.today());

		this.isValidating = wasValidating;
	}

	public boolean isNoPublicada() {
		return this.estado.equals(INACTIVA);
	}

	public boolean isActiva() {
		return this.estado.equals(ACTIVA);
	}

	public boolean isCerrada() {
		return this.estado.equals(CERRADA);
	}

	public String getPublicacion() {
		return publicacion;
	}

	public void setPublicacion(String publicacion) {
		this.publicacion = publicacion;
	}
	
	

}