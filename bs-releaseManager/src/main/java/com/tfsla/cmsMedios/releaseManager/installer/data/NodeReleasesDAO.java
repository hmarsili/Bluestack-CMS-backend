package com.tfsla.cmsMedios.releaseManager.installer.data;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.tfsla.cmsMedios.releaseManager.installer.common.ClusterNode;
import com.tfsla.cmsMedios.releaseManager.installer.common.ClusterNodeHistory;
import com.tfsla.cmsMedios.releaseManager.installer.common.DeployMessage;
import com.tfsla.cmsMedios.releaseManager.installer.common.ExceptionMessages;
import com.tfsla.cmsMedios.releaseManager.installer.common.SqlQueries;
import com.tfsla.cmsMedios.releaseManager.installer.service.DeployMessageSerializer;

public class NodeReleasesDAO extends BaseDAO {

	public ClusterNode getNodeByIP(String ip) throws SQLException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		ClusterNode ret = null;
		try {
			stmt = conn.prepareStatement(SqlQueries.GET_NODE_BY_IP);
			stmt.setString(1, ip);
			
			rs = stmt.executeQuery();
			
			while (rs.next()) {
				ret = new ClusterNode();
				ret.setID(rs.getInt("ID"));
				ret.setIP(ip);
				ret.setIsWP(rs.getInt("IS_WP") == 1);
				ret.setName(rs.getString("NAME"));
				ret.setRM(rs.getString("RM"));
				ret.setNeedsConfiguration(rs.getInt("NEEDS_CONFIGURATION") == 1);
				ret.setManifest(rs.getString("MANIFEST"));
				ret.setReadme(rs.getString("README"));
				byte[] messages = rs.getBytes("MESSAGES");
				if(messages == null) {
					ret.setMessages(new ArrayList<DeployMessage>());
				} else {
					ret.setMessages(DeployMessageSerializer.deserialize(rs.getBytes("MESSAGES")));
				}
			}
			return ret;
		} catch(SQLException ex) {
			throw ex;
		} finally {
			try {
				stmt.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
			try {
				rs.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void updateNode(ClusterNode node) throws SQLException, FileNotFoundException, IOException {
		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(SqlQueries.UPDATE_NODE);
			stmt.setString(1, node.getIP());
			stmt.setString(2, node.getName());
			stmt.setString(3, node.getRM());
			stmt.setInt(4, node.getIsWP() ? 1 : 0);
			stmt.setInt(5, node.getNeedsConfiguration() ? 1 : 0);
			stmt.setString(6, node.getManifest());
			stmt.setString(7, node.getReadme());
			stmt.setBytes(8, DeployMessageSerializer.serialize(node.getMessages()));
			stmt.setInt(9, node.getID());
			
			int affectedRows = stmt.executeUpdate();
			if (affectedRows == 0) {
				String additionalMessage = "";
				if(stmt.getWarnings() != null) additionalMessage = stmt.getWarnings().getMessage();
	            throw new SQLException(String.format(ExceptionMessages.ERROR_UPDATING_RECORD, additionalMessage, SqlQueries.UPDATE_NODE));
	        }
		} catch(SQLException ex) {
			throw ex;
		} finally {
			try {
				stmt.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void addNodeHistory(ClusterNodeHistory nodeHistory) throws SQLException {
		PreparedStatement stmt = null;
		try {
			java.sql.Timestamp date = new java.sql.Timestamp(nodeHistory.getDate().getTime());
			stmt = conn.prepareStatement(SqlQueries.INSERT_NODE_HISTORY);
			stmt.setInt(1, nodeHistory.getNodeID());
			stmt.setInt(2, nodeHistory.getSetupResult().getValue());
			stmt.setString(3, nodeHistory.getRM());
			stmt.setString(4, nodeHistory.getLog());
			stmt.setTimestamp(5, date);
			stmt.setString(6, nodeHistory.getManifest());
			
			int affectedRows = stmt.executeUpdate();
			if (affectedRows == 0) {
				String additionalMessage = "";
				if(stmt.getWarnings() != null) additionalMessage = stmt.getWarnings().getMessage();
	            throw new SQLException(String.format(ExceptionMessages.ERROR_INSERTING_RECORD, additionalMessage, SqlQueries.INSERT_NODE_HISTORY));
	        }
		} catch(SQLException ex) {
			throw ex;
		} finally {
			try {
				stmt.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
}
