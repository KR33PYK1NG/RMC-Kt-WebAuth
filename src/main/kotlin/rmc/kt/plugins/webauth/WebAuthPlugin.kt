package rmc.kt.plugins.webauth

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import rmc.kt.plugins.core.CorePlugin
import rmc.kt.plugins.core.base.CommandBase
import rmc.kt.plugins.core.base.ListenerBase
import rmc.kt.plugins.core.helpers.ChatHelper
import rmc.kt.plugins.core.helpers.DbHelper
import rmc.kt.plugins.core.helpers.TaskHelper
import rmc.kt.plugins.webauth.commands.EmailCommand
import rmc.kt.plugins.webauth.commands.LoginCommand
import rmc.kt.plugins.webauth.commands.RegisterCommand
import rmc.kt.plugins.webauth.helpers.AuthHelper
import rmc.kt.plugins.webauth.helpers.WebHelper
import rmc.kt.plugins.webauth.listeners.AsyncPlayerPreLoginListener
import rmc.kt.plugins.webauth.listeners.PlayerJoinListener
import rmc.kt.plugins.webauth.listeners.PlayerQuitListener
import java.lang.reflect.Field
import java.util.concurrent.Callable

/**
 * Разработано командой RMC, 2021
 */
class WebAuthPlugin: JavaPlugin() {

    companion object {

        @JvmStatic var loginTimeout = 0; private set
        @JvmStatic var webApiKey = ""; private set
        @JvmStatic var webBaseUrl = ""; private set
        @JvmStatic var webHttpTimeout = 0; private set
        @JvmStatic var locationWorld = ""; private set
        @JvmStatic var locationX = 0.0; private set
        @JvmStatic var locationY = 0.0; private set
        @JvmStatic var locationZ = 0.0; private set
        @JvmStatic var locationYaw = 0.0f; private set
        @JvmStatic var locationPitch = 0.0f; private set

        private var gravitFieldCache: Field? = null

        internal fun handleCommandExecution(sender: CommandSender, argSize: Int, label: String, usage: String, errorIfAuthIs: Boolean, request: Callable<WebHelper.Companion.GameGetResponse>, onSuccess: Runnable) {
            if (sender !is Player) {
                sender.sendMessage(ChatHelper.format("&cЭта команда доступна только игрокам."))
                return
            }
            if (argSize != 1) {
                sender.sendMessage(ChatHelper.format("&cИспользование: /$label $usage"))
                return
            }
            if (errorIfAuthIs == AuthHelper.isAuthorized(sender.name, sender.address!!.hostString)) {
                sender.sendMessage(ChatHelper.format(if (errorIfAuthIs) "&cВы уже авторизованы!" else "&cВы еще не авторизованы!"))
                return
            }
            TaskHelper.asyncNow {
                val result = request.call()
                TaskHelper.syncNow {
                    if (result.success) {
                        onSuccess.run()
                    } else sender.sendMessage(ChatHelper.format("&cОшибка: &e${result.message}"))
                }
            }
        }

        internal fun tryFetchGravitAuths(): MutableMap<String, String> {
            return try {
                if (gravitFieldCache == null)
                    gravitFieldCache = Bukkit::class.java.getDeclaredField("rmc\$gravitAuths").apply { trySetAccessible() }

                @Suppress("UNCHECKED_CAST")
                gravitFieldCache!!.get(null) as MutableMap<String, String>
            } catch (ignore: Throwable) {
                mutableMapOf()
            }
        }

        internal fun savePlayerLastPos(player: Player) {
            player.run {
                DbHelper.executeUpdate("REPLACE INTO `${CorePlugin.serverName}`.`webauth_last_pos`(`username`, `world`, `x`, `y`, `z`, `yaw`, `pitch`) " +
                                       "VALUES(?, ?, ?, ?, ?, ?, ?);", name, world.name, location.x, location.y, location.z, location.yaw, location.pitch)
            }
        }

    }

    override fun onEnable() {
        saveDefaultConfig()
        loginTimeout = config.get("login_timeout")!! as Int
        webApiKey = config.get("web.api_key")!! as String
        webBaseUrl = config.get("web.base_url")!! as String
        webHttpTimeout = config.get("web.http_timeout")!! as Int
        locationWorld = config.get("location.world")!! as String
        locationX = config.get("location.x")!! as Double
        locationY = config.get("location.y")!! as Double
        locationZ = config.get("location.z")!! as Double
        locationYaw = (config.get("location.yaw")!! as Double).toFloat()
        locationPitch = (config.get("location.pitch")!! as Double).toFloat()
        DbHelper.executeUpdate("CREATE TABLE IF NOT EXISTS `${CorePlugin.serverName}`.`webauth_last_pos`(`username` VARCHAR(100) PRIMARY KEY, `world` VARCHAR(100), `x` DOUBLE, `y` DOUBLE, `z` DOUBLE, `yaw` FLOAT, `pitch` FLOAT);")
        CommandBase.register(EmailCommand::class.java, "email")
        CommandBase.register(LoginCommand::class.java, "login")
        CommandBase.register(RegisterCommand::class.java, "register")
        ListenerBase.register(AsyncPlayerPreLoginListener::class.java)
        ListenerBase.register(PlayerJoinListener::class.java)
        ListenerBase.register(PlayerQuitListener::class.java)
        AuthHelper.startExtensionTask()
        val types = mutableListOf<PacketType>()
        for (type in PacketType.values()) {
            if (type.sender == PacketType.Sender.CLIENT
                && type.protocol == PacketType.Protocol.PLAY) {
                types.add(type)
            }
        }
        ProtocolLibrary.getProtocolManager().addPacketListener(object: PacketAdapter(this, types) {
            override fun onPacketReceiving(event: PacketEvent?) {
                event?.run {
                    if (!AuthHelper.isAuthorized(player.name, player.address!!.hostString)) {
                        if (packetType == PacketType.Play.Client.CHAT) {
                            val msg = (packet.strings.readSafely(0) ?: "").lowercase()
                            if (!msg.startsWith("/login")
                                && !msg.startsWith("/register")) {
                                isCancelled = true
                            }
                        } else isCancelled = true
                    }
                }
            }
        })
    }

    override fun onDisable() {
        for (player in Bukkit.getOnlinePlayers()) {
            if (AuthHelper.isAuthorized(player.name, player.address!!.hostString)) {
                savePlayerLastPos(player)
            }
        }
    }

}
