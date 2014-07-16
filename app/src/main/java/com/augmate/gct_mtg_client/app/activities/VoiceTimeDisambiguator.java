package com.augmate.gct_mtg_client.app.activities;

import com.augmate.gct_mtg_client.app.BookingTime;
import com.augmate.gct_mtg_client.app.Log;

import java.util.List;
import java.util.Map;

public class VoiceTimeDisambiguator {

    public static final String TAG = "VoiceDisambiguator";

    /**
     * given results from speech-api, find the right calendar time slot, given a dictionary of possible choices
     * @param actualVoiceResult String of the speech-api raw result
     * @param roomExpectedMatches Map of possible matches we care about
     * @return TimeResult representing the hour we want to book, or none
     */

    public static BookingTime match(String actualVoiceResult, Map<BookingTime, List<String>> roomExpectedMatches) {
        Log.debug("Voice result: " + actualVoiceResult);

        actualVoiceResult = actualVoiceResult.replace(".", "");

        for (BookingTime time : roomExpectedMatches.keySet()) {
            for (String expectedVoiceResult : roomExpectedMatches.get(time)) {
                if (actualVoiceResult.equalsIgnoreCase(expectedVoiceResult)) {
                    Log.debug("Matched: " + actualVoiceResult + " to time " + time.hour + " using " + expectedVoiceResult);
                    return time;
                }
            }
        }

        return BookingTime.INVALID;
    }
}
