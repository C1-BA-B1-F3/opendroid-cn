// Modified by opendroid-cn (Chinese localization fork): UI strings routed through i18n.tr(). See NOTICE.
package com.opendroid.ai.ui.screens



import com.opendroid.ai.i18n.tr

import com.opendroid.ai.i18n.AppText

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.opendroid.ai.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicenseScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    AppText(
                        text = tr("LICENSE"),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 20.sp,
                        letterSpacing = 2.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = tr("Back"),
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Header card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, AccentPurple.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = CardBackground)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = tr("License"),
                            tint = AccentPurple,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            AppText(
                                text = tr("Open Source License"),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            AppText(
                                text = tr("Apache License 2.0"),
                                fontSize = 12.sp,
                                color = AccentPurple
                            )
                        }
                    }
                }
            }

            item {
                PolicySection(
                    title = tr("APACHE LICENSE 2.0"),
                    content = "Copyright (c) 2026 OpenDroid Contributors\n" +
                            tr("Last Updated: August 18, 2026\n\n") +
                            tr("Licensed under the Apache License, Version 2.0 (the \"License\"); ") +
                            tr("you may not use this file except in compliance with the License.\n") +
                            tr("You may obtain a copy of the License at:\n\n") +
                            tr("    http://www.apache.org/licenses/LICENSE-2.0\n\n") +
                            tr("Unless required by applicable law or agreed to in writing, software ") +
                            tr("distributed under the License is distributed on an \"AS IS\" BASIS, ") +
                            tr("WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. ") +
                            tr("See the License for the specific language governing permissions and ") +
                            tr("limitations under the License.")
                )
            }

            item {
                PolicySection(
                    title = tr("THIRD-PARTY LICENSES"),
                    content = "OpenDroid uses the following open-source libraries:\n\n" +
                            tr("• Jetpack Compose — Apache License 2.0\n") +
                            tr("• Dagger/Hilt — Apache License 2.0\n") +
                            tr("• Room Database — Apache License 2.0\n") +
                            tr("• OkHttp & Retrofit — Apache License 2.0\n") +
                            tr("• Kotlin Serialization — Apache License 2.0\n") +
                            tr("• Coil Image Loading — Apache License 2.0\n") +
                            tr("• Lottie Animations — Apache License 2.0\n") +
                            tr("• DataStore Preferences — Apache License 2.0")
                )
            }

            item {
                PolicySection(
                    title = tr("CONTRIBUTION"),
                    content = "OpenDroid is a community-driven project. By contributing code, documentation, or other materials, " +
                            tr("you agree that your contributions will be licensed under the same MIT License.\n\n") +
                            tr("We welcome contributions of all kinds:\n\n") +
                            tr("• Bug reports and feature requests\n") +
                            tr("• Code contributions via pull requests\n") +
                            tr("• Documentation improvements\n") +
                            tr("• Translation and localization\n\n") +
                            tr("Please refer to CONTRIBUTING.md in the repository for contribution guidelines.")
                )
            }

            item {
                PolicySection(
                    title = tr("ATTRIBUTION"),
                    content = "OpenDroid is built with ❤\uFE0F by the open-source community.\n\n" +
                            tr("Special thanks to all contributors who have helped make this project possible. ") +
                            tr("Full contributor list is available on the GitHub repository.")
                )
            }
        }
    }
}
