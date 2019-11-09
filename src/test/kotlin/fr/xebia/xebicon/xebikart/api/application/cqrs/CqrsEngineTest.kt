package fr.xebia.xebicon.xebikart.api.application.cqrs

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class CqrsEngineTest {

    private val eventStore = TestEventStore()
    private val cqrsEngine = CqrsEngine(eventStore, Boat())

    @Test
    fun `successfully handle a command`() {

        val command = DitchToHarbor(
                titanic,
                havre
        )

        val actual = cqrsEngine.handleCommand(command)

        assertThat(actual).isInstanceOf(SuccessfulCommandHandled::class.java)
    }

    @Test
    fun `fail to handle a command`() {

        val command = LeaveHarbor(
                titanic,
                havre,
                newYork
        )

        var actual: CommandHandleResult = cqrsEngine.handleCommand(command)

        actual = cqrsEngine.handleCommand(command)

        assertThat(actual).isInstanceOf(FailedCommandHandled::class.java)
        if (actual is FailedCommandHandled) {
            println(actual.reason)
        }
    }
}