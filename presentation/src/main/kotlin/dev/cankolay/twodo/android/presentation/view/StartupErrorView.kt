package dev.cankolay.twodo.android.presentation.view

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.composable.ErrorCard
import dev.cankolay.twodo.android.presentation.composable.OnboardingLayout
import dev.cankolay.twodo.android.presentation.navigation.route.Route

@Composable
fun StartupErrorView(
    result: ApiResult<*>?,
    onRetry: () -> Unit,
) {
    StartupErrorContent(result = result, onRetry = onRetry)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun StartupErrorContent(result: ApiResult<*>?, onRetry: () -> Unit) {
    val isLoading = result == ApiResult.Loading

    OnboardingLayout(
        route = Route.StartupError,
        title = stringResource(id = R.string.startup_error_title),
        description = stringResource(id = R.string.startup_error_desc),
        isLoading = isLoading,
        onRefresh = onRetry,
        actions = {
            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                onClick = onRetry,
            ) {
                Text(text = stringResource(id = R.string.try_again))
            }
        },
        lazyContent = {
            when (result) {
                is ApiResult.Failure -> {
                    item {
                        ErrorCard(error = result.error.message)
                    }
                }

                null -> {
                    item {
                        ErrorCard(error = stringResource(id = R.string.startup_error_unknown))
                    }
                }

                else -> Unit
            }
        },
    )
}
