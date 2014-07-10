package com.augmate.gct_mtg_client.app.tasks;

import java.util.List;

public interface VoiceTimeSelectActivityCallbacks {
    public void onTaskSuccess(List<String> availabilities);
    public void onTaskFailed();
}
