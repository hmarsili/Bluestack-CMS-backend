package com.tfsla.opencmsdev.module;

import java.util.Map;

import com.tfsla.collections.CollectionFactory;
import com.tfsla.opencmsdev.module.pages.PageConfiguration;
import com.tfsla.opencmsdev.module.pages.Project;

public abstract class TfsConstants {
	/** resource type id de noticia*/
	public static final Integer NOTICIA_TYPEID = 50;

    /** resource type id de noticia de la edici�n impresa **/
    public static final Integer NOTICIA_TYPEID_EDIMP = 1101;

    //Por lo menos que queden como constantes
    public static final Integer ONLINE_PROJECT_ID = 1;
    public static final Integer EDICIONIMPRESA_PROJECT_ID = 2;


	/** prefijo con el que empiezan las properties para la home*/
	public static final String HOME_TARGET  = "home";
	/** prefijo con el que empiezan las properties para la seccion*/
	public static final String SECTION_TARGET  = "section";

	public static final String SECTION_EDIMP_TARGET  = "sectionedimp";
	public static final String HOME_EDIMP_TARGET  = "homeedimp";

	public static final String ACTIVE_PROPERTY  = "active";
	public static final String ZONE_PROPERTY  = "zone";
	public static final String PRIORITY_PROPERTY  = "priority";
	public static final String TITULO_PROPERTY = "title";
	public static final String SECTION_PROPERTY = "seccion";
    public static final String STATE_PROPERTY = "state";

	/** en el opencms, es el valor que se pone para verdadero, en los combos binarios */
	public static final String TRUE_STRING_VALUE = "si";
	/** en el opencms, es el valor que se pone para falso, en los combos binarios */
	public static final String FALSE_STRING_VALUE = "no";
	/** es el valor que toma la property de zona cuando no se tiene que mostrar en ninguna zona */
	public static final String NO_MOSTRAR_VALUE = "no_mostrar";

	public static final String ORDER_SECCION = "seccion";
	public static final String ORDER_ESTADO = "estado";
	public static final String ORDER_ZONA = "zona";
	public static final String ORDER_TITULO = "titulo";
    public static final String ORDER_DEFAULT = "predeterminado";

	public static final Integer TITULO_UBICATION_KEY = new Integer(0);
	public static final Integer KEYWORD_UBICATION_KEY= new Integer(1);

	public static final String KEYWORD_PROPERTY = "Keywords";

	public static final String TITULO_UBICATION = "titulo";
	public static final String KEYWORD_UBICATION = "keyword";
	public static final String ANY_UBICATION= "indistinto";

    public static final String HOME_FILENAME  = "index.html";

	/** @deprecated*/
    public static final String ACTION_RECUPERAR_NOTA = "RecuperarNota";
    /** @deprecated*/
    public static final String ACTION_RECHAZAR_NOTA = "RechazarNota";
    /** @deprecated*/
    public static final String ACTION_OBSERVAR_NOTA = "ObservarNota";

	public static final String ACTION_DESPUBLICAR_NOTA = "DespublicarNota";
	public static final String ACTION_PUBLICAR_NOTA = "PublicarNota";
	public static final String ACTION_ENVIAR_A_PARRILLA = "EnviarAParrilla";
	public static final String ACTION_ENVIAR_A_EDICION = "EnviarAEdicion";
	public static final String ACTION_CREAR_NOTA = "CrearNota";
	public static final String ACTION_PROGRAMAR = "Programar";
	public static final String ACTION_ENVIAR_A_REDACCION = "EnviarARedaccion";

	public static final String GROUP_REDACTOR = "Redactores";
	public static final String GROUP_EDITOR = "Editores";
	public static final String PROPERTY_LAST_USER = "lastUser";
	public static final String PROPERTY_LAST_ROLE = "lastRole";
	public static final String PROPERTY_ACTION_TIME = "actionTime";
	public static final String ACTIVE_ONLINE_PROPERTY = "active.online";

	public static final String ULTIMA_MODIFICACION_PROPERTY = "ultimaModificacion";

	//Pongo numeros altos para evitar pisar
	public static final int EVENT_PLANILLA_BEGIN = 1000;
	public static final int EVENT_PLANILLA_END = 1001;
	public static final int EVENT_PLANILLA_EDIMP_BEGIN = 1002;
	public static final int EVENT_PLANILLA_EDIMP_END = 1003;


    public static Map<String, String> seccionesPageMap = CollectionFactory.createMap();
    public static Map<String, String> seccionesNameMap = CollectionFactory.createMap();

    /**
     * @deprecated use PageConfiguration.getSectionDescription instead.
     */
    public static String getDescripcionSeccion(String seccion) {
        Project project = PageConfiguration.getInstance().getProjectById(1);
        return PageConfiguration.getInstance().getSectionDescription(project, seccion);
    }

    /**
     * @deprecated use PageConfiguration.getSectionPageName instead.
     */
    public static String getNombrePaginaSeccion(String seccion) {
        Project project = PageConfiguration.getInstance().getProjectById(1);
        return PageConfiguration.getInstance().getSectionPageName(project, seccion);
    }

}

