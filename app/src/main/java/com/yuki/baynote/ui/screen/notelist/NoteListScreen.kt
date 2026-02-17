package com.yuki.baynote.ui.screen.notelist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.yuki.baynote.data.model.Folder
import com.yuki.baynote.data.model.NoteWithTags
import com.yuki.baynote.ui.screen.components.FolderDrawerContent
import com.yuki.baynote.ui.screen.components.MoveToFolderDialog
import com.yuki.baynote.ui.screen.components.NoteCard
import com.yuki.baynote.ui.theme.AppTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteListScreen(
    viewModel: NoteListViewModel,
    onNoteClick: (Long) -> Unit,
    onCreateNote: () -> Unit,
    onFolderClick: (Long) -> Unit,
    onNavigateBack: (() -> Unit)? = null,
    onAllNotesClick: (() -> Unit)? = null,
    currentTheme: AppTheme = AppTheme.DEFAULT,
    onThemeChange: (AppTheme) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val isHome = onNavigateBack == null
    var isSearchActive by remember { mutableStateOf(false) }
    var noteToMove by remember { mutableStateOf<NoteWithTags?>(null) }

    val content: @Composable () -> Unit = {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                if (isSearchActive) {
                    SearchTopBar(
                        query = uiState.searchQuery,
                        onQueryChange = viewModel::onSearchQueryChange,
                        onClose = {
                            isSearchActive = false
                            viewModel.onSearchQueryChange("")
                        }
                    )
                } else {
                    TopAppBar(
                        title = {
                            Text(uiState.currentFolder?.name ?: "Baynote")
                        },
                        navigationIcon = {
                            if (isHome) {
                                val drawerState = LocalDrawerState.current
                                val scope = rememberCoroutineScope()
                                IconButton(onClick = { scope.launch { drawerState?.open() } }) {
                                    Icon(Icons.Filled.Menu, contentDescription = "Menu")
                                }
                            } else {
                                IconButton(onClick = { onNavigateBack?.invoke() }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                }
                            }
                        },
                        actions = {
                            IconButton(onClick = { isSearchActive = true }) {
                                Icon(Icons.Filled.Search, contentDescription = "Search")
                            }
                        }
                    )
                }
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onCreateNote,
                    shape = CircleShape
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "New note")
                }
            }
        ) { innerPadding ->
            NoteListContent(
                notes = uiState.notes,
                allFolders = uiState.allFolders,
                onNoteClick = onNoteClick,
                onPinToggle = { viewModel.togglePin(it.note) },
                onDelete = { viewModel.deleteNote(it.note) },
                onMoveToFolder = { noteToMove = it },
                emptyMessage = if (uiState.searchQuery.isNotBlank()) "No results" else if (isHome) "No notes yet" else "This folder is empty",
                modifier = Modifier.padding(innerPadding)
            )
        }

        noteToMove?.let { nwt ->
            MoveToFolderDialog(
                folders = uiState.allFolders,
                currentFolderId = nwt.note.folderId,
                onDismiss = { noteToMove = null },
                onFolderSelected = { targetId ->
                    viewModel.moveNoteToFolder(nwt.note, targetId)
                    noteToMove = null
                }
            )
        }
    }

    if (isHome) {
        val drawerState = rememberDrawerState(DrawerValue.Closed)
        val scope = rememberCoroutineScope()

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                FolderDrawerContent(
                    folders = uiState.folders,
                    onAllNotesClick = {
                        scope.launch { drawerState.close() }
                        onAllNotesClick?.invoke()
                    },
                    onFolderClick = { folderId ->
                        scope.launch { drawerState.close() }
                        onFolderClick(folderId)
                    },
                    onCreateFolder = viewModel::createFolder,
                    onDeleteFolder = viewModel::deleteFolder,
                    currentTheme = currentTheme,
                    onThemeChange = onThemeChange
                )
            }
        ) {
            androidx.compose.runtime.CompositionLocalProvider(
                LocalDrawerState provides drawerState
            ) {
                content()
            }
        }
    } else {
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchTopBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit
) {
    TopAppBar(
        title = {
            TextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text("Search notes...") },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
        },
        navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(Icons.Filled.Close, contentDescription = "Close search")
            }
        }
    )
}

@Composable
private fun NoteListContent(
    notes: List<NoteWithTags>,
    allFolders: List<Folder>,
    onNoteClick: (Long) -> Unit,
    onPinToggle: (NoteWithTags) -> Unit,
    onDelete: (NoteWithTags) -> Unit,
    onMoveToFolder: (NoteWithTags) -> Unit,
    emptyMessage: String,
    modifier: Modifier = Modifier
) {
    if (notes.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = emptyMessage,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.outline
            )
        }
        return
    }

    val pinned = notes.filter { it.note.isPinned }
    val others = notes.filter { !it.note.isPinned }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (pinned.isNotEmpty()) {
            item {
                Text(
                    "Pinned",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            items(pinned, key = { it.note.id }) { noteWithTags ->
                NoteCard(
                    noteWithTags = noteWithTags,
                    onClick = { onNoteClick(noteWithTags.note.id) },
                    onPinToggle = { onPinToggle(noteWithTags) },
                    onDelete = { onDelete(noteWithTags) },
                    onMoveToFolder = { onMoveToFolder(noteWithTags) },
                    folderName = noteWithTags.note.folderId?.let { folderId ->
                        allFolders.find { it.id == folderId }?.name
                    }
                )
            }
        }

        if (pinned.isNotEmpty() && others.isNotEmpty()) {
            item {
                Text(
                    "Others",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }

        items(others, key = { it.note.id }) { noteWithTags ->
            NoteCard(
                noteWithTags = noteWithTags,
                onClick = { onNoteClick(noteWithTags.note.id) },
                onPinToggle = { onPinToggle(noteWithTags) },
                onDelete = { onDelete(noteWithTags) },
                onMoveToFolder = { onMoveToFolder(noteWithTags) },
                folderName = noteWithTags.note.folderId?.let { folderId ->
                    allFolders.find { it.id == folderId }?.name
                }
            )
        }
    }
}

private val LocalDrawerState = androidx.compose.runtime.staticCompositionLocalOf<androidx.compose.material3.DrawerState?> { null }
