package com.tenli.oneview.ui.features.home


import android.app.Application
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tenli.oneview.TenliApp
import com.tenli.oneview.data.local.GlobalData
import com.tenli.oneview.data.local.UserSession
import com.tenli.oneview.data.local.db.AppDatabase
import com.tenli.oneview.data.mapper.EventProcessor
import com.tenli.oneview.data.repository.AppRepository
import com.tenli.oneview.data.repository.BoxRepository
import com.tenli.oneview.data.repository.DataRepository
import com.tenli.oneview.data.repository.EventRepository
import com.tenli.oneview.model.network.DeviceItem
import com.tenli.oneview.model.network.EventItem
import com.tenli.oneview.model.network.HomeGroupDisplay
import com.tenli.oneview.model.network.getDisplayTitle
import com.tenli.oneview.ui.utils.AppKeys
import com.tenli.oneview.util.DispatcherProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    application: Application,
    private val appRepository: AppRepository,
    private val eventRepository: EventRepository,
    private val boxRepository: BoxRepository,
    private val dispatcherProvider: DispatcherProvider
) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val eventDao = database.eventDao()
    private val repository = DataRepository
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        repository.restore()
        val currentUserId = UserSession.userData?.id ?: -1
        _uiState.update { it.copy(userId = currentUserId) }

        uiState
            .map { it.currentDeviceIds } // Theo dõi ID của Group đang chọn
            .distinctUntilChanged() // Chỉ chạy tiếp nếu ID thực sự thay đổi
            .flatMapLatest { deviceIds ->
                if (deviceIds.isEmpty()) return@flatMapLatest flowOf(emptyList<EventItem>())
                eventDao.getEventsByDevicesFlow(deviceIds)
            }
            .onEach { allEvents ->
                val recent = allEvents.take(5)
                val grouped = allEvents
                    .groupBy { event ->
                        repository.eventTypeDefs.find { it.key == event.eType }
                            ?.display?.getLocalText() ?: "Sự kiện khác"
                    }
                    .mapValues { it.value.take(3) }

                _uiState.update {
                    it.copy(
                        recentEvents = recent,
                        groupedEvents = grouped
                    )
                }
            }
            .launchIn(viewModelScope)

        loadCachedData()
        refreshData()
    }

    fun refreshData(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            if (!repository.hasDefines() || forceRefresh) {
                fetchEventDefines()
            }

            if (repository.groupList.isEmpty() || forceRefresh) {
                fetchGroups()
            } else {
                _uiState.update { it.copy(displayGroups = repository.groupList) }
            }

            val selectedId = uiState.value.selectedGroup?.group?.id
            if (selectedId != null) {
                val detailJob = async { fetchGroupDetail(selectedId) }

                if (!repository.hasDevices() || forceRefresh) {
                    fetchDevices()
                } else {
                    updateUiWithCachedDevices()
                }
                fetchRecentEvents()
                fetchGroupEvents()

                detailJob.await()
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

//    private suspend fun fetchDevices() {
//        val currentGroupId = uiState.value.selectedGroup?.group?.id ?: return
//        try {
//            val response = CloudAuthClient.create(DeviceApi::class.java).getListDevice()
//            if (response.isSuccessful) {
//                repository.deviceList = response.body()?.data ?: emptyList()
//                repository.persist()
//
//                updateUiWithCachedDevices()
//                val filteredDevices = repository.deviceList.filter { it.userGroupId == currentGroupId }
//                fetchScriptsForGroup(filteredDevices)
//            }
//        } catch (_: Exception) {
//        }
//    }

    private suspend fun fetchDevices() {
        try {
            val result = appRepository.getListDevice()
            if (result.isSuccess) {
                val newData = result.getOrNull() ?: emptyList()
                val currentGroupId = uiState.value.selectedGroup?.group?.id

                val currentList = repository.deviceList.toMutableList()

                // 1. Xóa những máy cũ THUỘC NHÀ HIỆN TẠI đang có trong Cache [cite: 2026-03-17]
                // Điều này đảm bảo nếu máy bị xóa trên server thì trong app cũng mất
                if (currentGroupId != null) {
                    currentList.removeAll { it.userGroupId == currentGroupId }
                }

                // 2. Thêm danh sách mới tải về vào (Cập nhật lại nhà hiện tại)
                currentList.addAll(newData)

                repository.deviceList = currentList
                repository.persist()
                updateUiWithCachedDevices()

                // 3. Load kịch bản chỉ cho những máy vừa tải về
                fetchScriptsForGroup(newData)
            }
        } catch (_: Exception) {}
    }

    private fun updateUiWithCachedDevices() {
        val currentGroupId = uiState.value.selectedGroup?.group?.id ?: return
        val filtered = repository.deviceList.filter { it.userGroupId == currentGroupId }
        val deviceIds = filtered.map { it.id }

        _uiState.update {
            it.copy(
                aiBoxCount = filtered.size,
                iotCount = filtered.sumOf { it.iotCount },
                currentDeviceIds = deviceIds // Đẩy vào State để kích hoạt Flow ở trên
            )
        }
    }

    private suspend fun fetchGroups() {
        try {
            val result = appRepository.getListGroup()
            if (result.isSuccess) {
                val rawGroups = result.getOrNull() ?: emptyList()
                val currentUserId = _uiState.value.userId
                val uiGroups = rawGroups.map { group ->
                    HomeGroupDisplay(group = group, displayName = group.getDisplayTitle(currentUserId))
                }
                repository.groupList = uiGroups
                repository.persist()
                val savedGroupId = GlobalData.preferences.getInt(AppKeys.GROUP_ID, -1)
                val selectedUiGroup = uiGroups.find { it.group.id == savedGroupId }
                    ?: uiGroups.find { it.group.userRequestRole == "owner" }
                    ?: uiGroups.firstOrNull()

                _uiState.update {
                    it.copy(
                        displayGroups = uiGroups,
                        selectedGroup = selectedUiGroup,
                        homeName = selectedUiGroup?.displayName ?: "Chưa có nhà"
                    )
                }
            }
        } catch (_: Exception) {
        }
    }

    private suspend fun fetchEventDefines() {
        try {
            val result = appRepository.getEventDefines()
            if (result.isSuccess) {
                val (types, groups) = result.getOrNull()!!
                repository.eventTypeDefs = types
                repository.eventGroupDefs = groups
                repository.persist()
            }
        } catch (_: Exception) {
        }
    }

    private suspend fun fetchRecentEvents() {
        val currentGroupId = uiState.value.selectedGroup?.group?.id ?: return
        val devicesInGroup = repository.deviceList.filter { it.userGroupId == currentGroupId }

        if (devicesInGroup.isEmpty()) return
        _uiState.update { it.copy(isRecentEventLoading = true) }
        try {
            val allEventsDeferred = devicesInGroup.map { device ->
                viewModelScope.async(dispatcherProvider.io) {
                    try {
                        val result = eventRepository.getRecentEvent(
                            deviceId = device.id,
                            pageSize = 5
                        )
                        result.getOrNull()?.data ?: emptyList()
                    } catch (_: Exception) {
                        emptyList()
                    }
                }
            }

            val combinedEvents = allEventsDeferred.awaitAll().flatten()
                .sortedByDescending { it.eTimestamp }
                .take(10)

            val mappedEvents = combinedEvents.map { event ->
                EventProcessor.enrich(event, repository.deviceList, repository.eventTypeDefs)
            }
            eventDao.insertEvents(mappedEvents)

            _uiState.update { it.copy(isRecentEventLoading = false) }
        } catch (_: Exception) {
            _uiState.update { it.copy(isRecentEventLoading = false) }
        }
    }

    private suspend fun fetchGroupDetail(groupId: Int) {
        try {
            val result = appRepository.getGroupDetail(groupId)
            if (result.isSuccess) {
                val users = result.getOrNull()?.users ?: emptyList()
                _uiState.update {
                    it.copy(memberCount = users.size)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun fetchScriptsForGroup(deviceItems: List<DeviceItem>) {
        val aiBoxes = deviceItems.filter { it.publicTargetURI.isNotEmpty() }
        if (aiBoxes.isEmpty()) {
            _uiState.update { it.copy(scriptCount = 0) }
            return
        }

        val scriptsDeferred = aiBoxes.map { device ->
            viewModelScope.async(dispatcherProvider.io) {
                try {
                    val result = boxRepository.getScripts(device.publicTargetURI, device.key)

                    if (result.isSuccess) {
                        val list = result.getOrNull() ?: emptyList()
                        device.id to list // Trả về cặp ID thiết bị và danh sách kịch bản
                    } else {
                        device.id to emptyList()
                    }
                } catch (_: Exception) {
                    device.id to emptyList()
                }
            }
        }

        // 2. Chờ tất cả các request hoàn thành
        val results = scriptsDeferred.awaitAll()

        // 3. Cập nhật vào Repository và lưu xuống bộ nhớ tạm (Cache) [cite: 2026-03-16]
        results.forEach { (deviceId, scripts) ->
            repository.scriptMap[deviceId] = scripts
        }
        repository.persist()

        // 4. Tính tổng số lượng kịch bản của Group để hiển thị lên UI Home
        val totalScripts = results.sumOf { it.second.size }
        _uiState.update { it.copy(scriptCount = totalScripts) }
    }

    fun onGroupSelected(groupDisplay: HomeGroupDisplay) {
        _uiState.update {
            it.copy(
                selectedGroup = groupDisplay,
                homeName = groupDisplay.displayName,
                aiBoxCount = 0,
                memberCount = 0,
                iotCount = 0,
                scriptCount = 0,
                recentEvents = emptyList()
            )
        }
        GlobalData.preferences.edit {
            putInt(AppKeys.GROUP_ID, groupDisplay.group.id)
        }

        viewModelScope.launch {
            launch { fetchGroupDetail(groupDisplay.group.id) }
            fetchDevices()
            fetchRecentEvents()
            fetchGroupEvents()
        }
    }

    private suspend fun fetchGroupEvents() {
        val currentGroupId = uiState.value.selectedGroup?.group?.id ?: return
        val devicesInGroup = repository.deviceList.filter { it.userGroupId == currentGroupId }
        val validGroups = ArrayList(
            repository.eventGroupDefs
                .map { it.key }
                .filter { it != "user-info" && it != "device-info" }
        )
        if (devicesInGroup.isEmpty() || validGroups.isEmpty()) return
        _uiState.update { it.copy(isGroupEventLoading = true) }

        try {
            val allEventsDeferred = devicesInGroup.map { device ->
                viewModelScope.async(dispatcherProvider.io) {
                    try {
                        val result = eventRepository.getEventByDeviceID(eType = validGroups, deviceId = device.id)
                        result.getOrNull()?.data?.map { it.copy(deviceId = device.id) } ?: emptyList()
                    } catch (e: Exception) {
                        emptyList()
                    }
                }
            }

            val results = allEventsDeferred.awaitAll().flatten()
            val enrichedEvents = results.map { event ->
                EventProcessor.enrich(event, repository.deviceList, repository.eventTypeDefs)
            }
            eventDao.insertEvents(enrichedEvents)
            eventDao.deleteOldEvents()

            _uiState.update { it.copy(isGroupEventLoading = false) }
        } catch (e: Exception) {
            _uiState.update { it.copy(isGroupEventLoading = false) }
        }
    }

    fun markEventAsRead(event: EventItem) {
        viewModelScope.launch(dispatcherProvider.io) {
            eventDao.markAsRead(event.id)
            eventRepository.getEventDetail(event.id)
        }
    }

    private fun loadCachedData() {
        val currentGroupId = GlobalData.preferences.getInt(AppKeys.GROUP_ID, -1)
        val selectedGroup = repository.groupList.find { it.group.id == currentGroupId } ?: repository.groupList.firstOrNull()

        _uiState.update {
            it.copy(
                displayGroups = repository.groupList,
                selectedGroup = selectedGroup,
                homeName = selectedGroup?.displayName ?: "Chưa có nhà",
                aiBoxCount = repository.deviceList.filter { it.userGroupId == currentGroupId }.size,
                iotCount = repository.deviceList.filter { it.userGroupId == currentGroupId }.sumOf { it.iotCount }
            )
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as TenliApp)
                HomeViewModel(
                    application = application,
                    appRepository = application.container.appRepository,
                    eventRepository = application.container.eventRepository,
                    boxRepository = application.container.boxRepository,
                    dispatcherProvider = application.container.dispatcherProvider
                )
            }
        }
    }
}