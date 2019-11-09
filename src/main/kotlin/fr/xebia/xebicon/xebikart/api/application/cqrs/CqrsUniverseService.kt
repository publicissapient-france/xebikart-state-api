package fr.xebia.xebicon.xebikart.api.application.cqrs

import fr.xebia.xebicon.xebikart.api.application.UniverseService
import fr.xebia.xebicon.xebikart.api.application.model.OsMobile
import fr.xebia.xebicon.xebikart.api.application.model.SurveyResult
import fr.xebia.xebicon.xebikart.api.application.model.Universe
import org.apache.commons.lang3.RandomStringUtils
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.write

class CqrsUniverseService(
        private val cqrsEngine: CqrsEngine<SurveyIdentifier, SurveyState, SurveyCommand, SurveyEvent>,
        private val eventStore: EventStore
) : UniverseService {

    private var currentSurveyIdentifier: SurveyIdentifier = SurveyNotExist.identifier

    private val lock = ReentrantReadWriteLock()

    override fun createSurvey(): SurveyIdentifier = lock.write {
        if (currentSurveyIdentifier != SurveyNotExist.identifier) {
            close()
        }
        return submitANewSurvey()
    }

    private fun submitANewSurvey(): SurveyIdentifier {
        val generatedIdentifier = SurveyIdentifier(RandomStringUtils.randomAlphanumeric(16))
        val command = CreateSurvey(generatedIdentifier, "Universe selection")
        return when (cqrsEngine.handleCommand(command)) {
            is SuccessfulCommandHandled -> {
                currentSurveyIdentifier = generatedIdentifier
                addChoiceAndStartSurvey(currentSurveyIdentifier)
                currentSurveyIdentifier
            }
            is FailedCommandHandled -> SurveyNotExist.identifier
        }
    }

    private fun addChoiceAndStartSurvey(surveyIdentifier: SurveyIdentifier) {
        var command: SurveyCommand = AddChoiceToSurvey(surveyIdentifier, "1")
        cqrsEngine.handleCommand(command)
        command = AddChoiceToSurvey(surveyIdentifier, "2")
        cqrsEngine.handleCommand(command)
        command = StartSurvey(surveyIdentifier)
        cqrsEngine.handleCommand(command)
    }

    override fun addVote(universe: Universe?, fromDeviceOs: OsMobile?) {
        if (
                currentSurveyIdentifier == SurveyNotExist.identifier ||
                universe == null ||
                fromDeviceOs == null
        ) return
        val command = AddVoteToChoiceInSurvey(
                currentSurveyIdentifier,
                Vote(universe.name, fromDeviceOs.name.toLowerCase())
        )
        lock.write {
            cqrsEngine.handleCommand(command)
        }
    }

    override fun close(): Either<String, SurveyResult> = lock.write {
        val command = CloseSurvey(currentSurveyIdentifier)
        when (val commandHandleResult = cqrsEngine.handleCommand(command)) {
            is SuccessfulCommandHandled -> {
                val survey = Survey()
                val events = eventStore.getEvents<SurveyEvent>(currentSurveyIdentifier)
                val surveyState = survey.replay(events)
                if (surveyState is SurveyCompleted) {
                    val res = Either.right(
                            SurveyResult(
                                    currentSurveyIdentifier,
                                    Universe(surveyState.name),
                                    surveyState.votes
                            )
                    )
                    currentSurveyIdentifier = SurveyNotExist.identifier
                    res
                } else {
                    Either.left("Unable to get a completed survey, obtain an ${surveyState::class.java.simpleName}.")
                }
            }
            is FailedCommandHandled -> Either.left(commandHandleResult.reason)
        }
    }
}