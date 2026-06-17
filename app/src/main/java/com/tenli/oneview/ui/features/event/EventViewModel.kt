package com.tenli.oneview.ui.features.event

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tenli.oneview.TenliApp
import com.tenli.oneview.data.local.db.AppDatabase
import com.tenli.oneview.data.mapper.EventProcessor
import com.tenli.oneview.data.repository.DataRepository
import com.tenli.oneview.data.repository.EventRepository
import com.tenli.oneview.model.network.EventItem
import com.tenli.oneview.util.DispatcherProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EventViewModel(
    application: Application,
    private val eventRepository: EventRepository,
    private val dispatcherProvider: DispatcherProvider
) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val eventDao = database.eventDao()

    private val _uiState = MutableStateFlow(EventUiState())
    val uiState = _uiState.asStateFlow()
    private val repository = DataRepository

    private var currentPage = 1
    private var isLastPage = false

    init {
        repository.restore()
        loadEventsFromCache()
        loadEvents(isRefresh = true)
    }

    private fun loadEventsFromCache() {
        viewModelScope.launch(dispatcherProvider.io) {
            val cached = eventDao.getRecentEvents(20)
            if (cached.isNotEmpty()) {
                _uiState.update { it.copy(events = cached, isDataEmpty = false) }
            }
        }
    }

    fun loadEvents(isRefresh: Boolean) {
        if (isRefresh) {
            currentPage = 1
        } else {
            if (_uiState.value.isPagingLoading) {
                return
            }
        }

        viewModelScope.launch {
            if (isRefresh) _uiState.update { it.copy(isRefreshing = true) }
            else _uiState.update { it.copy(isPagingLoading = true) }

            try {
                val state = _uiState.value
                val hasDateFilter = state.fromTime != null || state.toTime != null
                val isFiltering = state.selectedDeviceIds.isNotEmpty() || state.selectedETypes.isNotEmpty() || hasDateFilter

                val result = if (isFiltering) {
                    val from = state.fromTime ?: state.toTime ?: ""
                    val to = state.toTime ?: state.fromTime ?: ""
                    val isoFrom = if (from.isNotEmpty()) formatToIso8601(from, false) else null
                    val isoTo = if (to.isNotEmpty()) formatToIso8601(to, true) else null
                    eventRepository.getListFilterEvents(
                        pageNumber = currentPage,
                        pageSize = 20,
                        eType = state.selectedETypes.ifEmpty { null },
                        fromTime = isoFrom,
                        toTime = isoTo,
                        deviceId = state.selectedDeviceIds.ifEmpty { null }
                    )
                } else {
                    eventRepository.getListEvents(
                        pageNumber = currentPage,
                        pageSize = 20
                    )
                }

                if (result.isSuccess) {
                    val rawEvents = result.getOrNull()?.data ?: emptyList()
                    val reachedEnd = rawEvents.isEmpty() || rawEvents.size < 20
                    val enriched = rawEvents.map { item ->
                        try {
                            EventProcessor.enrich(item, repository.deviceList, repository.eventTypeDefs)
                        } catch (e: Exception) {
                            item
                        }
                    }

                    viewModelScope.launch(dispatcherProvider.io) {
                        eventDao.insertEvents(enriched)
                        if (isRefresh && !isFiltering) {
                            eventDao.deleteOldEvents()
                        }
                    }

                    _uiState.update { currentState ->
                        val combinedList = if (isRefresh) enriched else currentState.events + enriched
                        val newList = combinedList.distinctBy { it.id }

                        currentState.copy(
                            events = newList,
                            isRefreshing = false,
                            isPagingLoading = false,
                            isDataEmpty = newList.isEmpty(),
                            isEndReached = reachedEnd
                        )
                    }

                    if (!reachedEnd) currentPage++

                } else {
                    _uiState.update { it.copy(isRefreshing = false, isPagingLoading = false) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(isRefreshing = false, isPagingLoading = false) }
            }
        }
    }

    fun openFilter() {
        _uiState.update {
            it.copy(
                currentLevel = 2,
                availableDevices = repository.deviceList,
                availableTypeGroups = repository.eventTypeDefs.groupBy { it.eventTypeGroup }
            )
        }
    }

    // Cập nhật lựa chọn thiết bị
    fun toggleDeviceSelection(deviceId: Int) {
        _uiState.update { state ->
            val newList = if (state.selectedDeviceIds.contains(deviceId)) {
                state.selectedDeviceIds - deviceId
            } else {
                state.selectedDeviceIds + deviceId
            }
            state.copy(selectedDeviceIds = newList)
        }
    }

    fun toggleTypeSelection(typeKey: String) {
        _uiState.update { state ->
            val newList = if (state.selectedETypes.contains(typeKey)) {
                state.selectedETypes - typeKey
            } else {
                state.selectedETypes + typeKey
            }
            state.copy(selectedETypes = newList)
        }
    }

    fun applyFilter() {
        currentPage = 1
        isLastPage = false

        _uiState.update {
            it.copy(
                currentLevel = 0,
                isRefreshing = true,
                events = emptyList() // XÓA SẠCH để không hiện sự kiện cũ ngoài khoảng thời gian [cite: 2026-03-01]
            )
        }
        loadEvents(isRefresh = true)
    }

    fun resetFilter() {
        _uiState.update {
            it.copy(
                selectedDeviceIds = emptyList(),
                selectedETypes = emptyList(),
                fromTime = null,
                toTime = null
            )
        }
        loadEvents(isRefresh = true)
    }

    fun navigateBack() {
        _uiState.update {
            it.copy(
                currentLevel = 0,
                selectedEvent = null
            )
        }
    }

    fun markEventAsRead(event: EventItem) {
        if (!event.isNew) return
        _uiState.update { currentState ->
            val updatedList = currentState.events.map { item ->
                if (item.id == event.id) {
                    item.copy(isNew = false)
                } else {
                    item
                }
            }
            currentState.copy(events = updatedList)
        }
        viewModelScope.launch(dispatcherProvider.io) {
            try {
                eventDao.markAsRead(event.id)
                eventRepository.getEventDetail(event.id)
            } catch (_: Exception) {
            }
        }
    }

    private fun formatToIso8601(date: String, isEndOfDay: Boolean): String {
        val apiDate = convertToApiFormat(date)
        return if (isEndOfDay) {
            "${apiDate}T23:59:59"
        } else {
            "${apiDate}T00:00:00"
        }
    }

    fun updateFromTime(date: String) {
        _uiState.update { it.copy(fromTime = date) }
    }

    fun updateToTime(date: String) {
        _uiState.update { it.copy(toTime = date) }
    }

    private fun convertToApiFormat(dateStr: String): String {
        return try {
            val parts = dateStr.split("-")
            if (parts.size == 3) {
                "${parts[2]}-${parts[1]}-${parts[0]}"
            } else dateStr
        } catch (_: Exception) {
            dateStr
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as TenliApp
                EventViewModel(
                    application = application,
                    eventRepository = application.container.eventRepository,
                    dispatcherProvider = application.container.dispatcherProvider
                )
            }
        }
    }
}