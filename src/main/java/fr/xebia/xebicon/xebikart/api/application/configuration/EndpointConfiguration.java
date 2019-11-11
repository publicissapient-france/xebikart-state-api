package fr.xebia.xebicon.xebikart.api.application.configuration;

import fr.xebia.xebicon.xebikart.api.application.UniverseService;
import fr.xebia.xebicon.xebikart.api.application.VideoFetcher;
import fr.xebia.xebicon.xebikart.api.application.cqrs.*;
import fr.xebia.xebicon.xebikart.api.application.cqrs.mode.ModeService;
import fr.xebia.xebicon.xebikart.api.infra.http.endpoint.*;
import fr.xebia.xebicon.xebikart.api.infra.http.server.JettySupport;
import org.eclipse.jetty.servlet.ServletHolder;

import java.util.List;
import java.util.Set;

public class EndpointConfiguration {

    public static Set<SparkEndpoint> buildSparkEndpoints(
            UniverseService universeService,
            ModeService modeService
    ) {
        return Set.of(
                healthEndpoint(),
                universeEndpoint(universeService),
                modeEndpoint(modeService)
        );
    }

    public static List<JettySupport.ServletContextHandlerConfigurer> buildServletContextHandlerConfigurers(VideoFetcher videoFetcher) {
        return List.of(
                rootEndpointConfigurer(),
                videoHtmlEndpointConfigurer(),
                videoEndpointConfigurer(videoFetcher)
        );
    }

    private static JettySupport.ServletContextHandlerConfigurer rootEndpointConfigurer() {
        return context -> context.addServlet(RootEndpoint.class, "/");
    }

    private static JettySupport.ServletContextHandlerConfigurer videoEndpointConfigurer(VideoFetcher videoFetcher) {
        var servlet = new VideoHttpServlet(videoFetcher);
        return context -> context.addServlet(new ServletHolder(servlet), "/car/video");
    }

    private static JettySupport.ServletContextHandlerConfigurer videoHtmlEndpointConfigurer() {
        return context -> context.addServlet(new ServletHolder(new VideoHtmlPage()), "/car");
    }

    private static SparkEndpoint healthEndpoint() {
        return new HealthEndpoint();
    }

    private static SparkEndpoint universeEndpoint(UniverseService universeService) {
        return new UniverseEndpoint(universeService);
    }

    private static SparkEndpoint modeEndpoint(ModeService modeService) {
        return new ModeEndpoint(modeService);
    }

}
