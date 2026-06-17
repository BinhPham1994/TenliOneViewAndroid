package com.tenli.oneview.ui.features.setting.core

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.tenli.oneview.R
import com.tenli.oneview.data.local.GlobalData
import com.tenli.oneview.data.local.UserSession
import com.tenli.oneview.model.network.ClientDevice
import com.tenli.oneview.model.network.UpdateUserRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * Extension xử lý các tác vụ liên quan đến Tài khoản & Bảo mật
 */

// --- 1. QUẢN LÝ THÔNG TIN CÁ NHÂN ---

fun SettingViewModel.prepareEditProfile() {
    val user = UserSession.userData ?: return
    updateAccountState { currentAccount ->
        currentAccount.copy(
            tempName = user.name,
            tempEmail = user.email,
            tempPhone = user.phoneNumber ?: "",
            tempGender = user.gender,
            tempAddress = user.address ?: ""
        )
    }
}

fun SettingViewModel.onAccountFieldChange(
    name: String? = null,
    email: String? = null,
    phone: String? = null,
    address: String? = null
) {
    updateAccountState { current ->
        current.copy(
            tempName = name ?: current.tempName,
            tempEmail = email ?: current.tempEmail,
            tempPhone = phone ?: current.tempPhone,
            tempAddress = address ?: current.tempAddress
        )
    }
}

fun SettingViewModel.onGenderSelect(gender: Int) {
    updateAccountState { it.copy(tempGender = gender) }
}

fun SettingViewModel.updateUserProfile(onSuccess: () -> Unit) {
    viewModelScope.launch {
        updateAccountState { it.copy(isUpdating = true) }

        val accountState = uiState.value.account
        val request = UpdateUserRequest(
            name = accountState.tempName,
            gender = accountState.tempGender,
            address = accountState.tempAddress,
            phoneNumber1 = accountState.tempPhone,
            email = accountState.tempEmail
        )

        try {
            val result = userRepository.updateUser(request)

            if (result.isSuccess) {
                // Cập nhật dữ liệu vào Session cục bộ
                UserSession.userData?.apply {
                    name = accountState.tempName
                    gender = accountState.tempGender
                    address = accountState.tempAddress
                    phoneNumber = accountState.tempPhone
                }
                UserSession.saveSession(GlobalData.preferences)
                refreshUserData()
                showSnackbar(getApplication<android.app.Application>().getString(R.string.msg_update_info_success))
                onSuccess()
            } else {
                showSnackbar("${getApplication<android.app.Application>().getString(R.string.msg_update_failed)}: ${result.exceptionOrNull()?.message}")
            }
        } catch (e: Exception) {
            showSnackbar("${getApplication<android.app.Application>().getString(R.string.msg_connection_error)}: ${e.localizedMessage}")
        } finally {
            updateAccountState { it.copy(isUpdating = false) }
        }
    }
}

// --- 2. QUẢN LÝ AVATAR ---

fun SettingViewModel.uploadAvatar(uri: android.net.Uri) {
    viewModelScope.launch {
        updateAccountState { it.copy(isUpdating = true) }
        try {
            val context = getApplication<Application>()
            val inputStream = context.contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes() ?: return@launch

            val requestFile = bytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("image", "avatar.jpg", requestFile)

            val result = userRepository.updateAvatar(body)

            if (result.isSuccess) {
                val newUserData = result.getOrNull()
                if (newUserData != null) {
                    UserSession.userData = newUserData
                    GlobalData.preferences?.edit()
                        ?.putString("tenli_user_data", Gson().toJson(newUserData))
                        ?.apply()
                    refreshUserData()
                }
                showSnackbar(getApplication<android.app.Application>().getString(R.string.msg_update_avatar_success))
            } else {
                showSnackbar("${getApplication<android.app.Application>().getString(R.string.msg_upload_image_failed)}: ${result.exceptionOrNull()?.message}")
            }
        } catch (e: Exception) {
            showSnackbar("${getApplication<android.app.Application>().getString(R.string.msg_error_prefix)}: ${e.localizedMessage}")
        } finally {
            updateAccountState { it.copy(isUpdating = false) }
        }
    }
}

// --- 3. QUẢN LÝ BẢO MẬT (MẬT KHẨU) ---

fun SettingViewModel.onPasswordChange(
    old: String? = null,
    new: String? = null,
    confirm: String? = null
) {
    updateAccountState { current ->
        current.copy(
            oldPassword = old ?: current.oldPassword,
            newPassword = new ?: current.newPassword,
            confirmPassword = confirm ?: current.confirmPassword
        )
    }
}


