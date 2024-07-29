package com.tfsla.diario.ediciones.services;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import com.tfsla.diario.ediciones.model.ImageInformation;

public class ImageOrientationFixer {
	
	@SuppressWarnings("null")
	public static synchronized InputStream transformImage(InputStream inputStream) throws Exception {
		InputStream stream = null;
		InputStream originalStream = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		try {
			byte[] buffer = new byte[1024];
			int len;
			while ((len = inputStream.read(buffer)) > -1 ) {
			    baos.write(buffer, 0, len);
			}
			baos.flush();
			stream = new ByteArrayInputStream(baos.toByteArray());
			originalStream = new ByteArrayInputStream(baos.toByteArray());
		} catch(Exception e) {
			if(inputStream.markSupported()) {
				if(stream != null) stream.close();
				if(originalStream != null) originalStream.close();
				inputStream.reset();
				return inputStream;
			} else {
				originalStream.reset();
				if(stream != null) stream.close();
				inputStream.close();
				return originalStream;
			}
		}
		
		inputStream.close();
		try {
			ImageMetadataPropertiesService service = new ImageMetadataPropertiesService();
			ImageInformation info = service.readImageInformation(stream);
			if(info.orientation == 1) {
				if(stream != null) stream.close();
				return originalStream;
			}
			
			//service.readImageInformation(stream) changes stream offset, needs to reset it
			stream.reset();
			BufferedImage image = getBufferedImage(stream);
			AffineTransform transform = getExifTransformation(info);
		    AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BICUBIC);
	
		    BufferedImage destinationImage = op.createCompatibleDestImage(image,  (image.getType() == BufferedImage.TYPE_BYTE_GRAY)? image.getColorModel() : null );
		    Graphics2D g = destinationImage.createGraphics();
		    g.setBackground(Color.WHITE);
		    g.clearRect(0, 0, destinationImage.getWidth(), destinationImage.getHeight());
		    destinationImage = op.filter(image, destinationImage);
		    
		    //Need to do that, Image Encoding not supported in OpenJDK.
		    //Otherwise will throw exception ImageIO.write Invalid argument to native writeImage
		    //see http://blog.sixthpoint.com/bufferedimage-jpeg-transparency-using-openjdk/
		    BufferedImage convertedImg = new BufferedImage(destinationImage.getWidth(), destinationImage.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
		    convertedImg.getGraphics().drawImage(destinationImage, 0, 0, null);
		    convertedImg.getGraphics().dispose();
		    
	    	ByteArrayOutputStream os = new ByteArrayOutputStream();
		    ImageIO.write(convertedImg, "jpeg", os);
		    InputStream is = new ByteArrayInputStream(os.toByteArray());
		    return is;
	    } catch(Exception e) {
	    	return originalStream;
	    } finally {
	    	if(stream != null) stream.close();
	    }
	}
	
	public static synchronized String transformImage(String file) throws Exception {
		ImageMetadataPropertiesService service = new ImageMetadataPropertiesService();
		ImageInformation info = service.readImageInformation(file);
		if(info.orientation == 1) return file;
		
		BufferedImage image = getBufferedImage(file);
		AffineTransform transform = getExifTransformation(info);
	    AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BICUBIC);

	    BufferedImage destinationImage = op.createCompatibleDestImage(image,  (image.getType() == BufferedImage.TYPE_BYTE_GRAY)? image.getColorModel() : null );
	    Graphics2D g = destinationImage.createGraphics();
	    g.setBackground(Color.WHITE);
	    g.clearRect(0, 0, destinationImage.getWidth(), destinationImage.getHeight());
	    destinationImage = op.filter(image, destinationImage);
	    
	    String fileName = file.replace(".jpeg", "_oriented.jpeg");
	    File destinationFile = new File(fileName);
	    ImageIO.write(destinationImage, "jpeg", destinationFile);
	    return fileName;
	}
	
	public static synchronized BufferedImage getBufferedImage(String file) throws IOException {
		Image image = ImageIO.read(new File(file));
		return (BufferedImage) image;
	}
	
	public static synchronized BufferedImage getBufferedImage(InputStream stream) throws IOException {
		Image image = ImageIO.read(stream);
		return (BufferedImage) image;
	}
	
	// Look at http://chunter.tistory.com/143 for information
	public static synchronized AffineTransform getExifTransformation(ImageInformation info) {

	    AffineTransform t = new AffineTransform();

	    switch (info.orientation) {
	    case 1:
	        break;
	    case 2: // Flip X
	        t.scale(-1.0, 1.0);
	        t.translate(-info.width, 0);
	        break;
	    case 3: // PI rotation 
	        t.translate(info.width, info.height);
	        t.rotate(Math.PI);
	        break;
	    case 4: // Flip Y
	        t.scale(1.0, -1.0);
	        t.translate(0, -info.height);
	        break;
	    case 5: // - PI/2 and Flip X
	        t.rotate(-Math.PI / 2);
	        t.scale(-1.0, 1.0);
	        break;
	    case 6: // -PI/2 and -width
	        t.translate(info.height, 0);
	        t.rotate(Math.PI / 2);
	        break;
	    case 7: // PI/2 and Flip
	        t.scale(-1.0, 1.0);
	        t.translate(-info.height, 0);
	        t.translate(0, info.width);
	        t.rotate(  3 * Math.PI / 2);
	        break;
	    case 8: // PI / 2
	        t.translate(0, info.width);
	        t.rotate(  3 * Math.PI / 2);
	        break;
	    }

	    return t;
	}
}
