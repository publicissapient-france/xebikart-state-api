package fr.xebia.xebicon.xebikart.api.infra.mqtt;

import fr.xebia.xebicon.xebikart.api.application.VideoFetcher;
import fr.xebia.xebicon.xebikart.api.application.bus.EventReceiver;
import fr.xebia.xebicon.xebikart.api.application.bus.EventSource;
import fr.xebia.xebicon.xebikart.api.application.model.CarVideoFrame;
import io.reactivex.Flowable;

import java.util.concurrent.LinkedBlockingQueue;

import static java.util.Objects.requireNonNull;

public class MqttVideoFetcher implements VideoFetcher, EventReceiver {

    private final LinkedBlockingQueue<CarVideoFrame> buffer = new LinkedBlockingQueue<>();

    @Override
    public Flowable<CarVideoFrame> video() {
        return Flowable.fromIterable(buffer);
    }

    @Override
    public void receive(EventSource eventSource) {
        requireNonNull(eventSource, "eventSource must be defined.");
        
    }

}
