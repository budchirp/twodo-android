package dev.cankolay.twodo.android.data.api.client

import android.content.Context
import android.content.pm.ApplicationInfo
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.cankolay.twodo.android.domain.repository.application.EnvironmentConfigRepository
import dev.cankolay.twodo.android.domain.usecase.application.auth.GetAuthStateUseCase
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.cache.storage.FileStorage
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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KtorClient
@Inject
constructor(
    @ApplicationContext private val context: Context,
    private val getAuthStateUseCase: GetAuthStateUseCase,
    private val environmentConfigRepository: EnvironmentConfigRepository
) {
    private val mutex = Mutex()
    private var client: HttpClient? = null
    private var token: String? = null

    private val isDebug =
        (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

    suspend operator fun invoke(): HttpClient = mutex.withLock {
        val current = getAuthStateUseCase().first().token
        val cached = client

        if (cached != null && token == current) {
            return cached
        }

        cached?.close()

        return build(token = current).also {
            client = it
            token = current
        }
    }

    private fun build(token: String): HttpClient = HttpClient(engineFactory = OkHttp) {
        defaultRequest {
            url(urlString = environmentConfigRepository.get().apiUrl)

            if (token.isNotBlank()) {
                headers.append(
                    name = HttpHeaders.Authorization,
                    value = "Bearer $token"
                )
            }

            contentType(type = ContentType.Application.Json)
        }

        install(plugin = HttpTimeout) {
            requestTimeoutMillis = 15 * 1000
            connectTimeoutMillis = 10 * 1000
            socketTimeoutMillis = 15 * 1000
        }

        install(plugin = HttpCache) {
            publicStorage(storage = FileStorage(directory = context.cacheDir))
        }

        install(plugin = ContentNegotiation) {
            json(
                json =
                    Json {
                        ignoreUnknownKeys = true
                        coerceInputValues = true
                        prettyPrint = isDebug
                    },
            )
        }

        if (isDebug) {
            install(plugin = Logging) {
                level = LogLevel.ALL
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
}
