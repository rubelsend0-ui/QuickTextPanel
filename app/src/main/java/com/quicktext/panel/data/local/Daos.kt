package com.quicktext.panel.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BoxDao {
    @Query("SELECT * FROM boxes ORDER BY createdAt ASC")
    fun observeAll(): Flow<List<BoxEntity>>

    @Insert
    suspend fun insert(box: BoxEntity): Long

    @Update
    suspend fun update(box: BoxEntity)

    @Delete
    suspend fun delete(box: BoxEntity)
}

@Dao
interface TemplateDao {
    @Query("SELECT * FROM templates ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<TemplateEntity>>

    @Insert
    suspend fun insert(template: TemplateEntity): Long

    @Update
    suspend fun update(template: TemplateEntity)

    @Delete
    suspend fun delete(template: TemplateEntity)

    @Query("UPDATE templates SET favorite = :favorite WHERE id = :id")
    suspend fun setFavorite(id: Long, favorite: Boolean)
}
