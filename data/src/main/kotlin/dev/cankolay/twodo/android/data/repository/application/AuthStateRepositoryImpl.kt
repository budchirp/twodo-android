package dev.cankolay.twodo.android.data.repository.application

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.cankolay.twodo.android.data.di.AuthDataStore
import dev.cankolay.twodo.android.domain.model.application.AuthState
import dev.cankolay.twodo.android.domain.repository.application.AuthStateRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

class AuthStateRepositoryImpl
@Inject
constructor(
    @AuthDataStore
    private val dataStore: DataStore<Preferences>
) : AuthStateRepository {
    private object PreferenceKeys {
        val TOKEN = stringPreferencesKey(name = "token")
    }

    override suspend fun update(state: AuthState) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.TOKEN] = state.token
        }
    }

    private val default = AuthState()

    override val state = dataStore.data.catch { exception ->
        if (exception is IOException) {
            emit(emptyPreferences())
        } else {
            throw exception
        }
    }.map { preferences ->
        AuthState(
            token = preferences[PreferenceKeys.TOKEN] ?: default.token
        )
    }
}
