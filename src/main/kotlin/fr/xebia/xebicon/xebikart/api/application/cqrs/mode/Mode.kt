package fr.xebia.xebicon.xebikart.api.application.cqrs.mode

import fr.xebia.xebicon.xebikart.api.application.cqrs.*
import java.time.Clock

class Mode(private val clock: Clock = Clock.systemDefaultZone()) : Aggregate<ModeState, ModeCommand, ModeEvent> {

    override fun decide(state: ModeState, command: ModeCommand): AggregateDecideResult =
            when (state) {
                is Default ->
                    when (command) {
                        is SetMode ->
                            SuccessfulDecideResult(listOf(
                                    ModeSet(command.identifier, clock.millis(), command.mode, command.data)))
                    }
            }

    override fun apply(state: ModeState, event: ModeEvent): ModeState = state

    override fun notExistState() = Default

}
