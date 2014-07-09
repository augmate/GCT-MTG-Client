package com.augmate.gct_mtg_client.app;

import java.io.Serializable;

public class RoomOption implements Serializable {
    public String name;
    public float confidence;
    public float distance;
    
    public RoomOption(String number, float confidence, float distance) {
        this.name = number;
        this.confidence = confidence;
        this.distance = distance;
    }
}
