package fr.xebia.xebicon.xebikart.api.application.bus;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class EventSource {

    private final String origin;

    private final byte[] payload;

    public EventSource(String origin, byte[] payload) {
        if (isBlank(origin)) {
            throw new IllegalArgumentException("origin must be defined and be non blank.");
        }
        requireNonNull(payload, "payload must be defined.");
        this.origin = origin;
        this.payload = payload;
    }

    public String getOrigin() {
        return origin;
    }

    public String getPayloadAsString() {
        return new String(payload);
    }

    public byte[] getPayload() {
        return payload;
    }
}
