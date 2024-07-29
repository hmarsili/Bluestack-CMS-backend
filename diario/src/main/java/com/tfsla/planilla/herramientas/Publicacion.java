package com.tfsla.planilla.herramientas;

public class Publicacion {
    
	private String id;
    private String nombre;
    private String descripcion;
    private String tipo;
    
    public Publicacion(String id, String nombre, String descripcion, String tipo) {
        this.id = id;
    	this.nombre = nombre;
        this.descripcion = descripcion;
        this.tipo = tipo;
    }
    
    public String getId() {
        return this.id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getNombre() {
        return this.nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }    
    
    public String getDescripcion() {
        return this.descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public String getTipo() {
        return this.tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
}