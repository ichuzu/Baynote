package com.yuki.baynote.data.model

import androidx.room.Entity
import androidx.room.Index


@Entity(
    tableName = "note_tag_cross_ref",
    primaryKeys = ["noteId", "tagId"],
    indices = [Index("tagId")]
)
data class NoteTagCrossRef(
    val noteId: Long,
    val tagId: Long
)
