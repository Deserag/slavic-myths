package org.slavicmyths.combat;

import net.minecraft.world.effect.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerLevel;
import org.slavicmyths.registry.ModEffects;

/** Separate cadence from the existing nightingale cut; never a melee source. */
public final class RareBleedEffect extends MobEffect {
    public RareBleedEffect(){super(MobEffectCategory.HARMFUL,0x8c2528);}
    public static void apply(net.minecraft.server.level.ServerPlayer p,LivingEntity target){apply(p,target,false);}
    public static void applyRune(net.minecraft.server.level.ServerPlayer p,LivingEntity target){apply(p,target,true);}
    private static void apply(net.minecraft.server.level.ServerPlayer p,LivingEntity target,boolean rune){
        if(!CutEffect.permitted(p,target))return;var old=current(target);var data=target.getPersistentData();long now=RareCombat.now(target);
        if(old==null){data.remove("RareBleedWolfUntil");data.remove("RareBleedWolfStacks");data.remove("RareBleedWolfActor");data.putInt("RareBleedNative",0);data.putInt("RareBleedRune",0);data.putLong("RareBleedNativeUntil",0);data.putLong("RareBleedRuneUntil",0);data.putLong("RareBleedPulse",now+org.slavicmyths.rpg.RuneBalance.BLEED_INTERVAL);}
        else if(!data.getBoolean("RareBleedSources")){data.putInt("RareBleedNative",Math.min(3,old.getAmplifier()+1));data.putLong("RareBleedNativeUntil",now+old.getDuration());if(data.hasUUID("RareBleedOwner"))data.putUUID("RareBleedNativeOwner",data.getUUID("RareBleedOwner"));}
        data.putBoolean("RareBleedSources",true);String source=rune?"Rune":"Native";int max=rune?org.slavicmyths.rpg.RuneBalance.RUNE_BLEED_MAX:org.slavicmyths.rpg.RuneBalance.NATIVE_BLEED_MAX;
        int before=data.getLong("RareBleed"+source+"Until")>now?data.getInt("RareBleed"+source):0;
        data.putInt("RareBleed"+source,Math.min(max,before+1));data.putLong("RareBleed"+source+"Until",now+org.slavicmyths.rpg.RuneBalance.BLEED_DURATION);data.putUUID("RareBleed"+source+"Owner",p.getUUID());
        int nativeStacks=data.getLong("RareBleedNativeUntil")>now?data.getInt("RareBleedNative"):0,runeStacks=data.getLong("RareBleedRuneUntil")>now?data.getInt("RareBleedRune"):0;
        int wolf=data.getLong("RareBleedWolfUntil")>now?data.getInt("RareBleedWolfStacks"):0;int stacks=Math.max(wolf,org.slavicmyths.rpg.RuneBalance.bleedStacks(nativeStacks,runeStacks));int duration=(int)((wolf>0?data.getLong("RareBleedWolfUntil"):Math.max(data.getLong("RareBleedNativeUntil"),data.getLong("RareBleedRuneUntil")))-now);
        data.putUUID("RareBleedOwner",p.getUUID());target.removeEffect(ModEffects.RARE_BLEED);target.removeEffect(ModEffects.WOLF_CUT);target.addEffect(new MobEffectInstance(wolf>0?ModEffects.WOLF_CUT:ModEffects.RARE_BLEED,duration,stacks-1,false,true,true),p);
    }
    public static MobEffectInstance current(LivingEntity target){var strong=target.getEffect(ModEffects.WOLF_CUT);return strong==null?target.getEffect(ModEffects.RARE_BLEED):strong;}
    @Override public boolean shouldApplyEffectTickThisTick(int duration,int amplifier){return true;}
    @Override public boolean applyEffectTick(LivingEntity target,int amplifier){
        if(!CreatureProperties.canBleed(target))return false;
        if(!(target.level() instanceof ServerLevel world))return true;
        var data=target.getPersistentData();long now=RareCombat.now(target);int stacks=Math.min(3,amplifier+1);String ownerKey="RareBleedOwner";
        if(data.getBoolean("RareBleedSources")){int nativeStacks=data.getLong("RareBleedNativeUntil")>now?data.getInt("RareBleedNative"):0,runeStacks=data.getLong("RareBleedRuneUntil")>now?data.getInt("RareBleedRune"):0;stacks=org.slavicmyths.rpg.RuneBalance.bleedStacks(nativeStacks,runeStacks);if(stacks==0&&data.getLong("RareBleedWolfUntil")<=now)return false;ownerKey=nativeStacks>=runeStacks?"RareBleedNativeOwner":"RareBleedRuneOwner";}
        int wolf=data.getLong("RareBleedWolfUntil")>now?Math.clamp(data.getInt("RareBleedWolfStacks"),0,3):0;stacks=Math.max(stacks,wolf);
        var actor=wolf>0&&data.hasUUID("RareBleedWolfActor")?world.getEntity(data.getUUID("RareBleedWolfActor")):null;
        if(wolf>0&&target instanceof net.minecraft.world.entity.player.Player player&&(player.isCreative()||player.isSpectator()))return false;
        var owner=data.hasUUID(ownerKey)?world.getServer().getPlayerList().getPlayer(data.getUUID(ownerKey)):null;
        if(target.getType().is(CutEffect.IMMUNE)||wolf==0&&owner!=null&&!CutEffect.permitted(owner,target))return false;
        if(wolf==0&&target instanceof net.minecraft.world.entity.player.Player && (owner==null||!world.getServer().isPvpAllowed()))return false;
        if(now>=data.getLong("RareBleedPulse")){
            data.putLong("RareBleedPulse",now+org.slavicmyths.rpg.RuneBalance.BLEED_INTERVAL);
            target.hurt(wolf>0?(actor==null?MythDamageSources.unattributed("cut",target):MythDamageSources.periodic("cut",actor)):(owner==null?MythDamageSources.unattributed("cut",target):MythDamageSources.periodic("cut",owner)),(float)((wolf>0?2:org.slavicmyths.rpg.RuneBalance.BLEED_HP)*stacks));
            if(wolf>0)world.sendParticles(new net.minecraft.core.particles.DustParticleOptions(new org.joml.Vector3f(.35F,.025F,.04F),.7F),target.getX(),target.getY()+1,target.getZ(),4,.1,.1,.1,0);
        }return true;
    }
}
