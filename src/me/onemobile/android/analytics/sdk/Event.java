package me.onemobile.android.analytics.sdk;


class Event {
	final int _id;
	final String uid;
	final long timestampEvent;
	final int times;
	final int versionCode;
	final String pageName;
	final int appid;
	final int where;
	final String flag;

	Event(int id, String uid, long timestampEvent, int times, String pageName,int versionCode,int appid,int where, String flag) {
		this._id = id;
		this.uid = uid;
		this.timestampEvent = timestampEvent;
		this.times = times;
		this.pageName = pageName;
		this.versionCode = versionCode;
		this.appid = appid;
		this.where = where;
		this.flag = flag;
	}

	Event(String uid, long timestampEvent, String pageName,int versionCode,int appid,int where, String flag) {
		this(-1, uid, timestampEvent, -1, pageName,versionCode,appid,where, flag);
	}
}
