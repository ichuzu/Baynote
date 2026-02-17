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

@Dao
interface FolderDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFolder(folder: Folder): Long

    @Update
    suspend fun updateFolder(folder: Folder)

    @Delete
    suspend fun deleteFolder(folder: Folder)

    @Query("SELECT * FROM folders WHERE parentId IS NULL ORDER BY name ASC")
    fun getRootFolders(): Flow<List<Folder>>

    @Query("SELECT * FROM folders WHERE parentId = :parentId ORDER BY name ASC")
    fun getChildFolders(parentId: Long): Flow<List<Folder>>

    @Query("SELECT * FROM folders WHERE id = :id")
    suspend fun getFolderById(id: Long): Folder?

    @Transaction
    @Query("SELECT * FROM folders WHERE id = :id")
    suspend fun getFolderWithChildren(id: Long): FolderWithChildren?

    @Query("SELECT * FROM folders ORDER BY parentId ASC, name ASC")
    fun getAllFolders(): Flow<List<Folder>>
}
