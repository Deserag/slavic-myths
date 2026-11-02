package org.slavicmyths.losthalls;
import java.util.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.slavicmyths.trophy.TrophyHeads;
/** Bounded event-driven delivery to the durably pinned recipient, never drops into lava. */
@EventBusSubscriber(modid="slavicmyths")
public final class BossRewardInbox {
 private static final Map<MinecraftServer,BossRewardAuthority> AUTHORITIES=new WeakHashMap<>();
 private static final String RECEIPTS="slavicmyths_boss_reward_receipts",ACTIVE="slavicmyths_boss_inbox_active";
 private static BossRewardAuthority authority(MinecraftServer server){if(!server.isSameThread())throw new IllegalStateException("Boss rewards require the server thread");return AUTHORITIES.computeIfAbsent(server,s->new BossRewardAuthority(s.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT).resolve("data/slavicmyths_boss_reward_authority.json")));}
 public static BossRewardAuthority.Entry prepare(MinecraftServer server,String key,String kind,UUID recipient,int cores,long readyAt){try{var entry=authority(server).pin(new BossRewardAuthority.Entry(key,kind,recipient,cores,readyAt));var player=server.getPlayerList().getPlayer(entry.recipient());if(player!=null)player.getPersistentData().putBoolean(ACTIVE,true);return entry;}catch(java.io.IOException|IllegalArgumentException failure){System.getLogger("slavicmyths.rewards").log(System.Logger.Level.ERROR,"Boss reward preserved/blocked: "+failure.getMessage());return null;}}
 private static ItemStack head(TrophyHeads.Kind kind){return new ItemStack(TrophyHeads.ITEMS.get(kind).get());}
 private static List<ItemStack> batch(BossRewardAuthority.Entry e){return switch(e.kind()){
  case "guardian_head"->List.of(head(TrophyHeads.Kind.GROVE_GUARDIAN));case "zharenitsa_head"->List.of(head(TrophyHeads.Kind.ZHARENITSA));
  case "posten"->List.of(head(TrophyHeads.Kind.POSTEN),new ItemStack(HallsContent.item("posten_heart")),new ItemStack(ShadowFace.RUNE.get()),new ItemStack(org.slavicmyths.rpg.RuneFoundation.item("soul")));
  case "morova_21x","trizna_21x"->org.slavicmyths.netherboss.NetherBossRewards.batch(e.kind().equals("morova_21x"),e.cores());
  case "magma"->org.slavicmyths.burningislands.MagmaRewards.batch(e.cores());default->throw new IllegalArgumentException("Unknown reward kind");};}
 public static boolean hasReceipt(CompoundTag data,String key){return data.getCompound(RECEIPTS).getBoolean(key);}
 public static void receipt(CompoundTag data,String key){var tags=data.getCompound(RECEIPTS);tags.putBoolean(key,true);data.put(RECEIPTS,tags);}
 public static void reconcile(ServerPlayer p){var a=authority(p.getServer());if(a.isReadOnly())return;boolean pending=false,changed=false;int budget=16;long now=p.getServer().overworld().getGameTime();
  for(var entry:a.forRecipient(p.getUUID())){if(hasReceipt(p.getPersistentData(),entry.key()))continue;
   // Legacy receipt means that its whole batch is either already delivered OR in the
   // old aggregate inbox, in the same player file. Let that inbox finish it once.
   if(entry.kind().equals("magma")&&p.getPersistentData().getCompound("slavicmyths_magma_receipts").getBoolean(entry.key())){receipt(p.getPersistentData(),entry.key());changed=true;continue;}
   if(entry.readyAt()>now||budget--<=0){pending=true;continue;}
   var source=new ArrayList<ItemStack>();for(int i=0;i<36;i++)source.add(p.getInventory().getItem(i));var planned=org.slavicmyths.burningislands.MagmaRewards.inventoryPlan(source,batch(entry));if(planned==null){pending=true;continue;}
   for(int i=0;i<36;i++)p.getInventory().setItem(i,planned.get(i));receipt(p.getPersistentData(),entry.key());if(entry.kind().equals("posten"))p.giveExperiencePoints(50);changed=true;
  }
  p.getPersistentData().putBoolean(ACTIVE,pending);if(changed){p.getInventory().setChanged();p.containerMenu.broadcastChanges();((org.slavicmyths.navya.mixin.PlayerListSaveInvoker)p.getServer().getPlayerList()).slavicmyths$save(p);}
 }
 @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent e){if(e.getEntity() instanceof ServerPlayer p){p.getPersistentData().putBoolean(ACTIVE,true);reconcile(p);}}
 @SubscribeEvent public static void tick(PlayerTickEvent.Post e){if(e.getEntity() instanceof ServerPlayer p&&p.tickCount%40==0&&p.getPersistentData().getBoolean(ACTIVE))reconcile(p);}
 @SubscribeEvent public static void clone(PlayerEvent.Clone e){for(String key:List.of(RECEIPTS,ACTIVE))if(e.getOriginal().getPersistentData().contains(key))e.getEntity().getPersistentData().put(key,e.getOriginal().getPersistentData().get(key).copy());}
 private BossRewardInbox(){}
}
