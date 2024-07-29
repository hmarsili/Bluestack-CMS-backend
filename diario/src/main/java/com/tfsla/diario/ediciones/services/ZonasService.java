package com.tfsla.diario.ediciones.services;

import java.util.List;

import org.opencms.file.CmsObject;

import com.tfsla.diario.ediciones.data.ZoneDAO;
import com.tfsla.diario.ediciones.model.Seccion;
import com.tfsla.diario.ediciones.model.Zona;

public class ZonasService extends baseService {

	public void crearZona(Zona z,CmsObject obj) throws RuntimeException
	{
		ZoneDAO zDAO = new ZoneDAO();

		try {
			zDAO.insertZona(z);

			hasError = false;
		} catch (Exception e) {
			if (e.getMessage().contains("Duplicate entry"))
			{
				errorDescription = "ERR_DUPLICATE_ZONA_0";
				hasError = true;
			}
			else
				throw new RuntimeException(e);
		}
	}

	public void borrarZona(int zonaId,CmsObject obj)
	{
		ZoneDAO zDAO = new ZoneDAO();

		try {
			zDAO.deleteZona(zonaId);

			ZonasCache.getInstance().reset();
			hasError = false;

		} catch (Exception e) {
			errorDescription = e.getMessage();
			hasError = true;
		}
	}

	public void actualizarZona(Zona z)
	{
		ZoneDAO zDAO = new ZoneDAO();
		try {
			zDAO.updateZona(z);
			ZonasCache.getInstance().reset();
			hasError = false;
		} catch (Exception e) {
			errorDescription = e.getMessage();
			hasError = true;
		}
	}
	
	public void actualizarOrden(int zoneId, int zoneOrder)
	{
		ZoneDAO zDAO = new ZoneDAO();
		try {
			zDAO.actualizarOrder(zoneId, zoneOrder);
			ZonasCache.getInstance().reset();
			hasError = false;
		} catch (Exception e) {
			errorDescription = e.getMessage();
			hasError = true;
		}
	}

	public Zona obtenerZona(int zonaId)
	{
		Zona z = ZonasCache.getInstance().getZona(zonaId);
		if (z!=null)
			return z;
		
		ZoneDAO zDAO = new ZoneDAO();
		try {
			z = zDAO.getZona(zonaId);
			ZonasCache.getInstance().putZona(z);
			hasError = false;
		} catch (Exception e) {
			errorDescription = e.getMessage();
			hasError = true;
		}
		return z;
	}

	
	public List<Zona> obtenerZonas(int tipoEdicion, int pagina)
	{
		ZoneDAO zDAO = new ZoneDAO();
		List<Zona> zonas=null;
		try {
			zonas = zDAO.getZonas(tipoEdicion, pagina);
			hasError = false;
		} catch (Exception e) {
			errorDescription = e.getMessage();
			hasError = true;
		}
		return zonas;

	}
	
	public List<Zona> obtenerZonas(int tipoEdicion)
	{
		ZoneDAO zDAO = new ZoneDAO();
		List<Zona> zonas=null;
		try {
			zonas = zDAO.getZonas(tipoEdicion);
			hasError = false;
		} catch (Exception e) {
			errorDescription = e.getMessage();
			hasError = true;
		}
		return zonas;

	}
	
	public List<Zona> obtenerZonas(int tipoEdicion, int pagina, Boolean visible)
	{
		ZoneDAO zDAO = new ZoneDAO();
		List<Zona> zonas=null;
		try {
			zonas = zDAO.getZonas(tipoEdicion, pagina, visible);
			hasError = false;
		} catch (Exception e) {
			errorDescription = e.getMessage();
			hasError = true;
		}
		return zonas;

	}

	public List<Zona> obtenerZonas(int tipoEdicion, int pagina, String text)
	{
		ZoneDAO zDAO = new ZoneDAO();
		List<Zona> zonas=null;
		try {
			zonas = zDAO.getZonas(tipoEdicion, pagina, text);
			hasError = false;
		} catch (Exception e) {
			errorDescription = e.getMessage();
			hasError = true;
		}
		return zonas;

	}
	
	public List<Zona> obtenerZonas(int tipoEdicion,String text)
	{
		ZoneDAO zDAO = new ZoneDAO();
		List<Zona> zonas=null;
		try {
			zonas = zDAO.getZonas(tipoEdicion,text);
			hasError = false;
		} catch (Exception e) {
			errorDescription = e.getMessage();
			hasError = true;
		}
		return zonas;

	}
	
	public boolean esVisible (int zonaId)
	{
		Zona z = ZonasCache.getInstance().getZona(zonaId);
		if (z!=null) {
			return z.getVisibility();
		}
			
		ZoneDAO zDAO = new ZoneDAO();
		try {
			z = zDAO.getZona(zonaId);
			ZonasCache.getInstance().putZona(z);
			return z.getVisibility();
		} catch (Exception e) {
			errorDescription = e.getMessage();
			return false;
		}
	}
}
