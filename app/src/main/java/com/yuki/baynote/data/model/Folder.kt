package com.yuki.baynote.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "folders")
data class Folder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,

    val parentId: Long? = null,

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class FolderWithChildren(
    @Embedded val folder: Folder,

    @Relation(
        parentColumn = "id",
        entityColumn = "parentId"
    )
    val children: List<Folder>
)
