package com.tfsla.diario.ediciones.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.tfsla.data.baseDAO;
import com.tfsla.diario.ediciones.model.Page;

public class PageDAO extends baseDAO {

	public List<Page> getPages() throws Exception {
		List<Page> pages = new ArrayList<Page>();

		try {
			if (!connectionIsOpen())
				OpenConnection();

			Statement stmt;

			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("Select ID_PAGE, PAGE_NAME from TFS_PAGE");

			while (rs.next()) {
				Page page = fillPage(rs);
				pages.add(page);
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

		return pages;
	}

	private Page fillPage(ResultSet rs) throws SQLException {
		Page page = new Page();

		page.setPageName(rs.getString("PAGE_NAME"));
		page.setIdPage(rs.getInt("ID_PAGE"));

		return page;

	}

	public Page getPage(int pageId) throws Exception {
		Page page = null;
		try {

			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;
			ResultSet rs;

			stmt = conn.prepareStatement("Select ID_PAGE, PAGE_NAME from TFS_PAGE where ID_PAGE=?");
			stmt.setInt(1,pageId);


			rs = stmt.executeQuery();

			rs.next();

			page = fillPage(rs);

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
		return page;
	}

	public Page getPage(String pageName) throws Exception {
		Page page = null;
		try {

			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;
			ResultSet rs;

			stmt = conn.prepareStatement("Select ID_PAGE, PAGE_NAME from TFS_PAGE where PAGE_NAME=?");
			stmt.setString(1,pageName);


			rs = stmt.executeQuery();

			rs.next();

			page = fillPage(rs);

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
		return page;
	}

}