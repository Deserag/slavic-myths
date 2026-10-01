package org.slavicmyths.armorer;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.*;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
public final class ArmorerBlock extends Block {
    public ArmorerBlock(){super(Properties.of(Material.WOOD).strength(3).sound(SoundType.WOOD).noOcclusion());}
    @Override public ActionResultType use(BlockState s,World w,BlockPos pos,PlayerEntity p,Hand hand,BlockRayTraceResult hit){if(!w.isClientSide)NetworkHooks.openGui((ServerPlayerEntity)p,new SimpleNamedContainerProvider((id,inv,player)->new ArmorerMenu(id,inv,pos),new TranslationTextComponent("block.slavicmyths.armorer_table")),pos);return ActionResultType.sidedSuccess(w.isClientSide);}
}
