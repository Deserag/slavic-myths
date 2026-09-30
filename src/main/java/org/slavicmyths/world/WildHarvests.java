package org.slavicmyths.world;
import net.minecraft.block.*;
import net.minecraft.enchantment.*;
import net.minecraft.item.*;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slavicmyths.registry.ModItems;
/** Event-only temporary wild harvest until dedicated berry bushes receive a complete implementation. */
@Mod.EventBusSubscriber(modid="slavicmyths")
public final class WildHarvests {
 @SubscribeEvent public static void leaves(BlockEvent.BreakEvent e){
  if(!(e.getWorld() instanceof World)||e.getWorld().isClientSide()||e.getPlayer().isCreative())return;
  ItemStack tool=e.getPlayer().getMainHandItem();
  if(tool.getItem() instanceof ShearsItem||EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH,tool)>0)return;
  Block block=e.getState().getBlock();if(!(block instanceof LeavesBlock))return;
  if(e.getPlayer().getRandom().nextInt(45)!=0)return;
  Item berry=block==Blocks.SPRUCE_LEAVES?ModItems.BLUEBERRY.get():ModItems.RASPBERRY.get();
  Block.popResource((World)e.getWorld(),e.getPos(),new ItemStack(berry));
 }
 private WildHarvests(){}
}
