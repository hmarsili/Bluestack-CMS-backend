package com.tfsla.opencmsdev.module.pages;

/**
 * Representa a un proyecto en OpenCms, como por ejemplo el diario o la edición impresa.
 * 
 * @author mpotelfeola
 */
public class Project {

    private int id;
    private String name;

    // ******************
    // ** Construcción **
    // ******************
    
    public Project(int id, String name) {
        this.id = id;
        this.name = name;
    }
    
    // ***************
    // ** Accessors **
    // ***************

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

}
