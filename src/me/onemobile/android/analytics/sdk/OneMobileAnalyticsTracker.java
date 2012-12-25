package me.onemobile.android.analytics.sdk;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.util.Log;

public class OneMobileAnalyticsTracker {
	private static final String PREF_NAME = "ONEMOBILE";
	private static final OneMobileAnalyticsTracker INSTANCE = new OneMobileAnalyticsTracker();
	private Context context;
	private ConnectivityManager connetivityManager;
	private int dispatchPeriod;
	private PersistentEventStore eventStore;
	private boolean powerSaveMode;
	private int eventsBeingDispatched;
	private int eventsDispatched;
	private boolean dispatcherIsBusy;

	private String country = "";
	private String language = "";
	private int preVersion;

	private static ExecutorService executor;

	public static String uid;
	public static String flag;
	public static String channel;

	private int versionCode;
	private Handler handler;
	private Runnable dispatchRunner = new Runnable() {
		public void run() {
			OneMobileAnalyticsTracker.this.dispatch();
		}
	};

	public static AnalyticsDataPoster analyticsDataPoster;
	public static Initializer initializer;

	public static OneMobileAnalyticsTracker getInstance() {
		return INSTANCE;
	}

	public static void setup() {
		if (initializer != null) {
			AnalyticsProvider.setAUTH(initializer.getAuth());
			OneMobileAnalyticsTracker.uid = initializer.getUID();
			OneMobileAnalyticsTracker.flag = initializer.getFlag();
			OneMobileAnalyticsTracker.channel = initializer.getChannel();
			OneMobileAnalyticsTracker.analyticsDataPoster = initializer.getAnalyticsDataPoster();
		}
	}

	public void start(Context paramContext, Long uid) {
		init();
		start(30, paramContext, uid);
	}

	private void init() {
		if (executor == null) {
			executor = Executors.newCachedThreadPool();
		}

	}

	private class InsertEvent implements Runnable {
		String pageName;
		int appid;
		int where;
		String flag;

		public InsertEvent(String pageName, int appid, int where, String flag) {
			this.pageName = pageName;
			this.appid = appid;
			this.where = where;
			this.flag = flag;
		}

		public void run() {
			if (Thread.interrupted()) {
				return;
			}
			Event event = new Event(OneMobileAnalyticsTracker.uid, System.currentTimeMillis(), pageName, versionCode, appid, where, flag);
			OneMobileAnalyticsTracker.this.eventStore.putEvent(event);
			resetPowerSaveMode();
		}
	}

	public void start(int paramInt, Context cxt, Long uid) {
		PersistentEventStore pes = null;
		if (this.eventStore == null) {
			pes = new PersistentEventStore(cxt);
		} else {
			pes = this.eventStore;
		}
		start(paramInt, cxt, pes, uid);
	}

