package com.tfsla.diario.terminos.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.tfsla.data.baseDAO;
import com.tfsla.diario.terminos.model.TermsTypes;

public class TermsTypesDAO extends baseDAO {

	public List<TermsTypes> getTermsTypes() throws Exception {
		List<TermsTypes> termstypes = new ArrayList<TermsTypes>();

		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn.prepareStatement("Select ID_TERMS_TYPE, DESCRIPTION from tfs_terms_types ");
			

			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				TermsTypes termtype = fillTermType(rs);
				termstypes.add(termtype);
			}

			rs.close();
			stmt.close();

		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}

		return termstypes;
	}

	

	public TermsTypes getTermType(String description) throws Exception {

		TermsTypes termtype = null;
		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;
			ResultSet rs;

			stmt = conn.prepareStatement("Select ID_TERMS_TYPE, DESCRIPTION from tfs_terms_types where DESCRIPTION=? ");
			stmt.setString(1, description);

			rs = stmt.executeQuery();

			if (rs.next()) {
				termtype = fillTermType(rs);
			}
			
			rs.close();
			stmt.close();

		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}

		return termtype;
	}
	
	public TermsTypes getTermTypes(Long termtypeId) throws Exception {

		TermsTypes termtype = null;
		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;
			ResultSet rs;

			stmt = conn.prepareStatement("select  ID_TERMS_TYPE, DESCRIPTION from tfs_terms_types where ID_TERMS_TYPE=?");
			stmt.setLong(1, termtypeId);

			rs = stmt.executeQuery();

			if (rs.next()) {
				termtype = fillTermType(rs);
			}
			
			rs.close();
			stmt.close();

		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}

		return termtype;
	}

	public void insertTermType(TermsTypes termtype) throws Exception {

		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn.prepareStatement("insert into tfs_terms_types (DESCRIPTION) values (?)",Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, termtype.getDescription());
			stmt.executeUpdate();

			stmt.close();

		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
	}

	public void updateTermType(TermsTypes termtype) throws Exception {
		try {

			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn.prepareStatement("update tfs_terms_types set DESCRIPTION=? where ID_TERMS_TYPE=?");
			stmt.setString(1, termtype.getDescription());
			stmt.setLong(2, termtype.getId_termType());
			
			stmt.executeUpdate();

			stmt.close();

		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
	}

	public void deleteTermType(Long IdtermType) throws Exception {

		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn.prepareStatement("delete from tfs_terms_types where ID_TERMS_TYPE=?");
			stmt.setLong(1, IdtermType);

			stmt.execute();

			stmt.close();

		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
	}

	private TermsTypes fillTermType(ResultSet rs) throws SQLException {
		TermsTypes termtype = new TermsTypes();
		termtype.setId_termType(rs.getLong("ID_TERMS_TYPE"));
		termtype.setDescription(rs.getString("DESCRIPTION"));
		return termtype;

	}

}