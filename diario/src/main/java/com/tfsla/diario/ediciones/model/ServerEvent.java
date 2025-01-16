package com.tfsla.diario.ediciones.model;

import java.util.Date;

public class ServerEvent {

	private String event;
	private String data;
	private String id;
	private int retry;
	private long timestamp;
	private String user;
	
	private final String toStringCache;
	
	private ServerEvent(String event, String data, String id, int retry, String user, String toStringCache) {
		super();
		this.event = event;
		this.data = data;
		this.id = id;
		this.retry = retry;
		this.user = user;
		this.toStringCache = toStringCache;
		
		timestamp = new Date().getTime();
	}
	
    public final String getEvent() {
		return event;
	}

	public final String getData() {
		return data;
	}

	public final String getId() {
		return id;
	}

	public final int getRetry() {
		return retry;
	}

	public final String getUser() {
        return user;
    }
	
	public final long getTimestamp() {
		return timestamp;
	}

	public final String toString() {
        return toStringCache;
    }

	public static class Builder {
        private String data = null;
        private String event = null;
        private Integer retry = null;
        private String id = null;
        private String user = null;

        public Builder setData(String data) {
            this.data = data;
            return this;
        }

        public Builder setEvent(String event) {
            this.event = event;
            return this;
        }

        public Builder setRetry(int retry) {
            this.retry = retry;
            return this;
        }

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setUser(String user) {
            this.user = user;
            return this;
        }
        public ServerEvent build() {
            StringBuilder sb = new StringBuilder();
            if(event != null) {
                sb.append("event: ").append(event.replace("\n", "")).append('\n');
            }
            if(data != null) {
                for(String s : data.split("\n")) {
                    sb.append("data: ").append(s).append('\n');
                }
            }
            if(retry != null) {
                sb.append("retry: ").append(retry).append('\n');
            }
            if(id != null) {
                sb.append("id: ").append(id.replace("\n","")).append('\n');
            }

            // an empty line dispatches the event
            sb.append('\n');
            return new ServerEvent(event,data,id,retry,user,sb.toString());
        }
    }
}
