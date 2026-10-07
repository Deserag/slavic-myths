package org.slavicmyths.block;
/** Existing registry class retained; the five-phase implementation is shared with new garden berries. */
public final class FolkBerryBush extends org.slavicmyths.garden.PerennialBush {
    public static final net.minecraft.world.level.block.state.properties.IntegerProperty AGE=PHASE;
    public FolkBerryBush(boolean raspberry){super(raspberry?0:1);}
}
