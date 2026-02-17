package com.yuki.baynote.ui.navigation

import android.app.Application
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.yuki.baynote.ui.screen.noteedit.NoteEditScreen
import com.yuki.baynote.ui.screen.noteedit.NoteEditViewModel
import com.yuki.baynote.ui.screen.notelist.NoteListScreen
import com.yuki.baynote.ui.screen.notelist.NoteListViewModel
import com.yuki.baynote.ui.theme.AppTheme

object BaynoteRoutes {
    const val NOTE_LIST = "notes"
    const val NOTE_EDIT = "note/{noteId}?folderId={folderId}"
    const val FOLDER_VIEW = "folder/{folderId}"

    fun noteEdit(noteId: Long, folderId: Long? = null) =
        if (folderId != null) "note/$noteId?folderId=$folderId" else "note/$noteId"
    fun folderView(folderId: Long) = "folder/$folderId"
}

@Composable
fun BaynoteNavGraph(
    navController: NavHostController,
    application: Application,
    currentTheme: AppTheme,
    onThemeChange: (AppTheme) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = BaynoteRoutes.NOTE_LIST,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }
    ) {

        composable(BaynoteRoutes.NOTE_LIST) {
            val viewModel: NoteListViewModel = viewModel(
                factory = NoteListViewModel.factory(application, folderId = null)
            )
            NoteListScreen(
                viewModel = viewModel,
                onNoteClick = { noteId -> navController.navigate(BaynoteRoutes.noteEdit(noteId)) },
                onCreateNote = { navController.navigate(BaynoteRoutes.noteEdit(0L)) },
                onFolderClick = { folderId -> navController.navigate(BaynoteRoutes.folderView(folderId)) },
                onAllNotesClick = { /* Already on home, do nothing */ },
                currentTheme = currentTheme,
                onThemeChange = onThemeChange
            )
        }

        composable(
            route = BaynoteRoutes.NOTE_EDIT,
            arguments = listOf(
                navArgument("noteId") { type = NavType.LongType },
                navArgument("folderId") {
                    type = NavType.LongType
                    defaultValue = 0L
                }
            )
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getLong("noteId") ?: 0L
            val folderId = backStackEntry.arguments?.getLong("folderId")?.takeIf { it != 0L }
            val viewModel: NoteEditViewModel = viewModel(
                factory = NoteEditViewModel.factory(application, noteId, folderId)
            )
            NoteEditScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = BaynoteRoutes.FOLDER_VIEW,
            arguments = listOf(navArgument("folderId") { type = NavType.LongType })
        ) { backStackEntry ->
            val folderId = backStackEntry.arguments?.getLong("folderId") ?: 0L
            val viewModel: NoteListViewModel = viewModel(
                factory = NoteListViewModel.factory(application, folderId = folderId)
            )
            NoteListScreen(
                viewModel = viewModel,
                onNoteClick = { noteId -> navController.navigate(BaynoteRoutes.noteEdit(noteId)) },
                onCreateNote = { navController.navigate(BaynoteRoutes.noteEdit(0L, folderId)) },
                onFolderClick = { id -> navController.navigate(BaynoteRoutes.folderView(id)) },
                onNavigateBack = { navController.popBackStack() },
                onAllNotesClick = {
                    navController.popBackStack(BaynoteRoutes.NOTE_LIST, inclusive = false)
                },
                currentTheme = currentTheme,
                onThemeChange = onThemeChange
            )
        }
    }
}
