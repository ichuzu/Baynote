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
import com.yuki.baynote.ui.screen.settings.SettingsScreen
import com.yuki.baynote.ui.screen.themecreator.ThemeCreatorScreen
import com.yuki.baynote.ui.theme.AppTheme
import com.yuki.baynote.ui.theme.CustomThemeColors
import com.yuki.baynote.ui.theme.DarkModePreference

object BaynoteRoutes {
    const val NOTE_LIST = "notes"
    const val NOTE_EDIT = "note/{noteId}?folderId={folderId}"
    const val FOLDER_VIEW = "folder/{folderId}"
    const val SETTINGS = "settings"
    const val THEME_CREATOR = "theme_creator"
    const val THEME_CREATOR_EDIT = "theme_creator/{editIndex}"

    fun noteEdit(noteId: Long, folderId: Long? = null) =
        if (folderId != null) "note/$noteId?folderId=$folderId" else "note/$noteId"
    fun folderView(folderId: Long) = "folder/$folderId"
    fun themeCreatorEdit(index: Int) = "theme_creator/$index"
}

@Composable
fun BaynoteNavGraph(
    navController: NavHostController,
    application: Application,
    currentTheme: AppTheme,
    onThemeChange: (AppTheme) -> Unit,
    darkMode: DarkModePreference,
    onDarkModeChange: (DarkModePreference) -> Unit,
    fontSize: Int,
    onFontSizeChange: (Int) -> Unit,
    headingMargin: Int,
    onHeadingMarginChange: (Int) -> Unit,
    customColors: CustomThemeColors?,
    savedThemes: List<CustomThemeColors>,
    onCustomThemeSave: (CustomThemeColors, Int?) -> Unit,
    onSavedThemeSelect: (CustomThemeColors) -> Unit,
    onSavedThemeDelete: (Int) -> Unit
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
                onAllNotesClick = { /* Already on home */ },
                onSettingsClick = { navController.navigate(BaynoteRoutes.SETTINGS) }
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
                onNavigateBack = { navController.popBackStack() },
                fontSize = fontSize
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
                onSettingsClick = { navController.navigate(BaynoteRoutes.SETTINGS) }
            )
        }

        composable(BaynoteRoutes.SETTINGS) {
            SettingsScreen(
                currentTheme = currentTheme,
                onThemeChange = onThemeChange,
                darkMode = darkMode,
                onDarkModeChange = onDarkModeChange,
                fontSize = fontSize,
                onFontSizeChange = onFontSizeChange,
                headingMargin = headingMargin,
                onHeadingMarginChange = onHeadingMarginChange,
                customColors = customColors,
                savedThemes = savedThemes,
                onThemeCreatorClick = { navController.navigate(BaynoteRoutes.THEME_CREATOR) },
                onSavedThemeSelect = onSavedThemeSelect,
                onSavedThemeEdit = { index -> navController.navigate(BaynoteRoutes.themeCreatorEdit(index)) },
                onSavedThemeDelete = onSavedThemeDelete,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Create new theme
        composable(BaynoteRoutes.THEME_CREATOR) {
            ThemeCreatorScreen(
                initialColors = null,
                onSave = { colors ->
                    onCustomThemeSave(colors, null)
                    navController.popBackStack()
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Edit existing saved theme
        composable(
            route = BaynoteRoutes.THEME_CREATOR_EDIT,
            arguments = listOf(navArgument("editIndex") { type = NavType.IntType })
        ) { backStackEntry ->
            val editIndex = backStackEntry.arguments!!.getInt("editIndex")
            ThemeCreatorScreen(
                initialColors = savedThemes.getOrNull(editIndex),
                onSave = { colors ->
                    onCustomThemeSave(colors, editIndex)
                    navController.popBackStack()
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
