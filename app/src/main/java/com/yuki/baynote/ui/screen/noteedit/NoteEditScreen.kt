package com.yuki.baynote.ui.screen.noteedit

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yuki.baynote.ui.screen.components.LabelPickerDialog
import com.yuki.baynote.ui.screen.noteedit.markdown.ContentSegment
import com.yuki.baynote.ui.screen.noteedit.markdown.ContentSegmentParser
import com.yuki.baynote.ui.screen.noteedit.markdown.FormattingAction
import com.yuki.baynote.ui.screen.noteedit.markdown.MarkdownVisualTransformation

private data class IndexedSegment(val id: Int, val segment: ContentSegment)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditScreen(
    viewModel: NoteEditViewModel,
    onNavigateBack: () -> Unit,
    fontSize: Int = 16
) {
    val uiState by viewModel.uiState.collectAsState()
    val allTags by viewModel.allTags.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showLabelDialog by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    val segments = remember { mutableStateListOf<IndexedSegment>() }
    val textFieldValues = remember { mutableStateMapOf<Int, TextFieldValue>() }
    var nextSegmentId by remember { mutableIntStateOf(0) }
    var lastSyncedContent by remember { mutableStateOf<String?>(null) }
    var focusedSegId by remember { mutableIntStateOf(-1) }

    val undoManager = remember { UndoRedoManager() }
    var isFormulaMode by remember { mutableStateOf(false) }
    var formulaInsertFn by remember { mutableStateOf<((String) -> Unit)?>(null) }

    fun newId() = nextSegmentId++

    fun loadContentIntoSegments(content: String) {
        segments.clear()
        textFieldValues.clear()
        val parsed = ContentSegmentParser.parseSegments(content)
        parsed.forEach { seg ->
            val id = newId()
            segments.add(IndexedSegment(id, seg))
            if (seg is ContentSegment.Text) {
                textFieldValues[id] = TextFieldValue(seg.text, TextRange(seg.text.length))
            }
        }
        if (segments.lastOrNull()?.segment !is ContentSegment.Text) {
            val id = newId()
            segments.add(IndexedSegment(id, ContentSegment.Text("")))
            textFieldValues[id] = TextFieldValue("")
        }
        lastSyncedContent = content
    }

    fun syncToViewModel() {
        val content = ContentSegmentParser.segmentsToString(segments.map { it.segment })
        lastSyncedContent = content
        viewModel.onContentChange(content)
    }

    fun pushUndoState() {
        val content = ContentSegmentParser.segmentsToString(segments.map { it.segment })
        undoManager.pushState(content)
    }

    // Updates segment content without clearing — preserves TextField composable identity so
    // focus and keyboard stay open. Falls back to a full rebuild only if the segment count
    // or types change (e.g. a table was inserted/removed).
    fun restoreSegments(content: String) {
        val parsed = ContentSegmentParser.parseSegments(content)

        val sameStructure = parsed.size == segments.size &&
            parsed.zip(segments).all { (new, old) -> new::class == old.segment::class }

        if (sameStructure) {
            parsed.forEachIndexed { i, newSeg ->
                val id = segments[i].id
                segments[i] = segments[i].copy(segment = newSeg)
                if (newSeg is ContentSegment.Text) {
                    val prevCursor = textFieldValues[id]?.selection?.start ?: newSeg.text.length
                    val clampedCursor = prevCursor.coerceAtMost(newSeg.text.length)
                    textFieldValues[id] = TextFieldValue(newSeg.text, TextRange(clampedCursor))
                }
            }
        } else {
            loadContentIntoSegments(content)
        }
        lastSyncedContent = content
    }

    fun performUndo() {
        pushUndoState() // save current state so redo can restore it
        val restored = undoManager.undo() ?: return
        restoreSegments(restored)
        viewModel.onContentChange(restored)
    }

    fun performRedo() {
        val restored = undoManager.redo() ?: return
        restoreSegments(restored)
        viewModel.onContentChange(restored)
    }

    LaunchedEffect(uiState.content) {
        if (uiState.content != lastSyncedContent) {
            loadContentIntoSegments(uiState.content)
            undoManager.pushState(uiState.content)
        }
    }

    val markdownTransformation = remember { MarkdownVisualTransformation() }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    LaunchedEffect(uiState.isSaved, uiState.isDeleted) {
        if (uiState.isSaved || uiState.isDeleted) {
            onNavigateBack()
        }
    }

    BackHandler {
        viewModel.saveNote()
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    BasicTextField(
                        value = uiState.title,
                        onValueChange = viewModel::onTitleChange,
                        textStyle = MaterialTheme.typography.titleLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        singleLine = true,
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        decorationBox = { innerTextField ->
                            Box {
                                if (uiState.title.isEmpty()) {
                                    Text(
                                        "Title",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                },
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    IconButton(onClick = { viewModel.saveNote() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Save and go back")
                    }
                },
                actions = {
                    IconButton(onClick = { showLabelDialog = true }) {
                        Icon(
                            Icons.AutoMirrored.Outlined.Label,
                            contentDescription = "Labels",
                            tint = if (uiState.tags.isNotEmpty()) MaterialTheme.colorScheme.primary
                                   else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { viewModel.togglePin() }) {
                        Icon(
                            if (uiState.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = if (uiState.isPinned) "Unpin" else "Pin",
                            tint = if (uiState.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (!uiState.isNew) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .imePadding()
        ) {
            // Content segments
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                segments.forEachIndexed { index, indexedSeg ->
                    key(indexedSeg.id) {
                        when (val seg = indexedSeg.segment) {
                            is ContentSegment.Text -> {
                                val segId = indexedSeg.id
                                var fieldValue by remember {
                                    mutableStateOf(
                                        textFieldValues[segId] ?: TextFieldValue(seg.text)
                                    )
                                }
                                if (seg.text != fieldValue.text) {
                                    val incoming = textFieldValues[segId]
                                    fieldValue = incoming ?: TextFieldValue(seg.text, TextRange(seg.text.length))
                                }

                                val isLast = index == segments.lastIndex

                                TextField(
                                    value = fieldValue,
                                    onValueChange = { newVal ->
                                        val oldText = fieldValue.text
                                        val newText = newVal.text

                                        val isBackspace = newText.length == oldText.length - 1 &&
                                            newVal.selection.collapsed &&
                                            newVal.selection.start <= oldText.length - 1 &&
                                            oldText.removeRange(newVal.selection.start, newVal.selection.start + 1) == newText

                                        val isEnter = newText.length == oldText.length + 1 &&
                                            newVal.selection.collapsed &&
                                            newVal.selection.start > 0 &&
                                            newText[newVal.selection.start - 1] == '\n'

                                        val resolvedVal = when {
                                            isBackspace -> FormattingAction.smartBackspace(fieldValue) ?: newVal
                                            isEnter -> FormattingAction.smartEnter(newVal) ?: newVal
                                            else -> newVal
                                        }

                                        val shouldPush = resolvedVal.text.length > oldText.length
                                        if (shouldPush) pushUndoState()

                                        fieldValue = resolvedVal
                                        textFieldValues[segId] = resolvedVal
                                        val idx = segments.indexOfFirst { it.id == segId }
                                        if (idx >= 0) {
                                            segments[idx] = segments[idx].copy(
                                                segment = ContentSegment.Text(resolvedVal.text)
                                            )
                                            syncToViewModel()
                                        }
                                    },
                                    placeholder = if (index == 0 && segments.all {
                                            it.segment is ContentSegment.Text &&
                                                (it.segment as ContentSegment.Text).text.isEmpty()
                                        }) {
                                        { Text("Start writing...") }
                                    } else null,
                                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = fontSize.sp),
                                    visualTransformation = markdownTransformation,
                                    colors = transparentTextFieldColors(),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 24.dp)
                                        .onFocusChanged { state ->
                                            if (state.isFocused) focusedSegId = segId
                                        }
                                        .let {
                                            if (isLast) it.defaultMinSize(minHeight = 200.dp)
                                            else it
                                        }
                                        .pointerInput(segId) {
                                            val doubleTapTimeout = viewConfiguration.doubleTapTimeoutMillis
                                            val doubleTapSlop = viewConfiguration.touchSlop * 8
                                            var lastDownTime = 0L
                                            var lastDownPosition = Offset.Zero

                                            awaitEachGesture {
                                                val down = awaitFirstDown(requireUnconsumed = false)
                                                val downTime = System.currentTimeMillis()
                                                val elapsed = downTime - lastDownTime
                                                val dist = (down.position - lastDownPosition).getDistance()

                                                if (elapsed < doubleTapTimeout && dist < doubleTapSlop) {
                                                    val currentValue = textFieldValues[segId]
                                                    if (currentValue != null) {
                                                        val range = findWordBoundaries(
                                                            currentValue.text,
                                                            currentValue.selection.start
                                                        )
                                                        if (range != null) {
                                                            val newValue = currentValue.copy(
                                                                selection = TextRange(range.first, range.second)
                                                            )
                                                            fieldValue = newValue
                                                            textFieldValues[segId] = newValue
                                                        }
                                                    }
                                                    lastDownTime = 0L
                                                } else {
                                                    lastDownTime = downTime
                                                    lastDownPosition = down.position
                                                }
                                            }
                                        }
                                )
                            }

                            is ContentSegment.Table -> {
                                val segId = indexedSeg.id
                                TableEditor(
                                    rows = seg.rows,
                                    onRowsChange = { newRows ->
                                        val idx = segments.indexOfFirst { it.id == segId }
                                        if (idx >= 0) {
                                            segments[idx] = segments[idx].copy(
                                                segment = ContentSegment.Table(newRows)
                                            )
                                            syncToViewModel()
                                        }
                                    },
                                    onDelete = {
                                        pushUndoState()
                                        val idx = segments.indexOfFirst { it.id == segId }
                                        if (idx >= 0) {
                                            segments.removeAt(idx)
                                            mergeAdjacentTextSegments(segments, textFieldValues)
                                            if (segments.isEmpty()) {
                                                val id = newId()
                                                segments.add(IndexedSegment(id, ContentSegment.Text("")))
                                                textFieldValues[id] = TextFieldValue("")
                                            }
                                            syncToViewModel()
                                        }
                                    },
                                    onUndoCheckpoint = { pushUndoState() },
                                    onFormulaModeChange = { active, insertFn ->
                                        isFormulaMode = active
                                        formulaInsertFn = insertFn
                                    },
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Undo / Redo
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = { performUndo() },
                    enabled = undoManager.canUndo,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Undo,
                        contentDescription = "Undo",
                        modifier = Modifier.size(20.dp),
                        tint = if (undoManager.canUndo) MaterialTheme.colorScheme.onSurfaceVariant
                               else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    )
                }
                IconButton(
                    onClick = { performRedo() },
                    enabled = undoManager.canRedo,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Redo,
                        contentDescription = "Redo",
                        modifier = Modifier.size(20.dp),
                        tint = if (undoManager.canRedo) MaterialTheme.colorScheme.onSurfaceVariant
                               else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    )
                }
            }

            // Formatting toolbar (swapped for FormulaToolbar when a formula cell is focused)
            if (isFormulaMode) {
                FormulaToolbar(
                    onInsert = { op -> formulaInsertFn?.invoke(op) },
                    onAccept = { formulaInsertFn?.invoke(" ") }
                )
            } else {
            FormattingToolbar(
                onFormat = { option ->
                    pushUndoState()
                    when (option) {
                        FormattingOption.Table -> {
                            val focusIdx = segments.indexOfFirst { it.id == focusedSegId }
                            val idx = if (focusIdx >= 0) focusIdx else segments.lastIndex

                            if (idx >= 0 && segments[idx].segment is ContentSegment.Text) {
                                val tfv = textFieldValues[segments[idx].id]
                                val text = (segments[idx].segment as ContentSegment.Text).text
                                val cursor = tfv?.selection?.start ?: text.length

                                val before = text.substring(0, cursor)
                                val after = text.substring(cursor)

                                // Update current text segment to text-before-cursor
                                val beforeId = segments[idx].id
                                segments[idx] = segments[idx].copy(
                                    segment = ContentSegment.Text(before)
                                )
                                textFieldValues[beforeId] = TextFieldValue(before, TextRange(before.length))

                                // Insert table + text-after
                                val tableId = newId()
                                val afterId = newId()
                                segments.add(idx + 1, IndexedSegment(tableId, ContentSegmentParser.createEmptyTable()))
                                segments.add(idx + 2, IndexedSegment(afterId, ContentSegment.Text(after)))
                                textFieldValues[afterId] = TextFieldValue(after)
                            } else {
                                // Append table at end
                                val tableId = newId()
                                val afterId = newId()
                                segments.add(IndexedSegment(tableId, ContentSegmentParser.createEmptyTable()))
                                segments.add(IndexedSegment(afterId, ContentSegment.Text("")))
                                textFieldValues[afterId] = TextFieldValue("")
                            }
                            syncToViewModel()
                        }

                        else -> {
                            val tfv = textFieldValues[focusedSegId] ?: return@FormattingToolbar
                            val newTfv = when (option) {
                                FormattingOption.H1 -> FormattingAction.toggleHeading(tfv, 1)
                                FormattingOption.H2 -> FormattingAction.toggleHeading(tfv, 2)
                                FormattingOption.H3 -> FormattingAction.toggleHeading(tfv, 3)
                                FormattingOption.Bold -> FormattingAction.toggleBold(tfv)
                                FormattingOption.Italic -> FormattingAction.toggleItalic(tfv)
                                else -> tfv
                            }
                            textFieldValues[focusedSegId] = newTfv
                            val idx = segments.indexOfFirst { it.id == focusedSegId }
                            if (idx >= 0) {
                                segments[idx] = segments[idx].copy(
                                    segment = ContentSegment.Text(newTfv.text)
                                )
                                syncToViewModel()
                            }
                        }
                    }
                }
            )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete note?") },
            text = { Text("This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteNote()
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showLabelDialog) {
        LabelPickerDialog(
            allTags = allTags,
            noteTags = uiState.tags,
            onToggle = { tag, add ->
                if (add) viewModel.addTag(tag)
                else viewModel.removeTag(tag)
            },
            onDismiss = { showLabelDialog = false },
            onCreateLabel = viewModel::createAndAddTag
        )
    }

    LaunchedEffect(uiState.isNew) {
        if (uiState.isNew) {
            focusRequester.requestFocus()
        }
    }
}

private fun mergeAdjacentTextSegments(
    segments: MutableList<IndexedSegment>,
    textFieldValues: MutableMap<Int, TextFieldValue>
) {
    var i = 0
    while (i < segments.size - 1) {
        val current = segments[i].segment
        val next = segments[i + 1].segment
        if (current is ContentSegment.Text && next is ContentSegment.Text) {
            val merged = when {
                current.text.isEmpty() && next.text.isEmpty() -> ""
                current.text.isEmpty() -> next.text
                next.text.isEmpty() -> current.text
                else -> current.text + "\n" + next.text
            }
            val keepId = segments[i].id
            val removeId = segments[i + 1].id
            segments[i] = segments[i].copy(segment = ContentSegment.Text(merged))
            textFieldValues[keepId] = TextFieldValue(merged, TextRange(merged.length))
            textFieldValues.remove(removeId)
            segments.removeAt(i + 1)
        } else {
            i++
        }
    }
}

private fun findWordBoundaries(text: String, offset: Int): Pair<Int, Int>? {
    if (text.isEmpty()) return null
    val pos = offset.coerceIn(0, (text.length - 1).coerceAtLeast(0))
    if (text[pos].isWhitespace()) return null
    var start = pos
    while (start > 0 && !text[start - 1].isWhitespace()) start--
    var end = pos + 1
    while (end < text.length && !text[end].isWhitespace()) end++
    return Pair(start, end)
}

@Composable
private fun transparentTextFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent
)
