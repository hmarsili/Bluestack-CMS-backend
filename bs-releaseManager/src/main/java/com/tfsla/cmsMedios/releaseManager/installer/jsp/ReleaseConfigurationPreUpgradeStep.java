package com.tfsla.cmsMedios.releaseManager.installer.jsp;

import java.io.IOException;

import com.tfsla.cmsMedios.releaseManager.installer.common.AmazonConfiguration;
import com.tfsla.cmsMedios.releaseManager.installer.common.ReleaseManagerConfiguration;
import com.tfsla.cmsMedios.releaseManager.installer.common.exceptions.ReleaseNameNotFoundException;
import com.tfsla.cmsMedios.releaseManager.installer.service.ReleaseRetriever;
import com.tfsla.diario.admin.jsp.TfsMessages;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class ReleaseConfigurationPreUpgradeStep extends ReleaseConfigurationStep {
	
	AmazonConfiguration amzConfiguration = null;
	String rmName = "";
	ReleaseManagerConfiguration rfsConfig = null;
	
	public ReleaseConfigurationPreUpgradeStep(AmazonConfiguration amzConfig,ReleaseManagerConfiguration rfsConfig,String rm,JSONObject manifest, TfsMessages messages) {
		super(manifest, messages);
		this.amzConfiguration = amzConfig;
		this.rmName = rm;
		this.rfsConfig = rfsConfig; 
	}

	@Override
	public int getOrder() {
		return 0;
	}
	
	@Override
	public String getHtmlAction() {
		
		htmlAction = "<hr/><div class='rm-readme-step'>";
			htmlAction += "<h3>"+messages.key("RM_PRE_UPGRADE_STEP_TITLE")+"</h3>";	
			
			htmlAction += "<div class='rm-readme-step-0' >";	
			
				htmlAction += "<input type='hidden' name='readmeOK' id='readmeOK' value='0'>";	
				
				htmlAction += "<div class='rm-requirements-description'>";	
					htmlAction += "<p>"+messages.key("RM_PRE_UPGRADE_STEP_DESCRIPTION")+"</p>";	
				htmlAction += "</div>";	
				
				htmlAction += getHtmlReadme();
				htmlAction += getHtmlAttachedFiles();
				htmlAction += "<br/>";
				htmlAction += "<input class='btn-next-0' type='button' value='"+messages.key("GUI_NEXT")+"'/>";
			
			htmlAction += "</div>";	
		
		htmlAction += "</div>";
		
		htmlAction += getHtmlProtectedFiles();
		
		return htmlAction;
	}
	
	private String getHtmlProtectedFiles(){
		
		 String htmlFiles = "";
		 
		 if(manifest.containsKey("protectedFiles")) {
				
			    htmlFiles += "<hr/><div class='rm-protected-files-step'>";
			    htmlFiles += "<h3>Archivos customizados del vfs</h3>";
			 	
			    htmlFiles += "<div class='rm-readme-step-1'>";
			    
				htmlFiles += "<div class='rm-requirements-description'>";
				htmlFiles += "En este rm se modificaron los siguientes archivos del vfs, seleccionar los archivos que desea proteger:";
				htmlFiles += "</div>";
				htmlFiles += "<div class='release-requeriments-step info-step'>";
				htmlFiles += "<br/><br/>";
				
				htmlFiles += "<form id='protectedFileFrm' '>";
				
				JSONArray protectedFiles = manifest.getJSONArray("protectedFiles");
				
				htmlFiles += "<input type='hidden' name='protectedFiles' id='protectedFiles' value='"+protectedFiles.size()+"'>";	
				 	
				int size = 0;
				
				for(Object protectedFile : protectedFiles) {
					String protectedFileStr = protectedFile.toString();
					
					if(!protectedFileStr.trim().equals("")){
						htmlFiles += "<input type='checkbox' name='protectedFileName' value='"+protectedFile+"'> "+protectedFile+"<br>";
						size++;
					}
				}
				
				htmlFiles += "<br/><br/>";
				htmlFiles += "<input class='btn-next-1'  type='button' value='Continuar' onclick='protectFilesVFS();'>";
				htmlFiles += "</form>";
				htmlFiles += "</div>";
				htmlFiles += "</div>";
				htmlFiles += "</div>";
				
				if(size==0)
					 htmlFiles = "";
		 }
		 
		 return htmlFiles;
	}
	
private String getHtmlAttachedFiles(){
		
		String htmlFiles = "";
		
		if(manifest.containsKey("attachedFiles")) {
			
			htmlFiles += "<div class='release-requeriments-step info-step'>";
			htmlFiles += "<br/><br/><strong>Archivos necesarios para este rm:</strong><br/>";
			
			JSONArray attachedFiles = manifest.getJSONArray("attachedFiles");
			
			int size = 0;
			
			for(Object attachedFile : attachedFiles) {
				 
				String errorMessage = "";
				String fileRFSPath = "";
				
				String fileName = attachedFile.toString();
				
				if(!fileName.trim().equals("")){
				
					try {
						fileRFSPath = ReleaseRetriever.getPublicURLFile(this.amzConfiguration, this.rmName,fileName);
						size++;
					} catch (IOException e) {
						errorMessage = "No se pudo obtener el archivo "+fileName+". Error:"+e.getMessage();
					} catch (ReleaseNameNotFoundException e) {
						errorMessage = "No se pudo obtener el archivo "+fileName+". Error:"+e.getMessage();
					}
					
					htmlFiles += "<span class='readme-file'>";
					
					if(!errorMessage.equals("")){
						htmlFiles += errorMessage;
					}else{
						htmlFiles += "<a href='"+fileRFSPath+"' download>"+fileName+"</a>";
					}
				}
				
				htmlFiles += "</span>";
				
			}
			
			htmlFiles += "</div>";
			
			if(size==0)
				htmlFiles = "";
		}
		
		return htmlFiles;
	}
	
	private String getHtmlReadme(){
		
		String readmeS3Path = null;
		String errorMessage = "";
		
		String htmlReadme = "";
		
		try {
			readmeS3Path = ReleaseRetriever.getPublicURLFile(this.amzConfiguration, this.rmName,"readme.txt");
		
		} catch (IOException e) {
			errorMessage = "No se pudo obtener el archivo readme.txt. Error: "+e.getMessage();
		} catch (ReleaseNameNotFoundException e) {
			errorMessage = "No se pudo obtener el archivo readme.txt. Error: "+e.getMessage();
		}
		
		htmlReadme = "<div class='release-requeriments-step info-step'>";
		
		htmlReadme += "<span class='readme-file'>";
		
		if(!errorMessage.equals("")){
			htmlReadme += errorMessage;
		}else{
			htmlReadme += "<a id='GetReadmeFile' data-url='"+readmeS3Path+"' data-filename='"+this.rmName+"_readme.txt' >INSTRUCCIONES DE DEPLOY</a>";
		}
		
		htmlReadme += "</span>";
		htmlReadme += "</div>";
		
		return htmlReadme;
	}
}
