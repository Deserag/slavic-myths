package org.slavicmyths.accessories;

import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import org.slavicmyths.combat.CombatTargeting;

/** Event reactions, persistent per-player cooldowns, no global ticking or entity scans. */
@EventBusSubscriber(modid="slavicmyths")
public final class AccessoryEvents {
    private static final String KEY="Accessories148";
    private static CompoundTag state(ServerPlayer p){var root=org.slavicmyths.rpg.PathData.data(p);
        if(!root.contains(KEY,10))root.put(KEY,new CompoundTag());return root.getCompound(KEY);}
    private static long now(ServerPlayer p){return p.server.overworld().getGameTime();}
    private static boolean ready(ServerPlayer p,String id){return AccessoryRules.ready(now(p),state(p).getLong(id));}
    private static void cooldown(ServerPlayer p,String id,int ticks){state(p).putLong(id,now(p)+ticks);}
    private static boolean player(ServerPlayer p){return p.isAlive()&&!p.isSpectator()&&!p.isCreative()&&!(p instanceof net.neoforged.neoforge.common.util.FakePlayer);}
    private static boolean hostile(LivingEntity target,ServerPlayer p){
        UUID owner=CombatTargeting.affiliation(target);
        boolean playerOwned=owner!=null&&(owner.equals(p.getUUID())||p.server.getPlayerList().getPlayer(owner)!=null);
        return !(target instanceof Player)&&!(target instanceof TamableAnimal pet&&pet.isTame())
                &&!playerOwned&&CombatTargeting.canAttack(p,target)
                &&(target instanceof Enemy||target instanceof Mob m&&m.getTarget()==p);
    }
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void bath(MobEffectEvent.Added event){
        if(!(event.getEntity() instanceof ServerPlayer p)||!player(p)||!Accessories.wears(p,"bath_ward")
                ||!ready(p,"bath"))return;
        var incoming=event.getEffectInstance();var type=incoming.getEffect();boolean ordinary=type.equals(MobEffects.POISON)||type.equals(MobEffects.WEAKNESS);
        var n=p.getPersistentData();String effect=net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.getKey(type.value()).toString();
        boolean protectedEffect=type.is(org.slavicmyths.rpg.classes.ClassControl.PROTECTED)||org.slavicmyths.mor.MorBlockExposure.applying(p)||n.contains(org.slavicmyths.morozko.FrostCold.KEY)
                ||n.getBoolean("Quest185Ground."+type.value().getDescriptionId())
                ||n.getCompound("Night192Effects").getAllKeys().stream().anyMatch(k->n.getCompound("Night192Effects").getCompound(k).contains(effect))
                ||event.getEffectSource() instanceof LivingEntity source&&(source.getType().is(CombatTargeting.BOSS)||CombatTargeting.sceneProtected(source))
                ||p.getLastHurtByMob()!=null&&p.tickCount-p.getLastHurtByMobTimestamp()<=1
                    &&(p.getLastHurtByMob().getType().is(CombatTargeting.BOSS)||CombatTargeting.sceneProtected(p.getLastHurtByMob()));
        int ticks=AccessoryRules.bathDuration(incoming.getDuration(),ordinary,protectedEffect);
        if(ticks==incoming.getDuration())return;
        var old=event.getOldEffectInstance();
        if(old!=null&&(old.getAmplifier()>incoming.getAmplifier()||old.getAmplifier()==incoming.getAmplifier()&&old.getDuration()>=ticks))return;
        // Added fires after applicability and before the native merge. Mutating this instance
        // preserves its hidden effects/source and never re-enters the effect event pipeline.
        incoming.duration=ticks;cooldown(p,"bath",AccessoryRules.BATH_CD);
    }
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void falling(LivingFallEvent event){
        if(event.getEntity() instanceof ServerPlayer p&&Accessories.wears(p,"podvey_belt")){
            var n=p.getPersistentData();n.putFloat("Accessory148Fall",event.getDistance());n.putLong("Accessory148FallAt",now(p));
        }
    }
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void fall(LivingDamageEvent.Pre event){
        if(!(event.getEntity() instanceof ServerPlayer p)||!player(p)||!event.getSource().is(DamageTypes.FALL)||!Accessories.wears(p,"podvey_belt"))return;
        var n=p.getPersistentData();float distance=n.getLong("Accessory148FallAt")==now(p)?n.getFloat("Accessory148Fall"):p.fallDistance;
        n.remove("Accessory148Fall");n.remove("Accessory148FallAt");
        float damage=AccessoryRules.fall(event.getNewDamage(),distance,ready(p,"podvey"));
        if(damage<event.getNewDamage()){event.setNewDamage(damage);n.putLong("Accessory148FallProc",now(p));}
    }
    @SubscribeEvent
    public static void damage(LivingDamageEvent.Post event){
        if(event.getEntity() instanceof ServerPlayer fallen&&event.getSource().is(DamageTypes.FALL)){
            var n=fallen.getPersistentData();if(n.contains("Accessory148FallProc")&&n.getLong("Accessory148FallProc")==now(fallen)&&event.getNewDamage()>0)cooldown(fallen,"podvey",AccessoryRules.PODVEY_CD);n.remove("Accessory148FallProc");
        }
        if(event.getNewDamage()<=0)return;
        if(event.getEntity() instanceof ServerPlayer p&&player(p)&&Accessories.wears(p,"leshy_necklace")&&ready(p,"leshy")
                &&event.getSource().getEntity() instanceof LivingEntity attacker
                &&AccessoryRules.forest(event.getNewDamage(),p.level().getBiome(p.blockPosition()).is(BiomeTags.IS_FOREST)
                    ||p.level().getBiome(p.blockPosition()).is(net.neoforged.neoforge.common.Tags.Biomes.IS_FOREST)
                    ||p.level().getBiome(p.blockPosition()).is(BiomeTags.IS_TAIGA),hostile(attacker,p))){
            p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,80,0));cooldown(p,"leshy",AccessoryRules.LESHY_CD);
        }
        if(event.getSource().getEntity() instanceof ServerPlayer p&&player(p)&&Accessories.wears(p,"spark_ring")&&ready(p,"spark")
                &&AccessoryRules.spark(event.getSource().getDirectEntity()==p&&event.getSource().is(DamageTypes.PLAYER_ATTACK),
                    hostile(event.getEntity(),p),event.getEntity().fireImmune(),p.getRandom().nextFloat())){
            event.getEntity().igniteForSeconds(3);cooldown(p,"spark",AccessoryRules.SPARK_CD);
        }
    }
    /** Shared by actual block drops and successful manual sickle harvesting. */
    public static ItemStack harvest(ServerPlayer p,BlockState block,ItemStack tool,List<ItemStack> drops){
        if(!player(p)||!Accessories.wears(p,"harvest_ring")||!ready(p,"harvest")||!(block.getBlock() instanceof CropBlock crop)||!crop.isMaxAge(block))return ItemStack.EMPTY;
        boolean rune=org.slavicmyths.rpg.Runes.has(tool,"farmer_iii")||org.slavicmyths.rpg.Runes.has(tool,"farmer_iv");
        // A strict produce whitelist prevents edible seeds/rare loot or modded loot copies.
        var produce=drops.stream().filter(s->!s.isEmpty()&&isProduce(s)).findFirst();
        if(produce.isEmpty()||!AccessoryRules.harvest(true,true,true,rune,p.getRandom().nextFloat()))return ItemStack.EMPTY;
        cooldown(p,"harvest",AccessoryRules.HARVEST_CD);return new ItemStack(produce.get().getItem(),1);
    }
    private static boolean isProduce(ItemStack s){
        if(s.is(Items.WHEAT)||s.is(Items.CARROT)||s.is(Items.POTATO)||s.is(Items.BEETROOT))return true;
        var id=net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(s.getItem());
        return id.getNamespace().equals("slavicmyths")&&Set.of("rye_grain","barley_grain","oat_grain","turnip","cabbage","pea_pod").contains(id.getPath());
    }
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void drops(BlockDropsEvent event){
        if(!(event.getBreaker() instanceof ServerPlayer p)||p.distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(event.getPos()))>64)return;
        var extra=harvest(p,event.getState(),event.getTool(),event.getDrops().stream().map(ItemEntity::getItem).toList());
        if(!extra.isEmpty())event.getDrops().add(new ItemEntity(event.getLevel(),event.getPos().getX()+.5,event.getPos().getY()+.5,event.getPos().getZ()+.5,extra));
    }
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void clone(PlayerEvent.Clone event){
        if(event.getOriginal() instanceof ServerPlayer old&&event.getEntity() instanceof ServerPlayer fresh){
            var n=org.slavicmyths.rpg.PathData.data(old);if(n.contains(KEY,10))org.slavicmyths.rpg.PathData.data(fresh).put(KEY,n.getCompound(KEY).copy());
        }
    }
    private AccessoryEvents(){}
}
