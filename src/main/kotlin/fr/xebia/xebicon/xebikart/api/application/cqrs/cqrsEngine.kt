package fr.xebia.xebicon.xebikart.api.application.cqrs

import fr.xebia.xebicon.xebikart.api.application.bus.EventEmitter
import fr.xebia.xebicon.xebikart.api.infra.GsonProvider

abstract class Identifier() {
    abstract val id: String
}

object Unknown : Identifier() {
    override val id: String
        get() = "Unknown"
}

interface Event {
    fun happenedDate(): Long
    fun identifier(): Identifier
}

interface State {
    fun identifier(): Identifier
}

object NotExit : State {
    override fun identifier(): Identifier {
        return Unknown
    }
}

interface Command<out I : Identifier> {
    fun identifier(): I
}

sealed class AggregateDecideResult
data class SuccessfulDecideResult(val eventsEmitted: List<Event>) : AggregateDecideResult()
data class FailedDecideResult(val reason: String) : AggregateDecideResult()

interface Aggregate<S : State, in C : Command<Identifier>, E : Event> {

    fun decide(state: S, command: C): AggregateDecideResult

    fun apply(state: S, event: E): S

    fun notExistState(): S

    fun replay(events: List<E>): S {
        var currentState: S = notExistState()
        for (event in events) {
            currentState = apply(currentState, event)
        }
        return currentState
    }

}

interface EventStore {
    fun appendEvents(id: Identifier, events: List<Event>)
    fun <E : Event> getEvents(id: Identifier): List<E>
}

interface EventStoreListener {
    fun <E : Event> eventsAppenned(events: List<E>)
}

interface AggrateStrategy {
    fun <S : State, C : Command<Identifier>, E : Event> aggregateForIdentifier(id: Identifier): Aggregate<S, C, E>?
}

sealed class CommandHandleResult
data class SuccessfulCommandHandled(val events: List<Event>) : CommandHandleResult()
data class FailedCommandHandled(val reason: String) : CommandHandleResult()

class CqrsEngine<I : Identifier, S : State, C : Command<I>, E : Event>(private val eventStore: EventStore, private val aggregate: Aggregate<S, C, E>) {

    fun handleCommand(command: C): CommandHandleResult {
        val identifier = command.identifier()
        val initialEvents: List<E> = eventStore.getEvents(identifier)
        val currentState = aggregate.replay(initialEvents)
        return when (val aggregateDecideResult = aggregate.decide(currentState, command)) {
            is SuccessfulDecideResult -> {
                eventStore.appendEvents(identifier, aggregateDecideResult.eventsEmitted)
                SuccessfulCommandHandled(aggregateDecideResult.eventsEmitted)
            }
            is FailedDecideResult -> FailedCommandHandled(aggregateDecideResult.reason)
        }
    }

    companion object {
        fun <I : Identifier, S : State, C : Command<I>, E : Event> fromCommand(command: C, aggrateStrategy: AggrateStrategy, eventStore: EventStore): CqrsEngine<I, S, C, E>? {
            val aggregate: Aggregate<S, C, E> = aggrateStrategy.aggregateForIdentifier(command.identifier())
                    ?: return null
            return CqrsEngine(
                    eventStore,
                    aggregate
            )
        }
    }

}

class OutputCqrsBusEntrypoint(private val emitter: EventEmitter) : EventStoreListener {

    override fun <E : Event> eventsAppenned(events: List<E>) {
        val gson = GsonProvider.provideGson()
        events.map {
            val json = gson.toJsonTree(it).asJsonObject
            val eventType = it::class.java.simpleName
            json.addProperty("eventType", eventType)
            emitter.send(eventType, json.toString())
        }
    }

}
