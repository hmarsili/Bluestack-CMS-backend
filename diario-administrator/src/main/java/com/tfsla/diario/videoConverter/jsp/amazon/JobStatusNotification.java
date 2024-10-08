package com.tfsla.diario.videoConverter.jsp.amazon;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonIgnoreProperties(ignoreUnknown=true)
public class JobStatusNotification {

    @JsonIgnoreProperties(ignoreUnknown=true)
    public static class JobInput {
        private String key;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        @Override
        public String toString() {
            return "JobInput [key=" + key + "]";
        }
    }
    
    @JsonIgnoreProperties(ignoreUnknown=true)
    public static class JobOutput {
        private String id;
        private String presetId;
        private String key;
        private String status;

        private String statusDetail;
        private int errorCode;
        
        public String getId() {
            return id;
        }
        
        public void setId(String id) {
            this.id = id;
        }
        
        public String getPresetId() {
            return presetId;
        }
        
        public void setPresetId(String presetId) {
            this.presetId = presetId;
        }
        
        public String getKey() {
            return key;
        }
        
        public void setKey(String key) {
            this.key = key;
        }
        
        public String getStatus() {
            return status;
        }
        
        public void setStatus(String status) {
            this.status = status;
        }
        
        public String getStatusDetail() {
            return statusDetail;
        }
        
        public void setStatusDetail(String statusDetail) {
            this.statusDetail = statusDetail;
        }
        
        public int getErrorCode() {
            return errorCode;
        }
        
        public void setErrorCode(int errorCode) {
            this.errorCode = errorCode;
        }
        
        @Override
        public String toString() {
            return "JobOutput [id=" + id + ", presetId=" + presetId + ", key="
                    + key + ", status=" + status + ", statusDetail="
                    + statusDetail + ", errorCode=" + errorCode + "]";
        }
    }
    
    public static enum JobState {
    	PROGRESSING,
        COMPLETED,
        WARNING,
        ERROR;
        
        public boolean isTerminalState() {
            return this.equals(JobState.COMPLETED) || this.equals(JobState.ERROR);
        }
    }
    
    private static final ObjectMapper mapper = new ObjectMapper();
    
    private JobState state;
    private int errorCode;
    private String version;
    private String jobId;
    private String pipelineId;
    private JobInput input;
    private String outputKeyPrefix;
    private List<JobOutput> outputs;
    
    public JobState getState() {
        return state;
    }
    
    public void setState(JobState state) {
        this.state = state;
    }
    
    public int getErrorCode() {
        return errorCode;
    }
    
    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public String getJobId() {
        return jobId;
    }
    
    public void setJobId(String jobId) {
        this.jobId = jobId;
    }
    
    public String getPipelineId() {
        return pipelineId;
    }
    
    public void setPipelineId(String pipelineId) {
        this.pipelineId = pipelineId;
    }
    
    public JobInput getInput() {
        return input;
    }
    
    public void setInput(JobInput input) {
        this.input = input;
    }
    
    public String getOutputKeyPrefix() {
        return outputKeyPrefix;
    }
    
    public void setOutputKeyPrefix(String outputKeyPrefix) {
        this.outputKeyPrefix = outputKeyPrefix;
    }
    
    public List<JobOutput> getOutputs() {
        return outputs;
    }
    
    public void setOutputs(List<JobOutput> outputs) {
        this.outputs = outputs;
    }
    
    @Override
    public String toString() {
        return "JobStatusNotification [state=" + state + ", errorCode="
                + errorCode + ", version=" + version + ", jobId=" + jobId
                + ", pipelineId=" + pipelineId + ", input=" + input
                + ", outputKeyPrefix=" + outputKeyPrefix + ", outputs="
                + outputs + "]";
    }

    public static JobStatusNotification valueOf(String value) throws IOException {
        return mapper.readValue(value, JobStatusNotification.class);
    }
}