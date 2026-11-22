package org.slavicmyths.combat;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/** Local, monotonic escape from existing block overlap; never searches distant teleport sites. */
@EventBusSubscriber(modid="slavicmyths")
public final class LargeMobRecovery {
    private static boolean supported(Mob m) {
        return m instanceof org.slavicmyths.gorynych.Gorynych
            || m instanceof org.slavicmyths.morovoy.MorovoyRaven
            || m instanceof org.slavicmyths.morovoy.MorovoyFireZmey
            || m instanceof org.slavicmyths.marine.OceanZmeyEntity
            || m instanceof org.slavicmyths.marine.DeepSeaSerpentEntity
            || m instanceof org.slavicmyths.marine.StratimBirdEntity
            || m instanceof org.slavicmyths.marine.ZhelvaEntity
            || m instanceof org.slavicmyths.marine.TsarPikeEntity
            || m instanceof org.slavicmyths.marine.PharaohkaEntity;
    }
    @SubscribeEvent public static void tick(EntityTickEvent.Post event) {
        if(event.getEntity() instanceof Mob m && !m.level().isClientSide && m.tickCount%10==0
                && supported(m) && !m.isNoAi() && !BossThreat.attacking(m)) recover(m);
    }
    private static boolean loaded(Mob m,AABB box) {
        if(box.minY<m.level().getMinBuildHeight() || box.maxY>m.level().getMaxBuildHeight()
                || !m.level().getWorldBorder().isWithinBounds(box)) return false;
        for(int x=((int)Math.floor(box.minX))>>4;x<=((int)Math.floor(box.maxX))>>4;x++)
            for(int z=((int)Math.floor(box.minZ))>>4;z<=((int)Math.floor(box.maxZ))>>4;z++)
                if(!m.level().hasChunkAt(new BlockPos(x<<4,(int)box.minY,z<<4)))return false;
        return true;
    }
    private static double overlap(Mob m,AABB box) {
        double volume=0;
        for(var shape:m.level().getBlockCollisions(m,box))for(var block:shape.toAabbs())
            volume+=Math.max(0,Math.min(box.maxX,block.maxX)-Math.max(box.minX,block.minX))
                *Math.max(0,Math.min(box.maxY,block.maxY)-Math.max(box.minY,block.minY))
                *Math.max(0,Math.min(box.maxZ,block.maxZ)-Math.max(box.minZ,block.minZ));
        return volume;
    }
    private static boolean wet(Mob m,AABB box) {
        for(double x:new double[]{box.minX+.01,(box.minX+box.maxX)/2,box.maxX-.01})
            for(double z:new double[]{box.minZ+.01,(box.minZ+box.maxZ)/2,box.maxZ-.01})
                if(m.level().getFluidState(BlockPos.containing(x,box.minY+.2,z)).is(net.minecraft.tags.FluidTags.WATER))return true;
        return false;
    }    public static boolean recover(Mob m) {
        if(m.level().isClientSide || !m.isAlive() || m.isRemoved() || m.noPhysics
                || m.isPassenger() || m.isVehicle())return false;
        if(m instanceof org.slavicmyths.gorynych.Gorynych g && !g.fighting()
                && g.state()!=org.slavicmyths.gorynych.GorynychRules.PATROL)return false;
        var original=m.getBoundingBox().deflate(.001);
        if(!loaded(m,original))return false;
        double initial=overlap(m,original);
        if(initial<.000001)return false;
        Vec3 best=null;double least=initial;
        for(int i=0;i<16;i++){
            double angle=i*Math.PI/8;
            var step=new Vec3(Math.cos(angle)*.25,0,Math.sin(angle)*.25);
            var box=original.move(step);var route=original.expandTowards(step);
            if(!loaded(m,route)||!m.isWithinRestriction(BlockPos.containing(m.position().add(step)))
                    ||!m.level().getEntityCollisions(m,box).isEmpty())continue;
            if(m.isInWaterOrBubble()&&!wet(m,box))continue;
            if(m.onGround()&&m.level().noCollision(m,new AABB(box.minX,box.minY-.125,box.minZ,box.maxX,box.minY,box.maxZ)))continue;
            boolean newObstacle=false;
            for(var shape:m.level().getBlockCollisions(m,route))for(var obstacle:shape.toAabbs())
                if(!obstacle.intersects(original)){newObstacle=true;break;}
            if(newObstacle)continue;
            double remaining=overlap(m,box);
            if(remaining>initial+.000001)continue;
            // A log entirely inside a wide body produces a flat overlap plateau.
            // Look ahead along the same local route, but move only the first quarter-block.
            double previous=remaining;
            if(remaining>=initial-.000001){
                int probes=(int)Math.ceil(Math.min(8,m.getBbWidth()/2D+2)*4);
                for(int n=2;n<=probes;n++){
                    var ahead=original.move(step.scale(n));var swept=original.expandTowards(step.scale(n));
                    if(!loaded(m,swept)||!m.isWithinRestriction(BlockPos.containing(m.position().add(step.scale(n)))))break;
                    boolean blocked=!m.level().getEntityCollisions(m,ahead).isEmpty();
                    for(var shape:m.level().getBlockCollisions(m,swept))for(var obstacle:shape.toAabbs())
                        if(!obstacle.intersects(original))blocked=true;
                    if(blocked)break;
                    if(m.isInWaterOrBubble()&&!wet(m,ahead))break;
                    if(m.onGround()&&m.level().noCollision(m,new AABB(ahead.minX,ahead.minY-.125,ahead.minZ,ahead.maxX,ahead.minY,ahead.maxZ)))break;
                    double next=overlap(m,ahead);if(next>previous+.000001)break;
                    previous=next;
                    if(next<initial-.000001){remaining=next;break;}
                }
            }
            if(remaining<least-.000001){least=remaining;best=step;}
        }
        if(best==null)return false;
        m.getNavigation().stop();m.setPos(m.position().add(best));m.setDeltaMovement(Vec3.ZERO);m.hasImpulse=true;
        return true;
    }
    private LargeMobRecovery(){}
}