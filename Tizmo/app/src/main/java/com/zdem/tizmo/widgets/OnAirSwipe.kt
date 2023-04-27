package com.zdem.tizmo.widgets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material.swipeable
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun OnAirSwipe(
    currentState: Boolean,
    onSwipeFinished: () -> Unit,
    onReset: () -> Unit,
) {
    val swipeableState = rememberSwipeableState(initialValue = 0)


    val widthInPx = with(LocalDensity.current) {
        300.dp.toPx()
    }
    val anchors = mapOf(
        0F to 0,
        widthInPx to 1,
    )

    LaunchedEffect(key1 = currentState) {
        if (currentState) {
            swipeableState.snapTo(0)
        }
    }

    LaunchedEffect(
        key1 = swipeableState.currentValue,
    ) {
        if (swipeableState.currentValue == 1) {
            onSwipeFinished()
        }
    }

    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(Color.Yellow)
            .animateContentSize()
            .then(
                if (currentState) {
                    Modifier.width(60.dp)
                } else {
                    Modifier.fillMaxWidth()
                }
            )
            .requiredHeight(60.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        if (!currentState) {
            SwipeIndicator(
                modifier = Modifier
                    .offset {
                        IntOffset(swipeableState.offset.value.roundToInt(), 0)
                    }
                    .swipeable(
                        state = swipeableState,
                        anchors = anchors,
                        thresholds = { _, _ ->
                            FractionalThreshold(0.7F)
                        },
                        orientation = Orientation.Horizontal,
                    ),
            )
            Text(text = "SWIPE To Go On Air", color = Color.Black, textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .offset {
                        IntOffset(swipeableState.offset.value.roundToInt(), 0)
                    }
            )
        }
        AnimatedVisibility(
            modifier = Modifier.fillMaxSize(),
            visible = currentState,
        ) {
            IconButton(onClick = { onReset() }) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier
                        .size(36.dp)
                        .align(
                            Alignment.Center
                        )
                )
            }
        }
    }
}

@Composable
fun SwipeIndicator(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(Color.White)
            .width(100.dp)
            .height(60.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.ArrowForward,
            contentDescription = null,
            tint = Color.Black,
            modifier = Modifier.size(36.dp),
        )
    }
}
