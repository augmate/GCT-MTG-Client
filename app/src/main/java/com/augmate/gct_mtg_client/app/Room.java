package com.augmate.gct_mtg_client.app;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;

public enum Room {
    ROOM_1("Room 1"),
    ROOM_2("Room 2"),
    ROOM_3("Room 3"),
    GARAGE("Garage"),
    ROOM_4("Room 4"),
    ROOM_5("Room 5");

    public String displayName;

    Room(String displayName) {
        this.displayName = displayName;
    }

    /**
     * this list helps matching against the speech-api
     * @return Map of pronunciation rules for each room
     */
    public static Map<Room, List<String>> asStringList() {

        Map<Room, List<String>> map = new HashMap<Room, List<String>>();

        map.put(ROOM_1, newArrayList("room 1", "room one"));
        map.put(ROOM_2, newArrayList("room 2", "room two"));
        map.put(ROOM_3, newArrayList("room 3", "room three"));
        map.put(ROOM_4, newArrayList("room 4", "room four"));
        map.put(ROOM_5, newArrayList("room 5", "room five"));
        map.put(GARAGE, newArrayList("garage", "garage room"));

        return map;
    }
}
