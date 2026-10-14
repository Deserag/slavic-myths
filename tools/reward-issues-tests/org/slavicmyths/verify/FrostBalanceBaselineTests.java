package org.slavicmyths.verify;

import net.minecraft.gametest.framework.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;
import org.slavicmyths.morozko.*;

@GameTestHolder("slavicmyths_rewards") @PrefixGameTestTemplate(false)
public final class FrostBalanceBaselineTests {
    private static net.minecraft.world.entity.monster.Zombie mob(GameTestHelper t,Vec3 at,int armor,int toughness){
        var mob=net.minecraft.world.entity.EntityType.ZOMBIE.create(t.getLevel());mob.moveTo(t.absoluteVec(at));mob.setNoAi(true);mob.setNoGravity(true);
        mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);mob.setHealth(1000);mob.getAttribute(Attributes.ARMOR).setBaseValue(armor);
        mob.getAttribute(Attributes.ARMOR_TOUGHNESS).setBaseValue(toughness);mob.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1);
        t.getLevel().addFreshEntity(mob);return mob;
    }
    private static void bolt(GameTestHelper t,int armor,int toughness){
        var caster=mob(t,new Vec3(1,2,1),0,0);var target=mob(t,new Vec3(1,2,6),armor,toughness);
        FrostShard.launch(caster,target.getBoundingBox().getCenter(),7,8,null);
        t.runAfterDelay(12,()->{float damage=1000-target.getHealth();t.assertTrue(damage>0&&damage<=7,"native projectile must hit and respect armor");
            System.out.println("FROST_BALANCE_BASELINE bolt armor="+armor+" toughness="+toughness+" HP1000->"+target.getHealth()+" damage="+damage);t.succeed();});
    }
    @GameTest(template="empty",timeoutTicks=100) public static void normalBolt(GameTestHelper t){bolt(t,0,0);}
    @GameTest(template="empty",timeoutTicks=100) public static void armoredBolt(GameTestHelper t){bolt(t,20,8);}
}
