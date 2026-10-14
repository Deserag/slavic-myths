package org.slavicmyths.verify;

import net.minecraft.gametest.framework.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.*;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.RegistryFriendlyByteBuf;
import io.netty.buffer.Unpooled;
import net.neoforged.neoforge.gametest.*;
import org.slavicmyths.gorynych.*;

@GameTestHolder("slavicmyths_rewards") @PrefixGameTestTemplate(false)
public final class GorynychFireIssueTests {
    private static net.minecraft.world.entity.monster.Zombie mob(GameTestHelper t,Vec3 at){
        var mob=EntityType.ZOMBIE.create(t.getLevel());mob.moveTo(t.absoluteVec(at));
        mob.setNoAi(true);mob.setNoGravity(true);mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);mob.setHealth(100);
        mob.getAttribute(Attributes.ARMOR).setBaseValue(0);t.getLevel().addFreshEntity(mob);return mob;
    }
    @GameTest(template="empty") public static void damageAndResistanceUnchanged(GameTestHelper t){
        var owner=mob(t,new Vec3(1,2,1));var target=mob(t,new Vec3(1,2,6));
        GorynychEquipment.burst(owner,target.position(),1,3);
        t.assertTrue(target.getHealth()==95,"fire impact retains 5 HP");
        t.assertTrue(target.getRemainingFireTicks()==40,"burn retains two seconds");
        var immune=mob(t,new Vec3(1,2,11));immune.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE,200));
        GorynychEquipment.burst(owner,immune.position(),1,3);
        t.assertTrue(immune.getHealth()==100,"fire resistance retains immunity");
        t.assertTrue(t.getLevel().getEntitiesOfClass(BreathCloud.class,target.getBoundingBox().inflate(16)).isEmpty(),"VFX must not create damaging cloud");
        System.out.println("FIRE_AFTER HP100->95 burnTicks40 resistantHP100 no damaging cloud");t.succeed();
    }
    @GameTest(template="empty") public static void visualMessageCodec(GameTestHelper t){
        var message=new FlameVfx.Impact(new Vec3(1.25,2.5,-3.75),3);
        var buffer=new RegistryFriendlyByteBuf(Unpooled.buffer(),t.getLevel().registryAccess());
        FlameVfx.Impact.CODEC.encode(buffer,message);
        t.assertTrue(buffer.readableBytes()==28,"one impact body is 28 bytes");
        var decoded=FlameVfx.Impact.CODEC.decode(buffer);buffer.release();
        t.assertTrue(message.equals(decoded),"impact payload roundtrip");
        t.assertTrue(FlameVfx.DURATION==40&&FlameVfx.MAX_EMITTERS==32,"bounded two-second visual policy");t.succeed();
    }
}
