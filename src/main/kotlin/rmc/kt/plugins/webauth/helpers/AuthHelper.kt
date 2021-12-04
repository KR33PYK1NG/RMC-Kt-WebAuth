package rmc.kt.plugins.webauth.helpers

import org.apache.commons.lang.mutable.MutableLong
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitTask
import rmc.kt.plugins.core.helpers.TaskHelper

/**
 * Разработано командой RMC, 2021
 */
class AuthHelper {

    companion object {

        private val auths = mutableMapOf<Pair<String, String>, MutableLong>()
        private val tasks = mutableMapOf<String, BukkitTask>()

        /**
         * Проверяет, авторизован ли игрок под таким IP.
         *
         * @param username Ник игрока
         * @param address IP игрока
         */
        @JvmStatic
        fun isAuthorized(username: String, address: String): Boolean {
            return !isExpired(auths.get(Pair(username, address)))
        }

        /**
         * Авторизует игрока под таким IP на следующую минуту.
         *
         * @param username Ник игрока
         * @param address IP игрока
         */
        @JvmStatic
        fun authorizeForMinute(username: String, address: String) {
            extendAuthorization(auths.getOrPut(Pair(username, address)) { MutableLong() })
        }

        /**
         * Регистрирует новый таск авторизации для последующей отмены.
         *
         * @param username Ник игрока для привязки к таску
         * @param task Регистрируемый таск
         */
        @JvmStatic
        fun registerTask(username: String, task: BukkitTask) {
            tasks.put(username, task)
        }

        /**
         * Удаляет регистрацию таска авторизации и отменяет его.
         *
         * @param username Ник игрока, привязанный к таску
         */
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
                val iter = auths.iterator()
                while (iter.hasNext()) {
                    val entry = iter.next()
                    val player = Bukkit.getPlayerExact(entry.key.first)
                    if (player != null && player.address!!.address.hostAddress == entry.key.second) {
                        extendAuthorization(entry.value)
                    } else if (isExpired(entry.value)) {
                        iter.remove()
                    }
                }
            }
        }

    }

}
