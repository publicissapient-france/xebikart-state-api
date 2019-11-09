package fr.xebia.xebicon.xebikart.api.infra;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import fr.xebia.xebicon.xebikart.api.application.model.OsMobile;

import java.lang.reflect.Type;

public class OsMobileJsonSerializer implements JsonSerializer<OsMobile> {

    @Override
    public JsonElement serialize(OsMobile src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.name().toLowerCase());
    }

}
