package com.tfsla.diario.videoConverter.jsp.amazon;

public interface JobStatusNotificationHandler {

    public void handle(JobStatusNotification jobStatusNotification);
    
    public Boolean hasError();
    
    public String getMessage();
}