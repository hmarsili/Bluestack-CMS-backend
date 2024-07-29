package com.tfsla.diario.ediciones.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.tfsla.data.baseDAO;
import com.tfsla.diario.ediciones.model.Zona;

public class ZoneDAO extends baseDAO {

	public List<Zona> getZonas(int TipoEdicionId, int PageId) throws Exception {
		List<Zona> zonas = new ArrayList<Zona>();

		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn
					.prepareStatement("Select ID_ZONE, ZONE_NAME, ZONE_DESCRIPTION, ZONE_COLOR, ZONE_ORDER, ID_PAGE, ID_TIPOEDICION, ORDER_DEFAULT, SIZE_DEFAULT, ZONE_VISIBILITY from TFS_ZONE where ID_TIPOEDICION=? AND ID_PAGE=?");
			stmt.setInt(1, TipoEdicionId);
			stmt.setInt(2, PageId);

			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				Zona zona = fillZona(rs);
				zonas.add(zona);
			}

			rs.close();
			stmt.close();

		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}

		return zonas;
	}
	
	public List<Zona> getZonas(int TipoEdicionId) throws Exception {
		List<Zona> zonas = new ArrayList<Zona>();

		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn
					.prepareStatement("Select ID_ZONE, ZONE_NAME, ZONE_DESCRIPTION, ZONE_COLOR, ZONE_ORDER, ID_PAGE, ID_TIPOEDICION, ORDER_DEFAULT, SIZE_DEFAULT, ZONE_VISIBILITY from TFS_ZONE where ID_TIPOEDICION=?");
			stmt.setInt(1, TipoEdicionId);

			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				Zona zona = fillZona(rs);
				zonas.add(zona);
			}

			rs.close();
			stmt.close();

		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}

		return zonas;
	}
	public List<Zona> getZonas(int TipoEdicionId, int PageId, String ZoneText) throws Exception {
		List<Zona> zonas = new ArrayList<Zona>();

		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn
					.prepareStatement("select ID_ZONE, ZONE_NAME, ZONE_DESCRIPTION, ZONE_COLOR, ZONE_ORDER, ID_PAGE, ID_TIPOEDICION, ORDER_DEFAULT, SIZE_DEFAULT, ZONE_VISIBILITY from TFS_ZONE where ID_TIPOEDICION=? AND ID_PAGE=? AND (ZONE_NAME LIKE ? OR ZONE_DESCRIPTION LIKE ? )");
			stmt.setInt(1, TipoEdicionId);
			stmt.setInt(2, PageId);
			stmt.setString(3, ZoneText);
			stmt.setString(4, ZoneText);


			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				Zona zona = fillZona(rs);
				zonas.add(zona);
			}

			rs.close();
			stmt.close();

		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}

		return zonas;
	}
	
	public List<Zona> getZonas(int TipoEdicionId, int PageId, Boolean visible) throws Exception {
		List<Zona> zonas = new ArrayList<Zona>();

		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn
					.prepareStatement("Select ID_ZONE, ZONE_NAME, ZONE_DESCRIPTION, ZONE_COLOR, ZONE_ORDER, ID_PAGE, ID_TIPOEDICION, ORDER_DEFAULT, SIZE_DEFAULT, ZONE_VISIBILITY from TFS_ZONE where ID_TIPOEDICION=? AND ID_PAGE=? AND ZONE_VISIBILITY=?");
			stmt.setInt(1, TipoEdicionId);
			stmt.setInt(2, PageId);
			stmt.setBoolean(3, visible);


			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				Zona zona = fillZona(rs);
				zonas.add(zona);
			}

			rs.close();
			stmt.close();

		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}

		return zonas;
	}
	
	
	public List<Zona> getZonas(int TipoEdicionId, String ZoneText) throws Exception {
		List<Zona> zonas = new ArrayList<Zona>();

		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn
					.prepareStatement("select ID_ZONE, ZONE_NAME, ZONE_DESCRIPTION, ZONE_COLOR, ZONE_ORDER, ID_PAGE, ID_TIPOEDICION, ORDER_DEFAULT, SIZE_DEFAULT, ZONE_VISIBILITY from TFS_ZONE where ID_TIPOEDICION=? AND (ZONE_NAME LIKE ? OR ZONE_DESCRIPTION LIKE ? )");
			stmt.setInt(1, TipoEdicionId);
			stmt.setString(3, ZoneText);
			stmt.setString(4, ZoneText);


			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				Zona zona = fillZona(rs);
				zonas.add(zona);
			}

			rs.close();
			stmt.close();

		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}

		return zonas;
	}

	public List<Zona> getZonas() throws Exception {
		List<Zona> zonas = new ArrayList<Zona>();

		try {
			if (!connectionIsOpen())
				OpenConnection();

			Statement stmt;

			stmt = conn.createStatement();
			ResultSet rs = stmt
					.executeQuery("Select  ID_ZONE, ZONE_NAME, ZONE_DESCRIPTION, ZONE_COLOR, ZONE_ORDER, ID_PAGE, ID_TIPOEDICION,  ORDER_DEFAULT, SIZE_DEFAULT, ZONE_VISIBILITY from TFS_ZONE");

			while (rs.next()) {
				Zona zona = fillZona(rs);
				zonas.add(zona);
			}

			rs.close();
			stmt.close();

		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
		return zonas;
	}

	public Zona getZone(int TipoEdicionId, int PageId, String ZoneName) throws Exception {

		Zona zona = null;
		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;
			ResultSet rs;

			stmt = conn
					.prepareStatement("select  ID_ZONE, ZONE_NAME, ZONE_DESCRIPTION, ZONE_COLOR, ZONE_ORDER, ID_PAGE, ID_TIPOEDICION, ORDER_DEFAULT, SIZE_DEFAULT, ZONE_VISIBILITY from TFS_ZONE where ID_TIPOEDICION=? AND ID_PAGE=? AND ZONE_NAME=? ");
			stmt.setInt(1, TipoEdicionId);
			stmt.setInt(2, PageId);
			stmt.setString(3, ZoneName);

			rs = stmt.executeQuery();

			rs.next();

			zona = fillZona(rs);

			rs.close();
			stmt.close();

		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}

		return zona;
	}
	
	public Zona getZona(int zonaId) throws Exception {

		Zona zona = null;
		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;
			ResultSet rs;

			stmt = conn
					.prepareStatement("select  ID_ZONE, ZONE_NAME, ZONE_DESCRIPTION, ZONE_COLOR, ZONE_ORDER, ID_PAGE, ID_TIPOEDICION, ORDER_DEFAULT, SIZE_DEFAULT, ZONE_VISIBILITY from TFS_ZONE where ID_ZONE=?");
			stmt.setInt(1, zonaId);

			rs = stmt.executeQuery();

			rs.next();

			zona = fillZona(rs);

			rs.close();
			stmt.close();

		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}

		return zona;
	}

	public void insertZona(Zona zona) throws Exception {

		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn
					.prepareStatement("insert into TFS_ZONE (ZONE_NAME, ZONE_DESCRIPTION, ZONE_COLOR, ZONE_ORDER, ID_PAGE, ID_TIPOEDICION,  ORDER_DEFAULT, SIZE_DEFAULT, ZONE_VISIBILITY) values (?,?,?,?,?,?,?,?,?)");
			stmt.setString(1, zona.getName());
			stmt.setString(2, zona.getDescription());
			stmt.setString(3, zona.getColor());
			stmt.setInt(4, zona.getOrder());
			stmt.setInt(5, zona.getIdPage());
			stmt.setInt(6, zona.getIdTipoEdicion());
			stmt.setString(7,zona.getOrderDefault());
			stmt.setInt(8, zona.getSizeDefault());
			stmt.setBoolean(9, zona.getVisibility());

			stmt.executeUpdate();

			stmt.close();

		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
	}

	public void updateZona(Zona zona) throws Exception {
		try {

			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn
					.prepareStatement("update TFS_ZONE set ZONE_NAME=?, ZONE_DESCRIPTION=?, ZONE_COLOR=?, ZONE_ORDER=?, ID_PAGE=?, ID_TIPOEDICION=?,  ORDER_DEFAULT=?, SIZE_DEFAULT=?, ZONE_VISIBILITY=? where ID_ZONE=?");
			stmt.setString(1, zona.getName());
			stmt.setString(2, zona.getDescription());
			stmt.setString(3, zona.getColor());
			stmt.setInt(4, zona.getOrder());
			stmt.setInt(5, zona.getIdPage());
			stmt.setInt(6, zona.getIdTipoEdicion());
			stmt.setString(7,zona.getOrderDefault());
			stmt.setInt(8, zona.getSizeDefault());
			stmt.setBoolean(9, zona.getVisibility());
			stmt.setInt(10, zona.getIdZone());
			
			stmt.executeUpdate();

			stmt.close();

		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
	}

	public void deleteZona(int zonaId) throws Exception {

		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn
					.prepareStatement("delete from TFS_ZONE where ID_ZONE=?");
			stmt.setInt(1, zonaId);

			stmt.execute();

			stmt.close();

		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
	}
	
	public void actualizarOrder(int zonaId, int order) throws Exception {

		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn
					.prepareStatement("update TFS_ZONE set ZONE_ORDER = ? where ID_ZONE=?");
			stmt.setInt(1, order);
			stmt.setInt(2, zonaId);

			stmt.execute();

			stmt.close();

		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
	}

	private Zona fillZona(ResultSet rs) throws SQLException {
		Zona zona = new Zona();

		zona.setIdZone(rs.getInt("ID_ZONE"));
		zona.setName(rs.getString("ZONE_NAME"));
		zona.setDescription(rs.getString("ZONE_DESCRIPTION"));
		zona.setOrder(rs.getInt("ZONE_ORDER"));
		zona.setColor(rs.getString("ZONE_COLOR"));
		zona.setIdPage(rs.getInt("ID_PAGE"));
		zona.setIdTipoEdicion(rs.getInt("ID_TIPOEDICION"));
		zona.setOrderDefault(rs.getString("ORDER_DEFAULT"));
		zona.setSizeDefault(rs.getInt("SIZE_DEFAULT"));
		zona.setVisibility(rs.getBoolean("ZONE_VISIBILITY"));
		
		return zona;

	}

}