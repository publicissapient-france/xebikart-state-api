package fr.xebia.xebicon.xebikart.api.application.cqrs

import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class SurveyTest {

    private lateinit var eventStore: EventStore
    private lateinit var cqrsEngine: CqrsEngine<SurveyIdentifier, SurveyState, SurveyCommand, SurveyEvent>

    @BeforeEach
    fun setup() {
        eventStore = TestEventStore()
        cqrsEngine = CqrsEngine(eventStore, Survey())
    }

    @Test
    fun `Create Survey on not existing survey should success`() {


        val command = CreateSurvey(bestBoatSurveyIdentifier, bestBoatSurveyName)

        val commandHandleResult = cqrsEngine.handleCommand(command)

        assertk.assertThat(commandHandleResult).isInstanceOf(SuccessfulCommandHandled::class)
        val successResult = commandHandleResult as SuccessfulCommandHandled

        assertk.assertThat(successResult.events).hasSize(1)

        val event = successResult.events[0]
        assertk.assertThat(event).isInstanceOf(SurveyCreated::class)
        val surveyCreated = event as SurveyCreated

        assertk.assertThat(surveyCreated.name).isEqualTo(bestBoatSurveyName)

    }

    @Test
    fun `Adding a choice to editing survey should success`() {

        eventStore.appendEvents(bestBoatSurveyIdentifier, listOf(
                SurveyCreated(bestBoatSurveyIdentifier, System.currentTimeMillis(), bestBoatSurveyName)
        ))

        val command = AddChoiceToSurvey(bestBoatSurveyIdentifier, "Beneteau")

        val commandHandleResult = cqrsEngine.handleCommand(command)

        assertk.assertThat(commandHandleResult).isInstanceOf(SuccessfulCommandHandled::class)
        val successResult = commandHandleResult as SuccessfulCommandHandled

        assertk.assertThat(successResult.events).hasSize(1)

        val event = successResult.events[0]
        assertk.assertThat(event).isInstanceOf(SurveyChoiceAdded::class)

        val choiceAdded = event as SurveyChoiceAdded
        assertk.assertThat(choiceAdded.choice).isEqualTo("Beneteau")

    }

    @Test
    fun `Adding a choice to editing survey which already contain choice should not emitted Event`() {

        eventStore.appendEvents(bestBoatSurveyIdentifier, listOf(
                SurveyCreated(bestBoatSurveyIdentifier, System.currentTimeMillis(), bestBoatSurveyName),
                SurveyChoiceAdded(bestBoatSurveyIdentifier, System.currentTimeMillis(), "Beneteau")
        ))

        val command = AddChoiceToSurvey(bestBoatSurveyIdentifier, "Beneteau")

        val commandHandleResult = cqrsEngine.handleCommand(command)

        assertk.assertThat(commandHandleResult).isInstanceOf(SuccessfulCommandHandled::class)
        val successResult = commandHandleResult as SuccessfulCommandHandled

        assertk.assertThat(successResult.events).hasSize(0)

    }


    @Test
    fun `Close an editing survey should success`() {

        eventStore.appendEvents(bestBoatSurveyIdentifier, listOf(
                SurveyCreated(bestBoatSurveyIdentifier, System.currentTimeMillis(), bestBoatSurveyName),
                SurveyChoiceAdded(bestBoatSurveyIdentifier, System.currentTimeMillis(), "Beneteau")
        ))

        val command = CloseSurvey(bestBoatSurveyIdentifier)

        val commandHandleResult = cqrsEngine.handleCommand(command)

        assertk.assertThat(commandHandleResult).isInstanceOf(SuccessfulCommandHandled::class)
        val successResult = commandHandleResult as SuccessfulCommandHandled

        assertk.assertThat(successResult.events).hasSize(1)

        val event = successResult.events[0]

        assertk.assertThat(event).isInstanceOf(SurveyClosed::class)
    }

    @Test
    fun `Start a survey in editing should allow to add a vote`() {

        eventStore.appendEvents(bestBoatSurveyIdentifier, listOf(
                SurveyCreated(bestBoatSurveyIdentifier, System.currentTimeMillis(), bestBoatSurveyName),
                SurveyChoiceAdded(bestBoatSurveyIdentifier, System.currentTimeMillis(), "Beneteau")
        ))

        var command: SurveyCommand = StartSurvey(bestBoatSurveyIdentifier)

        var commandHandleResult = cqrsEngine.handleCommand(command)

        assertk.assertThat(commandHandleResult).isInstanceOf(SuccessfulCommandHandled::class)
        var successResult = commandHandleResult as SuccessfulCommandHandled

        assertk.assertThat(successResult.events).hasSize(1)
        var event = successResult.events[0]

        assertk.assertThat(event).isInstanceOf(SurveyStarted::class)

        command = AddVoteToChoiceInSurvey(bestBoatSurveyIdentifier, Vote("Beneteau", "android"))

        commandHandleResult = cqrsEngine.handleCommand(command)

        assertk.assertThat(commandHandleResult).isInstanceOf(SuccessfulCommandHandled::class)
        successResult = commandHandleResult as SuccessfulCommandHandled

        assertk.assertThat(successResult.events).hasSize(1)
        event = successResult.events[0]

        assertk.assertThat(event).isInstanceOf(SurveyVoteReceived::class)
        val surveyVoteReceived = event as SurveyVoteReceived
        assertk.assertThat(surveyVoteReceived.vote.choice).isEqualTo("Beneteau")

    }

    @Test
    fun `Start a survey in editing without choice should not emitted event`() {

        eventStore.appendEvents(bestBoatSurveyIdentifier, listOf(
                SurveyCreated(bestBoatSurveyIdentifier, System.currentTimeMillis(), bestBoatSurveyName)
        ))

        val command = StartSurvey(bestBoatSurveyIdentifier)

        val commandHandleResult = cqrsEngine.handleCommand(command)

        assertk.assertThat(commandHandleResult).isInstanceOf(SuccessfulCommandHandled::class)
        val successResult = commandHandleResult as SuccessfulCommandHandled

        assertk.assertThat(successResult.events).hasSize(0)

    }

    @Test
    fun `Close a started survey should success`() {

        eventStore.appendEvents(bestBoatSurveyIdentifier, listOf(
                SurveyCreated(bestBoatSurveyIdentifier, System.currentTimeMillis(), bestBoatSurveyName),
                SurveyChoiceAdded(bestBoatSurveyIdentifier, System.currentTimeMillis(), "Beneteau"),
                SurveyStarted(bestBoatSurveyIdentifier, System.currentTimeMillis())
        ))

        val command = CloseSurvey(bestBoatSurveyIdentifier)

        val commandHandleResult = cqrsEngine.handleCommand(command)

        assertk.assertThat(commandHandleResult).isInstanceOf(SuccessfulCommandHandled::class)
        val successResult = commandHandleResult as SuccessfulCommandHandled

        assertk.assertThat(successResult.events).hasSize(1)

        val event = successResult.events[0]

        assertk.assertThat(event).isInstanceOf(SurveyClosed::class)
    }

}