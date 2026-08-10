package dev.cankolay.twodo.android.data.cache

import dev.cankolay.twodo.android.domain.model.api.user.Couple
import dev.cankolay.twodo.android.domain.model.api.user.User
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionCache @Inject constructor() {
    private var cachedUser: User? = null
    private var isUserLoaded = false

    private var cachedCouple: Couple? = null
    private var isCoupleLoaded = false

    @Synchronized
    fun getUser(): User? = cachedUser

    @Synchronized
    fun isUserCached(): Boolean = isUserLoaded

    @Synchronized
    fun setUser(user: User?) {
        cachedUser = user
        isUserLoaded = true
        if (user?.couple != null) {
            cachedCouple = user.couple
            isCoupleLoaded = true
        }
    }

    @Synchronized
    fun getCouple(): Couple? = cachedCouple

    @Synchronized
    fun isCoupleCached(): Boolean = isCoupleLoaded

    @Synchronized
    fun setCouple(couple: Couple?) {
        cachedCouple = couple
        isCoupleLoaded = true
        cachedUser = cachedUser?.copy(couple = couple)
    }

    @Synchronized
    fun clear() {
        cachedUser = null
        isUserLoaded = false
        cachedCouple = null
        isCoupleLoaded = false
    }
}
