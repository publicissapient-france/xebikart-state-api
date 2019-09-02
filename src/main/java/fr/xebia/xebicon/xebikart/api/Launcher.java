package fr.xebia.xebicon.xebikart.api;

import fr.xebia.xebicon.xebikart.api.application.configuration.JettyConfiguration;
import fr.xebia.xebicon.xebikart.api.application.configuration.RabbitMqConfiguration;
import fr.xebia.xebicon.xebikart.api.application.configuration.SSEConfiguration;
import fr.xebia.xebicon.xebikart.api.infra.DummyPipeEvent;
import fr.xebia.xebicon.xebikart.api.infra.amqp.AmqpConsumer;
import fr.xebia.xebicon.xebikart.api.infra.http.endpoint.sse.EventSSERegistry;
import fr.xebia.xebicon.xebikart.api.infra.http.server.JettySupport;
import org.slf4j.Logger;

import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

public class Launcher {

    private static final Logger LOGGER = getLogger(Launcher.class);

    private JettySupport jettySupport;
    private AmqpConsumer amqpConsumer;

    public static void main(String[] args) {
        var launcher = new Launcher();
        Runtime.getRuntime().addShutdownHook(new Thread(launcher::stop));
        launcher.start();
    }

    public void start() {

        var eventSSERegistry = new EventSSERegistry();
        var sseConfiguration = new SSEConfiguration("/events", eventSSERegistry);

        var jettyConfiguration = new JettyConfiguration(
                8080,
                null,
                List.of(sseConfiguration)
        );
/*
        var rabbitMqConfiguration = new RabbitMqConfiguration(
                "rabbitmq.xebik.art",
                1883,
                List.of("xebikart-events"),
                "xebikart1",
                "xebikart1"
        );
        */


        var rabbitMqConfiguration = new RabbitMqConfiguration(
                "localhost",
                5672,
                List.of("xebikart-events"),
                "user",
                "password"
        );

        amqpConsumer = new AmqpConsumer(rabbitMqConfiguration, List.of(new DummyPipeEvent(eventSSERegistry)));
        amqpConsumer.start();

        jettySupport = new JettySupport(jettyConfiguration);
        jettySupport.start();
/*
        while (!Thread.currentThread().isInterrupted()) {
            eventSSERegistry.sendData("{\"race\": {\"state\": \"AWAITING\"}}");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
*/
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