	void start(int paramInt, Context _context, PersistentEventStore _eventStore, Long uid) {
		this.context = _context;
		this.country = Locale.getDefault().getCountry();
		this.language = Locale.getDefault().getLanguage();
		if (this.country == null || "".equals(this.country)) {
			this.country = "EN";
		}

		if (this.language == null || "".equals(this.language)) {
			this.language = "en";
		}
		try {
			this.versionCode = this.context.getPackageManager().getPackageInfo(this.context.getPackageName(), 0).versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		int preV = this.context.getSharedPreferences(PREF_NAME, 0).getInt("pre_v", 0);
		int curV = this.context.getSharedPreferences(PREF_NAME, 0).getInt("cur_v", 0);

		if (preV == 0 || curV == 0) {
			this.context.getSharedPreferences(PREF_NAME, 0).edit().putInt("pre_v", this.versionCode).putInt("cur_v", this.versionCode).commit();
			this.preVersion = this.versionCode;
		} else if (curV < this.versionCode) {
			this.context.getSharedPreferences(PREF_NAME, 0).edit().putInt("pre_v", curV).putInt("cur_v", this.versionCode).commit();
			this.preVersion = curV;
		} else {
			this.preVersion = preV;
		}

		this.eventStore = _eventStore;
		this.eventStore.startNewVisit(this.context, uid);
		this.eventsBeingDispatched = 0;
		this.dispatcherIsBusy = false;
		if (this.connetivityManager == null) {
			this.connetivityManager = ((ConnectivityManager) this.context.getSystemService(Context.CONNECTIVITY_SERVICE));
		}
		if (this.handler == null) {
			this.handler = new Handler(this.context.getMainLooper());
		} else {
			cancelPendingDispathes();
		}
		setDispatchPeriod(paramInt);
	}

	public void trackDownload(int appid, int where) {
		createEvent("-1", appid, where, "-1");
	}

	public void trackEvent(String pageName, int where) {
		createEvent(pageName, -1, where, "-1");
	}

	/**
	 * pageView where = -1
	 * 
	 * @param pageName
	 */
	public void trackPageView(String pageName) {
		createEvent(pageName, -1, -1, "-1");
	}

	private void createEvent(String pageName, int appid, int where, String flag) {
		executor.execute(new InsertEvent(pageName, appid, where, flag));
	}

	public void setDispatchPeriod(int paramInt) {
		int i = this.dispatchPeriod;
		this.dispatchPeriod = paramInt;
		if (i <= 0) {
			maybeScheduleNextDispatch();
		} else {
			if (i <= 0) {
				return;
			}
			cancelPendingDispathes();
			maybeScheduleNextDispatch();
		}
	}

	private void maybeScheduleNextDispatch() {
		if (this.dispatchPeriod < 0) {
			return;
		}
		if (!this.handler.postDelayed(this.dispatchRunner, this.dispatchPeriod * 1000)) {
			return;
		}
	}

	private void cancelPendingDispathes() {
		this.handler.removeCallbacks(this.dispatchRunner);
	}

	private void resetPowerSaveMode() {
		if (!this.powerSaveMode) {
			return;
		}
		this.powerSaveMode = false;
		maybeScheduleNextDispatch();
	}

	public boolean dispatch() {
		if (this.dispatcherIsBusy) {
			maybeScheduleNextDispatch();
			return false;
		}
		NetworkInfo localNetworkInfo = this.connetivityManager.getActiveNetworkInfo();
		if ((localNetworkInfo == null) || (!localNetworkInfo.isAvailable())) {
			maybeScheduleNextDispatch();
			return false;
		}

		this.eventsBeingDispatched = this.eventStore.getNumStoredEvents();
		if (this.eventsBeingDispatched > 0) {
			this.eventsDispatched = 0;
			this.dispatcherIsBusy = true;

			Event[] events = this.eventStore.peekEvents();
			if (events != null && events.length > 0) {
				dispatchEvents(events);
			}
			this.powerSaveMode = true;
			return true;
		}
		this.powerSaveMode = true;
		return false;
	}

	private void dispatchEvents(Event[] events) {
		new PostDataThread(events).start();
	}

	class PostDataThread extends Thread {
		Event[] events;

		public PostDataThread(Event[] events) {
			this.events = events;
		}

		public void run() {
			JSONArray jsonArray = new JSONArray();
			JSONObject json;
			Event event;
			for (int i = 0; i < events.length; i++) {
				event = events[i];
				try {
					json = new JSONObject();
					json.put("uid", event.uid);
					json.put("timestamp", event.timestampEvent);
					json.put("times", event.times);
					json.put("pagename", event.pageName);
					json.put("versionCode", event.versionCode);
					json.put("appid", event.appid);
					json.put("where", event.where);
					json.put("flag", event.flag);
					// 2.2 add
					json.put("channel", OneMobileAnalyticsTracker.channel);
					json.put("language", language);
					json.put("country", country);
					json.put("preVersion", preVersion);

					jsonArray.put(json);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			if (analyticsDataPoster != null) {
				synchronized (analyticsDataPoster) {
					if (analyticsDataPoster.post(jsonArray)) {
						for (int i = 0; i < events.length; i++) {
							eventDispatched(events[i]._id);
						}
					}
				}
			}
			dispatchFinished();
		}

	}

	void eventDispatched(int id) {
		this.eventStore.deleteEvent(id);
		this.eventsDispatched += 1;
	}

	void dispatchFinished() {
		this.eventsBeingDispatched = 0;
		this.dispatcherIsBusy = false;
	}

	public void stop() {
		cancelPendingDispathes();
	}

	public interface AnalyticsDataPoster {
		public boolean post(JSONArray jArray);
	}

	public void setAnalyticsDataPoster(AnalyticsDataPoster analyticsDataPoster) {
		OneMobileAnalyticsTracker.analyticsDataPoster = analyticsDataPoster;
	}

	public interface Initializer {
		public String getAuth();

		public String getUID();

		public String getFlag();

		public String getChannel();

		public AnalyticsDataPoster getAnalyticsDataPoster();
	}

	public static void setInitializer(Initializer initializer) {
		OneMobileAnalyticsTracker.initializer = initializer;
	}

}
