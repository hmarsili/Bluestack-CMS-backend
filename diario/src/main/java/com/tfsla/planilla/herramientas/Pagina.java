package com.tfsla.planilla.herramientas;

import java.util.Collection;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;

import com.tfsla.collections.CollectionFactory;
import com.tfsla.opencmsdev.module.TfsConstants;
import com.tfsla.opencmsdev.module.pages.Section;

public class Pagina {

    private String nombre;
    private int id;
    private Collection<Alias> aliases = CollectionFactory.createCollection();

    public Pagina(String nombre, int id) {
        this.nombre = nombre;
        this.id = id;
    }

    public boolean containsAlias(String valorAlias) {
        return CollectionUtils.exists(this.aliases, new PredicateAlias(valorAlias));
    }

    public Pagina addAlias(String valor, String descripcion, String fileName) {
        Alias alias = new Alias(valor, descripcion, fileName);
        alias.setPagina(this);
        this.aliases.add(alias);
        return this;
    }

    public void addAliases(Collection<Section> sections) {
        for (Section section : sections) {
            this.addAlias(section.getName(), StringUtils.capitalize(section.getDescription()), section
                    .getPageName());
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Pagina addAlias(String valor, String descripcion) {
        return this.addAlias(valor, descripcion, TfsConstants.HOME_FILENAME);
    }

    public String getNombre() {
        return this.nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Collection<Alias> getAliases() {
        return this.aliases;
    }

}

class PredicateAlias implements Predicate {

    private String valorAlias;

    public PredicateAlias(String valorAlias) {
        this.valorAlias = valorAlias;
    }

    public boolean evaluate(Object arg0) {
        if (this.valorAlias == null)
            return false;

        Alias alias = (Alias) arg0;
        return this.valorAlias.equals(alias.getValor());
    }

}
