package org.slavicmyths.dungeon;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.tags.DamageTypeTags;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.*;
import org.slavicmyths.registry.ModEffects;

/** Buff is attached only to dungeon-spawned guardians. The normal world roster is unchanged. */
@EventBusSubscriber(modid="slavicmyths")
public final class DungeonGuardians {
    static final String GROUP="SlavicDungeonGroup", ORIGIN="SlavicDungeonOrigin", FLOOR="SlavicDungeonFloor", HP="SlavicDungeonLogicalHP";
    private static final ResourceLocation HEALTH=ResourceLocation.fromNamespaceAndPath("slavicmyths","dungeon_guardian_health");
    static Mob create(ServerLevel w,String id,BlockPos pos,BlockPos origin,int floor,UUID group,int encounter,int room,boolean roaming){
        EntityType<?> type=BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.fromNamespaceAndPath("slavicmyths",id));
        if(type==null||!(type.create(w) instanceof Mob mob))return null;
        mob.moveTo(pos.getX()+.5,pos.getY(),pos.getZ()+.5,0,0);
        mob.setPersistenceRequired();
        if(mob instanceof org.slavicmyths.underground.EncounterMob old)old.bind(pos,"");
        if(!w.noCollision(mob))return null;
        var tag=mob.getPersistentData();
        tag.putUUID(GROUP,group);tag.putLong(ORIGIN,origin.asLong());tag.putInt(FLOOR,floor);
        tag.putInt("SlavicDungeonEncounter",encounter);tag.putInt("SlavicDungeonRoom",room);tag.putBoolean("SlavicDungeonRoaming",roaming);
        double logical=baseLogicalHealth(mob)*1.5;
        var health=mob.getAttribute(Attributes.MAX_HEALTH);
        if(health==null)return null;
        health.addPermanentModifier(new AttributeModifier(HEALTH,.5,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        tag.putDouble(HP,logical);
        mob.setHealth(mob.getMaxHealth());
        mob.addEffect(new MobEffectInstance(ModEffects.DUNGEON_GUARDIAN,-1,0,false,true,true));
        return mob;
    }
    private static double baseLogicalHealth(LivingEntity mob){
        if(mob instanceof org.slavicmyths.desert.IndrikEntity)return org.slavicmyths.desert.IndrikRules.HP;
        if(mob instanceof org.slavicmyths.desert.StormIdolEntity)return org.slavicmyths.desert.StormIdolRules.HP;
        return mob.getMaxHealth();
    }
    public static boolean barVisible(LivingEntity boss,net.minecraft.server.level.ServerPlayer player){
        if(!guardian(boss))return true;
        if(!boss.isAlive()||boss.level()!=player.level()||player.isSpectator()||!DungeonPlan.inRange(boss.distanceToSqr(player)))return false;
        var tag=boss.getPersistentData();
        return DungeonRecords.get(player.serverLevel()).canSeeBoss(BlockPos.of(tag.getLong(ORIGIN)),tag.getInt(FLOOR),
            tag.contains("SlavicDungeonRoom")?tag.getInt("SlavicDungeonRoom"):-1,tag.getBoolean("SlavicDungeonRoaming"),player.blockPosition());
    }
    public static boolean guardian(Entity e){return e!=null&&e.getPersistentData().hasUUID(GROUP);}
    public static double logicalHealth(LivingEntity e){return guardian(e)?Math.max(e.getMaxHealth(),e.getPersistentData().getDouble(HP)):e.getMaxHealth();}
    private static UUID group(Entity e){
        if(guardian(e))return e.getPersistentData().getUUID(GROUP);
        if(e instanceof org.slavicmyths.desert.AntMinionEntity a&&!a.playerBound()&&a.ownerId()!=null&&a.level() instanceof ServerLevel w){
            Entity owner=w.getEntity(a.ownerId());if(guardian(owner))return owner.getPersistentData().getUUID(GROUP);
        }
        return null;
    }
    public static boolean allied(Entity a,Entity b){
        UUID g=group(a);return g!=null&&g.equals(group(b));
    }
    @SubscribeEvent public static void outgoing(LivingIncomingDamageEvent e){
        if(e.getEntity().level().isClientSide)return;
        Entity source=e.getSource().getEntity();
        if(source==null)return;
        if(allied(source,e.getEntity())){e.setCanceled(true);return;}
        if(guardian(source))e.setAmount(e.getAmount()*1.25F);
    }
    // Vanilla's maximum-health attribute caps at 1024. Normalize damage after armor
    // so a logical 1200-HP Sand King has the full intended health pool, without global cap changes.
    @SubscribeEvent public static void health(LivingDamageEvent.Pre e){
        LivingEntity target=e.getEntity();
        if(target.level().isClientSide||!guardian(target)||e.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY))return;
        if(target instanceof org.slavicmyths.desert.IndrikEntity||target instanceof org.slavicmyths.desert.StormIdolEntity){e.setNewDamage(e.getNewDamage()/1.5F);return;}
        double logical=logicalHealth(target);
        if(logical>target.getMaxHealth())e.setNewDamage((float)(e.getNewDamage()*target.getMaxHealth()/logical));
    }
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void death(LivingDeathEvent e){
        LivingEntity mob=e.getEntity();
        if(mob.level() instanceof ServerLevel w&&guardian(mob)){
            var tag=mob.getPersistentData();
            DungeonRecords.get(w).defeated(w,BlockPos.of(tag.getLong(ORIGIN)),tag.contains("SlavicDungeonEncounter")?tag.getInt("SlavicDungeonEncounter"):tag.getInt(FLOOR),mob.getUUID());
        }
    }
    private DungeonGuardians(){}
}
