package com.tfsla.diario.ediciones.data;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;

import com.tfsla.data.baseDAO;
import com.tfsla.diario.ediciones.model.Seccion;

public class SeccionDAO extends baseDAO {

	public List<Seccion> getSecciones() throws Exception {
		List<Seccion> secciones = new ArrayList<Seccion>();

		try {
			if (!connectionIsOpen())
				OpenConnection();

			Statement stmt;

			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("Select ID_SECTION, SECTION_NAME, SECTION_DESCRIPTION, SECTION_PAGE, ID_TIPOEDICION, SECTION_ORDER, SECTION_VISIBILITY from TFS_SECTION");
			//ResultSet rs = stmt.executeQuery("Select ID_SECTION, SECTION_NAME, SECTION_DESCRIPTION, SECTION_PAGE, ID_TIPOEDICION, SECTION_ORDER, SECTION_TWITTERACCOUNT, SECTION_FACEBOOKACCOUNT, SECTION_FACEBOOKPAGEACCOUNT from TFS_SECTION");

			while (rs.next()) {
				Seccion seccion = fillSeccion(rs);
				secciones.add(seccion);
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
		return secciones;
	}

	public List<Seccion> getSeccionesByTipoEdicionId(int tipoEdicionId) throws Exception {
		List<Seccion> secciones = new ArrayList<Seccion>();

		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn.prepareStatement("Select ID_SECTION, SECTION_NAME, SECTION_DESCRIPTION, SECTION_PAGE, ID_TIPOEDICION, SECTION_ORDER, SECTION_VISIBILITY from TFS_SECTION WHERE ID_TIPOEDICION=? ORDER BY SECTION_ORDER,SECTION_DESCRIPTION ");
			//stmt = conn.prepareStatement("Select ID_SECTION, SECTION_NAME, SECTION_DESCRIPTION, SECTION_PAGE, ID_TIPOEDICION, SECTION_ORDER, SECTION_TWITTERACCOUNT, SECTION_FACEBOOKACCOUNT, SECTION_FACEBOOKPAGEACCOUNT from TFS_SECTION WHERE ID_TIPOEDICION=? ORDER BY SECTION_ORDER,SECTION_DESCRIPTION ");
			stmt.setInt(1,tipoEdicionId);

			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				Seccion seccion = fillSeccion(rs);
				secciones.add(seccion);
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
		return secciones;
	}

	public Seccion getSeccion(int seccionId) throws Exception {

		Seccion seccion = null;
		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;
			ResultSet rs;

			stmt = conn.prepareStatement("select ID_SECTION, SECTION_NAME, SECTION_DESCRIPTION, SECTION_PAGE, ID_TIPOEDICION, SECTION_ORDER, SECTION_VISIBILITY from TFS_SECTION where ID_SECTION=?");
			//stmt = conn.prepareStatement("select ID_SECTION, SECTION_NAME, SECTION_DESCRIPTION, SECTION_PAGE, ID_TIPOEDICION, SECTION_ORDER, SECTION_TWITTERACCOUNT, SECTION_FACEBOOKACCOUNT, SECTION_FACEBOOKPAGEACCOUNT from TFS_SECTION where ID_SECTION=?");
			stmt.setInt(1,seccionId);


			rs = stmt.executeQuery();

			rs.next();

			seccion = fillSeccion(rs);

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
		return seccion;
	}

	public void insertSeccion(Seccion seccion) throws Exception {

		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn.prepareStatement("insert into TFS_SECTION (SECTION_NAME, SECTION_DESCRIPTION, SECTION_PAGE, ID_TIPOEDICION, SECTION_ORDER, SECTION_VISIBILITY) values (?,?,?,?,?,?)");
			//stmt = conn.prepareStatement("insert into TFS_SECTION (SECTION_NAME, SECTION_DESCRIPTION, SECTION_PAGE, ID_TIPOEDICION, SECTION_ORDER, SECTION_TWITTERACCOUNT, SECTION_FACEBOOKACCOUNT, SECTION_FACEBOOKPAGEACCOUNT) values (?,?,?,?,?,?,?,?)");
			stmt.setString(1,seccion.getName());
			stmt.setString(2,seccion.getDescription());
			stmt.setString(3,seccion.getPage());
			stmt.setInt(4,seccion.getIdTipoEdicion());
			stmt.setInt(5,seccion.getOrder());
			stmt.setBoolean(6,seccion.getVisibility());
			//stmt.setString(6,seccion.getTwitterAccount());
			//stmt.setString(7,seccion.getFacebookAccount());
			//stmt.setString(8,seccion.getFacebookPageAccount());

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

	public void updateSeccion(Seccion seccion) throws Exception {
		if (!connectionIsOpen())
			OpenConnection();

		PreparedStatement stmt;

		stmt = conn.prepareStatement("update TFS_SECTION set SECTION_NAME=?, SECTION_DESCRIPTION=?, SECTION_PAGE=?, ID_TIPOEDICION=?, SECTION_ORDER=?, SECTION_VISIBILITY=? where ID_SECTION=?");
		//stmt = conn.prepareStatement("update TFS_SECTION set SECTION_NAME=?, SECTION_DESCRIPTION=?, SECTION_PAGE=?, ID_TIPOEDICION=?, SECTION_ORDER=?, SECTION_TWITTERACCOUNT=?, SECTION_FACEBOOKACCOUNT=?, SECTION_FACEBOOKPAGEACCOUNT =? where ID_SECTION=?");
		stmt.setString(1,seccion.getName());
		stmt.setString(2,seccion.getDescription());
		stmt.setString(3,seccion.getPage());
		stmt.setInt(4,seccion.getIdTipoEdicion());
		stmt.setInt(5,seccion.getOrder());
		stmt.setBoolean(6,seccion.getVisibility());
		stmt.setInt(7,seccion.getIdSection());
		
		//stmt.setString(6,seccion.getTwitterAccount());
		//stmt.setString(7,seccion.getFacebookAccount());
		//stmt.setString(8, seccion.getFacebookPageAccount());
		
		stmt.executeUpdate();

		stmt.close();

		if (connectionIsOpenLocaly())
			closeConnection();
	}

	public void deleteSeccion(int seccionId)   throws Exception {
		try  {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn.prepareStatement("delete from TFS_SECTION where ID_SECTION=?");
			stmt.setInt(1,seccionId);

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

	private Seccion fillSeccion(ResultSet rs) throws SQLException {
		Seccion seccion = new Seccion();

		seccion.setIdSection(rs.getInt("ID_SECTION"));
		seccion.setName(rs.getString("SECTION_NAME"));
		seccion.setDescription(rs.getString("SECTION_DESCRIPTION"));
		seccion.setPage(rs.getString("SECTION_PAGE"));
		seccion.setIdTipoEdicion(rs.getInt("ID_TIPOEDICION"));
		seccion.setOrder(rs.getInt("SECTION_ORDER"));
		seccion.setVisibility(rs.getBoolean("SECTION_VISIBILITY"));
		//seccion.setTwitterAccount(rs.getString("SECTION_TWITTERACCOUNT"));
		//seccion.setFacebookAccount(rs.getString("SECTION_FACEBOOKACCOUNT"));
		//seccion.setFacebookPageAccount(rs.getString("SECTION_FACEBOOKPAGEACCOUNT"));
		
		return seccion;

	}

	public Seccion getSeccion(String nombre, int idTipoEdicion) throws Exception {

		Seccion seccion = null;
		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;
			ResultSet rs;

			stmt = conn.prepareStatement("select ID_SECTION, SECTION_NAME, SECTION_DESCRIPTION, SECTION_PAGE, ID_TIPOEDICION, SECTION_ORDER, SECTION_VISIBILITY from TFS_SECTION where SECTION_NAME=? and ID_TIPOEDICION=?");
			//stmt = conn.prepareStatement("select ID_SECTION, SECTION_NAME, SECTION_DESCRIPTION, SECTION_PAGE, ID_TIPOEDICION, SECTION_ORDER, SECTION_TWITTERACCOUNT, SECTION_FACEBOOKACCOUNT, SECTION_FACEBOOKPAGEACCOUNT from TFS_SECTION where SECTION_NAME=? and ID_TIPOEDICION=?");
			stmt.setString(1,nombre);
			stmt.setInt(2,idTipoEdicion);


			rs = stmt.executeQuery();

			rs.next();

			seccion = fillSeccion(rs);

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

		return seccion;

	}
	public List<Seccion> getSeccionesByVisibility(int tipoEdicionId, boolean visitibiliy) throws Exception {

		List<Seccion> secciones = new ArrayList<Seccion>();

		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn.prepareStatement("Select ID_SECTION, SECTION_NAME, SECTION_DESCRIPTION, SECTION_PAGE, ID_TIPOEDICION, SECTION_ORDER, SECTION_VISIBILITY from TFS_SECTION WHERE ID_TIPOEDICION=? and SECTION_VISIBILITY=? ORDER BY SECTION_ORDER,SECTION_DESCRIPTION ");
			//stmt = conn.prepareStatement("Select ID_SECTION, SECTION_NAME, SECTION_DESCRIPTION, SECTION_PAGE, ID_TIPOEDICION, SECTION_ORDER, SECTION_TWITTERACCOUNT, SECTION_FACEBOOKACCOUNT, SECTION_FACEBOOKPAGEACCOUNT from TFS_SECTION WHERE ID_TIPOEDICION=? ORDER BY SECTION_ORDER,SECTION_DESCRIPTION ");
			stmt.setInt(1,tipoEdicionId);
			stmt.setBoolean(2,visitibiliy);

			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				Seccion seccion = fillSeccion(rs);
				secciones.add(seccion);
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
		return secciones;
		
	}
	
	public Seccion getSeccionByPage(String pageName, int idTipoEdicion) throws Exception {

		Seccion seccion = null;
		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;
			ResultSet rs;

			stmt = conn.prepareStatement("select ID_SECTION, SECTION_NAME, SECTION_DESCRIPTION, SECTION_PAGE, ID_TIPOEDICION, SECTION_ORDER, SECTION_VISIBILITY from TFS_SECTION where SECTION_PAGE=? and ID_TIPOEDICION=?");
			
			//stmt = conn.prepareStatement("select ID_SECTION, SECTION_NAME, SECTION_DESCRIPTION, SECTION_PAGE, ID_TIPOEDICION, SECTION_ORDER, SECTION_TWITTERACCOUNT, SECTION_FACEBOOKACCOUNT, SECTION_FACEBOOKPAGEACCOUNT from TFS_SECTION where SECTION_PAGE=? and ID_TIPOEDICION=?");
			stmt.setString(1,pageName);
			stmt.setInt(2,idTipoEdicion);


			rs = stmt.executeQuery();

			rs.next();

			seccion = fillSeccion(rs);

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

		return seccion;

	}

}