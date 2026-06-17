package com.tenli.aiot.ui.features.setting.core

import androidx.lifecycle.viewModelScope
import com.tenli.aiot.R
import com.tenli.aiot.data.local.UserSession
import com.tenli.aiot.data.repository.DataRepository
import com.tenli.aiot.model.network.GroupUser
import com.tenli.aiot.model.network.HomeGroupDisplay
import com.tenli.aiot.model.network.getDisplayTitle
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

fun SettingViewModel.fetchGroups() {
    val cachedGroups = DataRepository.groupList
    updateGroupState { it.copy(displayGroups = cachedGroups) }

    viewModelScope.launch {
        try {
            val result = appRepository.getListGroup()
            if (result.isSuccess) {
                val rawGroups = result.getOrNull() ?: emptyList()
                val currentUserId = UserSession.userData?.id ?: -1

                val uiGroups = rawGroups.map { group ->
                    HomeGroupDisplay(
                        group = group,
                        displayName = group.getDisplayTitle(currentUserId)
                    )
                }
                if (uiGroups != cachedGroups) {
                    DataRepository.groupList = uiGroups
                    DataRepository.persist()
                    updateGroupState { it.copy(displayGroups = DataRepository.groupList) }
                }
            }
        } catch (_: Exception) {
        }
    }
}

fun SettingViewModel.fetchGroupDetail(groupDisplay: HomeGroupDisplay) {
    _uiState.update {
        it.copy(
            isLoading = true,
            group = it.group.copy(selectedGroup = groupDisplay)
        )
    }

    viewModelScope.launch {
        try {
            val result = appRepository.getGroupDetail(groupDisplay.group.id)
            if (result.isSuccess) {
                updateGroupState { it.copy(groupDetail = result.getOrNull()) }
            } else {
                showSnackbar(getApplication<android.app.Application>().getString(R.string.msg_cannot_fetch_group_info))
            }
        } catch (_: Exception) {
            showSnackbar(getApplication<android.app.Application>().getString(R.string.msg_cannot_fetch_group_detail))
        } finally {
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}

fun SettingViewModel.onGroupFieldChange(name: String? = null, code: String? = null) {
    updateGroupState {
        it.copy(
            tempGroupName = name ?: it.tempGroupName,
            tempJoinCode = code ?: it.tempJoinCode
        )
    }
}

fun SettingViewModel.createGroup(onSuccess: () -> Unit) {
    val name = uiState.value.group.tempGroupName
    if (name.isBlank()) return

    viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true) }
        try {
            val result = appRepository.createGroup(name)
            if (result.isSuccess) {
                showSnackbar(getApplication<android.app.Application>().getString(R.string.msg_create_group_success))
                fetchGroups()
                onSuccess()
            } else {
                showSnackbar(getApplication<android.app.Application>().getString(R.string.msg_create_group_failed))
            }
        } catch (e: Exception) {
            showSnackbar(getApplication<android.app.Application>().getString(R.string.msg_connection_error))
        } finally {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    group = it.group.copy(tempGroupName = "")
                )
            }
        }
    }
}

fun SettingViewModel.joinGroup(onSuccess: () -> Unit) {
    val code = uiState.value.group.tempJoinCode
    if (code.length < 6) return

    viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true) }
        try {
            val result = appRepository.joinGroup(code)
            if (result.isSuccess) {
                showSnackbar(getApplication<android.app.Application>().getString(R.string.msg_join_group_success))
                fetchGroups()
                onSuccess()
            } else {
                showSnackbar(getApplication<android.app.Application>().getString(R.string.msg_invalid_or_expired_code))
            }
        } catch (e: Exception) {
            showSnackbar("${getApplication<android.app.Application>().getString(R.string.msg_error_prefix)}: ${e.message}")
        } finally {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    group = it.group.copy(tempJoinCode = "")
                )
            }
        }
    }
}

fun SettingViewModel.updateGroupName(onSuccess: () -> Unit) {
    val currentSelected = uiState.value.group.selectedGroup ?: return
    val newName = uiState.value.group.tempGroupName
    if (newName.isBlank()) return

    viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true) }
        try {
            val request = com.tenli.aiot.model.network.EditGroupRequest(name = newName)
            val result = appRepository.editGroup(currentSelected.group.id, request)

            if (result.isSuccess) {
                val updatedGroupDisplay = currentSelected.copy(displayName = newName)

                // Cập nhật lại tiêu đề trong Stack điều hướng
                val lastIdx = navigationStack.indexOfLast { it.first == SettingScreenType.GroupDetail }
                if (lastIdx != -1) {
                    navigationStack[lastIdx] = navigationStack[lastIdx].copy(second = newName)
                }

                _uiState.update {
                    it.copy(
                        title = newName,
                        group = it.group.copy(selectedGroup = updatedGroupDisplay)
                    )
                }
                showSnackbar(getApplication<android.app.Application>().getString(R.string.msg_update_group_name_success))
                fetchGroups()
                onSuccess()
            }
        } catch (e: Exception) {
            showSnackbar("${getApplication<android.app.Application>().getString(R.string.msg_error_prefix)}: ${e.message}")
        } finally {
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}

