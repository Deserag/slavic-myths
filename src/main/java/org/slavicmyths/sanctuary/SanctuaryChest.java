package org.slavicmyths.sanctuary;
import java.util.UUID;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.server.level.ServerLevel;
/** Vanilla lid/menu/seeded loot; locked access also excludes hopper and break-time extraction. */
public final class SanctuaryChest extends ChestBlockEntity {
 private UUID instance;private boolean spring;
 public SanctuaryChest(BlockPos p,BlockState s){super(SanctuaryContent.CHEST.get(),p,s);}
 public void configure(UUID id,boolean spring){instance=id;this.spring=spring;setChanged();}
 private boolean ready(){if(instance==null)return true;if(!(level instanceof ServerLevel w))return false;var d=SanctuaryRecords.get(w);return spring?d.cleansed(instance):d.restored(instance);}
 @Override public boolean canOpen(Player p){if(!ready()){p.displayClientMessage(SanctuaryContent.text("cache_sealed"),true);return false;}return super.canOpen(p);}
 @Override public ItemStack getItem(int slot){return ready()?super.getItem(slot):ItemStack.EMPTY;}
 @Override public ItemStack removeItem(int slot,int count){return ready()?super.removeItem(slot,count):ItemStack.EMPTY;}
 @Override public ItemStack removeItemNoUpdate(int slot){return ready()?super.removeItemNoUpdate(slot):ItemStack.EMPTY;}
 @Override public boolean canPlaceItem(int slot,ItemStack stack){return ready()&&super.canPlaceItem(slot,stack);}
 @Override public void unpackLootTable(Player p){if(ready())super.unpackLootTable(p);}
 @Override protected void saveAdditional(CompoundTag n,HolderLookup.Provider r){super.saveAdditional(n,r);if(instance!=null)n.putUUID("SanctuaryInstance",instance);n.putBoolean("SanctuarySpring",spring);}
 @Override protected void loadAdditional(CompoundTag n,HolderLookup.Provider r){super.loadAdditional(n,r);instance=n.hasUUID("SanctuaryInstance")?n.getUUID("SanctuaryInstance"):null;spring=n.getBoolean("SanctuarySpring");}
}
