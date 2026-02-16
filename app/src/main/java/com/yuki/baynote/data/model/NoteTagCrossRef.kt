package com.yuki.baynote.data.model

import androidx.room.Entity

/**
 * Junction table linking notes to tags (many-to-many relationship).
 *
 * A note can have many tags. A tag can apply to many notes.
 * Room requires this explicit cross-reference entity to model that.
 *
 * The composite primary key (noteId + tagId) ensures no duplicate entries.
 */
@Entity(
    tableName = "note_tag_cross_ref",
    primaryKeys = ["noteId", "tagId"]
)
data class NoteTagCrossRef(
    val noteId: Long,
    val tagId: Long
)
