package fr.xebia.xebicon.xebikart.api.infra;

public interface EventReceiver {

    void receive(EventSource eventSource);

}
