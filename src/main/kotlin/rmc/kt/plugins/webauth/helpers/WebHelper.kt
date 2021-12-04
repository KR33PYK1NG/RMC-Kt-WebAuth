package rmc.kt.plugins.webauth.helpers

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import rmc.kt.plugins.core.helpers.LogHelper
import rmc.kt.plugins.webauth.WebAuthPlugin
import java.net.URL

/**
 * Разработано командой RMC, 2021
 */
class WebHelper {

    companion object {

        @Serializable
        class GameGetResponse(val success: Boolean, val message: String)

        /**
         * Проверяет, существует ли аккаунт с таким ником.
         *
         * @param username Ник аккаунта
         */
        @JvmStatic
        fun hasAccountGetRequestBlocking(username: String): GameGetResponse {
            return gameGetRequestBlocking("exists?username=$username")
        }

        /**
         * Проверяет, привязана ли к аккаунту почта.
         *
         * @param username Ник аккаунта
         */
        @JvmStatic
        fun hasEmailGetRequestBlocking(username: String): GameGetResponse {
            return gameGetRequestBlocking("has-email?username=$username")
        }

        /**
         * (Пере)привязывает почту к аккаунту.
         *
         * @param username Ник аккаунта
         * @param email Привязываемая почта
         */
        @JvmStatic
        fun emailGetRequestBlocking(username: String, email: String): GameGetResponse {
            return gameGetRequestBlocking("set-email?username=$username&email=$email")
        }

        /**
         * Пытается войти в аккаунт, используя пароль.
         *
         * @param username Ник аккаунта
         * @param password Пароль аккаунта
         * @param limit_ip IP входящего (защита от брутфорса)
         */
        @JvmStatic
        fun loginGetRequestBlocking(username: String, password: String, limit_ip: String): GameGetResponse {
            return gameGetRequestBlocking("auth?username=$username&password=$password&limit_ip=$limit_ip")
        }

        /**
         * Пытается зарегистрировать новый аккаунт.
         *
         * @param username Ник аккаунта
         * @param password Пароль аккаунта
         */
        @JvmStatic
        fun registerGetRequestBlocking(username: String, password: String): GameGetResponse {
            return gameGetRequestBlocking("register?username=$username&password=$password")
        }

        private fun gameGetRequestBlocking(relUrl: String): GameGetResponse {
            LogHelper.debug("About to send Web API request: $relUrl")
            return try {
                URL("${WebAuthPlugin.webBaseUrl}/api/game/$relUrl").openConnection().run {
                    setRequestProperty("XF-Api-Key", WebAuthPlugin.webApiKey)
                    readTimeout = WebAuthPlugin.webHttpTimeout
                    connectTimeout = WebAuthPlugin.webHttpTimeout

                    Json.decodeFromString(String(getInputStream().readAllBytes()))
                }
            } catch (stack: Throwable) {
                LogHelper.trace(stack)

                GameGetResponse(false, "Произошел сбой, сообщите админу!")
            }
        }

    }

}
