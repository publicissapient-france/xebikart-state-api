package fr.xebia.xebicon.xebikart.api.infra.http.server;

import com.google.gson.GsonBuilder;
import spark.ResponseTransformer;

public class JsonTransformer implements ResponseTransformer {

    @Override
    public String render(Object model) {
        return new GsonBuilder().create().toJson(model);
    }

}
