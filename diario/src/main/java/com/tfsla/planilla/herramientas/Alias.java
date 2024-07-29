package com.tfsla.planilla.herramientas;

import com.tfsla.opencmsdev.module.TfsConstants;

public class Alias extends ComboOption {

    private Pagina pagina;
    private String fileName;

    public Alias(String valor, String descripcion, String fileName) {
        super(valor, descripcion);
        this.fileName = fileName;
    }
    
    public Alias(String valor) {
        super(valor, valor);
        this.fileName = TfsConstants.HOME_FILENAME;
    }

    public Pagina getPagina() {
        return this.pagina;
    }

    public void setPagina(Pagina pagina) {
        this.pagina = pagina;
    }

    public String getFileName() {
        return this.fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    
}
