package fr.xebia.xebicon.xebikart.api.infra.http.endpoint;

import fr.xebia.xebicon.xebikart.api.infra.http.server.JsonTransformer;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

public class RootEndpoint implements SparkEndpoint {

    @Override
    public void configure() {
        Spark.get(
                "/",
                "application/json",
                (request, response) -> "",
                new JsonTransformer()
        );
    }
    
}
