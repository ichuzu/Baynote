package com.yuki.baynote.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a tag that can be applied to notes.
 *
 * Tags are stored in their own table and linked to notes
 * through [NoteTagCrossRef] (a many-to-many junction table).
 *
 * [usageCount] is incremented each time this tag is applied to a note.
 * The UI uses this to sort "frequently used" tags at the front of the chip row.
 */
@Entity(tableName = "tags")
data class Tag(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // Tag names are case-insensitive by convention (store lowercase, display as-is).
    val name: String,

    // Tracks how often this tag has been used. Drives the "frequent tags" sort order.
    val usageCount: Int = 0
)
