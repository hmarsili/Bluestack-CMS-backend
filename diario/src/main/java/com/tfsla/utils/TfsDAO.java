package com.tfsla.utils;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;

import org.opencms.db.CmsDbContext;
import org.opencms.file.CmsObject;
import org.opencms.main.TfsContext;

import com.tfsla.collections.CollectionFactory;
import com.tfsla.exceptions.ApplicationException;
import com.tfsla.opencmsdev.module.ScheduledPublishPacket;
import com.tfsla.opencmsdev.module.pages.Page;
import com.tfsla.opencmsdev.module.pages.Project;
import com.tfsla.opencmsdev.module.pages.Section;
import com.tfsla.opencmsdev.module.pages.TfsSQLConstants;
import com.tfsla.opencmsdev.module.pages.Zone;
import com.tfsla.workflow.QueryBuilder;
import com.tfsla.workflow.ResultSetProcessor;

/**
 * Este objeto facilita el acceso a los datos de las p�ginas almacenados en la DB.
 *
 * @author mpotelfeola
 */
public class TfsDAO implements TfsSQLConstants {

    public static Collection<Project> getProjectsFromDB() {
        String query = "SELECT * FROM TFS_PROJECT";

        Collection<Project> proyectos = new QueryBuilder<Collection<Project>>(TfsContext.getInstance()
                .getCmsObject()).setSQLQuery(query).execute(new ResultSetProcessor<Collection<Project>>() {

            private Collection<Project> projects = CollectionFactory.createCollection();

            public void processTuple(ResultSet rs) {

                try {
                    projects.add(new Project(rs.getInt(ID_PROJECT), rs.getString(PROJECT_NAME)));
                }
                catch (Exception e) {
                    throw new ApplicationException("No se pudo leer los proyectos de la DB", e);
                }
            }

            public Collection<Project> getResult() {
                return this.projects;
            }
        });

        return proyectos;
    }

    public static Collection<Page> getPagesFromDB() {
        String query = "SELECT * FROM TFS_PAGE ";

        Collection<Page> proyectos = new QueryBuilder<Collection<Page>>(TfsContext.getInstance().getCmsObject())
                .setSQLQuery(query).execute(new ResultSetProcessor<Collection<Page>>() {

                    private Collection<Page> pages = CollectionFactory.createCollection();

                    public void processTuple(ResultSet rs) {

                        try {
                            pages.add(new Page(rs.getInt(ID_PAGE), rs.getString(PAGE_NAME)));
                        }
                        catch (Exception e) {
                            throw new ApplicationException("No se pudo leer las p�ginas de la DB", e);
                        }
                    }

                    public Collection<Page> getResult() {
                        return this.pages;
                    }
                });

        return proyectos;
    }

/*
    public static Collection<Page> getPagesFromDB(Project project) {
        String query = "SELECT * FROM TFS_PAGE WHERE ID_PROJECT = " + project.getId();

        Collection<Page> proyectos = new QueryBuilder<Collection<Page>>(TfsContext.getInstance().getCmsObject())
                .setSQLQuery(query).execute(new ResultSetProcessor<Collection<Page>>() {

                    private Collection<Page> pages = CollectionFactory.createCollection();

                    public void processTuple(ResultSet rs) {

                        try {
                            pages.add(new Page(rs.getInt(ID_PAGE), rs.getInt(ID_PROJECT), rs.getString(PAGE_NAME)));
                        }
                        catch (Exception e) {
                            throw new ApplicationException("No se pudo leer las p�ginas de la DB", e);
                        }
                    }

                    public Collection<Page> getResult() {
                        return this.pages;
                    }
                });

        return proyectos;
    }
*/
    public static Collection<Zone> getZonesFromDB(Page page, Project project) {
        String query = "SELECT * FROM TFS_ZONE INNER JOIN TFS_TIPO_EDICIONES ON TFS_TIPO_EDICIONES.id = TFS_ZONE.ID_TIPOEDICION WHERE TFS_TIPO_EDICIONES.PROYECTO = '" + project.getName() + "' AND ID_PAGE = " + page.getId() + " AND TIPOPUBLICACION=1";

        Collection<Zone> zonas = new QueryBuilder<Collection<Zone>>(TfsContext.getInstance().getCmsObject())
                .setSQLQuery(query).execute(new ResultSetProcessor<Collection<Zone>>() {

                    private Collection<Zone> zones = CollectionFactory.createCollection();

                    public void processTuple(ResultSet rs) {

                        try {
                            zones.add(new Zone(rs.getInt(ID_ZONE), rs.getInt(ID_PAGE), rs.getString(ZONE_NAME), rs
                                    .getString(ZONE_COLOR), rs.getString(ZONE_DESCRIPTION), rs.getInt(ZONE_ORDER), rs.getInt("ID_TIPOEDICION")));
                        }
                        catch (Exception e) {
                            throw new ApplicationException("No se pudo leer las zonas de la DB.", e);
                        }
                    }

                    public Collection<Zone> getResult() {
                        return this.zones;
                    }

                });

        return zonas;
    }

