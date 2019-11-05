package fr.xebia.xebicon.xebikart.api.application.bus;

import fr.xebia.xebicon.xebikart.api.application.cqrs.*;

import javax.inject.Inject;
import java.util.Optional;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public class InputCqrsBridge<I extends Identifier, C extends Command<I>, E extends Event> implements EventReceiver {

    private final EventSourceToCommandConverter<I> commandConverter;

    private final CqrsEventToEventSourceConverter cqrsEventConverter;

    private final CqrsEngine cqrsEngine;

    private final EventReceiver output;

    @Inject
    public InputCqrsBridge(EventSourceToCommandConverter<I> commandConverter, CqrsEventToEventSourceConverter<E> cqrsEventConverter, CqrsEngine cqrsEngine, EventReceiver output) {
        requireNonNull(commandConverter);
        requireNonNull(cqrsEventConverter);
        requireNonNull(cqrsEngine);
        requireNonNull(output);
        this.commandConverter = commandConverter;
        this.cqrsEventConverter = cqrsEventConverter;
        this.cqrsEngine = cqrsEngine;
        this.output = output;
    }

    @Override
    public void receive(EventSource eventSource) {
        requireNonNull(eventSource);
        commandConverter.convert(eventSource)
                .map((Function<Command<I>, CommandHandleResult>) cqrsEngine::handleCommand)
                .filter(commandHandleResult -> commandHandleResult instanceof SuccessfulCommandHandled)
                .map(commandHandleResult -> (SuccessfulCommandHandled) commandHandleResult)
                .map(SuccessfulCommandHandled::getEvents)
                .ifPresent(events -> {
                    events.stream()
                            .map((Function<Event, EventSource>) cqrsEventConverter::convert)
                            .forEach(output::receive);
                });
    }

    interface EventSourceToCommandConverter<I extends Identifier> {
        Optional<Command<I>> convert(EventSource eventSource);
    }

    interface CqrsEventToEventSourceConverter<E> {
        EventSource convert(E event);
    }

}
