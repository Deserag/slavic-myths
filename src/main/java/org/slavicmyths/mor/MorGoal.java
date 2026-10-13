package org.slavicmyths.mor;

import java.util.EnumSet;
import net.minecraft.world.entity.ai.goal.Goal;
import org.slavicmyths.entity.LandSpiritEntity;

/** Exclusive MOVE/LOOK gate; original move sets resume unchanged for NORMAL entities. */
public final class MorGoal extends Goal {
    private final LandSpiritEntity mob;
    public MorGoal(LandSpiritEntity mob){this.mob=mob;setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK,Flag.JUMP));}
    @Override public boolean canUse(){return mob.morState()!=0;}
    @Override public boolean canContinueToUse(){return canUse();}
    @Override public void start(){mob.getNavigation().stop();}
}
