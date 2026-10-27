package dev.cankolay.twodo.android.data.api.client

import android.content.Context
import android.content.pm.ApplicationInfo
import android.util.Log
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
    private val environmentConfigRepository: EnvironmentConfigRepository
) {
    private val isDebug =
        (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

    private val client = HttpClient(engineFactory = OkHttp) {
        defaultRequest {
            url(urlString = environmentConfigRepository.get().apiUrl)
            contentType(type = ContentType.Application.Json)
        }

        install(createClientPlugin("Credentials") {
            onRequest { request, _ ->
                val token = credentials.state.first().token
                if (token.isNotBlank()) {
                    request.headers[HttpHeaders.Authorization] = "Bearer $token"
                }
            }
        })

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
                    },
            )
        }

        if (isDebug) {
            install(plugin = Logging) {
                level = LogLevel.INFO
                logger =
                    object : Logger {
                        override fun log(message: String) {
                            Log.d("KtorClient", message)
                        }
                    }
                sanitizeHeader { header -> header == HttpHeaders.Authorization }
            }
        }
    }

    operator fun invoke(): HttpClient = client
}
