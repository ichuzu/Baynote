package com.yuki.baynote.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.yuki.baynote.data.model.Folder
import com.yuki.baynote.data.model.FolderWithChildren
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for folder operations.
 *
 * All functions that return Flow will automatically re-emit whenever the
 * underlying data changes — no manual refresh needed in the UI.
 *
 * @Dao tells Room this interface defines database operations.
 * Room generates the actual SQL implementation at compile time via KSP.
 */
@Dao
interface FolderDao {

    // ─── Insert ──────────────────────────────────────────────────────────────

    /**
     * Insert a new folder. Returns the new row's auto-generated id.
     * IGNORE strategy means if somehow an id collision occurs, it's skipped silently.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFolder(folder: Folder): Long

    // ─── Update ──────────────────────────────────────────────────────────────

    @Update
    suspend fun updateFolder(folder: Folder)

    // ─── Delete ──────────────────────────────────────────────────────────────

    @Delete
    suspend fun deleteFolder(folder: Folder)

    // ─── Queries ─────────────────────────────────────────────────────────────

    /** All root-level folders (no parent), sorted alphabetically. */
    @Query("SELECT * FROM folders WHERE parentId IS NULL ORDER BY name ASC")
    fun getRootFolders(): Flow<List<Folder>>

    /** All subfolders of a given parent, sorted alphabetically. */
    @Query("SELECT * FROM folders WHERE parentId = :parentId ORDER BY name ASC")
    fun getChildFolders(parentId: Long): Flow<List<Folder>>

    /** Get a single folder by id. */
    @Query("SELECT * FROM folders WHERE id = :id")
    suspend fun getFolderById(id: Long): Folder?

    /**
     * Get a folder together with its direct children.
     * @Transaction tells Room to run both the parent and child queries
     * in a single database transaction so results are consistent.
     */
    @Transaction
    @Query("SELECT * FROM folders WHERE id = :id")
    suspend fun getFolderWithChildren(id: Long): FolderWithChildren?

    /** All folders — useful for building a full tree in the ViewModel. */
    @Query("SELECT * FROM folders ORDER BY parentId ASC, name ASC")
    fun getAllFolders(): Flow<List<Folder>>
}
