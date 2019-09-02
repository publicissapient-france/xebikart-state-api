package fr.xebia.xebicon.xebikart.api.infra.endpoint.sse;

import fr.xebia.xebicon.xebikart.api.infra.EventEmitter;
import org.eclipse.jetty.servlets.EventSource;
import org.eclipse.jetty.servlets.EventSourceServlet;
import org.slf4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.slf4j.LoggerFactory.getLogger;

public class EventSSERegistry extends EventSourceServlet implements EventEmitter {

    private static final Logger LOGGER = getLogger(EventSSERegistry.class);

    private final List<EventEmitter> eventSources;

    public EventSSERegistry() {
        LOGGER.info("Start an instance of EventSSERegistry");
        eventSources = new ArrayList<>();
    }

    @Override
    protected EventSource newEventSource(HttpServletRequest request) {
        LOGGER.info("request a new EventSource");
        var eventSource = new HttpEventEmitter(request);
        eventSources.add(eventSource);
        return eventSource;
    }

    @Override
    public void send(String data) {
        if (isBlank(data)) {
            throw new IllegalArgumentException("data must be defined and be non blank.");
        }
        eventSources.forEach(eventEmitter -> {
            try {
                eventEmitter.send(data);
            } catch (RuntimeException e) {
                LOGGER.error("Unable to send following data: {}", data, e);
            }
        });
    }

}
