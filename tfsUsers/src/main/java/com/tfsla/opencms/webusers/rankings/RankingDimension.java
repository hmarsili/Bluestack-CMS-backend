package com.tfsla.opencms.webusers.rankings;

public class RankingDimension extends UserDimension {
	
	private String dependsOn;
	private String basePath;
	
	public String getDependsOn() {
		return dependsOn;
	}
	public void setDependsOn(String dependsOn) {
		this.dependsOn = dependsOn;
	}
	public String getBasePath() {
		return basePath;
	}
	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null || !(obj instanceof RankingDimension)) return false;
		RankingDimension field = (RankingDimension)obj;
		
		return field.getName().equals(this.getName());
	}
}
