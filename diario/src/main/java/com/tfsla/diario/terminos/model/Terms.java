package com.tfsla.diario.terminos.model;
public class Terms {
	public Terms() {
		this.isFullTag = true;
	}
	
	protected long id_term;
	protected String name;
	protected String name_search;
	protected String lastmodified;
	protected String description;
	protected String image;
	protected String url;
	protected long type;
	protected int approved;
	protected String template;
	protected String synonymous="";
	protected Boolean isFullTag;
	protected long PrevType;
	protected String custom1="";
	protected String custom2 ="";
	protected String custom3 ="";
	protected String custom4 ="";
	protected String custom5 ="";
	protected String custom6 ="";
	protected String custom7 ="";
	protected String custom8 ="";
	protected String custom9 ="";
	protected String custom10 ="";
	protected String facebook ="";
	protected String twt ="";
	protected String linkedin ="";
	protected String google ="";
	protected String instagram ="";
	protected String cuerpo ="";
	
	@Override
	protected Object clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return super.clone();
	}
	
	public long getPrevType() {
		return PrevType;
	}
	public void setPrevType(long prevType) {
		PrevType = prevType;
	}
	public String getTemplate() {
		return template;
	}
	public void setTemplate(String template) {
		this.template = template;
	}

	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getImage() {
		return image;
	}
	public void setImage(String image) {
		this.image = image;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public long getId_term() {
		return id_term;
	}
	public void setId_term(long id_term) {
		this.id_term = id_term;
	}
	public String getLastmodified() {
		return lastmodified;
	}
	public void setLastmodified(String lastmodified) {
		this.lastmodified = lastmodified;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public long getType() {
		return type;
	}
	public void setType(long type) {
		this.type = type;
	}
	public int getApproved() {
		return approved;
	}
	public void setAproved(int approved) {
		this.approved = approved;
	}
	public Boolean getIsFullTag() {
		return isFullTag;
	}
	public void setIsFullTag(Boolean isFullTag) {
		this.isFullTag = isFullTag;
	}

	public String getSynonymous() {
		return synonymous;
	}

	public void setSynonymous(String synonymous) {
		if (synonymous== null || synonymous.trim().equals("null"))
			synonymous="";
		this.synonymous = synonymous;
	}
	public String getName_search() {
		return name_search;
	}
	public void setName_search(String name_search) {
		this.name_search = name_search;
	}

	public String getCustom1() {
		return custom1;
	}

	public void setCustom1(String custom1) {
		this.custom1 = custom1;
	}

	public String getCustom2() {
		return custom2;
	}

	public void setCustom2(String custom2) {
		this.custom2 = custom2;
	}

	public String getCustom3() {
		return custom3;
	}

	public void setCustom3(String custom3) {
		this.custom3 = custom3;
	}

	public String getCustom4() {
		return custom4;
	}

	public void setCustom4(String custom4) {
		this.custom4 = custom4;
	}

	public String getCustom5() {
		return custom5;
	}

	public void setCustom5(String custom5) {
		this.custom5 = custom5;
	}

	public String getCustom6() {
		return custom6;
	}

	public void setCustom6(String custom6) {
		this.custom6 = custom6;
	}

	public String getCustom7() {
		return custom7;
	}

	public void setCustom7(String custom7) {
		this.custom7 = custom7;
	}

	public String getCustom8() {
		return custom8;
	}

	public void setCustom8(String custom8) {
		this.custom8 = custom8;
	}

	public String getCustom9() {
		return custom9;
	}

	public void setCustom9(String custom9) {
		this.custom9 = custom9;
	}

	public String getCustom10() {
		return custom10;
	}

	public void setCustom10(String custom10) {
		this.custom10 = custom10;
	}

	public String getFacebook() {
		return facebook;
	}

	public void setFacebook(String facebook) {
		this.facebook = facebook;
	}

	public String getTwt() {
		return twt;
	}

	public void setTwt(String twt) {
		this.twt = twt;
	}

	public String getLinkedin() {
		return linkedin;
	}

	public void setLinkedin(String linkedin) {
		this.linkedin = linkedin;
	}

	public String getGoogle() {
		return google;
	}

	public void setGoogle(String google) {
		this.google = google;
	}

	public String getInstagram() {
		return instagram;
	}

	public void setInstagram(String instagram) {
		this.instagram = instagram;
	}
	
	public String getCuerpo() {
		return cuerpo;
	}

	public void setCuerpo(String cuerpo) {
		this.cuerpo = cuerpo;
	}


}
