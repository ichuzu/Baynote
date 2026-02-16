package com.yuki.baynote.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation

/**
 * Represents a folder in the note hierarchy.
 *
 * Folders support unlimited nesting via [parentId]:
 *   - parentId = null  →  root-level folder
 *   - parentId = X     →  subfolder inside folder with id X
 *
 * Notes can optionally belong to a folder (see Note.folderId).
 * Notes with folderId = null live outside any folder.
 */
@Entity(tableName = "folders")
data class Folder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,

    // Null means this is a top-level folder.
    // Non-null means this is nested inside another folder.
    val parentId: Long? = null,

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * A folder paired with its direct children.
 * Used by the DAO when building the folder tree UI.
 *
 * Note: this only fetches one level of children. To render
 * the full tree, you'll load children recursively in the ViewModel.
 */
data class FolderWithChildren(
    @Embedded val folder: Folder,

    @Relation(
        parentColumn = "id",
        entityColumn = "parentId"
    )
    val children: List<Folder>
)
