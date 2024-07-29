package com.tfsla.genericImport.transformation;

import javax.swing.ImageIcon;

import org.apache.commons.logging.Log;
import org.opencms.main.CmsLog;

import com.tfsla.genericImport.exception.DataTransformartionException;

public class ImageSizeTransformation  extends A_dataTransformation implements I_dataTransformation  {

    protected static final Log LOG = CmsLog.getLog(FindReplaceDataTransformation.class);

	public ImageSizeTransformation() {
		name = "imageSize";
		
		parameters.add("campos a aplicar separado por comas (vacio aplica a todos)");

	}

	@Override
	public String getNiceDescription() {
		return "Obtener dimensiones de imagen";
	}

	@Override
	public String getTransformationDescription(String transformation) {
		String descrip = "Obtener dimensiones de imagen";
		
		String campos = getParameter(transformation,1);
		if (campos.equals(""))
			descrip += " (todos los campos)"; 
		else
			descrip += " ( campos " + campos + ")";
		
		return descrip;

	}

	@Override
	public String getHelpText() {
		return "Obtener dimensiones de imagen";
	}

	@Override
	public Object[] execute(String transformation, Object[] value)
			throws DataTransformartionException {
		
		if (value==null)
			return null;

		String campos = getParameter(transformation,1);
		
		setCamposToProcess(campos);

		
		Object[] results= new Object[value.length];
			
		int campIdx = 1;
		for (Object val : value)
		{
			if (processCampo(campIdx)) {
				if (val!=null) {
					
					if (!(val instanceof String))
						throw new DataTransformartionException("El elemento a transformar no es de tipo texto");

					ImageIcon icono = new ImageIcon(val.toString());
					java.awt.Image imagen = icono.getImage();

					results[campIdx-1] = "w:"+imagen.getWidth(null)+",h:"+imagen.getHeight(null);
			         
				}
				else {
					results[campIdx-1] = null;
				}
			}
			else 
				results[campIdx-1] = val;
			campIdx++;

		}

		return results;
	}

}
