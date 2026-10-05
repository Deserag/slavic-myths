package org.slavicmyths.yaga;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.minecraft.server.level.ServerPlayer;
/** Only vanilla menu-button packets. Every mutation validates NPC, range, state and inputs on server. */
public final class YagaMenu extends AbstractContainerMenu{
 public static final DeferredHolder<MenuType<?>,MenuType<YagaMenu>> TYPE=org.slavicmyths.rpg.RpgMenu.MENUS.register("yaga",()->IMenuTypeExtension.create((id,inv,b)->new YagaMenu(id,inv,b.readVarInt())));
 public static void register(){}
 public final int npcId;public final SimpleContainer input=new SimpleContainer(5);private final Player player;private final SimpleContainerData info=new SimpleContainerData(9);
 public YagaMenu(int id,Inventory inv,int npcId){super(TYPE.get(),id);this.npcId=npcId;player=inv.player;addDataSlots(info);
  for(int i=0;i<4;i++)addSlot(new Slot(input,i,126+i*28,108){@Override public boolean isActive(){return tab()==3;}@Override public boolean mayPlace(ItemStack s){return tab()==3;}});
  addSlot(new Slot(input,4,276,108){@Override public boolean isActive(){return tab()==3;}@Override public boolean mayPlace(ItemStack s){return false;}@Override public boolean mayPickup(Player p){return tab()==3;}});
  for(int row=0;row<3;row++)for(int col=0;col<9;col++)addSlot(new Slot(inv,9+row*9+col,135+col*18,149+row*18));for(int col=0;col<9;col++)addSlot(new Slot(inv,col,135+col*18,207));
 }
 public static void open(ServerPlayer p,BabaYaga npc,int tab){p.openMenu(new SimpleMenuProvider((id,inv,player)->{YagaMenu m=new YagaMenu(id,inv,npc.getId());m.info.set(6,tab);m.update();return m;},Component.translatable("entity.slavicmyths.baba_yaga")),b->b.writeVarInt(npc.getId()));}
 public int stage(){return info.get(0);}public int active(){return info.get(1);}public int blocked(){return info.get(2);}public int contract(){return info.get(3);}public int contractWait(){return info.get(4);}public int tab(){return info.get(6);}public int dialogue(){return info.get(7);}public int recipe(){return info.get(8);}
 public void clientTab(int tab){if(player.level().isClientSide)info.set(6,tab);}
 private BabaYaga npc(){Entity e=player.level().getEntity(npcId);return e instanceof BabaYaga?(BabaYaga)e:null;}
 private YagaData data(){return YagaData.get((ServerLevel)player.level());}private YagaData.Progress progress(){return data().progress(player.getUUID());}
 private void update(){if(player.level().isClientSide)return;YagaData.Progress p=progress();info.set(0,p.stage);info.set(1,p.active.startsWith("main:")?p.stage+1:p.active.startsWith("contract:")?10+p.contract:0);info.set(2,(int)Math.max(0,(p.blockedUntil-player.level().getGameTime()+19)/20));info.set(3,p.contract);info.set(4,(int)Math.max(0,(p.contractReady-player.level().getGameTime()+19)/20));info.set(5,p.intro);}
 @Override public void broadcastChanges(){update();super.broadcastChanges();}
 @Override public boolean stillValid(Player p){BabaYaga n=npc();if(n==null||!n.isAlive()||!p.isAlive()||p.isSpectator()||p.level().dimension()!=Level.OVERWORLD||p.distanceToSqr(n)>64)return false;return p.level().isClientSide||n.canonical()&&(n.isEggSummoned()||progress().intro>=2)&&!progress().blocked(p.level().getGameTime());}
 @Override public ItemStack quickMoveStack(Player p,int index){if(!stillValid(p)||index<0||index>=slots.size())return ItemStack.EMPTY;Slot slot=slots.get(index);if(!slot.hasItem()||!slot.mayPickup(p)||!slot.isActive())return ItemStack.EMPTY;ItemStack s=slot.getItem(),copy=s.copy();if(index<5){if(!moveItemStackTo(s,5,slots.size(),true))return ItemStack.EMPTY;}else if(tab()!=3||!moveItemStackTo(s,0,4,false))return ItemStack.EMPTY;if(s.isEmpty())slot.set(ItemStack.EMPTY);else slot.setChanged();slot.onTake(p,s);return copy;}
 @Override public void clicked(int slot,int button,ClickType type,Player p){if(!stillValid(p)||slot>=0&&slot<5&&tab()!=3)return;super.clicked(slot,button,type,p);}
 @Override public void removed(Player p){super.removed(p);if(!p.level().isClientSide){clearContainer(p,input);BabaYaga npc=npc();if(npc!=null){npc.gesture(2);npc.playSound(org.slavicmyths.registry.ModSounds.YAGA_CLOSE.get(),.25F,.7F);}}}
 @Override public boolean clickMenuButton(Player p,int action){if(!(p instanceof ServerPlayer)||!stillValid(p))return false;ServerPlayer server=(ServerPlayer)p;YagaData.Progress progress=progress();boolean result=false;
  if(action>=0&&action<=3){info.set(6,action);result=true;}
  else if(action>=10&&action<=13){if(action==10)info.set(6,1);else if(action==13){p.closeContainer();return true;}else info.set(7,action-10);result=true;}
  else if(action>=100&&action<103)result=progress.accept("main:"+(action-100),p.level().getGameTime());
  else if(action==120)result=progress.stage>=YagaServices.CONTRACTS[YagaServices.POOL[progress.contract]].stage&&progress.accept("contract:"+progress.contract,p.level().getGameTime());
  else if(action>=200&&action<203)result=completeMain(server,action-200);
  else if(action==220)result=completeContract(server);
  else if(action>=400&&action<400+YagaServices.EXCHANGES.length){YagaServices.Entry e=YagaServices.EXCHANGES[action-400];if(progress.stage>=e.stage&&YagaServices.has(p.getInventory(),e.inputs)){YagaServices.consume(p.getInventory(),e.inputs);YagaServices.give(server,e.output());result=true;}else YagaServices.fail(server,progress.stage<e.stage?"locked":"ingredients");}
  else if(action>=500&&action<500+YagaServices.BREWS.length){info.set(8,action-500);result=true;}
  else if(action==600)result=brew(server);
  if(result){if(action==120)org.slavicmyths.navigation.NavigationManager.contractAccepted(server);data().setDirty();BabaYaga n=npc();if(n!=null){n.gesture(action==600?4:1);n.playSound(action>=200?net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP:org.slavicmyths.registry.ModSounds.YAGA_TALK.get(),.45F,.7F);}}
  else if(action>=100&&action<200)YagaServices.fail(server,"quest");update();broadcastChanges();return result;
 }
 private boolean completeMain(ServerPlayer p,int id){YagaData.Progress progress=progress();if(progress.stage!=id||!progress.active.equals("main:"+id))return YagaServices.fail(p,"quest");YagaServices.Entry e=YagaServices.MAIN[id];if(id==2&&(!YagaServices.killed(p,"name_your_misfortune")||!YagaServices.killed(p,"steppe_champion")))return YagaServices.fail(p,"strength");if(!YagaServices.has(p.getInventory(),e.inputs))return YagaServices.fail(p,"ingredients");if(!progress.completeMain(id))return false;YagaServices.consume(p.getInventory(),e.inputs);YagaServices.give(p,e.output());if(id==0){YagaServices.award(p,"not_empty_handed");YagaServices.award(p,"guest_of_yaga");}if(id==1)YagaServices.award(p,"follow_the_thread");if(id==2)YagaServices.award(p,"trusted_by_yaga");p.displayClientMessage(Component.translatable("yaga.dialogue.completed"),false);return true;}
 private boolean completeContract(ServerPlayer p){YagaData.Progress progress=progress();YagaServices.Entry e=YagaServices.CONTRACTS[YagaServices.POOL[progress.contract]];if(progress.stage<e.stage||!YagaServices.has(p.getInventory(),e.inputs))return YagaServices.fail(p,"ingredients");String navigationQuest=progress.active;if(!progress.completeContract(progress.contract,p.level().getGameTime()))return YagaServices.fail(p,"quest");YagaServices.consume(p.getInventory(),e.inputs);YagaServices.give(p,e.output());org.slavicmyths.navigation.NavigationManager.endQuest(p,navigationQuest,true);return true;}
 private boolean brew(ServerPlayer p){if(tab()!=3)return false;YagaServices.Entry e=YagaServices.BREWS[recipe()];if(progress().stage<e.stage)return YagaServices.fail(p,"locked");BabaYaga npc=npc();if(npc==null||npc.home==null||p.level().getBlockState(npc.home.offset(-2,0,1)).getBlock()!=org.slavicmyths.registry.ModBlocks.YAGA_CAULDRON.get())return YagaServices.fail(p,"cauldron");for(int i=0;i<4;i++)if(input.getItem(i).getItem()!=e.inputs[i].item()||input.getItem(i).getCount()<e.inputs[i].count)return YagaServices.fail(p,"ingredients");ItemStack out=input.getItem(4),result=e.output();if(!out.isEmpty()&&(!ItemStack.isSameItemSameComponents(out,result)||out.getCount()+result.getCount()>out.getMaxStackSize()))return YagaServices.fail(p,"output");
  for(int i=0;i<4;i++){ItemStack used=input.getItem(i);ItemStack remainder=used.getCraftingRemainingItem();int count=e.inputs[i].count;used.shrink(count);if(!remainder.isEmpty()){remainder.setCount(remainder.getCount()*count);if(used.isEmpty())input.setItem(i,remainder);else YagaServices.give(p,remainder);}}
  if(out.isEmpty())input.setItem(4,result);else out.grow(result.getCount());input.setChanged();YagaServices.award(p,"cauldron_service");p.level().playSound(null,p.blockPosition(),org.slavicmyths.registry.ModSounds.YAGA_STIR.get(),SoundSource.PLAYERS,.7F,.7F);return true;
 }
}
