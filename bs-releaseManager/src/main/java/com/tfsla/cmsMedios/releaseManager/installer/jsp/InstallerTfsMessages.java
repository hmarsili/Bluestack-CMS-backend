package com.tfsla.cmsMedios.releaseManager.installer.jsp;

import java.util.Hashtable;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;

import com.tfsla.diario.admin.jsp.TfsMessages;

public class InstallerTfsMessages extends TfsMessages {

	public InstallerTfsMessages(PageContext context, HttpServletRequest req, HttpServletResponse res) {
		super(context, req, res);
	}
	
	@Override
	public String key(String key) {
		if(_strings.containsKey(key)) {
			return _strings.get(key);
		}
		return key;
	}

	public static void addEntry(String key, String value) {
		if(_strings.containsKey(key)) {
			_strings.remove(key);
		}
		_strings.put(key, value);
	}
	
	@SuppressWarnings("serial")
	private static Hashtable<String, String> _strings = new Hashtable<String, String>() {{
		put("GUI_WAIT", "Espere...");
		put("GUI_NEXT", "Continuar");
		put("GUI_SUBMIT", "Enviar");
		put("GUI_INSTALL", "Instalar");
		put("GUI_CHECK_UPDATES", "Buscar actualizaciones");
		put("GUI_NEW_UPDATES", "Nuevas actualizaciones disponibles, seleccione la versión a instalar:");
		put("GUI_NO_UPDATES", "Ya se está ejecutando la versión mas reciente del sistema.");
		put("RM_UPGRADING_TO", "Actualización a %s");
		put("RM_INSTALL", "Instalar Release");
		put("RM_INFO_STEP_NO_DESCRIPTION", "No hay mas detalles para esta versión.");
		put("RM_INFO_STEP_TITLE", "Información de la instalación");
		put("RM_INFO_STEP_DESCRIPTION", "Se procederá a generar un backup de los archivos a modificar, despliegue de contenidos y código JSP, actualizaciones de JARS y ejecución de scripts de base de datos. Finalizada la instalación se configurará la nueva versión en caso de ser necesario.");
		put("RM_EMPTY_STEP", "No se requieren acciones adicionales para esta versión en este paso");
		put("RM_SQL_STEP_TITLE", "Scripts de base de datos (SQL)");
		put("RM_SQL_STEP_NOPARAMS_SCRIPT", "No hay parámetros para esta consulta");
		put("RM_SQL_STEP_DESCRIPTION", "En este paso se configurarán los parámetros para las consultas SQL requeridas para las nuevas funcionalidades del RM");
		put("RM_CONFIRM_STEP_TITLE", "Confirmar");
		put("RM_CONFIRM_STEP_DESCRIPTION", "Confirmar e iniciar la instalación del release");
		put("CHECK_MANIFEST", "revisar manifest");
		put("RM_PRE_UPGRADE_STEP_TITLE","Requerimientos para la instalación");
		put("RM_PRE_UPGRADE_STEP_DESCRIPTION","Antes de comenzar la instalación puede ser necesario hacer otras modificaciones de forma manual sobre recursos customizados.");
	}};
}
