package rmc.kt.plugins.webauth.commands

import org.bukkit.command.CommandSender
import rmc.kt.plugins.core.base.CommandBase
import rmc.kt.plugins.core.helpers.ChatHelper
import rmc.kt.plugins.core.helpers.LogHelper
import rmc.kt.plugins.webauth.WebAuthPlugin
import rmc.kt.plugins.webauth.helpers.WebHelper

/**
 * Разработано командой RMC, 2021
 */
class EmailCommand: CommandBase() {

    override fun onCommand(sender: CommandSender, label: String, args: Array<String>) {
        WebAuthPlugin.handleCommandExecution(sender, args.size, label, "почта", false, {
            WebHelper.emailGetRequestBlocking(sender.name, args[0])
        }, {
            sender.sendMessage(ChatHelper.format("&aВы установили новую почту! Теперь можно восстанавливать пароль через сайт."))
            LogHelper.debug("${sender.name} set email through Web API")
        })
    }

}
