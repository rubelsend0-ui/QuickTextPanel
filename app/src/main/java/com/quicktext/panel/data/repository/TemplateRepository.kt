package com.quicktext.panel.data.repository

import com.quicktext.panel.data.local.BoxDao
import com.quicktext.panel.data.local.BoxEntity
import com.quicktext.panel.data.local.TemplateDao
import com.quicktext.panel.data.local.TemplateEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TemplateRepository @Inject constructor(
    private val boxDao: BoxDao,
    private val templateDao: TemplateDao,
) {
    fun observeBoxes(): Flow<List<BoxEntity>> = boxDao.observeAll()
    fun observeTemplates(): Flow<List<TemplateEntity>> = templateDao.observeAll()

    suspend fun addBox(name: String): Long = boxDao.insert(BoxEntity(name = name.trim()))
    suspend fun renameBox(box: BoxEntity, name: String) = boxDao.update(box.copy(name = name.trim()))
    suspend fun deleteBox(box: BoxEntity) = boxDao.delete(box)

    suspend fun saveTemplate(template: TemplateEntity) {
        if (template.id == 0L) {
            templateDao.insert(template)
        } else {
            templateDao.update(template.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    suspend fun duplicate(template: TemplateEntity) {
        val now = System.currentTimeMillis()
        templateDao.insert(
            template.copy(
                id = 0,
                title = template.title + " (copy)",
                favorite = false,
                createdAt = now,
                updatedAt = now,
            ),
        )
    }

    suspend fun delete(template: TemplateEntity) = templateDao.delete(template)
    suspend fun setFavorite(id: Long, favorite: Boolean) = templateDao.setFavorite(id, favorite)
}
