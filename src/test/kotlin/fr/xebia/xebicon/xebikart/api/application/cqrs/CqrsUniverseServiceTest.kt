package fr.xebia.xebicon.xebikart.api.application.cqrs

import assertk.assertions.*
import fr.xebia.xebicon.xebikart.api.application.model.OsMobile
import fr.xebia.xebicon.xebikart.api.application.model.Universe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class CqrsUniverseServiceTest {

    private lateinit var eventStore: TestEventStore
    private lateinit var cqrsEngine: CqrsEngine<SurveyIdentifier, SurveyState, SurveyCommand, SurveyEvent>
    private lateinit var cqrsUniverseService: CqrsUniverseService

    @BeforeEach
    fun setup() {
        eventStore = TestEventStore()
        cqrsEngine = CqrsEngine(eventStore, Survey())
        cqrsUniverseService = CqrsUniverseService(cqrsEngine, eventStore)
    }

    @Test
    fun `Create a survey in an empty event store should success`() {
        val surveyIdentifier = cqrsUniverseService.createSurvey()

        assertk.assertThat(surveyIdentifier).isNotNull()
        assertk.assertThat(surveyIdentifier).isNotEqualTo(SurveyNotExist.identifier)
        val currentState = currentState()
        assertk.assertThat(currentState).isInstanceOf(SurveyWaitingVote::class)
        val surveyWaitingVote = currentState as SurveyWaitingVote

        for (universeId in 1..2) {
            assertk.assertThat(surveyWaitingVote.votes.keys).contains(universeId.toString())
        }
    }

    @Test
    fun `Add one vote to universe one should success`() {
        cqrsUniverseService.createSurvey()

        cqrsUniverseService.addVote(Universe("1"), OsMobile.ANDROID)

        val currentState = currentState()
        assertk.assertThat(currentState).isInstanceOf(SurveyWaitingVote::class)
        val surveyWaitingVote = currentState as SurveyWaitingVote

        assertk.assertThat(surveyWaitingVote.votes["1"]).isEqualTo(1)
    }

    @Test
    fun `Add two votes to universe one with different os mobile should success`() {
        cqrsUniverseService.createSurvey()

        cqrsUniverseService.addVote(Universe("1"), OsMobile.ANDROID)
        cqrsUniverseService.addVote(Universe("1"), OsMobile.IOS)

        val currentState = currentState()
        assertk.assertThat(currentState).isInstanceOf(SurveyWaitingVote::class)
        val surveyWaitingVote = currentState as SurveyWaitingVote

        assertk.assertThat(surveyWaitingVote.votes["1"]).isEqualTo(2)
    }

    @Test
    fun `Add votes to universe then close survey should success`() {
        val surveyIdentifier = cqrsUniverseService.createSurvey()

        cqrsUniverseService.addVote(Universe("1"), OsMobile.ANDROID)
        cqrsUniverseService.addVote(Universe("1"), OsMobile.IOS)
        cqrsUniverseService.addVote(Universe("2"), OsMobile.IOS)

        val actual = cqrsUniverseService.close()

        assertk.assertThat(actual).isInstanceOf(Either.Right::class)
        val right = actual as Either.Right
        val surveyResult = right.right
        assertk.assertThat(surveyResult.surveyIdentifier).isEqualTo(surveyIdentifier)

        val currentState = currentState()
        assertk.assertThat(currentState).isInstanceOf(SurveyCompleted::class)
        val surveyCompleted = currentState as SurveyCompleted

        assertk.assertThat(surveyCompleted.votes["1"]).isEqualTo(2)
    }

    private fun currentState(): SurveyState {
        val events = eventStore.getAllEvents<SurveyEvent>()
        val survey = Survey()
        return survey.replay(events)
    }

}