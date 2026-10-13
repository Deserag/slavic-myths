package org.slavicmyths.mor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.*;
import net.minecraft.world.item.*;
import org.slavicmyths.registry.*;
/** Decorative blight spikes. Local contact damage has a one-second victim cooldown. */
public final class MorGrowth extends Block {
 public static final net.neoforged.neoforge.registries.DeferredHolder<Block,Block> BLOCK=ModBlocks.BLOCKS.register("mor_growth",()->new MorGrowth());
 static {ModItems.ITEMS.register("mor_growth",()->new BlockItem(BLOCK.get(),new Item.Properties()));}
 public MorGrowth(){super(Properties.ofFullCopy(Blocks.POINTED_DRIPSTONE).strength(1).noOcclusion());}
 @Override public VoxelShape getShape(BlockState s,net.minecraft.world.level.BlockGetter w,BlockPos p,CollisionContext c){return box(3,0,3,13,15,13);}
 @Override protected void entityInside(BlockState s,Level w,BlockPos pos,Entity e){if(w.isClientSide||!(e instanceof net.minecraft.world.entity.LivingEntity l)||org.slavicmyths.poloz.PolozRewards.full(l)||e instanceof net.minecraft.world.entity.player.Player p&&(p.isCreative()||p.isSpectator()))return;var n=e.getPersistentData();if(w.getGameTime()<n.getLong("MorGrowthReady"))return;n.putLong("MorGrowthReady",w.getGameTime()+20);e.hurt(new net.minecraft.world.damagesource.DamageSource(w.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE).getHolderOrThrow(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DAMAGE_TYPE,org.slavicmyths.quest185.Quest185Content.id("mor_block")))),1);}
 public static void init(){}
}
