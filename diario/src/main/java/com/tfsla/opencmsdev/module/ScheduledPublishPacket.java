package com.tfsla.opencmsdev.module;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import com.tfsla.collections.CollectionFactory;

public class ScheduledPublishPacket implements Iterable<String> {

    private Collection<String> resources = CollectionFactory.createCollection();
    private Date publishDate;
    private String estado;
    private long packetId;
    private int projectId;

    public static String PENDIENTE = "Pendiente";
    public static String PUBLICADO = "Publicado";
    
    // **********************
    // ** Construcción **
    // **********************

    public ScheduledPublishPacket(Collection<String> resources, java.sql.Date publishDate, long packetId, int projectId) {
        this.resources = resources;
        this.publishDate = new Date(publishDate.getTime());
        this.packetId = packetId;
        this.projectId = projectId;
        this.estado = PENDIENTE;
    }
    
    public ScheduledPublishPacket(java.sql.Date publishDate, long packetId, int projectId, String estado) {
        this.publishDate = new Date(publishDate.getTime());
        this.packetId = packetId;
        this.projectId = projectId;
        this.estado = estado;
    }
    
    // **********************
    // ** Métodos públicos **
    // **********************
    
    public Iterator<String> iterator() {
        return this.getResources().iterator();
    }
    
    public int getCantRecursos() {
        return this.getResources().size();
    }
    
    public String getFechaFormateada() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        return dateFormat.format(this.getPublishDate());
    }
    
    // ***************
    // ** Accessors **
    // ***************
    
    public long getPacketId() {
        return packetId;
    }

    public void setPacketId(long packetId) {
        this.packetId = packetId;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public Date getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(Date publishDate) {
        this.publishDate = publishDate;
    }

    public Collection<String> getResources() {
        return resources;
    }

    public void setResources(Collection<String> resources) {
        this.resources = resources;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

}