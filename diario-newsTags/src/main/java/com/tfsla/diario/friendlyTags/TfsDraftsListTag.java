package com.tfsla.diario.friendlyTags;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.jsp.JspException;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.flex.CmsFlexController;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;

import com.tfsla.diario.ediciones.services.PublicationService;
import com.tfsla.diario.model.TfsLista;
import com.tfsla.webusersposts.common.XmlUserPost;
import com.tfsla.webusersposts.service.PostsService;

public class TfsDraftsListTag extends A_TfsUsuarioValueTag {
	
	@Override
	public int doStartTag() throws JspException {
		
		savePosts();
		
		CmsFlexController controller = CmsFlexController.getController(pageContext.getRequest());
	 	CmsObject cms = controller.getCmsObject();
		
	 	if(this.publication == 0) {
			try {
				publication = PublicationService.getPublicationId(cms);
			} catch (Exception e) {
				e.printStackTrace();
			}
	 	}
	 	
	 	if(this.username != null && !this.username.trim().equals("")) {
			CmsUser user = null;
			try {
				user = cms.readUser(username);
				this.userId = user.getId().toString();
			} catch (CmsException e) {
				e.printStackTrace();
			}
		}
		if(this.userId == null || this.userId.trim().equals("")) {
			CmsUser user= getCurrentUser().getUser();
			this.userId = user.getId().toString();
		}
		if(this.site == null || this.site.trim().equals("")) {
			this.site = cms.getRequestContext().getSiteRoot();
		}
		
		PostsService postsService = new PostsService();
		try {
			this.posts = postsService.getXmlUserPosts(
				cms, 
				this.userId, 
				this.site, 
				String.valueOf(publication), 
				this.countPosts, 
				this.fromPost, 
				this.status
			);
		} catch (Exception e) {
			this.posts = new ArrayList<XmlUserPost>();
			e.printStackTrace();
		}
		
		return (hasMoreContent() ? EVAL_BODY_INCLUDE : SKIP_BODY );
	}
	
	@Override
	public int doAfterBody() throws JspException {
		if (hasMoreContent()) {
			return EVAL_BODY_AGAIN;
		}
		if (OpenCms.getSystemInfo().getServletContainerSettings().isReleaseTagsAfterEnd()) {
			release();
		}
		return SKIP_BODY;
	}
	
	@Override
	public int doEndTag() {
		if (OpenCms.getSystemInfo().getServletContainerSettings().isReleaseTagsAfterEnd()) {
			release();
		}
		index = -1;
		return EVAL_PAGE;
	}
	
	private boolean hasMoreContent() {
		if(posts == null) return false;
		
		index++;
		
		if (index<posts.size())
			exposePost(posts.get(index));
		else
			restorePosts();

		return (index<posts.size());
	}
	
	private void exposePost(XmlUserPost post) {
		TfsLista lista = new TfsLista(this.posts.size(),this.index+1,this.size,this.page);
		pageContext.getRequest().setAttribute("postslists", lista);
		pageContext.getRequest().setAttribute("post", post);
	}
	
	private void restorePosts() {
		pageContext.getRequest().setAttribute("post", previous);
    	pageContext.getRequest().setAttribute("postslists", previousList);
	}
	
	private void savePosts() {
		previousList = (TfsLista) pageContext.getRequest().getAttribute("postslists");
		previous = (XmlUserPost) pageContext.getRequest().getAttribute("post");
		
    	pageContext.getRequest().setAttribute("postslists",null);
    	pageContext.getRequest().setAttribute("post",null);
    }
	
	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	public String getSite() {
		return site;
	}

	public void setSite(String site) {
		this.site = site;
	}
	
	public int getCountPosts() {
		return countPosts;
	}

	public void setCountPosts(int countPosts) {
		this.countPosts = countPosts;
	}
	
	public int getPublication() {
		return publication;
	}

	public void setPublication(int publication) {
		this.publication = publication;
	}
	
	public int getFromPost() {
		return fromPost;
	}

	public void setFromPost(int fromPost) {
		this.fromPost = fromPost;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	private List<XmlUserPost> posts;
	private int index = -1;
	private int size=0;
	private int page=1;
	private int publication=0;
	private int countPosts=0;
	private int fromPost=0;
	private String status;
	private String userId;
	private String username;
	private String site;
	private XmlUserPost previous;
	private TfsLista previousList;
	private static final long serialVersionUID = -4954176729601692262L;
}
