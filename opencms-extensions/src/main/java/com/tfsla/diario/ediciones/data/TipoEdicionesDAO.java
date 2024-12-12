package com.tfsla.diario.ediciones.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.tfsla.data.baseDAO;
import com.tfsla.diario.ediciones.model.TipoEdicion;

public class TipoEdicionesDAO extends baseDAO {


	public List<TipoEdicion> getTipoEdiciones() throws Exception {

		List<TipoEdicion> tipoEdiciones = new ArrayList<TipoEdicion>();
		try {
			if (!connectionIsOpen())
				OpenConnection();

			Statement stmt;

			stmt = conn.createStatement();

			ResultSet rs = stmt.executeQuery("SELECT "
					+ "nombre, "
					+ "descripcion, "
					+ "id, "
					+ "url, "
					+ "proyecto, "
					+ "tipoPublicacion, "
					+ "language, "
					+ "edicionActiva, "
					+ "noticiasIndex, "
					+ "imagenesIndex, "
					+ "videosIndex, "
					+ "audiosIndex, "
					+ "encuestasIndex, "
					+ "twitterFeedIndex, "
					+ "noticiasIndexOffline, "
					+ "imagenesIndexOffline, "
					+ "videosIndexOffline, "
					+ "audiosIndexOffline, "
					+ "encuestasIndexOffline, "
					+ "twitterFeedIndexOffline, "
					+ "videoYoutubeDefaultVFSPath, "
					+ "videoEmbeddedDefaultVFSPath, "
					+ "eventosIndex, "
					+ "eventosIndexOffline, "
					+ "videosVodindexOnline,"
					+ "videoVodIndexOffline, "
					+ "vodIndexOffline, "
					+ "vodIndexOnline, "
					+ "playlistIndexOffline, "
					+ "playlistIndexOnline,"
					+ "vodGenericIndexOnline ,"
					+ "vodGenericIndexOffline ,"
					+ "triviasIndex, "
					+ "triviasIndexOffline, "
					+ "recetaIndexOnline ,"
					+ "recetaIndexOffline ,"
					+ "customDomain ,"
					+ "imagePath "
					+ "FROM TFS_TIPO_EDICIONES");

			

			while (rs.next()) {
				TipoEdicion tipoEdicion = fillTipoEdicion(rs);
				tipoEdiciones.add(tipoEdicion);
			}

			rs.close();
			stmt.close();

		}
		catch (Exception e) {
			throw e;
		}
		finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
		return tipoEdiciones;
	}

