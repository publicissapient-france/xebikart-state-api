package fr.xebia.xebicon.xebikart.api.application.bus;

public interface EventReceiver {

    void receive(EventSource eventSource);

}
