package dev.cankolay.twodo.android.presentation.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle

/** Only deliver an operation's navigation outcome while its route is resumed. */
@Composable
fun <T : Any> HandleNavigation(result: T?, onNavigate: (T) -> Unit) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val navigate by rememberUpdatedState(newValue = onNavigate)
    LaunchedEffect(result, lifecycle) {
        if (result == null) return@LaunchedEffect
        var delivered = false
        lifecycle.repeatOnLifecycle(state = Lifecycle.State.RESUMED) {
            if (!delivered) {
                delivered = true
                navigate(result)
            }
        }
    }
}
