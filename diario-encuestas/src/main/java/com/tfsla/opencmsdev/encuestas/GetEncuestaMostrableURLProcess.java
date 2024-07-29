package com.tfsla.opencmsdev.encuestas;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;

import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.TipoEdicionService;

public class GetEncuestaMostrableURLProcess extends ConfigurableTableProcess {

	/**
	 * Comparator para ordenar las encuestas por fecha de cierre.
	 * 
	 * @author jpicasso
	 */
	private final class EncuestasFechaCierreComparator implements Comparator {
		public int compare(Object o1, Object o2) {
			EncuestaBean enc1 = (EncuestaBean) o1;
			EncuestaBean enc2 = (EncuestaBean) o2;

			// confio en que fechaCierre no es null, porque esta en estado cerrada.
			return enc2.getFechaCierre().compareTo(enc1.getFechaCierre());
		}
	}

	private Comparator encuestasFechaCierreComparator = new EncuestasFechaCierreComparator();
	
	/**
	 * Comparator para ordenar las encuestas activas por fecha de creacion.
	 * 
	 */
	private final class EncuestasFechaCreacionComparator implements Comparator {
		public int compare(Object o1, Object o2) {
			EncuestaBean enc1 = (EncuestaBean) o1;
			EncuestaBean enc2 = (EncuestaBean) o2;

			return enc2.getFechaCreacion().compareTo(enc1.getFechaCreacion());
		}
	}

	private Comparator encuestasFechaCreacionComparator = new EncuestasFechaCreacionComparator();

	// ***************************
	// ** Metodo execute()
	// ***************************
	public EncuestaBean execute(CmsObject cms, String grupo, String orden) {
		try {
			
			TipoEdicionService tService = new TipoEdicionService();
			TipoEdicion tEdicion = tService.obtenerTipoEdicion(cms,cms.getRequestContext().getUri());
			
		
			
			List activasResources = cms.readResourcesWithProperty(ModuloEncuestas.getEncuestaPath(cms, tEdicion.getId()),
				Encuesta.ESTADO_GRUPO_PROPERTY, Encuesta.getEstadoYGrupoIdentifier(Encuesta.ACTIVA, grupo));
			if (!activasResources.isEmpty()) {
				
				//CmsResource resource = (CmsResource) activasResources.get(0);

				//return new EncuestaBean(ModuloEncuestas.getCompletePath(resource.getName()), Encuesta.ACTIVA);
				
				Set ActivasOrdenadas = new TreeSet(this.encuestasFechaCreacionComparator);
				
				for (Iterator it = activasResources.iterator(); it.hasNext();) {
					CmsResource resource = (CmsResource) it.next();
					CmsProperty propertyCierre = cms.readPropertyObject(resource, "fechaCierre", false);
					String fechaCierre = propertyCierre.getValue();
					
					try {
						Encuesta encuesta = Encuesta.getEncuestaFromURL(cms, cms.getRequestContext().removeSiteRoot(resource.getRootPath()));
						String fechaCreacion = encuesta.getFechaCreacion();
						
						ActivasOrdenadas.add(new EncuestaBean(cms.getRequestContext().removeSiteRoot(resource.getRootPath()),
							Encuesta.ACTIVA, fechaCierre, fechaCreacion));
						
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
				
				if (orden.equals("asc")){
					
					EncuestaBean resource = null;
					for (Iterator it = ActivasOrdenadas.iterator(); it.hasNext();) {
					      resource =  (EncuestaBean) it.next();
					}
					
					return (EncuestaBean) resource;
				}else{
					 // la primera es la activa mas recientemente, por el comparator.
					return (EncuestaBean) ActivasOrdenadas.iterator().next();
				}
				
			}
			else {
				// no hay una activa, buscamos la cerrada mas recientemente

				// traigo las cerradas del grupo...
				List cerradasResources = cms.readResourcesWithProperty(ModuloEncuestas.getEncuestaPath(cms, tEdicion.getId()),
					Encuesta.ESTADO_GRUPO_PROPERTY, Encuesta.getEstadoYGrupoIdentifier(Encuesta.CERRADA, grupo));

				if (!cerradasResources.isEmpty()) {

					Set cerradasOrdenadas = new TreeSet(this.encuestasFechaCierreComparator);

					for (Iterator it = cerradasResources.iterator(); it.hasNext();) {
						CmsResource resource = (CmsResource) it.next();
						CmsProperty property = cms.readPropertyObject(resource, "fechaCierre", false);
						String fechaCierre = property.getValue();
						cerradasOrdenadas.add(new EncuestaBean(cms.getRequestContext().removeSiteRoot(resource.getRootPath()),
							Encuesta.CERRADA, fechaCierre));
					}

					// la primera es la cerrada mas recientemente, por el comparator.
					return (EncuestaBean) cerradasOrdenadas.iterator().next();
				}
			}

			// no habia ni una activa ni una cerrada
			return null;
		}
		catch (CmsException e) {
			throw new RuntimeException("Error al buscar la encuesta mostrable");
		} catch (Exception e) {
			throw new RuntimeException("Error al buscar la encuesta mostrable");
		}
	}
}