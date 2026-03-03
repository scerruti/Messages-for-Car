package com.otabi.messagesforcar.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import android.content.Intent
import android.net.Uri
import com.otabi.messagesforcar.ui.theme.MessagesForCarTheme

private data class MockConversation(
    val id: String,
    val title: String,
    val preview: String,
    val time: String,
    val unread: Int = 0
)

private data class MockMessage(
    val id: String,
    val text: String,
    val mine: Boolean,
    val timestamp: String
)

private val mockConversations = listOf(
    MockConversation("1", "ICE Susan Cerruti", "Perfect", "Mon", unread = 1),
    MockConversation("2", "Anthony Cerruti", "Somehow despite leaving the country...", "Mon"),
    MockConversation("3", "Family", "Now a book I could totally do...", "Fri"),
    MockConversation("4", "Dominic Cerruti", "IRS Free File program delivered...", "Sat"),
    MockConversation("5", "Cricket Wireless", "Thank you for your auto...", "Feb 16")
)

private val mockMessages = listOf(
    MockMessage("1", "I can't decide if I'm stopping for coffee, groceries or both.", mine = true, timestamp = "2:40 PM"),
    MockMessage("2", "Sanity. Can you get me that??", mine = false, timestamp = "2:42 PM"),
    MockMessage("3", "I just got out of my meeting. Still not ready for my sub.", mine = false, timestamp = "2:43 PM"),
    MockMessage("4", "Perfect", mine = false, timestamp = "3:53 PM")
)

@Composable
fun CarMessagesMockupScreen(
    modifier: Modifier = Modifier,
    isParked: Boolean = true,
    isLoggedIn: Boolean = true
) {
    val context = LocalContext.current
    val topBarHeight = 56.dp
    var loggedInState by remember(isLoggedIn) { mutableStateOf(isLoggedIn) }
    var selectedConversationId by remember { mutableStateOf(mockConversations.first().id) }
    var isHamburgerOpen by remember { mutableStateOf(false) }
    var isProfileMenuOpen by remember { mutableStateOf(false) }

    if (!isParked) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Messaging is available while parked.",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(24.dp)
                )
            }
        }
        return
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val landscape = maxWidth > maxHeight

        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                TopShellBar(
                    isLoggedIn = loggedInState,
                    isMenuOpen = isHamburgerOpen,
                    onMenuClick = { isHamburgerOpen = true },
                    onProfileClick = { isProfileMenuOpen = true },
                    modifier = Modifier.height(topBarHeight)
                )

                if (!loggedInState) {
                    LoggedOutWelcomePane(
                        onSignInClick = {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse("https://messages.google.com/web"))
                            )
                        },
                        onOpenPrivacy = {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse("https://policies.google.com/privacy"))
                            )
                        },
                        onOpenTerms = {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse("https://policies.google.com/terms"))
                            )
                        },
                        onOpenFeedback = {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse("https://support.google.com/messages"))
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                    return@Column
                }

                if (landscape) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        ConversationPaneList(
                            selectedConversationId = selectedConversationId,
                            onSelectConversation = { selectedConversationId = it },
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(0.33f)
                        )
                        VerticalDivider(modifier = Modifier.fillMaxHeight().width(1.dp))
                        ConversationDetailPane(
                            title = mockConversations.first { it.id == selectedConversationId }.title,
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(1f)
                        )
                    }
                } else {
                    ConversationChipsRow(
                        selectedConversationId = selectedConversationId,
                        onSelectConversation = { selectedConversationId = it }
                    )
                    HorizontalDivider(modifier = Modifier.fillMaxWidth())
                    ConversationDetailPane(
                        title = mockConversations.first { it.id == selectedConversationId }.title,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (loggedInState && isHamburgerOpen) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = Color.Black.copy(alpha = 0.35f),
                        onClick = { isHamburgerOpen = false }
                    ) {}

                    HamburgerMenuPanel(
                        onClose = { isHamburgerOpen = false },
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(0.42f)
                    )
                }
            }

            if (loggedInState && isProfileMenuOpen) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = Color.Black.copy(alpha = 0.35f),
                        onClick = { isProfileMenuOpen = false }
                    ) {}

                    ProfileAccountPanel(
                        onClose = { isProfileMenuOpen = false },
                        onSignOut = {
                            isProfileMenuOpen = false
                            loggedInState = false
                        },
                        onOpenPrivacy = {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse("https://policies.google.com/privacy"))
                            )
                        },
                        onOpenTerms = {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse("https://policies.google.com/terms"))
                            )
                        },
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 84.dp)
                            .fillMaxWidth(0.62f)
                            .widthIn(min = 420.dp, max = 760.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun LoggedOutWelcomePane(
    onSignInClick: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onOpenTerms: () -> Unit,
    onOpenFeedback: () -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val signInButtonWidth = minOf(maxWidth - 48.dp, 290.dp)

        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Welcome to Messages for Car",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Stay connected while parked with a familiar, simple messaging experience.",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onSignInClick,
                    modifier = Modifier
                        .width(signInButtonWidth)
                        .heightIn(min = 56.dp)
                ) {
                    Text("Sign in")
                }
            }

            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onOpenPrivacy) { Text("Privacy") }
                TextButton(onClick = onOpenTerms) { Text("Terms") }
                TextButton(onClick = onOpenFeedback) { Text("Send feedback") }
            }
        }
    }
}

