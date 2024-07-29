package com.tfsla.genericImport.transformation;

import org.apache.commons.logging.Log;
import org.opencms.main.CmsLog;

import com.tfsla.genericImport.exception.DataTransformartionException;

public class JoinDataTransformation  extends A_dataTransformation implements I_dataTransformation  {

    protected static final Log LOG = CmsLog.getLog(FindReplaceDataTransformation.class);

	public JoinDataTransformation() {
		name = "Joindata";
		
		parameters.add("separator");
		parameters.add("campos a aplicar separado por comas (vacio aplica a todos)");

	}

	@Override
	public String getNiceDescription() {
		return "Juntar valores";
	}

	@Override
	public String getTransformationDescription(String transformation) {
		String descrip = "Juntar valores con separador " + getParameter(transformation,1);
		
		String campos = getParameter(transformation,2);
		if (campos.equals(""))
			descrip += " (todos los campos)"; 
		else
			descrip += " ( campos " + campos + ")";
		
		return descrip;

	}

	@Override
	public String getHelpText() {
		return "Juntar los valores con el separador indicado";
	}

	@Override
	public Object[] execute(String transformation, Object[] value)
			throws DataTransformartionException {
		
		if (value==null)
			return null;

		String separator = getParameter(transformation,1);
		String campos = getParameter(transformation,2);
		
		setCamposToProcess(campos);

		
		String valueJoined="";
		Object[] results;
		if (isProcessAll())
			results = new Object[1];
		else 
			results = new Object[value.length - getCamposToProcessCount() + 1];
			
		int campIdx = 1;
		for (Object val : value)
		{
			if (processCampo(campIdx)) {
				if (val!=null)
					valueJoined+=val+separator;												
			}
			else {
				results[campIdx-1] = val;
				campIdx++;
			}
		}

		if (!valueJoined.equals("")) {
			int idx = valueJoined.lastIndexOf(separator);
			if (idx>0)
				valueJoined=valueJoined.substring(0,idx);

			results[results.length-1]=valueJoined;
		}
		else
			results[results.length-1]=null;
		return new Object[] {valueJoined};
	}


}
