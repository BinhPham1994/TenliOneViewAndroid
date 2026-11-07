package com.tenli.oneview.data.local

import android.util.Log
import androidx.compose.runtime.Immutable
import com.tenli.oneview.data.network.ApiProvider
import com.tenli.oneview.model.network.CameraModel
import com.tenli.oneview.model.network.EventData
import com.tenli.oneview.model.network.UserData
import com.tenli.oneview.model.network.UserPermissions
import com.tenli.oneview.model.network.VmsUserModel
import com.tenli.oneview.ui.features.event.cameraUuidOrNull
import com.tenli.oneview.ui.utils.AiModuleOption
import com.tenli.oneview.ui.utils.AiTypeHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicLong

@Immutable
data class UserPermissionState(
    val isAdmin: Boolean = true,
    val permissions: UserPermissions? = null,
    val canAccessLiveStream: Boolean = true,
    val canAccessPlayback: Boolean = true,
    val canAccessEvents: Boolean = true,
    val canAccessSettings: Boolean = true,
    val activeAiModules: List<AiModuleOption> = AiTypeHelper.defaultModules,
    val version: Long = 0L
)

object UserPermissionManager {
    private const val TAG = "UserPermissionManager"
    private val versionCounter = AtomicLong(0L)

    private val _permissionState = MutableStateFlow(UserPermissionState())
    val permissionState: StateFlow<UserPermissionState> = _permissionState.asStateFlow()

    /**
     * Checks if the current session is an older persisted session where neither isAdmin nor permissions
     * has been resolved from `/VMS/api/User/{id}` yet.
     */
    private fun isUnresolvedLegacySession(userData: UserData?): Boolean {
        if (userData == null) return true
        return userData.isAdmin == null && userData.permissions == null
    }

    private fun resolveUsername(userData: UserData?): String {
        if (userData == null) return ""
        return (userData.username?.takeIf { it.isNotBlank() }
            ?: userData.email.takeIf { it.isNotBlank() }
            ?: userData.name).trim()
    }

    fun isAdmin(userData: UserData? = UserSession.userData): Boolean {
        if (userData == null) return true
        val uname = resolveUsername(userData)
        if (isUnresolvedLegacySession(userData)) {
            return uname == "admin" || userData.isAdmin == null
        }
        return userData.isAdmin == true && uname == "admin"
    }

    fun currentPermissions(userData: UserData? = UserSession.userData): UserPermissions? {
        return userData?.permissions
    }

    fun canAccessLiveStream(userData: UserData? = UserSession.userData): Boolean {
        if (isAdmin(userData)) return true
        val perms = currentPermissions(userData) ?: return false
        if (perms.visualMonitoring?.enabled == false) return false
        if (perms.visualMonitoring?.features?.monitoring == false) return false
        if (perms.cameras?.mode == "custom") {
            return !perms.cameras.list.isNullOrEmpty()
        }
        return true
    }

    fun canAccessPlayback(userData: UserData? = UserSession.userData): Boolean {
        if (isAdmin(userData)) return true
        val perms = currentPermissions(userData) ?: return false
        if (perms.visualMonitoring?.enabled == false) return false
        val playbackFlag = perms.visualMonitoring?.features?.playback
        if (playbackFlag != null) {
            return playbackFlag
        }
        return true
    }

    fun canAccessEvents(userData: UserData? = UserSession.userData): Boolean {
        if (isAdmin(userData)) return true
        val perms = currentPermissions(userData) ?: return false
        if (perms.aiEvents?.mode == "custom" || perms.events?.mode == "custom") {
            val list = perms.aiEvents?.list ?: perms.events?.list
            return !list.isNullOrEmpty()
        }
        return true
    }

    fun canAccessSettings(userData: UserData? = UserSession.userData): Boolean {
        if (isAdmin(userData)) return true
        val perms = currentPermissions(userData) ?: return false
        return perms.allowSettings == true
    }

    fun isCustomCameraPermission(userData: UserData? = UserSession.userData): Boolean {
        if (isAdmin(userData)) return false
        val perms = currentPermissions(userData) ?: return false
        return perms.cameras?.mode == "custom" || perms.visualMonitoring?.enabled == false
    }

    fun isCustomAiEventPermission(userData: UserData? = UserSession.userData): Boolean {
        if (isAdmin(userData)) return false
        val perms = currentPermissions(userData) ?: return false
        return perms.aiEvents?.mode == "custom" || perms.events?.mode == "custom"
    }

    fun syncFromUserSession() {
        val user = UserSession.userData
        val admin = isAdmin(user)
        val perms = currentPermissions(user)

        AiTypeHelper.applyPermissions(isAdmin = admin, permissions = perms)

        val nextState = UserPermissionState(
            isAdmin = admin,
            permissions = perms,
            canAccessLiveStream = canAccessLiveStream(user),
            canAccessPlayback = canAccessPlayback(user),
            canAccessEvents = canAccessEvents(user),
            canAccessSettings = canAccessSettings(user),
            activeAiModules = AiTypeHelper.activeModules,
            version = versionCounter.incrementAndGet()
        )
        _permissionState.value = nextState
    }

