package org.slavicmyths.bandit;

import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.particles.*;
import net.minecraft.tags.BlockTags;
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
import net.minecraft.world.level.GameRules;
import net.minecraft.server.level.ServerLevel;

/** Attack-time work only. At most 64 targets, 3 x 128 bounded voxel steps per target,
 * and 9 x 20 terrain samples; never requests an unloaded chunk. */
public final class WindAttack {
    private static final net.minecraft.tags.TagKey<Block> FRAGILE=net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK,ResourceLocation.fromNamespaceAndPath("slavicmyths","whistle_fragile"));
    public static Vec3 facing(float yaw) { double a=Math.toRadians(yaw);return new Vec3(-Math.sin(a),0,Math.cos(a)); }
    public static boolean inCone(Vec3 delta,Vec3 direction,double range,boolean radial) {
        return WindGeometry.contains(delta.x,delta.y,delta.z,direction.x,direction.z,range,radial);
    }
    private static boolean solid(ServerLevel w,BlockPos p) {
        if(!w.hasChunkAt(p))return true;
        BlockState s=w.getBlockState(p);
        return !s.is(BlockTags.LEAVES) && s.isCollisionShapeFullBlock(w,p);
    }
    public static float exposure(ServerLevel w,Vec3 from,LivingEntity target) {
        int clear=0;
        for(double height:new double[]{.25,.55,.8}) {
            Vec3 end=target.position().add(0,target.getBbHeight()*height,0),d=end.subtract(from);
            boolean blocked=!WindGeometry.clear(from.x,from.y,from.z,end.x,end.y,end.z,(x,y,z)->solid(w,new BlockPos(x,y,z)));
            if(!blocked)clear++;
        }
        return clear/3F;
    }
    public static void release(LivingEntity source,Vec3 direction,double range,float damage,float push,boolean radial,boolean playerArtifact) {
        if(!(source.level() instanceof ServerLevel))return;
        ServerLevel w=(ServerLevel)source.level();int processed=0;
        for(LivingEntity victim:w.getEntitiesOfClass(LivingEntity.class,source.getBoundingBox().inflate(range))) {
            if(processed++>=64)break;
            if(victim==source||!victim.isAlive()||source.isAlliedTo(victim))continue;
            if(victim instanceof Player&&(((Player)victim).isCreative()||victim.isSpectator()))continue;
            if(source instanceof NightingaleEntity && victim instanceof BanditEntity)continue;
            // Player artifacts never damage or control bosses, including modded ones.
            if(playerArtifact&&(!victim.canChangeDimensions(victim.level(),victim.level())||victim.getMaxHealth()>=100))continue;
            Vec3 delta=victim.position().subtract(source.position());
            if(!inCone(delta,direction,range,radial))continue;
            float cover=exposure(w,source.getEyePosition(1),victim);if(cover==0)continue;
            float falloff=(float)(1-.65*Math.min(1,delta.length()/range));
            victim.hurt(source instanceof Player?source.damageSources().playerAttack((Player)source):source.damageSources().mobAttack(source),damage*falloff*cover);
            victim.knockback(push*falloff*cover,-delta.x,-delta.z);victim.hurtMarked=true;
        }
    }
    public static void particles(ServerLevel w,Vec3 origin,Vec3 direction,double radius,boolean radial) {
        for(int i=0;i<9;i++) {
            double angle=radial?i*Math.PI*2/9:(i-4)*.075;
            Vec3 v=new Vec3(direction.x*Math.cos(angle)-direction.z*Math.sin(angle),0,direction.x*Math.sin(angle)+direction.z*Math.cos(angle));
            Vec3 p=origin.add(v.scale(radius));BlockPos ground=BlockPos.containing(p).below();
            if(!w.hasChunkAt(ground))continue;
            w.sendParticles(ParticleTypes.CLOUD,p.x,p.y,p.z,1,.1,.15,.1,.025);
            if(i==4)w.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK,Blocks.OAK_LEAVES.defaultBlockState()),p.x,p.y,p.z,2,.2,.15,.2,.07);
            if(i%3==0)w.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK,w.getBlockState(ground)),p.x,p.y-.6,p.z,2,.15,.1,.15,.06);
        }
    }
    public static void terrain(LivingEntity source,Vec3 direction,int range,boolean destroy) {
        ServerLevel w=(ServerLevel)source.level();
        boolean grief=destroy&&w.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)&&net.neoforged.neoforge.event.EventHooks.canEntityGrief(w,source);
        java.util.Set<BlockPos> visited=new java.util.HashSet<>();int broken=0;
        for(int lane=-4;lane<=4;lane++)for(int step=1;step<=range;step++) {
            double a=lane*.08;Vec3 v=new Vec3(direction.x*Math.cos(a)-direction.z*Math.sin(a),0,direction.x*Math.sin(a)+direction.z*Math.cos(a));
            BlockPos p=BlockPos.containing(source.position().add(v.scale(step)));
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
