package com.yuki.baynote.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation

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

    val content: String,

    val folderId: Long? = null,

    val isPinned: Boolean = false,

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class NoteWithTags(
    @Embedded val note: Note,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            NoteTagCrossRef::class,
            parentColumn = "noteId",
            entityColumn = "tagId"
        )
    )
    val tags: List<Tag>
)
