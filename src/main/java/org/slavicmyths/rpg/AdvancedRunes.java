package org.slavicmyths.rpg;

import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.damagesource.*;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.core.particles.ParticleTypes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/** Event-local server effects. Ephemeral series use actual stack identity; cooldowns use persisted player data. */
@EventBusSubscriber(modid="slavicmyths")
public final class AdvancedRunes {
    private static final String KEY="RunesPatch";
    private static final Map<ServerPlayer,State> STATES=new WeakHashMap<>();
    private record Pending(ServerPlayer player,DamageSource source,boolean counter,boolean hunter,boolean oath){}
    private static final Map<LivingEntity,Pending> PENDING=new WeakHashMap<>();
    private static final Map<LivingEntity,DamageSource> LAST_PENDING=new WeakHashMap<>();
    private static final class State {
        ItemStack weapon=ItemStack.EMPTY,shield=ItemStack.EMPTY;
        List<String> weaponRunes=List.of(),shieldRunes=List.of();
        UUID target;int marks;long at,counterUntil;boolean counterPending,hunterPending;
    }
    public static boolean newRune(String id){return AdvancedRuneRules.IDS.contains(id);}
    public static boolean iv(String id){return id.equals("earth_lord_iv")||id.equals("veles_oath_iv");}
    private static boolean melee(ItemStack s){return Set.of("sword","dagger","spear","heavy","whip").contains(Runes.category(s));}
    public static boolean active(ItemStack s,String id){return Runes.has(s,id)&&(!iv(id)||Runes.list(s).stream().filter(AdvancedRunes::iv).findFirst().orElse("").equals(id));}
    public static boolean supports(ItemStack s,String id){return switch(id){
        case "counterstrike_iii"->s.getItem() instanceof ShieldItem;
        case "hunter_iii"->RuneRework.ranged(s);
        case "last_stand_iii"->s.getItem() instanceof ArmorItem a&&a.getType()==ArmorItem.Type.CHESTPLATE;
        case "depths_iii"->s.getItem() instanceof PickaxeItem;
        case "veles_oath_iv","perun_rage_iv"->melee(s);
        case "unbreakable_iv"->s.getItem() instanceof ShieldItem||s.getItem() instanceof ArmorItem a&&a.getType()==ArmorItem.Type.CHESTPLATE;
        case "forest_spirit_iv"->s.getItem() instanceof ArmorItem a&&(a.getType()==ArmorItem.Type.CHESTPLATE||a.getType()==ArmorItem.Type.HELMET);
        case "mastery_iv"->s.getItem() instanceof PickaxeItem;
        default->false;
    };}
    public static Component description(String id){if(CraftRuneRules.IDS.contains(id))return Component.translatable("runes_patch.effect."+id);return switch(id){
        case "counterstrike_iii"->Component.translatable("runes_patch.effect."+id,AdvancedRuneRules.COUNTER_HP,AdvancedRuneRules.COUNTER_WINDOW/20,AdvancedRuneRules.COUNTER_CD/20);
        case "hunter_iii"->Component.translatable("runes_patch.effect."+id,AdvancedRuneRules.HUNTER_RANGE,AdvancedRuneRules.HUNTER_BONUS*100,AdvancedRuneRules.HUNTER_CD/20);
        case "last_stand_iii"->Component.translatable("runes_patch.effect."+id,AdvancedRuneRules.LAST_THRESHOLD*100,AdvancedRuneRules.LAST_REDUCTION*100,AdvancedRuneRules.LAST_CD/20);
        case "depths_iii"->Component.translatable("runes_patch.effect."+id,AdvancedRuneRules.DEPTH_BONUS*100,AdvancedRuneRules.MINING_CAP*100);
        default->Component.translatable("runes_patch.effect.veles_oath_iv",AdvancedRuneRules.OATH_STEP*100,AdvancedRuneRules.OATH_STEP*500,AdvancedRuneRules.OATH_WINDOW/20);
    };}
    private static long now(ServerPlayer p){return p.server.overworld().getGameTime();}
    private static CompoundTag data(ServerPlayer p){var n=PathData.data(p);if(!n.contains(KEY,10))n.put(KEY,new CompoundTag());return n.getCompound(KEY);}
    private static boolean ready(ServerPlayer p,String key){return now(p)>=data(p).getLong(key);}
    private static void cooldown(ServerPlayer p,String key,int ticks){data(p).putLong(key,now(p)+ticks);}
    public static void reset(ServerPlayer p){org.slavicmyths.rpg.tools.ToolRuneRuntime.clear(p);STATES.remove(p);CraftRunes.reset(p);}
    private static State state(ServerPlayer p){var n=STATES.computeIfAbsent(p,x->new State());var w=p.getMainHandItem();var runes=Runes.list(w);
        if(w!=n.weapon||!runes.equals(n.weaponRunes)||now(p)-n.at>=AdvancedRuneRules.OATH_WINDOW){n.marks=0;n.target=null;}
        n.weapon=w;n.weaponRunes=List.copyOf(runes);
        if(n.shield!=ItemStack.EMPTY&&(p.getMainHandItem()!=n.shield&&p.getOffhandItem()!=n.shield||!Runes.list(n.shield).equals(n.shieldRunes))){n.counterUntil=0;n.shield=ItemStack.EMPTY;}
        if(n.counterUntil<=now(p)){n.counterUntil=0;n.shield=ItemStack.EMPTY;}
        return n;
    }
    public static void blocked(LivingEntity defender,LivingShieldBlockEvent e){if(!(defender instanceof ServerPlayer p)||!e.getBlocked()||e.getDamageContainer().getBlockedDamage()<=0||!p.isBlocking())return;
        if(!(e.getDamageSource().getEntity() instanceof LivingEntity attacker)||!CraftRunes.hostile(p,attacker))return;
        var shield=p.getUseItem();if(!supports(shield,"counterstrike_iii")||!Runes.has(shield,"counterstrike_iii")||!ready(p,"Counter"))return;
        var n=state(p);n.shield=shield;n.shieldRunes=List.copyOf(Runes.list(shield));n.counterUntil=now(p)+AdvancedRuneRules.COUNTER_WINDOW;cooldown(p,"Counter",AdvancedRuneRules.COUNTER_CD);
    }
    public static void shot(AbstractArrow arrow,net.minecraft.world.entity.player.Player p,ItemStack bow){if(!(p instanceof ServerPlayer)||!Runes.has(bow,"hunter_iii"))return;var n=arrow.getPersistentData();n.putBoolean("RunesHunter",true);n.putDouble("RunesShotX",p.getX());n.putDouble("RunesShotY",p.getY());n.putDouble("RunesShotZ",p.getZ());}
    private static boolean primary(ServerPlayer p,DamageSource s){return s.is(DamageTypes.PLAYER_ATTACK)&&s.getDirectEntity()==p&&!org.slavicmyths.poloz.PolozRewardEvents.secondary(p);}
    private static boolean combat(DamageSource s,ServerPlayer victim){if(!(s.getEntity() instanceof LivingEntity attacker)||attacker==victim||attacker.isAlliedTo(victim)||s.is(DamageTypeTags.BYPASSES_INVULNERABILITY)||s.is(DamageTypeTags.BYPASSES_ARMOR)||s.is(DamageTypeTags.BYPASSES_EFFECTS))return false;
        if(attacker instanceof net.minecraft.world.entity.player.Player&&!victim.server.isPvpAllowed())return false;
        return s.getDirectEntity()==attacker&&(s.is(DamageTypes.PLAYER_ATTACK)||s.is(DamageTypes.MOB_ATTACK))||s.is(DamageTypeTags.IS_PROJECTILE)&&s.getDirectEntity() instanceof AbstractArrow;
    }
    @SubscribeEvent public static void damage(LivingDamageEvent.Pre e){if(e.getEntity().level().isClientSide||e.getNewDamage()<=0)return;var source=e.getSource();CraftRunes.defend(e);
        if(e.getEntity() instanceof ServerPlayer victim&&AdvancedRuneRules.lastStand(victim.getHealth(),victim.getMaxHealth(),e.getNewDamage(),combat(source,victim))&&!CraftRunes.defending(victim,source)&&Runes.has(victim.getItemBySlot(EquipmentSlot.CHEST),"last_stand_iii")&&ready(victim,"Last")){
            e.setNewDamage((float)(e.getNewDamage()*(1-AdvancedRuneRules.LAST_REDUCTION)));LAST_PENDING.put(victim,source);
        }
        if(!(source.getEntity() instanceof ServerPlayer p)||!org.slavicmyths.combat.CombatTargeting.canAttack(p,e.getEntity())||p.getUUID().equals(org.slavicmyths.kalin.KalinParty.owner(e.getEntity()))||e.getEntity() instanceof net.minecraft.world.entity.player.Player&&!p.server.isPvpAllowed())return;
        if(!primary(p,source)&&!(source.is(DamageTypeTags.IS_PROJECTILE)&&source.getDirectEntity() instanceof AbstractArrow))return;
        if(primary(p,source)&&!p.hasLineOfSight(e.getEntity()))return;CraftRunes.prepare(p,e.getEntity(),source);var n=state(p);n.counterPending=false;n.hunterPending=false;
        if(primary(p,source)&&melee(p.getMainHandItem())){
            if(!e.getEntity().getUUID().equals(n.target))n.marks=0;
            var s=p.getMainHandItem();double existing=Math.min(RuneBalance.DAMAGE_CAP,RuneBalance.STRENGTH*RuneBalance.effective(RuneRework.copies(s,"strength"))+RuneBalance.CRUSHING_DAMAGE*RuneBalance.effective(RuneRework.copies(s,"crushing")));
            double oath=active(s,"veles_oath_iv")&&supports(s,"veles_oath_iv")?Math.clamp(n.marks,0,5)*AdvancedRuneRules.OATH_STEP:0;
            e.setNewDamage((float)(e.getNewDamage()*CraftRuneRules.damageFactor(existing,oath,CraftRunes.directRage(p,e.getEntity(),source))));
            if(n.counterUntil>now(p)){e.setNewDamage((float)(e.getNewDamage()+AdvancedRuneRules.COUNTER_HP));n.counterPending=true;}
        }else if(source.getDirectEntity() instanceof AbstractArrow arrow&&source.is(DamageTypeTags.IS_PROJECTILE)){
            var a=arrow.getPersistentData();if(a.getBoolean("RunesHunter")&&!a.getBoolean("RunesHunterUsed")&&ready(p,"Hunter")&&AdvancedRuneRules.hunter(e.getEntity().position().distanceToSqr(new net.minecraft.world.phys.Vec3(a.getDouble("RunesShotX"),a.getDouble("RunesShotY"),a.getDouble("RunesShotZ"))))){e.setNewDamage((float)(e.getNewDamage()*(1+AdvancedRuneRules.HUNTER_BONUS)));n.hunterPending=true;}
        }
        PENDING.put(e.getEntity(),new Pending(p,source,n.counterPending,n.hunterPending,primary(p,source)&&active(p.getMainHandItem(),"veles_oath_iv")&&supports(p.getMainHandItem(),"veles_oath_iv")));
    }
    @SubscribeEvent public static void hit(LivingDamageEvent.Post e){CraftRunes.hit(e);var last=LAST_PENDING.get(e.getEntity());if(last==e.getSource()){LAST_PENDING.remove(e.getEntity());if(e.getNewDamage()>0&&e.getEntity() instanceof ServerPlayer victim){cooldown(victim,"Last",AdvancedRuneRules.LAST_CD);fx(victim,victim,ParticleTypes.ENCHANT,6);victim.serverLevel().playSound(null,victim.blockPosition(),net.minecraft.sounds.SoundEvents.SHIELD_BLOCK,net.minecraft.sounds.SoundSource.PLAYERS,.35F,1.2F);}}var pending=PENDING.get(e.getEntity());if(pending==null||pending.source()!=e.getSource())return;PENDING.remove(e.getEntity());if(e.getNewDamage()<=0)return;var p=pending.player();var n=state(p);
        if(primary(p,e.getSource())&&melee(p.getMainHandItem())){
            if(pending.counter()){n.counterUntil=0;n.counterPending=false;fx(p,e.getEntity(),ParticleTypes.CRIT,4);}
            if(pending.oath()){
                if(!e.getEntity().getUUID().equals(n.target))n.marks=0;n.target=e.getEntity().getUUID();n.at=now(p);int previous=n.marks;n.marks=Math.min(5,n.marks+1);if(previous==4)fx(p,e.getEntity(),ParticleTypes.HAPPY_VILLAGER,3);
            }
        }else if(pending.hunter()&&e.getSource().getDirectEntity() instanceof AbstractArrow arrow){arrow.getPersistentData().putBoolean("RunesHunterUsed",true);n.hunterPending=false;cooldown(p,"Hunter",AdvancedRuneRules.HUNTER_CD);fx(p,e.getEntity(),ParticleTypes.CRIT,4);}
    }
    private static void fx(ServerPlayer p,LivingEntity target,net.minecraft.core.particles.SimpleParticleType type,int count){p.serverLevel().sendParticles(type,target.getX(),target.getY()+.8,target.getZ(),count,.15,.15,.15,.01);}
    public static double mining(PlayerEvent.BreakSpeed e,boolean ring){var p=e.getEntity();var s=p.getMainHandItem();var pos=e.getPosition();boolean depths=pos.isPresent()&&Runes.has(s,"depths_iii")&&pos.get().getY()<=p.level().getSeaLevel()-16&&!p.level().canSeeSky(pos.get().above());return (ring?1.12:1)*CraftRuneRules.mining(Runes.has(s,"mining_1"),Runes.has(s,"mining_2"),depths,Runes.has(s,"mastery_iv"));}
    public static boolean storyClaimed(ServerPlayer p){var personal=PathData.data(p).getCompound("Gorynych196");var ledger=org.slavicmyths.gorynych.GorynychLedger.get(p.serverLevel());
        for(String key:personal.getAllKeys()){UUID id;try{id=UUID.fromString(key);}catch(IllegalArgumentException ex){continue;}if(!personal.getCompound(key).getBoolean("Claimed")||personal.getCompound(key).getBoolean("CreativeClaim"))continue;
            var row=ledger.row(id);if(!row.getBoolean("Standalone")&&row.getInt("State")==org.slavicmyths.gorynych.GorynychRules.PURIFIED&&row.getCompound("Participants").getBoolean(p.getUUID().toString()))return true;
        }return false;
    }
    public static boolean boss(LivingEntity t){return t instanceof org.slavicmyths.hunt.HuntMob||t instanceof org.slavicmyths.hunt.ElementHuntMob||t instanceof org.slavicmyths.bandit.BanditEntity bandit&&bandit.role==org.slavicmyths.bandit.BanditEntity.ATAMAN||t instanceof org.slavicmyths.morozko.Morozko||t instanceof org.slavicmyths.karachun.Karachun||t instanceof org.slavicmyths.gorynych.Gorynych||t instanceof org.slavicmyths.poloz.GreatPoloz||t instanceof org.slavicmyths.earth.EarthSpirit||t instanceof org.slavicmyths.guardians.GuardianEntity||t instanceof org.slavicmyths.hunt.WorldBoss||t instanceof org.slavicmyths.bogatyr.IdolishcheEntity||t instanceof org.slavicmyths.marinka.MarinkaEntity||t instanceof org.slavicmyths.skim.SkimBeastEntity||t instanceof org.slavicmyths.finist.KartausEntity||t instanceof org.slavicmyths.kalin.KalinEntity||t instanceof org.slavicmyths.firebird.FirebirdEntity||t instanceof org.slavicmyths.volkolak.CursedVolkolak||t instanceof org.slavicmyths.underground.RoomBoss;}
    @SubscribeEvent(priority=net.neoforged.bus.api.EventPriority.HIGHEST) public static void equip(LivingEquipmentChangeEvent e){if(e.getEntity() instanceof ServerPlayer player)CraftRunes.validate(player);if(e.getEntity() instanceof ServerPlayer p&&STATES.containsKey(p))state(p);}
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e){if(e.getEntity() instanceof ServerPlayer p)reset(p);}
    @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent e){if(e.getEntity() instanceof ServerPlayer p)reset(p);}
    @SubscribeEvent public static void dimension(PlayerEvent.PlayerChangedDimensionEvent e){if(e.getEntity() instanceof ServerPlayer p)reset(p);}
    @SubscribeEvent public static void respawn(PlayerEvent.PlayerRespawnEvent e){if(e.getEntity() instanceof ServerPlayer p)reset(p);}
    @SubscribeEvent public static void death(LivingDeathEvent e){if(e.getEntity() instanceof ServerPlayer p)reset(p);}
    private AdvancedRunes(){}
}
