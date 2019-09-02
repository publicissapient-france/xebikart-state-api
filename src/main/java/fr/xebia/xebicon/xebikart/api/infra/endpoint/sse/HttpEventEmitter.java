package fr.xebia.xebicon.xebikart.api.infra.endpoint.sse;

import fr.xebia.xebicon.xebikart.api.infra.EventEmitter;
import org.eclipse.jetty.servlets.EventSource;
import org.slf4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import static java.util.Objects.requireNonNull;
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
        this.emitter = emitter;
    }

    @Override
    public void onClose() {
        this.emitter = null;
    }

    @Override
    public void send(String data) {
        if (emitter != null) {
            try {
                emitter.data(data);
            } catch (IOException e) {
                emitter = null;
                LOGGER.debug("Unable to send following data using SSE: {}", data, e);
            }
        }
    }
}
