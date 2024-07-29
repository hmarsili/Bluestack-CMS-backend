package com.tfsla.diario.auditActions.resourceMonitor;

public abstract class A_ResourceMonitor implements I_ResourceMonitor {

	protected int resourceType;
	
	@Override
	public int getResourceType() {
		return resourceType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + resourceType;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		A_ResourceMonitor other = (A_ResourceMonitor) obj;
		if (resourceType != other.resourceType)
			return false;
		return true;
	}

}
