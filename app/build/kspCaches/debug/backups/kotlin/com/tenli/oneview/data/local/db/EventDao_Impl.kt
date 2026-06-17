package com.tenli.oneview.`data`.local.db

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.appendPlaceholders
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.tenli.oneview.model.network.EventFileItem
import com.tenli.oneview.model.network.EventItem
import com.tenli.oneview.model.network.SData
import javax.`annotation`.processing.Generated
import kotlin.Any
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlin.text.StringBuilder
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class EventDao_Impl(
  __db: RoomDatabase,
) : EventDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfEventItem: EntityInsertAdapter<EventItem>

  private val __eventConverters: EventConverters = EventConverters()
  init {
    this.__db = __db
    this.__insertAdapterOfEventItem = object : EntityInsertAdapter<EventItem>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `events` (`id`,`eType`,`eTypeGroup`,`eTime`,`eTimestamp`,`isNew`,`eCameraId`,`deviceId`,`eImages`,`eVideos`,`eProcessId`,`eValues`,`sData`,`fileItems`,`displayTitle`,`timeAgo`,`deviceUri`,`deviceKey`,`aiTitle`,`actionPart`,`cameraName`,`deviceName`,`foundConnector`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: EventItem) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.eType)
        statement.bindText(3, entity.eTypeGroup)
        statement.bindText(4, entity.eTime)
        statement.bindLong(5, entity.eTimestamp)
        val _tmp: Int = if (entity.isNew) 1 else 0
        statement.bindLong(6, _tmp.toLong())
        val _tmpECameraId: Int? = entity.eCameraId
        if (_tmpECameraId == null) {
          statement.bindNull(7)
        } else {
          statement.bindLong(7, _tmpECameraId.toLong())
        }
        val _tmpDeviceId: Int? = entity.deviceId
        if (_tmpDeviceId == null) {
          statement.bindNull(8)
        } else {
          statement.bindLong(8, _tmpDeviceId.toLong())
        }
        val _tmpEImages: String? = entity.eImages
        if (_tmpEImages == null) {
          statement.bindNull(9)
        } else {
          statement.bindText(9, _tmpEImages)
        }
        val _tmpEVideos: String? = entity.eVideos
        if (_tmpEVideos == null) {
          statement.bindNull(10)
        } else {
          statement.bindText(10, _tmpEVideos)
        }
        val _tmpEProcessId: Int? = entity.eProcessId
        if (_tmpEProcessId == null) {
          statement.bindNull(11)
        } else {
          statement.bindLong(11, _tmpEProcessId.toLong())
        }
        val _tmpEValues: Map<String, Any?>? = entity.eValues
        val _tmp_1: String? = __eventConverters.fromMap(_tmpEValues)
        if (_tmp_1 == null) {
          statement.bindNull(12)
        } else {
          statement.bindText(12, _tmp_1)
        }
        val _tmpSData: SData? = entity.sData
        val _tmp_2: String? = __eventConverters.fromSData(_tmpSData)
        if (_tmp_2 == null) {
          statement.bindNull(13)
        } else {
          statement.bindText(13, _tmp_2)
        }
        val _tmp_3: String? = __eventConverters.fromFileList(entity.fileItems)
        if (_tmp_3 == null) {
          statement.bindNull(14)
        } else {
          statement.bindText(14, _tmp_3)
        }
        statement.bindText(15, entity.displayTitle)
        statement.bindText(16, entity.timeAgo)
        val _tmpDeviceUri: String? = entity.deviceUri
        if (_tmpDeviceUri == null) {
          statement.bindNull(17)
        } else {
          statement.bindText(17, _tmpDeviceUri)
        }
        val _tmpDeviceKey: String? = entity.deviceKey
        if (_tmpDeviceKey == null) {
          statement.bindNull(18)
        } else {
          statement.bindText(18, _tmpDeviceKey)
        }
        statement.bindText(19, entity.aiTitle)
        statement.bindText(20, entity.actionPart)
        statement.bindText(21, entity.cameraName)
        statement.bindText(22, entity.deviceName)
        statement.bindText(23, entity.foundConnector)
      }
    }
  }

  public override suspend fun insertEvents(events: List<EventItem>): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfEventItem.insert(_connection, events)
  }

  public override suspend fun getEventById(eventId: Long): EventItem? {
    val _sql: String = "SELECT * FROM events WHERE id = ? LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, eventId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfEType: Int = getColumnIndexOrThrow(_stmt, "eType")
        val _columnIndexOfETypeGroup: Int = getColumnIndexOrThrow(_stmt, "eTypeGroup")
        val _columnIndexOfETime: Int = getColumnIndexOrThrow(_stmt, "eTime")
        val _columnIndexOfETimestamp: Int = getColumnIndexOrThrow(_stmt, "eTimestamp")
        val _columnIndexOfIsNew: Int = getColumnIndexOrThrow(_stmt, "isNew")
        val _columnIndexOfECameraId: Int = getColumnIndexOrThrow(_stmt, "eCameraId")
        val _columnIndexOfDeviceId: Int = getColumnIndexOrThrow(_stmt, "deviceId")
        val _columnIndexOfEImages: Int = getColumnIndexOrThrow(_stmt, "eImages")
        val _columnIndexOfEVideos: Int = getColumnIndexOrThrow(_stmt, "eVideos")
        val _columnIndexOfEProcessId: Int = getColumnIndexOrThrow(_stmt, "eProcessId")
        val _columnIndexOfEValues: Int = getColumnIndexOrThrow(_stmt, "eValues")
        val _columnIndexOfSData: Int = getColumnIndexOrThrow(_stmt, "sData")
        val _columnIndexOfFileItems: Int = getColumnIndexOrThrow(_stmt, "fileItems")
        val _columnIndexOfDisplayTitle: Int = getColumnIndexOrThrow(_stmt, "displayTitle")
        val _columnIndexOfTimeAgo: Int = getColumnIndexOrThrow(_stmt, "timeAgo")
        val _columnIndexOfDeviceUri: Int = getColumnIndexOrThrow(_stmt, "deviceUri")
        val _columnIndexOfDeviceKey: Int = getColumnIndexOrThrow(_stmt, "deviceKey")
        val _columnIndexOfAiTitle: Int = getColumnIndexOrThrow(_stmt, "aiTitle")
        val _columnIndexOfActionPart: Int = getColumnIndexOrThrow(_stmt, "actionPart")
        val _columnIndexOfCameraName: Int = getColumnIndexOrThrow(_stmt, "cameraName")
        val _columnIndexOfDeviceName: Int = getColumnIndexOrThrow(_stmt, "deviceName")
        val _columnIndexOfFoundConnector: Int = getColumnIndexOrThrow(_stmt, "foundConnector")
        val _result: EventItem?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpEType: String
          _tmpEType = _stmt.getText(_columnIndexOfEType)
          val _tmpETypeGroup: String
          _tmpETypeGroup = _stmt.getText(_columnIndexOfETypeGroup)
          val _tmpETime: String
          _tmpETime = _stmt.getText(_columnIndexOfETime)
          val _tmpETimestamp: Long
          _tmpETimestamp = _stmt.getLong(_columnIndexOfETimestamp)
          val _tmpIsNew: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsNew).toInt()
          _tmpIsNew = _tmp != 0
          val _tmpECameraId: Int?
          if (_stmt.isNull(_columnIndexOfECameraId)) {
            _tmpECameraId = null
          } else {
            _tmpECameraId = _stmt.getLong(_columnIndexOfECameraId).toInt()
          }
          val _tmpDeviceId: Int?
          if (_stmt.isNull(_columnIndexOfDeviceId)) {
            _tmpDeviceId = null
          } else {
            _tmpDeviceId = _stmt.getLong(_columnIndexOfDeviceId).toInt()
          }
          val _tmpEImages: String?
          if (_stmt.isNull(_columnIndexOfEImages)) {
            _tmpEImages = null
          } else {
            _tmpEImages = _stmt.getText(_columnIndexOfEImages)
          }
          val _tmpEVideos: String?
          if (_stmt.isNull(_columnIndexOfEVideos)) {
            _tmpEVideos = null
          } else {
            _tmpEVideos = _stmt.getText(_columnIndexOfEVideos)
          }
          val _tmpEProcessId: Int?
          if (_stmt.isNull(_columnIndexOfEProcessId)) {
            _tmpEProcessId = null
          } else {
            _tmpEProcessId = _stmt.getLong(_columnIndexOfEProcessId).toInt()
          }
          val _tmpEValues: Map<String, Any?>?
          val _tmp_1: String?
          if (_stmt.isNull(_columnIndexOfEValues)) {
            _tmp_1 = null
          } else {
            _tmp_1 = _stmt.getText(_columnIndexOfEValues)
          }
          _tmpEValues = __eventConverters.toMap(_tmp_1)
          val _tmpSData: SData?
          val _tmp_2: String?
          if (_stmt.isNull(_columnIndexOfSData)) {
            _tmp_2 = null
          } else {
            _tmp_2 = _stmt.getText(_columnIndexOfSData)
          }
          _tmpSData = __eventConverters.toSData(_tmp_2)
          val _tmpFileItems: List<EventFileItem>
          val _tmp_3: String?
          if (_stmt.isNull(_columnIndexOfFileItems)) {
            _tmp_3 = null
          } else {
            _tmp_3 = _stmt.getText(_columnIndexOfFileItems)
          }
          val _tmp_4: List<EventFileItem>? = __eventConverters.toFileList(_tmp_3)
          if (_tmp_4 == null) {
            error("Expected NON-NULL 'kotlin.collections.List<com.tenli.oneview.model.network.EventFileItem>', but it was NULL.")
          } else {
            _tmpFileItems = _tmp_4
          }
          val _tmpDisplayTitle: String
          _tmpDisplayTitle = _stmt.getText(_columnIndexOfDisplayTitle)
          val _tmpTimeAgo: String
          _tmpTimeAgo = _stmt.getText(_columnIndexOfTimeAgo)
          val _tmpDeviceUri: String?
          if (_stmt.isNull(_columnIndexOfDeviceUri)) {
            _tmpDeviceUri = null
          } else {
            _tmpDeviceUri = _stmt.getText(_columnIndexOfDeviceUri)
          }
          val _tmpDeviceKey: String?
          if (_stmt.isNull(_columnIndexOfDeviceKey)) {
            _tmpDeviceKey = null
          } else {
            _tmpDeviceKey = _stmt.getText(_columnIndexOfDeviceKey)
          }
          val _tmpAiTitle: String
          _tmpAiTitle = _stmt.getText(_columnIndexOfAiTitle)
          val _tmpActionPart: String
          _tmpActionPart = _stmt.getText(_columnIndexOfActionPart)
          val _tmpCameraName: String
          _tmpCameraName = _stmt.getText(_columnIndexOfCameraName)
          val _tmpDeviceName: String
          _tmpDeviceName = _stmt.getText(_columnIndexOfDeviceName)
          val _tmpFoundConnector: String
          _tmpFoundConnector = _stmt.getText(_columnIndexOfFoundConnector)
          _result = EventItem(_tmpId,_tmpEType,_tmpETypeGroup,_tmpETime,_tmpETimestamp,_tmpIsNew,_tmpECameraId,_tmpDeviceId,_tmpEImages,_tmpEVideos,_tmpEProcessId,_tmpEValues,_tmpSData,_tmpFileItems,_tmpDisplayTitle,_tmpTimeAgo,_tmpDeviceUri,_tmpDeviceKey,_tmpAiTitle,_tmpActionPart,_tmpCameraName,_tmpDeviceName,_tmpFoundConnector)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getEventsByDevicesFlow(deviceIds: List<Int>): Flow<List<EventItem>> {
    val _stringBuilder: StringBuilder = StringBuilder()
    _stringBuilder.append("SELECT * FROM events WHERE deviceId IN (")
    val _inputSize: Int = deviceIds.size
    appendPlaceholders(_stringBuilder, _inputSize)
    _stringBuilder.append(") ORDER BY eTimestamp DESC")
    val _sql: String = _stringBuilder.toString()
    return createFlow(__db, false, arrayOf("events")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        for (_item: Int in deviceIds) {
          _stmt.bindLong(_argIndex, _item.toLong())
          _argIndex++
        }
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfEType: Int = getColumnIndexOrThrow(_stmt, "eType")
        val _columnIndexOfETypeGroup: Int = getColumnIndexOrThrow(_stmt, "eTypeGroup")
        val _columnIndexOfETime: Int = getColumnIndexOrThrow(_stmt, "eTime")
        val _columnIndexOfETimestamp: Int = getColumnIndexOrThrow(_stmt, "eTimestamp")
        val _columnIndexOfIsNew: Int = getColumnIndexOrThrow(_stmt, "isNew")
        val _columnIndexOfECameraId: Int = getColumnIndexOrThrow(_stmt, "eCameraId")
        val _columnIndexOfDeviceId: Int = getColumnIndexOrThrow(_stmt, "deviceId")
        val _columnIndexOfEImages: Int = getColumnIndexOrThrow(_stmt, "eImages")
        val _columnIndexOfEVideos: Int = getColumnIndexOrThrow(_stmt, "eVideos")
        val _columnIndexOfEProcessId: Int = getColumnIndexOrThrow(_stmt, "eProcessId")
        val _columnIndexOfEValues: Int = getColumnIndexOrThrow(_stmt, "eValues")
        val _columnIndexOfSData: Int = getColumnIndexOrThrow(_stmt, "sData")
        val _columnIndexOfFileItems: Int = getColumnIndexOrThrow(_stmt, "fileItems")
        val _columnIndexOfDisplayTitle: Int = getColumnIndexOrThrow(_stmt, "displayTitle")
        val _columnIndexOfTimeAgo: Int = getColumnIndexOrThrow(_stmt, "timeAgo")
        val _columnIndexOfDeviceUri: Int = getColumnIndexOrThrow(_stmt, "deviceUri")
        val _columnIndexOfDeviceKey: Int = getColumnIndexOrThrow(_stmt, "deviceKey")
        val _columnIndexOfAiTitle: Int = getColumnIndexOrThrow(_stmt, "aiTitle")
        val _columnIndexOfActionPart: Int = getColumnIndexOrThrow(_stmt, "actionPart")
        val _columnIndexOfCameraName: Int = getColumnIndexOrThrow(_stmt, "cameraName")
        val _columnIndexOfDeviceName: Int = getColumnIndexOrThrow(_stmt, "deviceName")
        val _columnIndexOfFoundConnector: Int = getColumnIndexOrThrow(_stmt, "foundConnector")
        val _result: MutableList<EventItem> = mutableListOf()
        while (_stmt.step()) {
          val _item_1: EventItem
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpEType: String
          _tmpEType = _stmt.getText(_columnIndexOfEType)
          val _tmpETypeGroup: String
          _tmpETypeGroup = _stmt.getText(_columnIndexOfETypeGroup)
          val _tmpETime: String
          _tmpETime = _stmt.getText(_columnIndexOfETime)
          val _tmpETimestamp: Long
          _tmpETimestamp = _stmt.getLong(_columnIndexOfETimestamp)
          val _tmpIsNew: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsNew).toInt()
          _tmpIsNew = _tmp != 0
          val _tmpECameraId: Int?
          if (_stmt.isNull(_columnIndexOfECameraId)) {
            _tmpECameraId = null
          } else {
            _tmpECameraId = _stmt.getLong(_columnIndexOfECameraId).toInt()
          }
          val _tmpDeviceId: Int?
          if (_stmt.isNull(_columnIndexOfDeviceId)) {
            _tmpDeviceId = null
          } else {
            _tmpDeviceId = _stmt.getLong(_columnIndexOfDeviceId).toInt()
          }
          val _tmpEImages: String?
          if (_stmt.isNull(_columnIndexOfEImages)) {
            _tmpEImages = null
          } else {
            _tmpEImages = _stmt.getText(_columnIndexOfEImages)
          }
          val _tmpEVideos: String?
          if (_stmt.isNull(_columnIndexOfEVideos)) {
            _tmpEVideos = null
          } else {
            _tmpEVideos = _stmt.getText(_columnIndexOfEVideos)
          }
          val _tmpEProcessId: Int?
          if (_stmt.isNull(_columnIndexOfEProcessId)) {
            _tmpEProcessId = null
          } else {
            _tmpEProcessId = _stmt.getLong(_columnIndexOfEProcessId).toInt()
          }
          val _tmpEValues: Map<String, Any?>?
          val _tmp_1: String?
          if (_stmt.isNull(_columnIndexOfEValues)) {
            _tmp_1 = null
          } else {
            _tmp_1 = _stmt.getText(_columnIndexOfEValues)
          }
          _tmpEValues = __eventConverters.toMap(_tmp_1)
          val _tmpSData: SData?
          val _tmp_2: String?
          if (_stmt.isNull(_columnIndexOfSData)) {
            _tmp_2 = null
          } else {
            _tmp_2 = _stmt.getText(_columnIndexOfSData)
          }
          _tmpSData = __eventConverters.toSData(_tmp_2)
          val _tmpFileItems: List<EventFileItem>
          val _tmp_3: String?
          if (_stmt.isNull(_columnIndexOfFileItems)) {
            _tmp_3 = null
          } else {
            _tmp_3 = _stmt.getText(_columnIndexOfFileItems)
          }
          val _tmp_4: List<EventFileItem>? = __eventConverters.toFileList(_tmp_3)
          if (_tmp_4 == null) {
            error("Expected NON-NULL 'kotlin.collections.List<com.tenli.oneview.model.network.EventFileItem>', but it was NULL.")
          } else {
            _tmpFileItems = _tmp_4
          }
          val _tmpDisplayTitle: String
          _tmpDisplayTitle = _stmt.getText(_columnIndexOfDisplayTitle)
          val _tmpTimeAgo: String
          _tmpTimeAgo = _stmt.getText(_columnIndexOfTimeAgo)
          val _tmpDeviceUri: String?
          if (_stmt.isNull(_columnIndexOfDeviceUri)) {
            _tmpDeviceUri = null
          } else {
            _tmpDeviceUri = _stmt.getText(_columnIndexOfDeviceUri)
          }
          val _tmpDeviceKey: String?
          if (_stmt.isNull(_columnIndexOfDeviceKey)) {
            _tmpDeviceKey = null
          } else {
            _tmpDeviceKey = _stmt.getText(_columnIndexOfDeviceKey)
          }
          val _tmpAiTitle: String
          _tmpAiTitle = _stmt.getText(_columnIndexOfAiTitle)
          val _tmpActionPart: String
          _tmpActionPart = _stmt.getText(_columnIndexOfActionPart)
          val _tmpCameraName: String
          _tmpCameraName = _stmt.getText(_columnIndexOfCameraName)
          val _tmpDeviceName: String
          _tmpDeviceName = _stmt.getText(_columnIndexOfDeviceName)
          val _tmpFoundConnector: String
          _tmpFoundConnector = _stmt.getText(_columnIndexOfFoundConnector)
          _item_1 = EventItem(_tmpId,_tmpEType,_tmpETypeGroup,_tmpETime,_tmpETimestamp,_tmpIsNew,_tmpECameraId,_tmpDeviceId,_tmpEImages,_tmpEVideos,_tmpEProcessId,_tmpEValues,_tmpSData,_tmpFileItems,_tmpDisplayTitle,_tmpTimeAgo,_tmpDeviceUri,_tmpDeviceKey,_tmpAiTitle,_tmpActionPart,_tmpCameraName,_tmpDeviceName,_tmpFoundConnector)
          _result.add(_item_1)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getEventsByMonitorFlow(monitorId: Int): Flow<List<EventItem>> {
    val _sql: String = "SELECT * FROM events WHERE eProcessId = ? ORDER BY eTimestamp DESC"
    return createFlow(__db, false, arrayOf("events")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, monitorId.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfEType: Int = getColumnIndexOrThrow(_stmt, "eType")
        val _columnIndexOfETypeGroup: Int = getColumnIndexOrThrow(_stmt, "eTypeGroup")
        val _columnIndexOfETime: Int = getColumnIndexOrThrow(_stmt, "eTime")
        val _columnIndexOfETimestamp: Int = getColumnIndexOrThrow(_stmt, "eTimestamp")
        val _columnIndexOfIsNew: Int = getColumnIndexOrThrow(_stmt, "isNew")
        val _columnIndexOfECameraId: Int = getColumnIndexOrThrow(_stmt, "eCameraId")
        val _columnIndexOfDeviceId: Int = getColumnIndexOrThrow(_stmt, "deviceId")
        val _columnIndexOfEImages: Int = getColumnIndexOrThrow(_stmt, "eImages")
        val _columnIndexOfEVideos: Int = getColumnIndexOrThrow(_stmt, "eVideos")
        val _columnIndexOfEProcessId: Int = getColumnIndexOrThrow(_stmt, "eProcessId")
        val _columnIndexOfEValues: Int = getColumnIndexOrThrow(_stmt, "eValues")
        val _columnIndexOfSData: Int = getColumnIndexOrThrow(_stmt, "sData")
        val _columnIndexOfFileItems: Int = getColumnIndexOrThrow(_stmt, "fileItems")
        val _columnIndexOfDisplayTitle: Int = getColumnIndexOrThrow(_stmt, "displayTitle")
        val _columnIndexOfTimeAgo: Int = getColumnIndexOrThrow(_stmt, "timeAgo")
        val _columnIndexOfDeviceUri: Int = getColumnIndexOrThrow(_stmt, "deviceUri")
        val _columnIndexOfDeviceKey: Int = getColumnIndexOrThrow(_stmt, "deviceKey")
        val _columnIndexOfAiTitle: Int = getColumnIndexOrThrow(_stmt, "aiTitle")
        val _columnIndexOfActionPart: Int = getColumnIndexOrThrow(_stmt, "actionPart")
        val _columnIndexOfCameraName: Int = getColumnIndexOrThrow(_stmt, "cameraName")
        val _columnIndexOfDeviceName: Int = getColumnIndexOrThrow(_stmt, "deviceName")
        val _columnIndexOfFoundConnector: Int = getColumnIndexOrThrow(_stmt, "foundConnector")
        val _result: MutableList<EventItem> = mutableListOf()
        while (_stmt.step()) {
          val _item: EventItem
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpEType: String
          _tmpEType = _stmt.getText(_columnIndexOfEType)
          val _tmpETypeGroup: String
          _tmpETypeGroup = _stmt.getText(_columnIndexOfETypeGroup)
          val _tmpETime: String
          _tmpETime = _stmt.getText(_columnIndexOfETime)
          val _tmpETimestamp: Long
          _tmpETimestamp = _stmt.getLong(_columnIndexOfETimestamp)
          val _tmpIsNew: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsNew).toInt()
          _tmpIsNew = _tmp != 0
          val _tmpECameraId: Int?
          if (_stmt.isNull(_columnIndexOfECameraId)) {
            _tmpECameraId = null
          } else {
            _tmpECameraId = _stmt.getLong(_columnIndexOfECameraId).toInt()
          }
          val _tmpDeviceId: Int?
          if (_stmt.isNull(_columnIndexOfDeviceId)) {
            _tmpDeviceId = null
          } else {
            _tmpDeviceId = _stmt.getLong(_columnIndexOfDeviceId).toInt()
          }
          val _tmpEImages: String?
          if (_stmt.isNull(_columnIndexOfEImages)) {
            _tmpEImages = null
          } else {
            _tmpEImages = _stmt.getText(_columnIndexOfEImages)
          }
          val _tmpEVideos: String?
          if (_stmt.isNull(_columnIndexOfEVideos)) {
            _tmpEVideos = null
          } else {
            _tmpEVideos = _stmt.getText(_columnIndexOfEVideos)
          }
          val _tmpEProcessId: Int?
          if (_stmt.isNull(_columnIndexOfEProcessId)) {
            _tmpEProcessId = null
          } else {
            _tmpEProcessId = _stmt.getLong(_columnIndexOfEProcessId).toInt()
          }
          val _tmpEValues: Map<String, Any?>?
          val _tmp_1: String?
          if (_stmt.isNull(_columnIndexOfEValues)) {
            _tmp_1 = null
          } else {
            _tmp_1 = _stmt.getText(_columnIndexOfEValues)
          }
          _tmpEValues = __eventConverters.toMap(_tmp_1)
          val _tmpSData: SData?
          val _tmp_2: String?
          if (_stmt.isNull(_columnIndexOfSData)) {
            _tmp_2 = null
          } else {
            _tmp_2 = _stmt.getText(_columnIndexOfSData)
          }
          _tmpSData = __eventConverters.toSData(_tmp_2)
          val _tmpFileItems: List<EventFileItem>
          val _tmp_3: String?
          if (_stmt.isNull(_columnIndexOfFileItems)) {
            _tmp_3 = null
          } else {
            _tmp_3 = _stmt.getText(_columnIndexOfFileItems)
          }
          val _tmp_4: List<EventFileItem>? = __eventConverters.toFileList(_tmp_3)
          if (_tmp_4 == null) {
            error("Expected NON-NULL 'kotlin.collections.List<com.tenli.oneview.model.network.EventFileItem>', but it was NULL.")
          } else {
            _tmpFileItems = _tmp_4
          }
          val _tmpDisplayTitle: String
          _tmpDisplayTitle = _stmt.getText(_columnIndexOfDisplayTitle)
          val _tmpTimeAgo: String
          _tmpTimeAgo = _stmt.getText(_columnIndexOfTimeAgo)
          val _tmpDeviceUri: String?
          if (_stmt.isNull(_columnIndexOfDeviceUri)) {
            _tmpDeviceUri = null
          } else {
            _tmpDeviceUri = _stmt.getText(_columnIndexOfDeviceUri)
          }
          val _tmpDeviceKey: String?
          if (_stmt.isNull(_columnIndexOfDeviceKey)) {
            _tmpDeviceKey = null
          } else {
            _tmpDeviceKey = _stmt.getText(_columnIndexOfDeviceKey)
          }
          val _tmpAiTitle: String
          _tmpAiTitle = _stmt.getText(_columnIndexOfAiTitle)
          val _tmpActionPart: String
          _tmpActionPart = _stmt.getText(_columnIndexOfActionPart)
          val _tmpCameraName: String
          _tmpCameraName = _stmt.getText(_columnIndexOfCameraName)
          val _tmpDeviceName: String
          _tmpDeviceName = _stmt.getText(_columnIndexOfDeviceName)
          val _tmpFoundConnector: String
          _tmpFoundConnector = _stmt.getText(_columnIndexOfFoundConnector)
          _item = EventItem(_tmpId,_tmpEType,_tmpETypeGroup,_tmpETime,_tmpETimestamp,_tmpIsNew,_tmpECameraId,_tmpDeviceId,_tmpEImages,_tmpEVideos,_tmpEProcessId,_tmpEValues,_tmpSData,_tmpFileItems,_tmpDisplayTitle,_tmpTimeAgo,_tmpDeviceUri,_tmpDeviceKey,_tmpAiTitle,_tmpActionPart,_tmpCameraName,_tmpDeviceName,_tmpFoundConnector)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getRecentEvents(limit: Int): List<EventItem> {
    val _sql: String = "SELECT * FROM events ORDER BY eTimestamp DESC LIMIT ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, limit.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfEType: Int = getColumnIndexOrThrow(_stmt, "eType")
        val _columnIndexOfETypeGroup: Int = getColumnIndexOrThrow(_stmt, "eTypeGroup")
        val _columnIndexOfETime: Int = getColumnIndexOrThrow(_stmt, "eTime")
        val _columnIndexOfETimestamp: Int = getColumnIndexOrThrow(_stmt, "eTimestamp")
        val _columnIndexOfIsNew: Int = getColumnIndexOrThrow(_stmt, "isNew")
        val _columnIndexOfECameraId: Int = getColumnIndexOrThrow(_stmt, "eCameraId")
        val _columnIndexOfDeviceId: Int = getColumnIndexOrThrow(_stmt, "deviceId")
        val _columnIndexOfEImages: Int = getColumnIndexOrThrow(_stmt, "eImages")
        val _columnIndexOfEVideos: Int = getColumnIndexOrThrow(_stmt, "eVideos")
        val _columnIndexOfEProcessId: Int = getColumnIndexOrThrow(_stmt, "eProcessId")
        val _columnIndexOfEValues: Int = getColumnIndexOrThrow(_stmt, "eValues")
        val _columnIndexOfSData: Int = getColumnIndexOrThrow(_stmt, "sData")
        val _columnIndexOfFileItems: Int = getColumnIndexOrThrow(_stmt, "fileItems")
        val _columnIndexOfDisplayTitle: Int = getColumnIndexOrThrow(_stmt, "displayTitle")
        val _columnIndexOfTimeAgo: Int = getColumnIndexOrThrow(_stmt, "timeAgo")
        val _columnIndexOfDeviceUri: Int = getColumnIndexOrThrow(_stmt, "deviceUri")
        val _columnIndexOfDeviceKey: Int = getColumnIndexOrThrow(_stmt, "deviceKey")
        val _columnIndexOfAiTitle: Int = getColumnIndexOrThrow(_stmt, "aiTitle")
        val _columnIndexOfActionPart: Int = getColumnIndexOrThrow(_stmt, "actionPart")
        val _columnIndexOfCameraName: Int = getColumnIndexOrThrow(_stmt, "cameraName")
        val _columnIndexOfDeviceName: Int = getColumnIndexOrThrow(_stmt, "deviceName")
        val _columnIndexOfFoundConnector: Int = getColumnIndexOrThrow(_stmt, "foundConnector")
        val _result: MutableList<EventItem> = mutableListOf()
        while (_stmt.step()) {
          val _item: EventItem
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpEType: String
          _tmpEType = _stmt.getText(_columnIndexOfEType)
          val _tmpETypeGroup: String
          _tmpETypeGroup = _stmt.getText(_columnIndexOfETypeGroup)
          val _tmpETime: String
          _tmpETime = _stmt.getText(_columnIndexOfETime)
          val _tmpETimestamp: Long
          _tmpETimestamp = _stmt.getLong(_columnIndexOfETimestamp)
          val _tmpIsNew: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsNew).toInt()
          _tmpIsNew = _tmp != 0
          val _tmpECameraId: Int?
          if (_stmt.isNull(_columnIndexOfECameraId)) {
            _tmpECameraId = null
          } else {
            _tmpECameraId = _stmt.getLong(_columnIndexOfECameraId).toInt()
          }
          val _tmpDeviceId: Int?
          if (_stmt.isNull(_columnIndexOfDeviceId)) {
            _tmpDeviceId = null
          } else {
            _tmpDeviceId = _stmt.getLong(_columnIndexOfDeviceId).toInt()
          }
          val _tmpEImages: String?
          if (_stmt.isNull(_columnIndexOfEImages)) {
            _tmpEImages = null
          } else {
            _tmpEImages = _stmt.getText(_columnIndexOfEImages)
          }
          val _tmpEVideos: String?
          if (_stmt.isNull(_columnIndexOfEVideos)) {
            _tmpEVideos = null
          } else {
            _tmpEVideos = _stmt.getText(_columnIndexOfEVideos)
          }
          val _tmpEProcessId: Int?
          if (_stmt.isNull(_columnIndexOfEProcessId)) {
            _tmpEProcessId = null
          } else {
            _tmpEProcessId = _stmt.getLong(_columnIndexOfEProcessId).toInt()
          }
          val _tmpEValues: Map<String, Any?>?
          val _tmp_1: String?
          if (_stmt.isNull(_columnIndexOfEValues)) {
            _tmp_1 = null
          } else {
            _tmp_1 = _stmt.getText(_columnIndexOfEValues)
          }
          _tmpEValues = __eventConverters.toMap(_tmp_1)
          val _tmpSData: SData?
          val _tmp_2: String?
          if (_stmt.isNull(_columnIndexOfSData)) {
            _tmp_2 = null
          } else {
            _tmp_2 = _stmt.getText(_columnIndexOfSData)
          }
          _tmpSData = __eventConverters.toSData(_tmp_2)
          val _tmpFileItems: List<EventFileItem>
          val _tmp_3: String?
          if (_stmt.isNull(_columnIndexOfFileItems)) {
            _tmp_3 = null
          } else {
            _tmp_3 = _stmt.getText(_columnIndexOfFileItems)
          }
          val _tmp_4: List<EventFileItem>? = __eventConverters.toFileList(_tmp_3)
          if (_tmp_4 == null) {
            error("Expected NON-NULL 'kotlin.collections.List<com.tenli.oneview.model.network.EventFileItem>', but it was NULL.")
          } else {
            _tmpFileItems = _tmp_4
          }
          val _tmpDisplayTitle: String
          _tmpDisplayTitle = _stmt.getText(_columnIndexOfDisplayTitle)
          val _tmpTimeAgo: String
          _tmpTimeAgo = _stmt.getText(_columnIndexOfTimeAgo)
          val _tmpDeviceUri: String?
          if (_stmt.isNull(_columnIndexOfDeviceUri)) {
            _tmpDeviceUri = null
          } else {
            _tmpDeviceUri = _stmt.getText(_columnIndexOfDeviceUri)
          }
          val _tmpDeviceKey: String?
          if (_stmt.isNull(_columnIndexOfDeviceKey)) {
            _tmpDeviceKey = null
          } else {
            _tmpDeviceKey = _stmt.getText(_columnIndexOfDeviceKey)
          }
          val _tmpAiTitle: String
          _tmpAiTitle = _stmt.getText(_columnIndexOfAiTitle)
          val _tmpActionPart: String
          _tmpActionPart = _stmt.getText(_columnIndexOfActionPart)
          val _tmpCameraName: String
          _tmpCameraName = _stmt.getText(_columnIndexOfCameraName)
          val _tmpDeviceName: String
          _tmpDeviceName = _stmt.getText(_columnIndexOfDeviceName)
          val _tmpFoundConnector: String
          _tmpFoundConnector = _stmt.getText(_columnIndexOfFoundConnector)
          _item = EventItem(_tmpId,_tmpEType,_tmpETypeGroup,_tmpETime,_tmpETimestamp,_tmpIsNew,_tmpECameraId,_tmpDeviceId,_tmpEImages,_tmpEVideos,_tmpEProcessId,_tmpEValues,_tmpSData,_tmpFileItems,_tmpDisplayTitle,_tmpTimeAgo,_tmpDeviceUri,_tmpDeviceKey,_tmpAiTitle,_tmpActionPart,_tmpCameraName,_tmpDeviceName,_tmpFoundConnector)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun markAsRead(eventId: Long) {
    val _sql: String = "UPDATE events SET isNew = 0 WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, eventId)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteOldEvents() {
    val _sql: String = "DELETE FROM events WHERE id NOT IN (SELECT id FROM events ORDER BY eTimestamp DESC LIMIT 100)"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun clearAll() {
    val _sql: String = "DELETE FROM events"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
