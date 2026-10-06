package org.slavicmyths.kurgan;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.*;
import net.minecraft.sounds.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.*;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import org.slavicmyths.registry.ModEntities;
import static org.slavicmyths.kurgan.KurganFighter.*;

@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths")
public final class KurganEncounters {
    public static EntityType<KurganCreature> type(Kind kind){switch(kind){case UPYR:return ModEntities.UPYR.get();case NAV:return ModEntities.NAV.get();case DRUZHINNIK:return ModEntities.DRUZHINNIK.get();case VOEVODA:return ModEntities.VOEVODA.get();case VOLKHV:return ModEntities.VOLKHV.get();default:return ModEntities.PRINCE.get();}}
    public static List<Kind> roster(KurganInstance i,KurganPlan.Room room){return KurganRoster.roster(i,room);}
    @SubscribeEvent public static void player(net.neoforged.neoforge.event.tick.PlayerTickEvent.Post e){if(!(e.getEntity() instanceof ServerPlayer)||e.getEntity().tickCount%20!=0||!e.getEntity().isAlive()||e.getEntity().isSpectator())return;
        ServerPlayer player=(ServerPlayer)e.getEntity();ServerLevel world=player.serverLevel();BurialRecords data=BurialRecords.get(world);KurganInstance i=data.at(player.blockPosition());if(i==null)return;
        if(i.plan.tier==2&&i.disturbance>=75){KurganPlan.Box seal=i.plan.seal;BlockPos anchor=i.origin.offset(seal.x1,seal.y0,((seal.z0+seal.z1)/2));if(player.blockPosition().distSqr(anchor)<100&&!i.bossDefeated&&(i.sealOpened||KurganDungeonPiece.openSeal(world,i.id)))trigger(world,data,i,i.plan.rooms.get(i.plan.finalRoom),player);}
        KurganPlan.Room room=i.room(player.blockPosition());if(room!=null&&(!room.hall()||i.sealOpened))trigger(world,data,i,room,player);
        else for(var nearby:i.plan.rooms)if(!nearby.hall()&&player.blockPosition().distSqr(i.origin.offset(nearby.x,nearby.y+1,nearby.z))<=64)trigger(world,data,i,nearby,player);
    }
    public static void trigger(ServerLevel world,BurialRecords data,KurganInstance i,KurganPlan.Room room,ServerPlayer player){
        if(room.hall()&&(!i.sealOpened||i.bossDefeated))return;
        KurganEncounterState state=i.encounters.computeIfAbsent(room.id,id->new KurganEncounterState());
        if(state.cleared)return;
        List<Kind> roster=roster(i,room);if(roster.isEmpty())return;
        // Older active rooms retain their original UUID roster, never silently reroll it.
        if(state.triggered&&state.slots.isEmpty())return;
        state.prepare(roster);boolean changed=false;
        for(var slot:state.slots){
            if(slot.defeated)continue;
            if(slot.uuid!=null){
                Entity existing=world.getEntity(slot.uuid);
                if(existing!=null){if(existing.isAlive())slot.last=existing.blockPosition();continue;}
                // FULL blocks do not imply entities have loaded. Never force-load to inspect a UUID.
                if(slot.restored||slot.last==null||!world.areEntitiesLoaded(net.minecraft.world.level.ChunkPos.asLong(slot.last)))continue;
            }
            KurganCreature mob=type(slot.kind).create(world);if(mob==null)throw new IllegalStateException("Missing kurgan entity "+slot.kind);
            mob.kurgan=i.id;mob.room=room.id;mob.home=i.origin.offset(room.x,room.y+1,room.z);
            BlockPos spot=encounterPosition(mob,room,player);if(spot==null)continue;
            mob.moveTo(spot.getX()+.5,spot.getY(),spot.getZ()+.5,0,0);mob.setPersistenceRequired();if(!player.isCreative())mob.setTarget(player);
            if(!world.addFreshEntity(mob))continue;
            if(slot.uuid!=null){state.alive.remove(slot.uuid);state.retired.add(slot.uuid);slot.restored=true;}
            slot.uuid=mob.getUUID();slot.last=spot;state.alive.add(slot.uuid);state.start();changed=true;
        }
        if(changed)data.setDirty();
    }
    private static BlockPos encounterPosition(KurganCreature mob,KurganPlan.Room room,ServerPlayer player){
        for(int distance:new int[]{3,1})for(int dx=-room.rx+1;dx<room.rx;dx++)for(int dz=-room.rz+1;dz<room.rz;dz++){
            BlockPos desired=mob.home.offset(dx,0,dz);if(desired.distSqr(player.blockPosition())<distance*distance)continue;
            BlockPos p=safePosition(mob,Vec3.atBottomCenterOf(desired),false);
            if(p!=null&&p.distSqr(player.blockPosition())>=distance*distance&&((ServerLevel)mob.level()).clip(new ClipContext(player.getEyePosition(),new Vec3(p.getX()+.5,p.getY()+1,p.getZ()+.5),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,player)).getType()==HitResult.Type.MISS)return p;
        }return null;
    }
    @SubscribeEvent public static void leaving(net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent e){
        if(!(e.getEntity() instanceof KurganCreature mob)||!(e.getLevel() instanceof ServerLevel world)||mob.kurgan==null)return;
        var data=BurialRecords.get(world);var instance=data.instances.get(mob.kurgan);if(instance==null)return;
        var state=instance.encounters.get(mob.room);if(state==null)return;
        for(var slot:state.slots)if(mob.getUUID().equals(slot.uuid)){slot.last=mob.blockPosition();data.setDirty();}
    }
    @SubscribeEvent public static void crossing(net.neoforged.neoforge.event.entity.EntityEvent.EnteringSection e){
        if(!e.didChunkChange()||!(e.getEntity() instanceof KurganCreature mob)||!(mob.level() instanceof ServerLevel world)||mob.kurgan==null)return;
        var data=BurialRecords.get(world);var instance=data.instances.get(mob.kurgan);if(instance==null)return;
        var state=instance.encounters.get(mob.room);if(state==null)return;
        for(var slot:state.slots)if(mob.getUUID().equals(slot.uuid)){slot.last=mob.blockPosition();data.setDirty();}
    }
    @SubscribeEvent public static void joining(net.neoforged.neoforge.event.entity.EntityJoinLevelEvent e){
        if(!(e.getEntity() instanceof KurganCreature mob)||!(e.getLevel() instanceof ServerLevel world)||mob.kurgan==null)return;
        var instance=BurialRecords.get(world).instances.get(mob.kurgan);if(instance==null)return;
        var state=instance.encounters.get(mob.room);if(state!=null&&state.retired.contains(mob.getUUID()))e.setCanceled(true);
    }
    public static BlockPos safePosition(KurganCreature mob,Vec3 desired,boolean sameRoom){if(!(mob.level() instanceof ServerLevel))return null;ServerLevel world=(ServerLevel)mob.level();KurganInstance instance=mob.kurgan==null?null:BurialRecords.get(world).instances.get(mob.kurgan);KurganPlan.Room current=instance==null?null:instance.room(mob.blockPosition());
        BlockPos center=BlockPos.containing(desired);for(int r=0;r<=3;r++)for(int dx=-r;dx<=r;dx++)for(int dz=-r;dz<=r;dz++){if(Math.max(Math.abs(dx),Math.abs(dz))!=r)continue;for(int dy=-2;dy<=2;dy++){BlockPos p=center.offset(dx,dy,dz);if(!world.hasChunkAt(p)||!world.hasChunkAt(p.offset(-1,0,-1))||!world.hasChunkAt(p.offset(1,0,1))||!world.hasChunkAt(p.offset(-1,0,1))||!world.hasChunkAt(p.offset(1,0,-1))||!world.getWorldBorder().isWithinBounds(p))continue;
            if(instance!=null){KurganPlan.Room dest=instance.room(p);if(dest==null||(sameRoom&&current!=null&&dest.id!=current.id)||dest.id!=mob.room&&!sameRoom)continue;}
            if(world.getBlockState(p).getBlock() instanceof net.minecraft.world.level.block.BasePressurePlateBlock||world.getBlockState(p.below()).is(net.minecraft.world.level.block.Blocks.MAGMA_BLOCK))continue;
            if(!world.getBlockState(p.below()).isFaceSturdy(world,p.below(),Direction.UP)||!world.getFluidState(p).isEmpty())continue;
            AABB box=new AABB(p.getX()+.5-mob.getBbWidth()/2,p.getY(),p.getZ()+.5-mob.getBbWidth()/2,p.getX()+.5+mob.getBbWidth()/2,p.getY()+mob.getBbHeight(),p.getZ()+.5+mob.getBbWidth()/2);
            if(world.noCollision(mob,box)&&world.getEntitiesOfClass(LivingEntity.class,box,e->e!=mob&&e.isAlive()).isEmpty())return p;
        }}return null;
    }
    public static void summon(KurganCreature owner,int count){ServerLevel world=(ServerLevel)owner.level();KurganInstance i=owner.kurgan==null?null:BurialRecords.get(world).instances.get(owner.kurgan);KurganEncounterState state=i==null?null:i.encounters.get(owner.room);
        for(int n=0;n<Math.min(3,count);n++){Kind kind=owner.kind==Kind.PRINCE&&n==2?Kind.DRUZHINNIK:n%2==0?Kind.NAV:Kind.UPYR;KurganCreature mob=type(kind).create(world);if(mob==null)continue;mob.kurgan=owner.kurgan;mob.room=owner.room;mob.home=owner.home==null?owner.blockPosition():owner.home;
            Vec3 desired=owner.position().add((n-1)*3,0,3);if(i!=null&&owner.kind==Kind.PRINCE&&n<i.plan.niches.size()){KurganPlan.Box niche=i.plan.niches.get(n);BlockPos p=i.origin.offset((niche.x0+niche.x1)/2,niche.y0,(niche.z0+niche.z1)/2);desired=new Vec3(p.getX()+.5,p.getY(),p.getZ()+.5);}
            BlockPos p=safePosition(mob,desired,false);if(p==null)continue;mob.moveTo(p.getX()+.5,p.getY(),p.getZ()+.5,0,0);mob.setTarget(owner.getTarget());mob.setPersistenceRequired();if(world.addFreshEntity(mob)&&state!=null){state.alive.add(mob.getUUID());BurialRecords.get(world).setDirty();}}
    }
    public static void died(KurganCreature mob){if(mob.kurgan==null)return;ServerLevel world=(ServerLevel)mob.level();BurialRecords data=BurialRecords.get(world);KurganInstance i=data.instances.get(mob.kurgan);if(i==null)return;KurganEncounterState state=i.encounters.get(mob.room);if(state!=null&&state.death(mob.getUUID()))data.setDirty();
        if(mob.kind==Kind.PRINCE&&!i.bossDefeated){i.bossDefeated=true;data.setDirty();KurganCurse.sourceDefeated(world,i.id);}
    }
    @SubscribeEvent public static void healing(LivingHealEvent e){if(!e.getEntity().level().isClientSide&&e.getEntity() instanceof Player&&e.getEntity().getPersistentData().getLong("SlavicUpyrBiteUntil")>e.getEntity().level().getGameTime())e.setAmount(e.getAmount()*.5F);}
}
