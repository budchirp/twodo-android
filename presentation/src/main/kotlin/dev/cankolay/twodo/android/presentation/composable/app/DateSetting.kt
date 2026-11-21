package dev.cankolay.twodo.android.presentation.composable.app

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import dev.cankolay.twodo.android.presentation.R
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateSetting(
    title: String,
    value: LocalDate?,
    onChange: (LocalDate?) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    allowFuture: Boolean = true,
    leadingContent: @Composable (() -> Unit)? = null,
    pickerState: DateSettingState = rememberDateSettingState(allowFuture = allowFuture),
) {
    val formatter =
        DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
            .withLocale(LocalLocale.current.platformLocale)

    SettingRow(
        modifier = modifier,
        title = title,
        description = value?.format(formatter) ?: stringResource(id = R.string.date_not_set),
        enabled = enabled,
        onClick = { pickerState.open(value) },
        leadingContent = leadingContent,
    )

    if (pickerState.visible) {
        val picker = pickerState.picker

        DatePickerDialog(
            onDismissRequest = { pickerState.visible = false },
            confirmButton = {
                TextButton(
                    enabled = picker.selectedDateMillis != null,
                    onClick = {
                        onChange(
                            picker.selectedDateMillis?.let {
                                Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                            }
                        )
                        pickerState.visible = false
                    },
                ) {
                    Text(text = stringResource(id = R.string.save))
                }
            },
            dismissButton = {
                if (value != null) {
                    TextButton(
                        onClick = {
                            onChange(null)
                            pickerState.visible = false
                        }
                    ) {
                        Text(text = stringResource(id = R.string.clear_date))
                    }
                }
                TextButton(onClick = { pickerState.visible = false }) {
                    Text(text = stringResource(id = R.string.cancel))
                }
            },
        ) {
            DatePicker(state = picker)
        }
    }
}

@Stable
@OptIn(ExperimentalMaterial3Api::class)
class DateSettingState
internal constructor(visibility: MutableState<Boolean>, internal val picker: DatePickerState) {
    var visible by visibility

    fun open(value: LocalDate?) {
        picker.selectedDateMillis = value?.toEpochDay()?.times(86_400_000L)
        picker.displayedMonthMillis =
            (value ?: LocalDate.now()).withDayOfMonth(1).toEpochDay() * 86_400_000L
        visible = true
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberDateSettingState(allowFuture: Boolean = true): DateSettingState {
    val visibility = rememberSaveable { mutableStateOf(value = false) }
    val picker =
        rememberDatePickerState(
            selectableDates =
                object : SelectableDates {
                    override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                        allowFuture ||
                                !Instant.ofEpochMilli(utcTimeMillis)
                                    .atZone(ZoneOffset.UTC)
                                    .toLocalDate()
                                    .isAfter(LocalDate.now())
                }
        )
    return remember(visibility, picker) {
        DateSettingState(visibility = visibility, picker = picker)
    }
}
