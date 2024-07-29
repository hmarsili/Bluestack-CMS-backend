package com.tfsla.diario.friendlyTags;

import java.util.Random;

import javax.servlet.jsp.JspException;

import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsObject;
import org.opencms.flex.CmsFlexController;
import org.opencms.i18n.CmsEncoder;

public class TfsDirectEditManualButton extends A_TfsNoticiaValue{
	
	private static final long serialVersionUID = -1976111127225402334L;
	
	boolean isOffline = false;
	String    newlink = CmsEncoder.encode("Contenidos|/contenidos/noticia_0001.html|50");
	String   backlink;
	String  closelink;
	private CPMConfig config;
	
	
	private boolean    hasNew;
	private boolean  hasDelete;
	
	public TfsDirectEditManualButton(){
		   hasNew = false;
		hasDelete = false;
		config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		
	}
	
	@Override
    public int doStartTag() throws JspException {
		
		CmsObject cms = CmsFlexController.getCmsObject(pageContext.getRequest());
		
		I_TfsNoticia noticia = getCurrentNews();
		String UrlNoticia = cms.getSitePath(noticia.getXmlDocument().getFile());
		
		Random generator = new Random();
		int  rand_button = 0;
		
		String protocolNavigation = config.getParam("", "", "newsPublisher", "protocolNavigation");
		String absoluteUrl = protocolNavigation + "://" + pageContext.getRequest().getServerName();
		
		isOffline = !cms.getRequestContext().currentProject().isOnlineProject();
		
		if(isOffline){
			 backlink = cms.getRequestContext().getUri();
			 closelink = cms.getRequestContext().getUri();
			 
			 rand_button = generator.nextInt(1500) + 1; 
		     id = "ocms_" + rand_button;
		
		     String Buttons ="";
		     
		     Buttons += "<script type=\"text/javascript\">";
		     Buttons += "registerButtonOcms(\""+id+"\");";
		     Buttons += "</script>";
		     Buttons += "<div style=\"position: static; visibility: visible;\" class=\"ocms_de_bt\" id=\"buttons_"+id+"\">";
		     Buttons += "<form name=\"form_"+id+"\" id=\"form_"+id+"\" method=\"post\" action=\"/system/workplace/editors/editor.jsp\" class=\"ocms_nomargin\" target=\"_top\">";
		     Buttons += "<input name=\"backlink\" value=\""+backlink+"\" type=\"hidden\">";
		     Buttons += "<input name=\"closelink\" type=\"hidden\" value=\""+closelink+"\">";
		     Buttons += "<input name=\"resource\" value=\""+UrlNoticia+"\" type=\"hidden\">";
		     Buttons += "<input name=\"directedit\" value=\"true\" type=\"hidden\">";
		     Buttons += "<input name=\"elementlanguage\" value=\"en\" type=\"hidden\">";
		     Buttons += "<input name=\"elementname\" value=\"null\" type=\"hidden\">";
		     Buttons += "<input name=\"redirect\" value=\"true\" type=\"hidden\">";
		     Buttons += "<input name=\"editortitle\" type=\"hidden\">";
		     Buttons += "<input name=\"newlink\" type=\"hidden\">";
		     Buttons += "</form>";
		     
		     Buttons += "<form name=\"formEdit_"+id+"\" id=\"formEdit_"+id+"\" method=\"get\" action=\"" + absoluteUrl + "/system/modules/com.tfsla.diario.admin/templates/editNews.jsp\" class=\"ocms_nomargin\" target=\"_top\">";
		     Buttons += "<input name=\"url\" class=\"url_"+id+"\" value=\""+UrlNoticia+"\" type=\"hidden\">";
		     Buttons += "<input name=\"backlink\" class=\"backlink_"+id+"\" value=\""+backlink+"\" type=\"hidden\">";
		     Buttons += "</form>";
		     
		     Buttons += "<table onmouseover=\"activateOcms('"+id+"');\" onmouseout=\"deactivateOcms('"+id+"');\" id=\"table_"+id+"\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tbody><tr>";
		     Buttons += "<td class=\"ocms_de\"><a href=\"#\" onclick=\"javascript:document.getElementById('formEdit_"+id+"').submit();\" class=\"ocms_button\"><span class=\"ocms_over\" onmouseover=\"className='ocms_over'\" onmouseout=\"className='ocms_over'\" onmousedown=\"className='ocms_push'\" onmouseup=\"className='ocms_over'\"><span id=\"bt_"+id+"\" class=\"ocms_combobutton\" style=\"padding-left: 15px; padding-right: 1px; background-image: url(&quot;/resources/buttons/directedit_cl.png&quot;); background-position: 0px 0px;\" title=\"Direct Edit\">&nbsp;</span></span></a></td>";      
		
		     if(hasDelete){
		        Buttons += "<td class=\"ocms_de\"><a href=\"#\" onclick=\"javascript:submitOcms('"+id+"', 'delete');\" class=\"ocms_button\"><span class=\"ocms_over\" onmouseover=\"className='ocms_over'\" onmouseout=\"className='ocms_over'\" onmousedown=\"className='ocms_push'\" onmouseup=\"className='ocms_over'\"><img src=\"/resources/buttons/deletecontent.png\" title=\"Delete\" alt=\"\" border=\"0\"></span></a></td>";
		     }
		     
		     if(hasNew){
		      //  Buttons += "<td class=\"ocms_de\"><a href=\"#\" onclick=\"javascript:submitOcms('"+id+"', 'new', '"+newlink+"');\" class=\"ocms_button\"><span class=\"ocms_over\" onmouseover=\"className='ocms_over'\" onmouseout=\"className='ocms_over'\" onmousedown=\"className='ocms_push'\" onmouseup=\"className='ocms_over'\"><img src=\"/resources/buttons/new.png\" title=\"New\" alt=\"\" border=\"0\"></span></a></td>";
		    	 Buttons += "<td class=\"ocms_de\"><a href=\"#\" onclick=\"javascript:newArticleButton('"+id+"');\" class=\"ocms_button\"><span class=\"ocms_over\" onmouseover=\"className='ocms_over'\" onmouseout=\"className='ocms_over'\" onmousedown=\"className='ocms_push'\" onmouseup=\"className='ocms_over'\"><img src=\"/resources/buttons/new.png\" title=\"New\" alt=\"\" border=\"0\"></span></a></td>";
		     }
		     
		     Buttons += "</tr></tbody></table>";
		     Buttons += "</div>\n";
		     Buttons += "<div id=\""+id+"\" class=\"ocms_de_norm\"></div>";
		
		     printContent(Buttons);
	     
		}
		
		return SKIP_BODY;
	}
	
	public boolean getHasNew(){
		return hasNew;
	}
	
	public void setHasNew(boolean hasNew){
		this.hasNew = hasNew;
	}
	
	public boolean getHasDelete(){
		return hasDelete;
	}
	
	public void setHasDelete(boolean hasDelete){
		this.hasDelete = hasDelete;
	}

}
