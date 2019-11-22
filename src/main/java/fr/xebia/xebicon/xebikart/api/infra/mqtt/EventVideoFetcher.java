package fr.xebia.xebicon.xebikart.api.infra.mqtt;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import fr.xebia.xebicon.xebikart.api.application.VideoFetcher;
import fr.xebia.xebicon.xebikart.api.application.bus.EventReceiver;
import fr.xebia.xebicon.xebikart.api.application.bus.EventSource;
import fr.xebia.xebicon.xebikart.api.application.model.CarVideoFrame;
import io.reactivex.Flowable;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.Base64;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;

import static java.util.Objects.requireNonNull;
import static org.slf4j.LoggerFactory.getLogger;

public class EventVideoFetcher implements VideoFetcher, EventReceiver {

    private static final Logger LOGGER = getLogger(EventVideoFetcher.class);

    private final LinkedBlockingQueue<CarVideoFrame> buffer = new LinkedBlockingQueue<>();


    @Override
    public Flowable<CarVideoFrame> video() {
        return Flowable.fromIterable(new InternalIterable());
    }

    @Override
    public void receive(EventSource eventSource) {
        requireNonNull(eventSource, "eventSource must be defined.");
        var jsonParser = new JsonParser();
        var jsonStr = eventSource.getPayloadAsString();
        try {
            var json = (JsonObject) jsonParser.parse(jsonStr);
            var payload = json.getAsJsonPrimitive("frame").getAsString();
            var carId = json.getAsJsonPrimitive("car").getAsInt();
            var content = Base64.getMimeDecoder().decode(payload);
            var carVideoFrame = new CarVideoFrame("mqtt", carId, content);
            buffer.offer(carVideoFrame);
        } catch (JsonParseException e) {
            LOGGER.error("Unable to parse json : {}", jsonStr);
        }
    }

    private class InternalIterable implements Iterator<CarVideoFrame>, Iterable<CarVideoFrame> {

        @NotNull
        @Override
        public Iterator<CarVideoFrame> iterator() {
            return this;
        }

        @Override
        public boolean hasNext() {
            var res = buffer.size() > 0;
            var attempt = 0;
            while (!res && attempt < 100) {
                res = retry();
                attempt++;
            }
            return res;
        }

        private boolean retry() {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return buffer.size() > 0;
        }

        @Override
        public CarVideoFrame next() {
            try {
                return buffer.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
                return new CarVideoFrame("empty", 0, new byte[0]);
            }
        }
    }

}
