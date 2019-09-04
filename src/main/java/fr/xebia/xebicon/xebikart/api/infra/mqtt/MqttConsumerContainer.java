package fr.xebia.xebicon.xebikart.api.infra.mqtt;

import fr.xebia.xebicon.xebikart.api.application.configuration.RabbitMqConfiguration;
import fr.xebia.xebicon.xebikart.api.infra.EventReceiver;
import fr.xebia.xebicon.xebikart.api.infra.EventSource;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static org.slf4j.LoggerFactory.getLogger;

public class MqttConsumerContainer {

    private static final Logger LOGGER = getLogger(MqttConsumerContainer.class);

    private final RabbitMqConfiguration rabbitMqConfiguration;

    private final List<EventReceiver> eventReceivers;

    private MqttClient mqttClient;

    public MqttConsumerContainer(RabbitMqConfiguration rabbitMqConfiguration, List<EventReceiver> eventReceivers) {
        requireNonNull(rabbitMqConfiguration, "rabbitMqConfiguration must be defined.");
        requireNonNull(eventReceivers, "eventReceivers must be defined.");
        this.rabbitMqConfiguration = rabbitMqConfiguration;
        this.eventReceivers = eventReceivers;
    }

    public synchronized void start() {
        if (mqttClient == null) {
            var serverUri = String.format(
                    "tcp://%s:%s",
                    rabbitMqConfiguration.getHost(),
                    rabbitMqConfiguration.getPort()
            );

            try {
                var options = new MqttConnectOptions();

                rabbitMqConfiguration.getCredentials().ifPresent(credentials -> {
                    options.setUserName(credentials.getUsername());
                    options.setPassword(credentials.getPassword().toCharArray());
                });

                options.setAutomaticReconnect(true);
                options.setCleanSession(true);
                options.setConnectionTimeout(10);

                mqttClient = new MqttClient(serverUri, "state-api");
                mqttClient.connect(options);
                LOGGER.info("Connected to Mqtt server {}", serverUri);

                rabbitMqConfiguration.getQueueNames().forEach(topic -> {
                    try {
                        LOGGER.info("Subscribing to MQTT queue '{}'.", topic);
                        mqttClient.subscribe(topic, new InternalListener());
                    } catch (MqttException e) {
                        LOGGER.error("Unable to subscribe to the topic {}.", topic, e);
                    }
                });

            } catch (MqttException e) {
                LOGGER.error("Unable to connected to mqtt server uri {}.", serverUri, e);
            }
        }
    }

    public synchronized void stop() {
        if (mqttClient != null) {
            LOGGER.info("Stopping MQTT client.");
            try {
                mqttClient.disconnectForcibly();
                mqttClient.close(true);
            } catch (MqttException e) {
                LOGGER.warn("Unable to close MQTT connection.", e);
            }
            mqttClient = null;
        }
    }

    private class InternalListener implements IMqttMessageListener {

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            requireNonNull(message, "message must be defined.");
            var payload = new String(message.getPayload(), StandardCharsets.UTF_8);

            LOGGER.trace("-> MQTT [{}] : {}", topic, payload);

            if (StringUtils.isNotBlank(payload)) {
                var eventSource = new EventSource(topic, payload);
                eventReceivers.forEach(eventReceiver -> eventReceiver.receive(eventSource));
            } else if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Received an empty payload MQTT message; ignoring it.");
            }
        }

    }

}
