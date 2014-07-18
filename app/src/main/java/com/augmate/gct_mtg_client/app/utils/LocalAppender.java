package com.augmate.gct_mtg_client.app.utils;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;

public class LocalAppender extends AppenderSkeleton {
    
    @Override
    protected void append(LoggingEvent event) {
        String logLine = getLayout().format(event);
        
        // TODO: add more level support
        if( event.getLevel() == Level.DEBUG)
            android.util.Log.d(logLine, event.getMessage().toString());

        if( event.getLevel() == Level.INFO)
            android.util.Log.i(logLine, event.getMessage().toString());

        // TODO: pass along throwable info (exception)
        if( event.getLevel() == Level.ERROR)
            android.util.Log.e(logLine, event.getMessage().toString());
    }

    @Override
    public void close() {

    }

    @Override
    public boolean requiresLayout() {
        return true;
    }
}
