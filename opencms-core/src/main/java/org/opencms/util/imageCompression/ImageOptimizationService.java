package org.opencms.util.imageCompression;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.opencms.main.CmsLog;

public class ImageOptimizationService {
	
    
	protected static final Log LOG = CmsLog.getLog(ImageOptimizationService.class);

	protected Boolean pngquantEnabled = true;
	protected Boolean jpgoptimEnabled = true;

	JpegoptimBashProcess jpegoptim	= new JpegoptimBashProcess();
	PngquantBashProcess	pngquant = new PngquantBashProcess(); 
	
	public static Boolean getPngquant_enabled() {
		return ImageOptimizationService.getInstance().pngquantEnabled;
	}

	public static void setPngquant_enabled(Boolean pngquant_enabled) {
		LOG.info("Definiendo pngquand como " + (pngquant_enabled ? "habilitado" : "no habilitado"));
		ImageOptimizationService.getInstance().pngquantEnabled = pngquant_enabled;
	}

	public static Boolean getJpgoptim_enabled() {
		return ImageOptimizationService.getInstance().jpgoptimEnabled;
	}

	public static void setJpgoptim_enabled(Boolean jpgoptim_enabled) {
		LOG.info("Definiendo jpgoptim como " + (jpgoptim_enabled ? "habilitado" : "no habilitado"));
		ImageOptimizationService.getInstance().jpgoptimEnabled = jpgoptim_enabled;
	}

	public static int getPngquantQuality() {
		return ImageOptimizationService.getInstance().pngquant.getQuality();
	}

	public static void setPngquantQuality(int quality) {
		LOG.info("Definiendo calidad de pngquant en " + quality);
		ImageOptimizationService.getInstance().pngquant.setQuality(quality);
	}

	public static int getJpegoptimQuality() {
		return ImageOptimizationService.getInstance().jpegoptim.getQuality();
	}

	public static void setJpegoptimQuality(int quality) {
		LOG.info("Definiendo calidad de jpegoptim en " + quality);
		ImageOptimizationService.getInstance().jpegoptim.setQuality(quality);
	}

	private static ImageOptimizationService instance = new ImageOptimizationService();
	
	public static ImageOptimizationService getInstance() {
		return instance;
	}
	
	public synchronized byte[] process(String extension, String rootPath, byte[] image) {
		LOG.debug("Intentando compactar la imagen " + rootPath);
		
		if (this.jpgoptimEnabled && jpegoptim.useProcessor(extension)) {
			LOG.debug("Se encontro el conversor '" + jpegoptim.getClass().getName() + "' para la imagen " + rootPath);
			try {
				int result = jpegoptim.execute(image);
				if (result==0)
					return jpegoptim.getResult();
				else {
					LOG.error("Error tratando de compactar la imagen " + rootPath + ". (" + result + ")" + jpegoptim.getErrorMsg());
					return image;
				}
			} catch (IOException e) {
				LOG.error("Error tratando de compactar la imagen " + rootPath,e);
			}
		}
		if (this.pngquantEnabled && pngquant.useProcessor(extension)) {
			LOG.debug("Se encontro el conversor '" + pngquant.getClass().getName() + "' para la imagen " + rootPath);
			try {
				int result = pngquant.execute(image);
				if (result==0)
					return pngquant.getResult();
				else {
					LOG.error("Error tratando de compactar la imagen " + rootPath + ". (" + result + ")" + pngquant.getErrorMsg());
					return image;
				}
			} catch (IOException e) {
				LOG.error("Error tratando de compactar la imagen " + rootPath,e);
			}
		}
			
		return image;
	}
}
