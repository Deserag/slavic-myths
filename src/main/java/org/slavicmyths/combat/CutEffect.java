package org.slavicmyths.combat;

import net.minecraft.core.registries.Registries;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.*;
import net.minecraft.world.damagesource.DamageTypes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import org.slavicmyths.registry.ModEffects;
import org.slavicmyths.registry.ModItems;

/** One native effect instance per victim; its remaining duration is the only expiration clock. */
@EventBusSubscriber(modid="slavicmyths")
public final class CutEffect extends MobEffect {
    public static final TagKey<EntityType<?>> IMMUNE=TagKey.create(Registries.ENTITY_TYPE,ResourceLocation.fromNamespaceAndPath("slavicmyths","bleed_immune"));
    private static final String OWNER="SlavicCutOwner",OWNER_NAME="SlavicCutOwnerName",PULSE="SlavicCutPulse";
    public CutEffect(){super(MobEffectCategory.HARMFUL,0x691C24);}
    public static boolean permitted(Player attacker,LivingEntity target){
        return target.isAlive()&&!target.getType().is(IMMUNE)&&target!=attacker&&
            (!(target instanceof Player other)||attacker.getServer()!=null&&attacker.getServer().isPvpAllowed()&&attacker.canHarmPlayer(other));
    }
    @SubscribeEvent public static void hit(LivingDamageEvent.Post e){
        if(e.getNewDamage()<=0||!e.getSource().is(DamageTypes.PLAYER_ATTACK)||!(e.getSource().getDirectEntity() instanceof ServerPlayer attacker)
            ||!attacker.getMainHandItem().is(ModItems.NIGHTINGALE_DAGGER.get()))return;
        apply(attacker,e.getEntity());
    }
    public static void apply(ServerPlayer attacker,LivingEntity target){
        if(!permitted(attacker,target))return;
        var old=target.getEffect(ModEffects.CUT);int stacks=old==null?1:Math.min(5,old.getAmplifier()+2);
        var data=target.getPersistentData();if(old==null)data.putInt(PULSE,20);
        // Explicit replacement prevents hidden lower-amplifier instances from surviving expiration.
        target.removeEffect(ModEffects.CUT);
        if(!target.addEffect(new MobEffectInstance(ModEffects.CUT,100,stacks-1,false,false,true),attacker))return;
        data.putUUID(OWNER,attacker.getUUID());
        data.putString(OWNER_NAME,attacker.getScoreboardName());
        ((ServerLevel)target.level()).sendParticles(new DustParticleOptions(new org.joml.Vector3f(.30F,.025F,.04F),.65F),target.getX(),target.getY()+target.getBbHeight()*.5,target.getZ(),stacks==5?4:2,.12,.15,.12,0);
    }
    @Override public boolean shouldApplyEffectTickThisTick(int duration,int amplifier){return true;}
    @Override public boolean applyEffectTick(LivingEntity target,int amplifier){
        if(!(target.level() instanceof ServerLevel world))return true;
        if(target.getType().is(IMMUNE))return false;
        var data=target.getPersistentData();
        Player owner=data.hasUUID(OWNER)?world.getServer().getPlayerList().getPlayer(data.getUUID(OWNER)):null;
        if(target instanceof Player player){
            if(!world.getServer().isPvpAllowed()||owner!=null&&!permitted(owner,target))return false;
            var team=world.getScoreboard().getPlayersTeam(data.getString(OWNER_NAME));
            if(team!=null&&team==player.getTeam()&&!team.isAllowFriendlyFire())return false;
        }
        int pulse=data.getInt(PULSE)-1;
        if(pulse<=0){pulse=20;target.hurt(owner==null?MythDamageSources.unattributed("cut",target):MythDamageSources.periodic("cut",owner),.25F*Math.min(5,amplifier+1));}
        data.putInt(PULSE,pulse);return true;
    }
}
