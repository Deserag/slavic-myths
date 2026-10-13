package org.slavicmyths.military;
import net.minecraft.core.BlockPos;
import net.minecraft.world.MenuProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
public final class MilitaryTableTile extends BlockEntity implements MenuProvider {
 public MilitaryTableTile(BlockPos p,BlockState s){super(Military.TILE.get(),p,s);}
 public Component getDisplayName(){return Component.translatable("block.slavicmyths.druzhinnik_table");}
 public AbstractContainerMenu createMenu(int id,Inventory inv,Player p){return new MilitaryMenu(id,inv,worldPosition);}
}
