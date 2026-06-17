package com.tenli.aiot.`data`.local.db

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class AppDatabase_Impl : AppDatabase() {
  private val _eventDao: Lazy<EventDao> = lazy {
    EventDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(3, "0819c8634ed483b00db119c3e15d0291", "fa0997b176e4b8a6b4d4347c58f91e73") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `events` (`id` INTEGER NOT NULL, `eType` TEXT NOT NULL, `eTypeGroup` TEXT NOT NULL, `eTime` TEXT NOT NULL, `eTimestamp` INTEGER NOT NULL, `isNew` INTEGER NOT NULL, `eCameraId` INTEGER, `deviceId` INTEGER, `eImages` TEXT, `eVideos` TEXT, `eProcessId` INTEGER, `eValues` TEXT, `sData` TEXT, `fileItems` TEXT NOT NULL, `displayTitle` TEXT NOT NULL, `timeAgo` TEXT NOT NULL, `deviceUri` TEXT, `deviceKey` TEXT, `aiTitle` TEXT NOT NULL, `actionPart` TEXT NOT NULL, `cameraName` TEXT NOT NULL, `deviceName` TEXT NOT NULL, `foundConnector` TEXT NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '0819c8634ed483b00db119c3e15d0291')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `events`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection): RoomOpenDelegate.ValidationResult {
        val _columnsEvents: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsEvents.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsEvents.put("eType", TableInfo.Column("eType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsEvents.put("eTypeGroup", TableInfo.Column("eTypeGroup", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsEvents.put("eTime", TableInfo.Column("eTime", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsEvents.put("eTimestamp", TableInfo.Column("eTimestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsEvents.put("isNew", TableInfo.Column("isNew", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsEvents.put("eCameraId", TableInfo.Column("eCameraId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsEvents.put("deviceId", TableInfo.Column("deviceId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsEvents.put("eImages", TableInfo.Column("eImages", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsEvents.put("eVideos", TableInfo.Column("eVideos", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsEvents.put("eProcessId", TableInfo.Column("eProcessId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsEvents.put("eValues", TableInfo.Column("eValues", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsEvents.put("sData", TableInfo.Column("sData", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsEvents.put("fileItems", TableInfo.Column("fileItems", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsEvents.put("displayTitle", TableInfo.Column("displayTitle", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsEvents.put("timeAgo", TableInfo.Column("timeAgo", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsEvents.put("deviceUri", TableInfo.Column("deviceUri", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsEvents.put("deviceKey", TableInfo.Column("deviceKey", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsEvents.put("aiTitle", TableInfo.Column("aiTitle", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsEvents.put("actionPart", TableInfo.Column("actionPart", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsEvents.put("cameraName", TableInfo.Column("cameraName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsEvents.put("deviceName", TableInfo.Column("deviceName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsEvents.put("foundConnector", TableInfo.Column("foundConnector", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysEvents: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesEvents: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoEvents: TableInfo = TableInfo("events", _columnsEvents, _foreignKeysEvents, _indicesEvents)
        val _existingEvents: TableInfo = read(connection, "events")
        if (!_infoEvents.equals(_existingEvents)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |events(com.tenli.aiot.model.network.EventItem).
              | Expected:
              |""".trimMargin() + _infoEvents + """
              |
              | Found:
              |""".trimMargin() + _existingEvents)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "events")
  }

  public override fun clearAllTables() {
    super.performClear(false, "events")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(EventDao::class, EventDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>): List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun eventDao(): EventDao = _eventDao.value
}
