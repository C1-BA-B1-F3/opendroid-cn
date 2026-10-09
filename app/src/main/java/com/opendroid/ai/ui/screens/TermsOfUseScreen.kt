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
import androidx.compose.material.icons.filled.Description
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
fun TermsOfUseScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    AppText(
                        text = tr("TERMS OF USE"),
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
                        .border(1.dp, BorderColor, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = CardBackground)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = tr("Terms"),
                            tint = AccentCyan,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            AppText(
                                text = tr("Terms of Use"),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            AppText(
                                text = tr("Effective: May 2026"),
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            item {
                PolicySection(
                    title = tr("1. ACCEPTANCE OF TERMS"),
                    content = "By downloading, installing, or using OpenDroid (\"the App\"), you agree to be bound by these Terms of Use. " +
                            tr("If you do not agree to these terms, do not use the App.\n\n") +
                            tr("OpenDroid is an open-source, autonomous AI assistant for Android. These terms govern your use of the App and all related services.")
                )
            }

            item {
                PolicySection(
                    title = tr("2. PERMITTED USE"),
                    content = "You may use OpenDroid for personal, non-commercial purposes including:\n\n" +
                            tr("• Automating device tasks (messaging, calls, alarms, etc.)\n") +
                            tr("• Managing smart home devices\n") +
                            tr("• Searching the web and retrieving information\n") +
                            tr("• File management and device control\n") +
                            tr("• Voice-activated commands\n\n") +
                            tr("You agree NOT to use OpenDroid to:\n\n") +
                            tr("• Violate any laws or regulations\n") +
                            tr("• Harass, spam, or harm other individuals\n") +
                            tr("• Attempt to bypass device security or access unauthorized systems\n") +
                            tr("• Interfere with other applications in a harmful manner")
                )
            }

            item {
                PolicySection(
                    title = tr("3. API KEYS & THIRD-PARTY SERVICES"),
                    content = "OpenDroid connects to third-party LLM providers (Google Gemini, OpenAI, Anthropic, etc.) using API keys you provide.\n\n" +
                            tr("• You are responsible for obtaining and managing your own API keys.\n") +
                            tr("• API key usage is subject to the respective provider's terms of service.\n") +
                            tr("• OpenDroid is not responsible for charges incurred through third-party API usage.\n") +
                            tr("• Your API keys are stored locally on your device using AES-256 encryption and are never transmitted to OpenDroid servers.")
                )
            }

            item {
                PolicySection(
                    title = tr("4. ACCESSIBILITY SERVICE"),
                    content = "OpenDroid uses Android's Accessibility Service to perform on-screen automations on your behalf. " +
                            tr("By enabling this service, you acknowledge that:\n\n") +
                            tr("• The service can interact with other apps on your device\n") +
                            tr("• It only acts when you explicitly give a command\n") +
                            tr("• You can disable it at any time from Android Settings\n") +
                            tr("• OpenDroid does not use this service to collect or transmit data")
                )
            }

            item {
                PolicySection(
                    title = tr("5. DISCLAIMER OF WARRANTIES"),
                    content = "OpenDroid is provided \"AS IS\" without warranties of any kind, either express or implied.\n\n" +
                            tr("• We do not guarantee uninterrupted or error-free operation.\n") +
                            tr("• AI-generated responses may be inaccurate or incomplete.\n") +
                            tr("• Automated actions may not execute as intended in all scenarios.\n") +
                            tr("• You assume all risks associated with using the App.")
                )
            }

            item {
                PolicySection(
                    title = tr("6. LIMITATION OF LIABILITY"),
                    content = "To the maximum extent permitted by law, the OpenDroid developers shall not be liable for any " +
                            tr("direct, indirect, incidental, special, or consequential damages arising from:\n\n") +
                            tr("• Use or inability to use the App\n") +
                            tr("• Unauthorized access to your data\n") +
                            tr("• Actions performed by the AI assistant\n") +
                            tr("• Third-party service failures or charges")
                )
            }

            item {
                PolicySection(
                    title = tr("7. OPEN SOURCE"),
                    content = "OpenDroid is open-source software. You are free to view, modify, and distribute the source code " +
                            tr("in accordance with the project's license terms. Contributions to the project are welcome and governed by the project's contribution guidelines.")
                )
            }

            item {
                PolicySection(
                    title = tr("8. CHANGES TO TERMS"),
                    content = "We may update these terms from time to time. Changes will be reflected in the App with an updated effective date. " +
                            tr("Continued use of OpenDroid after changes constitutes acceptance of the updated terms.")
                )
            }

            item {
                PolicySection(
                    title = tr("9. CONTACT"),
                    content = "For questions about these Terms of Use, please open an issue on our GitHub repository (yashab-cyber/opendroid) or contact the development team.\n\n" +
                            tr("• Email: opendroid.ai@gmail.com\n") +
                            tr("• Email: yashabalam707@gmail.com")
                )
            }
        }
    }
}
