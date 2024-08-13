package org.opencms.loader;

import org.opencms.configuration.CmsMedios;
import org.opencms.configuration.CmsMediosInit;
import org.opencms.file.CmsObject;
import org.opencms.flex.CmsFlexController;
import org.opencms.jsp.util.CmsJspLinkMacroResolver;
import org.opencms.jsp.util.TfsJspLinkMacroResolver;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Random;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;

import com.octo.captcha.service.image.ImageCaptchaService;
import com.tfsla.data.baseDAO;
import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.TipoEdicionBaseService;

public class TfsJspLoader extends CmsJspLoader implements I_CmsResourceLoader, I_CmsFlexCacheEnabledLoader, I_CmsEventListener {

/*
	private static String errorMsg = "hTe3GIq5GxbIz1X5LDKv143owhIi873jejaGuU3fdJwN4HWUiShongy00qunm8ZdWAtP3OzcqFM60xxboyTfFZ5Os9i6aFrqYwvagQDhveTt4wWmCRzAS4UxX0V+3pWbxgr7+rnFvFnBXychgbEqqLwT1IsvkylGkiG0WR1cc1U=";

	static {

		if (CmsMedios.getInstance()==null)
			throw new RuntimeException(errorMsg);
		
		
		int sizeConn =0;
		InputStream in = baseDAO.class.getResourceAsStream("/org/opencms/configuration/CmsMediosInit.class");
		
		try {
			byte[] chrs = IOUtils.toByteArray(in);

			sizeConn = chrs.length;
					
		} catch (IOException e) {
			throw new RuntimeException(CmsMediosInit.getInstance().decode(errorMsg));
		}
		finally
		{
			try {
				if (in != null)
					in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			in = null;

		}
		if (sizeConn!=28048)
			throw new RuntimeException(CmsMediosInit.getInstance().decode(errorMsg));
	}
*/
	
    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(TfsJspLoader.class);

	@Override
    protected byte[] parseJsp(
        byte[] byteContent,
        String encoding,
        CmsFlexController controller,
        Set updatedFiles,
        boolean isHardInclude) throws ServletException {

        String content;
        // make sure encoding is set correctly
        try {
            content = new String(byteContent, encoding);
        } catch (UnsupportedEncodingException e) {
            // encoding property is not set correctly 
            LOG.error(Messages.get().getBundle().key(
                Messages.LOG_UNSUPPORTED_ENC_1,
                controller.getCurrentRequest().getElementUri()), e);
            try {
                encoding = OpenCms.getSystemInfo().getDefaultEncoding();
                content = new String(byteContent, encoding);
            } catch (UnsupportedEncodingException e2) {
                // should not happen since default encoding is always a valid encoding (checked during system startup)
                content = new String(byteContent);
            }
        }

        // parse for special %(link:...) macros
        content = parseJspLinkMacros(content, controller);
        // parse for special <%@cms file="..." %> tag
        content = parseJspCmsTag(content, controller, updatedFiles);
        // parse for included files in tags
        content = parseJspIncludes(content, controller, updatedFiles);
        // parse for <%@page pageEncoding="..." %> tag
        content = parseJspEncoding(content, encoding, isHardInclude);
        // convert the result to bytes and return it
        content = parseTfsCounter(content, controller);
        
        try {
            return content.getBytes(encoding);
        } catch (UnsupportedEncodingException e) {
            // should not happen since encoding was already checked
            return content.getBytes();
        }
    }

    protected String parseTfsCounter(String content, CmsFlexController controller) throws ServletException {    	
    	CmsObject cms = controller.getCmsObject();

    	if (controller.isTop() && cms.getRequestContext().currentProject().isOnlineProject() &&  !OpenCms.getSiteManager().getCurrentSite(cms).getUrl().equals("/") && !cms.getRequestContext().getUri().startsWith("/system/")) {

 /*
    		boolean restrictive = CmsMediosInit.getInstance().restrictiveMode(cms);
    		long views = CmsMediosInit.getInstance().getViews(cms);
    		long maxViews = CmsMediosInit.getInstance().getPermViews(cms);

    		Random rnd = new Random();
    		int rNro = rnd.nextInt(10000);
    		int license = rNro>=1000 ? 0 : CmsMediosInit.getInstance().checkLicense(cms);
    		
    		if (license!=0 || restrictive && maxViews<views || views==-1L)
    		{
    			//controller.getTopResponse().setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    			content = CmsMediosInit.getInstance().decode("fXHCU4QLe0+WU81yrxo3+m7XcpbMY3MqQsHTYy3kM854BmtvtH48ps4eLCk/dSqcv5KzuaRfgDU+PATM/ufcXb/GuxdrqrVsotilsx7U9fK4KpLnd1mdqhOUfeurUceu9vv7+b6HDSK1bxISNpYpcTzC0q4UAnGMIiq3lm/FNX0=");
    			
    			throw new ServletException(content);

    		}
    		else {	    		
 */
	    		String cont = content.toLowerCase();
		    	String aux = "<img src=\"<%=com.tfsla.capcha.CaptchaManager.getPathCaptchaImage(pageContext, request, response)%>\" />";
		    	if (cont.contains("</body>") && !content.contains(aux))
		    	{
		    		int idx = cont.indexOf("</body>");
		    		content = content.substring(0,idx) + aux + content.substring(idx);
		    	}
//    		}
    	}
    	return content;
    }
    @Override
    protected String parseJspLinkMacros(String content, CmsFlexController controller) {

        TfsJspLinkMacroResolver macroResolver = new TfsJspLinkMacroResolver(controller.getCmsObject(), null, true);
        return macroResolver.resolveMacros(content);
    }
}
