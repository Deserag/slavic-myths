package org.slavicmyths.mor;

import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

/** Bounded 5x5x5 player-local contact sampling, once a second. Transient timers only. */
@EventBusSubscriber(modid="slavicmyths")
public final class MorBlockExposure {
    private static final ThreadLocal<java.util.UUID> APPLYING=new ThreadLocal<>();
    public static boolean applying(ServerPlayer p){return p.getUUID().equals(APPLYING.get());}
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue HURT;
    static {var b=new ModConfigSpec.Builder();HURT=b.define("morBlocksHurtPlayers",true);SPEC=b.build();}
    @SubscribeEvent public static void tick(PlayerTickEvent.Post event) {
        if(!(event.getEntity() instanceof ServerPlayer p)||p.tickCount%20!=0)return;
        var n=p.getPersistentData();
        if(!p.isAlive()||p.isCreative()||p.isSpectator()||org.slavicmyths.poloz.PolozRewards.full(p)) {n.remove("MorBlockSeconds");return;}
        boolean slow=false,weak=false,dot=false,blind=false,found=false;
        Vec3 feet=p.position().add(0,.15,0);
        for(BlockPos pos:BlockPos.betweenClosed(p.blockPosition().offset(-2,-1,-2),p.blockPosition().offset(2,2,2))) {
            if(!p.serverLevel().hasChunkAt(pos))continue;
            int i=MorBlocks.index(p.level().getBlockState(pos));if(i<0)continue;
            Vec3 point=new Vec3(Math.clamp(feet.x,pos.getX()+.01,pos.getX()+.99),Math.clamp(feet.y,pos.getY()+.01,pos.getY()+.99),Math.clamp(feet.z,pos.getZ()+.01,pos.getZ()+.99));
            if(feet.distanceToSqr(point)>2.25)continue;
            var hit=p.level().clip(new ClipContext(feet,point,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p));
            if(hit.getType()!=HitResult.Type.MISS&&!hit.getBlockPos().equals(pos))continue;
            found=true;slow|=i==0||i==1||i==3||i==5||i==9||i==13;weak|=i==2||i==5||i==6||i==8||i==10||i==12;
            dot|=i==0||i==1||i==4||i==7||i==11;blind|=i==7;
        }
        if(!found){n.remove("MorBlockSeconds");return;}
        int seconds=n.contains("MorBlockSeconds")?n.getInt("MorBlockSeconds")+1:0;n.putInt("MorBlockSeconds",seconds);
        if(seconds==0)org.slavicmyths.tales.TaleService.promote(p,"slavicmyths:mor_block_trace",2,false);
        APPLYING.set(p.getUUID());try {if(seconds>=3) {
            if(slow)p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,org.slavicmyths.sanctuary.SanctuaryEvents.morDuration(p,(p.hasEffect(org.slavicmyths.quest185.Quest185Content.WARD_EFFECT)?20:40)),0));
            if(weak)p.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,org.slavicmyths.sanctuary.SanctuaryEvents.morDuration(p,(p.hasEffect(org.slavicmyths.quest185.Quest185Content.WARD_EFFECT)?20:40)),0));
            if(seconds==3)p.playNotifySound(net.minecraft.sounds.SoundEvents.DEEPSLATE_STEP,net.minecraft.sounds.SoundSource.BLOCKS,.25f,.7f);
        }
        if(seconds>=5) {
            if(dot&&seconds%5==0&&HURT.get())p.hurt(new DamageSource(p.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(net.minecraft.resources.ResourceKey.create(Registries.DAMAGE_TYPE,ResourceLocation.fromNamespaceAndPath("slavicmyths","mor_block")))),1);
            long now=p.level().getGameTime();
            if(blind&&now>=n.getLong("MorBlockBlindUntil")){p.addEffect(new MobEffectInstance(MobEffects.BLINDNESS,org.slavicmyths.sanctuary.SanctuaryEvents.morDuration(p,(p.hasEffect(org.slavicmyths.quest185.Quest185Content.WARD_EFFECT)?20:40)),0));n.putLong("MorBlockBlindUntil",now+300);}
        }}finally{APPLYING.remove();}
    }
    public static void clearCleansed(ServerPlayer p){Vec3 feet=p.position().add(0,.15,0);for(BlockPos pos:BlockPos.betweenClosed(p.blockPosition().offset(-2,-1,-2),p.blockPosition().offset(2,2,2))){if(!p.serverLevel().hasChunkAt(pos)||MorBlocks.index(p.level().getBlockState(pos))<0)continue;Vec3 point=new Vec3(Math.clamp(feet.x,pos.getX()+.01,pos.getX()+.99),Math.clamp(feet.y,pos.getY()+.01,pos.getY()+.99),Math.clamp(feet.z,pos.getZ()+.01,pos.getZ()+.99));if(feet.distanceToSqr(point)<=2.25){var hit=p.level().clip(new ClipContext(feet,point,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p));if(hit.getType()==HitResult.Type.MISS||hit.getBlockPos().equals(pos))return;}}for(var effect:java.util.List.of(MobEffects.MOVEMENT_SLOWDOWN,MobEffects.WEAKNESS,MobEffects.BLINDNESS)){String key="Quest185Ground."+effect.value().getDescriptionId();if(p.getPersistentData().getBoolean(key)&&p.getEffect(effect)!=null&&p.getEffect(effect).getAmplifier()==0)p.removeEffect(effect);p.getPersistentData().remove(key);}p.getPersistentData().remove("MorBlockSeconds");}
    private MorBlockExposure() {}
}
