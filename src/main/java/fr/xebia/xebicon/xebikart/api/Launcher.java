package fr.xebia.xebicon.xebikart.api;

import fr.xebia.xebicon.xebikart.api.application.configuration.ConfigurationFactory;
import fr.xebia.xebicon.xebikart.api.infra.DummyPipeEvent;
import fr.xebia.xebicon.xebikart.api.infra.http.endpoint.sse.SSEServletEventEmitterRegistry;
import fr.xebia.xebicon.xebikart.api.infra.http.server.JettySupport;
import fr.xebia.xebicon.xebikart.api.infra.mqtt.MqttConsumerContainer;

import java.util.List;

public class Launcher {

    private JettySupport jettySupport;
    private MqttConsumerContainer amqpConsumerContainer;

    public static void main(String[] args) {
        var launcher = new Launcher();
        Runtime.getRuntime().addShutdownHook(new Thread(launcher::stop));
        launcher.start();
    }

    public void start() {

        var eventSSERegistry = new SSEServletEventEmitterRegistry();

        var jettyConfiguration = ConfigurationFactory.buildJettyConfiguration(eventSSERegistry);

        var rabbitMqConfiguration = ConfigurationFactory.buildRabbitMqConfiguration();

        amqpConsumerContainer = new MqttConsumerContainer(rabbitMqConfiguration, List.of(new DummyPipeEvent(eventSSERegistry)));
        amqpConsumerContainer.start();

        jettySupport = new JettySupport(jettyConfiguration);
        jettySupport.start();

    }

    public void stop() {
        if (jettySupport != null) {
            jettySupport.stop();
            jettySupport = null;
        }
        if (amqpConsumerContainer != null) {
            amqpConsumerContainer.stop();
            amqpConsumerContainer = null;
        }

    }

}
