package com.tfsla.opencmsdev.module;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsEvent;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.TfsContext;

import com.tfsla.utils.CmsResourceUtils;

/**
 * Listener para cuando se cierra el editor de un contenido
 * @author lgassman
 *
 */
public abstract class SaveInEditorEventListener implements I_CmsEventListener{

	public SaveInEditorEventListener() {
		super();
	}

	public void cmsEvent(CmsEvent event) {

		for (Object resource : (Iterable) event.getData().get("resources")) {
			String url = CmsResourceUtils.getLink(((CmsResource) resource));
			if (url.contains("~")) {
				url = url.replace("~", "");
				this.doExecute(url, TfsContext.getInstance().getCmsObject());
			}
		}
	}

	protected abstract void doExecute(String url, CmsObject cmsObject);

}
