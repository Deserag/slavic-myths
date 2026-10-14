package org.slavicmyths.karachun;
import java.util.*;
import net.minecraft.nbt.*;
import net.minecraft.world.entity.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.attributes.*;
/** Small per-entity source ledger. Never touches offhand inventory or death/totem checks. */
public final class NightStatus {
 public static final String KEY="Night192Status";
 private static final net.minecraft.resources.ResourceLocation SLOW=NightContent.id("waning_slow"),WEAK=NightContent.id("waning_weak");
 public static void add(LivingEntity e,UUID source,double waning,double fear,boolean night){if(e.level().isClientSide||org.slavicmyths.combat.CreatureSafety.protectedPlayer(e))return;var n=e.getPersistentData().getCompound(KEY);var k=source.toString();var row=n.getCompound(k);long now=e.level().getGameTime();row.putDouble("Waning",Math.clamp(row.getDouble("Waning")+waning,0,100));row.putDouble("Fear",Math.clamp(row.getDouble("Fear")+fear,0,100));row.putLong("Touched",now);if(night)row.putLong("Night",now+8);hysteresis(row,now);n.put(k,row);e.getPersistentData().put(KEY,n);}
 public static double value(LivingEntity e,String key){double max=0;var n=e.getPersistentData().getCompound(KEY);for(String k:n.getAllKeys())max=Math.max(max,n.getCompound(k).getDouble(key));return max;}
 private static void hysteresis(CompoundTag row,long now){double fear=row.getDouble("Fear");int tier=row.getInt("FearTier"),next=NightRules.tier(fear);if(next>tier){tier=next;row.remove("FearFallAt");}else if(next<tier&&fear<tier*25-8){if(!row.contains("FearFallAt"))row.putLong("FearFallAt",now);if(now-row.getLong("FearFallAt")>=40){tier=next;row.remove("FearFallAt");}}else row.remove("FearFallAt");row.putInt("FearTier",tier);}
 public static double visualFear(LivingEntity e){double result=0;var n=e.getPersistentData().getCompound(KEY);for(String k:n.getAllKeys()){var row=n.getCompound(k);result=Math.max(result,Math.max(row.getDouble("Fear"),row.getInt("FearTier")*25));}return result;}
 public static boolean night(LivingEntity e){long now=e.level().getGameTime();var n=e.getPersistentData().getCompound(KEY);return n.getAllKeys().stream().anyMatch(k->n.getCompound(k).getLong("Night")>now);}
 public static int tier(LivingEntity e){int t=NightRules.tier(value(e,"Waning"));return e instanceof org.slavicmyths.morozko.Morozko||e instanceof Karachun?Math.min(2,t):t;}
 public static boolean offhand(LivingEntity e){return tier(e)>=3;}
 public static void reduceFear(LivingEntity e,UUID source){var n=e.getPersistentData().getCompound(KEY).getCompound(source.toString());long now=e.level().getGameTime();if(now>=n.getLong("HitReady")){n.putDouble("Fear",Math.max(0,n.getDouble("Fear")-6));n.putLong("HitReady",now+40);}}
 public static void clear(LivingEntity e,UUID source){var n=e.getPersistentData().getCompound(KEY);n.remove(source.toString());apply(e);if(n.isEmpty())e.getPersistentData().remove(KEY);}
 public static void clearAll(LivingEntity e){e.getPersistentData().remove(KEY);apply(e);}
 public static void tick(LivingEntity e){if(e.level().isClientSide||e.tickCount%4!=0||!e.getPersistentData().contains(KEY))return;var n=e.getPersistentData().getCompound(KEY);long now=e.level().getGameTime();for(String k:new ArrayList<>(n.getAllKeys())){UUID source;try{source=UUID.fromString(k);}catch(IllegalArgumentException bad){n.remove(k);continue;}if(NightLedger.get((ServerLevel)e.level()).closed(source)){NightEffects.clear(e,source);n.remove(k);continue;}var row=n.getCompound(k);if(now-row.getLong("Touched")>40){row.putDouble("Waning",Math.max(0,row.getDouble("Waning")-NightConfig.value("decay")/5));row.putDouble("Fear",Math.max(0,row.getDouble("Fear")-NightConfig.value("fearDecay")/5));}hysteresis(row,now);if(row.getDouble("Waning")<=0&&row.getDouble("Fear")<=0&&row.getLong("Night")<=now)n.remove(k);}int previous=e.getPersistentData().getInt("Night192Tier");apply(e);if(tier(e)>previous)NightContent.sound(e,"waning_stage_up");if(n.isEmpty())e.getPersistentData().remove(KEY);}
 private static void apply(LivingEntity e){int t=tier(e);e.getPersistentData().putInt("Night192Tier",t);modifier(e,Attributes.MOVEMENT_SPEED,SLOW,t>0?-.15:0,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);modifier(e,Attributes.ATTACK_DAMAGE,WEAK,!(e instanceof net.minecraft.world.entity.player.Player)&&t>=2?(t==3?-.35:-.2):0,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);if(offhand(e)&&e.isUsingItem()&&e.getUsedItemHand()==net.minecraft.world.InteractionHand.OFF_HAND)e.stopUsingItem();}
 private static void modifier(LivingEntity e,net.minecraft.core.Holder<Attribute> type,net.minecraft.resources.ResourceLocation id,double value,AttributeModifier.Operation op){var a=e.getAttribute(type);if(a==null)return;var old=a.getModifier(id);if(old!=null&&old.amount()==value)return;a.removeModifier(id);if(value!=0)a.addTransientModifier(new AttributeModifier(id,value,op));}
 private NightStatus(){}
}
