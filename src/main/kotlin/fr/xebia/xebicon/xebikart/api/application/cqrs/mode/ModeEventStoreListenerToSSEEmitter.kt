package fr.xebia.xebicon.xebikart.api.application.cqrs.mode

import fr.xebia.xebicon.xebikart.api.application.bus.EventEmitter
import fr.xebia.xebicon.xebikart.api.application.cqrs.*
import fr.xebia.xebicon.xebikart.api.infra.GsonProvider

class ModeEventStoreListenerToSSEEmitter(
        private val eventEmitter: EventEmitter,
        private val eventStore: EventStore
) : EventStoreListener {

    override fun <E : Event> eventsAppenned(events: List<E>) {
        val provideGson = GsonProvider.provideGson()
        events.forEach {
            eventEmitter.send(ModeSet::class.java.simpleName, provideGson.toJson(it))
        }

    }
}