    public static Collection<Section> getSectionsFromDB(Project project) {
        String query = "SELECT * FROM TFS_SECTION INNER JOIN TFS_TIPO_EDICIONES ON TFS_TIPO_EDICIONES.id = TFS_SECTION.ID_TIPOEDICION WHERE TFS_TIPO_EDICIONES.PROYECTO = '" + project.getName() + "'";

        Collection<Section> secciones = new QueryBuilder<Collection<Section>>(TfsContext.getInstance()
                .getCmsObject()).setSQLQuery(query).execute(new ResultSetProcessor<Collection<Section>>() {

            private Collection<Section> sections = CollectionFactory.createCollection();

            public void processTuple(ResultSet rs) {

                try {
                    sections
                            .add(new Section(rs.getInt(ID_SECTION), rs.getInt(ID_TIPOEDICION), rs
                                    .getString(SECTION_NAME), rs.getString(SECTION_DESCRIPTION), rs
                                    .getString(SECTION_PAGE)));

                }
                catch (Exception e) {
                    throw new ApplicationException("No se pudo leer las secciones de la DB.", e);
                }
            }

            public Collection<Section> getResult() {
                return this.sections;
            }
        });

        return secciones;
    }

    /**
     * Devuelve todos los paquetes de publicaci�n diferida listos para publicar. Se usa en el scheduled job
     * de publicaci�n diferida.
     */
    public static List<ScheduledPublishPacket> obtainReadyToPublishPackets(CmsObject cms, int projectId) {
        return obtainPackets(cms, obtainReadyToPublishPacketIds(cms, projectId));
    }

    /**
     * Devuelve todos los paquetes de publicaci�n diferida pendientes de publicaci�n.
     */
    public static List<ScheduledPublishPacket> obtainPendingPackets(int projectId) {
        CmsObject cms = TfsContext.getInstance().getCmsObject();
        return obtainPackets(cms, obtainPendingPacketIds(cms, projectId));
    }

    /**
     * Devuelve todos los paquetes de publicaci�n diferida (publicados o no).
     */
    public static List<ScheduledPublishPacket> obtainAllPackets(int projectId) {
        CmsObject cms = TfsContext.getInstance().getCmsObject();
        return obtainPackets(cms, obtainAllPacketIds(cms, projectId));
    }

    private static List<ScheduledPublishPacket> obtainPackets(CmsObject cms, List<Long> packetIds) {
        try {
            List<ScheduledPublishPacket> packets = CollectionFactory.createList();
            for (Long packetId : packetIds) {
                packets.add(obtainPacket(cms, packetId.longValue()));
            }
            return packets;
        }
        catch (Exception e) {
            throw new ApplicationException("No se pudo obtener los paquetes de de la DB.", e);
        }
    }

    public static void deletePacket(long packetId) {
        CmsObject cms = TfsContext.getInstance().getCmsObject();
        // Inicializaci�n del QueryBuilder, que al ejecutarse desde el Job es distinto.
        CmsDbContext dbc = TFSDriversContainer.getInstance().getDBContextFactory().getDbContext(
                cms.getRequestContext());
        QueryBuilder<String> queryBuilder = new QueryBuilder<String>(TFSDriversContainer.getInstance()
                .getSqlManager(), dbc);

        String query = "DELETE FROM TFS_SCHEDULED_PUBLISH_RESOURCE WHERE PACKET_ID = " + packetId;
        queryBuilder.setSQLQuery(query).execute();

        queryBuilder = new QueryBuilder<String>(TFSDriversContainer.getInstance().getSqlManager(), dbc);
        query = "DELETE FROM TFS_SCHEDULED_PUBLISH_PACKET WHERE PACKET_ID = " + packetId;
        queryBuilder.setSQLQuery(query).execute();
    }

