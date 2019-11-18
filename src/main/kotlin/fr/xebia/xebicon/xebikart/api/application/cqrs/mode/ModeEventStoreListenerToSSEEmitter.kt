package fr.xebia.xebicon.xebikart.api.application.cqrs.mode

import com.google.gson.JsonParseException
import fr.xebia.xebicon.xebikart.api.application.bus.EventEmitter
import fr.xebia.xebicon.xebikart.api.application.cqrs.Event
import fr.xebia.xebicon.xebikart.api.application.cqrs.EventStoreListener
import fr.xebia.xebicon.xebikart.api.application.cqrs.ModeEvent
import fr.xebia.xebicon.xebikart.api.application.cqrs.ModeSet
import fr.xebia.xebicon.xebikart.api.infra.GsonProvider
import org.jetbrains.annotations.NotNull
import org.slf4j.LoggerFactory

class ModeEventStoreListenerToSSEEmitter(
        private val eventEmitter: @NotNull EventEmitter) : EventStoreListener {
    private val logger = LoggerFactory.getLogger(ModeEventStoreListenerToSSEEmitter::class.java)

    override fun <E : Event> eventsAppenned(events: List<E>) {
        val provideGson = GsonProvider.provideGson()
        events.filterIsInstance<ModeEvent>()
                .forEach {
                    try {
                        eventEmitter.send(ModeSet::class.java.simpleName, provideGson.toJson(it))
                    } catch (e: JsonParseException) {
                        logger.warn("Cannot send event ${it.identifier()}")
                    }
                }

    }
}
