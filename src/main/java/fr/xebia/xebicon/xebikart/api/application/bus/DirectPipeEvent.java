package fr.xebia.xebicon.xebikart.api.application.bus;

import static java.util.Objects.requireNonNull;

public class DirectPipeEvent implements EventReceiver {

    private final EventEmitter eventEmitter;

    public DirectPipeEvent(EventEmitter eventEmitter) {
        requireNonNull(eventEmitter, "eventEmitter must be defined.");
        this.eventEmitter = eventEmitter;
    }

    @Override
    public void receive(EventSource eventSource) {
        eventEmitter.sendData(eventSource.getPayload());
    }

}
