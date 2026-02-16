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

/**
 * The single Room database for BayNote.
 *
 * @Database lists every @Entity class that Room should create a table for.
 * [version] must be incremented whenever you change the schema (add/remove columns, tables).
 * When you bump the version you'll also need to provide a Migration — we'll handle that later.
 *
 * This class uses the Singleton pattern so the database is only opened once per app launch.
 * Opening a database is expensive; you never want more than one instance.
 */
@Database(
    entities = [
        Note::class,
        Folder::class,
        Tag::class,
        NoteTagCrossRef::class
    ],
    version = 1,
    exportSchema = false   // Set to true later if you want schema version history files
)
abstract class BaynoteDatabase : RoomDatabase() {

    // Room generates concrete implementations of these at compile time.
    abstract fun noteDao(): NoteDao
    abstract fun folderDao(): FolderDao
    abstract fun tagDao(): TagDao

    companion object {
        // @Volatile means writes to this field are immediately visible to all threads.
        @Volatile
        private var INSTANCE: BaynoteDatabase? = null

        /**
         * Get (or create) the single database instance.
         *
         * [synchronized] ensures only one thread can enter this block at a time,
         * preventing two threads from each creating their own instance on first access.
         */
        fun getInstance(context: Context): BaynoteDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,  // Always use applicationContext to avoid memory leaks
                    BaynoteDatabase::class.java,
                    "baynote_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
