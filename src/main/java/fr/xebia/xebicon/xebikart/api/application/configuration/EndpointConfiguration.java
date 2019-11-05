package fr.xebia.xebicon.xebikart.api.application.configuration;

import fr.xebia.xebicon.xebikart.api.application.VideoFetcher;
import fr.xebia.xebicon.xebikart.api.application.cqrs.*;
import fr.xebia.xebicon.xebikart.api.infra.http.endpoint.*;
import fr.xebia.xebicon.xebikart.api.infra.http.server.JettySupport;
import org.eclipse.jetty.servlet.ServletHolder;

import java.util.List;
import java.util.Set;

public class EndpointConfiguration {

    public static Set<SparkEndpoint> buildSparkEndpoints(CqrsEngine<UniverseIdentifier, UniverseState, UniverseCommand, UniverseEvent> cqrsEngine) {
        return Set.of(
                healthEndpoint(),
                universeEndpoint(cqrsEngine)
        );
    }

    public static List<JettySupport.ServletContextHandlerConfigurer> buildServletContextHandlerConfigurers(VideoFetcher videoFetcher) {
        return List.of(
                rootEndpointConfigurer(),
                videoHtmlEndpointConfigurer(),
                videoEndpointConfigurer(videoFetcher)
        );
    }

    public static JettySupport.ServletContextHandlerConfigurer rootEndpointConfigurer() {
        return context -> context.addServlet(RootEndpoint.class, "/");
    }

    public static JettySupport.ServletContextHandlerConfigurer videoEndpointConfigurer(VideoFetcher videoFetcher) {
        var servlet = new VideoHttpServlet(videoFetcher);
        return context -> context.addServlet(new ServletHolder(servlet), "/car/video");
    }

    public static JettySupport.ServletContextHandlerConfigurer videoHtmlEndpointConfigurer() {
        return context -> context.addServlet(new ServletHolder(new VideoHtmlPage()), "/car");
    }

    public static SparkEndpoint healthEndpoint() {
        return new HealthEndpoint();
    }

    public static SparkEndpoint universeEndpoint(CqrsEngine<UniverseIdentifier, UniverseState, UniverseCommand, UniverseEvent> cqrsEngine) {
        return new UniverseEndpoint(cqrsEngine);
    }

}
