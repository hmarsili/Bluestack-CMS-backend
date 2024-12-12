package com.tfsla.diario.ediciones.model;

public class TipoEdicion {

	int id;
	String nombre;
	String descripcion;
	String baseURL;
	String proyecto;
	String tipoPublicacion;
	int edicionActiva;
	boolean online;
	String imagePath;
	String language;
	
	//TODO:Agregar en el service la creacion - si no existen - de las carpetas
	//TODO: Agregar en news-tags que use los indices correspondientes.
	String noticiasIndex;
	String imagenesIndex;
	String videosIndex;
	String audiosIndex;
	String encuestasIndex;
	String eventosIndex;
	String playlistIndex;

	String noticiasIndexOffline;
	String imagenesIndexOffline;
	String videosIndexOffline;
	String audiosIndexOffline;
	String encuestasIndexOffline;
	String eventosIndexOffline;
	String playlistIndexOffline;
	
	String vodGenericIndexOnline;
	String vodGenericIndexOffline;

	String videoVodIndexOffline;
	String vodIndexOffline;
	String vodIndexOnline;

	String twitterFeedIndex;
	
	String triviasIndex;
	String triviasIndexOffline;
	
	String recetaIndexOnline;
	String recetaIndexOffline;

public String getRecetaIndexOnline() {
		return recetaIndexOnline;
	}

	public void setRecetaIndexOnline(String recetaIndexOnline) {
		this.recetaIndexOnline = recetaIndexOnline;
	}

	public String getRecetaIndexOffline() {
		return recetaIndexOffline;
	}

	public void setRecetaIndexOffline(String recetaIndexOffline) {
		this.recetaIndexOffline = recetaIndexOffline;
	}

	
	public String getPlaylistIndex() {
		return playlistIndex;
	}

	public void setPlaylistIndex(String playlistIndex) {
		this.playlistIndex = playlistIndex;
	}

	public String getPlaylistIndexOffline() {
		return playlistIndexOffline;
	}

	public void setPlaylistIndexOffline(String playlistIndexOffline) {
		this.playlistIndexOffline = playlistIndexOffline;
	}

	String twitterFeedIndexOffline;
	
	String videoYoutubeDefaultVFSPath;
	String videoEmbeddedDefaultVFSPath;

	String customDomain;

	
	String videosVodindexOnline;
	public String getVideosVodindexOnline() {
		return videosVodindexOnline;
	}

	public void setVideosVodindexOnline(String videosVodindexOnline) {
		this.videosVodindexOnline = videosVodindexOnline;
	}

	public String getVideoVodIndexOffline() {
		return videoVodIndexOffline;
	}

	public void setVideoVodIndexOffline(String videoVodIndexOffline) {
		this.videoVodIndexOffline = videoVodIndexOffline;
	}

	public String getVodIndexOffline() {
		return vodIndexOffline;
	}

	public void setVodIndexOffline(String vodIndexOffline) {
		this.vodIndexOffline = vodIndexOffline;
	}

	public String getVodIndexOnline() {
		return vodIndexOnline;
	}

	public void setVodIndexOnline(String vodIndexOnline) {
		this.vodIndexOnline = vodIndexOnline;
	}

	
	public String getAudiosIndex() {
		return audiosIndex;
	}

	public void setAudiosIndex(String audiosIndex) {
		this.audiosIndex = audiosIndex;
	}

	public String getAudiosIndexOffline() {
		return audiosIndexOffline;
	}

	public void setAudiosIndexOffline(String audiosIndexOffline) {
		this.audiosIndexOffline = audiosIndexOffline;
	}

	public String getCustomDomain() {
		return customDomain;
	}

	public void setCustomDomain(String customDomain) {
		this.customDomain = customDomain;
	}

	public String getBaseURL() {
		return baseURL;
	}

	public void setBaseURL(String baseURL) {
		this.baseURL = baseURL;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getProyecto() {
		return proyecto;
	}

	public void setProyecto(String proyecto) {
		this.proyecto = proyecto;
	}
	
	public String getTipoPublicacion() {
		return tipoPublicacion;
	}

	public void setTipoPublicacion(String tipoPublicacion) {
		this.tipoPublicacion = tipoPublicacion;
	}

	public int getEdicionActiva() {
		return edicionActiva;
	}

	public void setEdicionActiva(int edicionActiva) {
		this.edicionActiva = edicionActiva;
	}
	
	public boolean isOnline() {
		return online;
	}

	public void setOnline(boolean online) {
		this.online = online;
	}
	
	public String getImagePath() {
		return imagePath;
	}

	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}

