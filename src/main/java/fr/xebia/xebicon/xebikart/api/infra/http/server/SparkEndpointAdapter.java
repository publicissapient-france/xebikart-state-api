package fr.xebia.xebicon.xebikart.api.infra.http.server;

import fr.xebia.xebicon.xebikart.api.infra.http.endpoint.SparkEndpoint;
import spark.Spark;
import spark.servlet.SparkApplication;

import java.util.Set;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class SparkEndpointAdapter implements SparkApplication {

    private final String pathPrefix;
    private final Set<SparkEndpoint> sparkEndpoints;

    public SparkEndpointAdapter(String pathPrefix, Set<SparkEndpoint> sparkEndpoints) {
        if (isBlank(pathPrefix)) {
            throw new IllegalArgumentException("pathPrefix must be defined and be non blank.");
        }

        requireNonNull(sparkEndpoints, "protectedSparkEndpoints must be defined.");
        this.pathPrefix = pathPrefix;
        this.sparkEndpoints = sparkEndpoints;
    }

    @Override
    public void init() {
        Spark.before("*", (request, response) -> response.header("Content-Type", "application/json"));
        Spark.path(pathPrefix, () -> sparkEndpoints.forEach(SparkEndpoint::configure));
    }

}
