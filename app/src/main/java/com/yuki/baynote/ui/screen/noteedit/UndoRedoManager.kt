package com.yuki.baynote.ui.screen.noteedit

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class UndoRedoManager {

    private val history = mutableListOf<String>()
    private var index = -1

    var canUndo by mutableStateOf(false)
        private set
    var canRedo by mutableStateOf(false)
        private set

    private fun updateFlags() {
        canUndo = index > 0
        canRedo = index < history.size - 1
    }

    fun pushState(content: String) {
        if (index >= 0 && index < history.size && history[index] == content) return
        // Truncate any redo history
        while (history.size > index + 1) {
            history.removeAt(history.size - 1)
        }
        history.add(content)
        index = history.size - 1
        // Cap history size
        if (history.size > 100) {
            history.removeAt(0)
            index--
        }
        updateFlags()
    }

    fun undo(): String? {
        if (!canUndo) return null
        index--
        updateFlags()
        return history[index]
    }

    fun redo(): String? {
        if (!canRedo) return null
        index++
        updateFlags()
        return history[index]
    }
}
