package com.tfsla.opencmsdev.listActions;

import java.util.Locale;

import org.opencms.i18n.CmsMessageContainer;

/**
 * Muestra un mensaje no internacionalizado. Necesario cuando reutilizamos codigo y no podemos acoplarnos a
 * tener una clase Messages en el mismo package.
 * 
 * @author jpicasso
 */
@SuppressWarnings("serial")
public class FixedCmsMessageContainer extends CmsMessageContainer {

	private String fixedMessage;

	// ******************************
	// ** Constructor
	// ******************************
	public FixedCmsMessageContainer(String fixedMessage) {
		super(null, null);
		this.fixedMessage = fixedMessage;
	}

	// ******************************
	// ** Message container interface
	// ******************************
	@Override
	public String key(Locale locale) {
		return this.key();
	}

	@Override
	public String key() {
		return this.fixedMessage;
	}
}