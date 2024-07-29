package com.tfsla.webusersposts.dataaccess;

import java.io.UnsupportedEncodingException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import com.tfsla.webusersposts.common.PostDetails;
import com.tfsla.webusersposts.common.PostStatus;
import com.tfsla.webusersposts.common.UserPost;
import com.tfsla.webusersposts.core.BaseDAO;
import com.tfsla.webusersposts.strings.ExceptionMessages;
import com.tfsla.webusersposts.strings.SqlQueries;

/**
 * Manages posts into the database, allowing insertion and updates 
 */
public class PostsDAO extends BaseDAO {
	
	private static final String PD_ID = "ID_EVENT";
	private static final String PD_FECHA = "LASTMODIFIED";
	private static final String PD_PATH = "TARGETID";
	private static final String PD_USUARIO = "USERNAME";
	private static final String PD_DESCRIPCION = "DESCRIPTION";
	private static final String PD_SITIO = "SITIO";
	
	/**
	 * Saves a user post into the database as a draft
	 * @param userPost UserPost instance representing the post
	 * @return the post with the data updated
	 * @throws Exception if any error was detected during the SQL insertion
	 */
	public UserPost addPost(UserPost userPost) throws Exception {
		PreparedStatement stmt = null;
		Exception exception = null;
		
		try {
			stmt = conn.prepareStatement(SqlQueries.INSERT_POST, Statement.RETURN_GENERATED_KEYS);
			userPost.setCreationDate(new Date());
			if(userPost.getStatus() != PostStatus.PENDING_USER)
				userPost.setStatus(PostStatus.DRAFT);
			java.sql.Timestamp date = new java.sql.Timestamp(userPost.getCreationDate().getTime());
			
			stmt.setString(1, userPost.getXmlContent());
			stmt.setString(2, userPost.getUserId());
			stmt.setString(3, userPost.getTitle());
			stmt.setTimestamp(4, date);
			stmt.setTimestamp(5, date);
			stmt.setInt(6, userPost.getPublication());
			stmt.setString(7, userPost.getSite());
			stmt.setString(8, userPost.getSocialNetworks());
			stmt.setString(9, userPost.getId());
			stmt.setInt(10, userPost.getStatus().getValue());
			stmt.setString(11, userPost.getUrl());

			int affectedRows = stmt.executeUpdate();
			
			if (affectedRows == 0) {
				String additionalMessage = "";
				if(stmt.getWarnings() != null) additionalMessage = stmt.getWarnings().getMessage();
	            throw new SQLException(ExceptionMessages.ERROR_ADDING_POST + " " + additionalMessage);
	        }
		} catch(Exception e) {
			exception = e;
		} finally {
			if(stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					exception = e;
				}
			}
		}
		
		if(exception != null) throw exception;
		
