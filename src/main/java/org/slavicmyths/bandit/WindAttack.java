package org.slavicmyths.bandit;

import net.minecraft.block.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.*;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.server.ServerWorld;

/** Attack-time work only. At most 64 targets, 3 x 128 bounded voxel steps per target,
 * and 9 x 20 terrain samples; never requests an unloaded chunk. */
public final class WindAttack {
    private static final net.minecraft.tags.ITag.INamedTag<Block> FRAGILE=BlockTags.createOptional(new ResourceLocation("slavicmyths","whistle_fragile"));
    public static Vector3d facing(float yaw) { double a=Math.toRadians(yaw);return new Vector3d(-Math.sin(a),0,Math.cos(a)); }
    public static boolean inCone(Vector3d delta,Vector3d direction,double range,boolean radial) {
        return WindGeometry.contains(delta.x,delta.y,delta.z,direction.x,direction.z,range,radial);
    }
    private static boolean solid(ServerWorld w,BlockPos p) {
        if(!w.hasChunkAt(p))return true;
        BlockState s=w.getBlockState(p);
        return !s.is(BlockTags.LEAVES) && s.isCollisionShapeFullBlock(w,p);
    }
    public static float exposure(ServerWorld w,Vector3d from,LivingEntity target) {
        int clear=0;
        for(double height:new double[]{.25,.55,.8}) {
            Vector3d end=target.position().add(0,target.getBbHeight()*height,0),d=end.subtract(from);
            boolean blocked=!WindGeometry.clear(from.x,from.y,from.z,end.x,end.y,end.z,(x,y,z)->solid(w,new BlockPos(x,y,z)));
            if(!blocked)clear++;
        }
        return clear/3F;
    }
    public static void release(LivingEntity source,Vector3d direction,double range,float damage,float push,boolean radial,boolean playerArtifact) {
        if(!(source.level instanceof ServerWorld))return;
        ServerWorld w=(ServerWorld)source.level;int processed=0;
        for(LivingEntity victim:w.getEntitiesOfClass(LivingEntity.class,source.getBoundingBox().inflate(range))) {
            if(processed++>=64)break;
            if(victim==source||!victim.isAlive()||source.isAlliedTo(victim))continue;
            if(victim instanceof PlayerEntity&&(((PlayerEntity)victim).isCreative()||victim.isSpectator()))continue;
            if(source instanceof NightingaleEntity && victim instanceof BanditEntity)continue;
            // Player artifacts never damage or control bosses, including modded ones.
            if(playerArtifact&&(!victim.canChangeDimensions()||victim.getMaxHealth()>=100))continue;
            Vector3d delta=victim.position().subtract(source.position());
            if(!inCone(delta,direction,range,radial))continue;
            float cover=exposure(w,source.getEyePosition(1),victim);if(cover==0)continue;
            float falloff=(float)(1-.65*Math.min(1,delta.length()/range));
            victim.hurt(source instanceof PlayerEntity?DamageSource.playerAttack((PlayerEntity)source):DamageSource.mobAttack(source),damage*falloff*cover);
            victim.knockback(push*falloff*cover,-delta.x,-delta.z);victim.hurtMarked=true;
        }
    }
    public static void particles(ServerWorld w,Vector3d origin,Vector3d direction,double radius,boolean radial) {
        for(int i=0;i<9;i++) {
            double angle=radial?i*Math.PI*2/9:(i-4)*.075;
            Vector3d v=new Vector3d(direction.x*Math.cos(angle)-direction.z*Math.sin(angle),0,direction.x*Math.sin(angle)+direction.z*Math.cos(angle));
            Vector3d p=origin.add(v.scale(radius));BlockPos ground=new BlockPos(p).below();
            if(!w.hasChunkAt(ground))continue;
            w.sendParticles(ParticleTypes.CLOUD,p.x,p.y,p.z,1,.1,.15,.1,.025);
            if(i==4)w.sendParticles(new BlockParticleData(ParticleTypes.BLOCK,Blocks.OAK_LEAVES.defaultBlockState()),p.x,p.y,p.z,2,.2,.15,.2,.07);
            if(i%3==0)w.sendParticles(new BlockParticleData(ParticleTypes.BLOCK,w.getBlockState(ground)),p.x,p.y-.6,p.z,2,.15,.1,.15,.06);
        }
    }
    public static void terrain(LivingEntity source,Vector3d direction,int range,boolean destroy) {
        ServerWorld w=(ServerWorld)source.level;
        boolean grief=destroy&&w.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)&&net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(w,source);
        java.util.Set<BlockPos> visited=new java.util.HashSet<>();int broken=0;
        for(int lane=-4;lane<=4;lane++)for(int step=1;step<=range;step++) {
            double a=lane*.08;Vector3d v=new Vector3d(direction.x*Math.cos(a)-direction.z*Math.sin(a),0,direction.x*Math.sin(a)+direction.z*Math.cos(a));
            BlockPos p=new BlockPos(source.position().add(v.scale(step)));
            if(!w.hasChunkAt(p)||solid(w,p))break;
            if(!visited.add(p))continue;BlockState s=w.getBlockState(p);
            if(s.getBlock() instanceof FireBlock)w.removeBlock(p,false);
            else if(grief&&broken<24&&w.getBlockEntity(p)==null&&s.is(FRAGILE)) {
                w.destroyBlock(p,false,source);broken++;
            }
        }
    }
    private WindAttack(){}
}
