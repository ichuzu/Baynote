package com.yuki.baynote.ui.screen.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.FolderOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yuki.baynote.data.model.Folder

@Composable
fun MoveToFolderDialog(
    folders: List<Folder>,
    currentFolderId: Long?,
    onDismiss: () -> Unit,
    onFolderSelected: (Long?) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Move to folder") },
        text = {
            LazyColumn {
                item {
                    FolderOption(
                        name = "No folder",
                        icon = { Icon(Icons.Outlined.FolderOff, contentDescription = null) },
                        isSelected = currentFolderId == null,
                        onClick = { onFolderSelected(null) }
                    )
                }
                items(folders) { folder ->
                    FolderOption(
                        name = folder.name,
                        icon = { Icon(Icons.Outlined.Folder, contentDescription = null) },
                        isSelected = folder.id == currentFolderId,
                        onClick = { onFolderSelected(folder.id) }
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun FolderOption(
    name: String,
    icon: @Composable () -> Unit,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 8.dp)
    ) {
        icon()
        Spacer(Modifier.width(16.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        if (isSelected) {
            Icon(
                Icons.Filled.Check,
                contentDescription = "Current",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
