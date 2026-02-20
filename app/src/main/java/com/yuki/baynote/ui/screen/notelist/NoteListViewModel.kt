package com.yuki.baynote.ui.screen.notelist

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yuki.baynote.data.BaynoteDatabase
import com.yuki.baynote.data.dao.FolderDao
import com.yuki.baynote.data.dao.NoteDao
import com.yuki.baynote.data.dao.TagDao
import com.yuki.baynote.data.model.Folder
import com.yuki.baynote.data.model.Note
import com.yuki.baynote.data.model.NoteTagCrossRef
import com.yuki.baynote.data.model.NoteWithTags
import com.yuki.baynote.data.model.Tag
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class NoteListUiState(
    val notes: List<NoteWithTags> = emptyList(),
    val folders: List<Folder> = emptyList(),
    val allFolders: List<Folder> = emptyList(),
    val allTags: List<Tag> = emptyList(),
    val selectedTagId: Long? = null,
    val currentFolder: Folder? = null,
    val searchQuery: String = "",
    val isSearchActive: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class NoteListViewModel(
    private val noteDao: NoteDao,
    private val folderDao: FolderDao,
    private val tagDao: TagDao,
    private val folderId: Long?
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedTagId = MutableStateFlow<Long?>(null)

    // Always streams unfiltered notes for this scope — used to derive which tags are visible
    private val baseNotesFlow = if (folderId != null) noteDao.getNotesInFolder(folderId)
                                else noteDao.getAllNotes()

    private val notesFlow = combine(_searchQuery, _selectedTagId) { query, tagId ->
        Pair(query, tagId)
    }.flatMapLatest { (query, tagId) ->
        when {
            query.isNotBlank() -> noteDao.searchNotes(query)
            tagId != null -> noteDao.getNotesByTag(tagId)
            folderId != null -> noteDao.getNotesInFolder(folderId)
            else -> noteDao.getAllNotes()
        }
    }

    val uiState: StateFlow<NoteListUiState> = combine(
        notesFlow,
        folderDao.getRootFolders(),
        folderDao.getAllFolders(),
        combine(tagDao.getAllTagsSortedByUsage(), baseNotesFlow) { tags, baseNotes -> Pair(tags, baseNotes) },
        _selectedTagId
    ) { notes, rootFolders, allFolders, (allTags, baseNotes), selectedTagId ->
        val visibleTags = if (folderId != null) {
            val tagIdsInFolder = baseNotes.flatMap { nwt -> nwt.tags.map { it.id } }.toSet()
            allTags.filter { it.id in tagIdsInFolder }
        } else allTags
        NoteListUiState(
            notes = notes,
            folders = rootFolders,
            allFolders = allFolders,
            allTags = visibleTags,
            selectedTagId = selectedTagId,
            currentFolder = if (folderId != null) allFolders.find { it.id == folderId } else null,
            searchQuery = _searchQuery.value,
            isSearchActive = _searchQuery.value.isNotBlank()
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NoteListUiState())

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun selectTag(tagId: Long?) {
        _selectedTagId.value = tagId
    }

    fun togglePin(note: Note) {
        viewModelScope.launch {
            noteDao.updateNote(
                note.copy(isPinned = !note.isPinned, updatedAt = System.currentTimeMillis())
            )
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            noteDao.deleteNote(note)
        }
    }

    fun moveNoteToFolder(note: Note, targetFolderId: Long?) {
        viewModelScope.launch {
            noteDao.updateNote(
                note.copy(folderId = targetFolderId, updatedAt = System.currentTimeMillis())
            )
        }
    }

    fun addTagToNote(note: Note, tag: Tag) {
        viewModelScope.launch {
            tagDao.insertNoteTagCrossRef(NoteTagCrossRef(note.id, tag.id))
            tagDao.incrementUsage(tag.id)
        }
    }

    fun removeTagFromNote(note: Note, tag: Tag) {
        viewModelScope.launch {
            tagDao.deleteNoteTagCrossRef(NoteTagCrossRef(note.id, tag.id))
            tagDao.decrementUsage(tag.id)
        }
    }

    fun createLabel(name: String) {
        viewModelScope.launch {
            tagDao.insertTag(Tag(name = name))
        }
    }

    fun deleteLabel(tag: Tag) {
        viewModelScope.launch {
            tagDao.deleteTag(tag)
        }
    }

    fun createFolder(name: String) {
        viewModelScope.launch {
            folderDao.insertFolder(Folder(name = name))
        }
    }

    fun deleteFolder(folder: Folder) {
        viewModelScope.launch {
            noteDao.deleteNotesByFolder(folder.id)
            folderDao.deleteFolder(folder)
        }
    }

    companion object {
        fun factory(application: Application, folderId: Long?): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val db = BaynoteDatabase.getInstance(application)
                    return NoteListViewModel(db.noteDao(), db.folderDao(), db.tagDao(), folderId) as T
                }
            }
    }
}
