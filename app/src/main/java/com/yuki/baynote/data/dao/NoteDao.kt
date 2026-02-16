package com.yuki.baynote.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.yuki.baynote.data.model.Note
import com.yuki.baynote.data.model.NoteWithTags
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    // ─── Insert ──────────────────────────────────────────────────────────────

    /** Insert a note and return its auto-generated id. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note): Long

    // ─── Update ──────────────────────────────────────────────────────────────

    @Update
    suspend fun updateNote(note: Note)

    // ─── Delete ──────────────────────────────────────────────────────────────

    @Delete
    suspend fun deleteNote(note: Note)

    // ─── Single-note queries ─────────────────────────────────────────────────

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: Long): Note?

    /**
     * Get a note with all its tags attached.
     * @Transaction ensures the note row and tag JOIN are read atomically.
     */
    @Transaction
    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteWithTags(id: Long): NoteWithTags?

    // ─── List queries ────────────────────────────────────────────────────────

    /**
     * All notes in a specific folder, pinned notes first, then by last modified.
     * Returns a Flow so the UI updates automatically when notes change.
     */
    @Transaction
    @Query("""
        SELECT * FROM notes
        WHERE folderId = :folderId
        ORDER BY isPinned DESC, updatedAt DESC
    """)
    fun getNotesInFolder(folderId: Long): Flow<List<NoteWithTags>>

    /**
     * Notes that are not in any folder (root-level notes), pinned first.
     */
    @Transaction
    @Query("""
        SELECT * FROM notes
        WHERE folderId IS NULL
        ORDER BY isPinned DESC, updatedAt DESC
    """)
    fun getRootNotes(): Flow<List<NoteWithTags>>

    /**
     * All notes regardless of folder, pinned first.
     * Used for the "All Notes" view and search.
     */
    @Transaction
    @Query("""
        SELECT * FROM notes
        ORDER BY isPinned DESC, updatedAt DESC
    """)
    fun getAllNotes(): Flow<List<NoteWithTags>>

    /**
     * Notes that have a specific tag applied.
     * The JOIN goes through note_tag_cross_ref to find matching notes.
     */
    @Transaction
    @Query("""
        SELECT DISTINCT n.* FROM notes n
        INNER JOIN note_tag_cross_ref ref ON n.id = ref.noteId
        WHERE ref.tagId = :tagId
        ORDER BY n.isPinned DESC, n.updatedAt DESC
    """)
    fun getNotesByTag(tagId: Long): Flow<List<NoteWithTags>>

    /**
     * Full-text search across title and content.
     * The % wildcards allow matching anywhere in the string (contains search).
     */
    @Transaction
    @Query("""
        SELECT * FROM notes
        WHERE title LIKE '%' || :query || '%'
           OR content LIKE '%' || :query || '%'
        ORDER BY isPinned DESC, updatedAt DESC
    """)
    fun searchNotes(query: String): Flow<List<NoteWithTags>>
}
