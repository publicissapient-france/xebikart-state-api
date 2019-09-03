package fr.xebia.xebicon.xebikart.api.infra;

import static java.util.Objects.requireNonNull;

public class DummyPipeEvent implements EventReceiver {

    private final EventEmitter eventEmitter;

    public DummyPipeEvent(EventEmitter eventEmitter) {
        requireNonNull(eventEmitter, "eventEmitter must be defined.");
        this.eventEmitter = eventEmitter;
    }

    @Override
    public void receive(EventSource eventSource) {
        eventEmitter.sendData(eventSource.getPayload());
    }
}
