package com.yuki.baynote.ui.screen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.yuki.baynote.data.model.Folder
import com.yuki.baynote.ui.theme.AppTheme

@Composable
fun FolderDrawerContent(
    folders: List<Folder>,
    onAllNotesClick: () -> Unit,
    onFolderClick: (Long) -> Unit,
    onCreateFolder: (String) -> Unit,
    onDeleteFolder: (Folder) -> Unit,
    currentTheme: AppTheme = AppTheme.DEFAULT,
    onThemeChange: (AppTheme) -> Unit = {}
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var folderToDelete by remember { mutableStateOf<Folder?>(null) }

    ModalDrawerSheet {
        Spacer(Modifier.height(16.dp))
        Text(
            "Baynote",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp)
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        NavigationDrawerItem(
            label = { Text("All Notes") },
            selected = false,
            onClick = onAllNotesClick,
            icon = { Icon(Icons.AutoMirrored.Outlined.Notes, contentDescription = null) },
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        Text(
            "Folders",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 28.dp, vertical = 4.dp)
        )

        folders.forEach { folder ->
            NavigationDrawerItem(
                label = { Text(folder.name) },
                selected = false,
                onClick = { onFolderClick(folder.id) },
                icon = { Icon(Icons.Outlined.Folder, contentDescription = null) },
                badge = {
                    IconButton(onClick = { folderToDelete = folder }) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Delete folder",
                            tint = MaterialTheme.colorScheme.outline
                        )
                    }
                },
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }

        NavigationDrawerItem(
            label = { Text("New Folder") },
            selected = false,
            onClick = { showCreateDialog = true },
            icon = { Icon(Icons.Filled.Add, contentDescription = null) },
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(Modifier.weight(1f))

        // Theme picker
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        Text(
            "Theme",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 28.dp, vertical = 4.dp)
        )
        Row(
            modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppTheme.entries.forEach { theme ->
                val isSelected = theme == currentTheme
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(theme.previewColor)
                        .then(
                            if (isSelected) Modifier.border(
                                width = 3.dp,
                                color = MaterialTheme.colorScheme.onSurface,
                                shape = CircleShape
                            ) else Modifier
                        )
                        .clickable { onThemeChange(theme) }
                )
            }
        }
        Spacer(Modifier.height(16.dp))
    }

    if (showCreateDialog) {
        CreateFolderDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = onCreateFolder
        )
    }

    folderToDelete?.let { folder ->
        AlertDialog(
            onDismissRequest = { folderToDelete = null },
            title = { Text("Delete Folder") },
            text = { Text("Are you sure you want to delete \"${folder.name}\"? All notes in this folder will also be deleted.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteFolder(folder)
                        folderToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { folderToDelete = null }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
