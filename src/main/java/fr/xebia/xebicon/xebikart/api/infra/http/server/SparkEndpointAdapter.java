package fr.xebia.xebicon.xebikart.api.infra.http.server;

import fr.xebia.xebicon.xebikart.api.infra.http.endpoint.SparkEndpoint;
import io.prometheus.client.Counter;
import io.prometheus.client.Summary;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import spark.servlet.SparkApplication;

import java.util.Set;

import static java.util.Objects.requireNonNull;
import static org.slf4j.LoggerFactory.getLogger;
import static spark.Spark.*;

public class SparkEndpointAdapter implements SparkApplication {

    private static final Logger LOGGER = getLogger(SparkEndpointAdapter.class);
    private final static String TIMER_ATTRIBUTE = "timer";

    // Custom Prometheus metrics
    private static final Counter httpRequestByEndpoint = Counter.build("http_requests_total", "HTTP requests by endpoint")
            .namespace("xebikart-state-api")
            .labelNames("method", "endpoint", "status")
            .register();

    private static final Summary requestLatencyByEndpoint = Summary.build()
            .name("http_request_duration_seconds")
            .help("Request latency")
            .namespace("xebikart-state-api")
            .labelNames("method", "endpoint")
            .create()
            .register();

    private final String pathPrefix;
    private final Set<SparkEndpoint> sparkEndpoints;

    public SparkEndpointAdapter(String pathPrefix, Set<SparkEndpoint> sparkEndpoints) {
        requireNonNull(sparkEndpoints, "protectedSparkEndpoints must be defined.");
        this.pathPrefix = pathPrefix;
        this.sparkEndpoints = sparkEndpoints;
    }

    @Override
    public void init() {
        before("*", (request, response) -> {
            Summary.Timer requestTimer = requestLatencyByEndpoint.labels(request.requestMethod(), request.pathInfo())
                    .startTimer();
            request.attribute(TIMER_ATTRIBUTE, requestTimer); // Pass timer into the filter chain

            response.header("Content-Type", "application/json");
        });
        afterAfter("/*", ((request, response) -> {
            Summary.Timer timer = request.attribute(TIMER_ATTRIBUTE);
            timer.observeDuration();

            httpRequestByEndpoint.labels(request.requestMethod(), request.pathInfo(), String.valueOf(response.status()))
                    .inc();
        }));
        if (StringUtils.isBlank(pathPrefix)) {
            sparkEndpoints.forEach(SparkEndpoint::configure);
        } else {
            LOGGER.info("Prefix Spark endpoint by {}.", pathPrefix);
            path(pathPrefix, () -> sparkEndpoints.forEach(SparkEndpoint::configure));
        }
    }

}
