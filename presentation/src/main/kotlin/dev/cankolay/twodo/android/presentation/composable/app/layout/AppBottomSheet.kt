package dev.cankolay.twodo.android.presentation.composable.app.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.presentation.composable.ErrorCard
import dev.cankolay.twodo.android.presentation.composable.app.AppLinearProgressIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBottomSheet(
    title: String,
    description: String? = null,
    onDismiss: () -> Unit,
    result: ApiResult<*>? = null,
    onRefresh: (() -> Unit)? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
    content: (LazyListScope.() -> Unit)? = null,
) {
    val isLoading = result == ApiResult.Loading

    val active by rememberUpdatedState(newValue = isLoading)
    val sheetState =
        rememberBottomSheetState(
            initialValue = SheetValue.Hidden,
            confirmValueChange = { it != SheetValue.Hidden || !active },
        )
    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = { if (!isLoading) onDismiss() },
        sheetGesturesEnabled = !isLoading,
    ) {
        AppLinearProgressIndicator(isLoading = isLoading)

        Column(modifier = Modifier.imePadding()) {
            Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
                Text(
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp),
                    text = title,
                    style =
                        MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                )

                description?.let {
                    Text(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        text = it,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }

            if (result is ApiResult.Failure || content != null || actions != null) {
                AppLazyColumn(fill = false) {
                    if (result is ApiResult.Failure) {
                        item {
                            ErrorCard(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                error = result.error.message,
                                onRefresh = onRefresh,
                            )
                        }
                    }
                    content?.invoke(this)

                    actions?.let { sheetActions ->
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement =
                                    Arrangement.spacedBy(
                                        space = 8.dp,
                                        alignment = Alignment.End,
                                    ),
                            ) {
                                sheetActions()
                            }
                        }
                    }
                }
            }
        }
    }
}
