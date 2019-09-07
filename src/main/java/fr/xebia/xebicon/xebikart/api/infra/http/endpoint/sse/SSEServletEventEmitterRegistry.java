package fr.xebia.xebicon.xebikart.api.infra.http.endpoint.sse;

import fr.xebia.xebicon.xebikart.api.infra.EventEmitter;
import org.eclipse.jetty.servlets.EventSource;
import org.eclipse.jetty.servlets.EventSourceServlet;
import org.slf4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.slf4j.LoggerFactory.getLogger;

public class SSEServletEventEmitterRegistry extends EventSourceServlet implements EventEmitter {

    private static final Logger LOGGER = getLogger(SSEServletEventEmitterRegistry.class);

    private final List<HttpEventEmitter> eventSources;

    public SSEServletEventEmitterRegistry(ScheduledExecutorService executorService) {
        LOGGER.info("Start an instance of SSEServletEventEmitterRegistry");
        eventSources = new ArrayList<>();
    }

    @Override
    protected EventSource newEventSource(HttpServletRequest request) {
        LOGGER.info("Request a new EventSource");
        var eventSource = new HttpEventEmitter(request);
        eventSources.add(eventSource);
        return eventSource;
    }

    @Override
    protected void respond(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.addHeader("Access-Control-Allow-Origin", "*");
        super.respond(request, response);
    }

    @Override
    public void send(String eventName, String data) {
        if (isBlank(eventName)) {
            throw new IllegalArgumentException("eventName must be defined and be non blank.");
        }
        if (isBlank(data)) {
            throw new IllegalArgumentException("data must be defined and be non blank.");
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(
                    "Dispatching event [{}] with following payload to {} SSE client(s): {}",
                    eventName,
                    eventSources.size(),
                    data
            );
        }
        eventSources.forEach(eventEmitter -> {
            try {
                eventEmitter.send(eventName, data);
            } catch (Exception e) {
                LOGGER.error("Unable to send following data: {}", data, e);
                eventSources.remove(eventEmitter);
            }
        });
    }

}
