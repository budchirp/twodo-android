package dev.cankolay.twodo.android.data.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.cankolay.twodo.android.data.repository.api.CalendarRepositoryImpl
import dev.cankolay.twodo.android.data.repository.api.CoupleRepositoryImpl
import dev.cankolay.twodo.android.data.repository.api.InviteRepositoryImpl
import dev.cankolay.twodo.android.data.repository.api.NoteRepositoryImpl
import dev.cankolay.twodo.android.data.repository.api.UserRepositoryImpl
import dev.cankolay.twodo.android.data.repository.application.AuthStateRepositoryImpl
import dev.cankolay.twodo.android.data.repository.application.EnvironmentConfigRepositoryImpl
import dev.cankolay.twodo.android.data.repository.application.SettingsStateRepositoryImpl
import dev.cankolay.twodo.android.domain.repository.api.CalendarRepository
import dev.cankolay.twodo.android.domain.repository.api.CoupleRepository
import dev.cankolay.twodo.android.domain.repository.api.InviteRepository
import dev.cankolay.twodo.android.domain.repository.api.NoteRepository
import dev.cankolay.twodo.android.domain.repository.api.UserRepository
import dev.cankolay.twodo.android.domain.repository.application.AuthStateRepository
import dev.cankolay.twodo.android.domain.repository.application.EnvironmentConfigRepository
import dev.cankolay.twodo.android.domain.repository.application.SettingsStateRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindCalendarRepository(
        impl: CalendarRepositoryImpl
    ): CalendarRepository

    @Binds
    abstract fun bindCoupleRepository(
        impl: CoupleRepositoryImpl
    ): CoupleRepository

    @Binds
    abstract fun bindInviteRepository(
        impl: InviteRepositoryImpl
    ): InviteRepository

    @Binds
    abstract fun bindNoteRepository(
        impl: NoteRepositoryImpl
    ): NoteRepository

    @Binds
    abstract fun bindUserRepository(
        impl: UserRepositoryImpl
    ): UserRepository

    @Binds
    abstract fun bindSettingsStateRepository(
        impl: SettingsStateRepositoryImpl
    ): SettingsStateRepository

    @Binds
    abstract fun bindAuthStateRepository(
        impl: AuthStateRepositoryImpl
    ): AuthStateRepository

    @Binds
    @Singleton
    abstract fun bindEnvironmentConfigRepository(
        impl: EnvironmentConfigRepositoryImpl
    ): EnvironmentConfigRepository
}
