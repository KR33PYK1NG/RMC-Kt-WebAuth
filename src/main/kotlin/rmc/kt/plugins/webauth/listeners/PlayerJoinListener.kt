package rmc.kt.plugins.webauth.listeners

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.event.player.PlayerJoinEvent
import rmc.kt.plugins.core.base.ListenerBase
import rmc.kt.plugins.core.helpers.ChatHelper
import rmc.kt.plugins.core.helpers.TaskHelper
import rmc.kt.plugins.webauth.WebAuthPlugin
import rmc.kt.plugins.webauth.helpers.AuthHelper
import rmc.kt.plugins.webauth.helpers.WebHelper

/**
 * Разработано командой RMC, 2021
 */
class PlayerJoinListener: ListenerBase<PlayerJoinEvent>() {

    override fun onEvent(event: PlayerJoinEvent) {
        event.player.run {
            if (!AuthHelper.isAuthorized(name, address!!.hostString)) {
                TaskHelper.asyncNow {
                    val result = WebHelper.hasAccountGetRequestBlocking(name)
                    TaskHelper.syncNow {
                        var count = 0
                        AuthHelper.unregisterTaskIfActive(name)
                        AuthHelper.registerTask(name, TaskHelper.syncTimer(0, 20) {
                            count++
                            if (count % WebAuthPlugin.loginTimeout == 0) {
                                kickPlayer(ChatHelper.format("&cПрошло слишком много времени для авторизации!"))
                            }
                            if (count == 1 || count % 5 == 0) {
                                sendMessage(ChatHelper.format(if (result.success) "&cВойдите в аккаунт с помощью &e/login пароль" else "&cЗарегистрируйтесь с помощью &e/register пароль"))
                            }
                            teleport(Location(Bukkit.getWorld(WebAuthPlugin.locationWorld), WebAuthPlugin.locationX, WebAuthPlugin.locationY, WebAuthPlugin.locationZ, WebAuthPlugin.locationYaw, WebAuthPlugin.locationPitch))
                        })
                    }
                }
            }
        }
    }

}
