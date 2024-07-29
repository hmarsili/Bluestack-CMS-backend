package com.tfsla.diario.webservices.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.tfsla.diario.webservices.common.PushItem;
import com.tfsla.diario.webservices.common.PushStatus;
import com.tfsla.diario.webservices.common.strings.ExceptionMessages;
import com.tfsla.diario.webservices.common.strings.PushNotificationTypes;
import com.tfsla.diario.webservices.common.strings.SqlQueries;
import com.tfsla.webusersposts.core.BaseDAO;

public class PushNewsDAO extends BaseDAO {

	public List<PushItem> getAllPushItems(String site, String publication, PushStatus status, Boolean filterPublication) throws Exception {
		List<PushItem> ret = new ArrayList<PushItem>();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			if(filterPublication)
				stmt = conn.prepareStatement(SqlQueries.GET_ALL_NEWS_FROM_PUSH_TABLE_BY_PUBLICATION);
			else
				stmt = conn.prepareStatement(SqlQueries.GET_ALL_NEWS_FROM_PUSH_TABLE);
			stmt.setString(1, site);
			stmt.setInt(2, status.getValue());
			if(filterPublication)
				stmt.setString(3, publication);
			
			rs = stmt.executeQuery();
			while (rs.next()) {
				ret.add(this.getItemFromRow(site, publication, rs));
			}
		} catch(Exception ex) {
			LOG.error(ExceptionMessages.ERROR_RETRIEVING_NEWS_TO_PUSH, ex);
			throw ex;
		} finally {
			if(stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				try {
					rs.close();
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		return ret;
	}

	public List<PushItem> getAllPushItems(String site, String publication, String topic, PushStatus status) throws Exception {
		List<PushItem> ret = new ArrayList<PushItem>();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.prepareStatement(SqlQueries.GET_ALL_NEWS_FROM_PUSH_TABLE_BY_TOPIC);
			
			stmt.setString(1, topic);
			stmt.setInt(2, status.getValue());
			
			rs = stmt.executeQuery();
			while (rs.next()) {
				ret.add(this.getItemFromRow(site, publication, rs));
			}
		} catch(Exception ex) {
			LOG.error(ExceptionMessages.ERROR_RETRIEVING_NEWS_TO_PUSH, ex);
			throw ex;
		} finally {
			if(stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				try {
					rs.close();
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		return ret;
	}

	public List<PushItem> getPushedItems(String site, String publication, Boolean filterPublication) throws Exception {
		List<PushItem> ret = new ArrayList<PushItem>();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			if (filterPublication) {
				stmt = conn.prepareStatement(SqlQueries.GET_PUSHED_NEWS_FROM_PUSH_TABLE_BY_PUBLICATION);
			} else {
				stmt = conn.prepareStatement(SqlQueries.GET_PUSHED_NEWS_FROM_PUSH_TABLE);
			}
			stmt.setString(1, site);
			if (filterPublication) {
				stmt.setString(2, publication);
			}
			
			rs = stmt.executeQuery();
			while (rs.next()) {
				ret.add(this.getItemFromRow(site, publication, rs));
			}
		} catch(Exception ex) {
			LOG.error(ExceptionMessages.ERROR_RETRIEVING_NEWS_TO_PUSH, ex);
			throw ex;
		} finally {
			if(stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				try {
					rs.close();
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		return ret;
	}
	
	
	
	public List<PushItem> getPushedItems(String site, String publication, String topic, String type, String user, Date from, Date to, String text, Integer count) throws Exception {
		List<PushItem> ret = new ArrayList<PushItem>();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			
			String sql = "select * from TFS_PUSH_MOBILE where SITE = ? and STATUS <> 0 ";
			

			if (publication!=null) {
				sql += " and PUBLICATION = ? ";
			}
			
			if (topic!=null) {
				sql += " and TOPIC = ? ";
			}
			
			if (from !=null) {
				sql += " and DATE_PUSHED >= ? ";
			}
			
			if (to !=null) {
				sql += " and DATE_PUSHED <= ? ";
			}	
			
			if (user!=null) {
				sql += "and USER_NAME like ? ";
			}
			
			if (type!=null) {
				sql += " and PUSH_MODE = ? ";
			
			}
			
			if (text!=null) {
				sql += "and TITLE like ? ";
			}
			
			sql += "order by DATE_PUSHED desc ";
			sql += "limit " + (count!=null ? count : "10");
			
			stmt = conn.prepareStatement(sql);
			
			int i = 1;
			stmt.setString(i++, site);
			if (publication!=null) {
				stmt.setString(i++, publication);
			}
			if (topic!=null) {
				stmt.setString(i++, topic);
			}
			if (from !=null) {
				stmt.setTimestamp(i++, new Timestamp(from.getTime()));	
			}
			
			if (to !=null) {
				stmt.setTimestamp(i++, new Timestamp(to.getTime()));
			}
			
			if (user!=null) {
				stmt.setString(i++, "%" + user + "%");
			}
			
			if (type!=null) {
				stmt.setString(i++, type);
			}
			
			if (text!=null) {
				stmt.setString(i++, "%" + text + "%");
			}
			//LOG.error(stmt.toString());
			rs = stmt.executeQuery();
			while (rs.next()) {
				ret.add(this.getItemFromRow(site, publication, rs));
			}
		} catch(Exception ex) {
			LOG.error(ExceptionMessages.ERROR_RETRIEVING_NEWS_TO_PUSH, ex);
			throw ex;
		} finally {
			if(stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				try {
					rs.close();
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		return ret;
	}
	

	public int getPushedItemsCount(String topic, String site, String publication, String type, Date from, Date to) throws Exception {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			
			String sql = "select count(*) as PUSHED_NEWS from TFS_PUSH_MOBILE where SITE = ? and STATUS <> 0 ";
			

			if (topic!=null) {
				sql += " and TOPIC = ? ";
			}
			
			if (publication!=null) {
				sql += " and PUBLICATION = ? ";
			}
			
			if (from !=null) {
				sql += " and DATE_PUSHED >= ? ";
			}
			
			if (to !=null) {
				sql += " and DATE_PUSHED <= ? ";
			}	
			
			if (type!=null) {
				sql += " and PUSH_MODE = ? ";
			
			}
			
			
			stmt = conn.prepareStatement(sql);
			
			int i = 1;
			
			
			stmt.setString(i++, site);

			if (topic!=null) {
				stmt.setString(i++, topic);
			}
			
			if (publication!=null) {
				stmt.setString(i++, publication);
			}
			if (from !=null) {
				stmt.setTimestamp(i++, new Timestamp(from.getTime()));	
			}
			
			if (to !=null) {
				stmt.setTimestamp(i++, new Timestamp(to.getTime()));
			}
			
			if (type!=null) {
				stmt.setString(i++, type);
			}
			
			rs = stmt.executeQuery();
			if (rs.next()) {
				return rs.getInt("PUSHED_NEWS");
			}

		} catch(Exception ex) {
			LOG.error(ExceptionMessages.ERROR_RETRIEVING_NEWS_TO_PUSH, ex);
			throw ex;
		} finally {
			if(stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				try {
					rs.close();
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		return 0;
	}
	
	public PushItem getItemToPush(int pushId) throws Exception {
		PushItem pItem = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.prepareStatement(SqlQueries.GET_NEWS_FROM_TOPIC_BY_ID);
			stmt.setInt(1, pushId);
			
			rs = stmt.executeQuery();
			if (rs.next()) {
				pItem = this.getItemFromRow(rs);
			}
		} catch(Exception ex) {
			LOG.error(ExceptionMessages.ERROR_RETRIEVING_NEWS_TO_PUSH, ex);
			throw ex;
		} finally {
			if(stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				try {
					rs.close();
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		return pItem;
		
	}
	public List<PushItem> getItemsToPush(String topicName, PushStatus status, int size) throws Exception {
		List<PushItem> ret = new ArrayList<PushItem>();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			if(size > 0) {
				stmt = conn.prepareStatement(SqlQueries.GET_NEWS_FROM_TOPIC_LIMITED);
			} else {
				stmt = conn.prepareStatement(SqlQueries.GET_NEWS_FROM_TOPIC);
			}
			stmt.setString(1, topicName);
			stmt.setInt(2, status.getValue());
			stmt.setString(3, PushNotificationTypes.EN_COLA);
			if(size > 0) {
				stmt.setInt(4, size);
			}
			
			//LOG.error("getItemsToPush:" + stmt.toString());
			rs = stmt.executeQuery();
			while (rs.next()) {
				ret.add(this.getItemFromRow(rs));
			}
		} catch(Exception ex) {
			LOG.error(ExceptionMessages.ERROR_RETRIEVING_NEWS_TO_PUSH, ex);
			throw ex;
		} finally {
			if(stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				try {
					rs.close();
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		return ret;
	}

	@Deprecated
	public List<PushItem> getItemsToPush(String site, String publication, PushStatus status, int size) throws Exception {
		List<PushItem> ret = new ArrayList<PushItem>();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			if(size > 0) {
				stmt = conn.prepareStatement(SqlQueries.GET_NEWS_FROM_PUSH_TABLE_LIMITED);
			} else {
				stmt = conn.prepareStatement(SqlQueries.GET_NEWS_FROM_PUSH_TABLE);
			}
			stmt.setString(1, site);
			stmt.setString(2, publication);
			stmt.setInt(3, status.getValue());
			stmt.setString(4, PushNotificationTypes.EN_COLA);
			if(size > 0) {
				stmt.setInt(5, size);
			}
			rs = stmt.executeQuery();
			while (rs.next()) {
				ret.add(this.getItemFromRow(site, publication, rs));
			}
		} catch(Exception ex) {
			LOG.error(ExceptionMessages.ERROR_RETRIEVING_NEWS_TO_PUSH, ex);
			throw ex;
		} finally {
			if(stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				try {
					rs.close();
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		return ret;
	}

	public void updatePriority(int pushId, int priority) throws Exception {
		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(SqlQueries.UPDATE_PUSH_PRIORITY);
			stmt.setInt(1, priority);
			stmt.setInt(2, pushId);
			
			int affectedRows = stmt.executeUpdate();
			if (affectedRows == 0) {
	            throw new SQLException(ExceptionMessages.ERROR_UPDATING_PRIORITY);
	        }
		} catch(Exception ex) {
			LOG.error(ExceptionMessages.ERROR_UPDATING_PRIORITY, ex);
			throw ex;
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
	
	public void removePush(String pushId) throws Exception {
		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(SqlQueries.DELETE_PUSH);
			stmt.setString(1, pushId);
			
			int affectedRows = stmt.executeUpdate();
			if (affectedRows == 0) {
	            throw new SQLException(String.format(ExceptionMessages.ERROR_REMOVING_PUSH, pushId));
	        }
		} catch(Exception ex) {
			ex.printStackTrace();
			LOG.error(String.format(ExceptionMessages.ERROR_REMOVING_PUSH, pushId));
			throw ex;
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

	public void setPushed(int pushId, PushStatus status, String info) throws Exception {
		PreparedStatement stmt = null;
		try {
			java.sql.Timestamp date = new java.sql.Timestamp(new Date().getTime());
			stmt = conn.prepareStatement(SqlQueries.SET_PUSHED_NOTIFICATION);
			stmt.setInt(1, status.getValue());
			stmt.setTimestamp(2, date);
			stmt.setString(3, info);
			stmt.setInt(4, pushId);
			
			int affectedRows = stmt.executeUpdate();
			if (affectedRows == 0) {
	            throw new SQLException(ExceptionMessages.ERROR_SETTING_PUSHED_NEW);
	        }
		} catch(Exception ex) {
			ex.printStackTrace();
			LOG.error(ExceptionMessages.ERROR_SETTING_PUSHED_NEW, ex);
			throw ex;
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

	
	@Deprecated
	public void setPushed(String structureId, String site, String publication, String pushMode, PushStatus status, String info) throws Exception {
		PreparedStatement stmt = null;
		try {
			java.sql.Timestamp date = new java.sql.Timestamp(new Date().getTime());
			stmt = conn.prepareStatement(SqlQueries.SET_PUSHED_NEW);
			stmt.setInt(1, status.getValue());
			stmt.setTimestamp(2, date);
			stmt.setString(3, info);
			stmt.setString(4, structureId);
			
			int affectedRows = stmt.executeUpdate();
			if (affectedRows == 0) {
	            throw new SQLException(ExceptionMessages.ERROR_SETTING_PUSHED_NEW);
	        }
		} catch(Exception ex) {
			ex.printStackTrace();
			LOG.error(ExceptionMessages.ERROR_SETTING_PUSHED_NEW, ex);
			throw ex;
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
	
	@Deprecated
	public String addPushedNew(String id, String site, String publication, String pushMode, String userName, String title, Date scheduledDate, String jobName) throws Exception {
		PreparedStatement stmt = null;
		ResultSet generatedKeys = null;
		try {
			java.sql.Timestamp date = new java.sql.Timestamp(new Date().getTime());
			if(scheduledDate != null) {
				stmt = conn.prepareStatement(SqlQueries.INSERT_PUSHED_NEW_WITH_SCHEDULE, Statement.RETURN_GENERATED_KEYS);
			} else {
				stmt = conn.prepareStatement(SqlQueries.INSERT_PUSHED_NEW, Statement.RETURN_GENERATED_KEYS);
			}
			stmt.setString(1, id);
			stmt.setString(2, site);
			stmt.setString(3, publication);
			stmt.setString(4, pushMode);
			stmt.setTimestamp(5, date);
			stmt.setString(6, userName);
			stmt.setString(7, title);
			if(scheduledDate != null) {
				java.sql.Timestamp scheduledTime = new java.sql.Timestamp(scheduledDate.getTime());
				stmt.setTimestamp(8, scheduledTime);
				stmt.setString(9, jobName);
			}
			
			int affectedRows = stmt.executeUpdate();
			if (affectedRows == 0) {
	            throw new SQLException(ExceptionMessages.ERROR_ADDING_PUSHED_NEW);
	        }
			generatedKeys = stmt.getGeneratedKeys();
			if (generatedKeys.next()) {
	        	return generatedKeys.getString(1);
	        }
			return null;
		} catch(Exception ex) {
			ex.printStackTrace();
			LOG.error(ExceptionMessages.ERROR_ADDING_PUSHED_NEW, ex);
			throw ex;
		} finally {
			if(generatedKeys != null) {
				try {
					generatedKeys.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if(stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public String addPushedNew(String id, String pushMode, String userName, String title, String subtitle, String url, Date scheduledDate, String  topic, String site, String publication, String jobName, String image) throws Exception {
		PreparedStatement stmt = null;
		ResultSet generatedKeys = null;
		try {
			java.sql.Timestamp date = new java.sql.Timestamp(new Date().getTime());
			if(scheduledDate != null) {
				stmt = conn.prepareStatement(SqlQueries.INSERT_IN_TOPIC_PUSHED_NEW_WITH_SCHEDULE, Statement.RETURN_GENERATED_KEYS);
			} else {
				stmt = conn.prepareStatement(SqlQueries.INSERT_IN_TOPIC_PUSHED_NEW, Statement.RETURN_GENERATED_KEYS);
			}
			
			stmt.setString(1, id);
			stmt.setString(2, topic);
			stmt.setString(3, site);
			stmt.setString(4, publication);
			stmt.setString(5, pushMode);
			stmt.setTimestamp(6, date);
			stmt.setString(7, userName);
			stmt.setString(8, title);
			stmt.setString(9, subtitle);
			stmt.setString(10, url);
			stmt.setString(11, image);
			
			if(scheduledDate != null) {
				java.sql.Timestamp scheduledTime = new java.sql.Timestamp(scheduledDate.getTime());
				stmt.setTimestamp(12, scheduledTime);
				stmt.setString(13, jobName);
			}
			
			int affectedRows = stmt.executeUpdate();
			if (affectedRows == 0) {
	            throw new SQLException(ExceptionMessages.ERROR_ADDING_PUSHED_NEW);
	        }
			generatedKeys = stmt.getGeneratedKeys();
			if (generatedKeys.next()) {
	        	return generatedKeys.getString(1);
	        }
			return null;
		} catch(Exception ex) {
			ex.printStackTrace();
			LOG.error(ExceptionMessages.ERROR_ADDING_PUSHED_NEW, ex);
			throw ex;
		} finally {
			if(generatedKeys != null) {
				try {
					generatedKeys.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if(stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public String addPushedNewWithTopic(String resourceId, String pushMode, String title, String subtitle, String url, String userName, String topic, String site, String publication, String image) throws Exception {
		return this.addPushedNew(resourceId, pushMode, userName, title, subtitle, url, null, topic, site, publication, null, image);
	}
	
	@Deprecated
	public String addPushedNew(String id, String site, String publication, String pushMode, String title, String userName) throws Exception {
		return this.addPushedNew(id, site, publication, pushMode, userName, title, null, null);
	}
	
	public String copyPushItem(String pushId, String userName) throws Exception {
		PreparedStatement stmt = null;
		ResultSet generatedKeys = null;
		try {
			stmt = conn.prepareStatement(SqlQueries.COPY_PUSHED_NEW, Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, userName);
			stmt.setString(2, pushId);
			int affectedRows = stmt.executeUpdate();
			if (affectedRows == 0) {
	            throw new SQLException(ExceptionMessages.ERROR_ADDING_PUSHED_NEW);
	        }
			generatedKeys = stmt.getGeneratedKeys();
	        if (generatedKeys.next()) {
	        	return generatedKeys.getString(1);
	        }
	        throw new Exception(ExceptionMessages.ERROR_RETRIEVING_GENERATED_KEYS);
		} catch(Exception ex) {
			ex.printStackTrace();
			LOG.error(ExceptionMessages.ERROR_COPYING_PUSHED_NEW, ex);
			throw ex;
		} finally {
			if(generatedKeys != null) {
				try {
					generatedKeys.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if(stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public Boolean isPushed(String id) throws Exception {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.prepareStatement(SqlQueries.GET_PUSH_ID);
			stmt.setString(1, id);
			rs = stmt.executeQuery();
			return rs.next();
		} catch(Exception ex) {
			ex.printStackTrace();
			LOG.error(ExceptionMessages.ERROR_CHECKING_NEW, ex);
			throw ex;
		} finally {
			try {
				if(stmt != null) stmt.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
			try {
				if(rs != null) rs.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public Boolean isPushed(String id, String site, String publication) throws Exception {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.prepareStatement(SqlQueries.GET_NEW_ID);
			stmt.setString(1, id);
			stmt.setString(2, site);
			stmt.setString(3, publication);
			rs = stmt.executeQuery();
			return rs.next();
		} catch(Exception ex) {
			ex.printStackTrace();
			LOG.error(ExceptionMessages.ERROR_CHECKING_NEW, ex);
			throw ex;
		} finally {
			try {
				if(stmt != null) stmt.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
			try {
				if(rs != null) rs.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public PushItem getPushItem(String pushId) throws Exception {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.prepareStatement(SqlQueries.GET_PUSH);
			stmt.setString(1, pushId);
			rs = stmt.executeQuery();
			if (rs.next()) {
				return this.getItemFromRow(rs);
			}
			return null;
		} catch(Exception ex) {
			ex.printStackTrace();
			LOG.error(ExceptionMessages.ERROR_RETRIEVING_NEWS_TO_PUSH, ex);
			throw ex;
		} finally {
			try {
				if(stmt != null) stmt.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
			try {
				if(rs != null) rs.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private PushItem getItemFromRow(ResultSet rs) throws SQLException {
		return this.getItemFromRow(null, null, rs);
	}
	
	private PushItem getItemFromRow(String site, String publication, ResultSet rs) throws SQLException {
		PushItem item = new PushItem();
		if(site == null) {
			item.setSite(rs.getString("SITE"));
		} else {
			item.setSite(site);
		}
		if(rs.getInt("PUBLICATION") > 0) {
			item.setPublication(rs.getInt("PUBLICATION"));
		} else {
			item.setPublication(Integer.valueOf(publication));
		}
		
		item.setTopic(rs.getString("TOPIC"));
		
		item.setPushType(rs.getString("PUSH_MODE"));
		item.setStructureId(rs.getString("STRUCTURE_ID"));
		item.setInfo(rs.getString("INFO"));
		item.setPriority(rs.getInt("PRIORITY"));
		item.setId(rs.getInt("REGISTER_ID"));
		item.setStatus(PushStatus.values()[rs.getInt("STATUS")]);
		item.setDateCreated(new Date(rs.getTimestamp("DATE_CREATED").getTime()));
		item.setUserName(rs.getString("USER_NAME"));
		item.setJobName(rs.getString("JOB_NAME"));
		item.setTitle(rs.getString("TITLE"));
		item.setSubTitle(rs.getString("SUBTITLE"));
		item.setUrl(rs.getString("URL"));
		item.setImage(rs.getString("IMAGE"));
		
		if(rs.getTimestamp("DATE_PUSHED") != null) {
			item.setDatePushed(new Date(rs.getTimestamp("DATE_PUSHED").getTime()));
		}
		if(rs.getTimestamp("PUSH_SCHEDULE") != null) {
			item.setDateScheduled(new Date(rs.getTimestamp("PUSH_SCHEDULE").getTime()));
		}
		return item;
	}
}