    fun reset() {
        AiTypeHelper.resetPermissions()
        _permissionState.value = UserPermissionState(
            version = versionCounter.incrementAndGet()
        )
    }

    fun isCameraPermitted(camera: CameraModel, userData: UserData? = UserSession.userData): Boolean {
        if (isAdmin(userData)) return true
        val perms = currentPermissions(userData) ?: return true
        if (perms.visualMonitoring?.enabled == false) return false
        if (perms.cameras?.mode == "custom") {
            val allowedList = perms.cameras.list ?: return false
            if (allowedList.isEmpty()) return false
            val vmsId = camera.vmsId
            return allowedList.any { item ->
                item.vmsId == vmsId && (item.cameraId == camera.id || item.cameraId == camera.cameraId)
            }
        }
        return true
    }

    fun filterPermittedCameras(
        cameras: List<CameraModel>,
        userData: UserData? = UserSession.userData
    ): List<CameraModel> {
        if (isAdmin(userData)) return cameras
        val perms = currentPermissions(userData) ?: return cameras
        if (perms.visualMonitoring?.enabled == false) return emptyList()
        if (perms.cameras?.mode == "custom") {
            val allowedList = perms.cameras.list ?: return emptyList()
            if (allowedList.isEmpty()) return emptyList()
            val allowedKeys = allowedList.map { "${it.vmsId}_${it.cameraId}" }.toSet()
            return cameras.filter { cam ->
                val vmsId = cam.vmsId
                allowedKeys.contains("${vmsId}_${cam.id}") ||
                    allowedKeys.contains("${vmsId}_${cam.cameraId}")
            }
        }
        return cameras
    }

    /**
     * Returns null when all cameras are allowed, or a Set of lowercase camera UUIDs when custom camera
     * permissions (or visualMonitoring disabled) are active.
     */
    fun buildAllowedCameraUuids(
        allCameras: List<CameraModel> = EventCacheManager.getCamerasInMemory(),
        userData: UserData? = UserSession.userData
    ): Set<String>? {
        if (isAdmin(userData)) return null
        val perms = currentPermissions(userData) ?: return null
        val camerasMode = perms.cameras?.mode ?: "all"
        if (camerasMode != "custom") return null

        val allowedCameras = perms.cameras?.list ?: emptyList()
        if (allowedCameras.isEmpty()) return emptySet()

        val allowedUuids = HashSet<String>()
        for (allowed in allowedCameras) {
            val targetVmsId = allowed.vmsId
            val targetCameraId = allowed.cameraId
            val matchedCam = allCameras.find { c ->
                c.vmsId == targetVmsId && (c.id == targetCameraId || c.cameraId == targetCameraId)
            }
            val uuid = matchedCam?.extra?.uuid?.trim()?.lowercase()
            if (!uuid.isNullOrEmpty()) {
                allowedUuids.add(uuid)
            }
        }
        return allowedUuids
    }

    fun isEventPermitted(
        event: EventData,
        allCameras: List<CameraModel> = EventCacheManager.getCamerasInMemory(),
        userData: UserData? = UserSession.userData
    ): Boolean {
        val eventType = event.type.trim()
        if (!AiTypeHelper.isAiTypeEnabled(eventType)) return false
        if (isAdmin(userData)) return true

        val perms = currentPermissions(userData) ?: return true
        val camerasMode = perms.cameras?.mode ?: "all"
        val aiEventsMode = perms.aiEvents?.mode ?: perms.events?.mode ?: "all"

        val allowedAiEvents = perms.aiEvents?.list ?: perms.events?.list ?: emptyList()
        if (aiEventsMode == "custom") {
            if (allowedAiEvents.isEmpty()) return false
            val isTypeAllowed = allowedAiEvents.any { allowedType ->
                val cleanAllowed = allowedType.trim()
                eventType.equals(cleanAllowed, ignoreCase = true) ||
                    AiTypeHelper.getAiEventTypes(cleanAllowed).any { it.equals(eventType, ignoreCase = true) }
            }
            if (!isTypeAllowed) return false
        }

        if (camerasMode == "custom") {
            val allowedUuids = buildAllowedCameraUuids(allCameras, userData) ?: return true
            if (allowedUuids.isEmpty()) return false
            val evUuid = event.cameraUuidOrNull?.trim()?.lowercase()
            if (evUuid.isNullOrEmpty() || !allowedUuids.contains(evUuid)) return false
        }

        return true
    }

