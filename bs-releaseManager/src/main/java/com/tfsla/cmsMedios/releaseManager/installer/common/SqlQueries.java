package com.tfsla.cmsMedios.releaseManager.installer.common;

public class SqlQueries {
	public static final String GET_NODE_BY_IP = "SELECT * FROM TFS_CLUSTER_NODES WHERE IP_ADDRESS = ?";
	
	public static final String UPDATE_NODE = "UPDATE TFS_CLUSTER_NODES SET IP_ADDRESS = ?, NAME = ?, RM = ?, IS_WP = ?, NEEDS_CONFIGURATION = ?, MANIFEST = ?, README = ?, MESSAGES = ? WHERE ID = ?";
	
	public static final String INSERT_NODE_HISTORY = "INSERT INTO TFS_NODES_HISTORY (NODE_ID, RESULT, RM, LOG, SETUP_DATE, MANIFEST) VALUES (?, ?, ?, ?, ?, ?)";
}
