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

        @JvmStatic
        fun hasAccountGetRequestBlocking(username: String): GameGetResponse {
            return gameGetRequestBlocking("exists?username=$username")
        }

        @JvmStatic
        fun hasEmailGetRequestBlocking(username: String): GameGetResponse {
            return gameGetRequestBlocking("has-email?username=$username")
        }

        @JvmStatic
        fun emailGetRequestBlocking(username: String, email: String): GameGetResponse {
            return gameGetRequestBlocking("set-email?username=$username&email=$email")
        }

        @JvmStatic
        fun loginGetRequestBlocking(username: String, password: String, limit_ip: String): GameGetResponse {
            return gameGetRequestBlocking("auth?username=$username&password=$password&limit_ip=$limit_ip")
        }

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
