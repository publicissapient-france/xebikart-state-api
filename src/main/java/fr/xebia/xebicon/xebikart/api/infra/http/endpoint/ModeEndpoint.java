package fr.xebia.xebicon.xebikart.api.infra.http.endpoint;

import fr.xebia.xebicon.xebikart.api.application.cqrs.mode.ModeService;
import fr.xebia.xebicon.xebikart.api.infra.http.server.JsonTransformer;
import org.slf4j.Logger;
import spark.Request;
import spark.Response;

import static fr.xebia.xebicon.xebikart.api.infra.GsonProvider.provideGson;
import static org.slf4j.LoggerFactory.getLogger;
import static spark.Spark.post;

public class ModeEndpoint implements SparkEndpoint {

    private static final Logger LOGGER = getLogger(ModeEndpoint.class);
    private static final String APPLICATION_JSON = "application/json";

    private final ModeService modeService;

    public ModeEndpoint(ModeService modeService) {
        this.modeService = modeService;
    }

    @Override
    public void configure() {
        post(
                "/mode",
                APPLICATION_JSON,
                this::setMode,
                new JsonTransformer()
        );
    }

    private Object setMode(Request request, Response response) {
        ModeDto modeDto = convertBodyToModeDto(request.body());
        return modeService.setMode(modeDto.mode, modeDto.data);
    }

    private ModeDto convertBodyToModeDto(String body) {
        return provideGson().fromJson(body, ModeDto.class);
    }

    private static class ModeDto {

        private final String mode;
        private final Object data;

        private ModeDto(String mode, Object data) {
            this.mode = mode;
            this.data = data;
        }

        public Object getData() {
            return data;
        }

        public String getMode() {
            return mode;
        }

    }
}
