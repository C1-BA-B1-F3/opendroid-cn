// Modified by opendroid-cn (Chinese localization fork): UI strings routed through i18n.tr(). See NOTICE.
package com.opendroid.ai.ui.screens



import com.opendroid.ai.i18n.tr

import com.opendroid.ai.i18n.AppText

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.opendroid.ai.data.models.HabitRoutine
import com.opendroid.ai.data.models.PlanStep
import com.opendroid.ai.data.models.RoutineStatus
import com.opendroid.ai.ui.theme.*
import com.opendroid.ai.ui.viewmodel.RoutineViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutinesScreen(
    viewModel: RoutineViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val suggestedRoutines by viewModel.suggestedRoutines.collectAsState()
    val activeRoutines by viewModel.activeRoutines.collectAsState()
    val recentEvents by viewModel.recentEvents.collectAsState()

    var isExecuting by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    AppText(
                        text = tr("HABITS & ROUTINES"),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.textPrimary,
                        fontSize = 18.sp,
                        letterSpacing = 1.5.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = tr("Back"),
                            tint = AppTheme.colors.textPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.triggerDetection()
                        Toast.makeText(context, tr("Scanning habit patterns..."), Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = tr("Scan Habits"),
                            tint = AppTheme.colors.textPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppTheme.colors.background)
            )
        },
        containerColor = AppTheme.colors.background,
        modifier = modifier
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // ── 1. SUGGESTED ROUTINES SECTION (AI DISCOVERED) ──────────
            if (suggestedRoutines.isNotEmpty()) {
                item {
                    AppText(
                        text = tr("DISCOVERED HABITS & SUGGESTIONS"),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.accentCyan,
                        fontSize = 13.sp,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(suggestedRoutines, key = { it.id }) { routine ->
                    SuggestedRoutineCard(
                        routine = routine,
                        onApprove = {
                            viewModel.approveRoutine(routine.id)
                            Toast.makeText(context, "例程“${routine.name}”已自动化！", Toast.LENGTH_SHORT).show()
                        },
                        onDismiss = {
                            viewModel.dismissRoutine(routine.id)
                            Toast.makeText(context, tr("Suggestion dismissed"), Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }

            // ── 2. AUTOMATED ACTIVE ROUTINES ───────────────────────────
            item {
                AppText(
                    text = "已自动化例程（${activeRoutines.size}）",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.accentCyan,
                    fontSize = 13.sp,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (activeRoutines.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, AppTheme.colors.borderColor, RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.cardBackground)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = AppTheme.colors.textSecondary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            AppText(
                                text = tr("No Active Routines Yet"),
                                color = AppTheme.colors.textPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            AppText(
                                text = tr("As you use apps like Gmail, Calendar, and Slack, OpenDroid detects repeated patterns and suggests automations here."),
                                color = AppTheme.colors.textSecondary,
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            } else {
                items(activeRoutines, key = { it.id }) { routine ->
                    ActiveRoutineCard(
                        routine = routine,
                        isExecuting = isExecuting == routine.id,
                        onToggle = { isEnabled ->
                            viewModel.toggleRoutine(routine.id, isEnabled)
                        },
                        onExecute = {
                            isExecuting = routine.id
                            viewModel.executeRoutine(routine.id, context) { success, msg ->
                                isExecuting = null
                                Toast.makeText(
                                    context,
                                    if (success) "例程已完成：$msg" else "执行失败：$msg",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        onDelete = {
                            viewModel.deleteRoutine(routine.id)
                        }
                    )
                }
            }

            // ── 3. PRE-BUILT TEMPLATES ──────────────────────────────────
            item {
                AppText(
                    text = tr("SAMPLE ROUTINE TEMPLATES"),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.textSecondary,
                    fontSize = 13.sp,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                RoutineTemplateCard(
                    title = tr("🌅 Morning Routine"),
                    description = tr("Read calendar → Summarize today's meetings → Check notifications → Task list → Morning briefing"),
                    trigger = "Every weekday at 9:00 AM",
                    onActivate = {
                        viewModel.triggerDetection()
                        Toast.makeText(context, tr("Morning Routine template activated!"), Toast.LENGTH_SHORT).show()
                    }
                )
            }

            item {
                RoutineTemplateCard(
                    title = tr("💼 Work Focus Routine"),
                    description = tr("Open Slack → Check Calendar → Read important notifications"),
                    trigger = "Every weekday at 9:30 AM",
                    onActivate = {
                        viewModel.triggerDetection()
                        Toast.makeText(context, tr("Work Focus template activated!"), Toast.LENGTH_SHORT).show()
                    }
                )
            }

            item {
                RoutineTemplateCard(
                    title = tr("🌙 Evening Wrap-up"),
                    description = tr("Check tomorrow's calendar → Check unread notifications → Daily summary"),
                    trigger = "Daily at 9:00 PM",
                    onActivate = {
                        viewModel.triggerDetection()
                        Toast.makeText(context, tr("Evening Wrap-up template activated!"), Toast.LENGTH_SHORT).show()
                    }
                )
            }

            // ── 4. HABIT LEARNING ANALYTICS ────────────────────────────
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, AppTheme.colors.borderColor, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = AppTheme.colors.cardBackground)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Analytics,
                                contentDescription = null,
                                tint = AppTheme.colors.accentPurple,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            AppText(
                                text = tr("HABIT LEARNING ENGINE"),
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = AppTheme.colors.textPrimary,
                                fontSize = 13.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        AppText(
                            text = "已记录事件：记录了 ${recentEvents.size} 条近期活动。\nOpenDroid 会在本机安全分析应用切换情况以学习你的日常例程，不会将数据传输到云端。",
                            color = AppTheme.colors.textSecondary,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SuggestedRoutineCard(
    routine: HabitRoutine,
    onApprove: () -> Unit,
    onDismiss: () -> Unit
) {
    var expanded by remember { mutableStateOf(true) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, AppTheme.colors.borderColor, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.cardBackground)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    color = AppTheme.colors.accentCyan.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    AppText(
                        text = tr("💡 ROUTINE DETECTED"),
                        color = AppTheme.colors.accentCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Surface(
                    color = AppTheme.colors.surface,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    AppText(
                        text = "匹配度 ${(routine.confidence * 100).toInt()}%",
                        color = AppTheme.colors.textSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            AppText(
                text = routine.suggestionMessage.ifBlank { "I noticed you usually do these tasks. Would you like me to automate them?" },
                color = AppTheme.colors.textPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            AppText(
                text = "⚡ ${routine.triggerLabel}",
                color = AppTheme.colors.accentCyan,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace
            )

            if (routine.detectedActions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    routine.detectedActions.forEach { action ->
                        Surface(
                            color = AppTheme.colors.surface,
                            shape = RoundedCornerShape(4.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.borderColor)
                        ) {
                            AppText(
                                text = action,
                                color = AppTheme.colors.textSecondary,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Expandable suggested steps preview
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { expanded = !expanded }
                    .padding(vertical = 4.dp)
            ) {
                AppText(
                    text = "建议的自动化（${routine.suggestedSteps.size} 个步骤）",
                    color = AppTheme.colors.accentPurple,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = AppTheme.colors.accentPurple,
                    modifier = Modifier.size(16.dp)
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 6.dp)
                ) {
                    routine.suggestedSteps.forEachIndexed { idx, step ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AppText(
                                text = "${idx + 1}.",
                                color = AppTheme.colors.accentCyan,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(20.dp)
                            )
                            AppText(
                                text = step.description,
                                color = AppTheme.colors.textPrimary,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onApprove,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppTheme.colors.textPrimary,
                        contentColor = AppTheme.colors.background
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = AppTheme.colors.background,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    AppText(
                        text = tr("Approve & Automate"),
                        color = AppTheme.colors.background,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.borderColor)
                ) {
                    AppText(
                        text = tr("Dismiss"),
                        color = AppTheme.colors.textSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ActiveRoutineCard(
    routine: HabitRoutine,
    isExecuting: Boolean,
    onToggle: (Boolean) -> Unit,
    onExecute: () -> Unit,
    onDelete: () -> Unit
) {
    val isEnabled = routine.status == RoutineStatus.ACTIVE || routine.status == RoutineStatus.APPROVED

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, if (isEnabled) AppTheme.colors.accentCyan else AppTheme.colors.borderColor, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.cardBackground)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    AppText(
                        text = routine.name,
                        color = AppTheme.colors.textPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    AppText(
                        text = "⚡ ${routine.triggerLabel}",
                        color = AppTheme.colors.accentCyan,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Switch(
                    checked = isEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = AppTheme.colors.textPrimary,
                        checkedTrackColor = AppTheme.colors.textPrimary.copy(alpha = 0.5f),
                        uncheckedThumbColor = AppTheme.colors.textSecondary,
                        uncheckedTrackColor = AppTheme.colors.surface
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            AppText(
                text = routine.description,
                color = AppTheme.colors.textSecondary,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onExecute,
                    enabled = !isExecuting,
                    colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.surface),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.accentCyan),
                    modifier = Modifier.weight(1f)
                ) {
                    if (isExecuting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            color = AppTheme.colors.accentCyan,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        AppText(tr("Running..."), color = AppTheme.colors.accentCyan, fontSize = 12.sp)
                    } else {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = AppTheme.colors.accentCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        AppText(
                            text = tr("Run Routine Now"),
                            color = AppTheme.colors.accentCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = tr("Delete"),
                        tint = AppTheme.colors.accentRed
                    )
                }
            }
        }
    }
}

@Composable
fun RoutineTemplateCard(
    title: String,
    description: String,
    trigger: String,
    onActivate: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, AppTheme.colors.borderColor, RoundedCornerShape(10.dp)),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.cardBackground)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(14.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                AppText(
                    text = title,
                    color = AppTheme.colors.textPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                AppText(
                    text = description,
                    color = AppTheme.colors.textSecondary,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                AppText(
                    text = "⏰ $trigger",
                    color = AppTheme.colors.accentCyan,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(onClick = onActivate) {
                Icon(
                    imageVector = Icons.Default.AddCircleOutline,
                    contentDescription = tr("Activate"),
                    tint = AppTheme.colors.accentCyan
                )
            }
        }
    }
}
