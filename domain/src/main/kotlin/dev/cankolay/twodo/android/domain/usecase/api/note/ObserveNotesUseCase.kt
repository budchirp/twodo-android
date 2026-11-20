package dev.cankolay.twodo.android.domain.usecase.api.note

import dev.cankolay.twodo.android.domain.repository.api.NoteRepository
import javax.inject.Inject

class ObserveNotesUseCase @Inject constructor(private val repository: NoteRepository) {
    operator fun invoke() = repository.notes
}
