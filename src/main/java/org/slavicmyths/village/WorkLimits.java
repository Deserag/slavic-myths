package org.slavicmyths.village;

/** Shared bounds; searches are scheduled, never global world/player polling. */
public final class WorkLimits {
    public static final int STORAGE_SLOTS=64, STORAGE_RADIUS=32, FIELD_RADIUS=32,
        ANIMAL_RADIUS=24, AUXILIARY_RADIUS=24, ANIMAL_CAP=16;
    public static final int SEARCH_COOLDOWN=400, ACTION_COOLDOWN=200, TRAVEL_TIMEOUT=300;
    public static final double INTERACTION_DISTANCE_SQUARED=6.25;
    private WorkLimits(){}
}
