package fr.xebia.xebicon.xebikart.api.application.cqrs

import fr.xebia.xebicon.xebikart.api.application.bus.EventEmitter
import fr.xebia.xebicon.xebikart.api.infra.GsonProvider

class SurveyEventStoreListenerToSSEEmitter(
        private val eventEmitter: EventEmitter,
        private val eventStore: EventStore
) : EventStoreListener {

    override fun <E : Event> eventsAppenned(events: List<E>) {
        val provideGson = GsonProvider.provideGson()
        events.filterIsInstance<SurveyEvent>()
                .forEach {
                    when(it) {
                        is SurveyVoteReceived -> {
                            eventEmitter.send(SurveyVoteReceived::class.java.simpleName, provideGson.toJson(it))
                        }
                        is SurveyClosed -> sendCompletedSurvey(it.identifier)
                    }
                }

    }

    private fun sendCompletedSurvey(surveyIdentifier: SurveyIdentifier) {
        val survey = Survey()
        val events = eventStore.getEvents<SurveyEvent>(surveyIdentifier)
        val surveyState = survey.replay(events)
        if (surveyState is SurveyCompleted) {
            val provideGson = GsonProvider.provideGson()
            eventEmitter.send(SurveyCompleted::class.java.simpleName, provideGson.toJson(surveyState))
        }
    }

}