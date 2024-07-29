package com.tfsla.opencms.webusers.params;

public abstract class A_PrimaryParam extends A_SimpleParam {

	public static final int DATATYPE_STRING = 1;
	public static final int DATATYPE_INTEGER = 2;
	public static final int DATATYPE_DATE = 3;
	
	protected int dataType;

	public int getDataType() {
		return dataType;
	}

	public void setDataType(int dataType) {
		this.dataType = dataType;
	}
	
}
