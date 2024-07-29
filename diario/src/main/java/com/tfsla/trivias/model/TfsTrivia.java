package com.tfsla.trivias.model;

import java.sql.Date;
import java.sql.Timestamp;

public class TfsTrivia {
	
	private String site;
	private String path;
	private String resultName;
	private String resultType;
	private String resultPoints;
	private String userId;
	private String userName;
	private String user;
	private String publicationName;
	private Timestamp closeDate;
	private Date resultDate;
	private String title;
	
	private int cantUsers;
	private int timeResolution;
	private int publication;
	private int idTrivia;
	
	private boolean multipleGame;
	private boolean registeredUser;
	private boolean storeResults;
	
	public boolean getMultipleGame(){
		return multipleGame;
	}
	
	public void setMultipleGame(boolean multipleGame){
		this.multipleGame = multipleGame;
	}
	
	public boolean getRegisteredUser(){
		return registeredUser;
	}
	
	public void setRegisteredUser(boolean registeredUser){
		this.registeredUser = registeredUser;
	}
	
	public boolean getStoreResults(){
		return storeResults;
	}
	
	public void setStoreResults(boolean storeResults){
		this.storeResults = storeResults;
	}
	
	public String getUserId(){
		return userId;
	}
	
	public void setUserId(String userId){
		this.userId = userId;
	}
	
	public int getIdTrivia(){
		return idTrivia;
	}
	
	public void setIdTrivia(int idTrivia){
		this.idTrivia = idTrivia;
	}
	
	public String getResultType(){
		return resultType;
	}
	
	public void setResultType(String resultType){
		this.resultType = resultType;
	}
	
	public int getTimeResolution(){
		return timeResolution;
	}
	
	public void setTimeResolution(int timeResolution){
		this.timeResolution = timeResolution;
	}
	
	public String getResultPoints(){
		return resultPoints;
	}
	
	public void setResultPoints(String resultPoints){
		this.resultPoints = resultPoints;
	}
	
	public String getResultName(){
		return resultName;
	}
	
	public void setResultName(String resultName){
		this.resultName = resultName;
	}
	
	public String getPath(){
		return path;
	}
	
	public void setPath(String path){
		this.path = path;
	}
	
	public String getSite() {
		return site;
	}
	public void setSite(String site) {
		this.site = site;
	}
	
	public int getPublication(){
		return publication;
	}
	
	public void setPublication(int publication){
		this.publication = publication;
	}

	public Timestamp getCloseDate() {
		return closeDate;
	}

	public void setCloseDate(Timestamp closeDate) {
		this.closeDate = closeDate;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPublicationName() {
		return publicationName;
	}

	public void setPublicationName(String publicationName) {
		this.publicationName = publicationName;
	}

	public int getCantUsers() {
		return cantUsers;
	}

	public void setCantUsers(int cantUsers) {
		this.cantUsers = cantUsers;
	}
	
	public Date getResultDate() {
		return resultDate;
	}

	public void setResultDate(Date resultDate) {
		this.resultDate = resultDate;
	}
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public String toString() {
		return "TfsTrivia [path=" + path + ", resultName=" + resultName + ", resultType="
				+ resultType + ", resultPoints=" + resultPoints + ", userId=" + userId + ", userName=" + userName
				+ ", userMail=" + user + ", publicationName=" + publicationName + ", closeDate=" + closeDate
				+ ", cantUsers=" + cantUsers + ", timeResolution=" + timeResolution + ", publication=" + publication
				+ ", idTrivia=" + idTrivia + "]";
	}
	
	public String getTriviaForExport(String separator) {
		return path + separator + resultName + separator + resultType  + separator 
			+ resultType +  separator + resultPoints + separator + userId + separator + userName
				+ separator + user +separator+ publicationName + separator + closeDate
				+ separator + cantUsers + separator + timeResolution + separator + publication
				+ separator + idTrivia ;
	}
	
}
