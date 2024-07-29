package com.tfsla.loader;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Iterator;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.flex.CmsFlexController;
import org.opencms.loader.I_CmsResourceLoader;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplaceManager;

import com.tfsla.diario.comentarios.model.Comment;
import com.tfsla.diario.comentarios.services.CommentsModule;

import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class TfsCommentLoader implements I_CmsResourceLoader {

	 /** The id of this loader. */
    public static final int RESOURCE_LOADER_ID = 1192;
    
    /** The param name of the comment id. */
	private final String PARAM_COMMENT_ID = "cId";
	
    /** The maximum age for dumped contents in the clients cache. */
    private static long m_clientCacheMaxAge;

    private String transformedXml = "";
    
    /** The resource loader configuration. */
    private Map<String, String> m_configuration;

    /**
     * The constructor of the class is empty and does nothing.<p>
     */
    public TfsCommentLoader() {
    m_configuration = new TreeMap<String, String>();

    }
    
    public void destroy() {
	}

	private String transformedXml(CmsObject cms,
	        CmsResource resource, String xml)
	{
		try {
			CmsProperty xsltProperty = cms.readPropertyObject(resource, getXsltPropertyDefinition(), true);
		
			if (xsltProperty==null || xsltProperty.getValue()==null)
				return xml;

			String xsltDefPath = xsltProperty.getValue();
			
            if (!cms.existsResource(xsltDefPath, CmsResourceFilter.IGNORE_EXPIRATION))
            	return xml;

            CmsFile xsltTemplate = cms.readFile(xsltDefPath, CmsResourceFilter.IGNORE_EXPIRATION);
            
			TransformerFactory tFactory = TransformerFactory.newInstance();
			InputStream is = new ByteArrayInputStream(xml.getBytes()); 
			InputStream isXslt = new ByteArrayInputStream(xsltTemplate.getContents()); 
			OutputStream os = new ByteArrayOutputStream();
			StreamSource source = new StreamSource(is);
			
			StreamSource sourceXslt = new StreamSource(isXslt);
			Transformer transformer = tFactory.newTransformer(sourceXslt);
			
			Result result = new StreamResult(os);
			transformer.transform(source, result);

			return os.toString();
			
		} catch (CmsException e) {
			e.printStackTrace();
			return xml;
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
			return xml;
		} catch (TransformerException e) {
			e.printStackTrace();
			return xml;
		}
	}
	
	private String getXmlComment(CmsObject cms, String cId)
	{
		Comment comment = CommentsModule.getInstance(cms).getComment(cms, cId);
			
		String texto = "<comment>";
		texto += "	<id>";
		texto += comment.getId();
		texto += "	</id>";
		texto += "	<noticia>";
		texto += comment.getNoticiaURL();
		texto += "	</noticia>";
		texto += "	<titulo>";
		texto += comment.getTituloNoticia(cms);
		texto += "	</titulo>";
		texto += "	<autor>";
		texto += comment.getUser();
		texto += "	</autor>";
		texto += "	<texto>";
		texto += comment.getText();
		texto += "	</texto>";
		texto += "	<reportes>";
		texto += comment.getCantReports();
		texto += "	</reportes>";
		texto += "	<fecha>";
		texto += comment.getDateAsString();
		texto += "	</fecha>";
		texto += "	<estado>";
		texto += comment.getState();
		texto += "	</estado>";
		texto += "</comment>";

		return texto;
	}
	
	public byte[] dump(CmsObject cms,
	        CmsResource resource,
	        String element,
	        Locale locale,
	        HttpServletRequest req,
	        HttpServletResponse res) throws IOException, CmsException {

		String cId = req.getParameter(PARAM_COMMENT_ID);
		if (cId==null)
			return new byte[0];
		
		String texto = getXmlComment(cms,cId);
		
		return texto.getBytes();
	
	}

	public byte[] export(CmsObject cms, CmsResource resource, HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException, CmsException {

		if ((req != null) && (res != null)) {
            for (Iterator i = OpenCms.getStaticExportManager().getExportHeaders().listIterator(); i.hasNext();) {
                String header = (String)i.next();

                // set header only if format is "key: value"
                String[] parts = CmsStringUtil.splitAsArray(header, ':');
                if (parts.length == 2) {
                    res.setHeader(parts[0], parts[1]);
                }
            }
            load(cms, resource, req, res);
		}
		return new byte[0];
		
	}


	public void load(CmsObject cms, CmsResource resource, HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException, CmsException {
        
        // set response status to "200 - OK" (required for static export "on-demand")
        res.setStatus(HttpServletResponse.SC_OK);
        // set content length header
        
        transformedXml = "";
		String cId = req.getParameter(PARAM_COMMENT_ID);
		if (cId!=null)
		{
			transformedXml = transformedXml(cms, resource, getXmlComment(cms, cId));
	
	        // set content length header
	        res.setContentLength(transformedXml.length());
			
		}
		else
	        // set content length header
	        res.setContentLength(transformedXml.length());
	
        if (CmsWorkplaceManager.isWorkplaceUser(req)) {
            // prevent caching for Workplace users
            res.setDateHeader(CmsRequestUtil.HEADER_LAST_MODIFIED, System.currentTimeMillis());
            CmsRequestUtil.setNoCacheHeaders(res);
        } else {
            // set date last modified header
            res.setDateHeader(CmsRequestUtil.HEADER_LAST_MODIFIED, resource.getDateLastModified());

            // set "Expires" only if cache control is not already set
            if (!res.containsHeader(CmsRequestUtil.HEADER_CACHE_CONTROL)) {
                long expireTime = resource.getDateExpired();
                if (expireTime == CmsResource.DATE_EXPIRED_DEFAULT) {
                    expireTime--;
                    // flex controller will automatically reduce this to a reasonable value
                }
                // now set "Expires" header        
                CmsFlexController.setDateExpiresHeader(res, expireTime, m_clientCacheMaxAge);
            }
        }

        service(cms, resource, req, res);
        

    }

    /**
     * @see org.opencms.loader.I_CmsResourceLoader#service(org.opencms.file.CmsObject, org.opencms.file.CmsResource, javax.servlet.ServletRequest, javax.servlet.ServletResponse)
     */
    public void service(CmsObject cms, CmsResource resource, ServletRequest req, ServletResponse res)
    throws CmsException, IOException {

    	res.getOutputStream().write(transformedXml.getBytes());
        //res.getOutputStream().write(cms.readFile(resource).getContents());
    }

	public void addConfigurationParameter(String paramName, String paramValue) {
		 m_configuration.put(paramName, paramValue);
		
	}

	public Map getConfiguration() {
		return m_configuration;
	}

	public void initConfiguration() {
		if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info("Iniciando TfsCommentLoader");
        }
		
	}
	
	protected String getXsltPropertyDefinition()
	{
		return "xslt";
	}

	public int getLoaderId() {
        return RESOURCE_LOADER_ID;
	}

	public String getResourceLoaderInfo() {
        return "Comentarios";
	}

	public boolean isStaticExportEnabled() {
		return false;
	}

	public boolean isStaticExportProcessable() {
		return false;
	}

	public boolean isUsableForTemplates() {
		return false;
	}

	public boolean isUsingUriWhenLoadingTemplate() {
		return false;
	}

   
  
}
