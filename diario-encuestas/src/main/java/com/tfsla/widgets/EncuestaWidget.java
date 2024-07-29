package com.tfsla.widgets;

import org.opencms.widgets.I_CmsWidget;
import org.opencms.widgets.I_CmsWidget;

/**
 * Widget para las encuestas
 * 
 */
public class EncuestaWidget extends AbstractEncuestaWidget {

    public EncuestaWidget() {
        super();
    }
    
    public EncuestaWidget(String configuration) {
        super(configuration);
    }
    
    public I_CmsWidget newInstance() {
        return new EncuestaWidget(getConfiguration());
    }

    @Override
    public String getNombrePagina() {
        return "buscadorEncuestas.html";
    }
}
