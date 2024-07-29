package com.tfsla.opencmsdev.module.pages;

/**
 * Representa a una secci�n en el diario (o en otro proyecto), contiene su nombre, descripci�n y nombre de p�gina.
 *
 * @author mpotelfeola
 */
public class Section {

    private int id;
    private int tipoEdicionId;
    private String name;
    private String description;
    private String pageName;

    // ******************
    // ** Construcci�n **
    // ******************

    public Section(int id, int tipoEdicionId, String name, String description, String pageName) {
        this.id = id;
        this.tipoEdicionId = tipoEdicionId;
        this.name = name;
        this.description = description;
        this.pageName = pageName;
    }

    // ***************
    // ** Accessors **
    // ***************

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

    public String getPageName() {
        return pageName;
    }

    public void setPageName(String pageName) {
        this.pageName = pageName;
    }

    public int getTipoEdicionId() {
        return tipoEdicionId;
    }

    public void setTipoEdicionId(int tipoEdicionId) {
        this.tipoEdicionId = tipoEdicionId;
    }

}
