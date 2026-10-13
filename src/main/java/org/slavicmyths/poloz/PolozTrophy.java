package org.slavicmyths.poloz;

import net.minecraft.core.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.shapes.*;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.*;

/** A 3 x 3 x 4 occupied trophy, with one rendering tile and one loot-bearing anchor. */
public final class PolozTrophy extends HorizontalDirectionalBlock implements EntityBlock {
 public static final IntegerProperty CELL=IntegerProperty.create("cell",0,35);
 public static final DeferredHolder<Block,Block> BLOCK=ModBlocks.BLOCKS.register("poloz_head",()->new PolozTrophy());
 public static final DeferredHolder<Item,Item> ITEM=ModItems.ITEMS.register("poloz_head",()->new BlockItem(BLOCK.get(),new Item.Properties().rarity(Rarity.EPIC)));
 public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<Tile>> TILE=ModTiles.TILES.register("poloz_head",()->BlockEntityType.Builder.of(Tile::new,BLOCK.get()).build(null));
 public PolozTrophy(){this(Properties.ofFullCopy(Blocks.DRAGON_HEAD).strength(3).noOcclusion().pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK));}
 private PolozTrophy(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(CELL,0));}
 @Override protected com.mojang.serialization.MapCodec<PolozTrophy> codec(){return simpleCodec(PolozTrophy::new);}
 @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,CELL);}
 public static BlockPos offset(Direction d,int n){return BlockPos.ZERO.relative(d,n%4).relative(d.getClockWise(),n/12).above(n/4%3);}
 public static BlockPos anchor(BlockPos p,BlockState s){return p.subtract(offset(s.getValue(FACING),s.getValue(CELL)));}
 @Override public BlockState getStateForPlacement(BlockPlaceContext c){var w=c.getLevel();var p=c.getClickedPos();var d=c.getHorizontalDirection();for(int i=0;i<36;i++){var at=p.offset(offset(d,i));if(!w.hasChunkAt(at)||!w.getWorldBorder().isWithinBounds(at)||at.getY()<w.getMinBuildHeight()||at.getY()>=w.getMaxBuildHeight()||!w.getBlockState(at).canBeReplaced(c)||c.getPlayer()!=null&&!c.getPlayer().mayUseItemAt(at,c.getClickedFace(),c.getItemInHand()))return null;}return defaultBlockState().setValue(FACING,d);}
 @Override public void setPlacedBy(Level w,BlockPos p,BlockState s,LivingEntity e,ItemStack stack){if(w.isClientSide)return;for(int i=1;i<36;i++)w.setBlock(p.offset(offset(s.getValue(FACING),i)),s.setValue(CELL,i),3);}
 @Override public RenderShape getRenderShape(BlockState s){return RenderShape.ENTITYBLOCK_ANIMATED;}
 @Override public VoxelShape getShape(BlockState s,BlockGetter w,BlockPos p,CollisionContext c){return Shapes.block();}
 @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return s.getValue(CELL)==0?new Tile(p,s):null;}
 @Override public BlockState playerWillDestroy(Level w,BlockPos p,BlockState s,net.minecraft.world.entity.player.Player player){if(!w.isClientSide&&player.isCreative()&&s.getValue(CELL)!=0)w.destroyBlock(anchor(p,s),false);return super.playerWillDestroy(w,p,s,player);}
 @Override protected void onRemove(BlockState s,Level w,BlockPos p,BlockState next,boolean moving){super.onRemove(s,w,p,next,moving);if(w.isClientSide||s.is(next.getBlock()))return;var root=anchor(p,s);var rootState=w.getBlockState(root);if(s.getValue(CELL)!=0){if(rootState.is(this)&&rootState.getValue(CELL)==0)w.destroyBlock(root,true);return;}for(int i=1;i<36;i++){var at=root.offset(offset(s.getValue(FACING),i));var cell=w.getBlockState(at);if(cell.is(this)&&cell.getValue(CELL)==i&&cell.getValue(FACING)==s.getValue(FACING))w.removeBlock(at,false);}}
 public static final class Tile extends BlockEntity {public Tile(BlockPos p,BlockState s){super(TILE.get(),p,s);}public net.minecraft.world.phys.AABB getRenderBoundingBox(){return new net.minecraft.world.phys.AABB(worldPosition).inflate(5);}}
 public static void init(){}
}
