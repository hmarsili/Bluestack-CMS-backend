package com.tfsla.widgets;

import org.opencms.widgets.I_CmsWidget;

/**
 * Widget para las noticias de la Edición Impresa
 * 
 * @author mpotelfeola
 */
public class NoticiaEdimpWidget extends AbstractNoticiaWidget {

    public NoticiaEdimpWidget() {
        super();
    }
    
    public NoticiaEdimpWidget(String configuration) {
        super(configuration);
    }
    
    public I_CmsWidget newInstance() {
        return new NoticiaEdimpWidget(getConfiguration());
    }

    @Override
    public String getNombrePagina() {
        return "buscadorEdimp.html";
    }
}
