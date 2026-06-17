package com.tenli.aiot.data.repository

import com.tenli.aiot.data.network.api.BoxApi
import com.tenli.aiot.data.network.retrofit.BoxAuthClient
import com.tenli.aiot.model.network.BoxSystemInfo
import com.tenli.aiot.model.network.BoxSystemState
import com.tenli.aiot.model.network.CameraItem
import com.tenli.aiot.model.network.Monitor
import com.tenli.aiot.model.network.MonitorType
import com.tenli.aiot.model.network.ScriptItem
import com.tenli.aiot.model.network.SystemSettingResponse
import retrofit2.HttpException

class BoxRepository {
    suspend fun getMonitors(deviceDomain: String, deviceToken: String): Result<List<Monitor>> {
        return try {
            val response = BoxAuthClient.create<BoxApi>(
                deviceDomain = deviceDomain,
                deviceToken = deviceToken
            ).getMonitors()

            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Failed to get monitors: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun monitorControl(
        deviceDomain: String,
        deviceToken: String,
        monitorId: String,
        body: Any
    ): Result<Boolean> {
        return try {
            val response = BoxAuthClient.create(BoxApi::class.java, deviceDomain, deviceToken)
                .monitorControl(monitorId, body)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSystemState(domain: String, token: String): Result<BoxSystemState?> {
        return try {
            val response = BoxAuthClient.create(BoxApi::class.java, domain, token).getSystemState()
            if (response.isSuccessful) {
                Result.success(response.body())
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDeviceInformation(domain: String, token: String): Result<BoxSystemInfo?> {
        return try {
            val response = BoxAuthClient.create(BoxApi::class.java, domain, token).getDeviceInformation()
            if (response.isSuccessful) {
                Result.success(response.body())
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun controlDevice(domain: String, token: String, body: Any): Result<Boolean> {
        return try {
            val response = BoxAuthClient.create(BoxApi::class.java, domain, token).controlDevice(body)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSystemSetting(domain: String, token: String): Result<SystemSettingResponse?> {
        return try {
            val response = BoxAuthClient.create(BoxApi::class.java, domain, token).getSystemSetting()
            if (response.isSuccessful) {
                Result.success(response.body())
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateSystemSetting(domain: String, token: String, requestBody: SystemSettingResponse): Result<Boolean> {
        return try {
            val response = BoxAuthClient.create(BoxApi::class.java, domain, token).updateSystemSetting(requestBody)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun configDeviceKey(domain: String, token: String, body: Any): Result<Boolean> {
        return try {
            val response = BoxAuthClient.create(BoxApi::class.java, domain, token).configDeviceKey(body)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCameras(domain: String, token: String): Result<List<CameraItem>> {
        return try {
            val response = BoxAuthClient.create(BoxApi::class.java, domain, token).getCameras()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addCamera(domain: String, token: String, body: Any): Result<Boolean> {
        return try {
            val response = BoxAuthClient.create(BoxApi::class.java, domain, token).addCamera(body)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun editCamera(domain: String, token: String, id: Int, body: Any): Result<Boolean> {
        return try {
            val response = BoxAuthClient.create(BoxApi::class.java, domain, token).editCamera(id, body)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCamera(domain: String, token: String, id: Int): Result<Boolean> {
        return try {
            val response = BoxAuthClient.create(BoxApi::class.java, domain, token).deleteCamera(id)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getScripts(domain: String, token: String): Result<List<ScriptItem>> {
        return try {
            val response = BoxAuthClient.create(BoxApi::class.java, domain, token).getScripts()
            if (response.isSuccessful) {
                Result.success(response.body()?.data ?: emptyList())
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addScript(domain: String, token: String, body: Any): Result<Boolean> {
        return try {
            val response = BoxAuthClient.create(BoxApi::class.java, domain, token).addScript(body)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun editScript(domain: String, token: String, id: String, body: Any): Result<Boolean> {
        return try {
            val response = BoxAuthClient.create(BoxApi::class.java, domain, token).editScript(id, body)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteScript(domain: String, token: String, id: String): Result<Boolean> {
        return try {
            val response = BoxAuthClient.create(BoxApi::class.java, domain, token).deleteScript(id)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMonitorType(domain: String, token: String): Result<List<MonitorType>> {
        return try {
            val response = BoxAuthClient.create(BoxApi::class.java, domain, token).getMonitorType()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addMonitor(domain: String, token: String, body: Any): Result<Boolean> {
        return try {
            val response = BoxAuthClient.create(BoxApi::class.java, domain, token).addMonitor(body)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateMonitor(domain: String, token: String, id: Int, body: Any): Result<Boolean> {
        return try {
            val response = BoxAuthClient.create(BoxApi::class.java, domain, token).updateMonitor(id, body)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteMonitor(domain: String, token: String, id: Int): Result<Boolean> {
        return try {
            val response = BoxAuthClient.create(BoxApi::class.java, domain, token).deleteMonitor(id)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getParamDefault(domain: String, token: String, monitorType: String): Result<Any?> {
        return try {
            val response = BoxAuthClient.create(BoxApi::class.java, domain, token).getParamDefault(monitorType)
            if (response.isSuccessful) {
                Result.success(response.body())
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
