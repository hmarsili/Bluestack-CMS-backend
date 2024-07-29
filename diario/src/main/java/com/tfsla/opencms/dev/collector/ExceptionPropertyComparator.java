package com.tfsla.opencms.dev.collector;

import java.util.List;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;

import com.tfsla.collections.CollectionFactory;
import com.tfsla.exceptions.ApplicationException;


public class ExceptionPropertyComparator extends PropertyComparator {

	private List<String> minValues = CollectionFactory.createList();
	private List<String> maxValues = CollectionFactory.createList();
	
	public ExceptionPropertyComparator(CmsObject cmsObject, String propertyName, boolean asc) {
		super(cmsObject, propertyName, asc);
	}
	
	public ExceptionPropertyComparator addMinValue(String propertyValue) {
		ApplicationException.assertFalse(this.maxValues.contains(propertyValue), "Ya estaba registrado el valor como maximo:" + propertyValue);
		this.minValues.add(propertyValue.toLowerCase());
		return this;
	}

	public ExceptionPropertyComparator addMaxValue(String propertyValue) {
		ApplicationException.assertFalse(this.minValues.contains(propertyValue), "Ya estaba registrado el valor como minimo:" + propertyValue);
		this.maxValues.add(propertyValue.toLowerCase());
		return this;
	}

	@Override
	public int compare(CmsResource obj1, CmsResource obj2) {
		String value1 = this.readPropertyAsLowerCase(obj1);
		String value2 = this.readPropertyAsLowerCase(obj2);
		
		// ambos son maximos
		if((this.maxValues.contains(value1) && this.maxValues.contains(value2)) ) {
			return new Integer(this.maxValues.indexOf(value1)).compareTo(this.maxValues.indexOf(value2)) * this.getAscendentCoeficiet();
		}
		
        //ambos son minimos
        if( (this.minValues.contains(value1) && this.minValues.contains(value2))) {
            return new Integer(this.minValues.indexOf(value2)).compareTo(this.minValues.indexOf(value1)) * this.getAscendentCoeficiet();            
        }
        
		// El primero es maximo o el segundo es minimo
		if(this.maxValues.contains(value1) || this.minValues.contains(value2)) {
			return -1;
		}
		
		// El segundo es maximo o el primero es minimo
		if(this.maxValues.contains(value2) || this.minValues.contains(value1)) {
			return 1;
		}
		
		// no son casos excepcionales
		return value1.compareTo(value2) * this.getAscendentCoeficiet();
	}
}
