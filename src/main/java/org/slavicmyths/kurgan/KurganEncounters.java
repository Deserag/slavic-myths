package org.slavicmyths.kurgan;

import java.util.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slavicmyths.registry.ModEntities;
import static org.slavicmyths.kurgan.KurganFighter.*;

@Mod.EventBusSubscriber(modid="slavicmyths")
public final class KurganEncounters {
    public static EntityType<KurganCreature> type(Kind kind){switch(kind){case UPYR:return ModEntities.UPYR.get();case NAV:return ModEntities.NAV.get();case DRUZHINNIK:return ModEntities.DRUZHINNIK.get();case VOEVODA:return ModEntities.VOEVODA.get();case VOLKHV:return ModEntities.VOLKHV.get();default:return ModEntities.PRINCE.get();}}
    public static List<Kind> roster(KurganInstance i,KurganPlan.Room room){return KurganRoster.roster(i,room);}
    @SubscribeEvent public static void player(TickEvent.PlayerTickEvent e){if(e.phase!=TickEvent.Phase.END||!(e.player instanceof ServerPlayerEntity)||e.player.tickCount%20!=0||!e.player.isAlive()||e.player.isSpectator()||e.player.isCreative())return;
        ServerPlayerEntity player=(ServerPlayerEntity)e.player;ServerWorld world=player.getLevel();BurialRecords data=BurialRecords.get(world);KurganInstance i=data.at(player.blockPosition());if(i==null)return;
        if(i.plan.tier==2&&i.disturbance>=75){KurganPlan.Box seal=i.plan.seal;BlockPos anchor=i.origin.offset(seal.x1,seal.y0,((seal.z0+seal.z1)/2));if(player.blockPosition().distSqr(anchor)<100&&!i.bossDefeated&&(i.sealOpened||KurganDungeonPiece.openSeal(world,i.id)))trigger(world,data,i,i.plan.rooms.get(i.plan.finalRoom),player);}
        KurganPlan.Room room=i.room(player.blockPosition());if(room!=null&&(!room.hall()||i.sealOpened))trigger(world,data,i,room,player);
    }
    private static void trigger(ServerWorld world,BurialRecords data,KurganInstance i,KurganPlan.Room room,ServerPlayerEntity player){KurganEncounterState state=i.encounters.computeIfAbsent(room.id,id->new KurganEncounterState());if(state.triggered)return;
        List<Kind> roster=roster(i,room);if(roster.isEmpty())return;
        // Preflight all positions before committing the once-only encounter.
        List<KurganCreature> mobs=new ArrayList<>();List<BlockPos> positions=new ArrayList<>();int n=0;
        for(Kind kind:roster){KurganCreature mob=type(kind).create(world);if(mob==null)return;mob.kurgan=i.id;mob.room=room.id;mob.home=i.origin.offset(room.x,room.y+1,room.z);BlockPos p=safePosition(mob,new Vector3d(mob.home.getX()+(n==0?3:n==1?-3:0),mob.home.getY(),mob.home.getZ()+(n==2?4:0)),false);if(p==null)return;for(BlockPos used:positions)if(used.distSqr(p)<2)return;positions.add(p);mobs.add(mob);n++;}
        if(!state.start())return;data.setDirty();
        for(int index=0;index<mobs.size();index++){KurganCreature mob=mobs.get(index);BlockPos p=positions.get(index);mob.moveTo(p.getX()+.5,p.getY(),p.getZ()+.5,0,0);mob.setPersistenceRequired();mob.setTarget(player);if(world.addFreshEntity(mob))state.alive.add(mob.getUUID());}
        if(state.alive.isEmpty())state.cleared=true;data.setDirty();
    }
    public static BlockPos safePosition(KurganCreature mob,Vector3d desired,boolean sameRoom){if(!(mob.level instanceof ServerWorld))return null;ServerWorld world=(ServerWorld)mob.level;KurganInstance instance=mob.kurgan==null?null:BurialRecords.get(world).instances.get(mob.kurgan);KurganPlan.Room current=instance==null?null:instance.room(mob.blockPosition());
        BlockPos center=new BlockPos(desired);for(int r=0;r<=3;r++)for(int dx=-r;dx<=r;dx++)for(int dz=-r;dz<=r;dz++){if(Math.max(Math.abs(dx),Math.abs(dz))!=r)continue;for(int dy=-2;dy<=2;dy++){BlockPos p=center.offset(dx,dy,dz);if(!world.hasChunkAt(p)||!world.hasChunkAt(p.offset(-1,0,-1))||!world.hasChunkAt(p.offset(1,0,1))||!world.hasChunkAt(p.offset(-1,0,1))||!world.hasChunkAt(p.offset(1,0,-1))||!world.getWorldBorder().isWithinBounds(p))continue;
            if(instance!=null){KurganPlan.Room dest=instance.room(p);if(dest==null||(sameRoom&&current!=null&&dest.id!=current.id)||dest.id!=mob.room&&!sameRoom)continue;}
            if(!world.getBlockState(p.below()).isFaceSturdy(world,p.below(),Direction.UP)||!world.getFluidState(p).isEmpty())continue;
            AxisAlignedBB box=new AxisAlignedBB(p.getX()+.5-mob.getBbWidth()/2,p.getY(),p.getZ()+.5-mob.getBbWidth()/2,p.getX()+.5+mob.getBbWidth()/2,p.getY()+mob.getBbHeight(),p.getZ()+.5+mob.getBbWidth()/2);
            if(world.noCollision(mob,box))return p;
        }}return null;
    }
    public static void summon(KurganCreature owner,int count){ServerWorld world=(ServerWorld)owner.level;KurganInstance i=owner.kurgan==null?null:BurialRecords.get(world).instances.get(owner.kurgan);KurganEncounterState state=i==null?null:i.encounters.get(owner.room);
        for(int n=0;n<Math.min(3,count);n++){Kind kind=owner.kind==Kind.PRINCE&&n==2?Kind.DRUZHINNIK:n%2==0?Kind.NAV:Kind.UPYR;KurganCreature mob=type(kind).create(world);if(mob==null)continue;mob.kurgan=owner.kurgan;mob.room=owner.room;mob.home=owner.home==null?owner.blockPosition():owner.home;
            Vector3d desired=owner.position().add((n-1)*3,0,3);if(i!=null&&owner.kind==Kind.PRINCE&&n<i.plan.niches.size()){KurganPlan.Box niche=i.plan.niches.get(n);BlockPos p=i.origin.offset((niche.x0+niche.x1)/2,niche.y0,(niche.z0+niche.z1)/2);desired=new Vector3d(p.getX()+.5,p.getY(),p.getZ()+.5);}
            BlockPos p=safePosition(mob,desired,false);if(p==null)continue;mob.moveTo(p.getX()+.5,p.getY(),p.getZ()+.5,0,0);mob.setTarget(owner.getTarget());mob.setPersistenceRequired();if(world.addFreshEntity(mob)&&state!=null){state.alive.add(mob.getUUID());BurialRecords.get(world).setDirty();}}
    }
    public static void died(KurganCreature mob){if(mob.kurgan==null)return;ServerWorld world=(ServerWorld)mob.level;BurialRecords data=BurialRecords.get(world);KurganInstance i=data.instances.get(mob.kurgan);if(i==null)return;KurganEncounterState state=i.encounters.get(mob.room);if(state!=null&&state.death(mob.getUUID()))data.setDirty();
        if(mob.kind==Kind.PRINCE&&!i.bossDefeated){i.bossDefeated=true;data.setDirty();KurganCurse.sourceDefeated(world,i.id);}
    }
    @SubscribeEvent public static void healing(LivingHealEvent e){if(!e.getEntityLiving().level.isClientSide&&e.getEntityLiving() instanceof PlayerEntity&&e.getEntityLiving().getPersistentData().getLong("SlavicUpyrBiteUntil")>e.getEntityLiving().level.getGameTime())e.setAmount(e.getAmount()*.5F);}
}
