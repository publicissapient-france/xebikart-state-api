package fr.xebia.xebicon.xebikart.api.infra.http.endpoint;

import fr.xebia.xebicon.xebikart.api.infra.http.server.JsonTransformer;
import spark.Spark;

public class HealthEndpoint implements SparkEndpoint {

    @Override
    public void configure() {
        Spark.get(
                "/health",
                "application/json",
                (request, response) -> "",
                new JsonTransformer()
        );
    }

}
