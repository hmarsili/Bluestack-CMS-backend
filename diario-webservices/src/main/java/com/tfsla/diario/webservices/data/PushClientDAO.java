package com.tfsla.diario.webservices.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.tfsla.diario.webservices.common.strings.SqlQueries;
import com.tfsla.webusersposts.core.BaseDAO;
import com.tfsla.diario.webservices.common.strings.ExceptionMessages;

public class PushClientDAO extends BaseDAO {

	public int getClientsCount(String site, String publication, Boolean filterPublication) throws Exception {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			if(!filterPublication)
				stmt = conn.prepareStatement(SqlQueries.GET_CLIENTS_COUNT_BY_SITE);
			else
				stmt = conn.prepareStatement(SqlQueries.GET_CLIENTS_COUNT_BY_SITE_AND_PUBLICATION);
			stmt.setString(1, site);
			if(filterPublication)
				stmt.setString(2, publication);
			rs = stmt.executeQuery();
			if (rs.next()) {
				return rs.getInt("PUSH_CLIENTS");
			}
			return 0;
		} catch(Exception ex) {
			return 0;
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
	
	public List<String> getClients(String platform, String site, String publication, Boolean filterPublication) throws Exception {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<String> ret = new ArrayList<String>();
		try {
			if(!filterPublication)
				stmt = conn.prepareStatement(SqlQueries.GET_CLIENTS_BY_PLATFORM);
			else
				stmt = conn.prepareStatement(SqlQueries.GET_CLIENTS_BY_PLATFORM_AND_PUBLICATION);
			stmt.setString(1, platform);
			stmt.setString(2, site);
			if(filterPublication)
				stmt.setString(3, publication);
			rs = stmt.executeQuery();
			while (rs.next()) {
				ret.add(rs.getString("CLIENT_ID"));
			}
		} catch(Exception ex) {
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
		return ret;
	}
	
	public Boolean userExists(String token, String platform, String topic, String site, int publication) throws Exception {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.prepareStatement(SqlQueries.GET_USER_BY_CLIENT_ID_AND_PLATFORM);
			stmt.setString(1, token);
			stmt.setString(2, platform);
			stmt.setString(3, topic);
			stmt.setString(4, site);
			stmt.setInt(5, publication);
			rs = stmt.executeQuery();
			return rs.next();
		} catch(Exception ex) {
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

	
	public void unregisterClient(String token, String platform, String topic) {
		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(SqlQueries.REMOVE_TOPIC_PUSH_CLIENT);
			stmt.setString(1, token);
			stmt.setString(2, platform);
			stmt.setString(3, topic);
			
			
			int affectedRows = stmt.executeUpdate();
			if (affectedRows == 0) {
	            //throw new SQLException(String.format(ExceptionMessages.ERROR_REMOVING_CLIENT, token));
	        }
		} catch(Exception e) {
			LOG.error(e);
		} finally {
			if(stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public void unregisterClient(String token, String platform) {
		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(SqlQueries.REMOVE_PUSH_CLIENT);
			stmt.setString(1, token);
			stmt.setString(2, platform);
			
			
			int affectedRows = stmt.executeUpdate();
			if (affectedRows == 0) {
	            //throw new SQLException(String.format(ExceptionMessages.ERROR_REMOVING_CLIENT, token));
	        }
		} catch(Exception e) {
			LOG.error(e);
		} finally {
			if(stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public void unregisterEndpoint(String endpoint) {
		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(SqlQueries.REMOVE_ENDPOINT);
			stmt.setString(1, endpoint);
			
			int affectedRows = stmt.executeUpdate();
			if (affectedRows == 0) {
	            //throw new SQLException(String.format(ExceptionMessages.ERROR_REMOVING_ENDPOINT, endpoint));
	        }
		} catch(Exception e) {
			LOG.error(e);
		} finally {
			if(stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	
	public List<String> getTopicFromClient(String token, String platform, String site, String publication, Boolean filterPublication) {
		List<String> topics = new ArrayList<String>();
		
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			
			if(!filterPublication)
				stmt = conn.prepareStatement(SqlQueries.GET_TOPICS_FROM_CLIENTS_AND_PLATFORM);
			else
				stmt = conn.prepareStatement(SqlQueries.GET_TOPICS_FROM_CLIENTS_AND_PLATFORM_AND_PUBLICATION);
			
			stmt.setString(1, token);
			stmt.setString(2, platform);
			stmt.setString(3, site);
			if(filterPublication)
				stmt.setString(4, publication);
			
			rs = stmt.executeQuery();
			while (rs.next()) {
				topics.add(rs.getString("TOPIC_ARN"));
			}
			return topics;
		} catch(Exception ex) {
			return topics;
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
	
	public void registerEndpoint(String token, String platform, String appArn, String topicArn, String site, String publication) throws Exception {
		PreparedStatement stmt = null;
		try {
			java.sql.Timestamp date = new java.sql.Timestamp(new Date().getTime());
			stmt = conn.prepareStatement(SqlQueries.INSERT_PUSH_ENDPOINT, Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, token);
			stmt.setString(2, platform);
			stmt.setTimestamp(3, date);
			stmt.setString(4, appArn);
			stmt.setString(5, topicArn);
			stmt.setString(6, site);
			stmt.setString(7, publication);
			
			int affectedRows = stmt.executeUpdate();
			if (affectedRows == 0) {
	            throw new SQLException(ExceptionMessages.ERROR_REGISTERING_ENDPOINT);
	        }
		} catch(Exception e) {
			throw e;
		} finally {
			if(stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public void updateEndpoint(String token, String platform, String appArn, String topicArn, String site, String publication) throws Exception {
		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(SqlQueries.UPDATE_PUSH_ENDPOINT);
			stmt.setString(1, appArn);
			stmt.setString(2, topicArn);
			stmt.setString(3, token);
			stmt.setString(4, site);
			stmt.setString(5, publication);
			stmt.setString(6, platform);
			
			stmt.executeUpdate();
		} catch(Exception e) {
			throw e;
		} finally {
			if(stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public void registerClient(String token, String email, String additionalInfo, String platform, String site, int publication) throws Exception {
		PreparedStatement stmt = null;
		try {
			java.sql.Timestamp date = new java.sql.Timestamp(new Date().getTime());
			stmt = conn.prepareStatement(SqlQueries.INSERT_PUSH_CLIENT, Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, token);
			stmt.setString(2, platform);
			stmt.setTimestamp(3, date);
			stmt.setString(4, additionalInfo);
			stmt.setString(5, email);
			stmt.setString(6, site);
			stmt.setInt(7, publication);
			
			int affectedRows = stmt.executeUpdate();
			if (affectedRows == 0) {
	            throw new SQLException(ExceptionMessages.ERROR_REGISTERING_CLIENT);
	        }
		} catch(Exception e) {
			throw e;
		} finally {
			if(stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public int getClientsCount(String site, String publication, boolean filterPublication, Date from, Date to) {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			if(!filterPublication)
				stmt = conn.prepareStatement(SqlQueries.GET_CLIENTS_COUNT_BY_SITE_AND_DATE);
			else
				stmt = conn.prepareStatement(SqlQueries.GET_CLIENTS_COUNT_BY_SITE_AND_PUBLICATION_AND_DATE);
			int i=1;
			stmt.setString(i++, site);
			if(filterPublication)
				stmt.setString(i++, publication);
			
			stmt.setTimestamp(i++, new Timestamp(from.getTime()));
			stmt.setTimestamp(i++, new Timestamp(to.getTime()));
			rs = stmt.executeQuery();
			if (rs.next()) {
				return rs.getInt("PUSH_CLIENTS");
			}
			return 0;
		} catch(Exception ex) {
			return 0;
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
	
	
	public int getClientsCount(String topic, String site, String publication, boolean filterPublication, Date from, Date to) {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			if(!filterPublication)
				stmt = conn.prepareStatement(SqlQueries.GET_CLIENTS_COUNT_BY_TOPIC_AND_SITE_AND_DATE);
			else
				stmt = conn.prepareStatement(SqlQueries.GET_CLIENTS_COUNT_BY_TOPIC_AND_SITE_AND_PUBLICATION_AND_DATE);
			int i=1;
			stmt.setString(i++, site);
			if(filterPublication)
				stmt.setString(i++, publication);
			
			stmt.setString(i++, topic);
			stmt.setTimestamp(i++, new Timestamp(from.getTime()));
			stmt.setTimestamp(i++, new Timestamp(to.getTime()));
			rs = stmt.executeQuery();
			if (rs.next()) {
				return rs.getInt("PUSH_CLIENTS");
			}
			return 0;
		} catch(Exception ex) {
			return 0;
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

}
