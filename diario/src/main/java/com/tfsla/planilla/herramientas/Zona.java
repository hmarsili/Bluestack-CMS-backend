package com.tfsla.planilla.herramientas;

public class Zona {
    
    private String nombre;
    private String color;
    private Pagina pagina;
    private String descripcion;
    private int orden;
    private int publicacion;
    
    public Zona(String nombre, Pagina pagina, String color, String descripcion, int orden) {
        this.nombre = nombre;
        this.pagina = pagina;
        this.color = color;
        this.descripcion = descripcion;
        this.orden = orden;
        this.publicacion = 0;
    }
    
    public Zona(String nombre, String color, String descripcion, int orden) {
        this.nombre = nombre;
        this.color = color;
        this.descripcion = descripcion;
        this.orden = orden;
        this.pagina = new Pagina("", 0);
        this.publicacion = 0;
    }    
    
    public Zona(String nombre, Pagina pagina, String color, String descripcion, int orden, int publicacion) {
        this.nombre = nombre;
        this.pagina = pagina;
        this.color = color;
        this.descripcion = descripcion;
        this.orden = orden;
        this.publicacion = publicacion;
    }
    
    public Zona(String nombre, String color, String descripcion, int orden, int publicacion) {
        this.nombre = nombre;
        this.color = color;
        this.descripcion = descripcion;
        this.orden = orden;
        this.pagina = new Pagina("", 0);
        this.publicacion = publicacion;
    }
    
    public boolean pertenecePagina(String aliasPagina) {
        return this.pagina.containsAlias(aliasPagina);
    }
    
    public String getColor() {
        return this.color;
    }
    
    public void setColor(String color) {
        this.color = color;
    }
    
    public String getNombre() {
        return this.nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public Pagina getPagina() {
        return this.pagina;
    }
    
    public void setPagina(Pagina pagina) {
        this.pagina = pagina;
    }

    public String getDescripcion() {
        return this.descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getOrden() {
        return this.orden;
    }

    public void setOrden(int orden) {
        this.orden = orden;
    }
    
    public int getPublicacion() {
        return this.publicacion;
    }

    public void setPublicacion(int publicacion) {
        this.publicacion = publicacion;
    }    
    
}
