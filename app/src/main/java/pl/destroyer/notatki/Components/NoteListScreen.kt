@file:Suppress("DEPRECATION")

package pl.destroyer.notatki.Components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pl.destroyer.notatki.dane.Note
import kotlin.math.abs



@OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
@Composable
fun NoteListScreen(
    notatki: MutableList<Note>,
    onNoteClick: (Int) -> Unit,
    onAddNote: () -> Unit,
    onDeleteNote: (Note) -> Unit,
) {
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var draggedNoteId by remember { mutableStateOf<Int?>(null) }
    var offsetY by remember { mutableStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    var targetIndex by remember { mutableStateOf<Int?>(null) }
    val PurpleColor = Color(0xFF452971)

    Scaffold(
        bottomBar = {
            BottomAppBar(
                containerColor = Color.Transparent,
                contentColor = Color.White
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    FloatingActionButton(
                        onClick = onAddNote,
                        containerColor = PurpleColor,
                        modifier = Modifier
                            .width(75.dp)
                            .height(75.dp)
                    ) {
                        Text("+", fontSize = 30.sp)
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            itemsIndexed(
                items = notatki,
                key = { _, item -> item.id }
            ) { index, note ->
                val elevation by animateFloatAsState(
                    targetValue = if (draggedNoteId == note.id) 8.dp.value else 0.dp.value
                )

                AnimatedVisibility(visible = true, enter = fadeIn(), exit = fadeOut()) {
                    Box(
                        modifier = Modifier
                            .graphicsLayer {
                                shadowElevation = elevation
                                translationY = if (draggedNoteId == note.id) offsetY else 0f
                                scaleX = if (isDragging && draggedNoteId == note.id) 0.9f else 1f
                                scaleY = if (isDragging && draggedNoteId == note.id) 0.9f else 1f
                            }
                            .padding(vertical = 8.dp)
                            .animateItemPlacement()
                            .pointerInput(Unit) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = {
                                        draggedNoteId = note.id
                                        isDragging = true
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        offsetY += dragAmount.y

                                        if (draggedNoteId != null) {
                                            val draggedIndex =
                                                notatki.indexOfFirst { it.id == draggedNoteId }
                                            if (draggedIndex != -1) {
                                                val targetIndexCalculated =
                                                    calculateDynamicTargetIndex(
                                                        lazyListState,
                                                        draggedIndex,
                                                        offsetY,
                                                        notatki
                                                    )
                                                if (targetIndex != targetIndexCalculated) {
                                                    targetIndex = targetIndexCalculated
                                                    coroutineScope.launch {
                                                        delay(200)
                                                        if (targetIndex != null && targetIndex != draggedIndex) {
                                                            val noteToMove =
                                                                notatki.removeAt(draggedIndex)
                                                            notatki.add(targetIndex!!, noteToMove)}
                                                        }
                                                    }
                                                }
                                            }
                                        },
                                        onDragEnd = {
                                            draggedNoteId = null
                                            offsetY = 0f
                                            isDragging = false
                                            targetIndex = null
                                        },
                                        onDragCancel = {
                                            draggedNoteId = null
                                            offsetY = 0f
                                            isDragging = false
                                            targetIndex = null
                                        },
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

    }
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


    if (closestItem == null) {
        return if (dragOffsetY > 0) notes.lastIndex else 0
    }

    val targetIndex = closestItem.index
    val distanceThreshold = draggedItem.size / 8

    return if (targetIndex != draggedIndex && targetIndex in notes.indices &&
        abs(draggedItemCenter - (closestItem.offset + closestItem.size / 2)) > distanceThreshold
    ) {
        targetIndex
    } else {
        draggedIndex
    }
}