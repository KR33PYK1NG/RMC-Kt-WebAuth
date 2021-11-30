package rmc.kt.plugins.webauth.commands

import com.earth2me.essentials.utils.LocationUtil
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import rmc.kt.plugins.core.CorePlugin
import rmc.kt.plugins.core.base.CommandBase
import rmc.kt.plugins.core.helpers.ChatHelper
import rmc.kt.plugins.core.helpers.DbHelper
import rmc.kt.plugins.core.helpers.LogHelper
import rmc.kt.plugins.core.helpers.TaskHelper
import rmc.kt.plugins.webauth.WebAuthPlugin
import rmc.kt.plugins.webauth.events.PlayerLoggedInEvent
import rmc.kt.plugins.webauth.helpers.AuthHelper
import rmc.kt.plugins.webauth.helpers.WebHelper

/**
 * Разработано командой RMC, 2021
 */
class LoginCommand: CommandBase() {

    override fun onCommand(sender: CommandSender, label: String, args: Array<String>) {
        WebAuthPlugin.handleCommandExecution(sender, args.size, label, "пароль", true, {
            WebHelper.loginGetRequestBlocking(sender.name, args[0], (sender as Player).address!!.hostString)
        }, {
            TaskHelper.asyncNow {
                DbHelper.executeQueryBlocking({
                    TaskHelper.syncNow {
                        if (it.isNotEmpty()) {
                            val row = it.get(0)
                            var lastpos = Location(Bukkit.getWorld(row.get("world") as String),
                                row.get("x") as Double,
                                row.get("y") as Double,
                                row.get("z") as Double,
                                row.get("yaw") as Float,
                                row.get("pitch") as Float)
                            try {
                                if (LocationUtil.isBlockUnsafe(lastpos.world, lastpos.blockX, lastpos.blockY, lastpos.blockZ)) {
                                    lastpos = LocationUtil.getSafeDestination(lastpos)
                                }
                            } catch (ignore: Throwable) {}
                            (sender as Player).teleport(lastpos)
                        }
                    }
                }, "SELECT * FROM `${CorePlugin.serverName}`.`webauth_last_pos` WHERE `username` = ?;", sender.name)
            }
            AuthHelper.unregisterTaskIfActive(sender.name)
            AuthHelper.authorizeForMinute(sender.name, (sender as Player).address!!.hostString)
            Bukkit.getPluginManager().callEvent(PlayerLoggedInEvent(sender.name, sender.address!!.hostString))
            sender.sendMessage(ChatHelper.format("&aВы вошли в аккаунт, приятной игры!"))
            LogHelper.debug("${sender.name} (ip: ${sender.address!!.hostString}) logged in through Web API")
            TaskHelper.asyncNow {
                val result = WebHelper.hasEmailGetRequestBlocking(sender.name)
                TaskHelper.syncNow {
                    if (!result.success) {
                        sender.sendMessage(ChatHelper.format("&7У вас не указана почта. Пароль не получится восстановить при потере.\n" +
                                                             "Установите почту с помощью &e/email почта"))
                    }
                }
            }
        })
    }

}
