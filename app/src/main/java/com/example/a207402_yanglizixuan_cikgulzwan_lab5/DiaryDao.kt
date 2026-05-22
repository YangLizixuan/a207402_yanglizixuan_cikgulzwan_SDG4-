package com.example.a207402_yanglizixuan_cikgulzwan_lab5

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DiaryDao {
    @Insert
    suspend fun insertDiary(diary: Diary)

    @Query("SELECT * FROM diary_table ORDER BY timestamp DESC")
    fun getAllDiaries(): Flow<List<Diary>>
}