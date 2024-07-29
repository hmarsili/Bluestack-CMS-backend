package com.tfsla.diario.ediciones.data;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;

import com.tfsla.data.baseDAO;
import com.tfsla.diario.ediciones.model.Project;

public class ProjectDAO  extends baseDAO {

	public List<Project> getProyectos() throws Exception {
		List<Project> proyectos = new ArrayList<Project>();


		try {
			if (!connectionIsOpen())
				OpenConnection();

			Statement stmt;

			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("Select ID_PROJECT, PROJECT_NAME from TFS_PROJECT");

			while (rs.next()) {
				Project proyect = fillProject(rs);
				proyectos.add(proyect);
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

		return proyectos;
	}

	private Project fillProject(ResultSet rs) throws SQLException {
		Project proyect = new Project();

		proyect.setIdProject(rs.getInt("ID_PROJECT"));
		proyect.setProjectName(rs.getString("PROJECT_NAME"));

		return proyect;

	}

	public Project getProject(int projectId) throws Exception {
		Project project=null;
		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;
			ResultSet rs;

			stmt = conn.prepareStatement("select ID_PROJECT, PROJECT_NAME from TFS_PROJECT where ID_PROJECT=?");
			stmt.setInt(1,projectId);


			rs = stmt.executeQuery();

			rs.next();

			project = fillProject(rs);

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
		return project;
	}

	public Project getProjectByName(String projectName) throws Exception {
		Project proyect=null;
		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;
			ResultSet rs;

			stmt = conn.prepareStatement("select ID_PROJECT, PROJECT_NAME from TFS_PROJECT where PROJECT_NAME=?");
			stmt.setString(1,projectName);


			rs = stmt.executeQuery();

			if (rs.next())
				proyect = fillProject(rs);

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
		return proyect;
	}

}
