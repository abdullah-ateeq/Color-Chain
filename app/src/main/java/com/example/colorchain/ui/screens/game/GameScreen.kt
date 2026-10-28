package com.example.colorchain.ui.screens.game

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.colorchain.data.model.ColorChainLevel
import com.example.colorchain.data.model.ColorTile
import com.example.colorchain.data.model.GameColor
import com.example.colorchain.data.model.GameState
import com.example.colorchain.data.model.Milestone
import com.example.colorchain.ui.components.ChainConnectionOverlay
import com.example.colorchain.ui.components.ColorChainTile
import com.example.colorchain.ui.components.ConfirmDialog
import com.example.colorchain.ui.components.ParticleBurstCanvas
import com.example.colorchain.ui.components.PauseDialog
import com.example.colorchain.ui.components.TargetColorSwatch
import com.example.colorchain.ui.theme.ChainError
import com.example.colorchain.ui.theme.ChainPrimary
import com.example.colorchain.ui.theme.ChainSuccess
import com.example.colorchain.ui.theme.ChainWarning
import com.example.colorchain.ui.viewmodel.ConnectionAnimationData
import com.example.colorchain.ui.viewmodel.TapFeedbackData
import java.util.Locale

@Composable
fun GameScreen(
    gameState: GameState,
    targetColor: GameColor,
    tiles: List<ColorTile>,
    levelConfig: ColorChainLevel,
    currentLevel: Int,
    chain: Int,
    score: Int,
    remainingTimeMs: Long,
    levelMatches: Int,
    colorAssistEnabled: Boolean,
    activeMilestone: Milestone?,
    levelCompletedEvent: Int?,
    lastTapFeedback: TapFeedbackData?,
    connectionAnimation: ConnectionAnimationData?,
    onTileTapped: (ColorTile) -> Unit,
    onPauseClicked: () -> Unit,
    onResumeClicked: () -> Unit,
    onRestartConfirmed: () -> Unit,
    onExitConfirmed: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showRestartDialog by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }

    // Intercept back button during gameplay to pause
    BackHandler {
        if (gameState == GameState.PLAYING) {
            onPauseClicked()
        }
    }

    var targetCenterOffset by remember { mutableStateOf<Offset?>(null) }
    val tileCenters = remember { mutableMapOf<Int, Offset>() }

    val remainingSeconds = remainingTimeMs / 1000.0
    val isTimeWarning = remainingTimeMs in 1L..3000L

    val infiniteTransition = rememberInfiniteTransition(label = "timerWarningAnim")
    val warningScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isTimeWarning) 1.15f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(250, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "timerPulse"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // TOP BAR: CHAIN, TIMER, PAUSE
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // CHAIN & LEVEL
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "CHAIN",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$chain",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = ChainPrimary
                        )
                    }
                    Text(
                        text = "LV $currentLevel • SCORE $score",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }

                // TIMER
                Box(
                    modifier = Modifier
                        .scale(warningScale)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isTimeWarning) ChainError.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .border(
                            width = if (isTimeWarning) 2.dp else 1.dp,
                            color = if (isTimeWarning) ChainError else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = String.format(Locale.ROOT, "%.1fs", remainingSeconds.coerceAtLeast(0.0)),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = if (isTimeWarning) ChainError else MaterialTheme.colorScheme.onSurface,
                        letterSpacing = 1.sp
                    )
                }

                // PAUSE BUTTON
                IconButton(
                    onClick = onPauseClicked,
                    enabled = gameState == GameState.PLAYING
                ) {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = "Pause",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // LEVEL PROGRESS BAR
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
                val progress = (levelMatches.toFloat() / levelConfig.requiredChain.toFloat()).coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = ChainSuccess,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            // TARGET COLOR AREA (Large swatch, dominant)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .onGloballyPositioned { coordinates ->
                        val bounds = coordinates.boundsInRoot()
                        targetCenterOffset = Offset(
                            bounds.left + bounds.width / 2f,
                            bounds.top + bounds.height / 2f
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                TargetColorSwatch(
                    targetColor = targetColor,
                    swatchSize = 78.dp,
                    showLabel = true,
                    showSymbol = colorAssistEnabled,
                    isUrgent = isTimeWarning
                )
            }

            // DYNAMIC TILE GRID
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                val rows = levelConfig.gridRows
                val cols = levelConfig.gridColumns
                val availableWidth = maxWidth
                val availableHeight = maxHeight
                val boardSize = minOf(availableWidth, availableHeight)

                Box(
                    modifier = Modifier
                        .size(boardSize)
                        .aspectRatio(1f)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (r in 0 until rows) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                for (c in 0 until cols) {
                                    val index = r * cols + c
                                    if (index < tiles.size) {
                                        val tile = tiles[index]
                                        val isWrong = lastTapFeedback?.tileId == tile.id && !lastTapFeedback.isCorrect
                                        val isCorrect = lastTapFeedback?.tileId == tile.id && lastTapFeedback.isCorrect

                                        ColorChainTile(
                                            tile = tile,
                                            onClick = { onTileTapped(tile) },
                                            enabled = gameState == GameState.PLAYING,
                                            isWrongTapped = isWrong,
                                            isCorrectTapped = isCorrect,
                                            showSymbol = colorAssistEnabled,
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(1f)
                                                .onGloballyPositioned { coordinates ->
                                                    val bounds = coordinates.boundsInRoot()
                                                    tileCenters[tile.id] = Offset(
                                                        bounds.left + bounds.width / 2f,
                                                        bounds.top + bounds.height / 2f
                                                    )
                                                }
                                        )
                                    } else {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        // CHAIN CONNECTION EFFECT OVERLAY
        val tappedTileCenter = connectionAnimation?.tileIndex?.let { tileCenters[it] }
        ChainConnectionOverlay(
            animationData = connectionAnimation,
            tileCenter = tappedTileCenter,
            targetCenter = targetCenterOffset
        )

        // MILESTONE BANNER
        AnimatedVisibility(
            visible = activeMilestone != null,
            enter = fadeIn(tween(150)) + scaleIn(tween(150)),
            exit = fadeOut(tween(200)) + scaleOut(tween(200)),
            modifier = Modifier.align(Alignment.Center)
        ) {
            if (activeMilestone != null) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 14.dp),
                    modifier = Modifier.padding(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 32.dp, vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = activeMilestone.title,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = ChainPrimary
                        )
                        if (activeMilestone.bonusPoints > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "+${activeMilestone.bonusPoints} BONUS",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ChainSuccess
                            )
                        }
                    }
                }
            }
        }

        // LEVEL COMPLETE CELEBRATION
        AnimatedVisibility(
            visible = levelCompletedEvent != null,
            enter = fadeIn(tween(150)) + scaleIn(tween(150)),
            exit = fadeOut(tween(200)) + scaleOut(tween(200)),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
                modifier = Modifier.padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 32.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "LEVEL $levelCompletedEvent COMPLETE!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = ChainSuccess
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "+500 BONUS",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ChainPrimary
                    )
                }
            }
        }

        // Lightweight particles for celebrations
        ParticleBurstCanvas(
            triggerKey = levelCompletedEvent ?: activeMilestone?.chainThreshold
        )

        // PAUSE DIALOG
        if (gameState == GameState.PAUSED) {
            PauseDialog(
                onResume = onResumeClicked,
                onRestartRequest = { showRestartDialog = true },
                onExitRequest = { showExitDialog = true }
            )
        }

        // CONFIRM RESTART
        if (showRestartDialog) {
            ConfirmDialog(
                title = "Restart this game?",
                message = "Current progress and chain will be lost.",
                confirmText = "RESTART",
                isDestructive = true,
                onConfirm = {
                    showRestartDialog = false
                    onRestartConfirmed()
                },
                onCancel = { showRestartDialog = false }
            )
        }

        // CONFIRM EXIT
        if (showExitDialog) {
            ConfirmDialog(
                title = "Exit game?",
                message = "Current progress will be lost.",
                confirmText = "EXIT",
                isDestructive = true,
                onConfirm = {
                    showExitDialog = false
                    onExitConfirmed()
                },
                onCancel = { showExitDialog = false }
            )
        }
    }
}
