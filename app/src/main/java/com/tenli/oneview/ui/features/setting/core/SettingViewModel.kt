package com.tenli.oneview.ui.features.setting.core

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tenli.oneview.R
import com.tenli.oneview.data.local.GlobalData
import com.tenli.oneview.data.local.UserSession
import com.tenli.oneview.data.local.db.AppDatabase
import com.tenli.oneview.data.repository.AppRepository
import com.tenli.oneview.data.repository.BoxRepository
import com.tenli.oneview.data.repository.DataRepository
import com.tenli.oneview.data.repository.UserRepository
import com.tenli.oneview.util.DispatcherProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingViewModel(
    application: Application,
    internal val userRepository: UserRepository,
    internal val boxRepository: BoxRepository,
    internal val appRepository: AppRepository,
    internal val dispatcherProvider: DispatcherProvider
) : AndroidViewModel(application) {

    internal val _uiState = MutableStateFlow(SettingUiState())
    val uiState = _uiState.asStateFlow()

    internal val _uiEvent = MutableSharedFlow<SettingUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    internal val navigationStack = mutableListOf<Pair<SettingScreenType, String>>()
    internal var timerJob: Job? = null

    private var lastProcessedTarget: String? = null

    init {
        refreshUserData()
        checkDeviceIssues()
    }

    fun handleInitialTarget(target: String?) {
        if (target == null || target == lastProcessedTarget) return
        lastProcessedTarget = target
        when (target) {
            "devices" -> {
                fetchDevices()
                navigateTo(SettingScreenType.DevicesManagement, getApplication<Application>().getString(R.string.lbl_device_management))
            }

            "members" -> {
                fetchGroups()
                navigateTo(SettingScreenType.Group, "Quản lý nhà")
            }
        }
    }

    fun refreshUserData() {
        _uiState.update { it.copy(userData = UserSession.userData) }
    }

    private fun checkDeviceIssues() {
        viewModelScope.launch {
            val hasIssue = DataRepository.deviceList.any { it.status == 0 }
            _uiState.update { it.copy(hasDeviceIssue = hasIssue) }
        }
    }

    fun navigateTo(screen: SettingScreenType, title: String) {
        navigationStack.add(uiState.value.currentScreen to uiState.value.title)
        _uiState.update { it.copy(currentScreen = screen, title = title) }
    }

    fun navigateBack() {
        if (navigationStack.isNotEmpty()) {
            val lastScreen = navigationStack.removeAt(navigationStack.size - 1)
            _uiState.update { it.copy(currentScreen = lastScreen.first, title = lastScreen.second) }
        } else {
            _uiState.update { it.copy(currentScreen = SettingScreenType.Main, title = "Cài đặt") }
        }
        if (_uiState.value.currentScreen == SettingScreenType.Main) {
            lastProcessedTarget = null
        }
    }

//    fun navigateBack() {
//        if (navigationStack.isNotEmpty()) {
//            val lastScreen = navigationStack.removeAt(navigationStack.size - 1)
//            _uiState.update { it.copy(currentScreen = lastScreen.first, title = lastScreen.second) }
//        } else {
//            _uiState.update { it.copy(currentScreen = SettingScreenType.Main, title = "Cài đặt") }
//        }
//    }

    fun setLanguage(lang: String) {
        _uiState.update { it.copy(currentLanguage = lang) }
        GlobalData.preferences?.edit()?.putString("tenli_app_language", lang)?.apply()
    }

    fun showLogoutDialog(show: Boolean) {
        _uiState.update { it.copy(isLogoutDialogOpen = show) }
    }

    fun performLogout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoggingOut = true) }
            try {
                userRepository.logout()
            } catch (_: Exception) {
            } finally {
                clearLocalData()
                _uiState.update { it.copy(isLoggingOut = false, isLogoutDialogOpen = false) }
                withContext(dispatcherProvider.main) { onSuccess() }
            }
        }
    }

    fun toggleScriptMenu(expanded: Boolean) {
        _uiState.update { it.copy(box = it.box.copy(isScriptMenuExpanded = expanded)) }
    }

    @SuppressLint("UseKtx")
    internal suspend fun clearLocalData() {
        withContext(dispatcherProvider.io) {
            try {
                val db = AppDatabase.Companion.getDatabase(getApplication())
                db.eventDao().clearAll()
                DataRepository.clearCache()
                UserSession.clear()
            } catch (_: Exception) {
            }
        }
    }

    internal fun updateAccountState(action: (AccountUiState) -> AccountUiState) {
        _uiState.update { it.copy(account = action(it.account)) }
    }

    internal fun updateGroupState(action: (GroupUiState) -> GroupUiState) {
        _uiState.update { it.copy(group = action(it.group)) }
    }

    internal fun updateDeviceState(action: (ClientUiState) -> ClientUiState) {
        _uiState.update { it.copy(client = action(it.client)) }
    }

    internal fun showSnackbar(message: String) {
        viewModelScope.launch {
            _uiEvent.emit(SettingUiEvent.ShowSnackbar(message))
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = checkNotNull(this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as com.tenli.oneview.TenliApp
                SettingViewModel(
                    application = application,
                    userRepository = application.container.userRepository,
                    boxRepository = application.container.boxRepository,
                    appRepository = application.container.appRepository,
                    dispatcherProvider = application.container.dispatcherProvider
                )
            }
        }
    }
}

