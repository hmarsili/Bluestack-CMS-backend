package com.tfsla.opencmsdev;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.opencms.file.CmsObject;
import org.opencms.lock.CmsLock;
import org.opencms.lock.CmsLockType;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.main.TfsContext;
import org.opencms.report.CmsHtmlReport;
import org.opencms.scheduler.I_CmsScheduledJob;

import com.tfsla.exceptions.ApplicationException;
import com.tfsla.opencmsdev.module.ScheduledPublishPacket;
import com.tfsla.utils.TfsDAO;

public class ScheduledPublishJob implements I_CmsScheduledJob {

    @SuppressWarnings("unchecked")
    public String launch(CmsObject cms, Map attributes) throws Exception {
        TfsContext.getInstance().setCmsObject(cms);
        try {
        StringBuffer sb = new StringBuffer();
        sb.append("-- Comienzo del proceso de publicaci�n diferida --\n\n");
        Set<Entry> entrySet = attributes.entrySet();
        for (Entry entry : entrySet) {
            sb.append("-- Comienzo del proceso de publicaci�n para el sitio " + entry.getKey() + " --\n\n");
            String projectIdString = (String) entry.getValue();
            int projectId = new Integer(projectIdString).intValue();
            publishResourcesBySite(cms, sb, projectId);
            sb.append("-- Fin del proceso de publicaci�n para el sitio " + entry.getKey() + " --\n\n");
        }

        sb.append("-- Fin del proceso de publicaci�n diferida --\n\n");
        return sb.toString();
        }
        finally {
            TfsContext.getInstance().removeCmsObject();        	
        }
    }

    private void publishResourcesBySite(CmsObject cms, StringBuffer sb, int projectId) {
        List<ScheduledPublishPacket> packets = TfsDAO.obtainReadyToPublishPackets(cms, projectId);

        for (ScheduledPublishPacket packet : packets) {
            try {
                sb.append("Comienzo de la publicaci�n del paquete nro " + packet.getPacketId() + "\n");
                this.publishPacket(packet, cms);
                TfsDAO.markPublishedPacket(cms, packet.getPacketId());
                sb.append("La publicaci�n del paquete nro " + packet.getPacketId() + " fue realizada con �xito\n");
            }
            catch (ApplicationException exception) {
                sb.append("Han ocurrido problemas al intentar publicar el paquete nro " + packet.getPacketId()
                        + "\n");
                sb.append("Es posible que algunos recursos se hayan publicado y otros no.\n");
                sb
                        .append("El paquete volver� a intentar publicarse a la pr�xima ejecuci�n del proceso de publicaci�n diferida, por favor revise los recursos indicados.");
                sb.append("Error: " + exception.getMessage() + "\n");
                sb.append("Caused by: " + exception.getCause() + "\n" + exception.getCause().getMessage());
            }
        }
    }

    private void publishPacket(ScheduledPublishPacket packet, CmsObject cms) {
        for (String resourceName : packet) {
            try {
                this.publishResource(resourceName, cms);
            }
            catch (Exception exception) {
            }
        }
    }

    private synchronized void publishResource(String resource, CmsObject cms) {
        try {
            CmsLock lock = cms.getLock(resource);
            try {
                if (lock.isNullLock() || (lock.getType() == CmsLockType.UNLOCKED)) {
                    cms.lockResource(resource);
                }

            }catch (Exception e){
                // do nothing
            }
            cms.unlockResource(resource);
            OpenCms.getPublishManager().publishResource(cms,resource, true, new CmsHtmlReport(new Locale("es", "AR"), "/"));
        }
        catch (Exception ex) {
            CmsLog.getLog(this)
                    .error("-- Ocurrio un error al intentar publicar el recurso " + resource + " --\n\n");
            throw new ApplicationException("No se pudo publicar el recurso " + resource, ex);
        }
    }

}
