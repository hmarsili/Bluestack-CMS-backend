package com.tfsla.diario.workplace.galleries;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypePointer;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.loader.CmsLoaderException;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.galleries.A_CmsAjaxGallery;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

public class TfsAjaxVideoGallery extends A_CmsAjaxGallery {

    /** Type name of the link gallery. */
    public static final String GALLERYTYPE_NAME = "videogallery";

    /** The uri suffix for the gallery start page. */
    public static final String OPEN_URI_SUFFIX = GALLERYTYPE_NAME + "/index.jsp";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(TfsAjaxVideoGallery.class);

    /** The resource type id of this gallery instance. */
    private int m_galleryTypeId;

    /**
     * Public empty constructor, required for {@link A_CmsAjaxGallery#createInstance(String, CmsJspActionElement)}.<p>
     */
    public TfsAjaxVideoGallery() {

        // noop
    }

    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public TfsAjaxVideoGallery(CmsJspActionElement jsp) {

        super(jsp);

    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public TfsAjaxVideoGallery(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));

    }

    /**
     * @see org.opencms.workplace.galleries.A_CmsAjaxGallery#getGalleryItemsTypeId()
     */
    @Override
    public int getGalleryItemsTypeId() {

        return CmsResourceTypePointer.getStaticTypeId();
    }

    /**
     * @see org.opencms.workplace.galleries.A_CmsAjaxGallery#getGalleryTypeId()
     */
    @Override
    public int getGalleryTypeId() {

        try {
            this.m_galleryTypeId = OpenCms.getResourceManager().getResourceType(GALLERYTYPE_NAME).getTypeId();
        } catch (CmsLoaderException e) {
            // resource type not found, log error
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        return this.m_galleryTypeId;
    }

    /**
     * @see org.opencms.workplace.galleries.A_CmsAjaxGallery#getGalleryTypeName()
     */
    @Override
    public String getGalleryTypeName() {

        return GALLERYTYPE_NAME;
    }

    /**
     * Fills the JSON object with the specific information used for pointer file resource type.<p>
     * 
     * <ul>
     * <li><code>pointer</code>: the content of the pointer resource. This could be an external or internal link.</li>
     * </ul>
     * 
     * @see org.opencms.workplace.galleries.A_CmsAjaxGallery#buildJsonItemSpecificPart(JSONObject jsonObj, CmsResource res, String sitePath)
     *
     */
    @Override
    protected void buildJsonItemSpecificPart(JSONObject jsonObj, CmsResource res, String sitePath) {

        // file target
        String pointer;
        try {
            pointer = new String(getCms().readFile(res).getContents());
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(pointer)) {
                pointer = getJsp().link(getCms().getSitePath(res));
            }
            jsonObj.append("pointer", pointer);
        } catch (CmsException e) {
            // reading the resource or property value failed
            LOG.error(e.getLocalizedMessage(), e);
        } catch (JSONException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }

    }

    /**
     * Writes the current link into the pointer resource. <p>
     * 
     * @see org.opencms.workplace.galleries.CmsAjaxLinkGallery#changeItemLinkUrl(String)
     * 
     * @param itemUrl the pointer resource to change the link of 
     * 
     */
    @Override
    protected void changeItemLinkUrl(String itemUrl) {

        try {
            JspWriter out = getJsp().getJspContext().getOut();
            if (getCms().existsResource(itemUrl)) {
                try {
                    writePointerLink(getCms().readResource(itemUrl));
                    out.print(buildJsonItemObject(getCms().readResource(itemUrl)));
                } catch (CmsException e) {
                    // can not happen in theory, because we used existsResource() before...
                }
            } else {
                out.print(RETURNVALUE_NONE);
            }
        } catch (IOException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }

    }

    /**
     * Writes the current link into the pointer resource. <p>
     * 
     * @param res the pointer resource to change the link of 
     * 
     * @throws CmsException if sth. goes wrong
     */
    private void writePointerLink(CmsResource res) throws CmsException {

        String resPath = getCms().getSitePath(res);
        String currentPropertyValue = getParamPropertyValue();
        boolean locked = true;
        CmsLock lock = getCms().getLock(res);
        if (lock.isUnlocked()) {
            // lock resource before operation
            getCms().lockResource(resPath);
            locked = false;
        }
        CmsFile file = getCms().readFile(res);
        file.setContents(currentPropertyValue.getBytes());
        checkLock(getCms().getSitePath(res));
        getCms().writeFile(file);
        if (!locked) {
            // unlock the resource
            getCms().unlockResource(resPath);
        }
    }
}
