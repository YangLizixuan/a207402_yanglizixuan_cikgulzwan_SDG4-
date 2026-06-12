package com.example.a207402_yanglizixuan_cikgulzwan_Project2

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "diary_table")
data class Diary(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String = "",  // 给 title 加默认值
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)