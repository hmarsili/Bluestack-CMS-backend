package com.tfsla.diario.videoConverter.jsp.amazon;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.tfsla.diario.videoConverter.ConverterLogger;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

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
    
    private SqsClient amazonSqs;
    private String queueUrl;
    private List<JobStatusNotificationHandler> handlers;
    private ConverterLogger LOG = null;
    
    private volatile boolean shutdown = false;
    
    public SqsQueueNotificationWorker(SqsClient amazonSqs, String queueUrl, ConverterLogger log) {
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
        ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder()
            .queueUrl(queueUrl)
            .maxNumberOfMessages(MAX_NUMBER_OF_MESSAGES)
            .visibilityTimeout(VISIBILITY_TIMEOUT)
            .waitTimeSeconds(WAIT_TIME_SECONDS)
            .build();
        
        while (!shutdown) {
            // Long pole the SQS queue.  This will return as soon as a message
            // is received, or when WAIT_TIME_SECONDS has elapsed.
            List<Message> messages = amazonSqs.receiveMessage(receiveMessageRequest).messages();
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
                    amazonSqs.deleteMessage(
                    		DeleteMessageRequest.builder()
                    			.queueUrl(queueUrl)
                    			.receiptHandle(message.receiptHandle())
                    			.build());
                }
            }
        }
    }
    
    private JobStatusNotification parseNotification(Message message) throws IOException {
        Notification<JobStatusNotification> notification = mapper.readValue(message.body(), new TypeReference<Notification<JobStatusNotification>>() {});
        return notification.getMessage();
    }

    public void shutdown() {
        shutdown = true;
    }
}
