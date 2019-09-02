package fr.xebia.xebicon.xebikart.api.infra.amqp;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Delivery;
import fr.xebia.xebicon.xebikart.api.application.configuration.RabbitMqConfiguration;
import fr.xebia.xebicon.xebikart.api.infra.EventReceiver;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static java.util.Objects.requireNonNull;
import static org.slf4j.LoggerFactory.getLogger;

public class AmqpConsumer implements DeliverCallback {

    private static final Logger LOGGER = getLogger(AmqpConsumer.class);

    private final RabbitMqConfiguration rabbitMqConfiguration;

    private final List<EventReceiver> eventReceivers;

    private Connection connection;

    public AmqpConsumer(RabbitMqConfiguration rabbitMqConfiguration, List<EventReceiver> eventReceivers) {
        requireNonNull(rabbitMqConfiguration, "rabbitMqConfiguration must be defined.");
        requireNonNull(eventReceivers, "eventReceivers must be defined.");
        this.rabbitMqConfiguration = rabbitMqConfiguration;
        this.eventReceivers = eventReceivers;
    }

    public synchronized void start() {
        var connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(rabbitMqConfiguration.getHost());
        connectionFactory.setPort(rabbitMqConfiguration.getPort());

        rabbitMqConfiguration.getCredentials().ifPresent(credentials -> {
            connectionFactory.setUsername(credentials.getUsername());
            connectionFactory.setPassword(credentials.getPassword());
        });

        try {
            connection = connectionFactory.newConnection();
            var channel = connection.createChannel();

            rabbitMqConfiguration.getQueueNames()
                    .forEach(queueName -> {
                        try {
                            channel.queueDeclare(queueName, false, false, false, null);
                            channel.basicConsume(queueName, true, this, consumerTag -> {
                            });
                        } catch (IOException e) {
                            LOGGER.error("Unable to declare queue {}", queueName, e);
                        }
                    });

        } catch (IOException | TimeoutException e) {
            throw new RuntimeException("Unable to connect to Rabbit mq on host " + rabbitMqConfiguration.getHost(), e);
        }

    }

    @Override
    public void handle(String consumerTag, Delivery delivery) throws IOException {
        var message = new String(delivery.getBody(), StandardCharsets.UTF_8);
        eventReceivers.forEach(eventReceiver -> eventReceiver.receive(message));
    }

    public synchronized void stop() {
        if (connection != null) {
            try {
                connection.close();
            } catch (IOException e) {
                LOGGER.error("unable to close consumer.", e);
            }
            connection = null;
        }
    }
}
