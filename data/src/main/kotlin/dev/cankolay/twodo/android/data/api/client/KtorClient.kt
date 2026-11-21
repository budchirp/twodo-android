package dev.cankolay.twodo.android.data.api.client

import android.content.Context
import android.content.pm.ApplicationInfo
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.cankolay.twodo.android.domain.repository.application.AuthStateRepository
import dev.cankolay.twodo.android.domain.repository.application.EnvironmentConfigRepository
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KtorClient
@Inject
constructor(
    @ApplicationContext private val context: Context,
    private val credentials: AuthStateRepository,
    private val environmentConfigRepository: EnvironmentConfigRepository,
) {
    private val client =
        HttpClient(engineFactory = OkHttp) {
            defaultRequest {
                url(urlString = environmentConfigRepository.get().apiUrl)
                contentType(type = ContentType.Application.Json)
            }

            install(
                plugin =
                    createClientPlugin(name = "Credentials") {
                        onRequest { request, _ ->
                            val language =
                                AppCompatDelegate.getApplicationLocales()[0]?.language
                                    ?: context.resources.configuration.locales[0]?.language
                            request.headers[HttpHeaders.AcceptLanguage] =
                                if (language == "tr") "tr" else "en"
                            val token = credentials.state.first().token
                            if (token.isNotBlank()) {
                                request.headers[HttpHeaders.Authorization] = "Bearer $token"
                            }
                        }
                        onResponse { response ->
                            if (response.status == HttpStatusCode.Unauthorized) {
                                val authorization =
                                    response.call.request.headers[HttpHeaders.Authorization]
                                if (authorization?.startsWith("Bearer ") == true) {
                                    credentials.invalidate(
                                        token = authorization.removePrefix("Bearer ")
                                    )
                                }
                            }
                        }
                    }
            )

            install(plugin = HttpTimeout) {
                requestTimeoutMillis = 15 * 1000
                connectTimeoutMillis = 10 * 1000
                socketTimeoutMillis = 15 * 1000
            }

            install(plugin = ContentNegotiation) {
                json(
                    json =
                        Json {
                            ignoreUnknownKeys = true
                            coerceInputValues = true
                        }
                )
            }

            if ((context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
                install(plugin = Logging) {
                    level = LogLevel.ALL
                    logger =
                        object : Logger {
                            override fun log(message: String) =
                                synchronized(this) {
                                    // Leave room for multi-byte UTF-8 characters and Logcat
                                    // prefixes.
                                    val chunks = buildList {
                                        var start = 0
                                        while (start < message.length) {
                                            var end = minOf(start + 1000, message.length)
                                            if (
                                                end < message.length &&
                                                message[end - 1].isHighSurrogate() &&
                                                message[end].isLowSurrogate()
                                            )
                                                end--
                                            add(message.substring(start, end))
                                            start = end
                                        }
                                    }
                                    chunks.forEachIndexed { index, chunk ->
                                        val prefix =
                                            if (chunks.size > 1) "[${index + 1}/${chunks.size}] "
                                            else ""
                                        Log.d("KtorClient", prefix + chunk)
                                    }
                                }
                        }
                    sanitizeHeader { header ->
                        header.lowercase() in
                                setOf(
                                    "authorization",
                                    "proxy-authorization",
                                    "cookie",
                                    "set-cookie"
                                )
                    }
                }
            }
        }

    operator fun invoke(): HttpClient = client
}
