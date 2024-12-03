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
import kotlinx.coroutines.launch
import pl.destroyer.notatki.dane.Note


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
    var draggedIndex by remember { mutableStateOf<Int?>(null) }
    var initialDragIndex by remember { mutableStateOf<Int?>(null) }

    val displayedNotes by remember {
        derivedStateOf { notatki.toList() }
    }

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
                items = displayedNotes,
                key = { _, item -> item.id }
            ) { index, note ->
                val elevation by animateFloatAsState(
                    targetValue = if (draggedIndex == index) 8.dp.value else 0.dp.value
                )

                var offsetY by remember { mutableStateOf(0f) }

                AnimatedVisibility(visible = true, enter = fadeIn(), exit = fadeOut()) {
                    Box(
                        modifier = Modifier
                            .graphicsLayer {
                                shadowElevation = elevation
                                translationY = offsetY
                            }
                            .padding(vertical = 8.dp)
                            .animateItemPlacement()
                            .pointerInput(Unit) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = { offset ->
                                        val startIndex = lazyListState.layoutInfo.visibleItemsInfo
                                            .find { it.offset <= offset.y && offset.y <= it.offset + it.size }
                                            ?.index
                                        draggedIndex = startIndex
                                        initialDragIndex = startIndex
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        offsetY += dragAmount.y

                                        val targetItemIndex = calculateTargetIndex(lazyListState, offsetY)

                                        if (targetItemIndex != draggedIndex && targetItemIndex in displayedNotes.indices) {
                                            coroutineScope.launch {
                                                val noteToMove = notatki.removeAt(draggedIndex!!)
                                                notatki.add(targetItemIndex, noteToMove)
                                                draggedIndex = targetItemIndex
                                            }
                                        }
                                    },
                                    onDragEnd = {
                                        draggedIndex = null
                                        initialDragIndex = null
                                        offsetY = 0f
                                    },
                                    onDragCancel = {
                                        draggedIndex = null
                                        initialDragIndex = null
                                        offsetY = 0f
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
    }
}

private fun <T> MutableList<T>.swap(index1: Int, index2: Int) {
    if (index1 != index2 && index1 in indices && index2 in indices) {
        val temp = this[index1]
        this[index1] = this[index2]
        this[index2] = temp
    }
}

private fun calculateTargetIndex(
    lazyListState: LazyListState,
    dragAmount: Float,
): Int {
    val visibleItems = lazyListState.layoutInfo.visibleItemsInfo
    if (visibleItems.isEmpty()) {
        return 0
    }

    val draggedItemHeight = visibleItems[0].size
    val targetOffset = lazyListState.firstVisibleItemScrollOffset + dragAmount


    val targetIndex = visibleItems.indexOfFirst {
        val itemOffset = it.offset
        val itemHeight = it.size
        (targetOffset.toInt() + draggedItemHeight / 2) in (itemOffset - draggedItemHeight / 8)..(itemOffset + itemHeight + draggedItemHeight / 8)
    }

    return if (targetIndex == -1) {
        lazyListState.firstVisibleItemIndex
    } else {
        lazyListState.firstVisibleItemIndex + targetIndex
    }
}