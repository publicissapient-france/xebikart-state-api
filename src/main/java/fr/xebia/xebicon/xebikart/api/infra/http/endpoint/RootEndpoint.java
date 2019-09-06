package fr.xebia.xebicon.xebikart.api.infra.http.endpoint;

import fr.xebia.xebicon.xebikart.api.infra.http.server.JsonTransformer;
import spark.Spark;

public class RootEndpoint implements SparkEndpoint {

    @Override
    public void configure() {
        Spark.get(
                "/",
                "*/*",
                (request, response) -> "",
                new JsonTransformer()
        );
    }

}
