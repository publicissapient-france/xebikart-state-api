package fr.xebia.xebicon.xebikart.api;

import fr.xebia.xebicon.xebikart.api.application.configuration.JettyConfiguration;
import fr.xebia.xebicon.xebikart.api.application.configuration.SSEConfiguration;
import fr.xebia.xebicon.xebikart.api.infra.endpoint.sse.EventSSERegistry;
import fr.xebia.xebicon.xebikart.api.infra.server.JettySupport;
import org.slf4j.Logger;

import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

public class Launcher {

    private static final Logger LOGGER = getLogger(Launcher.class);

    private JettySupport jettySupport;

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

        jettySupport = new JettySupport(jettyConfiguration);
        jettySupport.start();

        while (!Thread.currentThread().isInterrupted()) {
            eventSSERegistry.send("Coucou");
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public void stop() {
        if (jettySupport != null) {
            jettySupport.stop();
            ;
        }
    }

}
