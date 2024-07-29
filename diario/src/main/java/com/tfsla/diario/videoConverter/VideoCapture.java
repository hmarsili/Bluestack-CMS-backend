package com.tfsla.diario.videoConverter;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.opencms.main.CmsLog;


public class VideoCapture {
	
	private FFMPEGLocator locator;
	private 	   String ss = "00:00:02";
	private        String source;
	private        String frameRate = "1";
	private        String target;
	private        String dirTarget = "";
	private 	   String status = "";
	private 	   String msgStatus = "";
	private        String interval = "1";
	private 	   String FileNamePattern = "";
	private 	   String[] formats = {"120","320","480","640","1280"};
	
	public VideoCapture() {
		this.locator = new DefaultFFMPEGLocator();
	}
	
	public VideoCapture(FFMPEGLocator locator) {
		this.locator = locator;
	}
	
	public void setInitPosition(String expression){
		this.ss = expression;
	}
	
	private String getInitPosition(){
		return this.ss;
	}
	
	public void setSource(String source){
		this.source = source;
	}
	
	private String getSource(){
		return this.source;
	}
	
	public void setInterval(String interval){
		this.interval = interval;
	}
	
	private String getInterval(){
		return this.interval;
	}
	
	public void setDirTarget(String  dirTarget){
		this.dirTarget = dirTarget;
	}
	
	private String getDirTarget(){
		return this.dirTarget;
	}
	
	public void setFrameRate(String frameRate){
		this.frameRate = frameRate;
	}
	
	private String getFrameRate(){
		return this.frameRate;
	}
	
	private String getFileNamePattern(){
		return this.FileNamePattern;
	}
	
	
	
	public String getTarget(){

		String[] sourceArr = this.source.split("/");
		int size = sourceArr.length;
		
		String FileName = sourceArr[size-1];
		FileName = FileName.replace(".", "-");
		
		Date date = new Date();
		Long now = date.getTime();
		
		this.FileNamePattern = FileName+"_"+now;
		
		FileName = FileName+"_"+now+"%d.jpg";
		
		this.target = getDirTarget()+FileName;

		return this.target;
	}
	
	public String getTarget(String imageSize){

		String[] sourceArr = this.source.split("/");
		int size = sourceArr.length;
		
		String FileName = sourceArr[size-1];
		FileName = FileName.replace(".", "-");
		
		Date date = new Date();
		Long now = date.getTime();

		if (this.FileNamePattern.equals("")) {
			this.FileNamePattern = FileName+"_"+now;
			FileName = FileName+"_"+now + imageSize+".jpg";
		} else
			FileName = this.FileNamePattern + imageSize+".jpg";
		this.target = getDirTarget()+FileName;

		return this.target;
	}
	
	private void setMsgStatus(String msg ){
		this.msgStatus = msg;
	}
	
	public String getMsgStatus(){
		return this.msgStatus;
	}
	
	private void setStatus( String msg){
        this.status = msg;		
	}
	
	public String getStatus(){
		return this.status;
	}
	
	public List<String> GetListImg(){
		
		List<String> images = new ArrayList<String>();
		
		int size = formats.length;
		
		for(int i=0;i<size; i++){
			images.add(getFileNamePattern()+formats[i]+".jpg");
		}
		
		return images;
	}
		
	
	public void executeVideoCapture(){
		
		for (int i=0; i< formats.length; i++) {
			
			FFMPEGExecutor ffmpeg = locator.createExecutor();
			
			//-ss 00:10:00 indica que queremos comenzar a capturar en el minuto 10 del vídeo
			ffmpeg.addArgument("-ss");
			ffmpeg.addArgument(getInitPosition());
			
			// -i archivo de origen
			ffmpeg.addArgument("-i");
			ffmpeg.addArgument(getSource());
			
			ffmpeg.addArgument("-vf");
			ffmpeg.addArgument("scale="+formats[i]+":-1");
			//ffmpeg.addArgument("scale=640:480,pad=iw:ih:(ow-iw)/2:(oh-ih)/2");
		
			//para capturar un solo elemento
			ffmpeg.addArgument("-vframes");
			ffmpeg.addArgument("1");
			
			ffmpeg.addArgument("-v");
			ffmpeg.addArgument("error");
			
			// -r 1 fuerza un frame rate de 1 frame por segundo 
			//(dado que sólo queremos una imagen; si no utilizáramos esta opción y el vídeo estuviera a 25 fps, por ejemplo, obtendríamos 25 imágenes)
			/*ffmpeg.addArgument("-r");
			ffmpeg.addArgument(getFrameRate());*
			
			//-t 1 es el número de segundos a capturar 
			/*ffmpeg.addArgument("-t");
			ffmpeg.addArgument(getInterval());*/
			
			ffmpeg.addArgument(getTarget(formats[i]));
			
			String state = "OK";
			
			try {
				ffmpeg.execute();
				
			} catch (IOException e) {
				setStatus("Error");
				setMsgStatus("Exception ["+getSource()+"]: "+e.getMessage());
				CmsLog.getLog(this).error("Error en la captura de imagenes de un video ["+getSource()+"] con ffmpeg: "+e.getMessage());
			} 
			
			try {
				String lastWarning = null;
				
				RBufferedReader reader = null;
				reader = new RBufferedReader(new InputStreamReader(ffmpeg
						.getErrorStream()));
				
				int step = 0;
				String line;
				String msg = "";
				
				while ((line = reader.readLine()) != null) {
					msg += line.trim()+"<br>";
					
					if (line.startsWith("WARNING: ")) {
						lastWarning = line.trim();
					}
				}
				
				if(lastWarning!=null) state = "OK with Warnings";
				
				setStatus(state);
				setMsgStatus(msg);
				
			}catch (IOException e) {
					setStatus("Error");
					setMsgStatus("Exception: "+e.getMessage());
					CmsLog.getLog(this).error("Error en la captura de imagenes de un video ["+getSource()+"] con ffmpeg: "+e.getMessage());
			} finally {
				ffmpeg.destroy();
			}
		}
		return;
	}


}
