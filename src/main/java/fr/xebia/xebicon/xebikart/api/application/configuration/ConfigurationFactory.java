package fr.xebia.xebicon.xebikart.api.application.configuration;

import fr.xebia.xebicon.xebikart.api.infra.http.endpoint.sse.EventSSERegistry;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class ConfigurationFactory {

    private ConfigurationFactory() {
        // Utility class
    }

    public static JettyConfiguration buildJettyConfiguration(EventSSERegistry eventSSERegistry) {
        var port = Integer.parseInt(getEnvValue("HTTP_PORT", "80"));
        return new JettyConfiguration(
                port,
                null,
                List.of(new SSEConfiguration("/events", eventSSERegistry))
        );
    }

    public static RabbitMqConfiguration buildRabbitMqConfiguration() {
        var host = getEnvValue("AMQP_HOST", "localhost");
        var port = Integer.parseInt(getEnvValue("AMQP_PORT", "5672"));
        var username = getEnvValue("AMQP_USERNAME", null);
        var password = getEnvValue("AMQP_PASSWORD", null);
        var queueName = getEnvValue("AMQP_QUEUE", "xebikart-events");
        return new RabbitMqConfiguration(
                host,
                port,
                List.of(queueName),
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
