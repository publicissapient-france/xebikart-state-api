package fr.xebia.xebicon.xebikart.api.application.configuration;

import fr.xebia.xebicon.xebikart.api.infra.http.endpoint.HealthEndpoint;
import fr.xebia.xebicon.xebikart.api.infra.http.endpoint.RootEndpoint;
import fr.xebia.xebicon.xebikart.api.infra.http.endpoint.SparkEndpoint;
import fr.xebia.xebicon.xebikart.api.infra.http.server.JettySupport;
import org.eclipse.jetty.servlet.ServletContextHandler;

import javax.servlet.http.HttpServlet;
import java.util.List;
import java.util.Set;

public class EndpointConfiguration {

    public static Set<SparkEndpoint> buildSparkEndpoints() {
        return Set.of(
                healthEndpoint()
        );
    }

    public static List<JettySupport.ServletContextHandlerConfigurer> buildServletContextHandlerConfigurers() {
        return List.of(
            rootEndpointConfigurer()
        );
    }

    public static JettySupport.ServletContextHandlerConfigurer rootEndpointConfigurer() {
        return context -> context.addServlet(RootEndpoint.class, "/");
    }

    public static SparkEndpoint healthEndpoint() {
        return new HealthEndpoint();
    }


}
