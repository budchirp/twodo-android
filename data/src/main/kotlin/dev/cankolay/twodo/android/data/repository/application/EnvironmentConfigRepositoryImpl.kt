package dev.cankolay.twodo.android.data.repository.application

import android.content.Context
import android.content.pm.ApplicationInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.cankolay.twodo.android.domain.model.api.AuthApiConstants
import dev.cankolay.twodo.android.domain.model.api.EnvironmentApiConstants
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
            EnvironmentType.DEBUG -> EnvironmentApiConstants.Debug.API_URL
            EnvironmentType.RELEASE -> EnvironmentApiConstants.Release.API_URL
        }

        val authUrl = when (type) {
            EnvironmentType.DEBUG -> EnvironmentApiConstants.Debug.AUTH_URL
            EnvironmentType.RELEASE -> EnvironmentApiConstants.Release.AUTH_URL
        }

        EnvironmentConfig(
            type = type,
            apiUrl = apiUrl,
            authUrl = "$authUrl${AuthApiConstants.AUTHORIZE_PATH}?id=${AuthApiConstants.APPLICATION_ID}&permissions=${AuthApiConstants.PERMISSIONS}&callback=${AuthApiConstants.CALLBACK_URI}"
        )
    }

    override fun get(): EnvironmentConfig = config
}
