package fr.xebia.xebicon.xebikart.api.application.cqrs.mode

import fr.xebia.xebicon.xebikart.api.application.cqrs.*
import org.apache.commons.lang3.RandomStringUtils

class CqrsModeService(private val cqrsModeEngine: CqrsEngine<ModeIdentifier, ModeState, ModeCommand, ModeEvent>) : ModeService {

    override fun setMode(mode: String, data: Any?): CommandHandleResult {
        val id = ModeIdentifier(RandomStringUtils.randomAlphanumeric(42))
        return cqrsModeEngine.handleCommand(SetMode(id, mode, data))
    }

}
