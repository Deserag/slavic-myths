package org.slavicmyths.block;
import net.minecraft.block.*;
import net.minecraft.entity.player.*;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.shapes.*;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.*;
import net.minecraftforge.fml.network.NetworkHooks;
import org.slavicmyths.kitchen.KitchenMenu;
public final class KitchenTableBlock extends Block {
 public KitchenTableBlock(){super(Properties.of(net.minecraft.block.material.Material.WOOD).strength(2.5F).sound(SoundType.WOOD).noOcclusion());}
 @Override public VoxelShape getShape(BlockState s,IBlockReader w,BlockPos p,ISelectionContext c){return VoxelShapes.or(box(0,11,0,16,14,16),box(1,0,1,4,11,4),box(12,0,1,15,11,4),box(1,0,12,4,11,15),box(12,0,12,15,11,15),box(2,3,2,14,5,14));}
 @Override public ActionResultType use(BlockState s,World w,BlockPos pos,PlayerEntity p,Hand h,BlockRayTraceResult hit){if(!w.isClientSide)NetworkHooks.openGui((ServerPlayerEntity)p,new SimpleNamedContainerProvider((id,inv,player)->new KitchenMenu(id,inv,pos),new TranslationTextComponent("block.slavicmyths.kitchen_table")),pos);return ActionResultType.sidedSuccess(w.isClientSide);}
}
