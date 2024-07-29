package com.tfsla.opencms.dev.collector;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.opencms.file.CmsObject;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

//import ar.edu.utn.frba.tadp.bttf.instant.DiscreteInstant;
//import ar.edu.utn.frba.tadp.bttf.instant.Month;

import com.tfsla.exceptions.ApplicationException;
import com.tfsla.utils.CmsResourceUtils;

/**
 * Este objeto arma las carpetas que corresponden a hoy y ayer Es usado por TodayAndYesterdayCollector y sus
 * subclases
 * 
 * @author lgassman
 */
public class DateFolder {


	private String folder;
	// REVISARME parsear en dos atributos si es necesario
	private String fileAndType;

	private boolean createFolder;

	private int folderType = CmsResourceTypeFolder.RESOURCE_TYPE_ID;

	public DateFolder(String param, boolean createFolder, int folderType) {
	
		this(param,createFolder);
		this.folderType = folderType;
		
	}
	/**
	 * Constructor con un String que es el atributo param del tag contentload
	 * 
	 * @param param example:"rootFolder/childFolder/filename|type"
	 */
	public DateFolder(String param, boolean createFolder) {
		int index = param.lastIndexOf('/');
		this.folder = param.substring(0, index);
		this.fileAndType = param.substring(index);
		this.createFolder = createFolder;
	}

	/**
	 * Agrega la carpeta correspondiente a ayer en la carpeta con que se construy� el objeto
	 * 
	 * @param cms el objeto que puede crear cosas en el vfs
	 * @return el nombre de la carpeta
	 */
	public String getYesterdayFolder(CmsObject cms) {
		return getDateFolder(cms, yesterday());
	}

	private Date yesterday() {
		// un dia en milisegundos: 1000 * 60 * 60 * 24
		long oneDay = 86400000;
		long today = System.currentTimeMillis();
		return new Date(today - oneDay);
	}

	/**
	 * Agrega la carpeta correspondiente a hoy en la carpeta con que se construy� el objeto
	 * 
	 * @param cms el objeto que puede crear cosas en el vfs
	 * @return el nombre de la carpeta
	 */
	public String getTodayFolder(CmsObject cms) {
		return getDateFolder(cms, new Date());
	}

	/**
	 * Si no existe la carpeta y no es online, la crea. Si no existe y es online, devuelve null.
	 * 
	 * @param cms objeto para crear la carpeta
	 * @param date
	 * @return (param == blah/fulanito|15 && date == 2005-12-1) ? "blah/2005/12/1/fulanito|15"
	 */
	private String getDateFolder(CmsObject cms, Date date) {
		String year = new SimpleDateFormat("yyyy").format(date);
		String month = new SimpleDateFormat("MM").format(date);
		String day = new SimpleDateFormat("dd").format(date);
		
		String folderName = this.folder + "/" + year;
		
		String publishLevel = "";
		if (!cms.existsResource(folderName) && !cms.getRequestContext().currentProject().isOnlineProject() && this.createFolder) {
			publishLevel = folderName;
		}
		
		boolean existFolder = makeFolderUnpublished(cms, folderName);
		//Si no pudo crear la del a�o, no tiene sentido que siga creando la del mes
		if(!existFolder) {
			return null;
		}
		
		folderName = folderName + "/" + month;

		if (publishLevel.equals("") && !cms.existsResource(folderName) && !cms.getRequestContext().currentProject().isOnlineProject() && this.createFolder) {
			publishLevel = folderName;
		}

		existFolder = makeFolderUnpublished(cms, folderName);
		//Si no pudo crear la del mes, no tiene sentido que siga creando la del dia
		if(!existFolder) {
			return null;
		}

		folderName = folderName + "/" + day;

		if (publishLevel.equals("") && !cms.existsResource(folderName) && !cms.getRequestContext().currentProject().isOnlineProject() && this.createFolder) {
			publishLevel = folderName;
		}

		existFolder = makeFolderUnpublished(cms, folderName);
		
		if (!publishLevel.equals(""))
		{
			try {
				OpenCms.getPublishManager().publishResource(cms,publishLevel);
			}
			catch (Exception e) {
				//Si no pudo publicarlarla, puede ser que este usuario no tenga permisos de publicacion, y entonces, no se tiene que romper
				//queda la carpeta sin publicarla.
				CmsLog.getLog(this).error("Error al publicar la carpeta " + publishLevel, e);
			}

		}
		
		return existFolder ? folderName + this.fileAndType : null;
	}

	
	private boolean makeFolderUnpublished(CmsObject cms, String folderName) {

		boolean out;
		if (!cms.existsResource(folderName)) {
			if (!cms.getRequestContext().currentProject().isOnlineProject() && this.createFolder) {
				try {
					cms.createResource(folderName, folderType);
					CmsResourceUtils.unlockResource(cms, folderName, false);
					out = true;
				}
				catch (Exception e) {
					throw new ApplicationException("making folder " + folderName, e);
				}

			}
			else {
				out = false;
			}
		}
		else {
			out = true;
		}

		return out;
	}

	/**
	 * Crea la carpeta en el vfs si se puede
	 * 
	 * @param cms
	 * @param folderName
	 * @return si la carpeta existe;
	 */
	private boolean makeFolder(CmsObject cms, String folderName) {

		boolean out;
		if (!cms.existsResource(folderName)) {
			if (!cms.getRequestContext().currentProject().isOnlineProject() && this.createFolder) {
				try {
					cms.createResource(folderName, folderType);
					CmsResourceUtils.unlockResource(cms, folderName, false);
					out = true;
				}
				catch (Exception e) {
					throw new ApplicationException("making folder " + folderName, e);
				}

				try {
					OpenCms.getPublishManager().publishResource(cms,folderName);
				}
				catch (Exception e) {
					//Si no pudo publicarlarla, puede ser que este usuario no tenga permisos de publicacion, y entonces, no se tiene que romper
					//queda la carpeta sin publicarla.
					CmsLog.getLog(this).error("Error al publicar la carpeta " + folderName, e);
				}

			}
			else {
				out = false;
			}
		}
		else {
			out = true;
		}

		return out;
	}
/*
	public String getDateFolder(CmsObject object, DiscreteInstant instant) {
		return getDateFolder(object, instant.toDate());
	}
*/
	/**
	 * REVISARME:Este codigo es demasiado parecido al que crea para un dia solo getDateFolder
	 * 
	 * 
	 * @param cms
	 * @param instant
	 * @return
	 */
/*
	public String getMonthFolder(CmsObject cms, Month instant) {
		String year = new SimpleDateFormat("yyyy").format(instant.toDate());
		String month = new SimpleDateFormat("MM").format(instant.toDate());
		
		String folderName = this.folder + "/" + year;
		boolean existFolder = makeFolder(cms, folderName);
		//Si no pudo crear la del a�o, no tiene sentido que siga creando la del mes
		if(!existFolder) {
			return null;
		}
		
		folderName = folderName + "/" + month;
		existFolder = makeFolder(cms, folderName);

		return existFolder ? folderName + this.fileAndType : null;
		
	}
*/
}