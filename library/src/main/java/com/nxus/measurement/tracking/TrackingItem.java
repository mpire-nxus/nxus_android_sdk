package com.nxus.measurement.tracking;

import java.util.Map;

/**
 * Helper class for getting formatted TrackingItem as delimited string value.
 * @author TechMpire Ltd.
 */

public class TrackingItem {
    private String eventIndex;
    private String event;
    private TrackingParams params;
    private long time;

    public TrackingItem (String eventIndex, String eventName, TrackingParams params) {
        this.eventIndex = eventIndex;
        this.event = eventName;
        this.params = params;
        this.time = System.currentTimeMillis();
    }

    public TrackingItem (String event, TrackingParams params) {
        this.eventIndex = "";
        this.event = event;
        this.params = params;
        this.time = System.currentTimeMillis();
    }

    public TrackingItem (String event, TrackingParams params, long time) {
        this.eventIndex = "";
        this.event = event;
        this.params = params;
        this.time = time;
    }

    public String getEventIndex() {
        return eventIndex;
    }

    public String getEvent() {
        return event;
    }

    public TrackingParams getParams() {
        return params;
    }

    public long getTime() {
        return time;
    }

    public String getTrack() {
        String tempParams = "";
        if (params != null) {
            String concatenator = "";
            StringBuilder builder = new StringBuilder();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                builder.append(concatenator);
                builder.append(entry.getKey());
                builder.append("=");
                builder.append(entry.getValue());

                concatenator = "&";
            }
            tempParams = builder.toString();
        }
        return eventIndex + ";" + event + ";" + tempParams + ";" + time;
    }
}
