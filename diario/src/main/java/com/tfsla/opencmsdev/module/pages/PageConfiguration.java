package com.tfsla.opencmsdev.module.pages;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.tfsla.collections.CollectionFactory;
import com.tfsla.opencmsdev.module.TfsConstants;
import com.tfsla.utils.TfsDAO;

/**
 * Clase que facilita el acceso a la base de la informaci�n sobre las p�ginas de los distintos proyectos del sistema.
 *
 * Es un Singleton, mantiene toda la informaci�n en memoria y se recarga haciendo reload, para minimizar el impacto en performance.
 *
 * @author mpotelfeola
 */
public class PageConfiguration {

    private static PageConfiguration instance = new PageConfiguration();

    private Collection<Project> projects = CollectionFactory.createCollection();
    private Collection<Page> pages = CollectionFactory.createCollection();
    private Map<Project, Collection<Section>> sections = CollectionFactory.createMap();

    private Map<Project, Map<Page, Collection<Zone>>> zones = new HashMap<Project, Map<Page, Collection<Zone>>>();

    private Map<Project, Map<String, String>> seccionDescriptions = CollectionFactory.createMap();
    private Map<Project, Map<String, String>> seccionPageNames = CollectionFactory.createMap();

    private Map<String, Project> projectsByName = CollectionFactory.createMap();
    private Map<Integer, Project> projectsById = CollectionFactory.createMap();


    // ********************
    // ** Inicializaci�n **
    // ********************

    /**
     * Obtiene la informaci�n necesaria de la base de datos.
     */
    public void init() {
        this.projects = TfsDAO.getProjectsFromDB();
        this.pages =  TfsDAO.getPagesFromDB();
        for (Project project : this.projects) {

            this.sections.put(project, TfsDAO.getSectionsFromDB(project));
            this.projectsByName.put(project.getName(), project);
            this.projectsById.put(project.getId(), project);

            for (Page page : this.pages) {
            	Map<Page, Collection<Zone>> zonasDePagina = null;
            	zonasDePagina = this.zones.get(project);

            	if (zonasDePagina == null)
            		zonasDePagina = CollectionFactory.createMap();
            	zonasDePagina.put(page, TfsDAO.getZonesFromDB(page,project));
            	this.zones.put(project,zonasDePagina);
            }

            Map<String, String> descriptions = CollectionFactory.createMap();
            this.seccionDescriptions.put(project, descriptions);

            Map<String, String> pageNames = CollectionFactory.createMap();
            this.seccionPageNames.put(project, pageNames);

            for (Section section : this.sections.get(project)) {
                this.seccionDescriptions.get(project).put(section.getName(), section.getDescription());
                this.seccionPageNames.get(project).put(section.getName(), section.getPageName());
            }
        }
    }

    /**
     * Reinicia la instancia. Vuelve a obtener de la base todos los datos necesarios.
     * Se utiliza para refrescar cuando se modifica la base de datos, ya que los datos son siempre
     * guardados en memoria.
     */
    public void reload() {
        this.projects = CollectionFactory.createCollection();
        this.pages = CollectionFactory.createCollection();
        this.sections = CollectionFactory.createMap();
        this.zones = CollectionFactory.createMap();
        this.seccionDescriptions = CollectionFactory.createMap();
        this.seccionPageNames = CollectionFactory.createMap();
        this.init();
    }

    // ******************
    // ** Construcci�n **
    // ******************

    /**
     * Constructor privado para el Singleton
     */
    private PageConfiguration() {
        this.init();
    }

    /**
     * Devuelve la instancia activa.
     */
    public static PageConfiguration getInstance() {
        return instance;
    }

    // ***************
    // ** Accessors **
    // ***************

    /**
     * Devuelve un proyecto buscandolo por su nombre. Si no lo encuentra devuelve null.
     */
    public Project getProjectByName(String name) {
        for (Project project : this.projects) {
            if (project.getName().equals(name)) {
                return project;
            }
        }

        return null;
    }

    /**
     * Devuelve un proyecto buscandolo por su ID. Si no lo encuentra devuelve null.
     */
    public Project getProjectById(int id) {
        return this.projectsById.get(id);
    }

    /**
     * Devuelve una p�gina buscandola por su nombre y proyecto al que pertenece. Si no lo encuentra devuelve null.
     */
    public Page getPageByName(String name) {
        for (Page page : this.pages) {
            if (page.getName().equals(name)) {
                return page;
            }
        }

        return null;
    }


    /**
     * Devuelve una Collection de p�ginas pertenecientes a un proyecto.
     */
   /* public Collection<Page> getPagesByProject(Project project) {
        return this.pages.get(project);
    }
*/
    /**
     * Devuelve una Collection de secciones pertenecientes a un proyecto.
     */
    public Collection<Section> getSectionsByProject(Project project) {
        return this.sections.get(project);
    }

    public Collection<Section> getSectionsByProject(String projectName) {
        return this.sections.get(this.projectsByName.get(projectName));
    }


    /**
     * Devuelve una Collection de zonas pertenecientes a una p�gina.
     */
    public Collection<Zone> getZonesByPage(Page page, Project project) {
        return this.zones.get(project).get(page);
    }

    /**
     * Devuelve la descripci�n de una secci�n buscandola por su nombre y el proyecto al que pertenece.
     */
    public String getSectionDescription(Project project, String sectionName) {
        String description = this.seccionDescriptions.get(project).get(sectionName);
        if (description == null) return "";
        return description;
    }

    /**
     * Devuelve el nombre de la p�gina de una secci�n buscandola por su nombre y el proyecto al que pertenece.
     */
    public String getSectionPageName(Project project, String sectionName) {
        return this.seccionPageNames.get(project).get(sectionName);
    }

	public Project getDiarioOnlineProject() {
		return this.getProjectById(TfsConstants.ONLINE_PROJECT_ID);
	}

	public Project getEdicionImpresaProject() {
		return this.getProjectById(TfsConstants.EDICIONIMPRESA_PROJECT_ID);
	}

}
