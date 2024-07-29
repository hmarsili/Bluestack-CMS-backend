package com.tfsla.rankUsers.action;

import java.util.Locale;

import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.db.CmsDefaultUsers;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplaceAction;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;

import com.tfsla.diario.comentarios.model.Comment;
import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.event.I_TfsEventListener;
import com.tfsla.rankUsers.service.RankService;
import com.tfsla.utils.TfsAdminUserProvider;

public class CommentEvent implements I_TfsEventListener {

	public void cmsEvent(CmsEvent event) {
		if (event.getType()==I_TfsEventListener.EVENT_COMMENT_ACEPTED || event.getType()==I_TfsEventListener.EVENT_COMMENT_REJECTED) {
			Comment comment = (Comment) event.getData().get(I_TfsEventListener.KEY_COMMENT);
			
			CmsObject cms;
			try {
				CmsObject _cmsObject =CmsWorkplaceAction.getInstance().getCmsAdminObject();
				cms = OpenCms.initCmsObject(_cmsObject);
				
				cms.getRequestContext().setSiteRoot("/sites/" + comment.getSite());
			
				CmsFile file = cms.readFile(comment.getNoticiaURL());
	
				int tEd=0;
				String siteName = OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot();

				String proyecto = siteName.replaceFirst("/sites/", "");

				String urlNoPath = cms.getRequestContext().removeSiteRoot(file.getRootPath());
				TipoEdicionService tService = new TipoEdicionService();
				TipoEdicion tEdicion = tService.obtenerTipoEdicion(cms, urlNoPath);
				if (tEdicion==null)
					tEdicion = tService.obtenerEdicionOnline(proyecto);

				if (tEdicion!=null) {
					tEd = tEdicion.getId();
				}

				
				CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, file);
	
				RankService rService = new RankService();
				com.tfsla.rankViews.service.RankService rViewService = new com.tfsla.rankViews.service.RankService();

				if (event.getType()==I_TfsEventListener.EVENT_COMMENT_ACEPTED) {
					CmsUser cmsUser = getAutor(content,cms,siteName,tEd);
					if (cmsUser!=null)
						rService.addUserHit(cmsUser, cms, RankService.COUNTER_COMENTARIOSRECIBIDOS, 1);
					rService.addUserHit(cms.readUser(comment.getUser()), cms, RankService.COUNTER_COMENTARIOSREALIZADOS, 1);
					
					rViewService.addComentario(file, cms, null);
				}
				else {
					rService.addUserHit(cms.readUser(comment.getUser()), cms, RankService.COUNTER_COMENTARIOSRECHAZADOS, 1);					
				}
			} catch (CmsException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}
	
	public CmsUser getAutor(CmsXmlContent content, CmsObject cms, String siteName, int tEd) throws CmsException
	{
		String userAnon = getParam(siteName, ""+tEd,"anonymousUser");
		String _excludeAnonymousFromRankings = getParam(siteName, ""+tEd,"excludeAnonymousFromRankings");
		boolean excludeAnonymousFromRankings = _excludeAnonymousFromRankings !=null && (_excludeAnonymousFromRankings.trim().toLowerCase().equals("yes") || _excludeAnonymousFromRankings.trim().toLowerCase().equals("true"));

		CmsUser cmsUser = null;
		I_CmsXmlContentValue contentValue = content.getValue("autor/internalUser",Locale.ENGLISH);		
		if (contentValue!=null)
		{
			String autor = contentValue.getStringValue(cms);
			if (autor!=null && autor.trim().length()>0)
			{
				
				if (!excludeAnonymousFromRankings || (excludeAnonymousFromRankings && !autor.equals(userAnon))) {
					try {
						cmsUser = cms.readUser(autor);
					}
					catch (org.opencms.db.CmsDbEntryNotFoundException e){}
					if (cmsUser==null)
					{
						try {
							cmsUser = cms.readUser(new CmsUUID(autor));
						}
						catch (org.opencms.db.CmsDbEntryNotFoundException e){}
						catch (NumberFormatException e){}
	
					}
				}
				return cmsUser;
			}
		}
		
		
		String indexingMode = getParam(siteName, ""+tEd,"indexingModeOnEmpty");
		//empty | anonymousUser | newsCreator
		if (!excludeAnonymousFromRankings && indexingMode.equals("anonymousUser")) {
			try {
				cmsUser = cms.readUser(userAnon);
			}
			catch (org.opencms.db.CmsDbEntryNotFoundException e){}
		} 
		else if (indexingMode.equals("newsCreator")) {
			try {
				cmsUser = cms.readUser(content.getFile().getUserCreated());
			}
			catch (org.opencms.db.CmsDbEntryNotFoundException e){}
		}

		return cmsUser;
		
	}

	private String getParam(String siteName, String publicationName, String paramName)
	{
    	String module = "newsAuthor";
 		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
 		
		return config.getParam(siteName, publicationName, module, paramName, "userCreated");

	}


}
