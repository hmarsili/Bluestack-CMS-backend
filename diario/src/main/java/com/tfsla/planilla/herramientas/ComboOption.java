package com.tfsla.planilla.herramientas;

public class ComboOption {

    public String valor;
    public String descripcion;

    public ComboOption(String value, String description) {
        this.valor = value;
        this.descripcion = description;
    }
    
    public String getDescripcion() {
        return this.descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getValor() {
        return this.valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

}
