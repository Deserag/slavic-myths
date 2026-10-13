package org.slavicmyths.bogatyr;
import java.util.EnumSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Zombie;
/** Local AI boundary; owned minions never chase an ally into the recovery zone. */
public final class IdolMinionGoal extends Goal {
    private final Zombie mob;
    public IdolMinionGoal(Zombie mob){this.mob=mob;setFlags(EnumSet.of(Flag.MOVE,Flag.TARGET));}
    private IdolishcheEntity owner(){var n=mob.getPersistentData();return mob.level() instanceof ServerLevel w&&n.hasUUID("IdolOwner")&&w.getEntity(n.getUUID("IdolOwner")) instanceof IdolishcheEntity b?b:null;}
    @Override public boolean canUse(){var b=owner();return b!=null&&(!b.active||mob.position().distanceToSqr(b.origin)>26*26||mob.getTarget() instanceof IlyaEntity h&&h.recovering());}
    @Override public boolean canContinueToUse(){return canUse();}
    @Override public void start(){mob.setTarget(null);mob.getNavigation().stop();}
    @Override public void tick(){var b=owner();if(b==null)return;mob.setTarget(null);if(!b.active){mob.discard();return;}if(mob.tickCount%10==0)mob.getNavigation().moveTo(b.origin.x,b.origin.y,b.origin.z,1);}
}
