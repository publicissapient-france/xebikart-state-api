package fr.xebia.xebicon.xebikart.api;

import fr.xebia.xebicon.xebikart.api.application.configuration.ConfigurationFactory;
import fr.xebia.xebicon.xebikart.api.infra.DummyPipeEvent;
import fr.xebia.xebicon.xebikart.api.infra.amqp.AmqpConsumer;
import fr.xebia.xebicon.xebikart.api.infra.http.endpoint.sse.EventSSERegistry;
import fr.xebia.xebicon.xebikart.api.infra.http.server.JettySupport;

import java.util.List;

public class Launcher {

    private JettySupport jettySupport;
    private AmqpConsumer amqpConsumer;

    public static void main(String[] args) {
        var launcher = new Launcher();
        Runtime.getRuntime().addShutdownHook(new Thread(launcher::stop));
        launcher.start();
    }

    public void start() {

        var eventSSERegistry = new EventSSERegistry();

        var jettyConfiguration = ConfigurationFactory.buildJettyConfiguration(eventSSERegistry);

        var rabbitMqConfiguration = ConfigurationFactory.buildRabbitMqConfiguration();

        amqpConsumer = new AmqpConsumer(rabbitMqConfiguration, List.of(new DummyPipeEvent(eventSSERegistry)));
        amqpConsumer.start();

        jettySupport = new JettySupport(jettyConfiguration);
        jettySupport.start();

    }

    public void stop() {
        if (jettySupport != null) {
            jettySupport.stop();
            jettySupport = null;
        }
        if (amqpConsumer != null) {
            amqpConsumer.stop();
            amqpConsumer = null;
        }

    }

}
