package com.yuki.baynote.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.yuki.baynote.data.dao.FolderDao
import com.yuki.baynote.data.dao.NoteDao
import com.yuki.baynote.data.dao.TagDao
import com.yuki.baynote.data.model.Folder
import com.yuki.baynote.data.model.Note
import com.yuki.baynote.data.model.NoteTagCrossRef
import com.yuki.baynote.data.model.Tag

@Database(
    entities = [
        Note::class,
        Folder::class,
        Tag::class,
        NoteTagCrossRef::class
    ],
    version = 1,
    exportSchema = false
)
abstract class BaynoteDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao
    abstract fun folderDao(): FolderDao
    abstract fun tagDao(): TagDao

    companion object {
        @Volatile
        private var INSTANCE: BaynoteDatabase? = null

        fun getInstance(context: Context): BaynoteDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BaynoteDatabase::class.java,
                    "baynote_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
