package com.trendscope.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "articles")
data class Article(
    @PrimaryKey val link: String,
    val title: String,
    val description: String,
    val content: String = "",
    val pubDate: String,
    val imageUrl: String = "",
    val source: String,
    val category: String,
    val isBookmarked: Boolean = false,
    val fetchedAt: Long = System.currentTimeMillis()
)
