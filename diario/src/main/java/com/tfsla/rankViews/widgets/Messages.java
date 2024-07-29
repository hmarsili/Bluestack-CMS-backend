package com.tfsla.rankViews.widgets;

import org.opencms.i18n.A_CmsMessageBundle;
import org.opencms.i18n.I_CmsMessageBundle;

public class Messages extends A_CmsMessageBundle {

	private static final String BUNDLE_NAME = "com.tfsla.rankViews.widgets.messages";

    private static final I_CmsMessageBundle INSTANCE = new Messages();

    public static final String GUI_VISITADOS_LIST_NAME_0 = "GUI_VISITADOS_LIST_NAME_0";

    public static final String VISITADOS_LIST_TITULO_NOTICIA_COLUMN = "VISITADOS_LIST_TITULO_NOTICIA_COLUMN";
    public static final String VISITADOS_LIST_CANTIDAD_NOTICIA_COLUMN = "VISITADOS_LIST_CANTIDAD_NOTICIA_COLUMN";

    public static final String VISITADOS_LIST_SECCION_NOTICIA_COLUMN = "VISITADOS_LIST_SECCION_NOTICIA_COLUMN";
    public static final String VISITADOS_LIST_URL_NOTICIA_COLUMN = "VISITADOS_LIST_URL_NOTICIA_COLUMN";
    public static final String VISITADOS_LIST_FECHA_NOTICIA_COLUMN = "VISITADOS_LIST_FECHA_NOTICIA_COLUMN";

	public static final String RECOMENDADOS_LIST_CANTIDAD_NOTICIA_COLUMN = "RECOMENDADOS_LIST_CANTIDAD_NOTICIA_COLUMN";

	public static final String GUI_RECOMENDADOS_LIST_NAME_0 = "GUI_RECOMENDADOS_LIST_NAME_0";

	public static final String COMENTADOS_LIST_CANTIDAD_NOTICIA_COLUMN = "COMENTADOS_LIST_CANTIDAD_NOTICIA_COLUMN";

	public static final String GUI_COMENTADOS_LIST_NAME_0 = "GUI_COMENTADOS_LIST_NAME_0";

	public static final String GUI_VALORADOS_LIST_NAME_0 = "GUI_VALORADOS_LIST_NAME_0";

	public static final String VALORADOS_LIST_CANTIDAD_NOTICIA_COLUMN = "VALORADOS_LIST_CANTIDAD_NOTICIA_COLUMN";

	public static final String VISITADOS_LIST_TIPO_NOTICIA_COLUMN = "VISITADOS_LIST_TIPO_NOTICIA_COLUMN";

	public String getBundleName() {
        return BUNDLE_NAME;
    }

    public static I_CmsMessageBundle get() {
        return INSTANCE;
    }


}
