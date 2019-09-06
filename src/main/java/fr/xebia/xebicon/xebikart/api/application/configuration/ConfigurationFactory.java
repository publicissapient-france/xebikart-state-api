package fr.xebia.xebicon.xebikart.api.application.configuration;

import fr.xebia.xebicon.xebikart.api.infra.http.endpoint.sse.SSEServletEventEmitterRegistry;

import java.util.Arrays;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class ConfigurationFactory {

    private ConfigurationFactory() {
        // Utility class
    }

    public static JettyConfiguration buildJettyConfiguration(SSEServletEventEmitterRegistry SSEServletEventEmitterRegistry) {
        var port = Integer.parseInt(getEnvValue("HTTP_PORT", "80"));
        return new JettyConfiguration(
                port,
                null,
                List.of(new SSEConfiguration("/events", SSEServletEventEmitterRegistry))
        );
    }

    public static RabbitMqConfiguration buildRabbitMqConfiguration() {
        var host = getEnvValue("MQTT_HOST", "localhost");
        var port = Integer.parseInt(getEnvValue("MQTT_PORT", "1883"));
        var username = getEnvValue("MQTT_USERNAME", null);
        var password = getEnvValue("MQTT_PASSWORD", null);
        var queueNames = getEnvValue("MQTT_QUEUES", "xebikart-events,race-events");
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
