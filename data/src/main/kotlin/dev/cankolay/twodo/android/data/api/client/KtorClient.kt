package dev.cankolay.twodo.android.data.api.client

import android.content.Context
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
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import javax.inject.Inject

class KtorClient
@Inject
constructor(
    @ApplicationContext private val context: Context,
    private val getAuthStateUseCase: GetAuthStateUseCase,
    private val environmentConfigRepository: EnvironmentConfigRepository
) {
    suspend operator fun invoke(): HttpClient {
        val authState = getAuthStateUseCase().first()

        val client = HttpClient(engineFactory = OkHttp) {
            defaultRequest {
                url(urlString = environmentConfigRepository.get().apiUrl)

                if (authState.token.isNotBlank()) {
                    headers.append(name = "Authorization", value = "Bearer ${authState.token}")
                }

                contentType(type = ContentType.Application.Json)
            }

            install(plugin = HttpTimeout) {
                requestTimeoutMillis = 10 * 1000
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
                            prettyPrint = true
                        },
                )
            }

            install(plugin = Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.ALL
                logger =
                    object : Logger {
                        override fun log(message: String) {
                            Log.i("KtorClient", message)
                        }
                    }
            }

        }

        return client
    }
}