fun SettingViewModel.changePassword(onSuccess: () -> Unit) {
    val accountState = uiState.value.account

    // Kiểm tra nhanh phía Client
    if (accountState.newPassword != accountState.confirmPassword) {
        showSnackbar(getApplication<android.app.Application>().getString(R.string.msg_password_mismatch))
        return
    }
    if (accountState.newPassword.length < 6) {
        showSnackbar(getApplication<android.app.Application>().getString(R.string.msg_password_too_short))
        return
    }

    viewModelScope.launch {
        updateAccountState { it.copy(isChangingPassword = true) }
        try {
            val result = userRepository.changePassword(
                accountState.oldPassword,
                accountState.newPassword
            )

            if (result.isSuccess) {
                showSnackbar(getApplication<android.app.Application>().getString(R.string.msg_change_password_success))
                // Xóa dữ liệu cục bộ và yêu cầu đăng nhập lại
                // Hàm clearLocalData() phải để internal trong file ViewModel chính
                clearLocalData()
                delay(1500)
                withContext(dispatcherProvider.main) { onSuccess() }
            } else {
                val errorMsg = result.exceptionOrNull()?.message?.let {
                    try {
                        com.google.gson.Gson().fromJson(it, com.google.gson.JsonObject::class.java).get("message").asString
                    } catch (_: Exception) {
                        "Đổi mật khẩu thất bại"
                    }
                } ?: "Đổi mật khẩu thất bại"
                showSnackbar(errorMsg)
            }
        } catch (e: Exception) {
            showSnackbar("${getApplication<android.app.Application>().getString(R.string.msg_connection_error)}: ${e.message}")
        } finally {
            updateAccountState { it.copy(isChangingPassword = false) }
        }
    }
}

// --- 4. BỔ SUNG: HELPER & VALIDATION ---

/**
 * Reset các trường mật khẩu về trống trước khi mở màn hình Đổi mật khẩu
 */
fun SettingViewModel.prepareChangePassword() {
    updateAccountState { it.copy(
        oldPassword = "",
        newPassword = "",
        confirmPassword = ""
    ) }
}

/**
 * Dọn dẹp toàn bộ dữ liệu tạm trong ngăn Account
 * (Nên gọi khi thoát khỏi module Cài đặt để tránh rác dữ liệu)
 */
fun SettingViewModel.clearAccountTempData() {
    updateAccountState { AccountUiState() } // Trả về trạng thái khởi tạo ban đầu
}

fun SettingViewModel.fetchClientDevices() {
    viewModelScope.launch {
        updateDeviceState { it.copy(isLoadingDevices = true) }

        try {
            val result = userRepository.getClients()

            if (result.isSuccess) {
                val freshClients = result.getOrNull() ?: emptyList()
                updateDeviceState {
                    it.copy(clientDevices = freshClients, isLoadingDevices = false)
                }
            } else {
                showSnackbar(getApplication<android.app.Application>().getString(R.string.msg_cannot_fetch_devices))
                updateDeviceState { it.copy(isLoadingDevices = false) }
            }
        } catch (e: Exception) {
            showSnackbar("${getApplication<android.app.Application>().getString(R.string.msg_connection_error)}: ${e.localizedMessage}")
            updateDeviceState { it.copy(isLoadingDevices = false) }
        }
    }
}

fun SettingViewModel.viewClientDetail(device: ClientDevice) {
    updateDeviceState { it.copy(selectedDevice = device) }
    navigateTo(SettingScreenType.ClientDetail, "Thông tin thiết bị")
}

fun SettingViewModel.logoutRemoteClient(deviceId: Int) {
    viewModelScope.launch {
        updateDeviceState { it.copy(isLoggingOutDevice = true) }

        try {
            val result = userRepository.logoutClient(deviceId)
            if (result.isSuccess) {
                showSnackbar(getApplication<android.app.Application>().getString(R.string.msg_logout_device_success))
                fetchClientDevices()
                navigateBack()
            } else {
                showSnackbar(getApplication<android.app.Application>().getString(R.string.msg_logout_device_failed))
            }
        } catch (e: Exception) {
            showSnackbar("${getApplication<android.app.Application>().getString(R.string.msg_system_error)}: ${e.message}")
        } finally {
            updateDeviceState { it.copy(isLoggingOutDevice = false) }
        }
    }
}
