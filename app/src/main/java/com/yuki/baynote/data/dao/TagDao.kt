package com.yuki.baynote.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.yuki.baynote.data.model.NoteTagCrossRef
import com.yuki.baynote.data.model.Tag
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {

    // ─── Tags ─────────────────────────────────────────────────────────────────

    /**
     * Insert a new tag. Returns the new row's auto-generated id.
     * IGNORE means if the same tag name is inserted twice, the second is skipped.
     * (We'll enforce uniqueness at the ViewModel level with a "find or create" helper.)
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTag(tag: Tag): Long

    @Update
    suspend fun updateTag(tag: Tag)

    @Delete
    suspend fun deleteTag(tag: Tag)

    /** All tags sorted by usage (most used first). Used for the chip row. */
    @Query("SELECT * FROM tags ORDER BY usageCount DESC, name ASC")
    fun getAllTagsSortedByUsage(): Flow<List<Tag>>

    /** Lookup by exact name (case-insensitive). Used before creating a new tag. */
    @Query("SELECT * FROM tags WHERE name = :name COLLATE NOCASE LIMIT 1")
    suspend fun getTagByName(name: String): Tag?

    /** Increment a tag's usage count by 1 when it's applied to a note. */
    @Query("UPDATE tags SET usageCount = usageCount + 1 WHERE id = :tagId")
    suspend fun incrementUsage(tagId: Long)

    /** Decrement a tag's usage count by 1 when it's removed from a note (floor at 0). */
    @Query("UPDATE tags SET usageCount = MAX(0, usageCount - 1) WHERE id = :tagId")
    suspend fun decrementUsage(tagId: Long)

    // ─── Junction table (Note ↔ Tag links) ───────────────────────────────────

    /** Link a tag to a note. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNoteTagCrossRef(crossRef: NoteTagCrossRef)

    /** Unlink a tag from a note. */
    @Delete
    suspend fun deleteNoteTagCrossRef(crossRef: NoteTagCrossRef)

    /** Remove all tag links for a note (used when deleting a note). */
    @Query("DELETE FROM note_tag_cross_ref WHERE noteId = :noteId")
    suspend fun deleteAllTagsForNote(noteId: Long)

    /** All tag ids currently linked to a note. */
    @Query("SELECT tagId FROM note_tag_cross_ref WHERE noteId = :noteId")
    suspend fun getTagIdsForNote(noteId: Long): List<Long>
}
