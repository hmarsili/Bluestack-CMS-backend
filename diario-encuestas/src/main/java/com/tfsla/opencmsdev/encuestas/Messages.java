package com.tfsla.opencmsdev.encuestas;

import org.opencms.i18n.A_CmsMessageBundle;
import org.opencms.i18n.I_CmsMessageBundle;

public class Messages extends A_CmsMessageBundle {

    private static final String BUNDLE_NAME = "com.tfsla.opencmsdev.encuestas.messages";

    private static final I_CmsMessageBundle INSTANCE = new Messages();

    public static final String GUI_ENCUESTAS_LIST_NAME_0 = "GUI_ENCUESTAS_LIST_NAME_0";

    public static final String ENCUESTAS_LIST_PREGUNTA_COLUMN = "ENCUESTAS_LIST_PREGUNTA_COLUMN";

    public static final String ENCUESTAS_LIST_ESTADO_COLUMN = "ENCUESTAS_LIST_ESTADO_COLUMN";

    public static final String ENCUESTAS_LIST_GRUPO_COLUMN = "ENCUESTAS_LIST_GRUPO_COLUMN";

    public static final String ENCUESTAS_LIST_FECHA_PUBLICACION_COLUMN = "ENCUESTAS_LIST_FECHA_PUBLICACION_COLUMN";

    public static final String ENCUESTAS_LIST_USUARIO_PUBLICADOR_COLUMN = "ENCUESTAS_LIST_USUARIO_PUBLICADOR_COLUMN";

    public static final String ENCUESTAS_LIST_FECHA_CREACION_COLUMN = "ENCUESTAS_LIST_FECHA_CREACION_COLUMN";

    public static final String ERR_PREGUNTA_VALIDATION_0 = "ERR_PREGUNTA_VALIDATION_0";

    public static final String ERR_RESPUESTAS_VALIDATION_0 = "ERR_RESPUESTAS_VALIDATION_0";

    public static final String GUI_ENCUESTAS_LIST_ACTION_MODIFICAR_NAME_0 = "GUI_ENCUESTAS_LIST_ACTION_MODIFICAR_NAME_0";

    public static final String GUI_ENCUESTAS_LIST_ACTION_MODIFICAR_HELP_0 = "GUI_ENCUESTAS_LIST_ACTION_MODIFICAR_HELP_0";

    public static final String GUI_ENCUESTAS_LIST_MODIFICAR_COLUMN_0 = "GUI_ENCUESTAS_LIST_MODIFICAR_COLUMN_0";

    public static final String ERR_GRUPO_VALIDATION_0 = "ERR_GRUPO_VALIDATION_0";

    public static final String GUI_ENCUESTAS_LIST_PUBLICAR_COLUMN_0 = "GUI_ENCUESTAS_LIST_PUBLICAR_COLUMN_0";

    public static final String GUI_ENCUESTAS_LIST_ACTION_PUBLICAR_NAME_0 = "GUI_ENCUESTAS_LIST_ACTION_PUBLICAR_NAME_0";

    public static final String GUI_ENCUESTAS_LIST_ACTION_PUBLICAR_HELP_0 = "GUI_ENCUESTAS_LIST_ACTION_PUBLICAR_HELP_0";

    public static final String GUI_ENCUESTAS_LIST_ACTION_PUBLICAR_CONF_0 = "GUI_ENCUESTAS_LIST_ACTION_PUBLICAR_CONF_0";

    public static final String ENCUESTA_PUBLICAR_ERROR = "ENCUESTA_PUBLICAR_ERROR";

    public static final String GUI_ENCUESTAS_LIST_CERRAR_COLUMN_0 = "GUI_ENCUESTAS_LIST_CERRAR_COLUMN_0";

    public static final String GUI_ENCUESTAS_LIST_ACTION_CERRAR_NAME_0 = "GUI_ENCUESTAS_LIST_ACTION_CERRAR_NAME_0";

    public static final String GUI_ENCUESTAS_LIST_ACTION_CERRAR_HELP_0 = "GUI_ENCUESTAS_LIST_ACTION_CERRAR_HELP_0";

    public static final String GUI_ENCUESTAS_LIST_ACTION_CERRAR_CONF_0 = "GUI_ENCUESTAS_LIST_ACTION_CERRAR_CONF_0";

    public static final String ENCUESTA_CERRAR_ERROR = "ENCUESTA_CERRAR_ERROR";

    public static final String ENCUESTA_PUBLICAR_ACTIVA_ERROR = "ENCUESTA_PUBLICAR_ACTIVA_ERROR";

    public static final String GUI_ENCUESTAS_SINCRONIZAR_0 = "GUI_ENCUESTAS_SINCRONIZAR_0";

	public static final String GUI_ENCUESTAS_LIST_ACTIVAR_COLUMN_0 = "GUI_ENCUESTAS_LIST_ACTIVAR_COLUMN_0";

	public static final String GUI_ENCUESTAS_LIST_ACTION_ACTIVAR_NAME_0 = "GUI_ENCUESTAS_LIST_ACTION_ACTIVAR_NAME_0";

	public static final String GUI_ENCUESTAS_LIST_ACTION_ACTIVAR_HELP_0 = "GUI_ENCUESTAS_LIST_ACTION_ACTIVAR_HELP_0";

	public static final String GUI_ENCUESTAS_LIST_ACTION_ACTIVAR_CONF_0 = "GUI_ENCUESTAS_LIST_ACTION_ACTIVAR_CONF_0";

	public static final String ERR_RESPUESTAS_CANTIDAD_VALIDATION_0 = "ERR_RESPUESTAS_CANTIDAD_VALIDATION_0";
	
	public static final String TOGGLE_VER_RESULTADOS = "TOGGLE_VER_RESULTADOS";
	
	public static final String TOGGLE_OCULTAR_RESULTADOS = "TOGGLE_OCULTAR_RESULTADOS";
	
	public static final String GUI_ENCUESTAS_DETAIL_0 = "GUI_ENCUESTAS_DETAIL_0";

    public static final String ERR_CATEGORIAS_VALIDATION_0 = "ERR_CATEGORIAS_VALIDATION_0";

    public String getBundleName() {
        return BUNDLE_NAME;
    }

    public static I_CmsMessageBundle get() {
        return INSTANCE;
    }

}
