package com.tfsla.diario.pines.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.tfsla.data.baseDAO;
import com.tfsla.diario.pines.model.Pin;

public class PinesDAO extends baseDAO {

	public List<Pin> getPines(String user, int publication) throws Exception {
		List<Pin> pines = new ArrayList<Pin>();

		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn.prepareStatement("Select ID_PIN, RESOURCE_PATH, USER_ID, PIN_PUBLICATION, PIN_ORDEN, RESOURCE_TYPE from TFS_PINES where "
					+ " USER_ID=? and PIN_PUBLICATION=?");

			stmt.setString(1, user);
			stmt.setInt(2, publication);
			
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				Pin pin = fillPin(rs);
				pines.add(pin);
			}

			rs.close();
			stmt.close();

		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}

		return pines;
	}
	
	public boolean existsPin(String user, int publication, String resource) throws Exception {

		Boolean pinExit = false;

		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn.prepareStatement("Select ID_PIN from TFS_PINES where USER_ID=? and PIN_PUBLICATION=? and RESOURCE_PATH=?");

			stmt.setString(1, user);
			stmt.setInt(2, publication);
			stmt.setString(3, resource);
			
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				pinExit = true;
			}

			rs.close();
			stmt.close();

		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}

		return pinExit;
	}
	
	public Pin getPin(String user, int publication, String resource) throws Exception {

		Pin pin = new Pin();

		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn.prepareStatement("Select * from TFS_PINES where USER_ID=? and PIN_PUBLICATION=? and RESOURCE_PATH=?");

			stmt.setString(1, user);
			stmt.setInt(2, publication);
			stmt.setString(3, resource);
			
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				pin = fillPin(rs);
			}

			rs.close();
			stmt.close();

		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}

		return pin;
	}

	public void insertPin(Pin pin) throws Exception {

		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn.prepareStatement("insert into TFS_PINES (RESOURCE_PATH, USER_ID, PIN_PUBLICATION, PIN_ORDEN,RESOURCE_TYPE) values (?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, pin.getResource());
			stmt.setString(2, pin.getUser());
			stmt.setInt(3, pin.getPublication());
			stmt.setInt(4, pin.getOrder());
			stmt.setInt(5, pin.getResourceType());
			stmt.executeUpdate();

			stmt.close();

		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
	}

	public void updatePin(Pin pin) throws Exception {
		try {

			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn.prepareStatement("update TFS_PINES set PIN_ORDEN=? where ID_PIN=?");
			stmt.setInt(1, pin.getOrder());
			stmt.setLong(2, pin.getId());
			
			stmt.executeUpdate();

			stmt.close();

		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
	}

	public void deletePin(int idPin) throws Exception {

		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn.prepareStatement("delete from TFS_PINES where ID_PIN=?");
			stmt.setLong(1, idPin);

			stmt.execute();

			stmt.close();

		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
	}

	private Pin fillPin(ResultSet rs) throws SQLException {
		Pin pin = new Pin();
		pin.setId(rs.getInt("ID_PIN"));
		pin.setResource(rs.getString("RESOURCE_PATH"));
		pin.setUser(rs.getString("USER_ID"));
		pin.setPublication(rs.getInt("PIN_PUBLICATION"));
		pin.setOrder(rs.getInt("PIN_ORDEN"));
		pin.setResourceType(rs.getInt("RESOURCE_TYPE"));
		return pin;

	}

}