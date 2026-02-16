package com.yuki.baynote.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation

/**
 * Represents a single note.
 *
 * [folderId] is nullable — null means the note is not inside any folder
 * and will appear at the root level of the note list.
 *
 * [content] stores plain text for now. In a later phase this will hold
 * Markdown-formatted text (bold, italic, headers) which aligns with
 * the Phase 2 import/export parser.
 *
 * The ForeignKey on [folderId] ensures referential integrity:
 * if a folder is deleted, its notes' folderId is set to null (not deleted).
 * The Index on folderId speeds up the common query "give me all notes in folder X".
 */
@Entity(
    tableName = "notes",
    foreignKeys = [
        ForeignKey(
            entity = Folder::class,
            parentColumns = ["id"],
            childColumns = ["folderId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("folderId")]
)
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val title: String,

    // Plain text for Phase 1; will evolve to Markdown in Phase 2.
    val content: String,

    // Which folder this note belongs to. Null = no folder (root level).
    val folderId: Long? = null,

    // Pinned notes appear at the top of any list they're in.
    val isPinned: Boolean = false,

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * A note paired with all its tags.
 *
 * Room uses the @Relation + @Junction annotations to automatically
 * JOIN through [NoteTagCrossRef] and fetch the tag list for each note.
 *
 * Use this in DAO queries wherever the UI needs to display tags on a note.
 */
data class NoteWithTags(
    @Embedded val note: Note,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(NoteTagCrossRef::class)
    )
    val tags: List<Tag>
)
