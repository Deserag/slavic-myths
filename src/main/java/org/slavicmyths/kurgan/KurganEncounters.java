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
    @SubscribeEvent public static void player(net.neoforged.neoforge.event.tick.PlayerTickEvent.Post e){if(!(e.getEntity() instanceof ServerPlayer)||e.getEntity().tickCount%20!=0||!e.getEntity().isAlive()||e.getEntity().isSpectator()||e.getEntity().isCreative())return;
        ServerPlayer player=(ServerPlayer)e.getEntity();ServerLevel world=player.serverLevel();BurialRecords data=BurialRecords.get(world);KurganInstance i=data.at(player.blockPosition());if(i==null)return;
        if(i.plan.tier==2&&i.disturbance>=75){KurganPlan.Box seal=i.plan.seal;BlockPos anchor=i.origin.offset(seal.x1,seal.y0,((seal.z0+seal.z1)/2));if(player.blockPosition().distSqr(anchor)<100&&!i.bossDefeated&&(i.sealOpened||KurganDungeonPiece.openSeal(world,i.id)))trigger(world,data,i,i.plan.rooms.get(i.plan.finalRoom),player);}
        KurganPlan.Room room=i.room(player.blockPosition());if(room!=null&&(!room.hall()||i.sealOpened))trigger(world,data,i,room,player);
    }
    private static void trigger(ServerLevel world,BurialRecords data,KurganInstance i,KurganPlan.Room room,ServerPlayer player){KurganEncounterState state=i.encounters.computeIfAbsent(room.id,id->new KurganEncounterState());if(state.triggered)return;
        List<Kind> roster=roster(i,room);if(roster.isEmpty())return;
        // Preflight all positions before committing the once-only encounter.
        List<KurganCreature> mobs=new ArrayList<>();List<BlockPos> positions=new ArrayList<>();int n=0;
        for(Kind kind:roster){KurganCreature mob=type(kind).create(world);if(mob==null)return;mob.kurgan=i.id;mob.room=room.id;mob.home=i.origin.offset(room.x,room.y+1,room.z);BlockPos p=safePosition(mob,new Vec3(mob.home.getX()+(n==0?3:n==1?-3:0),mob.home.getY(),mob.home.getZ()+(n==2?4:0)),false);if(p==null)return;for(BlockPos used:positions)if(used.distSqr(p)<2)return;positions.add(p);mobs.add(mob);n++;}
        if(!state.start())return;data.setDirty();
        for(int index=0;index<mobs.size();index++){KurganCreature mob=mobs.get(index);BlockPos p=positions.get(index);mob.moveTo(p.getX()+.5,p.getY(),p.getZ()+.5,0,0);mob.setPersistenceRequired();mob.setTarget(player);if(world.addFreshEntity(mob))state.alive.add(mob.getUUID());}
        if(state.alive.isEmpty())state.cleared=true;data.setDirty();
    }
    public static BlockPos safePosition(KurganCreature mob,Vec3 desired,boolean sameRoom){if(!(mob.level() instanceof ServerLevel))return null;ServerLevel world=(ServerLevel)mob.level();KurganInstance instance=mob.kurgan==null?null:BurialRecords.get(world).instances.get(mob.kurgan);KurganPlan.Room current=instance==null?null:instance.room(mob.blockPosition());
        BlockPos center=BlockPos.containing(desired);for(int r=0;r<=3;r++)for(int dx=-r;dx<=r;dx++)for(int dz=-r;dz<=r;dz++){if(Math.max(Math.abs(dx),Math.abs(dz))!=r)continue;for(int dy=-2;dy<=2;dy++){BlockPos p=center.offset(dx,dy,dz);if(!world.hasChunkAt(p)||!world.hasChunkAt(p.offset(-1,0,-1))||!world.hasChunkAt(p.offset(1,0,1))||!world.hasChunkAt(p.offset(-1,0,1))||!world.hasChunkAt(p.offset(1,0,-1))||!world.getWorldBorder().isWithinBounds(p))continue;
            if(instance!=null){KurganPlan.Room dest=instance.room(p);if(dest==null||(sameRoom&&current!=null&&dest.id!=current.id)||dest.id!=mob.room&&!sameRoom)continue;}
            if(!world.getBlockState(p.below()).isFaceSturdy(world,p.below(),Direction.UP)||!world.getFluidState(p).isEmpty())continue;
            AABB box=new AABB(p.getX()+.5-mob.getBbWidth()/2,p.getY(),p.getZ()+.5-mob.getBbWidth()/2,p.getX()+.5+mob.getBbWidth()/2,p.getY()+mob.getBbHeight(),p.getZ()+.5+mob.getBbWidth()/2);
            if(world.noCollision(mob,box))return p;
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
