package fr.xebia.xebicon.xebikart.api.application.cqrs

data class RaceIdentifier(override val id: String) : Identifier()
data class CarIdentifier(override val id: String) : Identifier()


sealed class RaceState() : State {
    abstract val identifier: RaceIdentifier
    override fun identifier(): RaceIdentifier {
        return identifier
    }
}

data class RacePlanned(override val identifier: RaceIdentifier, val startDate: Long, val cars: List<CarIdentifier>) : RaceState()
data class RaceReady(override val identifier: RaceIdentifier, val startDate: Long, val cars: List<CarIdentifier>) : RaceState()
data class RaceRunning(override val identifier: RaceIdentifier, val startedDate: Long, val cars: List<CarIdentifier>) : RaceState()
data class RaceComplete(override val identifier: RaceIdentifier, val startedDate: Long, val endedDate: Long, val cars: List<CarIdentifier>) : RaceState()

sealed class RaceCommand() : Command<RaceIdentifier> {
    abstract val identifier: RaceIdentifier
    override fun identifier(): RaceIdentifier {
        return identifier
    }
}

data class CreateRace(override val identifier: RaceIdentifier, val startDate: Long) : RaceCommand()
data class SubscribeCarToRace(override val identifier: RaceIdentifier, val car: CarIdentifier) : RaceCommand()
data class UnsubscribeCarToRace(override val identifier: RaceIdentifier, val car: CarIdentifier) : RaceCommand()
data class CloseCarSubscription(override val identifier: RaceIdentifier) : RaceCommand()
data class StartRace(override val identifier: RaceIdentifier, val startedDate: Long) : RaceCommand()
data class CarWinPosition(override val identifier: RaceIdentifier, val car: CarIdentifier, val nbPositionWon: Int = 1) : RaceCommand()
data class EndRace(override val identifier: RaceIdentifier, val endedDate: Long) : RaceCommand()

sealed class RaceEvent() : Event {
    abstract val identifier: RaceIdentifier
    abstract val happenedDate: Long
    override fun identifier(): RaceIdentifier {
        return identifier
    }

    override fun happenedDate(): Long {
        return happenedDate
    }
}

data class RaceCreated(override val identifier: RaceIdentifier, override val happenedDate: Long) : RaceEvent()
data class CarSubscribed(override val identifier: RaceIdentifier, override val happenedDate: Long, val car: CarIdentifier) : RaceEvent()
data class CarUnsubscribed(override val identifier: RaceIdentifier, override val happenedDate: Long, val car: CarIdentifier) : RaceEvent()
data class RaceClosedForSubscription(override val identifier: RaceIdentifier, override val happenedDate: Long) : RaceEvent()
data class RaceStarted(override val identifier: RaceIdentifier, override val happenedDate: Long) : RaceEvent()
data class CarPositionWon(override val identifier: RaceIdentifier, override val happenedDate: Long, val car: CarIdentifier, val nbPositionWon: Int) : RaceEvent()
data class RaceEnd(override val identifier: RaceIdentifier, override val happenedDate: Long) : RaceEvent()
data class RaceAborted(override val identifier: RaceIdentifier, override val happenedDate: Long) : RaceEvent()

object RaceNotExist : RaceState() {
    override val identifier: RaceIdentifier
        get() = RaceIdentifier(Unknown.id)
}

data class UniverseIdentifier(override val id: String) : Identifier()

sealed class UniverseState() : State {
    abstract val identifier: UniverseIdentifier
    override fun identifier(): UniverseIdentifier {
        return identifier
    }
}

object UniverseNotExist : UniverseState() {
    override val identifier: UniverseIdentifier
        get() = UniverseIdentifier(Unknown.id)
}

object Default : ModeState() {
    override val identifier: ModeIdentifier
        get() = ModeIdentifier(Unknown.id)
}

sealed class UniverseCommand() : Command<UniverseIdentifier> {
    abstract val identifier: UniverseIdentifier
    override fun identifier(): UniverseIdentifier {
        return identifier
    }
}

