package com.android.smartsoundscheduler.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.android.smartsoundscheduler.ui.screens.AddEditSlotScreen
import com.android.smartsoundscheduler.ui.screens.HomeScreen
import com.android.smartsoundscheduler.ui.screens.SettingsScreen
import com.android.smartsoundscheduler.viewmodel.MainViewModel

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object AddSlot : Screen("add_slot")
    object EditSlot : Screen("edit_slot/{slotId}") {
        fun createRoute(slotId: Long) = "edit_slot/$slotId"
    }
    object Settings : Screen("settings")
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val viewModel: MainViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val addEditState by viewModel.addEditState.collectAsState()
    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                uiState = uiState,
                onAddClick = {
                    viewModel.initAddSlot()
                    navController.navigate(Screen.AddSlot.route)
                },
                onEditClick = { slotId ->
                    viewModel.initEditSlot(slotId)
                    navController.navigate(Screen.EditSlot.createRoute(slotId))
                },
                onToggleEnabled = { slot ->
                    viewModel.toggleSlotEnabled(slot)
                },
                onDeleteSlot = { slot ->
                    viewModel.deleteTimeSlot(slot)
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                },
                onRequestPermission = {
                    viewModel.requestNotificationPolicyAccess(context)
                }
            )
        }

        composable(Screen.AddSlot.route) {
            AddEditSlotScreen(
                state = addEditState,
                onNameChange = viewModel::updateName,
                onStartTimeChange = viewModel::updateStartTime,
                onEndTimeChange = viewModel::updateEndTime,
                onSoundModeChange = viewModel::updateSoundMode,
                onDayToggle = viewModel::toggleDay,
                onQuickDaySelect = { days ->
                    days.forEach { day ->
                        if (!addEditState.activeDays.contains(day)) {
                            viewModel.toggleDay(day)
                        }
                    }
                    addEditState.activeDays.forEach { day ->
                        if (!days.contains(day)) {
                            viewModel.toggleDay(day)
                        }
                    }
                },
                onVibrationChange = viewModel::updateVibration,
                onColorChange = viewModel::updateColor,
                onSave = {
                    viewModel.saveTimeSlot()
                },
                onNavigateBack = {
                    viewModel.resetSaveSuccess()
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.EditSlot.route,
            arguments = listOf(navArgument("slotId") { type = NavType.LongType })
        ) {
            AddEditSlotScreen(
                state = addEditState,
                onNameChange = viewModel::updateName,
                onStartTimeChange = viewModel::updateStartTime,
                onEndTimeChange = viewModel::updateEndTime,
                onSoundModeChange = viewModel::updateSoundMode,
                onDayToggle = viewModel::toggleDay,
                onQuickDaySelect = { days ->
                    val currentDays = addEditState.activeDays.toMutableSet()
                    currentDays.clear()
                    currentDays.addAll(days)
                    // Reset and set new days
                    addEditState.activeDays.forEach { viewModel.toggleDay(it) }
                    days.forEach { viewModel.toggleDay(it) }
                },
                onVibrationChange = viewModel::updateVibration,
                onColorChange = viewModel::updateColor,
                onSave = {
                    viewModel.saveTimeSlot()
                },
                onNavigateBack = {
                    viewModel.resetSaveSuccess()
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                hasNotificationPolicyAccess = uiState.hasNotificationPolicyAccess,
                onRequestNotificationPolicyAccess = {
                    viewModel.requestNotificationPolicyAccess(context)
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}