package fr.xebia.xebicon.xebikart.api.infra.http.server;

import spark.ResponseTransformer;

import static fr.xebia.xebicon.xebikart.api.infra.GsonProvider.provideGson;

public class JsonTransformer implements ResponseTransformer {

    @Override
    public String render(Object model) {
        return provideGson().toJson(model);
    }

}
