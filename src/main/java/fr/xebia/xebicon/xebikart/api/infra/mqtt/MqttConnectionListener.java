package fr.xebia.xebicon.xebikart.api.infra.mqtt;

import com.hivemq.client.mqtt.lifecycle.MqttClientConnectedContext;
import com.hivemq.client.mqtt.lifecycle.MqttClientConnectedListener;
import com.hivemq.client.mqtt.lifecycle.MqttClientDisconnectedContext;
import com.hivemq.client.mqtt.lifecycle.MqttClientDisconnectedListener;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class MqttConnectionListener implements MqttClientConnectedListener, MqttClientDisconnectedListener {

    private static final Logger LOGGER = getLogger(MqttConnectionListener.class);

    @Override
    public void onConnected(@NotNull MqttClientConnectedContext context) {
        var clientConfig = context.getClientConfig();
        LOGGER.info("MQTT client connected to {}:{}", clientConfig.getServerHost(), clientConfig.getServerPort());
    }

    @Override
    public void onDisconnected(@NotNull MqttClientDisconnectedContext context) {
        LOGGER.error("MQTT client disconnected.");
    }

}
