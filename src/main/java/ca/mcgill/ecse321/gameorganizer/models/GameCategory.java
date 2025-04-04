package ca.mcgill.ecse321.gameorganizer.models;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum GameCategory {
    FAMILY,
    PARTY,
    STRATEGY,
    CARDS,
    COOPERATIVE,
    OTHER,
    NONE;

    @JsonCreator
    public static GameCategory fromString(String key) {
        return key == null ? NONE : GameCategory.valueOf(key.toUpperCase());
    }
}
