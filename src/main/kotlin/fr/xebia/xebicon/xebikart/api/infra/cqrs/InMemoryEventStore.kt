package fr.xebia.xebicon.xebikart.api.infra.cqrs

import fr.xebia.xebicon.xebikart.api.application.cqrs.Event
import fr.xebia.xebicon.xebikart.api.application.cqrs.EventStore
import fr.xebia.xebicon.xebikart.api.application.cqrs.EventStoreListener
import fr.xebia.xebicon.xebikart.api.application.cqrs.Identifier
import java.lang.Exception

class InMemoryEventStore : EventStore {

    private val cache: MutableMap<Identifier, List<Event>> = mutableMapOf()

    private val listeners: MutableList<EventStoreListener> = mutableListOf()

    override fun appendEvents(id: Identifier, events: List<Event>) {
        val currentEvents = cache[id] ?: mutableListOf()

        val nextEvents = currentEvents.toMutableList()
        nextEvents.addAll(events)
        cache[id] = nextEvents

        listeners.forEach {
            try {
                it.eventsAppenned(events)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun <E : Event> getEvents(id: Identifier): List<E> {
        return cache[id] as List<E>? ?: emptyList()
    }

    fun registerListener(listener: EventStoreListener) {
        listeners.add(listener)
    }
}