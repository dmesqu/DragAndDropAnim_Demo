@file:OptIn(ExperimentalFoundationApi::class)

package edu.farmingdale.draganddropanim_demo

import android.content.ClipData
import android.content.ClipDescription
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.repeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.draganddrop.dragAndDropSource
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
//my solution todo3: need size to draw a visible rectangle
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.DragAndDropTransferData
import androidx.compose.ui.draganddrop.mimeTypes
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

@Composable
fun DragAndDropBoxes(modifier: Modifier = Modifier) {
    // controls whether the rect is rotating
    var isPlaying by remember { mutableStateOf(false) }
    //my solution todo7: track the target offset so we can move the rect horizontally & vertically
    // start at the center (0,0), and we offset relative to center of the bottom area
    var targetOffset by remember { mutableStateOf(IntOffset(0, 0)) }
    //my solution todo6: track rotation direction so drop "up" vs "down" can change animation
    var rotationDirection by remember { mutableStateOf(1f) }
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .weight(0.2f)
        ) {
            val boxCount = 4
            var dragBoxIndex by remember {
                mutableIntStateOf(0)
            }
            repeat(boxCount) { index ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(10.dp)
                        .border(1.dp, Color.Black)
                        .dragAndDropTarget(
                            shouldStartDragAndDrop = { event ->
                                event
                                    .mimeTypes()
                                    .contains(ClipDescription.MIMETYPE_TEXT_PLAIN)
                            },
                            target = remember {
                                object : DragAndDropTarget {
                                    override fun onDrop(event: DragAndDropEvent): Boolean {
                                        dragBoxIndex = index

                                        //my solution todo9: enable different animation based on which box receives the drop
                                        // 0,1 = "up" (negative Y), 2,3 = "down" (positive Y)
                                        targetOffset = when (index) {
                                            0 -> IntOffset(-150, -150)  // up-left
                                            1 -> IntOffset(150, -150)   // up-right
                                            2 -> IntOffset(-150, 150)   // down-left
                                            else -> IntOffset(150, 150) // down-right
                                        }
                                        // Up boxes rotate clockwise, down boxes rotate counter-clockwise
                                        rotationDirection = if (index <= 1) 1f else -1f
                                        isPlaying = true

                                        return true
                                    }
                                }
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    this@Row.AnimatedVisibility(
                        visible = index == dragBoxIndex,
                        enter = scaleIn() + fadeIn(),
                        exit = scaleOut() + fadeOut()
                    ) {
                        //my solution todo4: replace "Right" text with an icon/image as the drag source
                        Icon(
                            imageVector = Icons.Default.Face,
                            contentDescription = "Drag source icon",
                            tint = Color.Red,
                            modifier = Modifier
                                .fillMaxSize()
                                .dragAndDropSource(
                                    block = {
                                        detectTapGestures(
                                            onLongPress = {
                                                startTransfer(
                                                    transferData = DragAndDropTransferData(
                                                        clipData = ClipData.newPlainText(
                                                            "text",
                                                            ""
                                                        )
                                                    )
                                                )
                                            }
                                        )
                                    }
                                )
                        )
                    }
                }
            }
        }
        //my solution todo7: animate the rect’s position horizontally and vertically
        val pOffset by animateIntOffsetAsState(
            targetValue = targetOffset,
            animationSpec = tween(3000, easing = LinearEasing)
        )
        //my solution todo6: rotate the rect around itself, direction depends on drop (up/down)
        val rtatView by animateFloatAsState(
            targetValue = if (isPlaying) 360f * rotationDirection else 0.0f,
            animationSpec = if (isPlaying) {
                repeatable(
                    iterations = 10,
                    animation = tween(durationMillis = 3000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                )
            } else {
                // when resetting, snap back to 0° without spinning
                tween(durationMillis = 0)
            }
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.8f)
                .background(Color.Red)
        ) {
            //my solution todo8: button to reset the rect back to the center of the screen
            Button(
                onClick = {
                    targetOffset = IntOffset(0, 0)
                    isPlaying = false
                    rotationDirection = 1f
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Text(text = "Reset")
            }

            //my solution todo3: change the moving "circle" into a rectangle
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        // offset relative to the center of the screen
                        .offset(pOffset.x.dp, pOffset.y.dp)
                        .rotate(rtatView)
                        .size(width = 120.dp, height = 80.dp)
                        .background(Color.Yellow)
                        .border(2.dp, Color.Black)
                )
            }
        }
    }
}