	public List<TipoEdicion> getTipoEdiciones(String Proyecto) throws Exception {
		List<TipoEdicion> tipoEdiciones = new ArrayList<TipoEdicion>();

		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;
			ResultSet rs;

			stmt = conn.prepareStatement("SELECT "
					+ "nombre, "
					+ "descripcion, "
					+ "id, "
					+ "url, "
					+ "proyecto, "
					+ "tipoPublicacion, "
					+ "language, "
					+ "edicionActiva, "
					+ "noticiasIndex, "
					+ "imagenesIndex, "
					+ "videosIndex, "
					+ "audiosIndex, "
					+ "encuestasIndex, "
					+ "twitterFeedIndex, "
					+ "noticiasIndexOffline, "
					+ "imagenesIndexOffline, "
					+ "videosIndexOffline, "
					+ "audiosIndexOffline, "
					+ "encuestasIndexOffline, "
					+ "twitterFeedIndexOffline, "
					+ "videoYoutubeDefaultVFSPath, "
					+ "videoEmbeddedDefaultVFSPath, "
					+ "eventosIndex, "
					+ "eventosIndexOffline, "
					+ "videosVodindexOnline,"
					+ "videoVodIndexOffline, "
					+ "vodIndexOffline, "
					+ "vodIndexOnline, "
					+ "playlistIndexOffline, "
					+ "playlistIndexOnline, "
					+ "vodGenericIndexOnline, "
					+ "vodGenericIndexOffline, "
					+ "triviasIndex, "
					+ "triviasIndexOffline, "
					+ "recetaIndexOnline ,"
					+ "recetaIndexOffline ,"
					+ "customDomain ,"
					+ "imagePath "
					+ "FROM TFS_TIPO_EDICIONES "
					+ "where proyecto=?");

			stmt.setString(1, Proyecto);

			rs = stmt.executeQuery();

			while (rs.next()) {
				TipoEdicion tipoEdicion = fillTipoEdicion(rs);
				tipoEdiciones.add(tipoEdicion);
			}

			rs.close();
			stmt.close();

		}
		catch (Exception e) {
			throw e;
		}
		finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
		return tipoEdiciones;
	}

	public boolean TipoEdicionExists(String nombre, String proyecto)  throws Exception {
		boolean exists = false;

		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;
			ResultSet rs;

			stmt = conn.prepareStatement("SELECT "
					+ "nombre, "
					+ "descripcion, "
					+ "id, "
					+ "url, "
					+ "proyecto, "
					+ "tipoPublicacion, "
					+ "language, "
					+ "edicionActiva, "
					+ "noticiasIndex, "
					+ "imagenesIndex, "
					+ "videosIndex, "
					+ "audiosIndex, "
					+ "encuestasIndex, "
					+ "twitterFeedIndex, "
					+ "noticiasIndexOffline, "
					+ "imagenesIndexOffline, "
					+ "videosIndexOffline, "
					+ "audiosIndexOffline, "
					+ "encuestasIndexOffline, "
					+ "twitterFeedIndexOffline, "
					+ "videoYoutubeDefaultVFSPath, "
					+ "videoEmbeddedDefaultVFSPath, "
					+ "eventosIndex, "
					+ "eventosIndexOffline, "
					+ "videosVodindexOnline,"
					+ "videoVodIndexOffline, "
					+ "vodIndexOffline, "
					+ "vodIndexOnline, "
					+ "playlistIndexOffline, "
					+ "playlistIndexOnline, "
					+ "vodGenericIndexOnline, "
					+ "vodGenericIndexOffline, "
					+ "triviasIndex, "
					+ "triviasIndexOffline, "
					+ "recetaIndexOnline ,"
					+ "recetaIndexOffline ,"
					+ "customDomain ,"
					+ "imagePath "
					+ "FROM TFS_TIPO_EDICIONES "
					+ "where nombre=? "
					+ "and proyecto=?");

			stmt.setString(1, nombre);
			stmt.setString(2, proyecto);

			rs = stmt.executeQuery();

			if (rs.next()) {
				exists = true;
			}

			rs.close();
			stmt.close();

		}
		catch (Exception e) {
			throw e;
		}
		finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
		return exists;
	}

	private TipoEdicion fillTipoEdicion(ResultSet rs) throws SQLException {
		TipoEdicion tipoEdicion = new TipoEdicion();

		tipoEdicion.setBaseURL(rs.getString("url"));
		tipoEdicion.setDescripcion(rs.getString("descripcion"));
		tipoEdicion.setNombre(rs.getString("nombre"));
		tipoEdicion.setId(rs.getInt("id"));
		tipoEdicion.setProyecto(rs.getString("proyecto"));
		tipoEdicion.setTipoPublicacion(rs.getString("tipoPublicacion"));
		
		if(rs.getInt("tipoPublicacion") == 1 || rs.getInt("tipoPublicacion") == 2)
			tipoEdicion.setOnline(true);
		else
			tipoEdicion.setOnline(false);
		
		tipoEdicion.setLanguage(rs.getString("language"));
			
		tipoEdicion.setEdicionActiva(rs.getInt("edicionActiva"));

		tipoEdicion.setNoticiasIndex(rs.getString("noticiasIndex"));
		tipoEdicion.setImagenesIndex (rs.getString("imagenesIndex"));
		tipoEdicion.setVideosIndex (rs.getString("videosIndex"));
		tipoEdicion.setEncuestasIndex(rs.getString("encuestasIndex"));
		tipoEdicion.setTwitterFeedIndex(rs.getString("twitterFeedIndex"));
		tipoEdicion.setAudiosIndex(rs.getString("audiosIndex"));
		tipoEdicion.setEventosIndex(rs.getString("eventosIndex"));
		
		tipoEdicion.setNoticiasIndexOffline(rs.getString("noticiasIndexOffline"));
		tipoEdicion.setImagenesIndexOffline(rs.getString("imagenesIndexOffline"));
		tipoEdicion.setVideosIndexOffline(rs.getString("videosIndexOffline"));
		tipoEdicion.setEncuestasIndexOffline(rs.getString("encuestasIndexOffline"));
		tipoEdicion.setTwitterFeedIndexOffline(rs.getString("twitterFeedIndexOffline"));
		tipoEdicion.setAudiosIndexOffline(rs.getString("audiosIndexOffline"));
		tipoEdicion.setEventosIndexOffline(rs.getString("eventosIndexOffline"));
		tipoEdicion.setVideoVodIndexOffline(rs.getString("videoVodIndexOffline"));
		tipoEdicion.setVideosVodindexOnline(rs.getString("videosVodindexOnline"));
		
		tipoEdicion.setVodIndexOffline(rs.getString("vodIndexOffline"));
		tipoEdicion.setVodIndexOnline(rs.getString("vodIndexOnline"));
		
		tipoEdicion.setPlaylistIndexOffline(rs.getString("playlistIndexOffline"));
		tipoEdicion.setPlaylistIndex(rs.getString("playlistIndexOnline"));
		
		tipoEdicion.setVodGenericIndexOffline(rs.getString("vodGenericIndexOffline"));
		tipoEdicion.setVodGenericIndexOnline(rs.getString("vodGenericIndexOnline"));
		
		tipoEdicion.setVideoYoutubeDefaultVFSPath (rs.getString("videoYoutubeDefaultVFSPath"));
		tipoEdicion.setVideoEmbeddedDefaultVFSPath (rs.getString("videoEmbeddedDefaultVFSPath"));
		
		tipoEdicion.setTriviasIndex(rs.getString("triviasIndex"));
		tipoEdicion.setTriviasIndexOffline(rs.getString("triviasIndexOffline"));
		
		tipoEdicion.setRecetaIndexOnline(rs.getString("recetaIndexOnline"));
		tipoEdicion.setRecetaIndexOffline (rs.getString("recetaIndexOffline"));
		
		tipoEdicion.setCustomDomain(rs.getString("customDomain"));
		
		tipoEdicion.setImagePath(rs.getString("imagePath"));
		
		return tipoEdicion;

	}


	@Deprecated
	public TipoEdicion getTipoEdicion(String nombre)  throws Exception {
		TipoEdicion tipoEdicion = null;

		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;
			ResultSet rs;

			stmt = conn.prepareStatement("SELECT "
					+ "nombre, "
					+ "descripcion, "
					+ "id, "
					+ "url, "
					+ "proyecto, "
					+ "tipoPublicacion, "
					+ "edicionActiva, "
					+ "language, "
					+ "noticiasIndex, "
					+ "imagenesIndex, "
					+ "videosIndex, "
					+ "audiosIndex, "
					+ "encuestasIndex, "
					+ "twitterFeedIndex, "
					+ "noticiasIndexOffline, "
					+ "imagenesIndexOffline, "
					+ "videosIndexOffline, "
					+ "audiosIndexOffline, "
					+ "encuestasIndexOffline, "
					+ "twitterFeedIndexOffline, "
					+ "videoYoutubeDefaultVFSPath, "
					+ "videoEmbeddedDefaultVFSPath, "
					+ "eventosIndex, "
					+ "eventosIndexOffline, "
					+ "videosVodindexOnline,"
					+ "videoVodIndexOffline, "
					+ "vodIndexOffline, "
					+ "vodIndexOnline, "
					+ "playlistIndexOffline, "
					+ "playlistIndexOnline, "
					+ "vodGenericIndexOnline, "
					+ "vodGenericIndexOffline, "
					+ "triviasIndex, "
					+ "triviasIndexOffline, "
					+ "recetaIndexOnline ,"
					+ "recetaIndexOffline ,"
					+ "customDomain ,"
					+ "imagePath "
					+ "FROM TFS_TIPO_EDICIONES "
					+ "where nombre=?");

			stmt.setString(1, nombre);

			rs = stmt.executeQuery();

			if (rs.next())
				tipoEdicion = fillTipoEdicion(rs);

			rs.close();
			stmt.close();

		}
		catch (Exception e) {
			throw e;
		}
		finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
		return tipoEdicion;
	}

	public TipoEdicion getTipoEdicion(int id) throws Exception {

		TipoEdicion tipoEdicion = null;
		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;
			ResultSet rs;

			stmt = conn.prepareStatement("SELECT "
					+ "nombre, "
					+ "descripcion, "
					+ "id, "
					+ "url, "
					+ "proyecto, "
					+ "tipoPublicacion, "
					+ "language, "
					+ "edicionActiva, "
					+ "noticiasIndex, "
					+ "imagenesIndex, "
					+ "videosIndex, "
					+ "audiosIndex, "
					+ "encuestasIndex, "
					+ "twitterFeedIndex, "
					+ "noticiasIndexOffline, "
					+ "imagenesIndexOffline, "
					+ "videosIndexOffline, "
					+ "audiosIndexOffline, "
					+ "encuestasIndexOffline, "
					+ "twitterFeedIndexOffline, "
					+ "videoYoutubeDefaultVFSPath, "
					+ "videoEmbeddedDefaultVFSPath, "
					+ "eventosIndex, "
					+ "eventosIndexOffline, "
					+ "videosVodindexOnline,"
					+ "videoVodIndexOffline, "
					+ "vodIndexOffline, "
					+ "vodIndexOnline, "	
					+ "playlistIndexOffline, "
					+ "playlistIndexOnline, "
					+ "vodGenericIndexOnline, "
					+ "vodGenericIndexOffline, "
					+ "triviasIndex, "
					+ "triviasIndexOffline, "
					+ "recetaIndexOnline ,"
					+ "recetaIndexOffline ,"
					+ "customDomain ,"
					+ "imagePath "
					+ "FROM TFS_TIPO_EDICIONES "
					+ "where id=?");
			stmt.setInt(1,id);

			rs = stmt.executeQuery();

			if (rs.next())
				tipoEdicion = fillTipoEdicion(rs);

			rs.close();
			stmt.close();

		}
		catch (Exception e) {
			throw e;
		}
		finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
		return tipoEdicion;
	}

	public TipoEdicion getTipoEdicion(String proyecto, String nombre) throws Exception {
		TipoEdicion tipoEdicion = null;
		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;
			ResultSet rs;

			stmt = conn.prepareStatement("SELECT "
					+ "nombre, "
					+ "descripcion, "
					+ "id, "
					+ "url, "
					+ "proyecto, "
					+ "tipoPublicacion, "
					+ "language, "
					+ "edicionActiva, "
					+ "noticiasIndex, "
					+ "imagenesIndex, "
					+ "videosIndex, "
					+ "audiosIndex, "
					+ "encuestasIndex, "
					+ "twitterFeedIndex, "
					+ "noticiasIndexOffline, "
					+ "imagenesIndexOffline, "
					+ "videosIndexOffline, "
					+ "audiosIndexOffline, "
					+ "encuestasIndexOffline, "
					+ "twitterFeedIndexOffline, "
					+ "videoYoutubeDefaultVFSPath, "
					+ "videoEmbeddedDefaultVFSPath, "
					+ "eventosIndex, "
					+ "eventosIndexOffline, "
					+ "videosVodindexOnline,"
					+ "videoVodIndexOffline, "
					+ "vodIndexOffline, "
					+ "vodIndexOnline, "
					+ "playlistIndexOffline, "
					+ "playlistIndexOnline, "
					+ "vodGenericIndexOnline, "
					+ "vodGenericIndexOffline, "
					+ "triviasIndex, "
					+ "triviasIndexOffline, "
					+ "recetaIndexOnline ,"
					+ "recetaIndexOffline ,"
					+ "customDomain ,"
					+ "imagePath "
					+ "FROM TFS_TIPO_EDICIONES "
					+ "where proyecto=? "
					+ "and nombre=?");
			stmt.setString(1,proyecto);
			stmt.setString(2,nombre);

			rs = stmt.executeQuery();

			if (rs.next())
				tipoEdicion = fillTipoEdicion(rs);

			rs.close();
			stmt.close();
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
		return tipoEdicion;
	}

	public TipoEdicion getTipoEdicionOnline(String proyecto) throws Exception {
		return this.getTipoEdicionOnlineRoot(proyecto);
	}

	public TipoEdicion getTipoEdicionOnlineRoot(String proyecto) throws Exception {
		TipoEdicion tipoEdicion = null;
		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;
			ResultSet rs;

			stmt = conn.prepareStatement("SELECT "
					+ "nombre, "
					+ "descripcion, "
					+ "id, "
					+ "url, "
					+ "proyecto, "
					+ "tipoPublicacion, "
					+ "language, "
					+ "edicionActiva, "
					+ "noticiasIndex, "
					+ "imagenesIndex, "
					+ "videosIndex, "
					+ "audiosIndex, "
					+ "encuestasIndex, "
					+ "twitterFeedIndex, "
					+ "noticiasIndexOffline, "
					+ "imagenesIndexOffline, "
					+ "videosIndexOffline, "
					+ "audiosIndexOffline, "
					+ "encuestasIndexOffline, "
					+ "twitterFeedIndexOffline, "
					+ "videoYoutubeDefaultVFSPath, "
					+ "videoEmbeddedDefaultVFSPath, "
					+ "eventosIndex, "
					+ "videosVodindexOnline,"
					+ "videoVodIndexOffline, "
					+ "vodIndexOffline, "
					+ "vodIndexOnline, "
					+ "eventosIndexOffline, "
					+ "playlistIndexOffline, "
					+ "playlistIndexOnline, "
					+ "vodGenericIndexOnline, "
					+ "vodGenericIndexOffline, "
					+ "triviasIndex, "
					+ "triviasIndexOffline, "
					+ "recetaIndexOnline ,"
					+ "recetaIndexOffline ,"
					+ "customDomain ,"
					+ "imagePath "
					+ "FROM TFS_TIPO_EDICIONES "
					+ "where proyecto=? "
					+ "and tipoPublicacion=1");
			stmt.setString(1,proyecto);

			rs = stmt.executeQuery();

			if (rs.next())
				tipoEdicion = fillTipoEdicion(rs);

			rs.close();
			stmt.close();
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
		return tipoEdicion;
	}


	public void deleteTipoEdicion(int id)   throws Exception {

		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn.prepareStatement("delete from TFS_TIPO_EDICIONES where id=?");
			stmt.setInt(1,id);

			stmt.execute();

			stmt.close();

		}
		catch (Exception e) {
			throw e;
		}
		finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
	}

	public void updateTipoEdicion(TipoEdicion tipoEdicion) throws Exception {

		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn.prepareStatement("update TFS_TIPO_EDICIONES set "
					+ "descripcion=?, "
					+ "noticiasIndex = ?, "
					+ "imagenesIndex = ?, "
					+ "videosIndex = ?, "
					+ "audiosIndex = ?, "
					+ "encuestasIndex = ?, "
					+ "twitterFeedIndex = ?, "
					+ "noticiasIndexOffline = ?, "
					+ "imagenesIndexOffline = ?, "
					+ "videosIndexOffline = ?, "
					+ "audiosIndexOffline = ?, "
					+ "encuestasIndexOffline = ?, "
					+ "twitterFeedIndexOffline = ?, "
					+ "videoYoutubeDefaultVFSPath = ?, "
					+ "videoEmbeddedDefaultVFSPath = ?, "
					+ "eventosIndex = ?, "
					+ "eventosIndexOffline = ?, "	
					+ "videosVodIndexOnline= ?,"
					+ "videoVodIndexOffline = ?, "
					+ "vodIndexOffline = ?, "
					+ "vodIndexOnline = ?, "
					+ "playlistIndexOffline= ?, "
					+ "playlistIndexOnline= ?, "
					+ "vodGenericIndexOnline = ?, "
					+ "vodGenericIndexOffline = ?, "
					+ "triviasIndex = ?, "
					+ "triviasIndexOffline= ?, "
					+ "recetaIndexOnline = ?,"
					+ "recetaIndexOffline = ?,"
					+ "customDomain=? ,"
					+ "language=?, "
					+ "imagePath=?"
					+ "where id=?");

			stmt.setString(1,tipoEdicion.getDescripcion());

			stmt.setString(2,tipoEdicion.getNoticiasIndex());
			stmt.setString(3, tipoEdicion.getImagenesIndex());
			stmt.setString(4, tipoEdicion.getVideosIndex());
			stmt.setString(5, tipoEdicion.getAudiosIndex());

			stmt.setString(6, tipoEdicion.getEncuestasIndex());
			stmt.setString(7, tipoEdicion.getTwitterFeedIndex());
			
			stmt.setString(8,tipoEdicion.getNoticiasIndexOffline());
			stmt.setString(9, tipoEdicion.getImagenesIndexOffline());
			stmt.setString(10, tipoEdicion.getVideosIndexOffline());
			stmt.setString(11, tipoEdicion.getAudiosIndexOffline());
			stmt.setString(12, tipoEdicion.getEncuestasIndexOffline());
			stmt.setString(13, tipoEdicion.getTwitterFeedIndexOffline());

			stmt.setString(14, tipoEdicion.getVideoYoutubeDefaultVFSPath());
			stmt.setString(15, tipoEdicion.getVideoEmbeddedDefaultVFSPath());

			stmt.setString(16, tipoEdicion.getEventosIndex());
			stmt.setString(17, tipoEdicion.getEventosIndexOffline());
	
			stmt.setString(18, tipoEdicion.getVideosVodindexOnline());
			stmt.setString(19, tipoEdicion.getVideoVodIndexOffline());
			stmt.setString(20, tipoEdicion.getVodIndexOffline());
			stmt.setString(21, tipoEdicion.getVodIndexOnline());

			stmt.setString(22, tipoEdicion.getPlaylistIndexOffline());
			stmt.setString(23, tipoEdicion.getPlaylistIndex());

			stmt.setString(24, tipoEdicion.getVodGenericIndexOnline());
			stmt.setString(25, tipoEdicion.getVodGenericIndexOffline());

			stmt.setString(26, tipoEdicion.getTriviasIndex());
			stmt.setString(27, tipoEdicion.getTriviasIndexOffline());
			
			stmt.setString(28, tipoEdicion.getRecetaIndexOnline());
			stmt.setString(29, tipoEdicion.getRecetaIndexOffline());
			
			stmt.setString(30,tipoEdicion.getCustomDomain());
			
			stmt.setString(31,tipoEdicion.getLanguage());
			
			stmt.setString(32,tipoEdicion.getImagePath());
			
			stmt.setInt(33,tipoEdicion.getId());

			stmt.executeUpdate();

			stmt.close();

		}
		catch (Exception e) {
			throw e;
		}
		finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
	}

	public void updateEdicionActiva(int tipo, int numero) throws Exception {
		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn.prepareStatement("update TFS_TIPO_EDICIONES set "
					+ "edicionActiva=?  "
					+ "where id=?");

			stmt.setInt(1,numero);
			stmt.setInt(2,tipo);

			stmt.executeUpdate();

			stmt.close();

		}
		catch (Exception e) {
			throw e;
		}
		finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
	}

	public void insertTipoEdicion(TipoEdicion tipoEdicion) throws Exception {
		if (!connectionIsOpen())
			OpenConnection();

		PreparedStatement stmt;

		stmt = conn.prepareStatement("insert into TFS_TIPO_EDICIONES ("
				+ "nombre, "
				+ "descripcion, "
				+ "url, "
				+ "proyecto, "
				+ "tipoPublicacion, "
				+ "language, "
				+ "noticiasIndex, "
				+ "imagenesIndex, "
				+ "videosIndex, "
				+ "audiosIndex, "				
				+ "encuestasIndex, "
				+ "twitterFeedIndex, "
				+ "noticiasIndexOffline, "
				+ "imagenesIndexOffline, "
				+ "videosIndexOffline, "
				+ "audiosIndexOffline, "				
				+ "encuestasIndexOffline, "
				+ "twitterFeedIndexOffline, "
				+ "videoYoutubeDefaultVFSPath, "
				+ "videoEmbeddedDefaultVFSPath, "
				+ "eventosIndex, "
				+ "eventosIndexOffline, "
				+ "triviasIndex, "
				+ "triviasIndexOffline, "
				+ "recetaIndexOnline ,"
				+ "recetaIndexOffline ,"
				+ "customDomain,"
				+ "imagePath ) "
				+ "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

		stmt.setString(1,tipoEdicion.getNombre());
		stmt.setString(2,tipoEdicion.getDescripcion());
		stmt.setString(3,tipoEdicion.getBaseURL());
		stmt.setString(4,tipoEdicion.getProyecto());
		stmt.setString(5,tipoEdicion.getTipoPublicacion());
		stmt.setString(6,tipoEdicion.getLanguage());

		stmt.setString(7,tipoEdicion.getNoticiasIndex());
		stmt.setString(8, tipoEdicion.getImagenesIndex());
		stmt.setString(9, tipoEdicion.getVideosIndex());
		stmt.setString(10, tipoEdicion.getAudiosIndex());

		stmt.setString(11, tipoEdicion.getEncuestasIndex());
		stmt.setString(12, tipoEdicion.getTwitterFeedIndex());

		stmt.setString(13,tipoEdicion.getNoticiasIndexOffline());
		stmt.setString(14, tipoEdicion.getImagenesIndexOffline());
		stmt.setString(15, tipoEdicion.getVideosIndexOffline());
		stmt.setString(16, tipoEdicion.getAudiosIndexOffline());

		stmt.setString(17, tipoEdicion.getEncuestasIndexOffline());
		stmt.setString(18, tipoEdicion.getTwitterFeedIndexOffline());

		stmt.setString(19, tipoEdicion.getVideoYoutubeDefaultVFSPath());
		stmt.setString(20, tipoEdicion.getVideoEmbeddedDefaultVFSPath());

		stmt.setString(21, tipoEdicion.getEventosIndex());
		stmt.setString(22, tipoEdicion.getEncuestasIndexOffline());
		
		stmt.setString(23, tipoEdicion.getTriviasIndex());
		stmt.setString(24, tipoEdicion.getTriviasIndexOffline());

		stmt.setString(25, tipoEdicion.getRecetaIndexOnline());
		stmt.setString(26, tipoEdicion.getRecetaIndexOffline());

		stmt.setString(27, tipoEdicion.getCustomDomain());
		stmt.setString(28, tipoEdicion.getImagePath());
		
		stmt.executeUpdate();

		stmt.close();

		if (connectionIsOpenLocaly())
			closeConnection();
	}

	public List<TipoEdicion> getTipoEdicionesImpresas(String proyecto) throws Exception {
		List<TipoEdicion> tipoEdiciones = new ArrayList<TipoEdicion>();

		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;
			ResultSet rs;

			stmt = conn.prepareStatement("SELECT "
					+ "nombre, "
					+ "descripcion, "
					+ "id, "
					+ "url, "
					+ "proyecto, "
					+ "tipoPublicacion, "
					+ "language, "
					+ "edicionActiva, "
					+ "noticiasIndex, "
					+ "imagenesIndex, "
					+ "videosIndex, "
					+ "audiosIndex, "
					+ "encuestasIndex, "
					+ "twitterFeedIndex, "
					+ "noticiasIndexOffline, "
					+ "imagenesIndexOffline, "
					+ "videosIndexOffline, "
					+ "audiosIndexOffline, "
					+ "encuestasIndexOffline, "
					+ "twitterFeedIndexOffline, "
					+ "videoYoutubeDefaultVFSPath, "
					+ "videoEmbeddedDefaultVFSPath, "
					+ "eventosIndex, "
					+ "eventosIndexOffline, "
					+ "triviasIndex, "
					+ "triviasIndexOffline, "
					+ "recetaIndexOnline ,"
					+ "recetaIndexOffline ,"
					+ "customDomain ,"
					+ "imagePath "
					+ "FROM TFS_TIPO_EDICIONES "
					+ "where tipoPublicacion = 3 "
					+ "AND proyecto=?");

			stmt.setString(1, proyecto);

			rs = stmt.executeQuery();

			while (rs.next()) {
				TipoEdicion tipoEdicion = fillTipoEdicion(rs);
				tipoEdiciones.add(tipoEdicion);
			}

			rs.close();
			stmt.close();

		}
		catch (Exception e) {
			throw e;
		}
		finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
		return tipoEdiciones;
	}

}
