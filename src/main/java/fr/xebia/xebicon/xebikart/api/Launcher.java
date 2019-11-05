package fr.xebia.xebicon.xebikart.api;

import fr.xebia.xebicon.xebikart.api.application.bus.DirectPipeEvent;
import fr.xebia.xebicon.xebikart.api.application.bus.FanOutPipeEvent;
import fr.xebia.xebicon.xebikart.api.application.bus.FilteredPipeEvent;
import fr.xebia.xebicon.xebikart.api.application.configuration.ConfigurationFactory;
import fr.xebia.xebicon.xebikart.api.application.configuration.EndpointConfiguration;
import fr.xebia.xebicon.xebikart.api.application.cqrs.*;
import fr.xebia.xebicon.xebikart.api.infra.cqrs.InMemoryEventStore;
import fr.xebia.xebicon.xebikart.api.infra.http.endpoint.sse.SSEServletEventEmitterRegistry;
import fr.xebia.xebicon.xebikart.api.infra.http.server.JettySupport;
import fr.xebia.xebicon.xebikart.api.infra.mqtt.EventVideoFetcher;
import fr.xebia.xebicon.xebikart.api.infra.mqtt.MqttConsumerContainer;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Launcher {

    private JettySupport jettySupport;
    private MqttConsumerContainer eventMqttConsumerContainer;
    private ExecutorService executorService;

    public static void main(String[] args) {
        var launcher = new Launcher();
        Runtime.getRuntime().addShutdownHook(new Thread(launcher::stop));
        launcher.start();
    }


    public void start() {

        var eventStore = new InMemoryEventStore();
        var cqrsEngine = new CqrsEngine<UniverseIdentifier, UniverseState, UniverseCommand, UniverseEvent>(
                eventStore,
                new Universe()
        );

        var universeEventSSERegistry = new SSEServletEventEmitterRegistry();

        var universeOutputBridge = new OutputCqrsBusEntrypoint(universeEventSSERegistry);
        eventStore.registerListener(universeOutputBridge);

        var eventSSERegistry = new SSEServletEventEmitterRegistry();

        var sparkEndpoints = EndpointConfiguration.buildSparkEndpoints(cqrsEngine);
        var sparkConfiguration = ConfigurationFactory.buildSparkConfiguration(sparkEndpoints);

        var jettyConfiguration = ConfigurationFactory.buildJettyConfiguration(eventSSERegistry, universeEventSSERegistry, sparkConfiguration);

        var eventRabbitMqConfiguration = ConfigurationFactory.buildRabbitMqConfiguration();

        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);

        var videoFetcher = new EventVideoFetcher();
        var sseOutput = new DirectPipeEvent(eventSSERegistry);
        var fanout = new FanOutPipeEvent(
                List.of(
                        new FilteredPipeEvent(eventSource -> "xebikart-events".equals(eventSource.getOrigin()), sseOutput),
                        new FilteredPipeEvent(eventSource -> "xebikart-car-video".equals(eventSource.getOrigin()), videoFetcher)
                ),
                executorService
        );

        eventMqttConsumerContainer = new MqttConsumerContainer(
                eventRabbitMqConfiguration,
                fanout,
                executorService
        );

        eventMqttConsumerContainer.start();

        /*
        var videoDirectory = new File("/home/jpthiery/workspace/xebia/xebikart/xebikart-api/src/test/resources/video/tub");
        var videoFetcher = new LocalFileVideoFetcher(videoDirectory);
        */

        var servletContextHandlerConfigurers = EndpointConfiguration.buildServletContextHandlerConfigurers(videoFetcher);

        jettySupport = new JettySupport(jettyConfiguration, servletContextHandlerConfigurers);
        jettySupport.start();

    }

    public void stop() {
        if (jettySupport != null) {
            jettySupport.stop();
            jettySupport = null;
        }
        if (eventMqttConsumerContainer != null) {
            eventMqttConsumerContainer.stop();
            eventMqttConsumerContainer = null;
        }
        executorService.shutdownNow();
    }

}