sealed class UniverseEvent() : Event {
    abstract val identifier: UniverseIdentifier
    abstract val happenedDate: Long
    override fun identifier(): UniverseIdentifier {
        return identifier
    }

    override fun happenedDate(): Long {
        return happenedDate
    }
}


data class SwitchUniverse(override val identifier: UniverseIdentifier) : UniverseCommand()

data class UniverseAvailable(override val identifier: UniverseIdentifier) : UniverseState()

data class UniverseSelected(override val identifier: UniverseIdentifier, override val happenedDate: Long) : UniverseEvent()


data class SurveyIdentifier(override val id: String) : Identifier()

data class ModeIdentifier(override val id: String) : Identifier()

sealed class SurveyState() : State {
    abstract val identifier: SurveyIdentifier
    override fun identifier(): SurveyIdentifier {
        return identifier
    }
}

sealed class ModeState() : State {
    abstract val identifier: ModeIdentifier
    override fun identifier(): ModeIdentifier {
        return identifier
    }
}

object SurveyNotExist : SurveyState() {
    override val identifier: SurveyIdentifier
        get() = SurveyIdentifier(Unknown.id)
}

sealed class SurveyCommand() : Command<SurveyIdentifier> {
    abstract val identifier: SurveyIdentifier
    override fun identifier(): SurveyIdentifier {
        return identifier
    }
}

sealed class ModeCommand() : Command<ModeIdentifier> {
    abstract val identifier: ModeIdentifier
    override fun identifier(): ModeIdentifier {
        return identifier
    }
}

sealed class SurveyEvent() : Event {
    abstract val identifier: SurveyIdentifier
    abstract val happenedDate: Long
    override fun identifier(): SurveyIdentifier {
        return identifier
    }

    override fun happenedDate(): Long {
        return happenedDate
    }
}

sealed class ModeEvent() : Event {
    abstract val identifier: ModeIdentifier
    abstract val happenedDate: Long
    override fun identifier(): ModeIdentifier {
        return identifier
    }

    override fun happenedDate(): Long {
        return happenedDate
    }
}

data class SurveyEditing(override val identifier: SurveyIdentifier, val name: String, val choices: List<String>) : SurveyState()
data class SurveyWaitingVote(override val identifier: SurveyIdentifier, val name: String, val votes: Map<String, Int>) : SurveyState()
data class SurveyCompleted(override val identifier: SurveyIdentifier, val name: String, val votes: Map<String, Int>) : SurveyState()

data class CreateSurvey(override val identifier: SurveyIdentifier, val name: String) : SurveyCommand()
data class AddChoiceToSurvey(override val identifier: SurveyIdentifier, val choice: String) : SurveyCommand()
data class StartSurvey(override val identifier: SurveyIdentifier) : SurveyCommand()
data class AddVoteToChoiceInSurvey(override val identifier: SurveyIdentifier, val vote: Vote) : SurveyCommand()
data class CloseSurvey(override val identifier: SurveyIdentifier) : SurveyCommand()

data class SurveyCreated(override val identifier: SurveyIdentifier, override val happenedDate: Long, val name: String) : SurveyEvent()
data class SurveyChoiceAdded(override val identifier: SurveyIdentifier, override val happenedDate: Long, val choice: String) : SurveyEvent()
data class SurveyStarted(override val identifier: SurveyIdentifier, override val happenedDate: Long) : SurveyEvent()
data class SurveyVoteReceived(override val identifier: SurveyIdentifier, override val happenedDate: Long, val vote: Vote) : SurveyEvent()
data class SurveyClosed(override val identifier: SurveyIdentifier, override val happenedDate: Long) : SurveyEvent()

data class Vote(val choice: String, val osOfParticipant: String)

data class SetMode(override val identifier: ModeIdentifier, val mode: String, val data: Any?) : ModeCommand()

data class ModeSet(override val identifier: ModeIdentifier, override val happenedDate: Long, val mode: String, val data: Any?) : ModeEvent()

