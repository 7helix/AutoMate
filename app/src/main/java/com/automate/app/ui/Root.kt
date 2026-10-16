package com.automate.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.navArgument
import com.automate.app.AppViewModel
import com.automate.app.ui.screens.AboutScreen
import com.automate.app.ui.screens.EditRuleScreen
import com.automate.app.ui.screens.HomeScreen
import com.automate.app.ui.screens.LogsScreen
import com.automate.app.ui.screens.OnboardingScreen
import com.automate.app.ui.screens.RulesScreen
import com.automate.app.ui.screens.SettingsScreen
import com.automate.app.ui.theme.AppBackground
import kotlinx.coroutines.delay

@Composable
fun AutoMateRoot(vm: AppViewModel) {
    var splash by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) { delay(1400); splash = false }

    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner) {
        val o = LifecycleEventObserver { _, e -> if (e == Lifecycle.Event.ON_RESUME) vm.refreshFlags() }
        owner.lifecycle.addObserver(o)
        onDispose { owner.lifecycle.removeObserver(o) }
    }

    AppBackground {
        if (!vm.onboarded) OnboardingScreen(vm) else MainScaffold(vm)
        ToastHost(vm.toasts, Modifier.align(Alignment.TopCenter))
        AnimatedVisibility(
            visible = splash,
            exit = fadeOut(tween(450)) + scaleOut(tween(450), targetScale = 1.15f)
        ) { SplashOverlay() }
    }
}

@Composable
private fun SplashOverlay() {
    val scale = remember { Animatable(0.4f) }
    LaunchedEffect(Unit) { scale.animateTo(1f, spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessLow)) }
    Box(Modifier.fillMaxSize().background(Color(0xFF6C5CE7)), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.graphicsLayer { scaleX = scale.value; scaleY = scale.value }
        ) {
            Icon(Icons.Rounded.Bolt, contentDescription = null, tint = Color.White, modifier = Modifier.size(96.dp))
            Text("AutoMate", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Bold)
            Text("Automate anything, anywhere", color = Color.White.copy(alpha = 0.85f))
        }
    }
}

private data class NavTab(val route: String, val label: String, val icon: ImageVector)

@Composable
private fun MainScaffold(vm: AppViewModel) {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route
    val tabs = listOf(
        NavTab("home", "Home", Icons.Rounded.Home),
        NavTab("rules", "Rules", Icons.Rounded.AutoAwesome),
        NavTab("logs", "Activity", Icons.Rounded.History),
        NavTab("settings", "Settings", Icons.Rounded.Settings)
    )
    val showBar = tabs.any { it.route == route }

    fun goTab(r: String) {
        nav.navigate(r) {
            popUpTo(nav.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    val anim = vm.animations
    val enter: EnterTransition = if (anim) fadeIn(tween(260)) + slideInHorizontally(tween(300)) { it / 12 } else EnterTransition.None
    val exit: ExitTransition = if (anim) fadeOut(tween(160)) else ExitTransition.None
    val popExit: ExitTransition = if (anim) fadeOut(tween(200)) + slideOutHorizontally(tween(260)) { it / 12 } else ExitTransition.None

    Scaffold(
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        bottomBar = {
            AnimatedVisibility(
                visible = showBar,
                enter = slideInVertically { it },
                exit = slideOutVertically { it }
            ) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
                    tabs.forEach { t ->
                        NavigationBarItem(
                            selected = route == t.route,
                            onClick = { goTab(t.route) },
                            icon = { Icon(t.icon, contentDescription = t.label) },
                            label = { Text(t.label) }
                        )
                    }
                }
            }
        }
    ) { inner ->
        NavHost(
            navController = nav,
            startDestination = "home",
            modifier = Modifier.padding(inner),
            enterTransition = { enter },
            exitTransition = { exit },
            popEnterTransition = { if (anim) fadeIn(tween(240)) else EnterTransition.None },
            popExitTransition = { popExit }
        ) {
            composable("home") { HomeScreen(vm, onEdit = { nav.navigate("edit/$it") }, onTab = { goTab(it) }) }
            composable("rules") { RulesScreen(vm, onEdit = { nav.navigate("edit/$it") }) }
            composable("logs") { LogsScreen(vm) }
            composable("settings") { SettingsScreen(vm, onAbout = { nav.navigate("about") }) }
            composable(
                "edit/{id}",
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) { e ->
                EditRuleScreen(vm, e.arguments?.getString("id") ?: "new", onBack = { nav.popBackStack() })
            }
            composable("about") { AboutScreen(onBack = { nav.popBackStack() }) }
        }
    }
}
