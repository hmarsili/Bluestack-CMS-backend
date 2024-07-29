package com.tfsla.opencms.dev.collector;

import java.util.Collection;

import org.apache.commons.collections.Predicate;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;

/**
 * Se fija si la propiedad asociada a un Resource corresponde con el Value asume que la porperty existe
 * 
 * @author lgassman
 * 
 */
public class PropertyPredicate {

	public static Predicate makeSingleValuePropertyPredicate(CmsObject object, String property, String value) {
		return new PropertyPredicate().new SingleValuePropertyPredicate(object, property, value);
	}

	public static Predicate makeMultipleValuesPropertyPredicate(CmsObject object, String property,
			Collection<String> values) {
		return new PropertyPredicate().new MultipleValuesPropertyPredicate(object, property, values);
	}

	private class SingleValuePropertyPredicate implements Predicate {

		private CmsObject cms;

		private String property;

		private String value;

		public SingleValuePropertyPredicate(CmsObject object, String property, String value) {
			this.cms = object;
			this.property = property;
			this.value = value;
		}

		public boolean evaluate(Object arg0) {
			try {
				CmsProperty property = this.cms.readPropertyObject(((CmsResource) arg0), this.property, false);
				return this.value.equals(property.getValue());
			} catch (CmsException e) {
				throw new RuntimeException("No se pudo obtener la propiedad " + this.property, e);
			}
		}
	}

	private class MultipleValuesPropertyPredicate implements Predicate {
		private CmsObject cms;

		private String property;

		private Collection<String> values;

		public MultipleValuesPropertyPredicate(CmsObject object, String property, Collection<String> values) {
			this.cms = object;
			this.property = property;
			this.values = values;
		}

		public boolean evaluate(Object arg0) {
			try {
				CmsProperty property = this.cms.readPropertyObject(((CmsResource) arg0), this.property, false);
				return this.values.contains(property.getValue());
			} catch (CmsException e) {
				throw new RuntimeException("No se pudo obtener la propiedad collector.zone ", e);
			}
		}
	}

}
