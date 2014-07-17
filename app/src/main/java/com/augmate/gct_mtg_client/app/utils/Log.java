package com.augmate.gct_mtg_client.app.utils;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import com.logentries.log4j.LogentriesAppender;
import org.apache.log4j.Logger;

/**
 * Special Logger :D
 * Wraps Log4j
 * + Logentries appender
 * + Standard LogCat compatible dbg-output appender
 * Uses a custom PatternLayout replacement that resolves "class::method()"
 * faster than LocationPatternConverter and lets us control amount of frames
 * popped off the stack to identify the exact caller we care about 
 */
public class Log {
    private static final String TAG = "AugmateLogger";
    private static Logger loggerInstance;

    private static Logger createInstance(String deviceId) {

        // not super random or collision free
        String sessionId = Long.toString(Math.abs(java.util.UUID.randomUUID().getLeastSignificantBits()), 36).substring(0, 6);

        // remote output
        LogentriesAppender logentriesAppender = new LogentriesAppender();
        logentriesAppender.setToken("c3a45763-9854-43cc-838a-7a1b71418c6c");
        logentriesAppender.setDebug(false);
        logentriesAppender.setLayout(new LogEntriesFormat(sessionId, deviceId));
        logentriesAppender.setSsl(false);

        // local output
        LocalAppender localAppender = new LocalAppender();
        localAppender.setLayout(new LocalFormat(sessionId, deviceId));

        Logger lgr = Logger.getRootLogger();
        lgr.addAppender(localAppender);
        lgr.addAppender(logentriesAppender);

        lgr.debug("Started logging on " + Build.MANUFACTURER + " " + Build.MODEL + " version=" + Build.ID);

        return lgr;
    }

    /**
     * entry-point for the application-wide logging setup
     *
     * @param ctx Context of the application. If not provided, deviceId will be N/A
     */
    public static void setupApplication(Context ctx) {
        if (loggerInstance == null) {
            String deviceId = "N/A";

            if (ctx == null) {
                android.util.Log.d(TAG, "getLogger() called without a ctx; creating Log without device-id");
            } else {
                deviceId = Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.ANDROID_ID);
                android.util.Log.d(TAG, "getLogger() found device-id: " + deviceId);
            }

            loggerInstance = createInstance(deviceId);
        }
    }

    private static Logger getLogger() {
        if (loggerInstance == null)
            setupApplication(null);

        return loggerInstance;
    }

    public static void error(String test, Exception err) {
        getLogger().error(test, err);
    }

    public static void error(String test) {
        getLogger().error(test);
    }

    public static void debug(String test) {
        getLogger().debug(test);
    }

    public static void info(String test) {
        getLogger().info(test);
    }
}
