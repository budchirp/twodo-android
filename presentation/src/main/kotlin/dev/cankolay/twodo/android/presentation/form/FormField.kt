package dev.cankolay.twodo.android.presentation.form

import androidx.annotation.StringRes

data class FormField<T>(
    val value: T,
    @param:StringRes val error: Int? = null
)

fun <T> FormField<T>.update(value: T) = copy(value = value, error = null)

fun FormField<String>.validateRequired(@StringRes error: Int) =
    if (value.isBlank()) copy(error = error) else copy(error = null)

fun <T> FormField<T?>.validatePresent(@StringRes error: Int) =
    if (value == null) copy(error = error) else copy(error = null)
