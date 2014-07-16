package com.augmate.gct_mtg_client.app;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

public class AndroidDbgAppender extends AppenderSkeleton {
    
    @Override
    protected void append(LoggingEvent event) {
        String logLine = getLayout().format(event);
        android.util.Log.d("AndroidDbgAppender", logLine);
    }

    @Override
    public void close() {

    }

    @Override
    public boolean requiresLayout() {
        return true;
    }
}
