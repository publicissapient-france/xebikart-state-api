package fr.xebia.xebicon.xebikart.api.application.configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.*;

public class RabbitMqConfiguration {

    private final String host;

    private final int port;

    private final List<String> queueNames;

    private final Credentials credentials;

    public RabbitMqConfiguration(String host, int port, List<String> queueNames, String username, String password) {
        if (isBlank(host)) {
            throw new IllegalArgumentException("host must be defined and be non blank.");
        }
        requireNonNull(queueNames, "queueNames must be defined.");
        this.host = host;
        this.port = port;
        this.queueNames = queueNames;
        if (isNotBlank(username) && isNotBlank(password)) {
            credentials = new Credentials(username, password);
        } else {
            credentials = null;
        }
    }

    public RabbitMqConfiguration(String host, int port, List<String> queueNames) {
        this(host, port, queueNames, null, null);
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }


    public List<String> getQueueNames() {
        return new ArrayList<>(queueNames);
    }

    public Optional<Credentials> getCredentials() {
        return Optional.ofNullable(credentials);
    }

    public static class Credentials {

        private final String username;
        private final String password;

        private Credentials(String username, String password) {
            this.username = username;
            this.password = password;
        }


        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }
    }
}
