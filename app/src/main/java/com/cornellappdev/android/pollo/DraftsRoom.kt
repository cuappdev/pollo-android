package com.cornellappdev.android.pollo

import androidx.room.*

@androidx.room.Entity
data class DraftEntity(
        @PrimaryKey(autoGenerate = true)
        var id: Int,

        @ColumnInfo(name = "question") var question: String,
        @ColumnInfo(name = "options") var options: ArrayList<String>,
        @ColumnInfo(name = "correctAnswer") var correctAnswer: Int
)

@Dao
interface DraftDao {
        @Query("SELECT * FROM draftentity")
        fun getAll(): List<DraftEntity>

        @Insert
        fun insertAll(vararg draft: DraftEntity)

//        @Delete
//        fun delete(todo: TodoEntity)
//
//        @Update
//        fun updateTodo(vararg todos: TodoEntity)
}

@Database(entities = [DraftEntity::class], version = 1)
@TypeConverters(StringListConverter::class)
abstract class AppDatabase : RoomDatabase() {
        abstract fun draftDao(): DraftDao

//        companion object {
//                private fun buildDatabase(context: Context) = Room.databaseBuilder(context,
//                        AppDatabase::class.java, "draft-list.db")
//                        .build()
//        }
}

