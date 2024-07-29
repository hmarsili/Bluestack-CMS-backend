package com.tfsla.diario.ediciones.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.tfsla.data.baseDAO;

public class NoticiasDAO extends baseDAO {

	public boolean hasNoticiasOfflineInSection(String seccionName, String PathTipoEdicion) throws Exception
	{
		boolean hasNoticias=false;

		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn.prepareStatement(
					"SELECT S.RESOURCE_PATH " +
					" FROM CMS_OFFLINE_PROPERTIES P, CMS_OFFLINE_STRUCTURE S, CMS_OFFLINE_PROPERTYDEF D " +
					" WHERE P.PROPERTY_MAPPING_ID = S.STRUCTURE_ID " +
					" AND S.RESOURCE_PATH LIKE CONCAT(? , '%') " +
					" AND D.PROPERTYDEF_ID = P.PROPERTYDEF_ID " +
					" AND D.PROPERTYDEF_NAME = 'seccion' " +
					" AND P.PROPERTY_VALUE = ?");

			stmt.setString(1,PathTipoEdicion);
			stmt.setString(2,seccionName);

			ResultSet rs = stmt.executeQuery();

			hasNoticias = rs.next();

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

		return hasNoticias;
	}

	public List<String> getNoticiasImpresas(String path, String seccionName) throws Exception
	{
		List<String> urls = new ArrayList<String>();
		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn.prepareStatement(
					"SELECT S.RESOURCE_PATH as path" +
					" FROM CMS_OFFLINE_PROPERTIES P, CMS_OFFLINE_STRUCTURE S, CMS_OFFLINE_PROPERTYDEF D " +
					" WHERE P.PROPERTY_MAPPING_ID = S.STRUCTURE_ID " +
					" AND S.RESOURCE_PATH LIKE CONCAT(? , '%') " +
					" AND D.PROPERTYDEF_ID = P.PROPERTYDEF_ID " +
					" AND D.PROPERTYDEF_NAME = 'seccion' " +
					" AND P.PROPERTY_VALUE = ?");

			stmt.setString(1,path);
			stmt.setString(2,seccionName);

			ResultSet rs = stmt.executeQuery();

			while (rs.next())
			{
				urls.add(rs.getString("path"));
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
		return urls;
	}

	public List<String> getNoticiasImpresas(String path) throws Exception
	{

		List<String> urls = new ArrayList<String>();

		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn.prepareStatement(
					"SELECT RESOURCE_PATH as path" +
					" FROM CMS_OFFLINE_STRUCTURE S " +
					" WHERE RESOURCE_PATH LIKE CONCAT(? , '%') ");

			stmt.setString(1,path);

			ResultSet rs = stmt.executeQuery();

			while (rs.next())
			{
				urls.add(rs.getString("path"));
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

		return urls;
	}

	public boolean hasNoticiasOnflineInSection(String seccionName, String PathTipoEdicion) throws Exception
	{
		boolean hasNoticias=false;

		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn.prepareStatement(
					"SELECT S.RESOURCE_PATH " +
					" FROM CMS_ONLINE_PROPERTIES P, CMS_ONLINE_STRUCTURE S, CMS_ONLINE_PROPERTYDEF D " +
					" WHERE P.PROPERTY_MAPPING_ID = S.STRUCTURE_ID " +
					" AND S.RESOURCE_PATH LIKE CONCAT(? , '%') " +
					" AND D.PROPERTYDEF_ID = P.PROPERTYDEF_ID " +
					" AND D.PROPERTYDEF_NAME = 'seccion' " +
					" AND P.PROPERTY_VALUE = ?");

			stmt.setString(1,PathTipoEdicion);
			stmt.setString(2,seccionName);

			ResultSet rs = stmt.executeQuery();

			hasNoticias = rs.next();

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
		return hasNoticias;
	}
}
