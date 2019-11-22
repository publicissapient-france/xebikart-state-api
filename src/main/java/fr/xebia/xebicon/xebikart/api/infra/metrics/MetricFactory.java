package fr.xebia.xebicon.xebikart.api.infra.metrics;

import io.prometheus.client.Counter;
import io.prometheus.client.Histogram;
import io.prometheus.client.Summary;

public class MetricFactory {

    private final static String NAMESPACE = "xebikart_state_api";

    public static Counter createCounter(String name, String description, String ...labels) {
        return Counter.build()
                .namespace(NAMESPACE)
                .name(name)
                .help(description)
                .labelNames(labels)
                .create();
    }

    public static Summary createSummary(String name, String description, String ...labels) {

        return Summary.build()
                .namespace(NAMESPACE)
                .quantile(0.99, 0.05)
                .quantile(0.90, 0.05)
                .name(name)
                .help(description)
                .labelNames(labels)
                .create();
    }

    public static Histogram createHistogram(String name, String description, String ...labels) {

        return Histogram.build()
                .name(NAMESPACE)
                .name(name)
                .help(description)
                .labelNames(labels)
                .create();
    }
}
