package org.slavicmyths.kurgan;
import java.util.*;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.*;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.*;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.*;
import net.minecraft.world.server.ServerWorld;
import org.slavicmyths.registry.*;
public final class BurialCoffinTile extends LockableLootTileEntity implements ITickableTileEntity {
 private NonNullList<ItemStack> items=NonNullList.withSize(5,ItemStack.EMPTY);
 public UUID barrow; public int remains; public boolean opened;
 private final Set<UUID> viewers=new HashSet<>();public float lid,oldLid;private boolean opening;
 public BurialCoffinTile(){super(ModTiles.BURIAL_COFFIN.get());}
 public void makeDomestic(){barrow=null;remains=0;opened=false;setChanged();sync();}
 public void natural(UUID id,int variant){barrow=id;remains=Math.max(0,Math.min(3,variant));setChanged();sync();}
 @Override public int getContainerSize(){return 5;}
 @Override protected NonNullList<ItemStack> getItems(){return items;}
 @Override protected void setItems(NonNullList<ItemStack> value){items=value;}
 @Override protected ITextComponent getDefaultName(){return new TranslationTextComponent("block.slavicmyths.burial_log_coffin");}
 @Override protected Container createMenu(int id,PlayerInventory inv){return new BurialCoffinMenu(id,inv,this);}
 @Override public void startOpen(PlayerEntity p){if(level.isClientSide||p.isSpectator())return;viewers.add(p.getUUID());if(barrow!=null)KurganDisturbance.burial(p,barrow,worldPosition);if(barrow!=null&&!opened){opened=true;BurialRecords.get((ServerWorld)level).opened(barrow);setChanged();}change(true);}
 @Override public void stopOpen(PlayerEntity p){if(level.isClientSide)return;viewers.remove(p.getUUID());if(viewers.isEmpty())change(false);}
 private void change(boolean open){if(opening==open)return;opening=open;level.playSound(null,worldPosition,open?ModSounds.COFFIN_OPEN.get():ModSounds.COFFIN_CLOSE.get(),SoundCategory.BLOCKS,.65F,.85F);sync();}
 private void sync(){if(level!=null&&!level.isClientSide)level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);}
 @Override public void tick(){oldLid=lid;lid=Math.max(0,Math.min(1,lid+(opening?.075F:-.075F)));if(!level.isClientSide&&opening&&level.getGameTime()%20==0){viewers.removeIf(id->{PlayerEntity p=level.getPlayerByUUID(id);return p==null||!(p.containerMenu instanceof BurialCoffinMenu)||((BurialCoffinMenu)p.containerMenu).inventory!=this;});if(viewers.isEmpty())change(false);}}
 @Override public void load(BlockState s,CompoundNBT n){super.load(s,n);items=NonNullList.withSize(5,ItemStack.EMPTY);if(!tryLoadLootTable(n))ItemStackHelper.loadAllItems(n,items);barrow=n.hasUUID("Barrow")?n.getUUID("Barrow"):null;remains=Math.max(0,Math.min(3,n.getInt("Remains")));opened=n.getBoolean("Opened");}
 @Override public CompoundNBT save(CompoundNBT n){super.save(n);if(!trySaveLootTable(n))ItemStackHelper.saveAllItems(n,items);if(barrow!=null)n.putUUID("Barrow",barrow);n.putInt("Remains",remains);n.putBoolean("Opened",opened);return n;}
 @Override public CompoundNBT getUpdateTag(){CompoundNBT n=new CompoundNBT();n.putInt("Remains",remains);n.putBoolean("Opening",opening);return n;}
 @Override public void handleUpdateTag(BlockState s,CompoundNBT n){remains=n.getInt("Remains");opening=n.getBoolean("Opening");}
 @Override public SUpdateTileEntityPacket getUpdatePacket(){return new SUpdateTileEntityPacket(worldPosition,0,getUpdateTag());}
 @Override public void onDataPacket(NetworkManager m,SUpdateTileEntityPacket p){handleUpdateTag(getBlockState(),p.getTag());}
 @Override public AxisAlignedBB getRenderBoundingBox(){return new AxisAlignedBB(worldPosition).inflate(2);}
}
