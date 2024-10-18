package org.opencms.jsp;


	import java.io.File;

import jakarta.servlet.ServletRequest;
	import jakarta.servlet.jsp.JspException;

	import org.apache.commons.logging.Log;
	import org.opencms.flex.CmsFlexController;
	import org.opencms.jsp.CmsJspTagLink;
	import org.opencms.jsp.Messages;
import org.opencms.loader.CmsImageLoader;
import org.opencms.main.CmsLog;
import org.opencms.cache.CmsVfsNameBasedDiskCache;
import org.opencms.file.CmsFile;
	import org.opencms.file.CmsObject;
	import org.opencms.main.OpenCms;
	import org.opencms.staticexport.CmsLinkManager;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsRequestUtil;
	import org.opencms.util.CmsStringUtil;

	/**
	 * Basicamente copiamos el codigo de CmsJspTagLink solo para el caso de exports,
	 * que los mismos tenga un folder con el modification date.
	 * 
	 * 
	 * @see CmsJspTagLink
	 * @author ctorres
	 * 
	 */
	public class TfsJspTagLink extends CmsJspTagLink {

		private static final Log LOG = CmsLog.getLog(TfsJspTagLink.class);

		private static final long serialVersionUID = 8624335784699825265L;

	    public static String linkTagAction(String target, ServletRequest req) {

	        CmsFlexController controller = CmsFlexController.getController(req);

	        String absUri = CmsLinkManager.getAbsoluteUri(target, controller.getCurrentRequest().getElementUri());
	        
	        String newlink = OpenCms.getLinkManager().substituteLinkForUnknownTarget(
	            controller.getCmsObject(),
	            absUri);
	        
			// Si lo tiene que exportar entonces ponerle como carpeta la fecha de ultima modificacion
			if (newlink.startsWith("/export/")) {
				long dateLastModified =
						getDateLastModified(newlink, absUri, req);
		        LOG.debug("DateLastModified: " + dateLastModified + " > " + newlink + " - " + absUri );
				if (dateLastModified > 0) {
					newlink = "/__export/" + dateLastModified
							+ newlink.substring("/export".length());
				}
			}

			
			return newlink;

	    }

		/**
		 * 
		 * 
		 * @see org.opencms.jsp.CmsJspTagLink#doEndTag()
		 */
		@Override
		public int doEndTag() throws JspException {
			ServletRequest req = pageContext.getRequest();

			// This will always be true if the page is called through OpenCms
			if (CmsFlexController.isCmsRequest(req)) {
				try {
					// Get link-string from the body and reset body
					String link = getBodyContent().getString();

					getBodyContent().clear();
					// Calculate the link substitution
					String newlink = linkTagAction(link, req);

					// Write the result back to the page
					getBodyContent().print(newlink);
					getBodyContent().writeOut(pageContext.getOut());

				} catch (Exception ex) {
					if (LOG.isErrorEnabled()) {
						LOG.error(
								Messages.get().getBundle()
										.key(Messages.ERR_PROCESS_TAG_1, "link"),
								ex);
					}
					throw new JspException(ex);
				}
			}
			return EVAL_PAGE;
		}

		@Override
		public void release() {
			super.release();
		}
		
		public static long getDateLastModified(String orig, String link,
				ServletRequest req) {

			CmsFlexController controller = CmsFlexController.getController(req);

			CmsObject cmsObject = controller.getCmsObject();

			if (CmsStringUtil.isEmpty(link)) {
				return -1;
			}
			String sitePath = link;
			String siteRoot = null;
			if (CmsLinkManager.hasScheme(link)) {
				// si es una url
				sitePath = OpenCms.getLinkManager().getRootPath(cmsObject, link);
				if (sitePath == null) {
					// es null porque era una url externa
					// salgo
					return -1;
				}
			}

			// aca tengo un sitePath quiero averiguar el siteRoot
			siteRoot = OpenCms.getSiteManager().getSiteRoot(sitePath);
			if (siteRoot == null) {
				// no tiene un siteRoot
				// pongo un siteRoot por defecto
				siteRoot = cmsObject.getRequestContext().getSiteRoot();
			} else {
				// como el sitePath incluye el siteRoot
				// se lo saco
				sitePath = sitePath.substring(siteRoot.length());
			}

			// si es una url le saco el query string
			sitePath = CmsRequestUtil.getRequestLink(sitePath);

			CmsFile readFile = null;

			try {
				readFile = cmsObject.readFile(sitePath);
				// tengo un archivo devuelvo el getDateLastModified
				return readFile.getDateLastModified();
			} catch (Exception ignore) {
			}
			return -1;
		}


	}
