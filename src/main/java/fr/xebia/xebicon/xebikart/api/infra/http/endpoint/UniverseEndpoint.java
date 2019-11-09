package fr.xebia.xebicon.xebikart.api.infra.http.endpoint;

import fr.xebia.xebicon.xebikart.api.application.UniverseService;
import fr.xebia.xebicon.xebikart.api.application.model.OsMobile;
import fr.xebia.xebicon.xebikart.api.application.model.SurveyResult;
import fr.xebia.xebicon.xebikart.api.application.model.Universe;
import fr.xebia.xebicon.xebikart.api.infra.GsonProvider;
import fr.xebia.xebicon.xebikart.api.infra.http.server.JsonTransformer;
import org.slf4j.Logger;
import spark.Request;
import spark.Response;
import spark.Spark;

import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.isAnyBlank;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.slf4j.LoggerFactory.getLogger;
import static spark.Spark.*;

public class UniverseEndpoint implements SparkEndpoint {

    private static final Logger LOGGER = getLogger(UniverseEndpoint.class);
    private static final String APPLICATION_JSON = "application/json";

    private final UniverseService universeService;

    public UniverseEndpoint(UniverseService universeService) {
        requireNonNull(universeService, "universeService must be defined.");
        this.universeService = universeService;
    }

    @Override
    public void configure() {

        before("/*", (request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Allow-Credentials", "true");
        });

        options("/*",
                (request, response) -> {
                    var accessControlRequestHeaders = request
                            .headers("Access-Control-Request-Headers");
                    if (accessControlRequestHeaders != null) {
                        response.header("Access-Control-Allow-Headers",
                                accessControlRequestHeaders);
                    }

                    var accessControlRequestMethod = request
                            .headers("Access-Control-Request-Method");
                    if (accessControlRequestMethod != null) {
                        response.header("Access-Control-Allow-Methods",
                                accessControlRequestMethod);
                    }

                    return "OK";
                });


        post(
                "/poll/reset",
                APPLICATION_JSON,
                this::createSurvey,
                new JsonTransformer()
        );

        post(
                "/universe",
                APPLICATION_JSON,
                this::vote,
                new JsonTransformer()
        );

        post(
                "/poll/stop",
                APPLICATION_JSON,
                this::stopSurvey,
                new JsonTransformer()
        );
    }

    public Object createSurvey(Request request, Response response) {
        var surveyIdentifier = universeService.createSurvey();
        return surveyIdentifier.getId();
    }

    public Object vote(Request request, Response response) {
        return convertBodyToUniverseDto(request.body())
                .map(universeDto -> {
                    var universe = new Universe(universeDto.universe);
                    var fromDeviceOs = OsMobile.fromString(universeDto.platform);
                    universeService.addVote(universe, fromDeviceOs);
                    return true;
                })
                .orElseGet(() -> {
                    response.status(412);
                    return false;
                });
    }

    public Object stopSurvey(Request request, Response response) {
        var surveyResult = universeService.close();
        return surveyResult.fold(reason -> reason,
                SurveyResultDto::new
        );
    }

    private Optional<UniverseDto> convertBodyToUniverseDto(String body) {
        try {
            var gson = GsonProvider.provideGson();
            var universeDto = gson.fromJson(body, UniverseDto.class);
            return Optional.ofNullable(checkedUniverseDto(universeDto));
        } catch (Exception e) {
            LOGGER.debug("Unable to convert body {} to UniverseDto", body, e);
            return Optional.empty();
        }
    }

    private UniverseDto checkedUniverseDto(UniverseDto universeDto) {
        if (isAnyBlank(
                universeDto.universe,
                universeDto.platform
        )) {
            return null;
        }
        return universeDto;
    }

    private static class UniverseDto {

        private final String universe;

        private final String platform;

        private UniverseDto(String universe, String platform) {
            this.universe = universe;
            this.platform = platform;
        }

        String getUniverse() {
            return universe;
        }

        String getPlatform() {
            return platform;
        }

    }

    private static class SurveyResultDto {

        private final String surveyIdentifier;

        private final String universe;

        private final Map<String, Integer> votes;

        private SurveyResultDto(SurveyResult surveyResult) {
            this(
                    surveyResult.getSurveyIdentifier().getId(),
                    surveyResult.getUniverse().getName(),
                    surveyResult.getVotes()
            );
        }

        private SurveyResultDto(String surveyIdentifier, String universe, Map<String, Integer> votes) {
            if (isBlank(surveyIdentifier)) {
                throw new IllegalArgumentException("surveyIdentifier must be defined and be non blank.");
            }
            if (isBlank(universe)) {
                throw new IllegalArgumentException("universe must be defined and be non blank.");
            }

            requireNonNull(votes, "votes must be defined.");
            this.surveyIdentifier = surveyIdentifier;
            this.universe = universe;
            this.votes = votes;
        }

        public String getSurveyIdentifier() {
            return surveyIdentifier;
        }

        public String getUniverse() {
            return universe;
        }

        public Map<String, Integer> getVotes() {
            return votes;
        }
    }

}
