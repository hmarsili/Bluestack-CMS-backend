package com.tfsla.widgets;

import org.opencms.widgets.I_CmsWidget;

/**
 * Widget para las noticias del Diario
 * 
 * @author mpotelfeola
 */
public class NoticiaWidget extends AbstractNoticiaWidget {

    public NoticiaWidget() {
        super();
    }
    
    public NoticiaWidget(String configuration) {
        super(configuration);
    }
    
    public I_CmsWidget newInstance() {
        return new NoticiaWidget(getConfiguration());
    }

    @Override
    public String getNombrePagina() {
        return "buscador.html";
    }
}
