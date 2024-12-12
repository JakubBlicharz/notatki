@file:Suppress("DEPRECATION")

package pl.destroyer.notatki.Components


import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import pl.destroyer.notatki.dane.Note
import kotlin.math.abs



@Composable
fun NoteListScreen(
    notatki: MutableList<Note>,
    onNoteClick: (Int) -> Unit,
    onAddNote: () -> Unit,
    onDeleteNote: (Note) -> Unit,
    onNoteReorder: (List<Int>) -> Unit
) {
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var draggedNoteId by remember { mutableStateOf<Int?>(null) }
    var offsetY by remember { mutableStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }



    Scaffold(
        bottomBar = {
            BottomAppBar(
                containerColor = Color.Transparent,
                contentColor = Color.White,
                modifier = Modifier.height(90.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FloatingActionButton(
                        onClick = onAddNote,
                        containerColor = Color(0xFF574AE2),
                        modifier = Modifier.size(62.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Note",
                            tint = Color.White
                        )
                    }
                }
            }
        },
        content = { padding ->
            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp)
            ) {
                itemsIndexed(
                    items = notatki,
                    key = { _, note -> note.id }
                ) { index, note ->
                    val animatedOffset by animateFloatAsState(if (draggedNoteId == note.id) offsetY else 0f)

                    Box(
                        modifier = Modifier
                            .graphicsLayer {
                                shadowElevation = if (draggedNoteId == note.id) 8.dp.value else 0.dp.value
                                translationY = animatedOffset
                                scaleX = if (isDragging && draggedNoteId == note.id) 0.95f else 1f
                                scaleY = if (isDragging && draggedNoteId == note.id) 0.95f else 1f
                            }
                            .padding(vertical = 8.dp)
                            .pointerInput(Unit) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = {
                                        draggedNoteId = note.id
                                        isDragging = true
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        offsetY += dragAmount.y

                                        val draggedIndex = notatki.indexOfFirst { it.id == draggedNoteId }
                                        val targetIndexCalculated = calculateDynamicTargetIndex(
                                            lazyListState, draggedIndex, offsetY, notatki
                                        )

                                        if (targetIndexCalculated != draggedIndex && abs(targetIndexCalculated - draggedIndex) == 1) {
                                            coroutineScope.launch {
                                                val newList = notatki.toMutableList()
                                                val noteToMove = newList.removeAt(draggedIndex)
                                                newList.add(targetIndexCalculated, noteToMove)

                                                newList.forEachIndexed { i, n -> n.order = i }
                                                notatki.clear()
                                                notatki.addAll(newList)
                                                onNoteReorder(newList.map { it.id })
                                                offsetY = 0f
                                            }
                                        }
                                    },
                                    onDragEnd = {
                                        draggedNoteId = null
                                        offsetY = 0f
                                        isDragging = false
                                    },
                                    onDragCancel = {
                                        draggedNoteId = null
                                        offsetY = 0f
                                        isDragging = false
                                    }
                                )
                            }
                    ) {
                        Note(
                            note = note,
                            onClick = { onNoteClick(note.id) },
                            onDelete = { onDeleteNote(note) }
                        )
                    }
                }
            }
        }
    )
}

private fun calculateDynamicTargetIndex(
    lazyListState: LazyListState,
    draggedIndex: Int,
    dragOffsetY: Float,
    notes: List<Note>,
): Int {
    val visibleItems = lazyListState.layoutInfo.visibleItemsInfo
    if (visibleItems.isEmpty() || draggedIndex !in notes.indices) return draggedIndex

    val draggedItem = visibleItems.find { it.index == draggedIndex } ?: return draggedIndex
    val draggedItemCenter = draggedItem.offset + draggedItem.size / 2 + dragOffsetY.toInt()

    val closestItem = visibleItems.minByOrNull { item ->
        val itemCenter = item.offset + item.size / 2
        abs(draggedItemCenter - itemCenter)
    }

    if (closestItem == null) return draggedIndex

    val targetIndex = closestItem.index

    return when {
        targetIndex > draggedIndex -> draggedIndex + 1
        targetIndex < draggedIndex -> draggedIndex - 1
        else -> draggedIndex
    }
}
