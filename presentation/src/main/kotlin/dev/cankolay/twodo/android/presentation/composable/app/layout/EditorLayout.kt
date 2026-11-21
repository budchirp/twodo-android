package dev.cankolay.twodo.android.presentation.composable.app.layout

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import dev.cankolay.twodo.android.presentation.composable.app.AppLinearProgressIndicator
import dev.cankolay.twodo.android.presentation.composable.app.EditorBottomBar
import dev.cankolay.twodo.android.presentation.navigation.route.Route

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorLayout(
    route: Route,
    title: @Composable () -> Unit,
    onBack: () -> Unit,
    onSave: () -> Unit,
    saveLabel: String,
    actionsLabel: String,
    onOpenActions: () -> Unit,
    canEdit: Boolean,
    isLoading: Boolean,
    showActions: Boolean = true,
    trailingContent: @Composable () -> Unit = {},
    content: @Composable () -> Unit,
) {
    AppLayout(
        route = route,
        context = { it.copy(scrollBehavior = null) },
        topBar = { context ->
            AppTopAppBar(
                context = context,
                type = AppTopAppBarType.Default,
                title = title,
                onBackClick = onBack,
                trailingContent = trailingContent,
            )
        },
        bottomBar = {
            EditorBottomBar(
                saveLabel = saveLabel,
                onSave = onSave,
                actionsLabel = actionsLabel,
                onOpenActions = onOpenActions,
                enabled = canEdit,
                showActions = showActions,
            )
        },
    ) {
        AppLinearProgressIndicator(isLoading = isLoading)

        content()
    }
}
