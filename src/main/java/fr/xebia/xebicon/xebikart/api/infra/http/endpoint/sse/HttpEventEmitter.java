package fr.xebia.xebicon.xebikart.api.infra.http.endpoint.sse;

import fr.xebia.xebicon.xebikart.api.infra.EventEmitter;
import org.eclipse.jetty.servlets.EventSource;
import org.slf4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.slf4j.LoggerFactory.getLogger;

public class HttpEventEmitter implements EventSource, EventEmitter {

    private static final Logger LOGGER = getLogger(HttpEventEmitter.class);

    private final HttpServletRequest httpServletRequest;

    private Emitter emitter;

    public HttpEventEmitter(HttpServletRequest httpServletRequest) {
        requireNonNull(httpServletRequest, "httpServletRequest must be defined.");
        LOGGER.info("Create a new Event source client");
        this.httpServletRequest = httpServletRequest;
    }

    @Override
    public void onOpen(Emitter emitter) throws IOException {
        requireNonNull(emitter, "emitter must be defined.");
        this.emitter = emitter;
    }

    @Override
    public void onClose() {
        this.emitter = null;
    }

    @Override
    public void send(String eventName, String data) {
        if (isBlank(eventName)) {
            throw new IllegalArgumentException("eventName must be defined and be non blank.");
        }
        if (isBlank(data)) {
            throw new IllegalArgumentException("data must be defined and be non blank.");
        }

        if (emitter != null) {
            try {
                emitter.event(eventName, data);
            } catch (IOException e) {
                emitter = null;
                LOGGER.debug("Unable to send following data using SSE: {}", data, e);
            }
        }
    }

    public void comment(String comment) {
        if (isBlank(comment)) {
            throw new IllegalArgumentException("comment must be defined and be non blank.");
        }
        if (emitter != null) {
            try {
                emitter.comment(comment);
            } catch (IOException e) {
                emitter = null;
                LOGGER.debug("Unable to send following comment using SSE: {}", comment, e);
            }
        }
    }

}
