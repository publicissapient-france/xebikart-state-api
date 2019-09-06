package fr.xebia.xebicon.xebikart.api.infra.http.server;

import fr.xebia.xebicon.xebikart.api.infra.http.endpoint.SparkEndpoint;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import spark.Spark;
import spark.servlet.SparkApplication;

import java.util.Set;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.slf4j.LoggerFactory.getLogger;

public class SparkEndpointAdapter implements SparkApplication {

    private static final Logger LOGGER = getLogger(SparkEndpointAdapter.class);

    private final String pathPrefix;
    private final Set<SparkEndpoint> sparkEndpoints;

    public SparkEndpointAdapter(String pathPrefix, Set<SparkEndpoint> sparkEndpoints) {
        requireNonNull(sparkEndpoints, "protectedSparkEndpoints must be defined.");
        this.pathPrefix = pathPrefix;
        this.sparkEndpoints = sparkEndpoints;
    }

    @Override
    public void init() {
        Spark.before("*", (request, response) -> response.header("Content-Type", "application/json"));
        if (StringUtils.isBlank(pathPrefix)) {
            sparkEndpoints.forEach(SparkEndpoint::configure);
        } else {
            LOGGER.info("Prefix Spark endpoint by {}.", pathPrefix);
            Spark.path(pathPrefix, () -> sparkEndpoints.forEach(SparkEndpoint::configure));
        }
    }

}
