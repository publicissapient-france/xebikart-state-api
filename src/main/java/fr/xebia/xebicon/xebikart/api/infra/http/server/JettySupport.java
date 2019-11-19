package fr.xebia.xebicon.xebikart.api.infra.http.server;

import fr.xebia.xebicon.xebikart.api.application.configuration.JettyConfiguration;
import io.prometheus.client.jetty.JettyStatisticsCollector;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.StatisticsHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Spark;

import javax.inject.Inject;
import javax.servlet.DispatcherType;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class JettySupport {
    private static final Logger LOGGER = LoggerFactory.getLogger(JettySupport.class);
    private static final String KEYSTORE_PASSWORD = "";
    private final JettyConfiguration configuration;
    private final List<ServletContextHandlerConfigurer> servletContextHandlerConfigurers;
    private final Object monitor = new Object();
    private Server server;

    @Inject
    public JettySupport(JettyConfiguration configuration, List<ServletContextHandlerConfigurer> servletContextHandlerConfigurers) {
        requireNonNull(configuration, "configuration must be defined.");
        this.configuration = configuration;
        if (servletContextHandlerConfigurers == null) {
            this.servletContextHandlerConfigurers = Collections.emptyList();
        } else {
            this.servletContextHandlerConfigurers = servletContextHandlerConfigurers;
        }
    }

    public JettySupport(JettyConfiguration configuration) {
        this(configuration, null);
    }

    public void start() {
        if (server == null) {
            synchronized (monitor) {
                if (server == null) {
                    doStart();
                }
            }
        }
    }

    public void stop() {
        if (server != null) {
            synchronized (monitor) {
                if (server != null) {
                    try {
                        LOGGER.info("Stopping Http server.");
                        Spark.stop();

                        server.stop();
                    } catch (Exception e) {
                        LOGGER.error("Unable to stop Jetty server.", e);
                    } finally {
                        server = null;
                    }
                }
            }
        }
    }

    public boolean isRunning() {
        return server != null;
    }

    private void doStart() {
        CountDownLatch startLatch = new CountDownLatch(1);
        server = createAndConfigureJettyServer();

        // Prometheus metrics
        StatisticsHandler stats = new StatisticsHandler();
        stats.setHandler(server.getHandler());
        server.setHandler(stats);
        new JettyStatisticsCollector(stats).register();

        LOGGER.info("Starting web server on port {}.", configuration.getPort());
        new Thread(() -> {
            try {
                server.start();
                startLatch.countDown();
                server.join();
            } catch (Exception e) {
                LOGGER.error("Unable to start Jetty server.", e);
            }
        }).start();
        try {
            if (startLatch.await(10, TimeUnit.SECONDS)) {
                LOGGER.info("Web server started on port {}.", configuration.getPort());
            } else {
                LOGGER.error("Unable to start Jetty after 10 seconds.");
                try {
                    server.stop();
                } catch (Exception e) {
                    LOGGER.info("Not able to request Jetty stop after failing start.", e);
                }
                server = null;
            }
        } catch (InterruptedException e) {
            LOGGER.error("Unable to start Jetty server.", e);
            server = null;
            Thread.currentThread().interrupt();
        }
    }

    private Server createAndConfigureJettyServer() {

        var server = new Server(configuration.getPort());

        //  Disable display version
        //  Source : https://stackoverflow.com/questions/15652902/remove-the-http-server-header-in-jetty-9
        Stream.of(server.getConnectors())
                .flatMap(connector -> connector.getConnectionFactories().stream())
                .filter(connFactory -> connFactory instanceof HttpConnectionFactory)
                .forEach(httpConnFactory -> ((HttpConnectionFactory) httpConnFactory).getHttpConfiguration().setSendServerVersion(false));

        var context = new ServletContextHandler();

        servletContextHandlerConfigurers.forEach(configurer -> configurer.configureServletContextHandler(context));

        configuration.getSseConfigurations().forEach(sseConfiguration -> {
            //  SSE power
            var sseServletHolder = new ServletHolder(sseConfiguration.getServlet());
            sseServletHolder.setAsyncSupported(true);
            context.addServlet(sseServletHolder, sseConfiguration.getPath());
        });


        configuration.getSparkConfiguration().ifPresent(sparkConfiguration -> {
            var sparkFilter = sparkConfiguration.getSparkFilter();

            var sparkFilterHolder = new FilterHolder(sparkFilter);

            context.addFilter(sparkFilterHolder, sparkConfiguration.getJettySparkFilterPrefix(), EnumSet.allOf(DispatcherType.class));

        });

        server.setHandler(context);
        return server;
    }

    public interface ServletContextHandlerConfigurer {
        void configureServletContextHandler(ServletContextHandler context);
    }

}
