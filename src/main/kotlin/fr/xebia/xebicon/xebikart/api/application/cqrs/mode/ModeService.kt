package fr.xebia.xebicon.xebikart.api.application.cqrs.mode

import fr.xebia.xebicon.xebikart.api.application.cqrs.CommandHandleResult

interface ModeService {

    fun setMode(mode: String, data: Any?): CommandHandleResult

}
