package com.tfsla.vod.model;

import java.sql.Timestamp;

public class TfsVodMyList {

	
	private String userId;
	private String source;
	private Timestamp fecha;
	private Timestamp sourceExpiration;
	private boolean sourceModified;
	
	
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public Timestamp getFecha() {
		return fecha;
	}
	public void setFecha(Timestamp fecha) {
		this.fecha = fecha;
	}
	public Timestamp getSourceExpiration() {
		return sourceExpiration;
	}
	public void setSourceExpiration(Timestamp sourceExpiration) {
		this.sourceExpiration = sourceExpiration;
	}
	public boolean getSourceModified() {
		return sourceModified;
	}
	public void setSourceModified(boolean sourceModified) {
		this.sourceModified = sourceModified;
	}
	
	
	
}
