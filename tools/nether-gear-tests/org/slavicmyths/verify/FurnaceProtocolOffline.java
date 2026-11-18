package org.slavicmyths.verify;
import net.minecraft.SharedConstants;import net.minecraft.server.Bootstrap;import net.minecraft.core.*;import net.minecraft.core.registries.BuiltInRegistries;import net.minecraft.nbt.*;import net.minecraft.world.item.*;import net.minecraft.world.level.block.*;import net.minecraft.world.level.block.entity.*;import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;
/** Native inherited container protocol only. Does not tick a world or instantiate the mod station. */
public final class FurnaceProtocolOffline {
 public static void main(String[] args){net.neoforged.fml.loading.LoadingModList.of(java.util.List.of(),java.util.List.of(),java.util.List.of(),java.util.List.of(),java.util.Map.of());SharedConstants.tryDetectVersion();Bootstrap.bootStrap();int checks=0;
  var lookup=net.minecraft.core.RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
  for(int count:new int[]{1,64}){
   var original=new FurnaceBlockEntity(BlockPos.ZERO,Blocks.FURNACE.defaultBlockState());var seed=new CompoundTag();net.minecraft.world.ContainerHelper.saveAllItems(seed,net.minecraft.core.NonNullList.of(ItemStack.EMPTY,new ItemStack(Items.RAW_IRON,count),new ItemStack(Items.COAL,3),ItemStack.EMPTY),lookup);original.loadWithComponents(seed,lookup);
   CompoundTag data=original.saveWithFullMetadata(lookup);data.putShort("BurnTime",(short)1200);data.putShort("CookTime",(short)73);data.putShort("CookTimeTotal",(short)200);
   var copy=new FurnaceBlockEntity(BlockPos.ZERO,Blocks.FURNACE.defaultBlockState());copy.loadWithComponents(data,lookup);var saved=copy.saveWithFullMetadata(lookup);
   if(copy.getItem(0).getCount()!=count||saved.getShort("BurnTime")!=1200||saved.getShort("CookTime")!=73)throw new AssertionError("container reload");checks++;
   var top=new SidedInvWrapper(copy,Direction.UP);var side=new SidedInvWrapper(copy,Direction.NORTH);var bottom=new SidedInvWrapper(copy,Direction.DOWN);
   if(top.getSlots()!=1||side.getSlots()!=1||bottom.getSlots()!=2)throw new AssertionError("sides");checks++;
   copy.setItem(2,new ItemStack(Items.IRON_INGOT,64));var extracted=bottom.extractItem(0,1,false);if(!extracted.is(Items.IRON_INGOT)||copy.getItem(2).getCount()!=63)throw new AssertionError("output extraction");checks++;
  }
  System.out.println("Native inherited furnace NBT and sided inventory primitives PASS "+checks+"; custom station tick/fuel/XP/recipebook/hopper/reload/MP NOT_RUN.");
 }
}

