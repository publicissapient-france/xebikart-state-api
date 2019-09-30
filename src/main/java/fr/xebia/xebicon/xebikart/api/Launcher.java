package fr.xebia.xebicon.xebikart.api;

import fr.xebia.xebicon.xebikart.api.application.LocalFileVideoFetcher;
import fr.xebia.xebicon.xebikart.api.application.configuration.ConfigurationFactory;
import fr.xebia.xebicon.xebikart.api.application.configuration.EndpointConfiguration;
import fr.xebia.xebicon.xebikart.api.application.bus.DirectPipeEvent;
import fr.xebia.xebicon.xebikart.api.infra.http.endpoint.sse.SSEServletEventEmitterRegistry;
import fr.xebia.xebicon.xebikart.api.infra.http.server.JettySupport;
import fr.xebia.xebicon.xebikart.api.infra.mqtt.MqttConsumerContainer;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Launcher {

    private JettySupport jettySupport;
    private MqttConsumerContainer mqttConsumerContainer;
    private ExecutorService executorService;

    public static void main(String[] args) {
        var launcher = new Launcher();
        Runtime.getRuntime().addShutdownHook(new Thread(launcher::stop));
        launcher.start();
    }

    public void start() {

        var eventSSERegistry = new SSEServletEventEmitterRegistry(Executors.newScheduledThreadPool(1));

        var sparkEndpoints = EndpointConfiguration.buildSparkEndpoints();
        var sparkConfiguration = ConfigurationFactory.buildSparkConfiguration(sparkEndpoints);

        var jettyConfiguration = ConfigurationFactory.buildJettyConfiguration(eventSSERegistry, sparkConfiguration);

        var rabbitMqConfiguration = ConfigurationFactory.buildRabbitMqConfiguration();

        executorService = Executors.newSingleThreadExecutor();
        mqttConsumerContainer = new MqttConsumerContainer(
                rabbitMqConfiguration,
                List.of(new DirectPipeEvent(eventSSERegistry)),
                executorService
        );

        mqttConsumerContainer.start();

        var videoDirectory = new File("/home/jpthiery/workspace/xebia/xebikart/xebikart-api/src/test/resources/video/tub");
        var videoFetcher = new LocalFileVideoFetcher(videoDirectory);
        var servletContextHandlerConfigurers = EndpointConfiguration.buildServletContextHandlerConfigurers(videoFetcher);

        jettySupport = new JettySupport(jettyConfiguration, servletContextHandlerConfigurers);
        jettySupport.start();

    }

    public void stop() {
        if (jettySupport != null) {
            jettySupport.stop();
            jettySupport = null;
        }
        if (mqttConsumerContainer != null) {
            mqttConsumerContainer.stop();
            mqttConsumerContainer = null;
        }
        executorService.shutdownNow();
    }

}
