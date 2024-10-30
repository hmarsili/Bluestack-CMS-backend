package com.tfsla.diario.newsletters.service;

import java.security.cert.X509Certificate;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.logging.Log;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsLog;

import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.Body;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.Message;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;
import com.tfsla.diario.newsletters.common.INewsletterDispatcherService;
import com.tfsla.diario.newsletters.common.INewsletterHtmlRetriever;
import com.tfsla.diario.newsletters.common.INewslettersService;
import com.tfsla.diario.newsletters.common.Newsletter;
import com.tfsla.diario.newsletters.common.NewsletterConfiguration;
import com.tfsla.diario.newsletters.common.NewsletterDispatch;
import com.tfsla.diario.newsletters.data.NewsletterDispatchDAO;

public class NewsletterDispatcherService implements INewsletterDispatcherService {
	
	class MessagesShuttle extends Thread { 

		protected Log LOG = CmsLog.getLog(this);
		
		private int startFrom;
		private int endAt;
		private int batchSize;
		private int newsletterID;
		private Newsletter newsletter;
		private NewsletterUnsubscribeTokenManager tokenManager; 
		private String htmlContent;
		private SesClient client;


		public MessagesShuttle (String name, 
				Newsletter newsletter, 
				NewsletterUnsubscribeTokenManager tokenManager, 
				String htmlContent, 
				SesClient client,
				int startFrom,
				int endAt,
				int batchSize,
				int newsletterID) { 

			super(name);
			this.batchSize = batchSize;
			this.endAt = endAt;
			this.startFrom = startFrom;
			this.newsletterID = newsletterID;

			this.newsletter = newsletter;
			this.tokenManager = tokenManager; 
			this.htmlContent = htmlContent;
			this.client = client;

		}

		public void doDispatch(String email, Newsletter newsletter, NewsletterUnsubscribeTokenManager tokenManager, String htmlContent, SesClient client) {
			String token = tokenManager.getUnsubscribeToken(email, newsletter.getID());
			String html = htmlContent.replace(NewsletterUnsubscribeTokenManager.UNSUBSCRIBE_TOKEN_MACRO, token);
			try {
				String emailFrom = newsletter.getEmailFrom();
				try {
					emailFrom = new String(emailFrom.getBytes("UTF-8"), "ISO-8859-1");
				} catch (Exception e) {
					e.printStackTrace();
				}
				SendEmailRequest request = SendEmailRequest.builder()
						.destination(Destination.builder()
								.toAddresses(email)
								.build()
								)
						.configurationSetName(newsletter.getConfigSet())
						.message(Message.builder()
								.body(Body.builder()
										.html(Content.builder()
												.charset("UTF-8")
												.data(html)
												.build()
												)
										.build()
										)
								.subject(Content.builder()
										.charset("UTF-8")
										.data(newsletter.getSubject())
										.build()
										)
								.build()
								)
						.replyToAddresses(emailFrom)
						.source(emailFrom)
						.build();
				client.sendEmail(request);
			} catch(Exception e) {
				LOG.error(e);
				e.printStackTrace();
			}
		}

