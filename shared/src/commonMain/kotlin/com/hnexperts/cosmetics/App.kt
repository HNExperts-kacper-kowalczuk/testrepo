package com.hnexperts.cosmetics

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.hnexperts.cosmetics.failure.Outcome
import com.hnexperts.cosmetics.legal.domain.LegalState
import com.hnexperts.cosmetics.legal.domain.LegalStore
import com.hnexperts.cosmetics.preferences.application.ThemeSession
import com.hnexperts.cosmetics.preferences.domain.ThemePreference
import com.hnexperts.cosmetics.resources.Res
import com.hnexperts.cosmetics.resources.tab_history
import com.hnexperts.cosmetics.resources.tab_more
import com.hnexperts.cosmetics.resources.tab_scan
import com.hnexperts.cosmetics.resources.tab_search
import com.hnexperts.cosmetics.scanning.application.LaunchIntentSession
import com.hnexperts.cosmetics.scanning.domain.ScannerMode
import com.hnexperts.cosmetics.ui.camera.CameraScanScreen
import com.hnexperts.cosmetics.ui.camera.CameraScanViewModel
import com.hnexperts.cosmetics.ui.confirm.ConfirmIngredientsScreen
import com.hnexperts.cosmetics.ui.confirm.ConfirmIngredientsViewModel
import com.hnexperts.cosmetics.ui.crop.CropIngredientsScreen
import com.hnexperts.cosmetics.ui.crop.CropIngredientsViewModel
import com.hnexperts.cosmetics.ui.compare.CompareScreen
import com.hnexperts.cosmetics.ui.compare.CompareViewModel
import com.hnexperts.cosmetics.ui.history.HistoryScreen
import com.hnexperts.cosmetics.ui.history.HistoryViewModel
import com.hnexperts.cosmetics.ui.legal.DisclaimerScreen
import com.hnexperts.cosmetics.ui.legal.DisclaimerViewModel
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
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

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

@Serializable
object CompareDestination

@Serializable
data class CameraDestination(val barcode: Boolean)

@Serializable
object CropIngredientsDestination

@Serializable
object ConfirmIngredientsDestination

@Composable
fun App() {
    val themeSession: ThemeSession = koinInject()
    val themePreference: ThemePreference by themeSession.preference.collectAsState()
    CosmeticsTheme(preference = themePreference) {
        val legal: LegalStore = koinInject()
        var accepted: Boolean? by remember { mutableStateOf(null) }
        LaunchedEffect(Unit) {
            accepted = when (val loaded: Outcome<LegalState> = legal.load()) {
                is Outcome.Ok -> loaded.value.disclaimerAccepted
                is Outcome.Err -> false
            }
        }
        when (accepted) {
            null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            false -> {
                val viewModel: DisclaimerViewModel = koinViewModel()
                DisclaimerScreen(viewModel = viewModel, onAccepted = { accepted = true })
            }
            true -> AppNavigation()
        }
    }
}

