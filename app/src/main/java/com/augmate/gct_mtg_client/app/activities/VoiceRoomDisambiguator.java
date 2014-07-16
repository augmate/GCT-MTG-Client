package com.augmate.gct_mtg_client.app.activities;

import com.augmate.gct_mtg_client.app.Log;
import com.augmate.gct_mtg_client.app.Room;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VoiceRoomDisambiguator {

    public static final String TAG = "RoomDisambiguator";

    /**
     * given results from speech-api, find the right room, given a dictionary of possible choices
     * @param voiceResults
     * @param roomExpectedMatches
     * @return Room or NULL if nothing was found
     */
    public static Room match(ArrayList<String> voiceResults, Map<Room, List<String>> roomExpectedMatches) {

        for (int i = 0; i < voiceResults.size(); ++i) {
            String actualVoiceResult = voiceResults.get(i);
            Log.debug("Voice result: " + actualVoiceResult);

            for(Room room : roomExpectedMatches.keySet()) {
                for (String expectedVoiceResult : roomExpectedMatches.get(room)) {
                    if (actualVoiceResult.equalsIgnoreCase(expectedVoiceResult)) {
                        return room;
                    }
                }
            }
        }
        return null;
    }
}
