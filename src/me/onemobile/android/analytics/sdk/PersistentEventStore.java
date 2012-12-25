package me.onemobile.android.analytics.sdk;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;

class PersistentEventStore {
	private Context cxt;
	private int times;
	private int numStoredEvents;
	private boolean sessionUpdated;
	private Uri uri;

	public PersistentEventStore(Context context) {
		this.cxt = context;
		this.uri = Uri.parse("content://" + AnalyticsProvider.getAUTH() + "/events");
	}

	public void deleteEvent(int id) {
		if (this.cxt.getContentResolver().delete(uri, "_id = '" + id + "'", null) == 0) {
			return;
		}
		this.numStoredEvents -= 1;
	}

	public Event[] peekEvents() {
		final Cursor cursor = this.cxt.getContentResolver().query(uri, null, null, null, " _id DESC LIMIT 100 ");
		ArrayList<Event> lst = new ArrayList<Event>();
		if (cursor != null) {
			while (cursor.moveToNext()) {
				lst.add(new Event(cursor.getInt(0), cursor.getString(1), cursor.getLong(2), cursor.getInt(3), cursor.getString(5), cursor.getInt(4), cursor
						.getInt(6), cursor.getInt(7), cursor.getString(8)));
			}
			cursor.close();
		}
		return (Event[]) lst.toArray(new Event[lst.size()]);
	}

	public void putEvent(Event event) {
		if (this.numStoredEvents >= 1000) {
			return;
		}
		if (!this.sessionUpdated) {
			storeUpdatedSession();
		}
		ContentValues values = new ContentValues();
		values.put("uid", event.uid);
		values.put("timestamp_event", Long.valueOf(event.timestampEvent));
		values.put("times", Integer.valueOf(this.times));
		values.put("pageName", event.pageName);
		values.put("versionCode", Integer.valueOf(event.versionCode));
		values.put("appid", Integer.valueOf(event.appid));
		values.put("wheres", Integer.valueOf(event.where));
		values.put("flag", OneMobileAnalyticsTracker.flag);
		try {
			this.cxt.getContentResolver().insert(uri, values);
			this.numStoredEvents += 1;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int getNumStoredEvents() {
		int count = -1;
		try {
			Cursor cursor = this.cxt.getContentResolver().query(uri, new String[] { "_id" }, null, null, null);
			if (cursor != null) {
				count = cursor.getCount();
				cursor.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return count;
	}

	public void startNewVisit(Context cxt, Long uid) {

		SharedPreferences shp = cxt.getSharedPreferences("analytic", 0);

		this.times = shp.getInt("times", 0) + 1;

		shp.edit().putInt("times", this.times).commit();

		this.sessionUpdated = false;
		this.numStoredEvents = getNumStoredEvents();

	}

	private void storeUpdatedSession() {
		// SQLiteDatabase db = this.databaseHelper.getWritableDatabase();
		// ContentValues values = new ContentValues();
		// values.put("times", Integer.valueOf(this.times));
		// db.update("session", values, "uid=?", new String[] { this.uid });
		// this.sessionUpdated = true;
	}

}
