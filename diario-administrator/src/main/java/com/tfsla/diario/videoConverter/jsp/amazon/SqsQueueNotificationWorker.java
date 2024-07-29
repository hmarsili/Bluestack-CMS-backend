package com.tfsla.diario.videoConverter.jsp.amazon;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.tfsla.diario.videoConverter.ConverterLogger;
import com.tfsla.diario.videoConverter.jsp.amazon.JobStatusNotification;
import com.tfsla.diario.videoConverter.jsp.amazon.JobStatusNotificationHandler;
import com.tfsla.diario.videoConverter.jsp.amazon.Notification;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Polls an SQS queue for Elastic Transcoder job status notification messages
 * and calls the handle method on all registered JobStatusNotificationHandler
 * objects before deleting the message from the queue.
 */
public class SqsQueueNotificationWorker implements Runnable {

    private static final int MAX_NUMBER_OF_MESSAGES = 5;
    private static final int VISIBILITY_TIMEOUT = 15;
    private static final int WAIT_TIME_SECONDS = 15;
    private static final ObjectMapper mapper = new ObjectMapper();
    
    private AmazonSQS amazonSqs;
    private String queueUrl;
    private List<JobStatusNotificationHandler> handlers;
    private ConverterLogger LOG = null;
    
    private volatile boolean shutdown = false;
    
    public SqsQueueNotificationWorker(AmazonSQS amazonSqs, String queueUrl, ConverterLogger log) {
        this.amazonSqs = amazonSqs;
        this.queueUrl = queueUrl;
        this.handlers = new ArrayList<JobStatusNotificationHandler>();
        this.LOG = log;
    }
    
    public void addHandler(JobStatusNotificationHandler jobStatusNotificationHandler) {
        synchronized(handlers) {
            this.handlers.add(jobStatusNotificationHandler);
        }
    }
    
    public void removeHandler(JobStatusNotificationHandler jobStatusNotificationHandler) {
        synchronized(handlers) {
            this.handlers.remove(jobStatusNotificationHandler);
        }
    }
    
    @Override
    public void run() {
        ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest()
            .withQueueUrl(queueUrl)
            .withMaxNumberOfMessages(MAX_NUMBER_OF_MESSAGES)
            .withVisibilityTimeout(VISIBILITY_TIMEOUT)
            .withWaitTimeSeconds(WAIT_TIME_SECONDS);
        
        while (!shutdown) {
            // Long pole the SQS queue.  This will return as soon as a message
            // is received, or when WAIT_TIME_SECONDS has elapsed.
            List<Message> messages = amazonSqs.receiveMessage(receiveMessageRequest).getMessages();
            if (messages == null) {
                // If there were no messages during this poll period, SQS
                // will return this list as null.  Continue polling.
                continue;
            }
            
            LOG.log(String.format("Received %s SQS messages from queue at %s, transfering to handlers", messages.size(), queueUrl));
            synchronized(handlers) {
                for (Message message : messages) {
                    try {
                        // Parse notification and call handlers.
                        JobStatusNotification notification = parseNotification(message);
                        for (JobStatusNotificationHandler handler : handlers) {
                            handler.handle(notification);
                        }
                    } catch (IOException e) {
                    	LOG.log("Failed to convert notification: " + e.getMessage());
                    }
                    
                    // Delete the message from the queue.
                    amazonSqs.deleteMessage(new DeleteMessageRequest().withQueueUrl(queueUrl).withReceiptHandle(message.getReceiptHandle()));
                }
            }
        }
    }
    
    private JobStatusNotification parseNotification(Message message) throws IOException {
        Notification<JobStatusNotification> notification = mapper.readValue(message.getBody(), new TypeReference<Notification<JobStatusNotification>>() {});
        return notification.getMessage();
    }

    public void shutdown() {
        shutdown = true;
    }
}