	public String getNoticiasIndex() {
		return noticiasIndex;
	}

	public void setNoticiasIndex(String noticiasIndex) {
		this.noticiasIndex = noticiasIndex;
	}

	public String getImagenesIndex() {
		return imagenesIndex;
	}

	public void setImagenesIndex(String imagenesIndex) {
		this.imagenesIndex = imagenesIndex;
	}

	public String getVideosIndex() {
		return videosIndex;
	}

	public void setVideosIndex(String videosIndex) {
		this.videosIndex = videosIndex;
	}

	public String getVideoYoutubeDefaultVFSPath() {
		return videoYoutubeDefaultVFSPath;
	}

	public void setVideoYoutubeDefaultVFSPath(String videoYoutubeDefaultVFSPath) {
		this.videoYoutubeDefaultVFSPath = videoYoutubeDefaultVFSPath;
	}

	public String getVideoEmbeddedDefaultVFSPath() {
		return videoEmbeddedDefaultVFSPath;
	}

	public void setVideoEmbeddedDefaultVFSPath(String videoEmbeddedDefaultVFSPath) {
		this.videoEmbeddedDefaultVFSPath = videoEmbeddedDefaultVFSPath;
	}

	public String getNoticiasIndexOffline() {
		return noticiasIndexOffline;
	}

	public void setNoticiasIndexOffline(String noticiasIndexOffline) {
		this.noticiasIndexOffline = noticiasIndexOffline;
	}

	public String getImagenesIndexOffline() {
		return imagenesIndexOffline;
	}

	public void setImagenesIndexOffline(String imagenesIndexOffline) {
		this.imagenesIndexOffline = imagenesIndexOffline;
	}

	public String getVideosIndexOffline() {
		return videosIndexOffline;
	}

	public void setVideosIndexOffline(String videosIndexOffline) {
		this.videosIndexOffline = videosIndexOffline;
	}
	
	public String getEncuestasIndex() {
		return encuestasIndex;
	}

	public void setEncuestasIndex(String encuestasIndex) {
		this.encuestasIndex = encuestasIndex;
	}

	public String getEncuestasIndexOffline() {
		return encuestasIndexOffline;
	}

	public void setEncuestasIndexOffline(String encuestasIndexOffline) {
		this.encuestasIndexOffline = encuestasIndexOffline;
	}

	public String getTwitterFeedIndex() {
		return twitterFeedIndex;
	}

	public void setTwitterFeedIndex(String twitterFeedIndex) {
		this.twitterFeedIndex = twitterFeedIndex;
	}

	public String getTwitterFeedIndexOffline() {
		return twitterFeedIndexOffline;
	}

	public void setTwitterFeedIndexOffline(String twitterFeedIndexOffline) {
		this.twitterFeedIndexOffline = twitterFeedIndexOffline;
	}

	public String getEventosIndexOffline() {
		return eventosIndexOffline;
	}

	public void setEventosIndexOffline(String eventosIndexOffline) {
		this.eventosIndexOffline = eventosIndexOffline;
	}
	
	public String getEventosIndex() {
		return eventosIndex;
	}

	public void setEventosIndex(String eventosIndex) {
		this.eventosIndex = eventosIndex;
	}


	public String getVodGenericIndexOnline() {
		return vodGenericIndexOnline;
	}

	public void setVodGenericIndexOnline(String vodGenericIndexOnline) {
		this.vodGenericIndexOnline = vodGenericIndexOnline;
	}

	public String getVodGenericIndexOffline() {
		return vodGenericIndexOffline;
	}

	public void setVodGenericIndexOffline(String vodGenericIndexOffline) {
		this.vodGenericIndexOffline = vodGenericIndexOffline;
	}
	
	public String getTriviasIndexOffline() {
		return triviasIndexOffline;
	}

	public void setTriviasIndexOffline(String triviasIndexOffline) {
		this.triviasIndexOffline = triviasIndexOffline;
	}
	
	public String getTriviasIndex() {
		return triviasIndex;
	}

	public void setTriviasIndex(String triviasIndex) {
		this.triviasIndex = triviasIndex;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}
}
