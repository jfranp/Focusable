package com.example.focusable.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.example.focusable.R
import com.example.focusable.ui.screen.home.HomeScreen
import com.example.focusable.ui.screen.home.HomeViewModel
import com.example.focusable.ui.screen.preferences.PreferencesScreen
import com.example.focusable.ui.screen.sessions.SessionsScreen
import org.koin.androidx.compose.koinViewModel

enum class NavTab(val icon: ImageVector, @StringRes val labelRes: Int) {
    HOME(Icons.Default.Home, R.string.nav_label_focus),
    SESSIONS(Icons.Default.Refresh, R.string.nav_label_sessions),
    PREFERENCES(Icons.Default.Settings, R.string.nav_label_preferences)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val homeViewModel: HomeViewModel = koinViewModel()
    val isDebugVisible by homeViewModel.isDebugVisible.collectAsState()

    var currentTab by rememberSaveable { mutableStateOf(NavTab.HOME) }

    Scaffold(
        topBar = {
            when (currentTab) {
                NavTab.HOME -> CenterAlignedTopAppBar(
                    modifier = Modifier.padding(top = 8.dp),
                    title = { Text(stringResource(R.string.app_name)) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        scrolledContainerColor = MaterialTheme.colorScheme.background
                    ),
                    actions = {
                        IconButton(
                            onClick = { homeViewModel.toggleDebug() }
                        ) {
                            Icon(
                                Icons.Default.Build,
                                contentDescription = if (isDebugVisible)
                                    stringResource(R.string.cd_hide_debug_dashboard)
                                else
                                    stringResource(R.string.cd_show_debug_dashboard)
                            )
                        }
                    }
                )
                NavTab.SESSIONS -> CenterAlignedTopAppBar(
                    modifier = Modifier.padding(top = 8.dp),
                    title = { Text(stringResource(R.string.top_bar_title_sessions)) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        scrolledContainerColor = MaterialTheme.colorScheme.background
                    )
                )
                NavTab.PREFERENCES -> CenterAlignedTopAppBar(
                    modifier = Modifier.padding(top = 8.dp),
                    title = { Text(stringResource(R.string.top_bar_title_preferences)) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        scrolledContainerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        },
        bottomBar = {
            NavigationBar {
                NavTab.entries.forEach { tab ->
                    val label = stringResource(tab.labelRes)
                    NavigationBarItem(
                        selected = currentTab == tab,
                        onClick = { currentTab = tab },
                        icon = { Icon(tab.icon, contentDescription = label) },
                        label = { Text(label) }
                    )
                }
            }
        },
        modifier = modifier
    ) { padding ->
        AnimatedContent(
            targetState = currentTab,
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith
                    fadeOut(animationSpec = tween(300))
            },
            label = "TabContent"
        ) { tab ->
            when (tab) {
                NavTab.HOME -> HomeScreen(
                    viewModel = homeViewModel,
                    isDebugVisible = isDebugVisible
                )
                NavTab.SESSIONS -> SessionsScreen()
                NavTab.PREFERENCES -> PreferencesScreen()
            }
        }
    }
}