		return userPost;
	}
	
	/**
	 * Updates a user post into the database
	 * @param userPost UserPost instance representing the post
	 * @return the post with the data updated
	 * @throws Exception if any error was detected during the SQL insertion
	 */
	public UserPost updatePost(UserPost userPost) throws Exception {
		PreparedStatement stmt = null;
		Exception exception = null;
		
		try {
			stmt = conn.prepareStatement(SqlQueries.UPDATE_POST);
			userPost.setCreationDate(new Date());
			userPost.setStatus(PostStatus.DRAFT);
			java.sql.Timestamp date = new java.sql.Timestamp(userPost.getCreationDate().getTime());
			
			stmt.setString(1, userPost.getXmlContent());
			stmt.setString(2, userPost.getUserId());
			stmt.setString(3, userPost.getTitle());
			stmt.setTimestamp(4, date);
			stmt.setInt(5, userPost.getPublication());
			stmt.setString(6, userPost.getSite());
			stmt.setString(7, userPost.getSocialNetworks());
			stmt.setInt(8, userPost.getStatus().getValue());
			stmt.setString(9, userPost.getId());

			int affectedRows = stmt.executeUpdate();
			
			if (affectedRows == 0) {
				String additionalMessage = "";
				if(stmt.getWarnings() != null) additionalMessage = stmt.getWarnings().getMessage();
	            throw new SQLException(
            		String.format(ExceptionMessages.ERROR_UPDATING_POST, userPost.getId()) 
            		+ " " + additionalMessage
        		);
	        }
		} catch(Exception e) {
			exception = e;
		} finally {
			if(stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					exception = e;
				}
			}
		}
		
		if(exception != null) throw exception;
		
		return userPost;
	}
	
	/**
	 * Updates user posts by setting their status as pending when
	 * there are posts pending since the user was not active when created them
	 * @param userId the posts owner ID
	 * @return returns the count of posts updated
	 * @throws Exception if any error was detected during the SQL operation 
	 */
	public int updateUserPendingPosts(String userId) throws Exception {
		PreparedStatement stmt = null;
		Exception exception = null;
		int ret = 0;
		try {
			stmt = conn.prepareStatement(SqlQueries.UPDATE_PENDING_USER_POSTS_STATUS);
			stmt.setInt(1, PostStatus.PENDING.getValue());
			stmt.setInt(2, PostStatus.PENDING_USER.getValue());
			stmt.setString(3, userId);
			ret = stmt.executeUpdate();
		} catch(Exception e) {
			exception = e;
		} finally {
			if(stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					exception = e;
				}
			}
		}
		if(exception != null) throw exception;
		return ret;
	}
	
	/**
	 * Updates the post status into the database
	 * @param userPost the post to be updated
	 * @param newStatus the new status to be set
	 * @return the userPost provided instance with the status updated
	 * @throws Exception if any error was detected during the SQL operation 
	 */
	public UserPost updateStatus(UserPost userPost, PostStatus newStatus) throws Exception {
		PreparedStatement stmt = null;
		Exception exception = null;

		try {
			java.sql.Timestamp date = new java.sql.Timestamp(new Date().getTime());
			
			stmt = conn.prepareStatement(SqlQueries.UPDATE_POST_STATUS);
			stmt.setInt(1, newStatus.getValue());
			stmt.setTimestamp(2, date);
			stmt.setString(3, userPost.getUrl());
			stmt.setString(4, userPost.getModerationMessage());
			stmt.setString(5, userPost.getId());

			int affectedRows = stmt.executeUpdate();
			if (affectedRows == 0) {
				String additionalMessage = "";
				if(stmt.getWarnings() != null) additionalMessage = stmt.getWarnings().getMessage();
	            throw new SQLException(ExceptionMessages.ERROR_UPDATING_POST_STATUS + " " + additionalMessage);
	        }
			
			userPost.setStatus(newStatus);
		} catch(Exception e) {
			exception = e;
		} finally {
			if(stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					exception = e;
				}
			}
		}
		
		if(exception != null) throw exception;
		return userPost;
	}

	public void deleteDraft(String draftId) throws Exception {
		PreparedStatement stmt = null;
		Exception exception = null;

		try {
			stmt = conn.prepareStatement(SqlQueries.DELETE_DRAFT);
			stmt.setString(1, draftId);

			int affectedRows = stmt.executeUpdate();
			if (affectedRows == 0) {
				String additionalMessage = "";
				if(stmt.getWarnings() != null) additionalMessage = stmt.getWarnings().getMessage();
	            throw new SQLException(String.format(
            		ExceptionMessages.ERROR_DELETING_POST, draftId)
            		+ " " + additionalMessage
        		);
	        }
		} catch(Exception e) {
			exception = e;
		} finally {
			if(stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					exception = e;
				}
			}
		}
		
		if(exception != null) throw exception;
	}
	
	public ArrayList<UserPost> getUserPosts(String userId, String site, int publication, int countPosts, int fromPost, String additionalFilter) throws Exception {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		ArrayList<UserPost> ret = new ArrayList<UserPost>();
		Exception exception = null;
		Boolean filtered = countPosts > 0 || (site != null && !site.equals("")) || publication > 0;
		
		try {
			if(filtered) {
				String filter = "";
				int filtersCount = 2;
				if(site != null && !site.trim().equals("")) 
					filter += String.format(SqlQueries.AND_FILTER_FORMAT, "site", SqlQueries.LIKE_OP);
				if(publication > 0)
					filter += String.format(SqlQueries.AND_FILTER_FORMAT, "publication", SqlQueries.EQ_OP);
				if(countPosts == 0)
					countPosts = 10;
				
				filter += " " + additionalFilter;
				
				stmt = conn.prepareStatement(
					String.format(SqlQueries.GET_USER_POSTS_FILTERED, filter)
				);
				
				stmt.setString(1, userId);
				if(site != null && !site.trim().equals("")) {
					stmt.setString(filtersCount, site);
					filtersCount++;
				}
				if(publication > 0) {
					stmt.setInt(filtersCount, publication);
					filtersCount++;
				}
				stmt.setInt(filtersCount, fromPost);
				filtersCount++;
				stmt.setInt(filtersCount, countPosts);
			} else {
				stmt = conn.prepareStatement(SqlQueries.GET_USER_POSTS);
				stmt.setString(1, userId);
			}
			rs = stmt.executeQuery();
			
			while (rs.next()) {
				ret.add(this.getPost(rs));
			}
		} catch(Exception ex) {
			exception = ex;
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
		
		if(exception != null) throw exception;
		return ret;
	}

	public UserPost getPostByUrlAndPublication(String url, String publication) throws Exception {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		UserPost newItem = null;
		Exception exception = null;
		try {
			stmt = conn.prepareStatement(SqlQueries.GET_POST_BY_URL_AND_PUBLICATION);
			stmt.setString(1, url);
			stmt.setString(2, publication);
			rs = stmt.executeQuery();
			
			if (rs.next()) {
				newItem = this.getPost(rs);
			}
		} catch(Exception ex) {
			exception = ex;
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
		
		if(exception != null) throw exception;
		return newItem;
	}

	public UserPost getPostById(String id) throws Exception {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		UserPost newItem = null;
		Exception exception = null;
		try {
			stmt = conn.prepareStatement(SqlQueries.GET_POST);
			stmt.setString(1, id);
			rs = stmt.executeQuery();
			
			if (rs.next()) {
				newItem = this.getPost(rs);
			}
		} catch(Exception ex) {
			exception = ex;
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
		
		if(exception != null) throw exception;
		return newItem;
	}

	/**
	 * Retrieves a list of pending posts from a site
	 * @param publication the publication to be filtered
	 * @param site the site to be filtered
	 * @param size how many posts to be retrieved
	 * @return a list of UserPost instance matching the criteria
	 */
	public ArrayList<UserPost> getPendingPostsBySite(String publication, String site, int size) {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		ArrayList<UserPost> ret = new ArrayList<UserPost>();
		
		try {
			stmt = conn.prepareStatement(SqlQueries.GET_POSTS);
			stmt.setInt(1, PostStatus.PENDING.getValue());
			stmt.setString(2, publication);
			stmt.setString(3, site);
			stmt.setInt(4, size);
			rs = stmt.executeQuery();
			
			while (rs.next()) {
				ret.add(this.getPost(rs));
			}
		} catch(Exception ex) {
			ex.printStackTrace();
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
	
	public void cleanOldDrafts() {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.prepareStatement(SqlQueries.CLEAN_OLD_DRAFTS);
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.DATE, -1);
			java.sql.Timestamp date = new java.sql.Timestamp(calendar.getTime().getTime());
			
			stmt.setTimestamp(1, date);
			rs = stmt.executeQuery();
		} catch(Exception ex) {
			ex.printStackTrace();
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
	
	private UserPost getPost(ResultSet rs) throws SQLException, UnsupportedEncodingException {
		UserPost newItem = new UserPost();
		
		java.sql.Blob blob = rs.getBlob("FILE_CONTENT");
		byte[] bytes = blob.getBytes(1, (int) blob.length());
		String xmlString = new String(bytes, "UTF-8");
		
		newItem = new UserPost();
		newItem.setId(rs.getString("RESOURCE_ID"));
		newItem.setCreationDate(rs.getDate("DATE_CREATED"));
		newItem.setUpdateDate(rs.getDate("DATE_UPDATED"));
		newItem.setPublication(rs.getInt("PUBLICATION"));
		newItem.setSite(rs.getString("SITE"));
		newItem.setModerationMessage(rs.getString("INFO"));
		newItem.setStatus(PostStatus.values()[rs.getInt("STATUS")]);
		newItem.setTitle(rs.getString("TITLE"));
		newItem.setUrl(rs.getString("URL"));
		newItem.setUserId(rs.getString("USER_ID"));
		newItem.setSocialNetworks(rs.getString("SOCIAL_NETWORKS"));
		newItem.setXmlContent(xmlString);
		
		return newItem;
	}
	
	private PostDetails getPostDetail(ResultSet rs) throws SQLException, UnsupportedEncodingException {
		PostDetails newItem = new PostDetails();
		
		newItem = new PostDetails();
		newItem.setId(rs.getInt(PD_ID));
		newItem.setFecha(rs.getDate(PD_FECHA));
		newItem.setPath(rs.getString(PD_PATH));
		newItem.setUsuario(rs.getString(PD_USUARIO));
		newItem.setDescription(rs.getString(PD_DESCRIPCION));
		newItem.setSitio(rs.getString(PD_SITIO));
		
		return newItem;
	}
	
	public int getPostAbuseReportCount(String resourcePath) throws SQLException{
		PreparedStatement stmt = null;
		ResultSet rs = null;
		int ret = 0;
		
		try {
			stmt = conn.prepareStatement("SELECT COUNT(*) FROM TFS_EVENT " + 
					"WHERE TARGETID = ? " +
					"AND ACTIONID = 18 ");
			stmt.setString(1, resourcePath);
			rs = stmt.executeQuery();
			
			while (rs.next()) {
				ret = rs.getInt(1);
			}
		} catch(Exception ex) {
			ex.printStackTrace();
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
	
	public void resetPostAbuseReportCount(String resourcePath, String sitio, String publication) throws SQLException{
		PreparedStatement stmt = null;
		
		try {
			stmt = conn.prepareStatement("DELETE FROM TFS_EVENT " + 
				"WHERE TARGETID = ? " +
				"AND ACTIONID = 18 AND PUBLICACION = ? AND SITIO = ?");
			stmt.setString(1, resourcePath);
			stmt.setString(2, publication);
			stmt.setString(3, sitio);
			stmt.executeUpdate();
		} catch(Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				stmt.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public ArrayList<PostDetails> getPostDetailsByResource(String resourcePath, int publication) throws SQLException{
		PreparedStatement stmt = null;
		ResultSet rs = null;
		ArrayList<PostDetails> ret = new ArrayList<PostDetails>();
		
		try {
			stmt = conn.prepareStatement("SELECT * FROM TFS_EVENT " + 
					"WHERE TARGETID = ? " +
					"AND ACTIONID = 18 " + 
					"AND PUBLICACION = ? ");
			stmt.setString(1, resourcePath);
			stmt.setInt(2, publication);
			rs = stmt.executeQuery();
			
			while (rs.next()) {
				ret.add(this.getPostDetail(rs));
			}
		} catch(Exception ex) {
			ex.printStackTrace();
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
}
