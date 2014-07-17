package com.augmate.gct_mtg_client.app.utils;

import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;
import org.joda.time.format.DateTimeFormat;

public class LocalFormat extends Layout
{
    private static final String TAG = LocalFormat.class.getName();
    private String sessionId;
    private String deviceId;
    
    public LocalFormat(String sessionId, String deviceId) {
        this.sessionId = sessionId;
        this.deviceId = deviceId;
    }
    
    @Override
    public String format(LoggingEvent event) {
        String caller = LogEntriesFormat.getFrame(0);
        String thread = Thread.currentThread().getName();
        String formatted = "[" + deviceId + "] #" + sessionId + " - " + thread + " - " + caller + "()";
        
        return formatted;
    }

    @Override
    public boolean ignoresThrowable() {
        return false;
    }

    @Override
    public void activateOptions() {

    }
}
