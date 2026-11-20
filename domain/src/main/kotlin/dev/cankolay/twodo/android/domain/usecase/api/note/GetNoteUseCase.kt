package dev.cankolay.twodo.android.domain.usecase.api.note

import dev.cankolay.twodo.android.domain.repository.api.NoteRepository
import javax.inject.Inject

class GetNoteUseCase @Inject constructor(private val repository: NoteRepository) {
    suspend operator fun invoke(id: String, forceRefresh: Boolean = false) =
        repository.get(id = id, forceRefresh = forceRefresh)
}
