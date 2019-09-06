package fr.xebia.xebicon.xebikart.api.infra.http.server;

import fr.xebia.xebicon.xebikart.api.infra.http.endpoint.SparkEndpoint;
import org.apache.commons.lang3.StringUtils;
import spark.Spark;
import spark.servlet.SparkApplication;

import java.util.Set;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class SparkEndpointAdapter implements SparkApplication {

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
            Spark.path(pathPrefix, () -> sparkEndpoints.forEach(SparkEndpoint::configure));
        }
    }

}
