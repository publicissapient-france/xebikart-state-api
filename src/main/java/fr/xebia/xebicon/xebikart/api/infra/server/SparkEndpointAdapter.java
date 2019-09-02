package fr.xebia.xebicon.xebikart.api.infra.server;

import fr.xebia.xebicon.xebikart.api.infra.endpoint.SparkEndpoint;
import spark.Spark;
import spark.servlet.SparkApplication;

import java.util.Set;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class SparkEndpointAdapter implements SparkApplication {

    private final String pathPrefix;
    private final Set<SparkEndpoint> protectedSparkEndpoints;

    public SparkEndpointAdapter(String pathPrefix, Set<SparkEndpoint> protectedSparkEndpoints) {
        if (isBlank(pathPrefix)) {
            throw new IllegalArgumentException("pathPrefix must be defined and be non blank.");
        }

        requireNonNull(protectedSparkEndpoints, "protectedSparkEndpoints must be defined.");
        this.pathPrefix = pathPrefix;
        this.protectedSparkEndpoints = protectedSparkEndpoints;
    }

    @Override
    public void init() {
        Spark.before("*", (request, response) -> response.header("Content-Type", "application/json"));
        Spark.path(pathPrefix, () -> protectedSparkEndpoints.forEach(SparkEndpoint::configure));
    }

}
