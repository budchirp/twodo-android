package dev.cankolay.twodo.android.presentation.composable

import android.os.Build
import android.view.RoundedCorner
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun rememberDeviceCornerRadius(): Dp {
    val view = LocalView.current
    val density = LocalDensity.current

    return remember(key1 = view, key2 = density) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val px = view.getAndroidWindowCornerRadiusPx()
            with(receiver = density) { px.toDp() }
        } else {
            0.dp
        }
    }
}

private fun View.getAndroidWindowCornerRadiusPx(): Int {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return 0

    val insets = rootWindowInsets ?: return 0

    val corners = listOfNotNull(
        insets.getRoundedCorner(RoundedCorner.POSITION_TOP_LEFT),
        insets.getRoundedCorner(RoundedCorner.POSITION_TOP_RIGHT),
        insets.getRoundedCorner(RoundedCorner.POSITION_BOTTOM_LEFT),
        insets.getRoundedCorner(RoundedCorner.POSITION_BOTTOM_RIGHT)
    )

    return corners.maxOfOrNull { it.radius } ?: 0
}