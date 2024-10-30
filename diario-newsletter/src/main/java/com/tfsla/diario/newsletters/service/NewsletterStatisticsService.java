package com.tfsla.diario.newsletters.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.logging.Log;
import org.opencms.main.CmsLog;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.GetSendStatisticsResponse;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.services.ses.model.SendDataPoint;
import com.tfsla.diario.newsletters.common.INewsletterStatisticsService;
import com.tfsla.diario.newsletters.common.NewsletterConfiguration;
import com.tfsla.diario.newsletters.common.NewsletterStatistics;
import com.tfsla.diario.newsletters.common.NewsletterSubscriptionStatistics;
import com.tfsla.diario.newsletters.data.NewsletterEventDAO;

public class NewsletterStatisticsService implements INewsletterStatisticsService {
	@Override
	public long getPerformanceFromDate(Date date) throws Exception {
		return this.getPerformanceFromDate(date, 0);
	}
	
	@Override
	public long getPerformanceFromDate(Date date, int newsletterID) throws Exception {
		NewsletterEventDAO dao = new NewsletterEventDAO();
		try {
			dao.openConnection();
			if (newsletterID == 0) {
				return dao.getNewsletterPerformance(date);
			}
			
			return dao.getNewsletterPerformance(date, newsletterID);
		} catch(Exception e) {
			LOG.error("Error while getting newsletter performance", e);
			throw e;
		} finally {
			try {
				dao.closeConnection();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public long getSubscriptionsUpTo(Date date, int newsletterID) throws Exception {
		NewsletterEventDAO dao = new NewsletterEventDAO();
		try {
			dao.openConnection();
			return dao.getSubscriptionsUpTo(date, newsletterID);
		} catch(Exception e) {
			LOG.error("Error while getting subscriptions up to " + date, e);
			throw e;
		} finally {
			try {
				dao.closeConnection();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public NewsletterSubscriptionStatistics getAudienceFromDate(Date date, int newsletterID) throws Exception {
		NewsletterEventDAO dao = new NewsletterEventDAO();
		try {
			dao.openConnection();
			return dao.getAudienceStatistics(date, newsletterID);
		} catch(Exception e) {
			LOG.error("Error while getting audience statistics", e);
			throw e;
		} finally {
			try {
				dao.closeConnection();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public NewsletterStatistics getAmazonLastSendStatistics(NewsletterConfiguration config) {
		List<NewsletterStatistics> statistics = this.getAmazonStatistics(config);
		return statistics.get(statistics.size() - 1);
	}
	
	@Override
	public List<NewsletterStatistics> getAmazonStatistics(NewsletterConfiguration config, Boolean groupByDay) {
		if(_statistics == null) {
			AwsBasicCredentials awsCreds = AwsBasicCredentials.create(config.getAmzAccessID(), config.getAmzAccessKey());
			
			SesClient client = SesClient.builder()
					.credentialsProvider(StaticCredentialsProvider.create(awsCreds))
	                .region(Region.of(config.getAmzRegion()))
	                .build();
			
			GetSendStatisticsResponse result = client.getSendStatistics();
			
			if(groupByDay) {
				_statistics = this.groupByDate(result.sendDataPoints());
			} else {
				_statistics = new ArrayList<NewsletterStatistics>();
				for(SendDataPoint point : result.sendDataPoints()) {
					_statistics.add(new NewsletterStatistics(point));
				}
			}
			
			Collections.sort(_statistics);
		}
		return _statistics;
	}
	
	@Override
	public List<NewsletterStatistics> getAmazonStatistics(NewsletterConfiguration config) {
		return this.getAmazonStatistics(config, true);
	}
	
	private List<NewsletterStatistics> groupByDate(List<SendDataPoint> points) {
		Hashtable<Date, NewsletterStatistics> dates = new Hashtable<Date, NewsletterStatistics>();
		Calendar cal = new GregorianCalendar();
		for(SendDataPoint point : points) {
			cal.setTime(Date.from(point.timestamp()));
			cal.set(Calendar.HOUR_OF_DAY, 0);
	        cal.set(Calendar.MINUTE, 0);
	        cal.set(Calendar.SECOND, 0);
	        cal.set(Calendar.MILLISECOND, 0);
	        Date date = cal.getTime();
			if(dates.containsKey(date)) {
				NewsletterStatistics s = dates.get(date);
				s.setBounces(s.getBounces() + point.bounces());
				s.setComplaints(s.getComplaints() + point.complaints());
				s.setDeliveryAttempts(s.getDeliveryAttempts() + point.deliveryAttempts());
				s.setRejects(s.getRejects() + point.rejects());
			} else {
				dates.put(date, new NewsletterStatistics(point));
			}
		}
		
		List<NewsletterStatistics> ret = 
			dates.values().size() > 0 ? 
				new ArrayList<NewsletterStatistics>(dates.values()) : 
					new ArrayList<NewsletterStatistics>();
		
		return ret;
	}
	
	protected List<NewsletterStatistics> _statistics;
	protected Log LOG = CmsLog.getLog(this.getClass());
}
