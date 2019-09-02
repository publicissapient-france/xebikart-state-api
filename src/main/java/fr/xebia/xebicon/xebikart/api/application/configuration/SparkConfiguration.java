package fr.xebia.xebicon.xebikart.api.application.configuration;

import fr.xebia.xebicon.xebikart.api.infra.http.endpoint.SparkEndpoint;
import fr.xebia.xebicon.xebikart.api.infra.http.server.SparkEndpointAdapter;
import spark.servlet.SparkApplication;
import spark.servlet.SparkFilter;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import java.util.Collections;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class SparkConfiguration {

    private final String sparkFilterPrefix;

    private final SparkFilter sparkFilter;

    public SparkConfiguration(String sparkFilterPrefix, SparkApplication sparkApplication, boolean withMetrics) {
        if (isBlank(sparkFilterPrefix)) {
            throw new IllegalArgumentException("sparkFilterPrefix must be defined and be non blank.");
        }
        requireNonNull(sparkApplication, "sparkApplication must be defined.");
        if (!sparkFilterPrefix.endsWith("*")) {
            if (!sparkFilterPrefix.endsWith("/*")) {
                this.sparkFilterPrefix = sparkFilterPrefix + "/*";
            } else {
                this.sparkFilterPrefix = sparkFilterPrefix + "*";
            }
        } else {
            this.sparkFilterPrefix = sparkFilterPrefix;
        }
        sparkFilter = new SparkFilter() {
            @Override
            protected SparkApplication[] getApplications(FilterConfig filterConfig) throws ServletException {
                return new SparkApplication[]{sparkApplication};
            }
        };
    }

    public SparkConfiguration(String sparkFilterPrefix, SparkEndpoint sparkEndpoint, boolean withMetrics) {
        this(
                sparkFilterPrefix,
                new SparkEndpointAdapter(sparkFilterPrefix, Collections.singleton(sparkEndpoint)),
                withMetrics
        );
    }

    public String getJettySparkFilterPrefix() {
        return sparkFilterPrefix;
    }

    public SparkFilter getSparkFilter() {
        return sparkFilter;
    }
}