@Composable
private fun AppNavigation() {
    val navController: NavHostController = rememberNavController()
        val backStack by navController.currentBackStackEntryAsState()
        val route: String = backStack?.destination?.route.orEmpty()
        val showBottomBar: Boolean = !hidesBottomBar(route)
        val launchIntents: LaunchIntentSession = koinInject()
        LaunchedEffect(Unit) {
            launchIntents.openBarcodeCamera.collect { requested ->
                if (requested) {
                    navController.navigate(CameraDestination(barcode = true))
                    launchIntents.consume()
                }
            }
        }
        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
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
                    ScanScreen(
                        viewModel = viewModel,
                        onResult = { navController.navigate(ResultDestination) },
                        onOpenBarcodeCamera = { navController.navigate(CameraDestination(barcode = true)) },
                        onOpenInciCamera = { navController.navigate(CameraDestination(barcode = false)) }
                    )
                }
                composable<SearchDestination> {
                    val viewModel: SearchViewModel = koinViewModel()
                    SearchScreen(viewModel = viewModel, onOpenResult = {
                        navController.navigate(ResultDestination)
                    })
                }
                composable<HistoryDestination> {
                    val viewModel: HistoryViewModel = koinViewModel()
                    HistoryScreen(
                        viewModel = viewModel,
                        onOpenResult = { navController.navigate(ResultDestination) },
                        onOpenCompare = { navController.navigate(CompareDestination) }
                    )
                }
                composable<MoreDestination> {
                    val viewModel: PreferencesViewModel = koinViewModel()
                    PreferencesScreen(viewModel)
                }
                composable<CameraDestination> { entry ->
                    val destination: CameraDestination = entry.toRoute()
                    val mode: ScannerMode = if (destination.barcode) {
                        ScannerMode.BARCODE
                    } else {
                        ScannerMode.INGREDIENT_LIST
                    }
                    val viewModel: CameraScanViewModel = koinViewModel(parameters = { parametersOf(mode) })
                    CameraScanScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() },
                        onResult = { navController.navigateToResultFromCamera() },
                        onCrop = { navController.navigate(CropIngredientsDestination) }
                    )
                }
                composable<CropIngredientsDestination> {
                    val viewModel: CropIngredientsViewModel = koinViewModel()
                    CropIngredientsScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() },
                        onConfirm = { navController.navigateToConfirmFromCrop() }
                    )
                }
                composable<ConfirmIngredientsDestination> {
                    val viewModel: ConfirmIngredientsViewModel = koinViewModel()
                    ConfirmIngredientsScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() },
                        onResult = { navController.navigateToResultFromConfirm() },
                        onAddPhoto = { navController.navigate(CameraDestination(barcode = false)) }
                    )
                }
                composable<ResultDestination> {
                    val viewModel: ResultViewModel = koinViewModel()
                    ResultScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() },
                        onCheckLabel = { navController.navigate(CameraDestination(barcode = false)) }
                    )
                }
                composable<CompareDestination> {
                    val viewModel: CompareViewModel = koinViewModel()
                    CompareScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
}

private fun hidesBottomBar(route: String): Boolean {
    return route.contains("Result") || route.contains("Camera") ||
        route.contains("Crop") || route.contains("Confirm") || route.contains("Compare")
}

private fun NavHostController.navigateToResultFromCamera() {
    navigate(ResultDestination) {
        popUpTo<CameraDestination> { inclusive = true }
    }
}

private fun NavHostController.navigateToConfirmFromCrop() {
    val poppedConfirm: Boolean = popBackStack<ConfirmIngredientsDestination>(inclusive = true)
    if (!poppedConfirm) {
        popBackStack<CameraDestination>(inclusive = true)
    }
    navigate(ConfirmIngredientsDestination)
}

private fun NavHostController.navigateToResultFromConfirm() {
    navigate(ResultDestination) {
        popUpTo<ConfirmIngredientsDestination> { inclusive = true }
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
            icon = { Icon(Icons.Default.Home, contentDescription = stringResource(Res.string.tab_scan)) },
            label = { Text(stringResource(Res.string.tab_scan)) }
        )
        NavigationBarItem(
            selected = destination?.hierarchy?.any { it.route?.contains("Search") == true } == true,
            onClick = { navController.navigateTab(SearchDestination) },
            icon = { Icon(Icons.Default.Search, contentDescription = stringResource(Res.string.tab_search)) },
            label = { Text(stringResource(Res.string.tab_search)) }
        )
        NavigationBarItem(
            selected = destination?.hierarchy?.any { it.route?.contains("History") == true } == true,
            onClick = { navController.navigateTab(HistoryDestination) },
            icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = stringResource(Res.string.tab_history)) },
            label = { Text(stringResource(Res.string.tab_history)) }
        )
        NavigationBarItem(
            selected = destination?.hierarchy?.any { it.route?.contains("More") == true } == true,
            onClick = { navController.navigateTab(MoreDestination) },
            icon = { Icon(Icons.Default.Settings, contentDescription = stringResource(Res.string.tab_more)) },
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
