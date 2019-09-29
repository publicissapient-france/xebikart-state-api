package fr.xebia.xebicon.xebikart.api.application.bus;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class EventSource {

    private final String origin;

    private final String payload;

    public EventSource(String origin, String payload) {
        if (isBlank(origin)) {
            throw new IllegalArgumentException("origin must be defined and be non blank.");
        }
        if (isBlank(payload)) {
            throw new IllegalArgumentException("payload must be defined and be non blank.");
        }

        this.origin = origin;
        this.payload = payload;
    }

    public String getOrigin() {
        return origin;
    }

    public String getPayload() {
        return payload;
    }
    
}
