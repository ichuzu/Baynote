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

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTag(tag: Tag): Long

    @Update
    suspend fun updateTag(tag: Tag)

    @Delete
    suspend fun deleteTag(tag: Tag)

    @Query("SELECT * FROM tags ORDER BY usageCount DESC, name ASC")
    fun getAllTagsSortedByUsage(): Flow<List<Tag>>

    @Query("SELECT * FROM tags WHERE name = :name COLLATE NOCASE LIMIT 1")
    suspend fun getTagByName(name: String): Tag?

    @Query("UPDATE tags SET usageCount = usageCount + 1 WHERE id = :tagId")
    suspend fun incrementUsage(tagId: Long)

    @Query("UPDATE tags SET usageCount = MAX(0, usageCount - 1) WHERE id = :tagId")
    suspend fun decrementUsage(tagId: Long)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNoteTagCrossRef(crossRef: NoteTagCrossRef)

    @Delete
    suspend fun deleteNoteTagCrossRef(crossRef: NoteTagCrossRef)

    @Query("DELETE FROM note_tag_cross_ref WHERE noteId = :noteId")
    suspend fun deleteAllTagsForNote(noteId: Long)

    @Query("SELECT tagId FROM note_tag_cross_ref WHERE noteId = :noteId")
    suspend fun getTagIdsForNote(noteId: Long): List<Long>
}
