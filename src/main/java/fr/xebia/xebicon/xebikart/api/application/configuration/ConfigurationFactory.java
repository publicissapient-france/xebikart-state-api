package fr.xebia.xebicon.xebikart.api.application.configuration;

import fr.xebia.xebicon.xebikart.api.infra.http.endpoint.SparkEndpoint;
import fr.xebia.xebicon.xebikart.api.infra.http.endpoint.sse.SSEServletEventEmitterRegistry;
import fr.xebia.xebicon.xebikart.api.infra.http.server.SparkEndpointAdapter;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class ConfigurationFactory {

    public static final String PATH_PREFIX = "/api";

    private ConfigurationFactory() {
        // Utility class
    }

    public static JettyConfiguration buildJettyConfiguration(
            SSEServletEventEmitterRegistry eventSSEServletEventEmitterRegistry,
            SSEServletEventEmitterRegistry universeSSEServletEventEmitterRegistry,
            SparkConfiguration sparkConfiguration
    ) {

        var port = Integer.parseInt(getEnvValue("HTTP_PORT", "80"));
        return new JettyConfiguration(
                port,
                sparkConfiguration,
                List.of(
                        new SSEConfiguration("/events", eventSSEServletEventEmitterRegistry),
                        new SSEConfiguration("/universes", universeSSEServletEventEmitterRegistry)
                )
        );

    }

    public static SparkConfiguration buildSparkConfiguration(Set<SparkEndpoint> sparkEndpoints) {
        var sparkApplication = new SparkEndpointAdapter(PATH_PREFIX, sparkEndpoints);
        return new SparkConfiguration(PATH_PREFIX, sparkApplication);
    }

    public static RabbitMqConfiguration buildRabbitMqConfiguration() {
        var host = getEnvValue("MQTT_HOST", "localhost");
        var port = Integer.parseInt(getEnvValue("MQTT_PORT", "1883"));
        var username = getEnvValue("MQTT_USERNAME", null);
        var password = getEnvValue("MQTT_PASSWORD", null);
        var queueNames = getEnvValue("MQTT_QUEUES", "xebikart-car-video,xebikart-events,race-events");
        return new RabbitMqConfiguration(
                host,
                port,
                Arrays.asList(queueNames.split(",")),
                username,
                password
        );
    }

    private static String getEnvValue(String key) {
        return getEnvValue(key, "");
    }

    private static String getEnvValue(String key, String defaultValue) {
        if (isBlank(key)) {
            throw new IllegalArgumentException("key must be defined and be non blank.");
        }
        var value = System.getenv(key);
        if (isBlank(value)) {
            return defaultValue;
        }
        return value;
    }


}
