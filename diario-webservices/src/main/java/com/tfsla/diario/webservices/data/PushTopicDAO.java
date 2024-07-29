package com.tfsla.diario.webservices.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.opencms.main.OpenCms;
import org.opencms.scheduler.CmsScheduledJobInfo;

import com.tfsla.diario.webservices.PushNotificationServices.jobs.PushNotificationsJob;
import com.tfsla.diario.webservices.common.TopicConfiguration;
import com.tfsla.diario.webservices.common.strings.SqlQueries;
import com.tfsla.diario.webservices.common.strings.StringConstants;
import com.tfsla.diario.webservices.common.strings.TimeUnits;
import com.tfsla.webusersposts.core.BaseDAO;

public class PushTopicDAO  extends BaseDAO {

	public List<TopicConfiguration> getTopics(String site, int publication) {
		List<TopicConfiguration> topics = new ArrayList<TopicConfiguration>();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.prepareStatement(SqlQueries.GET_TOPIC_CONFIGURATION_BY_LOCATION);
			stmt.setString(1, 	site);
			stmt.setInt(2, 	publication);
			rs = stmt.executeQuery();
			
			while (rs.next()) {
				topics.add(getTopic(rs));
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
		
		return topics;
		
	}
	public TopicConfiguration getTopicConfiguration(String name) {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.prepareStatement(SqlQueries.GET_TOPIC_CONFIGURATION);
			stmt.setString(1, 	name);
			rs = stmt.executeQuery();
			
			if (rs.next()) {
				return getTopic(rs);
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
		
		return null;
	}
	
	private TopicConfiguration getTopic(ResultSet rs) throws SQLException {
		if (rs!=null)
		{
			TopicConfiguration tConfig = new TopicConfiguration();
			
			tConfig.setName(rs.getString("NAME"));
			tConfig.setDescription(rs.getString("DESCRIPTION"));
			tConfig.setSite(rs.getString("SITE"));
			tConfig.setPublication(rs.getInt("PUBLICATION"));
			
			tConfig.setInterval(rs.getInt("INTERVAL"));
			tConfig.setFromHour(rs.getInt("FROMHOUR"));
			tConfig.setFromMinutes(rs.getInt("FROMMINUTES"));
			tConfig.setToHour(rs.getInt("TOHOUR"));
			tConfig.setToMinutes(rs.getInt("TOMINUTES"));
			tConfig.setPushUrlParams(rs.getString("URLPARAMS"));
			tConfig.setPriority(rs.getInt("PRIORITY"));
			
			tConfig.setIntervalUnit(TimeUnits.MINUTES);
			tConfig.setIsJobScheduled(true);
			try {
				CmsScheduledJobInfo job = getPushJob(tConfig.getName());
				if(job == null || !job.isActive()) tConfig.setIsJobScheduled(false);
			} catch(Exception e) {
				tConfig.setIsJobScheduled(false);
			}
			return tConfig;
		}
		return null;
	}
	
	public static synchronized CmsScheduledJobInfo getPushJob(String topicName) {
		List jobs = OpenCms.getScheduleManager().getJobs();
		for(Object job : jobs) {
			CmsScheduledJobInfo jobInfo = (CmsScheduledJobInfo)job;
			if(jobInfo.getClassName().equals(PushNotificationsJob.class.getName())) {
				if(jobInfo.getCronExpression().endsWith(" * * ?")) {
					if(jobInfo.getParameters().containsKey("topic") && jobInfo.getParameters().get("topic").equals(topicName)) {
						return jobInfo;
					}
				}
			}
		}
		return null;
	}

}
