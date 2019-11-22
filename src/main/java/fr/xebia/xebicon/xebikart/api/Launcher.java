package fr.xebia.xebicon.xebikart.api;

import fr.xebia.xebicon.xebikart.api.application.bus.DirectPipeEvent;
import fr.xebia.xebicon.xebikart.api.application.bus.FanOutPipeEvent;
import fr.xebia.xebicon.xebikart.api.application.bus.FilteredPipeEvent;
import fr.xebia.xebicon.xebikart.api.application.configuration.ConfigurationFactory;
import fr.xebia.xebicon.xebikart.api.application.configuration.EndpointConfiguration;
import fr.xebia.xebicon.xebikart.api.application.cqrs.*;
import fr.xebia.xebicon.xebikart.api.application.cqrs.mode.CqrsModeService;
import fr.xebia.xebicon.xebikart.api.application.cqrs.mode.Mode;
import fr.xebia.xebicon.xebikart.api.application.cqrs.mode.ModeEventStoreListenerToSSEEmitter;
import fr.xebia.xebicon.xebikart.api.infra.cqrs.InMemoryEventStore;
import fr.xebia.xebicon.xebikart.api.infra.http.endpoint.sse.SSEServletEventEmitterRegistry;
import fr.xebia.xebicon.xebikart.api.infra.http.server.JettySupport;
import fr.xebia.xebicon.xebikart.api.infra.mqtt.EventVideoFetcher;
import fr.xebia.xebicon.xebikart.api.infra.mqtt.MqttConsumerContainer;
import io.prometheus.client.hotspot.DefaultExports;

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


    private void start() {

        var store = new InMemoryEventStore();

        var cqrsUniverseEngine = new CqrsEngine<SurveyIdentifier, SurveyState, SurveyCommand, SurveyEvent>(
                store,
                new Survey()
        );
        var cqrsModeEngine = new CqrsEngine<ModeIdentifier, ModeState, ModeCommand, ModeEvent>(
                store,
                new Mode()
        );

        var eventSSERegistry = new SSEServletEventEmitterRegistry();

        var eventRabbitMqConfiguration = ConfigurationFactory.buildRabbitMqConfiguration();

        executorService = Executors.newFixedThreadPool(20);

        var sseOutput = new DirectPipeEvent(eventSSERegistry);
        var videoFetcher = new EventVideoFetcher();
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

        var modeSSERegistry = new SSEServletEventEmitterRegistry();
        store.registerListener(new ModeEventStoreListenerToSSEEmitter(modeSSERegistry, eventMqttConsumerContainer));

        var modeService = new CqrsModeService(cqrsModeEngine);

        var universeEventSSERegistry = new SSEServletEventEmitterRegistry();
        store.registerListener(new SurveyEventStoreListenerToSSEEmitter(universeEventSSERegistry, store));

        var universeService = new CqrsUniverseService(cqrsUniverseEngine, store);

        var sparkEndpoints = EndpointConfiguration.buildSparkEndpoints(universeService, modeService);
        var sparkConfiguration = ConfigurationFactory.buildSparkConfiguration(sparkEndpoints);

        var jettyConfiguration = ConfigurationFactory.buildJettyConfiguration(eventSSERegistry, modeSSERegistry, universeEventSSERegistry, sparkConfiguration);

        // JVM default metrics
        DefaultExports.initialize();

        eventMqttConsumerContainer.start();


        /*
        var videoDirectory = new File("/home/jpthiery/workspace/xebia/xebikart/xebikart-api/src/test/resources/video/tub");
        var videoFetcher = new LocalFileVideoFetcher(videoDirectory);
        */

        var servletContextHandlerConfigurers = EndpointConfiguration.buildServletContextHandlerConfigurers(videoFetcher);

        jettySupport = new JettySupport(jettyConfiguration, servletContextHandlerConfigurers);
        jettySupport.start();
    }

    private void stop() {
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
