package fr.xebia.xebicon.xebikart.api.infra.http.endpoint;

import com.google.gson.GsonBuilder;
import fr.xebia.xebicon.xebikart.api.application.cqrs.*;
import fr.xebia.xebicon.xebikart.api.infra.http.server.JsonTransformer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import spark.Request;
import spark.Response;

import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static org.slf4j.LoggerFactory.getLogger;
import static spark.Spark.post;

public class UniverseEndpoint implements SparkEndpoint {

    private static final Logger LOGGER = getLogger(UniverseEndpoint.class);

    private final CqrsEngine<UniverseIdentifier, UniverseState, UniverseCommand, UniverseEvent> cqrsEngine;

    public UniverseEndpoint(CqrsEngine<UniverseIdentifier, UniverseState, UniverseCommand, UniverseEvent> cqrsEngine) {
        requireNonNull(cqrsEngine, "busEntrypoint must be defined.");
        this.cqrsEngine = cqrsEngine;
    }

    @Override
    public void configure() {

        post(
                "/universe",
                "application/json",
                this::changeUniverse,
                new JsonTransformer()
        );

    }

    public Object changeUniverse(Request request, Response response) {
        return convertUniverseDtoToIdentifier(request.body())
                .map(identifier -> {
                    var command = new SwitchUniverse(identifier);
                    var result = cqrsEngine.handleCommand(command);
                    return result instanceof SuccessfulCommandHandled;
                })
                .orElse(false);
    }

    @NotNull
    private Optional<UniverseIdentifier> convertUniverseDtoToIdentifier(String body) {
        try {
            var gson = new GsonBuilder().create();
            var universeDto = gson.fromJson(body, UniverseDto.class);
            return Optional.of(new UniverseIdentifier(universeDto.getUniverse()));
        } catch (Exception e) {
            LOGGER.debug("Unable to convert body {} to UniverseIdentifier", body, e);
            return Optional.empty();
        }
    }

    private static class UniverseDto {

        private final String universe;

        private UniverseDto(String universe) {
            this.universe = universe;
        }

        public String getUniverse() {
            return universe;
        }
    }

}
