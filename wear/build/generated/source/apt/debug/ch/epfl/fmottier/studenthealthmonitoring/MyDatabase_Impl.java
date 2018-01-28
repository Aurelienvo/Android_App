package ch.epfl.fmottier.studenthealthmonitoring;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.db.SupportSQLiteOpenHelper;
import android.arch.persistence.db.SupportSQLiteOpenHelper.Callback;
import android.arch.persistence.db.SupportSQLiteOpenHelper.Configuration;
import android.arch.persistence.room.DatabaseConfiguration;
import android.arch.persistence.room.InvalidationTracker;
import android.arch.persistence.room.RoomOpenHelper;
import android.arch.persistence.room.RoomOpenHelper.Delegate;
import android.arch.persistence.room.util.TableInfo;
import android.arch.persistence.room.util.TableInfo.Column;
import android.arch.persistence.room.util.TableInfo.ForeignKey;
import android.arch.persistence.room.util.TableInfo.Index;
import java.lang.IllegalStateException;
import java.lang.Override;
import java.lang.String;
import java.util.HashMap;
import java.util.HashSet;

public class MyDatabase_Impl extends MyDatabase {
  private volatile SensorDataInterface _sensorDataInterface;

  protected SupportSQLiteOpenHelper createOpenHelper(DatabaseConfiguration configuration) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(configuration, new RoomOpenHelper.Delegate(1) {
      public void createAllTables(SupportSQLiteDatabase _db) {
        _db.execSQL("CREATE TABLE IF NOT EXISTS `SensorData` (`uid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `timestamp` TEXT, `type` INTEGER NOT NULL, `value` REAL NOT NULL)");
        _db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        _db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, \"8f03d27d4003bd2fb9ba9fc4d86a6d2d\")");
      }

      public void dropAllTables(SupportSQLiteDatabase _db) {
        _db.execSQL("DROP TABLE IF EXISTS `SensorData`");
      }

      protected void onCreate(SupportSQLiteDatabase _db) {
        if (mCallbacks != null) {
          for (int _i = 0, _size = mCallbacks.size(); _i < _size; _i++) {
            mCallbacks.get(_i).onCreate(_db);
          }
        }
      }

      public void onOpen(SupportSQLiteDatabase _db) {
        mDatabase = _db;
        internalInitInvalidationTracker(_db);
        if (mCallbacks != null) {
          for (int _i = 0, _size = mCallbacks.size(); _i < _size; _i++) {
            mCallbacks.get(_i).onOpen(_db);
          }
        }
      }

      protected void validateMigration(SupportSQLiteDatabase _db) {
        final HashMap<String, TableInfo.Column> _columnsSensorData = new HashMap<String, TableInfo.Column>(4);
        _columnsSensorData.put("uid", new TableInfo.Column("uid", "INTEGER", true, 1));
        _columnsSensorData.put("timestamp", new TableInfo.Column("timestamp", "TEXT", false, 0));
        _columnsSensorData.put("type", new TableInfo.Column("type", "INTEGER", true, 0));
        _columnsSensorData.put("value", new TableInfo.Column("value", "REAL", true, 0));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSensorData = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSensorData = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoSensorData = new TableInfo("SensorData", _columnsSensorData, _foreignKeysSensorData, _indicesSensorData);
        final TableInfo _existingSensorData = TableInfo.read(_db, "SensorData");
        if (! _infoSensorData.equals(_existingSensorData)) {
          throw new IllegalStateException("Migration didn't properly handle SensorData(ch.epfl.fmottier.studenthealthmonitoring.SensorData).\n"
                  + " Expected:\n" + _infoSensorData + "\n"
                  + " Found:\n" + _existingSensorData);
        }
      }
    }, "8f03d27d4003bd2fb9ba9fc4d86a6d2d");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(configuration.context)
        .name(configuration.name)
        .callback(_openCallback)
        .build();
    final SupportSQLiteOpenHelper _helper = configuration.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  protected InvalidationTracker createInvalidationTracker() {
    return new InvalidationTracker(this, "SensorData");
  }

  @Override
  public SensorDataInterface sensorDataDao() {
    if (_sensorDataInterface != null) {
      return _sensorDataInterface;
    } else {
      synchronized(this) {
        if(_sensorDataInterface == null) {
          _sensorDataInterface = new SensorDataInterface_Impl(this);
        }
        return _sensorDataInterface;
      }
    }
  }
}
