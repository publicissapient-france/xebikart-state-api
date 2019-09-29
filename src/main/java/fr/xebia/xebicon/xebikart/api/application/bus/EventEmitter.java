package fr.xebia.xebicon.xebikart.api.application.bus;

public interface EventEmitter {

    void send(String eventName, String data);

    default void sendData(String data) {
        send("incomingData", data);
    }

}