fun SettingViewModel.createShareCode(role: String) {
    val state = uiState.value.group
    val groupId = state.selectedGroup?.group?.id ?: return

    viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true) }
        try {
            val limitTime = state.selectedExpiryMinutes ?: 60
            val result = appRepository.createShareCode(groupId, role, limitTime)
            if (result.isSuccess) {
                val shareCodeData = result.getOrNull()
                if (shareCodeData != null && shareCodeData.code.isNotEmpty()) {
                    updateGroupState {
                        it.copy(
                            shareCode = shareCodeData.code,
                            shareCodeExpireTime = limitTime * 60,
                            isShareCodeDialogOpen = true
                        )
                    }
                    startShareCodeTimer()
                }
            }
        } catch (e: Exception) {
            showSnackbar("${getApplication<android.app.Application>().getString(R.string.msg_create_code_error)}: ${e.message}")
        } finally {
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}

internal fun SettingViewModel.startShareCodeTimer() {
    timerJob?.cancel()
    timerJob = viewModelScope.launch {
        while (uiState.value.group.shareCodeExpireTime > 0) {
            delay(1000)
            updateGroupState { it.copy(shareCodeExpireTime = it.shareCodeExpireTime - 1) }
        }
        updateGroupState { it.copy(isShareCodeDialogOpen = false) }
    }
}

fun SettingViewModel.closeShareCodeDialog() {
    timerJob?.cancel()
    updateGroupState { it.copy(isShareCodeDialogOpen = false) }
}

fun SettingViewModel.removeMember(userId: Int) {
    val groupId = uiState.value.group.selectedGroup?.group?.id ?: return

    viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true) }
        try {
            val result = appRepository.deleteUser(groupId, userId)
            if (result.isSuccess) {
                showSnackbar(getApplication<android.app.Application>().getString(R.string.msg_remove_member_success))
                fetchGroupDetail(uiState.value.group.selectedGroup!!)
            }
        } catch (e: Exception) {
            showSnackbar(getApplication<android.app.Application>().getString(R.string.msg_remove_member_error))
        } finally {
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}

fun SettingViewModel.confirmDeleteMember() {
    val member = uiState.value.group.memberToDelete ?: return
    removeMember(member.id)
    updateGroupState { it.copy(isConfirmDeleteMemberOpen = false, memberToDelete = null) }
}

fun SettingViewModel.toggleGroupMenu(show: Boolean) {
    updateGroupState { it.copy(isGroupMenuExpanded = show) }
}

fun SettingViewModel.prepareEditGroupName() {
    val currentSelected = uiState.value.group.selectedGroup
    val currentName = currentSelected?.displayName ?: ""
    updateGroupState { it.copy(tempGroupName = currentName) }
}

fun SettingViewModel.toggleMemberMenu(show: Boolean) {
    updateGroupState { it.copy(isMemberMenuExpanded = show) }
}

fun SettingViewModel.setExpiryTime(minutes: Int?) {
    updateGroupState { it.copy(selectedExpiryMinutes = minutes) }
}

fun SettingViewModel.showConfirmDeleteMember(member: GroupUser?) {
    updateGroupState {
        it.copy(
            isConfirmDeleteMemberOpen = member != null,
            memberToDelete = member
        )
    }
}

fun SettingViewModel.handleGroupAction(onSuccess: () -> Unit) {
    val group = uiState.value.group.selectedGroup?.group ?: return
    val isOwner = group.userRequestRole == "owner"

    viewModelScope.launch {
        updateAccountState { it.copy(isUpdating = true) }
        try {
            val result = if (isOwner) {
                appRepository.deleteGroup(group.id)
            } else {
                appRepository.leaveGroup(group.id)
            }
            if (result.isSuccess) {
                showSnackbar(if (isOwner) getApplication<android.app.Application>().getString(R.string.msg_delete_group_success) else getApplication<android.app.Application>().getString(R.string.msg_leave_group_success))
                fetchGroups()
                onSuccess()
            } else {
                showSnackbar(getApplication<android.app.Application>().getString(R.string.msg_operation_failed))
            }
        } catch (e: Exception) {
            showSnackbar("${getApplication<android.app.Application>().getString(R.string.msg_error_prefix)}: ${e.message}")
        } finally {
            updateAccountState { it.copy(isUpdating = false) }
        }
    }
}

fun SettingViewModel.toggleGroupActionConfirm(isOpen: Boolean) {
    _uiState.update { it.copy(group = it.group.copy(isConfirmGroupActionOpen = isOpen)) }
}