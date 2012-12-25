package me.onemobile.android.analytics.flurry;

import java.util.Map;

import android.content.Context;

public class FlurryAnalytics {

	public static boolean enable = false;
	public static String UUID = "";
	public static String FLURRY_API_KEY = "";

	public static void setup(boolean enable, String uid, String apiKey) {
		FlurryAnalytics.enable = enable;
		FlurryAnalytics.UUID = uid;
		FlurryAnalytics.FLURRY_API_KEY = apiKey;
	}

	public static void startSession(Context ctx) {
		// if (enable) {
		// FlurryAgent.setReportLocation(false);
		// FlurryAgent.setUserId(UUID);
		// FlurryAgent.onStartSession(ctx, FLURRY_API_KEY);
		// }
	}

	public static void stopSession(Context ctx) {
		// if (enable) {
		// FlurryAgent.onEndSession(ctx);
		// }
	}

	public static void trackPageView(String pageName) {
		// if (enable) {
		// FlurryAgent.logEvent(pageName);
		// }
	}

	public static void trackPageView(String pageName, Map<String, String> params) {
		// if (enable) {
		// FlurryAgent.logEvent(pageName, params);
		// }
	}

	public static void trackEvent(String eventName) {
		// if (enable) {
		// FlurryAgent.logEvent(eventName);
		// }
	}

}
