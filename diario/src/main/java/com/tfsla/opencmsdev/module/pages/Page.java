package com.tfsla.opencmsdev.module.pages;

/**
 * Representa a una p�gina en el projecto (o si se quiere, a un tipo de p�gina). Por ejemplo en el diario s�lo hay
 * dos p�ginas Home y Secci�n. Se agrupan porque tienen las mismas zonas y usan el mismo template, y puedo poner
 * noticias en cualquiera de ellas. En la edici�n impresa podr�a agregarse Suplemento, aunque como comparten
 * template podr�an ser secciones tambi�n.
 *
 * @author mpotelfeola
 */
public class Page {

    private int id;
//    private int idProject;
    private String name;

    // ******************
    // ** Construcci�n **
    // ******************

//    public Page(int id, int idProject, String name) {
    public Page(int id, String name) {
    	this.id = id;
 //       this.idProject = idProject;
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
/*
    public int getIdProject() {
        return idProject;
    }

    public void setIdProject(int idProject) {
        this.idProject = idProject;
    }
*/
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
