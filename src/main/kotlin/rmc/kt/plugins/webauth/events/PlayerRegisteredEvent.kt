package rmc.kt.plugins.webauth.events

import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * Разработано командой RMC, 2021
 */
@Deprecated("Only for newbie intro implementation!")
class PlayerRegisteredEvent(val username: String, val ip: String): Event() {

    companion object {

        val hlist = HandlerList()

        @JvmStatic
        fun getHandlerList() = hlist

    }

    override fun getHandlers() = hlist

}
