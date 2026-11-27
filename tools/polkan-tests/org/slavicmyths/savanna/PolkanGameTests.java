package org.slavicmyths.savanna;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;
import org.slavicmyths.registry.ModEntities;

@GameTestHolder("slavicmyths_accessories278")
@PrefixGameTestTemplate(false)
public final class PolkanGameTests {
    private static <T extends Mob> T mob(GameTestHelper h,EntityType<T> type,Vec3 p){
        T m=type.create(h.getLevel());if(m==null)throw new IllegalStateException("Entity creation failed");
        m.moveTo(p);m.setNoAi(true);m.setNoGravity(true);m.setPersistenceRequired();
        h.assertTrue(h.getLevel().addFreshEntity(m),"Entity spawn failed");return m;
    }
    private static Vec3 origin(GameTestHelper h){return Vec3.atBottomCenterOf(h.absolutePos(new BlockPos(2,2,2)));}
    @GameTest(template="empty")
    public static void polkanRealHealthAndGuardStats(GameTestHelper h){
        var p=ModEntities.POLKAN.get().create(h.getLevel());var g=ModEntities.POLKAN_TARPAN.get().create(h.getLevel());var normal=ModEntities.TARPAN.get().create(h.getLevel());
        if(p==null||g==null||normal==null)throw new IllegalStateException("Entity creation failed");
        p.setHealth(1400);
        h.assertTrue(p.getMaxHealth()==1400&&p.getHealth()==1400,"Health capped");
        h.assertTrue(p.getAttributeValue(Attributes.MAX_HEALTH)==1400,"Uncapped attribute mismatch");
        h.assertTrue(p.getAttributeValue(Attributes.ARMOR)==18&&p.getAttributeValue(Attributes.ARMOR_TOUGHNESS)==6,"Wrong armor");
        h.assertTrue(g.getMaxHealth()==50&&normal.getMaxHealth()==32&&g.polkanGuard()&&!normal.polkanGuard(),"Guard changed ordinary tarpan");
        h.assertTrue(p.getTarget()==null&&p.action()==Polkan.IDLE,"Boss starts hostile");
        p.discard();g.discard();normal.discard();h.succeed();
    }
    @GameTest(template="empty")
    public static void polkanOnlyRetaliatesAgainstAggressors(GameTestHelper h){
        Vec3 pos=origin(h);var p=mob(h,ModEntities.POLKAN.get(),pos);var a=mob(h,EntityType.ZOMBIE,pos.add(0,0,5));var b=mob(h,EntityType.SKELETON,pos.add(5,0,0));var guard=mob(h,ModEntities.POLKAN_TARPAN.get(),pos.add(-4,0,0));
        h.assertTrue(!p.hostileTo(a)&&!p.hostileTo(b),"Neutral creatures selected");
        h.assertTrue(p.hurt(h.getLevel().damageSources().mobAttack(a),30),"Provocation failed");
        h.assertTrue(p.getTarget()==a&&p.hostileTo(a)&&!p.hostileTo(b),"Incorrect first target");
        p.provoke(b);p.provoke(guard);
        h.assertTrue(p.hostileTo(a)&&p.hostileTo(b)&&!p.hostileTo(guard),"Threat list or friendly filter invalid");
        p.discard();a.discard();b.discard();guard.discard();h.succeed();
    }
    @GameTest(template="empty")
    public static void polkanHerdIdentityAndLossPersistence(GameTestHelper h){
        var w=h.getLevel();var p=ModEntities.POLKAN.get().create(w);if(p==null)throw new IllegalStateException("Polkan missing");
        var guards=new ArrayList<SavannaAnimal>();
        for(int i=0;i<8;i++){var g=ModEntities.POLKAN_TARPAN.get().create(w);if(g==null)throw new IllegalStateException("Guard missing");guards.add(g);}
        var ledger=PolkanHerd.get(w);ledger.register(p.getUUID(),guards);
        for(int i=0;i<8;i++)h.assertTrue(ledger.member(p.getUUID(),guards.get(i).getUUID(),i),"Missing member");
        h.assertTrue(!ledger.member(p.getUUID(),guards.get(1).getUUID(),0),"Wrong slot accepted");
        for(int i=0;i<6;i++)ledger.dead(p.getUUID(),i);
        ledger.dead(p.getUUID(),0);
        h.assertTrue(ledger.losses(p.getUUID())==6,"Duplicate or missing guard death");
        var restored=PolkanHerd.load(ledger.save(new CompoundTag(),w.registryAccess()),w.registryAccess());
        h.assertTrue(restored.losses(p.getUUID())==6&&restored.member(p.getUUID(),guards.get(7).getUUID(),7),"Herd failed save/load");
        p.discard();guards.forEach(Entity::discard);h.succeed();
    }
    @GameTest(template="empty")
    public static void polkanDeathReleasesGuard(GameTestHelper h){
        var w=h.getLevel();var pos=origin(h);var p=mob(h,ModEntities.POLKAN.get(),pos);var g=mob(h,ModEntities.POLKAN_TARPAN.get(),pos.add(3,0,0));
        g.polkanOwner=p.getUUID();g.polkanSlot=0;PolkanHerd.get(w).register(p.getUUID(),List.of(g));
        p.hurt(w.damageSources().genericKill(),10000);
        h.assertTrue(PolkanHerd.get(w).ended(p.getUUID())&&g.polkanReleased&&g.polkanOwner==null&&g.getTarget()==null,"Leader death did not release escort");
        p.discard();g.discard();h.succeed();
    }
}