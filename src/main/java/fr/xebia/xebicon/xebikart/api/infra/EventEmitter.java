package fr.xebia.xebicon.xebikart.api.infra;

public interface EventEmitter {

    void send(String eventName, String data);

}
