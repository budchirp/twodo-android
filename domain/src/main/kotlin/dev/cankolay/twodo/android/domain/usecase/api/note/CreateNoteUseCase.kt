package dev.cankolay.twodo.android.domain.usecase.api.note

import dev.cankolay.twodo.android.domain.repository.api.NoteRepository
import javax.inject.Inject

class CreateNoteUseCase @Inject constructor(private val repository: NoteRepository) {
    suspend operator fun invoke(title: String) = repository.create(title = title)
}