    private static ScheduledPublishPacket obtainPacket(CmsObject cms, long packetId) {
        String query = "SELECT * FROM TFS_SCHEDULED_PUBLISH_PACKET WHERE PACKET_ID = " + packetId;

        // Inicializaci�n del QueryBuilder, que al ejecutarse desde el Job es distinto.
        CmsDbContext dbc = TFSDriversContainer.getInstance().getDBContextFactory().getDbContext(
                cms.getRequestContext());
        QueryBuilder<ScheduledPublishPacket> queryBuilder = new QueryBuilder<ScheduledPublishPacket>(
                TFSDriversContainer.getInstance().getSqlManager(), dbc);

        ScheduledPublishPacket packet = queryBuilder.setSQLQuery(query).execute(
                new ResultSetProcessor<ScheduledPublishPacket>() {

                    private ScheduledPublishPacket packetFromDb;

                    public void processTuple(ResultSet rs) {

                        try {
                            Timestamp timeStamp = rs.getTimestamp(PUBLISH_TIME);
                            packetFromDb = new ScheduledPublishPacket(new Date(timeStamp.getTime()), rs
                                    .getLong(PACKET_ID), rs.getInt(ID_PROJECT), rs.getString(ESTADO_PAQUETE));
                        }
                        catch (Exception e) {
                            throw new ApplicationException("No se pudo obtener el paquete de la DB.", e);
                        }
                    }

                    public ScheduledPublishPacket getResult() {
                        return this.packetFromDb;
                    }
                });

        packet.setResources(obtainResourcesFromPacket(cms, packetId));
        return packet;
    }

    private static Collection<String> obtainResourcesFromPacket(CmsObject cms, long packetId) {
        String query = "SELECT RESOURCE_NAME FROM TFS_SCHEDULED_PUBLISH_RESOURCE WHERE PACKET_ID = " + packetId;

        // Inicializaci�n del QueryBuilder, que al ejecutarse desde el Job es distinto.
        CmsDbContext dbc = TFSDriversContainer.getInstance().getDBContextFactory().getDbContext(
                cms.getRequestContext());
        QueryBuilder<Collection<String>> queryBuilder = new QueryBuilder<Collection<String>>(TFSDriversContainer
                .getInstance().getSqlManager(), dbc);

        Collection<String> resources = queryBuilder.setSQLQuery(query).execute(
                new ResultSetProcessor<Collection<String>>() {

                    private Collection<String> resourcesFromDb = CollectionFactory.createCollection();

                    public void processTuple(ResultSet rs) {

                        try {
                            resourcesFromDb.add(rs.getString(RESOURCE_NAME));
                        }
                        catch (Exception e) {
                            throw new ApplicationException(
                                    "No se pudo leer los n�meros de ID de paquete de la DB.", e);
                        }
                    }

                    public Collection<String> getResult() {
                        return this.resourcesFromDb;
                    }
                });

        return resources;
    }

    public static void markPublishedPacket(CmsObject cms, long packetId) {
        // Inicializaci�n del QueryBuilder, que al ejecutarse desde el Job es distinto.
        CmsDbContext dbc = TFSDriversContainer.getInstance().getDBContextFactory().getDbContext(
                cms.getRequestContext());
        QueryBuilder<String> queryBuilder = new QueryBuilder<String>(TFSDriversContainer.getInstance()
                .getSqlManager(), dbc);

        String query = "UPDATE TFS_SCHEDULED_PUBLISH_PACKET SET ESTADO = '" + ScheduledPublishPacket.PUBLICADO + "' WHERE PACKET_ID = "
                + packetId;
        queryBuilder.setSQLQuery(query).execute();
    }

    /**
     * Obtiene los Ids de todos los paquetes de publicaci�n diferidas que est�n pendientes.
     */
    private static List<Long> obtainPendingPacketIds(CmsObject cms, int projectId) {
        String query = "SELECT DISTINCT PACKET_ID FROM TFS_SCHEDULED_PUBLISH_PACKET WHERE ESTADO = '" + ScheduledPublishPacket.PENDIENTE + "' AND "
                + "ID_PROJECT = " + projectId + " ORDER BY PUBLISH_TIME ASC";

        return obtainPacketIdsFromDb(cms, query);
    }

