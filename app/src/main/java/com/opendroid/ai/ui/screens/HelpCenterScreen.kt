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
import androidx.compose.material.icons.filled.HelpOutline
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
fun HelpCenterScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    AppText(
                        text = tr("HELP CENTER"),
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
                        .border(1.dp, AccentCyan.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = CardBackground)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = tr("Help"),
                            tint = AccentCyan,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            AppText(
                                text = tr("How can we help?"),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            AppText(
                                text = tr("Quick answers to common questions"),
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            item {
                PolicySection(
                    title = tr("GETTING STARTED"),
                    content = "1. Launch OpenDroid and complete the onboarding setup\n" +
                            tr("2. Grant the requested permissions (microphone, accessibility, etc.)\n") +
                            tr("3. Go to Settings and enter your LLM provider API key\n") +
                            tr("4. Start talking or typing commands!\n\n") +
                            tr("Tip: Google Gemini is the default provider. Get a free API key at ai.google.dev")
                )
            }

            item {
                PolicySection(
                    title = tr("VOICE COMMANDS"),
                    content = "OpenDroid listens for the wake word \"Hey OpenDroid\" when the app is running.\n\n" +
                            tr("Examples of what you can say:\n\n") +
                            tr("• \"Send a WhatsApp message to Mom saying I'll be late\"\n") +
                            tr("• \"Set an alarm for 7 AM tomorrow\"\n") +
                            tr("• \"Turn on the flashlight\"\n") +
                            tr("• \"What's the weather like today?\"\n") +
                            tr("• \"Play some music on Spotify\"\n") +
                            tr("• \"Call John\"\n") +
                            tr("• \"Take a photo\"")
                )
            }

            item {
                PolicySection(
                    title = tr("SETTING UP API KEYS"),
                    content = "OpenDroid needs an LLM API key to generate responses:\n\n" +
                            tr("1. Go to Settings → Provider API Keys\n") +
                            tr("2. Enter your API key for the provider you want to use\n") +
                            tr("3. Select that provider from the \"Active Brain Provider\" dropdown\n\n") +
                            tr("Supported providers:\n") +
                            tr("• Google Gemini (recommended for beginners)\n") +
                            tr("• OpenAI (GPT-4, GPT-3.5)\n") +
                            tr("• Anthropic Claude\n") +
                            tr("• Groq (fast inference)\n") +
                            tr("• Mistral AI, OpenRouter, Together AI, Cohere, DeepSeek\n") +
                            tr("• Ollama (fully local, no API key needed)")
                )
            }

            item {
                PolicySection(
                    title = tr("ACCESSIBILITY SERVICE"),
                    content = "The Accessibility Service lets OpenDroid tap buttons and type in other apps (e.g., sending WhatsApp messages automatically).\n\n" +
                            tr("To enable it:\n") +
                            tr("1. Go to Android Settings → Accessibility\n") +
                            tr("2. Find \"OpenDroid\" in Installed Services\n") +
                            tr("3. Toggle it ON\n\n") +
                            tr("Note: This is optional. Without it, OpenDroid will still open apps but may need you to tap the final \"Send\" button.")
                )
            }

            item {
                PolicySection(
                    title = tr("MACROS & AUTOMATION"),
                    content = "You can create macros to run multiple actions in sequence:\n\n" +
                            tr("• Go to the Macros tab\n") +
                            tr("• Create a new macro with a name and list of steps\n") +
                            tr("• Schedule macros with cron expressions for timed automation\n\n") +
                            tr("Example: Create a \"Good Morning\" macro that turns on lights, reads the weather, and plays your favorite playlist.")
                )
            }

            item {
                PolicySection(
                    title = tr("MEMORY SYSTEM"),
                    content = "OpenDroid has 4 types of memory:\n\n" +
                            tr("• Working Memory — Current session context (auto-cleared)\n") +
                            tr("• Episodic Memory — Conversation history\n") +
                            tr("• Semantic Memory — Facts about you (name, preferences)\n") +
                            tr("• Procedural Memory — Learned task patterns\n\n") +
                            tr("You can view and clear any memory type from the Memory tab.")
                )
            }

            item {
                PolicySection(
                    title = tr("TROUBLESHOOTING"),
                    content = "\"OpenDroid isn't responding\"\n" +
                            tr("→ Check that your API key is valid and the provider is reachable.\n\n") +
                            tr("\"Voice commands don't work\"\n") +
                            tr("→ Make sure microphone permission is granted and the service is running.\n\n") +
                            tr("\"WhatsApp messages aren't sending automatically\"\n") +
                            tr("→ Enable the Accessibility Service in Android Settings.\n\n") +
                            tr("\"App crashes on startup\"\n") +
                            tr("→ Clear app data and re-enter your settings. Your API keys are encrypted and will need to be re-entered.")
                )
            }

            item {
                PolicySection(
                    title = tr("CONTACT & SUPPORT"),
                    content = "• GitHub: Report bugs and request features at github.com/yashab-cyber/opendroid\n" +
                            tr("• Discord: Join our community for live help and discussion\n") +
                            tr("• Email: opendroid.ai@gmail.com / yashabalam707@gmail.com\n\n") +
                            tr("OpenDroid is open-source and community-driven. We welcome contributions!")
                )
            }
        }
    }
}