    fun filterEventsByPermissions(
        events: List<EventData>,
        allCameras: List<CameraModel> = EventCacheManager.getCamerasInMemory(),
        userData: UserData? = UserSession.userData
    ): List<EventData> {
        if (events.isEmpty()) return emptyList()
        if (isAdmin(userData)) {
            return events.filter { AiTypeHelper.isAiTypeEnabled(it.type) }
        }

        val perms = currentPermissions(userData)
        if (perms == null) {
            return events.filter { AiTypeHelper.isAiTypeEnabled(it.type) }
        }

        val camerasMode = perms.cameras?.mode ?: "all"
        val aiEventsMode = perms.aiEvents?.mode ?: perms.events?.mode ?: "all"
        val allowedCameras = perms.cameras?.list ?: emptyList()
        val allowedAiEvents = perms.aiEvents?.list ?: perms.events?.list ?: emptyList()

        if (camerasMode == "custom" && allowedCameras.isEmpty()) return emptyList()
        if (aiEventsMode == "custom" && allowedAiEvents.isEmpty()) return emptyList()

        val allowedUuids: Set<String>? = if (camerasMode == "custom") {
            val set = buildAllowedCameraUuids(allCameras, userData) ?: emptySet()
            if (set.isEmpty()) return emptyList()
            set
        } else {
            null
        }

        return events.filter { ev ->
            val eventType = ev.type.trim()
            if (!AiTypeHelper.isAiTypeEnabled(eventType)) return@filter false

            if (allowedUuids != null) {
                val evUuid = ev.cameraUuidOrNull?.trim()?.lowercase()
                if (evUuid.isNullOrEmpty() || !allowedUuids.contains(evUuid)) {
                    return@filter false
                }
            }

            if (aiEventsMode == "custom") {
                val isTypeAllowed = allowedAiEvents.any { allowedType ->
                    val cleanAllowed = allowedType.trim()
                    eventType.equals(cleanAllowed, ignoreCase = true) ||
                        AiTypeHelper.getAiEventTypes(cleanAllowed).any { it.equals(eventType, ignoreCase = true) }
                }
                if (!isTypeAllowed) return@filter false
            }

            true
        }
    }

    /**
     * Fetches `/VMS/api/User/{userId}` with 1 retry (1500ms) on 5xx or network error,
     * identical to `fetchAndApplyPermissions` in `tenli-oneview/src/store/auth.ts`.
     */
    suspend fun fetchVmsUserWithRetry(
        domain: String,
        token: String,
        userId: Int
    ): VmsUserModel? {
        val cleanDomain = domain.trimEnd('/')
        if (cleanDomain.isBlank() || token.isBlank() || userId <= 0) return null
        val url = "$cleanDomain/VMS/api/User/$userId"
        val authHeader = "Bearer $token"

        for (attempt in 0..1) {
            try {
                val response = ApiProvider.vmsApi.getUserByIdExt(url = url, authHeader = authHeader)
                if (response.isSuccessful) {
                    return response.body()
                }
                val code = response.code()
                if (attempt == 0 && code >= 500) {
                    delay(1500)
                    continue
                }
                Log.w(TAG, "fetchVmsUserWithRetry HTTP $code for userId=$userId")
                return null
            } catch (e: Exception) {
                if (attempt == 0) {
                    delay(1500)
                    continue
                }
                Log.w(TAG, "fetchVmsUserWithRetry failed for userId=$userId: ${e.message}")
                return null
            }
        }
        return null
    }

    /**
     * Fetches user info & permissions from `/VMS/api/User/{userId}` and applies them to `UserSession`
     * and `AiTypeHelper`.
     */
    suspend fun fetchAndApplyPermissions(
        explicitDomain: String? = null,
        explicitToken: String? = null,
        explicitUserId: Int? = null,
        initialIsAdmin: Boolean? = null,
        fallbackUsername: String? = null
    ): UserPermissions? {
        val currentUser = UserSession.userData
        val domain = (explicitDomain ?: UserSession.domain).trimEnd('/')
        val token = explicitToken ?: UserSession.accessToken
        val userId = explicitUserId ?: currentUser?.id ?: 0

        if (domain.isBlank() || token.isBlank() || userId <= 0) {
            syncFromUserSession()
            return currentUser?.permissions
        }

        val vmsUser = fetchVmsUserWithRetry(domain = domain, token = token, userId = userId)
        if (vmsUser != null) {
            val resolvedUsername = vmsUser.username?.trim()?.takeIf { it.isNotEmpty() }
                ?: fallbackUsername?.trim()?.takeIf { it.isNotEmpty() }
                ?: resolveUsername(currentUser)

            val rawAdminFlag = initialIsAdmin ?: currentUser?.isAdmin ?: (resolvedUsername == "admin")
            val effectiveIsAdmin = rawAdminFlag && resolvedUsername == "admin"
            val resolvedPerms = vmsUser.extra?.permissions

            UserSession.updateUserPermissions(
                username = resolvedUsername,
                fullName = vmsUser.name?.takeIf { it.isNotBlank() },
                isAdmin = effectiveIsAdmin,
                permissions = resolvedPerms
            )
            syncFromUserSession()
            return resolvedPerms
        } else {
            syncFromUserSession()
            return currentUser?.permissions
        }
    }
}
