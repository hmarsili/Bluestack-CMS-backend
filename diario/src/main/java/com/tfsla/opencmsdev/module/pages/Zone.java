package com.tfsla.opencmsdev.module.pages;

/**
 * Representa a una zona de una p�gina. Tiene atributos que sirven para mostrarla o para manejarla desde la planilla
 * de administraci�n (nombre, color, orden, etc).
 * 
 * @author mpotelfeola
 */
public class Zone {

    private int id;
    private int pageId;
    private String name;
    private String description;
    private String color;
    private int order;
    private int publication;

    // ******************
    // ** Construcci�n **
    // ******************
    
    public Zone(int id, int pageId, String name, String color, String description, int order) {
        this.id = id;
        this.pageId = pageId;
        this.name = name;
        this.description = description;
        this.color = color;
        this.order = order;
        this.publication = 0;
    }    
    
    public Zone(int id, int pageId, String name, String color, String description, int order, int publication) {
        this.id = id;
        this.pageId = pageId;
        this.name = name;
        this.description = description;
        this.color = color;
        this.order = order;
        this.publication = publication;
    }
    
    // ***************
    // ** Accessors **
    // ***************
    
    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
    
    public int getPublication() {
        return publication;
    }

    public void setPublication(int publication) {
        this.publication = publication;
    }    

    public int getPageId() {
        return pageId;
    }

    public void setPageId(int pageId) {
        this.pageId = pageId;
    }

}
