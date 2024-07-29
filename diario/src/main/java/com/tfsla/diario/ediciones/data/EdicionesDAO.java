package com.tfsla.diario.ediciones.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.tfsla.data.baseDAO;
import com.tfsla.diario.ediciones.model.Edicion;

public class EdicionesDAO extends baseDAO {

	public List<Edicion> getEdiciones() throws Exception {
		List<Edicion> ediciones = new ArrayList<Edicion>();
		try {
			if (!connectionIsOpen())
				OpenConnection();

			Statement stmt;

			stmt = conn.createStatement();

			ResultSet rs = stmt.executeQuery("SELECT Numero, Fecha, Tipo, tituloTapa, portada, logo, NULLIF(fechaPublicacion, '0000-00-00') AS fechaPublicacion FROM TFS_EDICIONES");

			while (rs.next()) {
				Edicion edicion = fillEdicion(rs);
				ediciones.add(edicion);
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
		return ediciones;
	}

	public List<Edicion> getEdiciones(int tipo) throws Exception {
		List<Edicion> ediciones = new ArrayList<Edicion>();
		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;
			ResultSet rs;

			stmt = conn.prepareStatement("SELECT Numero, Fecha, Tipo, tituloTapa, portada, logo, NULLIF(fechaPublicacion, '0000-00-00') AS fechaPublicacion FROM TFS_EDICIONES WHERE Tipo=? ORDER BY Numero desc");

			stmt.setInt(1,tipo);

			rs = stmt.executeQuery();
					
			while (rs.next()) {
				Edicion edicion = fillEdicion(rs);
				ediciones.add(edicion);
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
		return ediciones;
	}

	public Edicion getEdicion(int tipo, Date fecha) throws Exception {
		Edicion edicion = null;
		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;
			ResultSet rs;

			stmt = conn.prepareStatement("SELECT Numero, Fecha, Tipo, tituloTapa, portada, logo, NULLIF(fechaPublicacion, '0000-00-00') AS fechaPublicacion FROM TFS_EDICIONES WHERE Tipo=? AND Fecha=? ORDER BY Fecha desc");

			stmt.setInt(1,tipo);
			stmt.setDate(2, new java.sql.Date(fecha.getTime()));

			rs = stmt.executeQuery();

			if (rs.next()) {
				  edicion = fillEdicion(rs);
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

		return edicion;
	}

	public List<Edicion> getEdiciones(int tipo, Date fecha) throws Exception {
		List<Edicion> ediciones = new ArrayList<Edicion>();
		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;
			ResultSet rs;

			stmt = conn.prepareStatement("SELECT Numero, Fecha, Tipo, tituloTapa, portada, logo, NULLIF(fechaPublicacion, '0000-00-00') AS fechaPublicacion FROM TFS_EDICIONES WHERE Tipo=? AND Fecha=?");

			stmt.setInt(1,tipo);
			stmt.setDate(2, new java.sql.Date(fecha.getTime()));

			rs = stmt.executeQuery();

			while (rs.next()) {
				  Edicion edicion = fillEdicion(rs);
				  ediciones.add(edicion);
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

		return ediciones;
	}

	public List<Edicion> getEdiciones(Date fecha) throws Exception {
		List<Edicion> ediciones = new ArrayList<Edicion>();
		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;
			ResultSet rs;

			stmt = conn.prepareStatement(
					"SELECT Numero, " +
							"Fecha, " +
							"Tipo, " +
							"tituloTapa, " +
							"portada, " +
							"logo, " +
							"NULLIF(fechaPublicacion, '0000-00-00') AS fechaPublicacion " +
					"FROM TFS_EDICIONES " +
					"WHERE Fecha<=? AND " +
						"NOT EXISTS (" +
							"SELECT 1 " +
							"FROM TFS_EDICIONES TFSE " +
							"WHERE TFSE.Tipo = TFS_EDICIONES.Tipo AND " +
							"TFSE.Fecha <= ? AND TFSE.Fecha > TFS_EDICIONES.Fecha" +
						")");

			stmt.setDate(1, new java.sql.Date(fecha.getTime()));
			stmt.setDate(2, new java.sql.Date(fecha.getTime()));

			rs = stmt.executeQuery();
			
			while (rs.next()) {
				Edicion edicion = fillEdicion(rs);
				ediciones.add(edicion);
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
		return ediciones;
	}


	private Edicion fillEdicion(ResultSet rs) throws SQLException {
		Edicion edicion = new Edicion();

		edicion.setFecha(new Date(rs.getDate("Fecha").getTime()));
		edicion.setNumero(rs.getInt("Numero"));
		edicion.setTipo(rs.getInt("Tipo"));
		edicion.setTituloTapa(rs.getString("tituloTapa"));
		edicion.setPortada(rs.getString("portada"));
		edicion.setLogo(rs.getString("logo"));
		try {
			if (rs.getTimestamp("fechaPublicacion")!=null)
				edicion.setPublicacion(new Date(rs.getTimestamp("fechaPublicacion").getTime()));
			else
				edicion.setPublicacion(null);
		}
		catch (SQLException e)
		{
			edicion.setPublicacion(null);
		}
		return edicion;

	}

	public List<Edicion> getEdiciones(int tipo, java.util.Date desde, java.util.Date hasta) throws Exception
	 {
	  List<Edicion> ediciones = new ArrayList<Edicion>();
	  try {
	    if (!connectionIsOpen()) {
	      OpenConnection();
	    }
	    PreparedStatement stmt = this.conn.prepareStatement("SELECT Numero, Fecha, Tipo, tituloTapa, portada, logo, NULLIF(fechaPublicacion, '0000-00-00') AS fechaPublicacion FROM TFS_EDICIONES WHERE Tipo=? AND Fecha>=? AND Fecha<=? ORDER BY Fecha desc");
	    stmt.setInt(1, tipo);
	    stmt.setDate(2, new java.sql.Date(desde.getTime()));
	    stmt.setDate(3, new java.sql.Date(hasta.getTime()));
	    ResultSet rs = stmt.executeQuery();
	    while (rs.next()) {
	      Edicion edicion = fillEdicion(rs);
	      ediciones.add(edicion);
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
	  return ediciones;
	 }
	public Edicion getEdicion(int tipo, int numero) throws Exception {

		Edicion edicion = null;
		try {
		if (!connectionIsOpen())
			OpenConnection();

			PreparedStatement stmt;
			ResultSet rs;

			stmt = conn.prepareStatement("SELECT Numero, Fecha, Tipo, tituloTapa, portada, logo, NULLIF(fechaPublicacion, '0000-00-00') AS fechaPublicacion from TFS_EDICIONES where Tipo=? AND Numero=?");

			stmt.setInt(1,tipo);
			stmt.setInt(2,numero);


			rs = stmt.executeQuery();

			if (rs.next())
				edicion = fillEdicion(rs);

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
		return edicion;
	}

	public void deleteEdicion(int tipo, int numero)   throws Exception {

		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn.prepareStatement("delete from TFS_EDICIONES where Tipo=? AND Numero=?");
			stmt.setInt(1,tipo);
			stmt.setInt(2,numero);


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


	public List<Edicion> getEdicionesAPublicar(Date now) throws Exception {
		List<Edicion> ediciones = new ArrayList<Edicion>();
		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;
			ResultSet rs;

			stmt = conn.prepareStatement("SELECT Numero, Fecha, Tipo, tituloTapa, portada, logo, NULLIF(fechaPublicacion, '0000-00-00') AS fechaPublicacion FROM TFS_EDICIONES WHERE fechaPublicacion <> '0000-00-00' and fechaPublicacion<=?");

			if (now!=null)
				stmt.setTimestamp(1,new java.sql.Timestamp(now.getTime()));
			else
				stmt.setTimestamp(1,null);

			rs = stmt.executeQuery();

			while (rs.next()) {
				Edicion edicion = fillEdicion(rs);
				ediciones.add(edicion);
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
		return ediciones;
	}

	public void updateFechaPublicacionEdicion(Edicion edicion) throws Exception {
		try {


			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn.prepareStatement("update TFS_EDICIONES set fechaPublicacion=? where Numero=? AND Tipo=?");

			if (edicion.getPublicacion()!=null)
				stmt.setDate(1,new java.sql.Date(edicion.getPublicacion().getTime()));
			else
				stmt.setDate(1,null);
			stmt.setInt(2,edicion.getNumero());
			stmt.setInt(3,edicion.getTipo());

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

	public void updateEdicion(Edicion edicion) throws Exception {
		try {

			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn.prepareStatement("update TFS_EDICIONES set Fecha=?, tituloTapa=?, portada=?, logo=?, fechaPublicacion=? where Numero=? AND Tipo=?");
			stmt.setDate(1,new java.sql.Date(edicion.getFecha().getTime()));
			stmt.setString(2,edicion.getTituloTapa());
			stmt.setString(3,edicion.getPortada());
			stmt.setString(4,edicion.getLogo());
			if (edicion.getPublicacion()!=null)
				stmt.setTimestamp(5, new java.sql.Timestamp(edicion.getPublicacion().getTime()));
			else
				stmt.setDate(5,null);
			stmt.setInt(6,edicion.getNumero());
			stmt.setInt(7,edicion.getTipo());

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

	public int getNextEdicionNumber(int tipo) throws Exception {
		int numero=1;
		try {
			if (!connectionIsOpen())
				OpenConnection();



			PreparedStatement stmt;
			ResultSet rs;

			stmt = conn.prepareStatement("SELECT IFNULL(MAX(Numero)+1,1) as Numero FROM TFS_EDICIONES WHERE Tipo= ? ");
			stmt.setInt(1,tipo);

			rs = stmt.executeQuery();

			if (rs.next()) {
				numero = rs.getInt("Numero");
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

		return numero;

	}

	public void insertEdicion(Edicion edicion) throws Exception {
		try {

			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn.prepareStatement("insert into TFS_EDICIONES (Numero,Tipo,Fecha, tituloTapa, portada, logo,fechaPublicacion) values (?,?,?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS);

			stmt.setInt(1,edicion.getNumero());
			stmt.setInt(2,edicion.getTipo());
			stmt.setDate(3,new java.sql.Date(edicion.getFecha().getTime()));
			stmt.setString(4,edicion.getTituloTapa());
			stmt.setString(5,edicion.getPortada());
			stmt.setString(6,edicion.getLogo());
			if (edicion.getPublicacion()!=null)
				stmt.setTimestamp(7, new java.sql.Timestamp(edicion.getPublicacion().getTime()));
			else
				stmt.setDate(7,null);

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
}

