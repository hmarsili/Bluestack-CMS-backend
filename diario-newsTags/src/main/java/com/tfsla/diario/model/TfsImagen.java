package com.tfsla.diario.model;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;


public class TfsImagen {

    protected static final Log LOG = CmsLog.getLog(TfsImagen.class);

	private String agency;
	private String author;
	private String title;
	private String description;
	private String tags;
	private Date lastmodifieddate;
	private Date creationdate;
	private String size;
	private String focalPoint;
	
	List<String> categorylist=null;

	Map<String,Boolean> categories=null;
	Map<String,Boolean> subCategories=null;


	public Map<String,Boolean> getHascategory()
	{
		return categories;
	}

	public Map<String,Boolean> getIsinsidecategory()
	{			
		return subCategories;
	}

	public TfsImagen(CmsObject m_cms, CmsResource res)
	{
		try {

			TimeZone  zone = TimeZone.getDefault();
			GregorianCalendar cal = new GregorianCalendar(zone, m_cms.getRequestContext().getLocale());
			
			cal.setTimeInMillis(res.getDateLastModified());
			lastmodifieddate = cal.getTime();

			cal.setTimeInMillis(res.getDateCreated());
			creationdate = cal.getTime();
			
			CmsProperty prop = m_cms.readPropertyObject(res, "Keywords", false);
			if (prop!=null)
				tags = prop.getValue();

			prop = m_cms.readPropertyObject(res, "Title", false);
			if (prop!=null)
				title = prop.getValue();
			
			prop = m_cms.readPropertyObject(res, "Description", false);
			if (prop!=null)
				description = prop.getValue();

			prop = m_cms.readPropertyObject(res, "Author", false);
			if (prop!=null)
				author = prop.getValue();

			prop = m_cms.readPropertyObject(res, "Agency", false);
			if (prop!=null)
				agency = prop.getValue();
			
			prop = m_cms.readPropertyObject(res, "image.size", false);
			if (prop!=null)
				size = prop.getValue();
			prop = m_cms.readPropertyObject(res, "image.focalPoint", false);
			if (prop!=null)
				focalPoint = prop.getValue();
			

			subCategories = new HashMap<String, Boolean>();
			categories = new HashMap<String, Boolean>();
			prop = m_cms.readPropertyObject(res, "category", false);
			if (prop!=null) {
				categorylist = (List<String>) prop.getValueList();
				
				if (categorylist!=null) {
					for (String category : categorylist)
					{
						categories.put(category, true);
	
						String[] subCategoria = category.split("/");
						String categ = "/";
						for (String part : subCategoria)
						{
							categ += part + "/";
							subCategories.put(categ, true);
						}
	
					}
				}
				else
					categorylist = new ArrayList<String>();
			}

		} catch (CmsException e) {
			LOG.error("Error al obtener la informacion de la Imagen",e);
		}
		
	}

	public String getAgency() {
		return agency;
	}

	public void setAgency(String agency) {
		this.agency = agency;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	public List<String> getCategorylist() {
		return categorylist;
	}

	public void setCategorylist(List<String> categorylist) {
		this.categorylist = categorylist;
	}
	
	public Date getLastmodifieddate() {
		return lastmodifieddate;
	}

	public void setLastmodifieddate(Date lastmodifieddate) {
		this.lastmodifieddate = lastmodifieddate;
	}

	public Date getCreationdate() {
		return creationdate;
	}

	public void setCreationdate(Date creationdate) {
		this.creationdate = creationdate;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public String getFocalPoint() {
		return focalPoint;
	}

	public void setFocalPoint(String focalPoint) {
		this.focalPoint = focalPoint;
	}

	
	
}
