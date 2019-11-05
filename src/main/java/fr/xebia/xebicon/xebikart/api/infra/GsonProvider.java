package fr.xebia.xebicon.xebikart.api.infra;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.xebia.xebicon.xebikart.api.application.cqrs.Identifier;
import fr.xebia.xebicon.xebikart.api.application.cqrs.UniverseIdentifier;

public class GsonProvider {

    private GsonProvider() {
        //  Utility classe
    }

    public static GsonBuilder provideGsonBuilder() {
        return new GsonBuilder()
                .registerTypeAdapter(Identifier.class, new CqrsIdentifierJsonserializer())
                .registerTypeAdapter(UniverseIdentifier.class, new CqrsIdentifierJsonserializer());
    }

    public static Gson provideGson() {
        return provideGsonBuilder().create();
    }
    
}
