package com.augmate.gct_mtg_client.app.tasks;

import com.augmate.gct_mtg_client.app.BookingTime;

import java.util.List;

public interface VoiceTimeSelectActivityCallbacks {
    public void onRecieveAvailabilities(List<BookingTime> availabilities);
}
