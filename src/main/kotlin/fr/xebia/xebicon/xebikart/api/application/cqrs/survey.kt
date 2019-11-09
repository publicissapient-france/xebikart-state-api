package fr.xebia.xebicon.xebikart.api.application.cqrs

import java.time.Clock

class Survey(private val clock: Clock = Clock.systemDefaultZone()) : Aggregate<SurveyState, SurveyCommand, SurveyEvent> {

    override fun decide(state: SurveyState, command: SurveyCommand): AggregateDecideResult {
        val decided = when (state) {
            is SurveyNotExist -> decideOnNotExistingSurvey(command)
            is SurveyEditing -> decideInSurveyEditing(state, command)
            is SurveyWaitingVote -> decideOnSurveyWaitingVote(state, command)
            else -> Either.left("Not implemented for state " + state::class.java.simpleName)
        }
        return decided
                .fold(
                        { reason -> FailedDecideResult(reason) },
                        { events -> SuccessfulDecideResult(events) }
                )
    }

    override fun apply(state: SurveyState, event: SurveyEvent): SurveyState = when (state) {
        SurveyNotExist -> applyOnNotExistingSurvey(event)
        is SurveyEditing -> applyOnSurveyEditing(state, event)
        is SurveyWaitingVote -> applyOnSurveyWaitingVote(state, event)
        is SurveyCompleted -> state
    }

    override fun notExistState(): SurveyState = SurveyNotExist

    private fun now(): Long = clock.millis()

    private fun decideOnNotExistingSurvey(command: SurveyCommand): Either<String, List<SurveyEvent>> = when (command) {
        is CreateSurvey -> Either.right(
                listOf(
                        SurveyCreated(command.identifier, now(), command.name)
                )
        )
        else -> Either.left("Not yet implemented command " + command::class.java.simpleName + " on not existing survey.")
    }

    private fun decideInSurveyEditing(state: SurveyEditing, command: SurveyCommand): Either<String, List<SurveyEvent>> = when (command) {
        is CreateSurvey -> Either.left("Not able to create an already existing survey")
        is AddChoiceToSurvey -> {
            if (!state.choices.contains(command.choice)) {
                Either.right(
                        listOf(
                                SurveyChoiceAdded(state.identifier, now(), command.choice)
                        )
                )
            } else Either.right(listOf())
        }

        is StartSurvey -> if (state.choices.isNotEmpty()) {
            Either.right(
                    listOf(
                            SurveyStarted(state.identifier, now())
                    )
            )
        } else Either.right(listOf())

        is AddVoteToChoiceInSurvey -> Either.left("Survey is editing, not able to receive vote.")
        is CloseSurvey -> Either.right(
                listOf(
                        SurveyClosed(state.identifier, now())
                )
        )
    }

    private fun decideOnSurveyWaitingVote(state: SurveyWaitingVote, command: SurveyCommand): Either<String, List<SurveyEvent>> = when (command) {
        is CreateSurvey -> Either.left("Survey already created.")
        is AddChoiceToSurvey -> Either.left("Survey already started.")
        is StartSurvey -> Either.left("Survey already started.")
        is AddVoteToChoiceInSurvey -> Either.right(
                listOf(
                        SurveyVoteReceived(state.identifier, now(), command.vote)
                )
        )
        is CloseSurvey -> Either.right(
                listOf(
                        SurveyClosed(state.identifier, now())
                )
        )
    }

    private fun applyOnNotExistingSurvey(event: SurveyEvent): SurveyState = when (event) {
        is SurveyCreated -> SurveyEditing(event.identifier, event.name, listOf())
        else -> SurveyNotExist
    }

    private fun applyOnSurveyEditing(state: SurveyEditing, event: SurveyEvent): SurveyState = when (event) {
        is SurveyChoiceAdded -> {
            val choices = state.choices.toMutableList()
            choices.add(event.choice)
            SurveyEditing(
                    state.identifier,
                    state.name,
                    choices.toList()
            )
        }
        is SurveyStarted -> {
            val votes = mutableMapOf<String, Int>()
            state.choices.forEach { votes[it] = 0 }
            SurveyWaitingVote(state.identifier, state.name, votes.toMap())
        }
        else -> state
    }

    private fun applyOnSurveyWaitingVote(state: SurveyWaitingVote, event: SurveyEvent): SurveyState = when(event) {
        is SurveyVoteReceived -> {
            val votes = state.votes.toMutableMap()
            val currentVoteCount = votes[event.vote.choice] ?: 0
            votes[event.vote.choice] = currentVoteCount.inc()
            SurveyWaitingVote(
                    state.identifier,
                    state.name,
                    votes.toMap()
            )
        }
        is SurveyClosed -> {
            SurveyCompleted(
                    state.identifier,
                    state.name,
                    state.votes
            )
        }
        else -> state
    }

}
