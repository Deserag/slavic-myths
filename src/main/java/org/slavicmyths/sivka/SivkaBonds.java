package org.slavicmyths.sivka;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/** One overworld ledger across all dimensions; direct UUID lookups, never chunk loads. */
@EventBusSubscriber(modid="slavicmyths")
public final class SivkaBonds extends SavedData {
    public static final class Bond {
        public UUID owner,id=UUID.randomUUID(),entity;
        public int stage;public long play,expiry,token=1,generation=1,lastFeed=-100,lastCall=-1000,recovery;
        public boolean expired;public String dimension="minecraft:overworld";public BlockPos last=BlockPos.ZERO;
        public float health=80;
        public Bond(UUID owner){this.owner=owner;}
        public boolean valid(){return stage==7||stage>0&&!expired&&play<expiry;}
        public CompoundTag save(){var n=new CompoundTag();n.putUUID("Owner",owner);n.putUUID("Bond",id);if(entity!=null)n.putUUID("Entity",entity);n.putInt("Stage",stage);n.putLong("Play",play);n.putLong("Expiry",expiry);n.putLong("Token",token);n.putLong("Generation",generation);n.putLong("Feed",lastFeed);n.putLong("Call",lastCall);n.putLong("Recovery",recovery);n.putBoolean("Expired",expired);n.putString("Dimension",dimension);n.putLong("Last",last.asLong());n.putFloat("Health",health);return n;}
        public static Bond load(CompoundTag n){var b=new Bond(n.getUUID("Owner"));b.id=n.getUUID("Bond");b.entity=n.hasUUID("Entity")?n.getUUID("Entity"):null;b.stage=Math.clamp(n.getInt("Stage"),0,7);b.play=Math.max(0,n.getLong("Play"));b.expiry=n.getLong("Expiry");b.token=n.getLong("Token");b.generation=n.getLong("Generation");b.lastFeed=n.getLong("Feed");b.lastCall=n.getLong("Call");b.recovery=n.getLong("Recovery");b.expired=n.getBoolean("Expired");b.dimension=n.getString("Dimension");b.last=BlockPos.of(n.getLong("Last"));b.health=Math.clamp(n.getFloat("Health"),1,80);return b;}
    }
    public final Map<UUID,Bond> bonds=new HashMap<>();
    public static SivkaBonds get(ServerLevel w){return w.getServer().overworld().getDataStorage().computeIfAbsent(new SavedData.Factory<>(SivkaBonds::new,SivkaBonds::load),"slavicmyths_sivka_bonds");}
    public static SivkaBonds load(CompoundTag n,HolderLookup.Provider provider){var d=new SivkaBonds();for(var t:n.getList("Bonds",Tag.TAG_COMPOUND)){var b=Bond.load((CompoundTag)t);d.bonds.put(b.owner,b);}return d;}
    @Override public CompoundTag save(CompoundTag n,HolderLookup.Provider provider){var l=new ListTag();bonds.values().forEach(b->l.add(b.save()));n.put("Bonds",l);n.putInt("Version",1);return n;}
    public SivkaEntity loaded(ServerLevel w,Bond b){if(b.entity==null)return null;var key=ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION,ResourceLocation.parse(b.dimension));var world=w.getServer().getLevel(key);return world!=null&&world.getEntity(b.entity) instanceof SivkaEntity h?h:null;}
    public void retire(ServerLevel w,Bond b){var h=loaded(w,b);b.generation++;b.entity=null;setDirty();if(h!=null){h.ejectPassengers();h.discard();}}
    public static CompoundTag token(ItemStack s){return s.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getCompound("Sivka");}
    public static void stamp(ItemStack s,Bond b){var n=new CompoundTag();n.putUUID("Owner",b.owner);n.putUUID("Bond",b.id);n.putLong("Token",b.token);n.putInt("Stage",b.stage);n.putLong("Remaining",b.stage==7?0:Math.max(0,b.expiry-b.play));var all=s.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();all.put("Sivka",n);s.set(DataComponents.CUSTOM_DATA,CustomData.of(all));}
    public static boolean matches(ItemStack s,Bond b,UUID owner){var n=token(s);return n.hasUUID("Owner")&&n.hasUUID("Bond")&&n.getUUID("Owner").equals(owner)&&n.getUUID("Bond").equals(b.id)&&n.getLong("Token")==b.token;}
    public static void give(ServerPlayer p,Bond b){var s=new ItemStack(SivkaContent.WHISTLE.get());stamp(s,b);if(!p.getInventory().add(s)){var drop=p.drop(s,false);if(drop!=null)drop.setTarget(p.getUUID());}}
    public static void message(Player p,String key,Object...args){p.displayClientMessage(Component.translatable("sivka.slavicmyths."+key,args),true);}
    public boolean feed(ServerPlayer p,SivkaEntity h,ItemStack held){
        if(p.isSpectator()||!p.isAlive()||p.distanceToSqr(h)>36||h.scared>0)return false;
        var b=bonds.get(p.getUUID());
        if(h.owner!=null&&!h.owner.equals(p.getUUID())){message(p,"other_owner");return false;}
        if(b!=null&&b.stage==7){return false;}
        boolean renew=b!=null&&b.expired;
        if(b!=null&&!renew&&b.entity!=null&&!b.entity.equals(h.getUUID())){message(p,"existing_bond");return false;}
        if(b!=null&&b.play-b.lastFeed<SivkaRules.FEED_DELAY)return false;
        int stage=b==null?0:b.stage;String wanted=renew?"minecraft:apple":SivkaRules.FOODS[Math.min(stage,6)];
        if(!net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(held.getItem()).toString().equals(wanted)){
            if(h.tickCount-h.lastHint>=30){message(p,"wrong_food",Component.translatable("item."+wanted.replace(':','.')));h.lastHint=h.tickCount;h.react(2,18);h.cue("snort_soft",.45F);}return false;
        }
        if(b==null){b=new Bond(p.getUUID());bonds.put(p.getUUID(),b);}
        if(renew){var old=loaded(p.serverLevel(),b);b.generation++;b.entity=null;if(old!=null&&old!=h){old.ejectPassengers();old.discard();}b.token++;}else b.stage++;
        b.expired=false;b.expiry=b.stage==7?0:b.play+SivkaRules.TEMP_TICKS;b.lastFeed=b.play;b.entity=h.getUUID();b.dimension=p.level().dimension().location().toString();b.last=h.blockPosition();
        h.owner=b.owner;h.bond=b.id;h.generation=b.generation;h.renewedInteraction();h.react(1,30);
        if(!p.getAbilities().instabuild){boolean honey=held.is(Items.HONEY_BOTTLE);held.shrink(1);if(honey&&!p.getInventory().add(new ItemStack(Items.GLASS_BOTTLE)))p.drop(new ItemStack(Items.GLASS_BOTTLE),false);}
        if(b.stage==1||renew)give(p,b);
        h.cue("feed_chew",.4F);h.cue(b.stage==7?"bond_complete":"bond_progress",.55F);if(b.stage==3||b.stage==6)h.cue("whinny_soft",.6F);h.spark(b.stage==7?20:10);
        message(p,b.stage==7?"complete":"accepted",b.stage);setDirty();syncInventory(p,b);return true;
    }
    private static void syncInventory(ServerPlayer p,Bond b){for(var s:p.getInventory().items)if(s.is(SivkaContent.WHISTLE.get())&&matches(s,b,p.getUUID()))stamp(s,b);for(var s:p.getInventory().offhand)if(s.is(SivkaContent.WHISTLE.get())&&matches(s,b,p.getUUID()))stamp(s,b);}
    @SubscribeEvent public static void tick(PlayerTickEvent.Post e){if(!(e.getEntity() instanceof ServerPlayer p))return;var d=get(p.serverLevel());var b=d.bonds.get(p.getUUID());if(b==null)return;
        b.play++;d.setDirty(); // Only an online bonded player's active clock; no world scan.
        if(b.stage<7&&!b.expired&&b.play>=b.expiry){b.expired=true;b.token++;d.retire(p.serverLevel(),b);message(p,"expired");p.playNotifySound(SivkaContent.sound("bond_expired"),net.minecraft.sounds.SoundSource.NEUTRAL,.35F,1);}
        if(p.tickCount%20==0){for(var s:p.getInventory().items)reconcile(s,p,b);for(var s:p.getInventory().offhand)reconcile(s,p,b);}
    }
    private static void reconcile(ItemStack s,ServerPlayer p,Bond b){if(!s.is(SivkaContent.WHISTLE.get()))return;var n=token(s);if(n.hasUUID("Owner")&&n.getUUID("Owner").equals(p.getUUID())){if(!matches(s,b,p.getUUID())||!b.valid())s.setCount(0);else stamp(s,b);}}
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e){if(e.getEntity() instanceof ServerPlayer p){var d=get(p.serverLevel());var b=d.bonds.get(p.getUUID());if(b!=null){var h=d.loaded(p.serverLevel(),b);if(h!=null){h.renewedInteraction();b.health=h.getHealth();b.last=h.blockPosition();}d.setDirty();}}}
}
