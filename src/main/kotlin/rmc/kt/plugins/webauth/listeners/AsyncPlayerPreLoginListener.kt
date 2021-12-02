package rmc.kt.plugins.webauth.listeners

import org.bukkit.Bukkit
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import rmc.kt.plugins.core.base.ListenerBase
import rmc.kt.plugins.core.helpers.ChatHelper
import rmc.kt.plugins.webauth.helpers.AuthHelper
import rmc.kt.plugins.webauth.helpers.WebHelper

/**
 * Разработано командой RMC, 2021
 */
class AsyncPlayerPreLoginListener: ListenerBase<AsyncPlayerPreLoginEvent>() {

    override fun onEvent(event: AsyncPlayerPreLoginEvent) {
        val player = Bukkit.getPlayerExact(event.name)
        if (player != null && AuthHelper.isAuthorized(player.name, player.address!!.address.hostAddress)) {
            event.kickMessage = ChatHelper.format("&cПод ником &e${player.name} &cуже кто-то авторизован и играет!")
            event.loginResult = AsyncPlayerPreLoginEvent.Result.KICK_OTHER
        } else {
            val result = WebHelper.hasAccountGetRequestBlocking(event.name)
            if (result.success && result.message != event.name) {
                event.kickMessage = ChatHelper.format("&cНеправильный регистр ника! Перезайдите под &e${result.message}")
                event.loginResult = AsyncPlayerPreLoginEvent.Result.KICK_OTHER
            }
        }
    }

}
