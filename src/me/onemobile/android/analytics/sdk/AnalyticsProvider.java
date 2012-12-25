package me.onemobile.android.analytics.sdk;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class AnalyticsProvider extends ContentProvider {

	public static final int DB_VERSION = 191;
	public static final String DB_TABLE = "events";

	private SQLiteOpenHelper mOpenHelper = null;

	private static String auth;

	public static String getAUTH() {
		if (auth == null || auth.length() == 0) {
			if (OneMobileAnalyticsTracker.initializer != null) {
				auth = OneMobileAnalyticsTracker.initializer.getAuth();
			}
		}
		return auth;
	}

	public static void setAUTH(String a) {
		auth = a;
	}

	private static class DataBaseHelper extends SQLiteOpenHelper {
		public DataBaseHelper(Context context) {
			super(context, "onemobile_analytics.db", null, 1);
		}

		public DataBaseHelper(Context context, String tableName) {
			super(context, tableName, null, 1);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			createTables(db);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int paramInt1, int paramInt2) {
			dropTables(db);
			createTables(db);
		}

		private void createTables(SQLiteDatabase db) {
			try {
				db.execSQL("CREATE TABLE " + DB_TABLE + " (" + String.format(" '%s' INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,", new Object[] { "_id" })
						+ String.format(" '%s' INTEGER,", new Object[] { "uid" }) + String.format(" '%s' INTEGER,", new Object[] { "timestamp_event" })
						+ String.format(" '%s' INTEGER,", new Object[] { "times" }) + String.format(" '%s' INTEGER,", new Object[] { "versionCode" })
						+ String.format(" '%s' TEXT,", new Object[] { "pagename" }) + String.format(" '%s' INTEGER,", new Object[] { "appid" })
						+ String.format(" '%s' INTEGER,", new Object[] { "wheres" }) + String.format(" '%s' TEXT", new Object[] { "flag" }) + ");");

			} catch (SQLException ex) {
				throw ex;
			}
		}

		private void dropTables(SQLiteDatabase db) {
			try {
				db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE);
			} catch (SQLException ex) {
				throw ex;
			}
		}
	}

	@Override
	public boolean onCreate() {
		mOpenHelper = new DataBaseHelper(getContext());
		return true;
	}

	@Override
	public Uri insert(final Uri uri, final ContentValues values) {
		try {
			SQLiteDatabase db = mOpenHelper.getWritableDatabase();
			long rowID = db.insert(DB_TABLE, null, values);

			if (rowID != -1) {
				getContext().getContentResolver().notifyChange(uri, null);
			} else {
			}
			return Uri.parse("content://" + getAUTH() + "/" + rowID);
		} catch (Exception e) {
			return Uri.EMPTY;
		}
	}

	@Override
	public Cursor query(final Uri uri, String[] projection, final String selection, final String[] selectionArgs, final String sort) {
		try {
			SQLiteDatabase db = mOpenHelper.getReadableDatabase();
			SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
			qb.setTables(DB_TABLE);
			Cursor ret = qb.query(db, projection, selection, selectionArgs, null, null, sort);
			ret.setNotificationUri(getContext().getContentResolver(), uri);
			return ret;
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public int update(final Uri uri, final ContentValues values, final String where, final String[] whereArgs) {
		int count = 0;
		try {
			SQLiteDatabase db = mOpenHelper.getWritableDatabase();
			count = db.update(DB_TABLE, values, where, whereArgs);
			getContext().getContentResolver().notifyChange(uri, null);
		} catch (Exception e) {
		}
		return count;
	}

	public int delete(final Uri uri, final String where, final String[] whereArgs) {
		int count = 0;
		try {
			SQLiteDatabase db = mOpenHelper.getWritableDatabase();
			count = db.delete(DB_TABLE, where, whereArgs);
			getContext().getContentResolver().notifyChange(uri, null);
		} catch (Exception e) {
		}
		return count;
	}

	public String getType(Uri uri) {
		return null;
	}
}
