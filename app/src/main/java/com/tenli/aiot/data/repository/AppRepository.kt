package com.tenli.aiot.data.repository

import com.tenli.aiot.data.network.api.DeviceApi
import com.tenli.aiot.data.network.api.EventApi
import com.tenli.aiot.data.network.api.GroupApi
import com.tenli.aiot.data.network.retrofit.CloudAuthClient
import com.tenli.aiot.model.network.DeviceItem
import com.tenli.aiot.model.network.EventGroupDef
import com.tenli.aiot.model.network.EventTypeDef
import com.tenli.aiot.model.network.GroupDetailData
import com.tenli.aiot.model.network.HomeGroup
import com.tenli.aiot.model.network.ShareCodeData

class AppRepository(private val dataRepository: DataRepository) {
    // We inject the singleton DataRepository to gradually migrate away from it,
    // or just use it inside here.
    
    suspend fun getListDevice(): Result<List<DeviceItem>> {
        return try {
            val response = CloudAuthClient.create(DeviceApi::class.java).getListDevice()
            if (response.isSuccessful) {
                Result.success(response.body()?.data ?: emptyList())
            } else {
                Result.failure(Exception("Failed to fetch devices"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDeviceDetail(deviceId: Int): Result<DeviceItem?> {
        return try {
            val response = CloudAuthClient.create(DeviceApi::class.java).getDeviceDetail(deviceId)
            if (response.isSuccessful) {
                Result.success(response.body()?.data)
            } else {
                Result.failure(Exception("Failed to fetch device detail"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addDevice(name: String, deviceCode: String, key: String, userGroupId: Int): Result<Boolean> {
        return try {
            val response = CloudAuthClient.create(DeviceApi::class.java).addDevice(name, deviceCode, key, userGroupId)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteDevice(deviceId: Int): Result<Boolean> {
        return try {
            val response = CloudAuthClient.create(DeviceApi::class.java).deleteDevice(deviceId)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getListGroup(): Result<List<HomeGroup>> {
        return try {
            val response = CloudAuthClient.create(GroupApi::class.java).getListGroup()
            if (response.isSuccessful) {
                Result.success(response.body()?.data ?: emptyList())
            } else {
                Result.failure(Exception("Failed to fetch groups"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getGroupDetail(groupId: Int): Result<GroupDetailData?> {
        return try {
            val response = CloudAuthClient.create(GroupApi::class.java).getGroupDetail(groupId)
            if (response.isSuccessful) {
                Result.success(response.body()?.data)
            } else {
                Result.failure(Exception("${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createGroup(name: String): Result<Boolean> {
        return try {
            val response = CloudAuthClient.create(GroupApi::class.java).createGroup(name = name)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun joinGroup(shareCode: String): Result<Boolean> {
        return try {
            val response = CloudAuthClient.create(GroupApi::class.java).joinGroup(shareCode = shareCode)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun editGroup(groupId: Int, body: Any): Result<Boolean> {
        return try {
            val response = CloudAuthClient.create(GroupApi::class.java).editGroup(groupId, body)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createShareCode(groupId: Int, role: String, limitTime: Int): Result<ShareCodeData?> {
        return try {
            val response = CloudAuthClient.create(GroupApi::class.java).createShareCode(
                id = groupId,
                role = role,
                expiredIn = limitTime
            )
            if (response.isSuccessful) {
                Result.success(response.body()?.data)
            } else {
                Result.failure(Exception("${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteUser(groupId: Int, userId: Int): Result<Boolean> {
        return try {
            val response = CloudAuthClient.create(GroupApi::class.java).deleteUser(groupId, userId)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteGroup(groupId: Int): Result<Boolean> {
        return try {
            val response = CloudAuthClient.create(GroupApi::class.java).deleteGroup(groupId)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun leaveGroup(groupId: Int): Result<Boolean> {
        return try {
            val response = CloudAuthClient.create(GroupApi::class.java).leaveGroup(groupId)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getEventDefines(): Result<Pair<List<EventTypeDef>, List<EventGroupDef>>> {
        return try {
            val typeResponse = CloudAuthClient.create(EventApi::class.java).getEventType()
            val groupResponse = CloudAuthClient.create(EventApi::class.java).getEventTypeGroup()
            val types = if (typeResponse.isSuccessful) typeResponse.body()?.data ?: emptyList() else emptyList()
            val groups = if (groupResponse.isSuccessful) groupResponse.body()?.data ?: emptyList() else emptyList()
            Result.success(Pair(types, groups))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
