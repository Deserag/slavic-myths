package org.slavicmyths.kurgan;
import java.util.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.core.NonNullList;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import org.slavicmyths.registry.*;
public final class BurialCoffinTile extends RandomizableContainerBlockEntity {
 private NonNullList<ItemStack> items=NonNullList.withSize(5,ItemStack.EMPTY);
 public UUID barrow; public int remains; public boolean opened;
 private final Set<UUID> viewers=new HashSet<>();public float lid,oldLid;private boolean opening;
 public BurialCoffinTile(net.minecraft.core.BlockPos pos,BlockState state){super(ModTiles.BURIAL_COFFIN.get(),pos,state);}
 public void makeDomestic(){barrow=null;remains=0;opened=false;setChanged();sync();}
 public void natural(UUID id,int variant){barrow=id;remains=Math.max(0,Math.min(3,variant));setChanged();sync();}
 @Override public int getContainerSize(){return 5;}
 @Override protected NonNullList<ItemStack> getItems(){return items;}
 @Override protected void setItems(NonNullList<ItemStack> value){items=value;}
 @Override protected Component getDefaultName(){return Component.translatable("block.slavicmyths.burial_log_coffin");}
 @Override protected AbstractContainerMenu createMenu(int id,Inventory inv){return new BurialCoffinMenu(id,inv,this);}
 @Override public void startOpen(Player p){if(level.isClientSide||p.isSpectator())return;viewers.add(p.getUUID());if(barrow!=null)KurganDisturbance.burial(p,barrow,worldPosition);if(barrow!=null&&!opened){opened=true;BurialRecords.get((ServerLevel)level).opened(barrow);setChanged();}change(true);}
 @Override public void stopOpen(Player p){if(level.isClientSide)return;viewers.remove(p.getUUID());if(viewers.isEmpty())change(false);}
 private void change(boolean open){if(opening==open)return;opening=open;level.playSound(null,worldPosition,open?ModSounds.COFFIN_OPEN.get():ModSounds.COFFIN_CLOSE.get(),SoundSource.BLOCKS,.65F,.85F);sync();}
 private void sync(){if(level!=null&&!level.isClientSide)level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);}
 public void tick(){oldLid=lid;lid=Math.max(0,Math.min(1,lid+(opening?.075F:-.075F)));if(!level.isClientSide&&opening&&level.getGameTime()%20==0){viewers.removeIf(id->{Player p=level.getPlayerByUUID(id);return p==null||!(p.containerMenu instanceof BurialCoffinMenu)||((BurialCoffinMenu)p.containerMenu).inventory!=this;});if(viewers.isEmpty())change(false);}}
 @Override protected void loadAdditional(CompoundTag n,net.minecraft.core.HolderLookup.Provider registries){super.loadAdditional(n,registries);items=NonNullList.withSize(5,ItemStack.EMPTY);if(!tryLoadLootTable(n))ContainerHelper.loadAllItems(n,items,registries);barrow=n.hasUUID("Barrow")?n.getUUID("Barrow"):null;remains=Math.max(0,Math.min(3,n.getInt("Remains")));opened=n.getBoolean("Opened");}
 @Override protected void saveAdditional(CompoundTag n,net.minecraft.core.HolderLookup.Provider registries){super.saveAdditional(n,registries);if(!trySaveLootTable(n))ContainerHelper.saveAllItems(n,items,registries);if(barrow!=null)n.putUUID("Barrow",barrow);n.putInt("Remains",remains);n.putBoolean("Opened",opened);}
 @Override public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries){CompoundTag n=new CompoundTag();n.putInt("Remains",remains);n.putBoolean("Opening",opening);return n;}
 @Override public void handleUpdateTag(CompoundTag n,net.minecraft.core.HolderLookup.Provider registries){remains=n.getInt("Remains");opening=n.getBoolean("Opening");}
 @Override public void onDataPacket(Connection connection,ClientboundBlockEntityDataPacket packet,net.minecraft.core.HolderLookup.Provider registries){handleUpdateTag(packet.getTag(),registries);}
 @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
}
