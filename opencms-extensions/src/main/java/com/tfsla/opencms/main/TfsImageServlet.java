package com.tfsla.opencms.main;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.*;  
import java.util.Date;
import java.awt.*;  
import java.awt.image.*;  

import javax.imageio.*;  

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.opencms.configuration.CmsMediosInit;
import org.opencms.db.CmsDefaultUsers;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.workplace.CmsWorkplaceAction;

import com.octo.captcha.service.CaptchaServiceException;
import com.tfsla.capcha.CaptchaManager;
import com.tfsla.utils.TfsAdminUserProvider;


public class TfsImageServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2213221577836801013L;
	

	private static final Log LOG = CmsLog.getLog(TfsImageServlet.class);
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
	{
		String extraId = request.getParameter("key");
		
		String currentSiteRoot = null;
		String currentPublication = null;
		String currentURI = null;
		
		String path = request.getRequestURI();
		
		currentSiteRoot = path.substring(path.indexOf("/sites/"));
		String[] parts = currentSiteRoot.split("/");
		currentSiteRoot = "/" + parts[1] + "/" + parts[2];
		currentPublication = parts[3];

		currentURI = parts[4];
		byte[] valueDecoded= Base64.decodeBase64(currentURI.getBytes() );
		currentURI = new String(valueDecoded);
		
		if (extraId==null) {

			try {
				
				CmsObject cmsObject =CmsWorkplaceAction.getInstance().getCmsAdminObject();
				cmsObject = OpenCms.initCmsObject(cmsObject);
				
				cmsObject.getRequestContext().setSiteRoot(currentSiteRoot);

				CmsResource res = cmsObject.readResource(currentURI);
				
				CmsMediosInit.getInstance().addHit(res, cmsObject, request.getSession());
				
			} catch (CmsException e1) {
				e1.printStackTrace();
				//CmsMediosInit.getInstance().addHit(currentSiteRoot);

			}

			
			
			
			response.setHeader("Cache-Control", "no-store");
			response.setHeader("Pragma", "no-cache");
			response.setDateHeader("Expires", 0);
			response.setContentType("image/jpeg");

			BufferedImage bufferedImage = new BufferedImage(1, 1,   
					BufferedImage.TYPE_INT_RGB);  

			Graphics g = bufferedImage.getGraphics();  
			g.setColor(Color.WHITE);  
			g.fillRect(0, 0, 1,1);  

			g.dispose();  
			try {
				ImageIO.write(bufferedImage, "jpg", response.getOutputStream());
			} catch (IOException e) {
				LOG.error("IOException Exception while creating the image",e);
			}  
		}
		else {
			byte[] captchaChallengeAsJpeg = null;

			ByteArrayOutputStream jpegOutputStream = new ByteArrayOutputStream();

			try
			{

				String captchaId = request.getSession().getId() + extraId;

				BufferedImage challenge = CaptchaManager.getInstance(currentSiteRoot,currentPublication).getImageChallengeForID(captchaId);

				ImageIO.write(challenge, "jpg", jpegOutputStream);
			}
			catch (IllegalArgumentException e)
			{
				LOG.error("Illegal Argument Exception while creating the captcha image",e);
				return;
			}
			catch (CaptchaServiceException e)
			{
				LOG.error("Captcha Service Exception while creating the captcha image",e);
				return;
			}
			catch (Exception e)
			{
				LOG.error("Exception while creating the captcha image",e);
				return;
			}

			captchaChallengeAsJpeg = jpegOutputStream.toByteArray();
			
			
			response.setHeader("Cache-Control", "no-store");
			response.setHeader("Pragma", "no-cache");
			response.setDateHeader("Expires", 0);
			response.setContentType("image/jpeg");
			ServletOutputStream responseOutputStream;
			try {
				responseOutputStream = response.getOutputStream();
				responseOutputStream.write(captchaChallengeAsJpeg);
				responseOutputStream.flush();
				responseOutputStream.close();
			} catch (IOException e) {
				LOG.error("IOException while creating the captcha image",e);
				return;
			}
		}

	}
	
	
	
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		
	}
}
