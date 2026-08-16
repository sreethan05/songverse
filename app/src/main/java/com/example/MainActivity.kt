package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.EaseInCubic
import androidx.compose.animation.core.EaseOutQuart
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.SongViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: SongViewModel = viewModel()
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()
            val selectedTheme by viewModel.selectedTheme.collectAsState()

            MyApplicationTheme(
                darkTheme = isDarkTheme,
                themePreset = selectedTheme
            ) {
                MainAppScreen(
                    viewModel = viewModel,
                    isDarkTheme = isDarkTheme
                )
            }
        }
    }
}

@Composable
fun AppHeader(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    viewModel: SongViewModel,
    onFocusSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // --- Row 1: Brand & Actions ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "SONGVERSE",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    letterSpacing = 2.sp
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                var showThemeDialog by remember { mutableStateOf(false) }
                val currentThemePreset by viewModel.selectedTheme.collectAsState()

                IconButton(
                    onClick = { showThemeDialog = true },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = "Change Theme Palette",
                        tint = NeonCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onToggleTheme,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "Toggle Theme",
                        tint = if (isDarkTheme) AccentOrange else NeonPurple,
                        modifier = Modifier.size(18.dp)
                    )
                }

                if (showThemeDialog) {
                    AlertDialog(
                        onDismissRequest = { showThemeDialog = false },
                        title = {
                            Text(
                                "Choose Theme Vibe",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        },
                        text = {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                val presets = listOf(
                                    Triple(AppThemePreset.SUNSET_GOLD, "Sunset Gold", Color(0xFFFFAA00)),
                                    Triple(AppThemePreset.OCEAN_BLUE, "Deep Ocean", Color(0xFF00E1D9)),
                                    Triple(AppThemePreset.FOREST_EMERALD, "Forest Emerald", Color(0xFF00FF87)),
                                    Triple(AppThemePreset.CYBERPUNK, "Cyberpunk Neon", Color(0xFFFF007F))
                                )

                                presets.forEach { (preset, label, previewColor) ->
                                    val isSelected = currentThemePreset == preset
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                                else Color.Transparent
                                            )
                                            .border(
                                                width = 1.dp,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .clickable {
                                                viewModel.setThemePreset(preset)
                                            }
                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            // Theme preview color dot
                                            Box(
                                                modifier = Modifier
                                                    .size(20.dp)
                                                    .clip(CircleShape)
                                                    .background(previewColor)
                                            )
                                            Text(
                                                text = label,
                                                fontSize = 14.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = MaterialTheme.colorScheme.onBackground
                                            )
                                        }
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = "Selected",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { showThemeDialog = false }) {
                                Text("Done", color = NeonCyan)
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(24.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // --- Row 2: Live Global Search Bar ---
        val borderBrush = Brush.linearGradient(colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary))
        val placeholderText = "Search any song, artist, album..."

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { 
                viewModel.updateSearchQuery(it)
                onFocusSearch() // Immediately switch to Search tab on typing/interacting
            },
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onBackground),
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { 
                    if (it.isFocused) {
                        onFocusSearch()
                    }
                }
                .testTag("persistent_search_input")
                .border(
                    width = 1.dp,
                    brush = borderBrush,
                    shape = RoundedCornerShape(14.dp)
                ),
            placeholder = { 
                Text(
                    text = placeholderText, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                ) 
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
            ),
            shape = RoundedCornerShape(14.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                if (searchQuery.trim().isNotEmpty()) {
                    viewModel.addRecentSearch(searchQuery)
                }
                focusManager.clearFocus()
            })
        )
    }
}

@Composable
fun MainAppScreen(
    viewModel: SongViewModel,
    isDarkTheme: Boolean
) {
    var selectedTab by remember { mutableStateOf(0) }
    val currentSong by viewModel.currentPlayingSong.collectAsState()
    var isPlayerExpanded by remember { mutableStateOf(false) }

    // Intercept device hardware back button when full player is expanded
    if (isPlayerExpanded) {
        BackHandler {
            isPlayerExpanded = false
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            AppHeader(
                isDarkTheme = isDarkTheme,
                onToggleTheme = { viewModel.toggleTheme() },
                viewModel = viewModel,
                onFocusSearch = { selectedTab = 1 }
            )
        },
        bottomBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Pin MiniPlayer above the navigation bar when a song is playing or loaded
                if (currentSong != null) {
                    MiniPlayer(
                        viewModel = viewModel,
                        onExpand = { isPlayerExpanded = true }
                    )
                }

                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    val activeIndicatorColor = NeonPurple.copy(alpha = 0.25f)
                    val activeContentColor = NeonCyan

                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.Explore,
                                contentDescription = "Discover"
                            )
                        },
                        label = { Text("Discover") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = activeContentColor,
                            unselectedIconColor = TextSecondary,
                            selectedTextColor = activeContentColor,
                            unselectedTextColor = TextSecondary,
                            indicatorColor = activeIndicatorColor
                        )
                    )

                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.Search,
                                contentDescription = "Search"
                            )
                        },
                        label = { Text("Search") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = activeContentColor,
                            unselectedIconColor = TextSecondary,
                            selectedTextColor = activeContentColor,
                            unselectedTextColor = TextSecondary,
                            indicatorColor = activeIndicatorColor
                        )
                    )

                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.LibraryMusic,
                                contentDescription = "Library"
                            )
                        },
                        label = { Text("Library") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = activeContentColor,
                            unselectedIconColor = TextSecondary,
                            selectedTextColor = activeContentColor,
                            unselectedTextColor = TextSecondary,
                            indicatorColor = activeIndicatorColor
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> DiscoverScreen(
                    viewModel = viewModel,
                    onNavigateToSearch = { selectedTab = 1 }
                )
                1 -> SearchScreen(viewModel = viewModel)
                2 -> LibraryScreen(viewModel = viewModel)
            }
        }
    }

    // Expanding full-screen visual music player sliding transition
    AnimatedVisibility(
        visible = isPlayerExpanded,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(400, easing = EaseOutQuart)
        ) + fadeIn(),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(350, easing = EaseInCubic)
        ) + fadeOut()
    ) {
        FullPlayerScreen(
            viewModel = viewModel,
            onMinimize = { isPlayerExpanded = false },
            onSearchQuerySelect = { query ->
                selectedTab = 1
                isPlayerExpanded = false
                viewModel.updateSearchQuery(query)
            }
        )
    }
}
