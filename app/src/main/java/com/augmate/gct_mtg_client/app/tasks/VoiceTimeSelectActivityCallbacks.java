package com.augmate.gct_mtg_client.app.tasks;

import java.util.List;

public interface VoiceTimeSelectActivityCallbacks {
    public void onTaskSuccess(List<Integer> availabilities);
    public void onTaskFailed();
}
