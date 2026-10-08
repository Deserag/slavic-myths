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
        if(!CutEffect.permitted(p,target))return;var old=target.getEffect(ModEffects.RARE_BLEED);var data=target.getPersistentData();long now=RareCombat.now(target);
        if(old==null){data.putInt("RareBleedNative",0);data.putInt("RareBleedRune",0);data.putLong("RareBleedNativeUntil",0);data.putLong("RareBleedRuneUntil",0);data.putLong("RareBleedPulse",now+org.slavicmyths.rpg.RuneBalance.BLEED_INTERVAL);}
        else if(!data.getBoolean("RareBleedSources")){data.putInt("RareBleedNative",Math.min(3,old.getAmplifier()+1));data.putLong("RareBleedNativeUntil",now+old.getDuration());if(data.hasUUID("RareBleedOwner"))data.putUUID("RareBleedNativeOwner",data.getUUID("RareBleedOwner"));}
        data.putBoolean("RareBleedSources",true);String source=rune?"Rune":"Native";int max=rune?org.slavicmyths.rpg.RuneBalance.RUNE_BLEED_MAX:org.slavicmyths.rpg.RuneBalance.NATIVE_BLEED_MAX;
        int before=data.getLong("RareBleed"+source+"Until")>now?data.getInt("RareBleed"+source):0;
        data.putInt("RareBleed"+source,Math.min(max,before+1));data.putLong("RareBleed"+source+"Until",now+org.slavicmyths.rpg.RuneBalance.BLEED_DURATION);data.putUUID("RareBleed"+source+"Owner",p.getUUID());
        int nativeStacks=data.getLong("RareBleedNativeUntil")>now?data.getInt("RareBleedNative"):0,runeStacks=data.getLong("RareBleedRuneUntil")>now?data.getInt("RareBleedRune"):0;
        int stacks=org.slavicmyths.rpg.RuneBalance.bleedStacks(nativeStacks,runeStacks);int duration=(int)(Math.max(data.getLong("RareBleedNativeUntil"),data.getLong("RareBleedRuneUntil"))-now);
        data.putUUID("RareBleedOwner",p.getUUID());target.removeEffect(ModEffects.RARE_BLEED);target.addEffect(new MobEffectInstance(ModEffects.RARE_BLEED,duration,stacks-1,false,true,true),p);
    }
    @Override public boolean shouldApplyEffectTickThisTick(int duration,int amplifier){return true;}
    @Override public boolean applyEffectTick(LivingEntity target,int amplifier){
        if(!(target.level() instanceof ServerLevel world))return true;
        var data=target.getPersistentData();long now=RareCombat.now(target);int stacks=Math.min(3,amplifier+1);String ownerKey="RareBleedOwner";
        if(data.getBoolean("RareBleedSources")){int nativeStacks=data.getLong("RareBleedNativeUntil")>now?data.getInt("RareBleedNative"):0,runeStacks=data.getLong("RareBleedRuneUntil")>now?data.getInt("RareBleedRune"):0;stacks=org.slavicmyths.rpg.RuneBalance.bleedStacks(nativeStacks,runeStacks);if(stacks==0)return false;ownerKey=nativeStacks>=runeStacks?"RareBleedNativeOwner":"RareBleedRuneOwner";}
        var owner=data.hasUUID(ownerKey)?world.getServer().getPlayerList().getPlayer(data.getUUID(ownerKey)):null;
        if(target.getType().is(CutEffect.IMMUNE)||owner!=null&&!CutEffect.permitted(owner,target))return false;
        if(target instanceof net.minecraft.world.entity.player.Player && (owner==null||!world.getServer().isPvpAllowed()))return false;
        if(now>=data.getLong("RareBleedPulse")){
            data.putLong("RareBleedPulse",now+org.slavicmyths.rpg.RuneBalance.BLEED_INTERVAL);
            target.hurt(owner==null?MythDamageSources.unattributed("cut",target):MythDamageSources.periodic("cut",owner),(float)(org.slavicmyths.rpg.RuneBalance.BLEED_HP*stacks));
        }return true;
    }
}
