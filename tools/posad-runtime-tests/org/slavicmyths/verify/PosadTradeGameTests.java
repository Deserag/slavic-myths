package org.slavicmyths.verify;
import java.util.UUID;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.*;
import org.slavicmyths.posad.*;
@GameTestHolder("slavicmyths_posad") @PrefixGameTestTemplate(false)
public final class PosadTradeGameTests {
 private static final class Connection extends net.minecraft.network.Connection{
  private final io.netty.channel.embedded.EmbeddedChannel channel=new io.netty.channel.embedded.EmbeddedChannel();
  Connection(){super(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);}
  @Override public io.netty.channel.Channel channel(){return channel;}
 }
 private static final class Listener extends net.minecraft.server.network.ServerGamePacketListenerImpl{
  Listener(ServerPlayer p){super(p.getServer(),new Connection(),p,net.minecraft.server.network.CommonListenerCookie.createInitial(p.getGameProfile(),false));}
  @Override public void send(net.minecraft.network.protocol.Packet<?> p){}
  @Override public void send(net.minecraft.network.protocol.Packet<?> p,net.minecraft.network.PacketSendListener l){}
 }
 @GameTest(template="empty",timeoutTicks=200)
 public static void nativeMerchantMenuPaymentOutputAndReload(GameTestHelper t){
  var w=t.getLevel();var p=new ServerPlayer(w.getServer(),w,new com.mojang.authlib.GameProfile(UUID.randomUUID(),"PosadTraderQA"),ClientInformation.createDefault());p.connection=new Listener(p);p.setGameMode(GameType.SURVIVAL);p.setPos(t.absoluteVec(new net.minecraft.world.phys.Vec3(3,2,3)));
  for(var role:PosadPopulationPlan.Role.values())for(int tier=1;tier<=5;tier++){
   var catalog=PosadTradeCatalog.create(role,tier,new java.util.HashMap<>(),new java.util.IdentityHashMap<>(),w.registryAccess());
   t.assertTrue(!catalog.isEmpty(),"Profession has no usable trade: "+role);
   for(var row:catalog)t.assertTrue(!row.getResult().isEmpty()&&!row.getCostA().isEmpty(),"Empty live merchant offer: "+role);
  }
  var npc=org.slavicmyths.registry.ModEntities.POSAD_RESIDENT.get().create(w);
  var member=PosadPopulationPlan.roster().stream().filter(m->m.role()==PosadPopulationPlan.Role.GUARD).findFirst().orElseThrow();
  npc.configure(new PosadPopulationPlan.WorldMember(UUID.randomUUID(),member,p.getBlockX(),p.getBlockZ()),UUID.randomUUID(),p.blockPosition());npc.setPos(p.position());
  npc.interact(p,InteractionHand.MAIN_HAND);
  t.assertTrue(p.containerMenu instanceof MerchantMenu&&npc.getTradingPlayer()==p,"Native merchant interaction did not bind player/menu");
  var menu=(MerchantMenu)p.containerMenu;var offer=npc.getOffers().getFirst();var expected=offer.getResult().copy();
  menu.setSelectionHint(0);menu.slots.get(0).set(offer.getCostA().copy());menu.slots.get(1).set(offer.getCostB().copy());
  t.assertTrue(ItemStack.isSameItemSameComponents(menu.slots.get(2).getItem(),expected),"Native merchant result absent or wrong");
  menu.clicked(2,0,ClickType.PICKUP,p);
  t.assertTrue(ItemStack.isSameItemSameComponents(menu.getCarried(),expected)&&menu.getCarried().getCount()==expected.getCount(),"Native merchant did not deliver exact output");
  t.assertTrue(menu.slots.get(0).getItem().isEmpty()&&menu.slots.get(1).getItem().isEmpty()&&offer.getUses()==1,"Payment/uses not exactly once");
  var second=PosadTestPlayers.create(w,"OtherCustomerQA");second.setPos(p.position());
  npc.interact(second,InteractionHand.MAIN_HAND);
  t.assertTrue(npc.getTradingPlayer()==p&&p.containerMenu==menu&&!(second.containerMenu instanceof MerchantMenu),"Second customer stole the active merchant session");
  int xp=npc.getVillagerXp();t.assertTrue(xp==offer.getXp(),"Native result slot did not advance merchant XP");
  menu.setCarried(ItemStack.EMPTY);menu.clicked(2,0,ClickType.PICKUP,p);t.assertTrue(menu.getCarried().isEmpty()&&offer.getUses()==1,"Empty-input repeat duplicated result");
  var insufficient=offer.getCostA().copy();insufficient.shrink(1);t.assertTrue(!offer.satisfiedBy(insufficient,offer.getCostB()),"Selected offer accepts insufficient payment");
  menu.slots.get(0).set(insufficient);menu.slots.get(1).set(offer.getCostB().copy());
  var container=(MerchantContainer)menu.slots.get(0).container;
  if(!menu.slots.get(2).getItem().isEmpty())t.assertTrue(container.getActiveOffer()!=null&&container.getActiveOffer().satisfiedBy(container.getItem(0),container.getItem(1)),"Merchant offers an unpaid fallback result");
  menu.slots.get(0).set(ItemStack.EMPTY);menu.slots.get(1).set(ItemStack.EMPTY);t.assertTrue(menu.slots.get(2).getItem().isEmpty(),"Empty payment produced result");
  var data=new net.minecraft.nbt.CompoundTag();npc.saveWithoutId(data);var restored=org.slavicmyths.registry.ModEntities.POSAD_RESIDENT.get().create(w);restored.load(data);
  t.assertTrue(restored.getVillagerXp()==xp&&restored.getOffers().getFirst().getUses()==1,"Trade XP/stock lost on serialization");
  p.closeContainer();t.assertTrue(npc.getTradingPlayer()==null,"Merchant stayed locked after close");npc.discard();restored.discard();t.succeed();
 }
 @GameTest(template="empty",timeoutTicks=100,batch="merchantProgress")
 public static void paidProgressionRefreshesCachedOfferAndRestocks(GameTestHelper t){
  var w=t.getLevel();var p=PosadTestPlayers.create(w,"TradeProgressQA");p.setPos(t.absoluteVec(new net.minecraft.world.phys.Vec3(3,2,3)));
  var npc=org.slavicmyths.registry.ModEntities.POSAD_RESIDENT.get().create(w);
  var member=PosadPopulationPlan.roster().stream().filter(m->m.role()==PosadPopulationPlan.Role.BLACKSMITH).findFirst().orElseThrow();
  npc.configure(new PosadPopulationPlan.WorldMember(UUID.randomUUID(),member,p.getBlockX(),p.getBlockZ()),UUID.randomUUID(),p.blockPosition());npc.setNoAi(true);npc.setPos(p.position());
  npc.interact(p,InteractionHand.MAIN_HAND);var menu=(MerchantMenu)p.containerMenu;int initial=npc.getOffers().size();
  for(int round=0;round<4;round++){
   var offer=npc.getOffers().getFirst();var payment=offer.getCostA().copy();payment.setCount(payment.getCount()*2);
   menu.setSelectionHint(0);menu.slots.get(0).set(payment);menu.slots.get(1).set(offer.getCostB().copy());
   for(int n=0;n<2;n++){
    menu.setCarried(ItemStack.EMPTY);menu.clicked(2,0,ClickType.PICKUP,p);
    t.assertTrue(!menu.getCarried().isEmpty(),"Paid progressive trade gave no output");
    var active=((MerchantContainer)menu.slots.get(0).container).getActiveOffer();
    if(!menu.slots.get(0).getItem().isEmpty())t.assertTrue(active==npc.getOffers().getFirst(),"Tier upgrade left a stale offer active");
   }
   t.assertTrue(menu.slots.get(0).getItem().isEmpty(),"Two purchases did not consume both payments");
  }
  t.assertTrue(npc.getVillagerXp()==16&&npc.getOffers().size()>initial,"Eight real purchases did not unlock tier two");
  t.assertTrue(npc.getOffers().getFirst().getUses()==8,"Progression reset or skipped uses");
  int before=npc.getVillagerXp();npc.overrideXp(1000000);t.assertTrue(npc.getVillagerXp()==before,"Unpaid XP injection accepted");
  menu.setCarried(npc.getOffers().getFirst().getResult().copyWithCount(64));
  menu.slots.get(0).set(npc.getOffers().getFirst().getCostA().copy());menu.clicked(2,0,ClickType.PICKUP,p);
  t.assertTrue(npc.getOffers().getFirst().getUses()==8&&!menu.slots.get(0).getItem().isEmpty(),"Full result cursor consumed payment or stock");
  menu.setCarried(ItemStack.EMPTY);p.closeContainer();
  var data=new net.minecraft.nbt.CompoundTag();npc.saveWithoutId(data);
  var restored=org.slavicmyths.registry.ModEntities.POSAD_RESIDENT.get().create(w);restored.load(data);restored.setNoAi(true);
  t.assertTrue(restored.getVillagerXp()==16&&restored.getOffers().getFirst().getUses()==8,"Progressive stock/XP serialization mismatch");
  restored.tickCount=1199;w.tickNonPassenger(restored);
  t.assertTrue(restored.getOffers().getFirst().getUses()==0,"Idle merchant at home did not restock");
  var after=new net.minecraft.nbt.CompoundTag();restored.saveWithoutId(after);t.assertTrue(after.getInt("PosadTradeRestocks")==1,"Restock count not persisted");
  npc.discard();restored.discard();t.succeed();
 }

}
