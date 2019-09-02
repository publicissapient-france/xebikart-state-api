package fr.xebia.xebicon.xebikart.api.application.configuration;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class JettyConfiguration {

    private final int port;

    private final SparkConfiguration sparkConfiguration;

    private final List<SSEConfiguration> sseConfigurations;

    public JettyConfiguration(int port, SparkConfiguration sparkConfiguration, List<SSEConfiguration> sseConfigurations) {
        this.port = port;
        this.sparkConfiguration = sparkConfiguration;
        this.sseConfigurations = sseConfigurations;
    }

    public int getPort() {
        return port;
    }

    public Optional<SparkConfiguration> getSparkConfiguration() {
        return Optional.ofNullable(sparkConfiguration);
    }

    public List<SSEConfiguration> getSseConfigurations() {
        return Optional.ofNullable(sseConfigurations).orElse(Collections.emptyList());
    }

}
