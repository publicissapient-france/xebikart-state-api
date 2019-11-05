package fr.xebia.xebicon.xebikart.api.application.bus;

import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;

public class FilteredPipeEvent implements EventReceiver {

    private final Predicate<EventSource> predicate;

    private final EventReceiver output;

    public FilteredPipeEvent(Predicate<EventSource> predicate, EventReceiver output) {
        requireNonNull(predicate, "predicate must be defined.");
        requireNonNull(output, "output must be defined.");
        this.predicate = predicate;
        this.output = output;
    }


    @Override
    public void receive(EventSource eventSource) {
        if (predicate.test(eventSource)) {
            output.receive(eventSource);
        }
    }
}
