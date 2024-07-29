package com.tfsla.opencmsdev.module;

import org.opencms.file.CmsResource;
import org.opencms.main.CmsEvent;
import org.opencms.main.I_CmsEventListener;

//import com.tfsla.buscador.IndexManagerOld;
import com.tfsla.utils.CmsResourceUtils;

public class DeleteIndexEventListener implements I_CmsEventListener {

	public DeleteIndexEventListener() {
		super();
		// TODO Auto-generated constructor stub
	}

	public void cmsEvent(CmsEvent event) {
		for (Object resource : (Iterable) event.getData().get("resources")) {
			String url = CmsResourceUtils.getLink(((CmsResource) resource));
			if (!url.contains("~") && ((CmsResource) resource).getFlags() == 0 && ((CmsResource) resource).getTypeId() == TfsConstants.NOTICIA_TYPEID.intValue()) {
				//IndexManagerOld.getInstance().deleteIndex(((CmsResource) resource), TfsConstants.TITULO_UBICATION_KEY);
				//IndexManagerOld.getInstance().deleteIndex(((CmsResource) resource), TfsConstants.KEYWORD_UBICATION_KEY);
			}
		}

	}

}
