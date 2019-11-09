package fr.xebia.xebicon.xebikart.api.application.cqrs

import java.time.Clock

data class BoatIdentifier(override val id: String) : Identifier()

object BoatNotExist : BoatState() {
    override val identifier: BoatIdentifier
        get() = BoatIdentifier(Unknown.id )
}

class Boat(val clock: Clock = Clock.systemDefaultZone()) :
        Aggregate<BoatState, BoatCommand, BoatEvent> {

    override fun decide(state: BoatState, command: BoatCommand): AggregateDecideResult = when (state) {
        is BoatMoored -> {
            when (command) {
                is LeaveHarbor -> {
                    SuccessfulDecideResult(
                            listOf(
                                    BoatLeavedHarbor(command.identifier(), clock.millis(), command.initialPosition, command.destination)
                            )
                    )
                }
                is DitchToHarbor -> FailedDecideResult("Boat is already in harbor ${state.harbor}")
            }
        }
        is BoatNavigate -> {
            when (command) {
                is LeaveHarbor -> FailedDecideResult("Boat is already navigating to ${state.to}")
                is DitchToHarbor -> {
                    if (state.to == command.destination) {
                        SuccessfulDecideResult(
                                listOf(
                                        BoatArrivedToHarbor(command.identifier, clock.millis(), command.destination)
                                )
                        )
                    } else {
                        FailedDecideResult("Boat destination is ${state.to}, not ${command.destination}")
                    }
                }
            }
        }
        BoatNotExist -> {
            when (command) {
                is LeaveHarbor ->
                    SuccessfulDecideResult(
                            listOf(
                                    BoatLeavedHarbor(command.identifier(), clock.millis(), command.initialPosition, command.destination)
                            )
                    )
                is DitchToHarbor ->
                    SuccessfulDecideResult(
                            listOf(
                                    BoatArrivedToHarbor(command.identifier, clock.millis(), command.destination)
                            )
                    )
            }
        }


    }

    override fun notExistState(): BoatState {
        return BoatNotExist
    }

    override fun apply(state: BoatState, event: BoatEvent): BoatState = when (state) {
        is BoatMoored -> {
            when (event) {
                is BoatArrivedToHarbor -> state
                is BoatLeavedHarbor -> BoatNavigate(
                        state.identifier,
                        state.harbor,
                        event.destination
                )
            }
        }
        is BoatNavigate -> {
            when (event) {
                is BoatArrivedToHarbor -> BoatMoored(state.identifier, event.to)
                is BoatLeavedHarbor -> state
            }
        }
        BoatNotExist -> {
            when (event) {
                is BoatArrivedToHarbor -> BoatMoored(event.identifier, event.to)
                is BoatLeavedHarbor -> BoatNavigate(event.identifier, event.from, event.destination)
            }
        }
    }


}

data class Harbor(val city: String)

//  State
sealed class BoatState() : State {
    abstract val identifier: BoatIdentifier
    override fun identifier(): BoatIdentifier {
        return identifier
    }
}

data class BoatNavigate(override val identifier: BoatIdentifier, val from: Harbor, val to: Harbor) : BoatState()
data class BoatMoored(override val identifier: BoatIdentifier, val harbor: Harbor) : BoatState()

//  Event
sealed class BoatEvent() : Event {

    abstract val identifier: BoatIdentifier
    abstract val happenedDate: Long
    override fun identifier(): Identifier {
        return identifier
    }

    override fun happenedDate(): Long {
        return happenedDate
    }
}

data class BoatLeavedHarbor(
        override val identifier: BoatIdentifier,
        override val happenedDate: Long,
        val from: Harbor,
        val destination: Harbor
) : BoatEvent()

data class BoatArrivedToHarbor(
        override val identifier: BoatIdentifier,
        override val happenedDate: Long,
        val to: Harbor
) : BoatEvent()

//  Command
sealed class BoatCommand() : Command<BoatIdentifier> {
    abstract val identifier: BoatIdentifier
    override fun identifier(): BoatIdentifier {
        return identifier
    }
}

data class LeaveHarbor(
        override val identifier: BoatIdentifier,
        val initialPosition: Harbor,
        val destination: Harbor
) : BoatCommand()

data class DitchToHarbor(
        override val identifier: BoatIdentifier,
        val destination: Harbor
) : BoatCommand()

object BoatIdentifierStrategy : AggrateStrategy {

    override fun <S : State, C : Command<Identifier>, E : Event> aggregateForIdentifier(id: Identifier): Aggregate<S, C, E>? = when (id) {
        is BoatIdentifier -> Boat() as Aggregate<S, C, E>
        else -> null
    }

}

class TestEventStore : EventStore {

    private val cache: MutableMap<Identifier, List<Event>> = mutableMapOf()

    override fun appendEvents(id: Identifier, events: List<Event>) {
        val currentEvents = cache[id] ?: mutableListOf()

        val nextEvents = currentEvents.toMutableList()
        nextEvents.addAll(events)
        cache[id] = nextEvents

    }

    override fun <E : Event> getEvents(id: Identifier): List<E> {
        return cache[id] as List<E>? ?: emptyList()
    }

    fun <E: Event> getAllEvents(): List<E> {
        return cache.values.toList()[0] as List<E>

    }
}


val titanic = BoatIdentifier("Titanic")
val brest = Harbor("Brest")
val havre = Harbor("Havre")
val newYork = Harbor("New-York")

val bestBoatSurveyIdentifier = SurveyIdentifier("best boat")
val bestBoatSurveyName = "Best boat survey"