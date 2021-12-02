package rmc.kt.plugins.webauth.commands

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import rmc.kt.plugins.core.base.CommandBase
import rmc.kt.plugins.core.helpers.ChatHelper
import rmc.kt.plugins.core.helpers.LogHelper
import rmc.kt.plugins.webauth.WebAuthPlugin
import rmc.kt.plugins.webauth.events.PlayerRegisteredEvent
import rmc.kt.plugins.webauth.helpers.AuthHelper
import rmc.kt.plugins.webauth.helpers.WebHelper

/**
 * Разработано командой RMC, 2021
 */
class RegisterCommand: CommandBase() {

    override fun onCommand(sender: CommandSender, label: String, args: Array<String>) {
        WebAuthPlugin.handleCommandExecution(sender, args.size, label, "пароль", true, {
            WebHelper.registerGetRequestBlocking(sender.name, args[0])
        }, {
            AuthHelper.unregisterTaskIfActive(sender.name)
            AuthHelper.authorizeForMinute(sender.name, (sender as Player).address!!.address.hostAddress)
            Bukkit.getPluginManager().callEvent(PlayerRegisteredEvent(sender.name, sender.address!!.address.hostAddress))
            sender.sendMessage(ChatHelper.format("&aВы зарегистрировались, приятной игры!"))
            LogHelper.debug("${sender.name} (ip: ${sender.address!!.address.hostAddress}) registered through Web API")
            sender.sendMessage(ChatHelper.format("&7У вас не указана почта. Пароль не получится восстановить при потере.\n" +
                                                 "Установите почту с помощью &e/email почта"))
        })
    }

}
