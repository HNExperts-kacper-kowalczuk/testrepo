package com.hnexperts.cosmetics

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.hnexperts.cosmetics.resources.Res
import com.hnexperts.cosmetics.resources.tab_history
import com.hnexperts.cosmetics.resources.tab_more
import com.hnexperts.cosmetics.resources.tab_scan
import com.hnexperts.cosmetics.resources.tab_search
import com.hnexperts.cosmetics.ui.history.HistoryScreen
import com.hnexperts.cosmetics.ui.history.HistoryViewModel
import com.hnexperts.cosmetics.ui.preferences.PreferencesScreen
import com.hnexperts.cosmetics.ui.preferences.PreferencesViewModel
import com.hnexperts.cosmetics.ui.result.ResultScreen
import com.hnexperts.cosmetics.ui.result.ResultViewModel
import com.hnexperts.cosmetics.ui.scan.ScanScreen
import com.hnexperts.cosmetics.ui.scan.ScanViewModel
import com.hnexperts.cosmetics.ui.search.SearchScreen
import com.hnexperts.cosmetics.ui.search.SearchViewModel
import com.hnexperts.cosmetics.ui.theme.CosmeticsTheme
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Serializable
object ScanDestination

@Serializable
object SearchDestination

@Serializable
object HistoryDestination

@Serializable
object MoreDestination

@Serializable
object ResultDestination

@Composable
fun App() {
    CosmeticsTheme {
        val navController: NavHostController = rememberNavController()
        val backStack by navController.currentBackStackEntryAsState()
        val showBottomBar: Boolean = backStack?.destination?.route?.contains("Result") != true
        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    AppBottomBar(navController)
                }
            }
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = ScanDestination,
                modifier = Modifier.padding(padding)
            ) {
                composable<ScanDestination> {
                    val viewModel: ScanViewModel = koinViewModel()
                    ScanScreen(viewModel = viewModel, onResult = {
                        navController.navigate(ResultDestination)
                    })
                }
                composable<SearchDestination> {
                    val viewModel: SearchViewModel = koinViewModel()
                    SearchScreen(viewModel = viewModel, onOpenResult = {
                        navController.navigate(ResultDestination)
                    })
                }
                composable<HistoryDestination> {
                    val viewModel: HistoryViewModel = koinViewModel()
                    HistoryScreen(viewModel = viewModel, onOpenResult = {
                        navController.navigate(ResultDestination)
                    })
                }
                composable<MoreDestination> {
                    val viewModel: PreferencesViewModel = koinViewModel()
                    PreferencesScreen(viewModel)
                }
                composable<ResultDestination> {
                    val viewModel: ResultViewModel = koinViewModel()
                    ResultScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                }
            }
        }
    }
}

@Composable
private fun AppBottomBar(navController: NavHostController) {
    val backStack by navController.currentBackStackEntryAsState()
    val destination = backStack?.destination
    NavigationBar {
        NavigationBarItem(
            selected = destination?.hierarchy?.any { it.route?.contains("Scan") == true } == true,
            onClick = { navController.navigateTab(ScanDestination) },
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            label = { Text(stringResource(Res.string.tab_scan)) }
        )
        NavigationBarItem(
            selected = destination?.hierarchy?.any { it.route?.contains("Search") == true } == true,
            onClick = { navController.navigateTab(SearchDestination) },
            icon = { Icon(Icons.Default.Search, contentDescription = null) },
            label = { Text(stringResource(Res.string.tab_search)) }
        )
        NavigationBarItem(
            selected = destination?.hierarchy?.any { it.route?.contains("History") == true } == true,
            onClick = { navController.navigateTab(HistoryDestination) },
            icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
            label = { Text(stringResource(Res.string.tab_history)) }
        )
        NavigationBarItem(
            selected = destination?.hierarchy?.any { it.route?.contains("More") == true } == true,
            onClick = { navController.navigateTab(MoreDestination) },
            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
            label = { Text(stringResource(Res.string.tab_more)) }
        )
    }
}

private fun NavHostController.navigateTab(destination: Any) {
    navigate(destination) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
