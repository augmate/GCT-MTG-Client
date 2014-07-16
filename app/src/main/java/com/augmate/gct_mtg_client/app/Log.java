package com.augmate.gct_mtg_client.app;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import com.logentries.log4j.LogentriesAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.joda.time.DateTime;

public class Log {

    private static Logger loggerInstance;

    private static Logger createInstance(String deviceId) {

        // not super random or collision free
        String sessionId = Long.toString(Math.abs(java.util.UUID.randomUUID().getLeastSignificantBits()), 36);
        
        PatternLayout pl = new PatternLayout("%d{ABSOLUTE} ["+sessionId+"] [" + deviceId + "] [%t] %C{1}::%M(); %m%n");

        LogentriesAppender logentriesAppender = new LogentriesAppender();
        logentriesAppender.setToken("c3a45763-9854-43cc-838a-7a1b71418c6c");
        logentriesAppender.setDebug(false);
        logentriesAppender.setLayout(pl);
        logentriesAppender.setSsl(false);

        Logger lgr = Logger.getRootLogger();
        lgr.addAppender(logentriesAppender);
        
        // TODO: add a local appender

        lgr.debug("Started logging on " + Build.MANUFACTURER + " " + Build.MODEL + " version=" + Build.ID);

        return lgr;
    }

    public static Logger getLogger(Context ctx) {

        if (loggerInstance == null) {

            String deviceId = "N/A";

            if (ctx == null) {
                android.util.Log.d("Log", "getLogger() called without a ctx; creating logger without device-id");
            } else {
                deviceId = Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.ANDROID_ID);
                android.util.Log.d("Log", "getLogger() found device-id: " + deviceId);
            }

            loggerInstance = createInstance(deviceId);
        }

        return loggerInstance;
    }
    
    public static void debug(String msg) {
        getLogger(null).debug(msg);
    }

    public static void debug(String msg, Exception err) {
        getLogger(null).debug(msg, err);
    }

    public static void error(String msg) {
        getLogger(null).error(msg);
    }
    
    public static void error(String msg, Exception err) {
        getLogger(null).error(msg, err);
    }

    public static void info(String msg) {
        getLogger(null).info(msg);
    }
}
