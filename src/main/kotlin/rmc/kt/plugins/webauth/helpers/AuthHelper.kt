package rmc.kt.plugins.webauth.helpers

import org.apache.commons.lang.mutable.MutableLong
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitTask
import rmc.kt.plugins.core.helpers.LogHelper
import rmc.kt.plugins.core.helpers.TaskHelper
import rmc.kt.plugins.webauth.WebAuthPlugin

/**
 * Разработано командой RMC, 2021
 */
class AuthHelper {

    companion object {

        private val auths = mutableMapOf<Pair<String, String>, MutableLong>()
        private val tasks = mutableMapOf<String, BukkitTask>()

        @JvmStatic
        fun isAuthorized(username: String, address: String): Boolean {
            return !isExpired(auths.get(Pair(username, address)))
        }

        @JvmStatic
        fun authorizeForMinute(username: String, address: String) {
            extendAuthorization(auths.getOrPut(Pair(username, address)) { MutableLong() })
        }

        @JvmStatic
        fun registerTask(username: String, task: BukkitTask) {
            tasks.put(username, task)
        }

        @JvmStatic
        fun unregisterTaskIfActive(username: String) {
            tasks.remove(username)?.cancel()
        }

        private fun isExpired(long: MutableLong?): Boolean {
            return long == null || System.currentTimeMillis() > long.toLong()
        }

        private fun extendAuthorization(long: MutableLong) {
            long.setValue(System.currentTimeMillis() + 60_000)
        }

        internal fun startExtensionTask() {
            TaskHelper.syncTimer(20) {
                val gravit = WebAuthPlugin.tryFetchGravitAuths()
                if (gravit.isNotEmpty()) {
                    synchronized(gravit) {
                        for (entry in gravit) {
                            authorizeForMinute(entry.key, entry.value)
                            LogHelper.debug("${entry.key} (ip: ${entry.value}) logged in through GravitLauncher")
                        }
                        gravit.clear()
                    }
                }
                val iter = auths.iterator()
                while (iter.hasNext()) {
                    val entry = iter.next()
                    val player = Bukkit.getPlayerExact(entry.key.first)
                    if (player != null && player.address!!.hostString == entry.key.second) {
                        extendAuthorization(entry.value)
                    } else if (isExpired(entry.value)) {
                        iter.remove()
                    }
                }
            }
        }

    }

}
