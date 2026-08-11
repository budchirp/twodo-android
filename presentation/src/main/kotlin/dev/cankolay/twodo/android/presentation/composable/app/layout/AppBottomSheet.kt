package dev.cankolay.twodo.android.presentation.composable.app.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBottomSheet(
    title: String,
    description: String? = null,
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(),
    actions: (@Composable RowScope.() -> Unit)? = null,
    content: LazyListScope.() -> Unit = {}
) {
    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismiss
    ) {
        AppLazyColumn(
            modifier = Modifier.imePadding(),
            contentPadding = PaddingValues(all = 16.dp),
            fill = false
        ) {
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(space = 8.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )

                    description?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            }

            content()

            actions?.let { sheetActions ->
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(
                            space = 8.dp,
                            alignment = Alignment.End
                        )
                    ) {
                        sheetActions()
                    }
                }
            }
        }
    }
}
