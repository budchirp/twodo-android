package dev.cankolay.twodo.android.data.repository.application

import android.content.Context
import android.content.pm.ApplicationInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.cankolay.twodo.android.domain.model.api.AuthApiConstants
import dev.cankolay.twodo.android.domain.model.application.EnvironmentConfig
import dev.cankolay.twodo.android.domain.model.application.EnvironmentType
import dev.cankolay.twodo.android.domain.repository.application.EnvironmentConfigRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EnvironmentConfigRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : EnvironmentConfigRepository {

    private val config: EnvironmentConfig by lazy {
        val isDebuggable = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        val type = if (isDebuggable) EnvironmentType.DEBUG else EnvironmentType.RELEASE

        val apiUrl = when (type) {
            EnvironmentType.DEBUG -> "http://192.168.1.13:8081"
            EnvironmentType.RELEASE -> "https://twodo-api.cankolay.dev"
        }

        val authUrl = when (type) {
            EnvironmentType.DEBUG -> "http://192.168.1.13:3000"
            EnvironmentType.RELEASE -> "https://trash.cankolay.dev"
        }

        EnvironmentConfig(
            type = type,
            apiUrl = apiUrl,
            authUrl = "$authUrl/en/authorize?id=${AuthApiConstants.APPLICATION_ID}&permissions=user:read&callback=twodo://authenticate"
        )
    }

    override fun get(): EnvironmentConfig = config
}
