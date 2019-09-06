package fr.xebia.xebicon.xebikart.api.application.configuration;

import fr.xebia.xebicon.xebikart.api.infra.http.endpoint.HealthEndpoint;
import fr.xebia.xebicon.xebikart.api.infra.http.endpoint.RootEndpoint;
import fr.xebia.xebicon.xebikart.api.infra.http.endpoint.SparkEndpoint;

import java.util.Set;

public class EndpointConfiguration {

    public static Set<SparkEndpoint> buildSparkEndpoints() {
        return Set.of(
                rootEndpoint(),
                healthEndpoint()
        );
    }

    public static SparkEndpoint rootEndpoint() {
        return new RootEndpoint();
    }

    public static SparkEndpoint healthEndpoint() {
        return new HealthEndpoint();
    }

}