    /**
     * Obtiene los Ids de todos los paquetes de publicaci�n diferidas que est�n listos para publicar.
     */
    private static List<Long> obtainReadyToPublishPacketIds(CmsObject cms, int projectId) {
        String query = "SELECT DISTINCT PACKET_ID FROM TFS_SCHEDULED_PUBLISH_PACKET WHERE ESTADO = '" + ScheduledPublishPacket.PENDIENTE + "' AND "
                + "ID_PROJECT = "
                + projectId
                + " AND PUBLISH_TIME <= CURRENT_TIMESTAMP() ORDER BY PUBLISH_TIME ASC";

        return obtainPacketIdsFromDb(cms, query);
    }

    /**
     * Obtiene los Ids de todos los paquetes de publicaci�n diferidas que est�n listos para publicar.
     */
    private static List<Long> obtainAllPacketIds(CmsObject cms, int projectId) {
        String query = "SELECT DISTINCT PACKET_ID FROM TFS_SCHEDULED_PUBLISH_PACKET WHERE ID_PROJECT = "
                + projectId
                + " ORDER BY PUBLISH_TIME ASC";

        return obtainPacketIdsFromDb(cms, query);
    }

    /**
     * Obtiene los Ids de todos los paquetes de publicaci�n seg�n el query proporcionado.
     */
    private static List<Long> obtainPacketIdsFromDb(CmsObject cms, String query) {
        // Inicializaci�n del QueryBuilder, que al ejecutarse desde el Job es distinto.
        CmsDbContext dbc = TFSDriversContainer.getInstance().getDBContextFactory().getDbContext(
                cms.getRequestContext());
        QueryBuilder<List<Long>> queryBuilder = new QueryBuilder<List<Long>>(TFSDriversContainer.getInstance()
                .getSqlManager(), dbc);

        List<Long> packetIds = queryBuilder.setSQLQuery(query).execute(new ResultSetProcessor<List<Long>>() {

            private List<Long> packetIdsFromDB = CollectionFactory.createList();

            public void processTuple(ResultSet rs) {

                try {
                    packetIdsFromDB.add(new Long(rs.getLong(PACKET_ID)));
                }
                catch (Exception e) {
                    throw new ApplicationException("No se pudo leer los n�meros de ID de paquete de la DB.", e);
                }
            }

            public List<Long> getResult() {
                return this.packetIdsFromDB;
            }
        });

        return packetIds;
    }

    /**
     * Inserta todos los recursos para publicaci�n diferida.
     */
    public static void insertScheduledPublishPacket(Collection<String> resources, String date, int projectId) {
        long packetId = obtainNextPacketId();

        String query = "INSERT INTO TFS_SCHEDULED_PUBLISH_PACKET (PACKET_ID, PUBLISH_TIME, ID_PROJECT) "
                + "VALUES (" + packetId + ", '" + date + ":00', " + projectId + ")";
        new QueryBuilder<String>(TfsContext.getInstance().getCmsObject()).setSQLQuery(query).execute();

        for (String resource : resources) {
            insertScheduledPublishResource(packetId, resource);
        }
    }

    /**
     * Inserta un recurso para publicaci�n diferida.
     */
    public static void insertScheduledPublishResource(long packetId, String resource) {
        String query = "INSERT INTO TFS_SCHEDULED_PUBLISH_RESOURCE (RESOURCE_NAME, PACKET_ID) " + "VALUES ('"
                + resource + "', " + packetId + ")";

        new QueryBuilder<String>(TfsContext.getInstance().getCmsObject()).setSQLQuery(query).execute();
    }

    /**
     * Obtiene el �ltimo n�mero de paquete para insertar en la DB.
     */
    public static synchronized long obtainNextPacketId() {
        String query = "SELECT MAX(PACKET_ID) FROM TFS_SCHEDULED_PUBLISH_PACKET";

        Long lastPacketId = new QueryBuilder<Long>(TfsContext.getInstance().getCmsObject()).setSQLQuery(query)
                .execute(new ResultSetProcessor<Long>() {

                    private Long packetId;

                    public void processTuple(ResultSet rs) {

                        try {
                            packetId = rs.getLong("MAX(" + PACKET_ID + ")");
                        }
                        catch (Exception e) {
                            throw new ApplicationException(
                                    "No se pudo leer el ID del paquete de publicaci�n en la DB.", e);
                        }
                    }

                    public Long getResult() {
                        if (this.packetId == null)
                            return new Long(1);
                        return this.packetId;
                    }
                });

        return lastPacketId.longValue() + 1;
    }

}
