package com.trendscope.app.data

import androidx.room.*

@Dao
interface ArticleDao {
    @Query("SELECT * FROM articles WHERE isBookmarked = 1 ORDER BY fetchedAt DESC")
    suspend fun getBookmarks(): List<Article>

    @Query("SELECT * FROM articles WHERE category = :category ORDER BY fetchedAt DESC")
    suspend fun getArticlesByCategory(category: String): List<Article>

    @Query("SELECT * FROM articles ORDER BY fetchedAt DESC")
    suspend fun getAllArticles(): List<Article>

    @Query("SELECT * FROM articles WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' ORDER BY fetchedAt DESC")
    suspend fun searchArticles(query: String): List<Article>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticles(articles: List<Article>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticle(article: Article)

    @Delete
    suspend fun deleteArticle(article: Article)

    @Query("UPDATE articles SET isBookmarked = NOT isBookmarked WHERE link = :link")
    suspend fun toggleBookmark(link: String)

    @Query("SELECT isBookmarked FROM articles WHERE link = :link")
    suspend fun isBookmarked(link: String): Boolean?

    @Query("DELETE FROM articles")
    suspend fun clearAll()

    @Query("SELECT DISTINCT category FROM articles ORDER BY category")
    suspend fun getCategories(): List<String>

    @Query("SELECT COUNT(*) FROM articles")
    suspend fun getCount(): Int
}
