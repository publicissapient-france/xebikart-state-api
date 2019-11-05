package fr.xebia.xebicon.xebikart.api.application.cqrs

import java.time.Clock


class Universe(private val clock: Clock = Clock.systemDefaultZone()) : Aggregate<UniverseState, UniverseCommand, UniverseEvent> {

    override fun decide(state: UniverseState, command: UniverseCommand): AggregateDecideResult {
        return SuccessfulDecideResult(
                listOf(UniverseSelected(command.identifier, now()))
        )
    }

    override fun apply(state: UniverseState, event: UniverseEvent): UniverseState {
        return UniverseAvailable(event.identifier)
    }

    override fun notExistState(): UniverseState = UniverseNotExist

    private fun now(): Long = clock.millis()
    
}