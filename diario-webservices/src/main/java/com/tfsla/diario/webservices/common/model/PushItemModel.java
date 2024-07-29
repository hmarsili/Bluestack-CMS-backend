package com.tfsla.diario.webservices.common.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsException;
import org.opencms.xml.content.CmsXmlContent;

import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.diario.model.TfsUsuario;
import com.tfsla.diario.webservices.PushNotificationServices.PushItemsService;
import com.tfsla.diario.webservices.common.PushItem;
import com.tfsla.diario.webservices.common.PushStatus;
import com.tfsla.diario.webservices.common.strings.PushNotificationTypes;

public class PushItemModel {
	
	public PushItemModel(PushItem item, CmsObject cms) throws CmsException {
		this.setItem(item);
		this.cms = cms;
		this.xmlContent = PushItemsService.getXmlContent(cms, item);
		this.locale = cms.getRequestContext().getLocale();
	}
	public String getPublicationName() {
		return _edicionService.obtenerTipoEdicion(this.item.getPublication()).getNombre();
	}
	public String getPublicationDescription() {
		return _edicionService.obtenerTipoEdicion(this.item.getPublication()).getDescripcion();
	}
	public String getPath() {
		if (this.xmlContent==null)
			return null;
		
		return this.xmlContent.getFile().getRootPath().replace(this.item.getSite(), "");
	}
	public String getRootPath() {
		if (this.xmlContent==null)
			return null;

		return this.xmlContent.getFile().getRootPath();
	}
	public String getTitle() {
		if (this.xmlContent==null)
			return null;

		return this.xmlContent.getStringValue(cms, "titulo", locale);
	}
	public String getInfo() {
		return this.item.getInfo();
	}
	public String getTitleTooltip() throws CmsException {
		String tooltip = (this.getPath()!=null ? this.getPath() + "<br/>" : "");
		if(this.getUser() != null && this.getUser().getNickname() != null && !this.getUser().getNickname().equals("")) {
			tooltip += "Por " + user.getNickname() + "<br/>";
		}
		tooltip += this.getFullUserName();
		return tooltip;
	}
	public Date getScheduledDate() {
		return this.item.getDateScheduled();
	}
	public String getPushedDateAsString(String format) {
		SimpleDateFormat dt = new SimpleDateFormat(format);
		return dt.format(this.item.getDatePushed());
	}
	public String getScheduledDateAsString(String format) {
		if(this.getType().equals(PushNotificationTypes.EN_COLA)) return "-";
		SimpleDateFormat dt = new SimpleDateFormat(format);
		return dt.format(this.item.getDateScheduled());
	}
	public String getCssClass() {
		if(this.isQueued()) return "index";
		return "non-sortable";
	}
	public String getFullUserName() throws CmsException {
		if(this.item.getUserName() == null || this.item.getUserName().trim().equals("") || this.getCmsUser() == null) return "-";
		return this.getUser().getLastname() + ", " + this.getUser().getFirstname() + "<br/>"
		 + "(" + this.getUser().getEmail() + ")";
	}
	public String getUserName() {
		if(this.item.getUserName() == null || this.item.getUserName().trim().equals("")) return "-";
		return this.item.getUserName();
	}
	public Boolean isDelayed() {
		return this.getScheduledDate().before(new Date());
	}
	public String getType() {
		return this.item.getPushType();
	}
	public int getPriority() {
		return this.item.getPriority();
	}
	public CmsXmlContent getXmlContent() {
		return this.xmlContent;
	}
	public PushStatus getStatus() {
		return item.getStatus();
	}
	public PushItem getItem() {
		return item;
	}
	public Boolean isQueued() {
		return this.getType().equals(PushNotificationTypes.EN_COLA);
	}
	public int getId() {
		return item.getId();
	}
	public void setItem(PushItem item) {
		this.item = item;
	}
	public TfsUsuario getUser() throws CmsException {
		if(this.user == null && this.getCmsUser() != null) {
			this.user = new TfsUsuario(this.getCmsUser(), this.cms);
		}
		return this.user;
	}
	public CmsUser getCmsUser() {
		if(this.cmsUser == null) {
			try {
				this.cmsUser = this.cms.readUser(this.getUserName());
			} catch(Exception e) {
				//e.printStackTrace();
			}
		}
		return this.cmsUser;
	}

	private TfsUsuario user;
	private CmsUser cmsUser;
	private PushItem item;
	private CmsXmlContent xmlContent;
	private CmsObject cms;
	private Locale locale;
	
	private static final TipoEdicionService _edicionService = new TipoEdicionService();
}