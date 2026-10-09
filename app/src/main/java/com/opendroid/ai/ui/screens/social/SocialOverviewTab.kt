// Modified by opendroid-cn (Chinese localization fork): UI strings routed through i18n.tr(). See NOTICE.
package com.opendroid.ai.ui.screens.social



import com.opendroid.ai.i18n.tr

import com.opendroid.ai.i18n.AppText

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.opendroid.ai.social.core.analytics.AnalyticsSummary
import com.opendroid.ai.social.domain.model.*
import com.opendroid.ai.ui.theme.AppTheme

@Composable
fun SocialOverviewTab(
    summary: AnalyticsSummary?,
    insights: List<SocialInsight>,
    posts: List<SocialPost>,
    interactions: List<SocialInteraction>,
    onNavigateToTab: (Int) -> Unit,
    onApprovePost: (String) -> Unit
) {
    val theme = AppTheme.colors
    val scheduledPosts = posts.filter { it.status == PostStatus.SCHEDULED || (it.status == PostStatus.DRAFT && it.scheduledPublishTime != null) }
    val topPosts = posts.filter { it.status == PostStatus.PUBLISHED }.sortedByDescending { it.reach }.take(3)

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── 1. STATS SUMMARY ROW ─────────────────────────────────────
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricCard(
                    title = tr("Followers"),
                    value = summary?.totalFollowers?.toString() ?: "0",
                    delta = summary?.followersGrowthDelta?.let { if (it >= 0) "+$it" else "$it" } ?: "+0",
                    icon = Icons.Default.People,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = tr("Total Reach"),
                    value = summary?.totalReach?.let { formatCount(it) } ?: "0",
                    delta = summary?.reachGrowthDelta?.let { if (it >= 0) "+$it" else "$it" } ?: "+0",
                    icon = Icons.Default.Visibility,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricCard(
                    title = tr("Engagement"),
                    value = summary?.averageEngagementRate?.let { "${String.format("%.1f", it)}%" } ?: "0.0%",
                    delta = "Active",
                    icon = Icons.Default.TrendingUp,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = tr("Scheduled"),
                    value = scheduledPosts.size.toString(),
                    delta = "Queue",
                    icon = Icons.Default.Schedule,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // ── 2. AI SOCIAL INSIGHTS CARD ───────────────────────────────
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
                    .border(1.dp, theme.accentCyan.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = theme.cardBackground)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = theme.accentCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            AppText(
                                text = tr("AI SOCIAL INSIGHTS"),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.accentCyan,
                                letterSpacing = 1.sp
                            )
                        }
                        AppText(
                            text = tr("Grounded Analysis"),
                            fontSize = 10.sp,
                            color = theme.textSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val topInsight = insights.firstOrNull()
                    if (topInsight != null) {
                        AppText(
                            text = "💡 ${topInsight.calculatedInsight}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = theme.textPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        AppText(
                            text = "观测数据：${topInsight.observedData}",
                            fontSize = 12.sp,
                            color = theme.textSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier.fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(theme.surface)
                                .padding(10.dp)
                        ) {
                            AppText(
                                text = "👉 建议：${topInsight.aiRecommendation}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = theme.textPrimary
                            )
                        }
                    } else {
                        AppText(
                            text = tr("Analyzing historical records... Connect accounts to generate tailored strategies."),
                            fontSize = 12.sp,
                            color = theme.textSecondary
                        )
                    }
                }
            }
        }

        // ── 3. TODAY'S ACTIVITY / INBOX SNAPSHOT ─────────────────────
        item {
            SectionHeader(
                title = tr("Recent Activity & Inbox"),
                actionLabel = tr("View All"),
                onAction = { onNavigateToTab(2) } // Inbox tab index
            )
        }

        if (interactions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().border(1.dp, theme.borderColor, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = theme.cardBackground)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        AppText(tr("No recent social activity recorded."), color = theme.textSecondary, fontSize = 13.sp)
                    }
                }
            }
        } else {
            items(interactions.take(3)) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth().border(1.dp, theme.borderColor, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = theme.cardBackground)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(theme.surface),
                            contentAlignment = Alignment.Center
                        ) {
                            AppText(item.platform.displayName.take(1), fontWeight = FontWeight.Bold, color = theme.textPrimary)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AppText(item.authorName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = theme.textPrimary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(theme.surface).padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    AppText(item.category.name, fontSize = 9.sp, color = theme.accentCyan)
                                }
                            }
                            AppText(item.content, maxLines = 1, fontSize = 12.sp, color = theme.textSecondary)
                        }
                    }
                }
            }
        }

        // ── 4. UPCOMING SCHEDULED POSTS ──────────────────────────────
        item {
            SectionHeader(
                title = tr("Upcoming Content"),
                actionLabel = tr("Calendar"),
                onAction = { onNavigateToTab(4) } // Calendar tab
            )
        }

        if (scheduledPosts.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().border(1.dp, theme.borderColor, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = theme.cardBackground)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                        AppText(tr("No upcoming scheduled posts. Create one in Content Composer!"), color = theme.textSecondary, fontSize = 12.sp)
                    }
                }
            }
        } else {
            items(scheduledPosts.take(2)) { post ->
                Card(
                    modifier = Modifier.fillMaxWidth().border(1.dp, theme.borderColor, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = theme.cardBackground)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AppText(post.platform.displayName, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = theme.accentCyan)
                            AppText(if (post.requiresApproval) "NEEDS APPROVAL" else "SCHEDULED", fontSize = 10.sp, color = theme.textSecondary)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        AppText(post.content, fontSize = 13.sp, color = theme.textPrimary, maxLines = 2)
                        if (post.requiresApproval) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { onApprovePost(post.id) },
                                modifier = Modifier.fillMaxWidth().height(36.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = theme.textPrimary, contentColor = theme.background),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                AppText(tr("Approve Post"), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // ── 5. TOP CONTENT ───────────────────────────────────────────
        if (topPosts.isNotEmpty()) {
            item {
                SectionHeader(
                    title = tr("Top Performing Posts"),
                    actionLabel = tr("Analytics"),
                    onAction = { onNavigateToTab(7) }
                )
            }

            items(topPosts) { post ->
                Card(
                    modifier = Modifier.fillMaxWidth().border(1.dp, theme.borderColor, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = theme.cardBackground)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            AppText(post.platform.displayName, fontSize = 11.sp, color = theme.accentCyan, fontWeight = FontWeight.SemiBold)
                            AppText(post.content, fontSize = 12.sp, color = theme.textPrimary, maxLines = 1)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            AppText("触达 ${post.reach}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = theme.textPrimary)
                            AppText("${String.format("%.1f", post.engagementRate)}% engage", fontSize = 10.sp, color = theme.textSecondary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    delta: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    val theme = AppTheme.colors
    Card(
        modifier = modifier.border(1.dp, theme.borderColor, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = theme.cardBackground)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppText(text = title, fontSize = 11.sp, color = theme.textSecondary, fontWeight = FontWeight.Medium)
                Icon(imageVector = icon, contentDescription = null, tint = theme.textSecondary, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            AppText(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = theme.textPrimary)
            Spacer(modifier = Modifier.height(4.dp))
            AppText(text = delta, fontSize = 11.sp, color = theme.accentCyan, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    val theme = AppTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppText(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = theme.textPrimary
        )
        if (actionLabel != null && onAction != null) {
            TextButton(onClick = onAction) {
                AppText(actionLabel, fontSize = 12.sp, color = theme.accentCyan, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

private fun formatCount(count: Int): String {
    return when {
        count >= 1_000_000 -> "${String.format("%.1f", count / 1_000_000f)}M"
        count >= 1_000 -> "${String.format("%.1f", count / 1_000f)}K"
        else -> count.toString()
    }
}
