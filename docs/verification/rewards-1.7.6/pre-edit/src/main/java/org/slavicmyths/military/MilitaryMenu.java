package org.slavicmyths.military;
import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.rpg.RpgMenu;
public final class MilitaryMenu extends AbstractContainerMenu {
 public static final DeferredHolder<MenuType<?>,MenuType<MilitaryMenu>> TYPE=RpgMenu.MENUS.register("druzhinnik_table",()->IMenuTypeExtension.create((id,inv,b)->new MilitaryMenu(id,inv,b.readBlockPos())));
 public final BlockPos pos;public MilitaryMenu(int id,Inventory inv,BlockPos pos){super(TYPE.get(),id);this.pos=pos;}
 public static void register(){}
 public boolean stillValid(Player p){return p.isAlive()&&p.distanceToSqr(pos.getCenter())<=64&&p.level().getBlockState(pos).is(Military.TABLE.get());}
 public ItemStack quickMoveStack(Player p,int slot){return ItemStack.EMPTY;}
}
