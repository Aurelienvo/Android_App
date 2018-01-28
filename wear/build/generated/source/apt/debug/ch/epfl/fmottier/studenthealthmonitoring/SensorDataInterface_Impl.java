package ch.epfl.fmottier.studenthealthmonitoring;

import android.arch.persistence.db.SupportSQLiteStatement;
import android.arch.persistence.room.EntityInsertionAdapter;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.RoomSQLiteQuery;
import android.arch.persistence.room.SharedSQLiteStatement;
import android.database.Cursor;
import java.lang.Override;
import java.lang.String;
import java.util.ArrayList;
import java.util.List;

public class SensorDataInterface_Impl implements SensorDataInterface {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter __insertionAdapterOfSensorData;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAll;

  public SensorDataInterface_Impl(RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfSensorData = new EntityInsertionAdapter<SensorData>(__db) {
      @Override
      public String createQuery() {
        return "INSERT OR ABORT INTO `SensorData`(`uid`,`timestamp`,`type`,`value`) VALUES (nullif(?, 0),?,?,?)";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, SensorData value) {
        stmt.bindLong(1, value.uid);
        if (value.timestamp == null) {
          stmt.bindNull(2);
        } else {
          stmt.bindString(2, value.timestamp);
        }
        stmt.bindLong(3, value.type);
        stmt.bindDouble(4, value.value);
      }
    };
    this.__preparedStmtOfDeleteAll = new SharedSQLiteStatement(__db) {
      @Override
      public String createQuery() {
        final String _query = "DELETE FROM SensorData";
        return _query;
      }
    };
  }

  @Override
  public long insertSensorData(SensorData sensorData) {
    __db.beginTransaction();
    try {
      long _result = __insertionAdapterOfSensorData.insertAndReturnId(sensorData);
      __db.setTransactionSuccessful();
      return _result;
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void deleteAll() {
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAll.acquire();
    __db.beginTransaction();
    try {
      _stmt.executeUpdateDelete();
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
      __preparedStmtOfDeleteAll.release(_stmt);
    }
  }

  @Override
  public List<SensorData> getLastNValues(int sensortype, int N) {
    final String _sql = "SELECT * FROM SensorData WHERE type = ? ORDER BY timestamp DESC LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, sensortype);
    _argIndex = 2;
    _statement.bindLong(_argIndex, N);
    final Cursor _cursor = __db.query(_statement);
    try {
      final int _cursorIndexOfUid = _cursor.getColumnIndexOrThrow("uid");
      final int _cursorIndexOfTimestamp = _cursor.getColumnIndexOrThrow("timestamp");
      final int _cursorIndexOfType = _cursor.getColumnIndexOrThrow("type");
      final int _cursorIndexOfValue = _cursor.getColumnIndexOrThrow("value");
      final List<SensorData> _result = new ArrayList<SensorData>(_cursor.getCount());
      while(_cursor.moveToNext()) {
        final SensorData _item;
        _item = new SensorData();
        _item.uid = _cursor.getInt(_cursorIndexOfUid);
        _item.timestamp = _cursor.getString(_cursorIndexOfTimestamp);
        _item.type = _cursor.getInt(_cursorIndexOfType);
        _item.value = _cursor.getDouble(_cursorIndexOfValue);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }
}
