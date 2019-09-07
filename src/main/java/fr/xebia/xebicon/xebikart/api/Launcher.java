package fr.xebia.xebicon.xebikart.api;

import fr.xebia.xebicon.xebikart.api.application.configuration.ConfigurationFactory;
import fr.xebia.xebicon.xebikart.api.application.configuration.EndpointConfiguration;
import fr.xebia.xebicon.xebikart.api.infra.DummyPipeEvent;
import fr.xebia.xebicon.xebikart.api.infra.http.endpoint.sse.SSEServletEventEmitterRegistry;
import fr.xebia.xebicon.xebikart.api.infra.http.server.JettySupport;
import fr.xebia.xebicon.xebikart.api.infra.mqtt.MqttConsumerContainer;

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
                List.of(new DummyPipeEvent(eventSSERegistry)),
                executorService
        );

        mqttConsumerContainer.start();

        var servletContextHandlerConfigurers = EndpointConfiguration.buildServletContextHandlerConfigurers();

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
