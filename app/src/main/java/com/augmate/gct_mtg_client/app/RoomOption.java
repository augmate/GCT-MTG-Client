package com.augmate.gct_mtg_client.app;

import java.io.Serializable;

public class RoomOption implements Serializable {
    public int number;
    public float confidence;

    public RoomOption() {
        
    }
    
    public RoomOption(int number, float confidence) {
        this.number = number;
        this.confidence = confidence;
    }
}