@Composable
private fun ConversationPaneList(
    selectedConversationId: String,
    onSelectConversation: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Button(
            onClick = { },
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth()
                .heightIn(min = 56.dp)
        ) {
            Text("Start chat")
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(mockConversations) { conversation ->
                val isSelected = conversation.id == selectedConversationId
                Card(
                    onClick = { onSelectConversation(conversation.id) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(conversation.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
                            Text(conversation.preview, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(conversation.time, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun TopShellBar(
    isLoggedIn: Boolean,
    isMenuOpen: Boolean,
    onMenuClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(tonalElevation = 2.dp, modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isLoggedIn && !isMenuOpen) {
                    IconButton(onClick = onMenuClick) {
                        Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu")
                    }
                } else if (isLoggedIn) {
                    Spacer(modifier = Modifier.width(48.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("Messages for Car", style = MaterialTheme.typography.titleMedium)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isLoggedIn) {
                    IconButton(onClick = onProfileClick) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Profile",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                } else {
                    Button(onClick = { }, modifier = Modifier.heightIn(min = 44.dp)) {
                        Text("Sign in")
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileAccountPanel(
    onClose: () -> Unit,
    onSignOut: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onOpenTerms: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "steve.cerruti@gmail.com",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                TextButton(onClick = onClose) {
                    Text("✕", style = MaterialTheme.typography.headlineSmall)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = null,
                modifier = Modifier
                    .size(88.dp)
                    .align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Hi, Stephen!",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onSignOut,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .widthIn(min = 180.dp)
                    .heightIn(min = 48.dp)
            ) {
                Text("Sign out")
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onOpenPrivacy) {
                    Text("Privacy Policy", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text("•", color = MaterialTheme.colorScheme.onSurfaceVariant)
                TextButton(onClick = onOpenTerms) {
                    Text("Terms of Service", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun HamburgerMenuPanel(
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        tonalElevation = 2.dp,
        onClick = onClose
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 12.dp)
        ) {
            Text(
                text = "Messages for Car",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            )

            HamburgerMenuItem(icon = Icons.Default.Refresh, label = "Archived")
            HamburgerMenuItem(icon = Icons.Default.Settings, label = "Spam & blocked")
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            HamburgerMenuItem(icon = Icons.Default.Settings, label = "Settings")
            HamburgerMenuItem(icon = Icons.Default.Phone, label = "Unpair")
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            HamburgerMenuItem(icon = Icons.Default.AccountCircle, label = "Help")
            HamburgerMenuItem(icon = Icons.Default.CheckCircle, label = "Send feedback")
        }
    }
}

@Composable
private fun HamburgerMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 58.dp)
            .padding(horizontal = 20.dp)
            .wrapContentHeight(Alignment.CenterVertically),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null)
        Spacer(modifier = Modifier.width(18.dp))
        Text(text = label, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun ConversationChipsRow(
    selectedConversationId: String,
    onSelectConversation: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(mockConversations) { conversation ->
            val selected = conversation.id == selectedConversationId
            AssistChip(
                onClick = { onSelectConversation(conversation.id) },
                label = { Text(conversation.title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                colors = if (selected) {
                    AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                } else {
                    AssistChipDefaults.assistChipColors()
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConversationDetailPane(
    title: String,
    modifier: Modifier = Modifier
) {
    var draft by remember { mutableStateOf("") }
    var isConversationMuted by remember { mutableStateOf(false) }
    var showKebabMenu by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        Surface(tonalElevation = 1.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = { isConversationMuted = !isConversationMuted }) {
                    Text(if (isConversationMuted) "🔕" else "🔔")
                }

                Box {
                    IconButton(onClick = { showKebabMenu = true }) {
                        Text("⋮")
                    }
                    DropdownMenu(
                        expanded = showKebabMenu,
                        onDismissRequest = { showKebabMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Group details") },
                            onClick = { showKebabMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Archive") },
                            onClick = { showKebabMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = { showKebabMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Send feedback") },
                            onClick = { showKebabMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Help") },
                            onClick = { showKebabMenu = false }
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(mockMessages) { message ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (message.mine) Arrangement.End else Arrangement.Start
                ) {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (message.mine) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                            Text(
                                text = message.text,
                                color = if (message.mine) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = message.timestamp,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (message.mine) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        HorizontalDivider(modifier = Modifier.fillMaxWidth())
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("RCS message") },
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = { }) {
                Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
            }
        }
    }
}

@Preview(name = "Car Mockup Landscape", widthDp = 1280, heightDp = 720, showBackground = true)
@Composable
private fun CarMessagesMockupLandscapePreview() {
    MessagesForCarTheme {
        CarMessagesMockupScreen()
    }
}

@Preview(name = "Car Mockup Portrait", widthDp = 720, heightDp = 1280, showBackground = true)
@Composable
private fun CarMessagesMockupPortraitPreview() {
    MessagesForCarTheme {
        CarMessagesMockupScreen()
    }
}
