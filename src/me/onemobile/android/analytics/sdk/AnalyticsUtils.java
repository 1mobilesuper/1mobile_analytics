package me.onemobile.android.analytics.sdk;

import android.content.Context;

/**
 * Helper singleton class for the Google Analytics tracking library.
 */
public class AnalyticsUtils {

    OneMobileAnalyticsTracker mTracker;

    /**
     * The analytics tracking code for the app.
     */

    private static final boolean ANALYTICS_ENABLED = true;

    private static AnalyticsUtils sInstance;

    /**
     * Returns the global {@link AnalyticsUtils} singleton object, creating one if necessary.
     */
    public static AnalyticsUtils getInstance(Context context) {
        if (!ANALYTICS_ENABLED) {
            return sEmptyAnalyticsUtils;
        }

        if (sInstance == null) {
            if (context == null) {
                return sEmptyAnalyticsUtils;
            }
            sInstance = new AnalyticsUtils(context.getApplicationContext());
            OneMobileAnalyticsTracker.setup();
        }

        return sInstance;
    }

    private AnalyticsUtils(Context context) {
        if (context == null) {
            // This should only occur for the empty Analytics utils object.
            return;
        }

        mTracker = OneMobileAnalyticsTracker.getInstance();

        mTracker.start(context, -1L);
    }
    
    public void trackEvent(String pageName, int where) {
    	mTracker.trackEvent(pageName, where);
	}
    
    public void trackPageView(String pageName) {
    	mTracker.trackPageView(pageName);
	}
    
    public void trackDownload(int appid, int where) {
    	mTracker.trackDownload(appid, where);
	}

    /**
     * Empty instance for use when Analytics is disabled or there was no Context available.
     */
    private static AnalyticsUtils sEmptyAnalyticsUtils = new AnalyticsUtils(null) {
    	
    	@Override
    	public void trackEvent(String pageName, int where) {}
        
    	@Override
        public void trackPageView(String pageName) {}
        
    	@Override
        public void trackDownload(int appid, int where) {}
    };
}
