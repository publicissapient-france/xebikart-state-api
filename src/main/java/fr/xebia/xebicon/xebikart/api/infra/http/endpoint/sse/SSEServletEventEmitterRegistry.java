package fr.xebia.xebicon.xebikart.api.infra.http.endpoint.sse;

import fr.xebia.xebicon.xebikart.api.infra.EventEmitter;
import org.eclipse.jetty.servlets.EventSource;
import org.eclipse.jetty.servlets.EventSourceServlet;
import org.slf4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.slf4j.LoggerFactory.getLogger;

public class SSEServletEventEmitterRegistry extends EventSourceServlet implements EventEmitter, Runnable {

    private static final Logger LOGGER = getLogger(SSEServletEventEmitterRegistry.class);

    private final List<HttpEventEmitter> eventSources;

    public SSEServletEventEmitterRegistry(ScheduledExecutorService executorService) {
        LOGGER.info("Start an instance of SSEServletEventEmitterRegistry");
        eventSources = new ArrayList<>();
        executorService.schedule(this, 20, TimeUnit.SECONDS);
    }

    @Override
    protected EventSource newEventSource(HttpServletRequest request) {
        LOGGER.info("Request a new EventSource");
        var eventSource = new HttpEventEmitter(request);
        eventSources.add(eventSource);
        return eventSource;
    }

    @Override
    public void send(String eventName, String data) {
        if (isBlank(eventName)) {
            throw new IllegalArgumentException("eventName must be defined and be non blank.");
        }
        if (isBlank(data)) {
            throw new IllegalArgumentException("data must be defined and be non blank.");
        }
        eventSources.forEach(eventEmitter -> {
            try {
                eventEmitter.send(eventName, data);
            } catch (RuntimeException e) {
                LOGGER.error("Unable to send following data: {}", data, e);
            }
        });
    }

    @Override
    public void run() {
        LOGGER.trace("Sending comment");
        eventSources.forEach(eventEmitter -> {
            try {
                eventEmitter.comment("heartbeat");
            } catch (Exception e) {
                LOGGER.error("Unable to send heartbeat to a SSE client.", e);
            }
        });
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.interrupted();
        }
    }

}
