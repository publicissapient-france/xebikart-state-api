package fr.xebia.xebicon.xebikart.api.infra;

import com.google.gson.*;
import fr.xebia.xebicon.xebikart.api.application.cqrs.Identifier;

import java.lang.reflect.Type;

public class CqrsIdentifierJsonserializer implements JsonSerializer<Identifier> {

    @Override
    public JsonElement serialize(Identifier src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.getId());
    }

}