		public void run() { 


			INewslettersService svc;
			int offset=startFrom;
			try {
				svc = NewsletterServiceContainer.getInstance(INewslettersService.class);

				int page = 0;
				while(offset < endAt) {

					int size = Math.min(batchSize, endAt-offset);
					List<String> emails = svc.getNewsletterSubscriptionsEmails(newsletterID, offset, size);
					LOG.debug(String.format("Amazon SES dispatch | MessagesShuttle %s - enviando mails desde subscriptor %s, los proximos  %s siguientes (desde %s hasta %s)", this.getName(), offset, size, emails.get(0), emails.get(emails.size()-1)));
					LOG.info(String.format("Amazon SES dispatch | MessagesShuttle %s - batch No. %s, sending %s emails", this.getName(), page, emails.size()));
					for(String email : emails) {
						//LOG.error(String.format("Amazon SES dispatch | MessagesShuttle %s - enviando a subscriptor %s", this.getName(), email));
						this.doDispatch(email, newsletter, tokenManager, htmlContent, client);
					}

					offset+=batchSize;
					page ++;
				}
			} catch (Exception e) {
				LOG.error(String.format("Amazon SES dispatch | MessagesShuttle %s - Error sending messages ",getName()) , e);
			}
			


		} 
	}


	
	public NewsletterDispatcherService() {
		try {
			TrustManager[] trustAllCerts = new TrustManager[] {
				new X509TrustManager() {
		            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
		                return null;
		            }
		            public void checkClientTrusted(X509Certificate[] certs, String authType) {
		            }
		            public void checkServerTrusted(X509Certificate[] certs, String authType) {
		            }
		        }
	        };
			
			// Install the all-trusting trust manager
	        SSLContext sc = SSLContext.getInstance("SSL");
	        sc.init(null, trustAllCerts, new java.security.SecureRandom());
	        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
	
	        // Create all-trusting host name verifier
	        HostnameVerifier allHostsValid = new HostnameVerifier() {
	            public boolean verify(String hostname, SSLSession session) {
	                return true;
	            }
	        };
	
	        // Install the all-trusting host verifier
	        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void addDispatch(NewsletterDispatch dispatch) {
		NewsletterDispatchDAO dao = new NewsletterDispatchDAO();
		try {
			dao.openConnection();
			dao.addDispatch(dispatch);
		} catch(Exception e) {
			LOG.error(e);
			throw e;
		} finally {
			dao.closeConnection();
		}
	}
	
	public List<NewsletterDispatch> getDispatches(int newsletterID) throws Exception {
		NewsletterDispatchDAO dao = new NewsletterDispatchDAO();
		try {
			dao.openConnection();
			return dao.getDispatches(newsletterID);
		} catch(Exception e) {
			LOG.error(e);
			throw e;
		} finally {
			dao.closeConnection();
		}
	}
	
	@Override
	public int dispatchNewsletter(int newsletterID, NewsletterConfiguration config, CmsObject cmsObject) throws Throwable {
		
		AwsBasicCredentials awsCreds = AwsBasicCredentials.create(config.getAmzAccessID(), config.getAmzAccessKey());
		
		SesClient client = SesClient.builder()
				.credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .region(Region.of(config.getAmzRegion()))
                .build();
		
		INewslettersService svc = NewsletterServiceContainer.getInstance(INewslettersService.class);
		Newsletter newsletter = svc.getNewsletter(newsletterID);

		INewsletterHtmlRetriever htmlRetriever = NewsletterServiceContainer.getInstance(INewsletterHtmlRetriever.class);
		String htmlContent = htmlRetriever.getHtml(newsletter.getHtmlPath());

		int batchSize = config.getBatchSize();
		int subscriptors = svc.getNewsletterSubscriptors(newsletterID);
		NewsletterUnsubscribeTokenManager tokenManager = new NewsletterUnsubscribeTokenManager();
		LOG.info(String.format("Will dispatch newsletter %s to %s subscribers using config set %s", newsletter.getName(), subscriptors, newsletter.getConfigSet()));
		
		
		
		
		int shuttles = config.getNumOfMessagesShuttles();
		
		int msgSize = subscriptors / shuttles;
		
		
		// 31 / 3 = 10  
		
		// 
		int startFrom = 0;
		for (int j=1;j<=config.getNumOfMessagesShuttles(); j++) {
			int endAt = startFrom + msgSize; 
			if (j==config.getNumOfMessagesShuttles()) {
				endAt = subscriptors;
			}
			LOG.info("Lanzando shuttle " + j + " con suscriptores de " + startFrom + " a " + endAt);
			MessagesShuttle ms = new MessagesShuttle("Shuttle " + j, newsletter, tokenManager, htmlContent, client, startFrom, endAt, batchSize, newsletterID);
			
			ms.start();
			startFrom = endAt;
		}
		
		
		/*while(page*batchSize < subscriptors) {
			List<String> emails = svc.getNewsletterSubscriptionsEmails(newsletterID, page*batchSize, batchSize);
			LOG.info(String.format("Amazon SES dispatch - batch No. %s, sending %s emails", page, emails.size()));
			for(String email : emails) {
				this.doDispatch(email, newsletter, tokenManager, htmlContent, client);
			}

			page++;
		}*/

		return subscriptors;
	}
	
	
	
	
	@Override
	public void singleDispatch(String email, Newsletter newsletter, NewsletterConfiguration config) throws Throwable {
		NewsletterUnsubscribeTokenManager tokenManager = new NewsletterUnsubscribeTokenManager();
		INewsletterHtmlRetriever htmlRetriever = NewsletterServiceContainer.getInstance(INewsletterHtmlRetriever.class);
		String htmlContent = htmlRetriever.getHtml(newsletter.getHtmlPath());
		
		AwsBasicCredentials awsCreds = AwsBasicCredentials.create(config.getAmzAccessID(), config.getAmzAccessKey());
		
		SesClient client = SesClient.builder()
				.credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .region(Region.of(config.getAmzRegion()))
                .build();
		
		this.doDispatch(email, newsletter, tokenManager, htmlContent, client);
	}
	
	@Override
	public void doDispatch(String email, Newsletter newsletter, NewsletterUnsubscribeTokenManager tokenManager, String htmlContent, SesClient client) {
		String token = tokenManager.getUnsubscribeToken(email, newsletter.getID());
		String html = htmlContent.replace(NewsletterUnsubscribeTokenManager.UNSUBSCRIBE_TOKEN_MACRO, token);
		try {
			String emailFrom = newsletter.getEmailFrom();
			try {
				emailFrom = new String(emailFrom.getBytes("UTF-8"), "ISO-8859-1");
			} catch (Exception e) {
				e.printStackTrace();
			}
			SendEmailRequest request = SendEmailRequest.builder()
				.destination(Destination.builder()
                        .toAddresses(email)
                        .build()
                )
				.configurationSetName(newsletter.getConfigSet())
				.message(Message.builder()
						.body(Body.builder()
								.html(Content.builder()
										.charset("UTF-8")
										.data(html)
										.build()
								)
								.build()
						)
						.subject(Content.builder()
								.charset("UTF-8")
								.data(newsletter.getSubject())
								.build()
						)
						.build()
				)
				.replyToAddresses(emailFrom)
				.source(emailFrom)
				.build();
			
			client.sendEmail(request);
		} catch(Exception e) {
			LOG.error(e);
			e.printStackTrace();
		}
	}

	protected Log LOG = CmsLog.getLog(this);
}