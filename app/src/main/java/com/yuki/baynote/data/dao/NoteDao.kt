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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note): Long

    @Update
    suspend fun updateNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)

    @Query("DELETE FROM notes WHERE folderId = :folderId")
    suspend fun deleteNotesByFolder(folderId: Long)

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: Long): Note?

    @Transaction
    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteWithTags(id: Long): NoteWithTags?

    @Transaction
    @Query("""
        SELECT * FROM notes
        WHERE folderId = :folderId
        ORDER BY isPinned DESC, updatedAt DESC
    """)
    fun getNotesInFolder(folderId: Long): Flow<List<NoteWithTags>>

    @Transaction
    @Query("""
        SELECT * FROM notes
        WHERE folderId IS NULL
        ORDER BY isPinned DESC, updatedAt DESC
    """)
    fun getRootNotes(): Flow<List<NoteWithTags>>

    @Transaction
    @Query("""
        SELECT * FROM notes
        ORDER BY isPinned DESC, updatedAt DESC
    """)
    fun getAllNotes(): Flow<List<NoteWithTags>>

    @Transaction
    @Query("""
        SELECT DISTINCT n.* FROM notes n
        INNER JOIN note_tag_cross_ref ref ON n.id = ref.noteId
        WHERE ref.tagId = :tagId
        ORDER BY n.isPinned DESC, n.updatedAt DESC
    """)
    fun getNotesByTag(tagId: Long): Flow<List<NoteWithTags>>

    @Transaction
    @Query("""
        SELECT * FROM notes
        WHERE title LIKE '%' || :query || '%'
           OR content LIKE '%' || :query || '%'
        ORDER BY isPinned DESC, updatedAt DESC
    """)
    fun searchNotes(query: String): Flow<List<NoteWithTags>>
}
