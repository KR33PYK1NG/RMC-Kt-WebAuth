package rmc.kt.plugins.webauth.listeners

import org.bukkit.event.player.PlayerQuitEvent
import rmc.kt.plugins.core.base.ListenerBase
import rmc.kt.plugins.webauth.WebAuthPlugin
import rmc.kt.plugins.webauth.helpers.AuthHelper

/**
 * Разработано командой RMC, 2021
 */
class PlayerQuitListener: ListenerBase<PlayerQuitEvent>() {

    override fun onEvent(event: PlayerQuitEvent) {
        event.player.run {
            if (AuthHelper.isAuthorized(name, address!!.hostString)) {
                WebAuthPlugin.savePlayerLastPos(this)
            } else {
                AuthHelper.unregisterTaskIfActive(name)
            }
        }
    }

}
