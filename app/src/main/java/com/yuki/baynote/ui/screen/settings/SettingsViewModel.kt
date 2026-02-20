package com.yuki.baynote.ui.screen.settings

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yuki.baynote.data.BaynoteDatabase
import com.yuki.baynote.data.dao.TagDao
import com.yuki.baynote.data.model.Tag
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val tagDao: TagDao) : ViewModel() {

    val tags: StateFlow<List<Tag>> = tagDao.getAllTagsSortedByUsage()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createLabel(name: String) {
        viewModelScope.launch {
            tagDao.insertTag(Tag(name = name))
        }
    }

    fun deleteLabel(tag: Tag) {
        viewModelScope.launch {
            tagDao.deleteTag(tag)
        }
    }

    companion object {
        fun factory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val db = BaynoteDatabase.getInstance(application)
                    return SettingsViewModel(db.tagDao()) as T
                }
            }
    }
}

