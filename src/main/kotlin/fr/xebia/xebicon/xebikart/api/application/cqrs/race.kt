package fr.xebia.xebicon.xebikart.api.application.cqrs

import java.time.Clock


class Race(private val clock: Clock = Clock.systemDefaultZone()) : Aggregate<RaceState, RaceCommand, RaceEvent> {

    override fun decide(state: RaceState, command: RaceCommand): AggregateDecideResult {
        val decided = when (state) {
            RaceNotExist -> decideOnRaceNotExist(command)
            is RacePlanned -> decideOnRacePlanned(state, command)
            is RaceReady -> decideOnRaceReady(state, command)
            is RaceRunning -> decideOnRaceRunning(state, command)
            else -> Either.left("Not yet implemented command $command")
        }
        return decided
                .fold(
                        { reason -> FailedDecideResult(reason) },
                        { events -> SuccessfulDecideResult(events) }
                )
    }


    override fun apply(state: RaceState, event: RaceEvent): RaceState {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun notExistState(): RaceState = RaceNotExist

    private fun now(): Long = clock.millis()

    private fun decideOnRaceNotExist(command: RaceCommand): Either<String, List<Event>> = when (command) {
        is CreateRace -> Either.right(
                listOf(
                        RaceCreated(command.identifier, now())
                )
        )
        else -> Either.left("Command $command not allow for a Race which not exist")
    }

    private fun decideOnRacePlanned(state: RacePlanned, command: RaceCommand): Either<String, List<Event>> = when (command) {
        is SubscribeCarToRace -> {
            if (state.cars.contains(command.car)) {
                Either.left("Car ${command.car} ahd already subscribe to race ${state.identifier}")
            } else {
                Either.right(
                        listOf(
                                CarSubscribed(state.identifier, now(), command.car)
                        )
                )
            }
        }
        is UnsubscribeCarToRace -> {
            if (state.cars.contains(command.car)) {
                Either.right(
                        listOf(
                                CarUnsubscribed(state.identifier, now(), command.car)
                        )
                )
            } else {
                Either.left("Car ${command.car} had not be registered on race ${state.identifier}")
            }
        }
        is CloseCarSubscription -> {
            if (state.cars.isEmpty()) {
                Either.right(
                        listOf(
                                RaceAborted(state.identifier, now())
                        )
                )
            } else {
                Either.right(
                        listOf(
                                RaceClosedForSubscription(state.identifier, now())
                        )
                )
            }
        }
        else -> Either.left("Command $command not allow for a Race which is planned")
    }

    private fun decideOnRaceReady(state: RaceReady, command: RaceCommand): Either<String, List<Event>> = when (command) {
        is StartRace -> Either.right(
                listOf(
                        RaceStarted(state.identifier, now())
                )
        )
        else -> Either.left("Command $command not allow for a Race which is ready")
    }

    private fun decideOnRaceRunning(state: RaceRunning, command: RaceCommand): Either<String, List<Event>> = when (command) {
        is CarWinPosition -> {
            if (state.cars.contains(command.car)) {
                val currentPosition = state.cars.indexOf(command.car)
                val delta = currentPosition - command.nbPositionWon
                val nbPositionWon = if (delta <= 0) {
                    0
                } else {
                    delta
                }
                if (nbPositionWon == 0) {
                    Either.right(listOf<Event>())
                } else {
                    Either.right(listOf(
                            CarPositionWon(state.identifier, now(), command.car, nbPositionWon)
                    ))
                }

            } else {
                Either.left("Car ${command.car} not running in this race.")
            }
        }
        else -> Either.left("Command $command not allow for a Race which is running")
    }


}

