package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.DeviceHub
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.CyberNavTab
import com.example.ui.CyberShieldViewModel
import com.example.ui.screens.AppLinkCheckerScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.FileInspectorScreen
import com.example.ui.screens.LinkCheckerScreen
import com.example.ui.screens.ScamDetectorScreen
import com.example.ui.theme.CyberBgDark
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberSafe
import com.example.ui.theme.CyberShieldTheme
import com.example.ui.theme.CyberSurfaceDark
import com.example.ui.theme.CyberSurfaceElevated
import com.example.ui.theme.CyberTextMuted
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary

class MainActivity : ComponentActivity() {

    private val viewModel: CyberShieldViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CyberShieldTheme {
                CyberShieldApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CyberShieldApp(viewModel: CyberShieldViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()

    // BackHandler to return to Dashboard if inside a subscreen
    if (currentTab != CyberNavTab.DASHBOARD) {
        BackHandler {
            viewModel.selectTab(CyberNavTab.DASHBOARD)
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(CyberBgDark)) {
        val isWideScreen = maxWidth >= 600.dp

        if (isWideScreen) {
            // Adaptive Tablet / Desktop Layout with NavigationRail
            Row(modifier = Modifier.fillMaxSize()) {
                NavigationRail(
                    modifier = Modifier
                        .fillMaxHeight()
                        .testTag("cyber_nav_rail")
                        .windowInsetsPadding(WindowInsets.statusBars),
                    containerColor = CyberSurfaceDark,
                    contentColor = CyberTextPrimary,
                    header = {
                        Box(
                            modifier = Modifier
                                .padding(16.dp)
                                .size(42.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(CyberCyan.copy(alpha = 0.2f))
                                .border(1.dp, CyberCyan, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "CyberShield",
                                tint = CyberCyan,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                ) {
                    CyberNavTab.entries.forEach { tab ->
                        NavigationRailItem(
                            selected = currentTab == tab,
                            onClick = { viewModel.selectTab(tab) },
                            icon = { Icon(getTabIcon(tab), contentDescription = tab.label) },
                            label = { Text(tab.label, fontSize = 10.sp, maxLines = 1, fontFamily = FontFamily.Monospace) },
                            modifier = Modifier.testTag("rail_tab_${tab.name.lowercase()}"),
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = CyberCyan,
                                selectedTextColor = CyberCyan,
                                indicatorColor = CyberSurfaceElevated,
                                unselectedIconColor = CyberTextMuted,
                                unselectedTextColor = CyberTextMuted
                            )
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .windowInsetsPadding(WindowInsets.statusBars)
                ) {
                    AppContent(currentTab = currentTab, viewModel = viewModel)
                }
            }
        } else {
            // Standard Handheld / Phone Layout with TopBar & Bottom NavigationBar
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = CyberBgDark,
                topBar = {
                    TopAppBar(
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(CyberCyan.copy(alpha = 0.2f))
                                        .border(1.dp, CyberCyan, RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = CyberCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "CYBERSHIELD // HUD",
                                        color = CyberTextPrimary,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = "ZERO-TRUST DEFENSE MATRIX",
                                        color = CyberCyan,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = CyberSurfaceDark,
                            titleContentColor = CyberTextPrimary
                        ),
                        actions = {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = CyberSurfaceElevated,
                                border = androidx.compose.foundation.BorderStroke(0.5.dp, CyberSafe.copy(alpha = 0.6f)),
                                modifier = Modifier.padding(end = 12.dp)
                            ) {
                                Text(
                                    text = "ENCLAVE v5.0",
                                    color = CyberSafe,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 0.5.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    )
                },
                bottomBar = {
                    NavigationBar(
                        modifier = Modifier
                            .testTag("cyber_bottom_nav")
                            .windowInsetsPadding(WindowInsets.navigationBars),
                        containerColor = CyberSurfaceDark,
                        tonalElevation = 6.dp
                    ) {
                        CyberNavTab.entries.forEach { tab ->
                            NavigationBarItem(
                                selected = currentTab == tab,
                                onClick = { viewModel.selectTab(tab) },
                                icon = {
                                    Icon(
                                        imageVector = getTabIcon(tab),
                                        contentDescription = tab.label
                                    )
                                },
                                label = {
                                    Text(
                                        text = when (tab) {
                                            CyberNavTab.DASHBOARD -> "Command"
                                            CyberNavTab.FILE_INSPECTOR -> "Files"
                                            CyberNavTab.LINK_CHECKER -> "Links"
                                            CyberNavTab.APP_LINK_CHECKER -> "App Links"
                                            CyberNavTab.SCAM_DETECTOR -> "Fraud"
                                        },
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = if (currentTab == tab) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                modifier = Modifier.testTag("bottom_tab_${tab.name.lowercase()}"),
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = CyberCyan,
                                    selectedTextColor = CyberCyan,
                                    indicatorColor = CyberSurfaceElevated,
                                    unselectedIconColor = CyberTextMuted,
                                    unselectedTextColor = CyberTextMuted
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
                    AppContent(currentTab = currentTab, viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun AppContent(
    currentTab: CyberNavTab,
    viewModel: CyberShieldViewModel,
    modifier: Modifier = Modifier
) {
    AnimatedContent(
        targetState = currentTab,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "tab_transition",
        modifier = modifier.fillMaxSize()
    ) { tab ->
        when (tab) {
            CyberNavTab.DASHBOARD -> DashboardScreen(viewModel = viewModel)
            CyberNavTab.FILE_INSPECTOR -> FileInspectorScreen(viewModel = viewModel)
            CyberNavTab.LINK_CHECKER -> LinkCheckerScreen(viewModel = viewModel)
            CyberNavTab.APP_LINK_CHECKER -> AppLinkCheckerScreen(viewModel = viewModel)
            CyberNavTab.SCAM_DETECTOR -> ScamDetectorScreen(viewModel = viewModel)
        }
    }
}

fun getTabIcon(tab: CyberNavTab): ImageVector {
    return when (tab) {
        CyberNavTab.DASHBOARD -> Icons.Default.Radar
        CyberNavTab.FILE_INSPECTOR -> Icons.Default.Archive
        CyberNavTab.LINK_CHECKER -> Icons.Default.Link
        CyberNavTab.APP_LINK_CHECKER -> Icons.Default.DeviceHub
        CyberNavTab.SCAM_DETECTOR -> Icons.Default.Psychology
    }
}
