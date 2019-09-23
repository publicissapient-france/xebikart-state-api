package fr.xebia.xebicon.xebikart.api.application.bus;

import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static org.slf4j.LoggerFactory.getLogger;

public class FanOutPipeEvent implements EventReceiver {

    private static final Logger LOGGER = getLogger(FanOutPipeEvent.class);

    private final List<Dispatcher> dispatchers;

    public FanOutPipeEvent(List<EventReceiver> eventReceivers, ExecutorService executorService) {
        requireNonNull(eventReceivers, "eventReceivers must be defined.");
        requireNonNull(executorService, "executorService must be defined.");
        this.dispatchers = eventReceivers.stream()
                .map(Dispatcher::new)
                .collect(Collectors.toList());
        this.dispatchers.forEach(executorService::submit);
    }

    @Override
    public void receive(EventSource eventSource) {
        requireNonNull(eventSource, "eventSource must be defined.");
        dispatchers.forEach(dispatcher -> dispatcher.appendEventSource(eventSource));
    }

    private static class Dispatcher implements Runnable {

        private final BlockingQueue<EventSource> buffer;

        private final EventReceiver eventReceiver;

        private Dispatcher(EventReceiver eventReceiver) {
            requireNonNull(eventReceiver, "eventReceiver must be defined.");
            this.eventReceiver = eventReceiver;
            this.buffer = new LinkedBlockingQueue<>();
        }

        void appendEventSource(EventSource eventSource) {
            buffer.add(eventSource);
        }

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                var drained = new ArrayList<EventSource>();
                var nbDrained = buffer.drainTo(drained);
                LOGGER.trace("Drained {} elements", nbDrained);
                drained.forEach(eventReceiver::receive);
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    LOGGER.error("Unable to wait 10 millis", e);
                }
            }
            Thread.interrupted();
        }
    }
}
