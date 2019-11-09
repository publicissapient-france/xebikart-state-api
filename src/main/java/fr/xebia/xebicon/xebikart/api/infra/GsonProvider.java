package fr.xebia.xebicon.xebikart.api.infra;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.xebia.xebicon.xebikart.api.application.cqrs.Identifier;
import fr.xebia.xebicon.xebikart.api.application.cqrs.SurveyIdentifier;
import fr.xebia.xebicon.xebikart.api.application.cqrs.UniverseIdentifier;
import fr.xebia.xebicon.xebikart.api.application.model.OsMobile;

public class GsonProvider {

    private GsonProvider() {
        //  Utility classe
    }

    public static GsonBuilder provideGsonBuilder() {
        return new GsonBuilder()
                .registerTypeAdapter(Identifier.class, new CqrsIdentifierJsonserializer())
                .registerTypeAdapter(UniverseIdentifier.class, new CqrsIdentifierJsonserializer())
                .registerTypeAdapter(SurveyIdentifier.class, new CqrsIdentifierJsonserializer())
                .registerTypeAdapter(OsMobile.class, new OsMobileJsonSerializer());
    }

    public static Gson provideGson() {
        return provideGsonBuilder().create();
    }
    
}
