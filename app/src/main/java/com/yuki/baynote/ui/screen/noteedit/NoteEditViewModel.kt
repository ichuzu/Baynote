package com.yuki.baynote.ui.screen.noteedit

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yuki.baynote.data.BaynoteDatabase
import com.yuki.baynote.data.dao.NoteDao
import com.yuki.baynote.data.dao.TagDao
import com.yuki.baynote.data.model.Note
import com.yuki.baynote.data.model.NoteTagCrossRef
import com.yuki.baynote.data.model.Tag
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class NoteEditUiState(
    val id: Long = 0,
    val title: String = "",
    val content: String = "",
    val folderId: Long? = null,
    val isPinned: Boolean = false,
    val tags: List<Tag> = emptyList(),
    val isNew: Boolean = true,
    val isSaved: Boolean = false,
    val isDeleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

class NoteEditViewModel(
    private val noteDao: NoteDao,
    private val tagDao: TagDao,
    private val noteId: Long,
    initialFolderId: Long? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(NoteEditUiState())
    val uiState: StateFlow<NoteEditUiState> = _uiState.asStateFlow()

    val allTags: StateFlow<List<Tag>> = tagDao.getAllTagsSortedByUsage()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var autoSaveJob: Job? = null

    init {
        if (noteId != 0L) {
            viewModelScope.launch {
                noteDao.getNoteWithTags(noteId)?.let { nwt ->
                    _uiState.value = NoteEditUiState(
                        id = nwt.note.id,
                        title = nwt.note.title,
                        content = nwt.note.content,
                        folderId = nwt.note.folderId,
                        isPinned = nwt.note.isPinned,
                        tags = nwt.tags,
                        isNew = false,
                        createdAt = nwt.note.createdAt
                    )
                }
            }
        } else {
            _uiState.value = _uiState.value.copy(folderId = initialFolderId)
        }
    }

    fun onTitleChange(title: String) {
        _uiState.value = _uiState.value.copy(title = title)
        scheduleAutoSave()
    }

    fun onContentChange(content: String) {
        _uiState.value = _uiState.value.copy(content = content)
        scheduleAutoSave()
    }

    private fun scheduleAutoSave() {
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            delay(1000)
            persistToDatabase()
        }
    }

    private suspend fun persistToDatabase() {
        val state = _uiState.value
        if (state.title.isBlank() && state.content.isBlank()) return
        val now = System.currentTimeMillis()
        if (state.isNew) {
            val id = noteDao.insertNote(
                Note(
                    title = state.title,
                    content = state.content,
                    folderId = state.folderId,
                    isPinned = state.isPinned,
                    createdAt = now,
                    updatedAt = now
                )
            )
            _uiState.value = _uiState.value.copy(id = id, isNew = false, createdAt = now)
        } else {
            noteDao.updateNote(
                Note(
                    id = state.id,
                    title = state.title,
                    content = state.content,
                    folderId = state.folderId,
                    isPinned = state.isPinned,
                    createdAt = state.createdAt,
                    updatedAt = now
                )
            )
        }
    }

    fun togglePin() {
        _uiState.value = _uiState.value.copy(isPinned = !_uiState.value.isPinned)
    }

    fun createAndAddTag(name: String) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return
        viewModelScope.launch {
            val existing = tagDao.getTagByName(trimmed)
            val tag = if (existing != null) existing else {
                val id = tagDao.insertTag(Tag(name = trimmed))
                Tag(id = id, name = trimmed)
            }
            addTag(tag)
        }
    }

    fun addTag(tag: Tag) {
        val state = _uiState.value
        if (state.tags.any { it.id == tag.id }) return
        viewModelScope.launch {
            // Save note first if it's new so we have an ID
            if (state.isNew) persistToDatabase()
            val currentId = _uiState.value.id
            if (currentId == 0L) return@launch
            tagDao.insertNoteTagCrossRef(NoteTagCrossRef(currentId, tag.id))
            tagDao.incrementUsage(tag.id)
            _uiState.value = _uiState.value.copy(tags = _uiState.value.tags + tag)
        }
    }

    fun removeTag(tag: Tag) {
        val state = _uiState.value
        if (state.isNew || state.id == 0L) return
        viewModelScope.launch {
            tagDao.deleteNoteTagCrossRef(NoteTagCrossRef(state.id, tag.id))
            tagDao.decrementUsage(tag.id)
            _uiState.value = _uiState.value.copy(tags = state.tags.filter { it.id != tag.id })
        }
    }

    fun saveNote() {
        autoSaveJob?.cancel()
        val state = _uiState.value
        if (state.title.isBlank() && state.content.isBlank()) {
            _uiState.value = state.copy(isSaved = true)
            return
        }
        viewModelScope.launch {
            persistToDatabase()
            _uiState.value = _uiState.value.copy(isSaved = true)
        }
    }

    fun deleteNote() {
        val state = _uiState.value
        if (state.isNew) {
            _uiState.value = state.copy(isDeleted = true)
            return
        }
        viewModelScope.launch {
            noteDao.deleteNote(
                Note(
                    id = state.id,
                    title = state.title,
                    content = state.content,
                    folderId = state.folderId,
                    isPinned = state.isPinned
                )
            )
            _uiState.value = _uiState.value.copy(isDeleted = true)
        }
    }

    companion object {
        fun factory(application: Application, noteId: Long, initialFolderId: Long? = null): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val db = BaynoteDatabase.getInstance(application)
                    return NoteEditViewModel(db.noteDao(), db.tagDao(), noteId, initialFolderId) as T
                }
            }
    }
}
