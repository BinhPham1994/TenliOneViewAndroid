package com.tenli.oneview.ui.features.monitor

import android.app.Application
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tenli.oneview.data.local.GlobalData
import com.tenli.oneview.data.local.db.AppDatabase
import com.tenli.oneview.data.mapper.EventProcessor
import com.tenli.oneview.data.repository.BoxRepository
import com.tenli.oneview.data.repository.DataRepository
import com.tenli.oneview.data.repository.EventRepository
import com.tenli.oneview.model.network.EventItem
import com.tenli.oneview.model.network.HomeGroupDisplay
import com.tenli.oneview.model.network.MonitorDisplayItem
import com.tenli.oneview.ui.utils.AppKeys
import com.tenli.oneview.util.DispatcherProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MonitorViewModel(
    application: Application,
    private val boxRepository: BoxRepository,
    private val eventRepository: EventRepository,
    private val dispatcherProvider: DispatcherProvider
) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val eventDao = database.eventDao()

    private val _uiState = MutableStateFlow(MonitorUiState())
    val uiState = _uiState.asStateFlow()
    private val repository = DataRepository

    private val _monitorEvents = MutableStateFlow<List<EventItem>>(emptyList())
    private val monitorTypeComparator = Comparator<String> { t1, t2 ->
        val p1 = when {
            t1.contains("fire") -> 0
            t1.contains("person") || t1.contains("face") -> 1
            else -> 2
        }
        val p2 = when {
            t2.contains("fire") -> 0
            t2.contains("person") || t2.contains("face") -> 1
            else -> 2
        }
        if (p1 == p2) t1.compareTo(t2) else p1.compareTo(p2)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val monitorEvents: StateFlow<List<EventItem>> = uiState
        .map { it.selectedMonitor?.monitor?.id }
        .distinctUntilChanged()
        .flatMapLatest { monitorId ->
            if (monitorId == null) flowOf(emptyList())
            else eventDao.getEventsByMonitorFlow(monitorId)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        repository.restore()
        loadInitialState()
        refreshMonitors()
    }

    private fun getFilteredAndGroupedMonitors(groupId: Int): Pair<List<MonitorDisplayItem>, java.util.SortedMap<String, List<MonitorDisplayItem>>> {
        // 1. Lấy danh sách Device ID thuộc Group
        val deviceIdsInGroup = repository.deviceList
            .filter { it.userGroupId == groupId }
            .map { it.id }

        // 2. Lấy Monitor và lọc trùng ngay lập tức [cite: 2026-03-19]
        val filtered = deviceIdsInGroup.flatMap { deviceId ->
            repository.monitorMap[deviceId] ?: emptyList()
        }.distinctBy { "${it.deviceId}_${it.monitor.id}" } // <--- THÊM DÒNG NÀY

        // 3. Nhóm dữ liệu
        val grouped = filtered.groupBy { it.monitor.type ?: "Khác" }
            .toSortedMap(monitorTypeComparator)

        return Pair(filtered, grouped)
    }

    fun refreshMonitors() {
        val selectedGroupId = _uiState.value.selectedGroup?.group?.id ?: return
        val devicesInGroup = repository.deviceList.filter {
            it.userGroupId == selectedGroupId && it.publicTargetURI.isNotEmpty()
        }

        if (devicesInGroup.isEmpty()) return

        viewModelScope.launch {
            if (_uiState.value.monitors.isEmpty()) {
                _uiState.update { it.copy(isRefreshing = true) }
            }

            val monitorJobs = devicesInGroup.map { device ->
                async(dispatcherProvider.io) {
                    try {
                        val result = boxRepository.getMonitors(
                            deviceDomain = device.publicTargetURI,
                            deviceToken = device.key
                        )

                        if (result.isSuccess) {
                            val items = result.getOrNull()?.map { monitor ->
                                MonitorDisplayItem(
                                    monitor = monitor,
                                    deviceUri = device.publicTargetURI,
                                    deviceKey = device.key,
                                    deviceId = device.id
                                )
                            } ?: emptyList()
                            repository.monitorMap[device.id] = items
                            items
                        } else null
                    } catch (e: Exception) {
                        null
                    }
                }
            }

            val results = monitorJobs.awaitAll().filterNotNull()

            if (results.isNotEmpty()) {
                val combinedMonitors = results.flatten().distinctBy { "${it.deviceId}_${it.monitor.id}" }
                if (combinedMonitors != _uiState.value.monitors) {
                    repository.persist()
                    val grouped = withContext(dispatcherProvider.default) {
                        combinedMonitors.groupBy { it.monitor.type ?: "Khác" }
                            .toSortedMap(monitorTypeComparator)
                    }
                    _uiState.update {
                        it.copy(
                            monitors = combinedMonitors,
                            groupedMonitors = grouped,
                            isRefreshing = false
                        )
                    }
                } else {
                    _uiState.update { it.copy(isRefreshing = false) }
                }
            }
        }
    }

    fun onGroupSelected(groupDisplay: HomeGroupDisplay) {
        val (filtered, grouped) = getFilteredAndGroupedMonitors(groupDisplay.group.id)

        _uiState.update {
            it.copy(
                selectedGroup = groupDisplay,
                homeName = groupDisplay.displayName,
                monitors = filtered,
                groupedMonitors = grouped
            )
        }

        GlobalData.preferences.edit { putInt(AppKeys.GROUP_ID, groupDisplay.group.id) }
        refreshMonitors()
    }

    fun onMonitorSelected(item: MonitorDisplayItem) {
        _uiState.update { it.copy(selectedMonitor = item, currentLevel = 1) }
        fetchEventsForMonitor(item)
    }

    fun navigateBack() {
        if (_uiState.value.currentLevel > 0) {
            _uiState.update { it.copy(currentLevel = it.currentLevel - 1, selectedMonitor = null) }
            _monitorEvents.value = emptyList()
        }
    }

    private fun loadInitialState() {
        val groups = repository.groupList
        val savedGroupId = GlobalData.preferences.getInt(AppKeys.GROUP_ID, -1)
        val selected = groups.find { it.group.id == savedGroupId } ?: groups.firstOrNull()

        if (selected != null) {
            val (filtered, grouped) = getFilteredAndGroupedMonitors(selected.group.id)

            _uiState.update {
                it.copy(
                    displayGroups = groups,
                    selectedGroup = selected,
                    homeName = selected.displayName,
                    monitors = filtered,
                    groupedMonitors = grouped
                )
            }
        }
    }

    private fun fetchEventsForMonitor(item: MonitorDisplayItem) {
        viewModelScope.launch(dispatcherProvider.io) {
            try {
                val result = eventRepository.getEventByMonitor(
                    monitorID = item.monitor.id,
                    deviceId = item.deviceId,
                    pageNumber = 1
                )

                if (result.isSuccess) {
                    val rawEvents = result.getOrNull()?.data ?: emptyList()
                    val enrichedEvents = rawEvents.map { event ->
                        EventProcessor.enrich(
                            event,
                            repository.deviceList,
                            repository.eventTypeDefs
                        )
                    }
                    eventDao.insertEvents(enrichedEvents)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun markEventAsRead(event: EventItem) {
        viewModelScope.launch(dispatcherProvider.io) {
            eventDao.markAsRead(event.id)
            eventRepository.getEventDetail(event.id)
        }
    }

    private fun updateLocalMonitorStatus(item: MonitorDisplayItem, isSecurityEnabled: Boolean) {
        repository.monitorList = repository.monitorList.map {
            if (it.monitor.id == item.monitor.id && it.deviceId == item.deviceId) {
                val newParams = it.monitor.param?.toMutableMap() ?: mutableMapOf()
                newParams["enableCreateEvent"] = isSecurityEnabled
                it.copy(monitor = it.monitor.copy(param = newParams))
            } else it
        }
        val deviceId = item.deviceId
        repository.monitorMap[deviceId] = repository.monitorMap[deviceId]?.map {
            if (it.monitor.id == item.monitor.id) {
                val newParams = it.monitor.param?.toMutableMap() ?: mutableMapOf()
                newParams["enableCreateEvent"] = isSecurityEnabled
                it.copy(monitor = it.monitor.copy(param = newParams))
            } else it
        } ?: emptyList()
        repository.persist()
        _uiState.update { state ->
            val updatedSelected = if (state.selectedMonitor?.monitor?.id == item.monitor.id) {
                val newParams = state.selectedMonitor.monitor.param?.toMutableMap() ?: mutableMapOf()
                newParams["enableCreateEvent"] = isSecurityEnabled
                state.selectedMonitor.copy(monitor = state.selectedMonitor.monitor.copy(param = newParams))
            } else state.selectedMonitor
            val updatedGroups = state.groupedMonitors.mapValues { entry ->
                entry.value.map { m ->
                    if (m.monitor.id == item.monitor.id) {
                        val newParams = m.monitor.param?.toMutableMap() ?: mutableMapOf()
                        newParams["enableCreateEvent"] = isSecurityEnabled
                        m.copy(monitor = m.monitor.copy(param = newParams))
                    } else m
                }
            }.toSortedMap(monitorTypeComparator)
            state.copy(
                selectedMonitor = updatedSelected,
                groupedMonitors = updatedGroups,
                monitorUpdateTicket = state.monitorUpdateTicket + 1
            )
        }
    }

    fun toggleMonitorStatus(item: MonitorDisplayItem, isChecked: Boolean) {
        viewModelScope.launch {
            updateLocalMonitorStatus(item, isChecked)

            try {
                val request = com.tenli.oneview.model.network.MonitorCommandRequest(
                    command = "enable-security",
                    enable = isChecked
                )
                val result = boxRepository.monitorControl(item.deviceUri, item.deviceKey, item.monitor.id.toString(), request)
                if (result.isSuccess) {
                    _uiState.update { it.copy(monitorUpdateTicket = it.monitorUpdateTicket + 1) }
                } else {
                    updateLocalMonitorStatus(item, !isChecked)
                }
            } catch (e: Exception) {
                updateLocalMonitorStatus(item, !isChecked)
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = checkNotNull(this[APPLICATION_KEY]) as com.tenli.oneview.TenliApp
                MonitorViewModel(
                    application = application,
                    boxRepository = application.container.boxRepository,
                    eventRepository = application.container.eventRepository,
                    dispatcherProvider = application.container.dispatcherProvider
                )
            }
        }
    }
}