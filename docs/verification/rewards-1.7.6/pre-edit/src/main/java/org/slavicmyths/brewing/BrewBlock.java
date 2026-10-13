package org.slavicmyths.brewing;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.*;
public final class BrewBlock extends HorizontalDirectionalBlock implements EntityBlock {
 public static final net.minecraft.world.level.block.state.properties.IntegerProperty VISUAL=net.minecraft.world.level.block.state.properties.IntegerProperty.create("visual",0,4);public final String kind;private static final MapCodec<BrewBlock> CODEC=RecordCodecBuilder.mapCodec(i->i.group(Codec.STRING.fieldOf("kind").forGetter(b->b.kind),propertiesCodec()).apply(i,BrewBlock::new));
 public BrewBlock(String k){this(k,Properties.ofFullCopy(Blocks.OAK_PLANKS).noOcclusion().strength(2));}private BrewBlock(String k,Properties p){super(p);kind=k;registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(VISUAL,0));}protected MapCodec<BrewBlock> codec(){return CODEC;}protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState>b){b.add(FACING,VISUAL);}public BlockState getStateForPlacement(BlockPlaceContext c){return defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite());}
 public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new BrewTile(p,s);}
 @SuppressWarnings("unchecked")public <T extends BlockEntity>BlockEntityTicker<T>getTicker(Level w,BlockState s,BlockEntityType<T>t){return !w.isClientSide&&kind.equals("fermentation_vat")&&t==Brewing.TYPE.get()?(l,p,st,tile)->BrewTile.tick((BrewTile)tile):null;}
 public ItemInteractionResult useItemOn(ItemStack stack,BlockState s,Level w,BlockPos pos,Player p,InteractionHand hand,BlockHitResult hit){if(!w.isClientSide&&!p.isSpectator()&&w.getBlockEntity(pos)instanceof BrewTile t){if(kind.equals("fermentation_vat"))((net.minecraft.server.level.ServerPlayer)p).openMenu(t,pos);else t.interact(p,hand);}return ItemInteractionResult.sidedSuccess(w.isClientSide);}
 public InteractionResult useWithoutItem(BlockState s,Level w,BlockPos pos,Player p,BlockHitResult hit){return useItemOn(p.getMainHandItem(),s,w,pos,p,InteractionHand.MAIN_HAND,hit).result();}
 public void onRemove(BlockState s,Level w,BlockPos p,BlockState n,boolean moving){if(!s.is(n.getBlock())&&!w.isClientSide&&w.getBlockEntity(p)instanceof BrewTile t)t.dropAll();super.onRemove(s,w,p,n,moving);}
 public VoxelShape getShape(BlockState s,BlockGetter w,BlockPos p,CollisionContext c){return kind.equals("fruit_press")?box(1,0,1,15,24,15):box(1,0,1,15,14,15);}
 public BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}public BlockState mirror(BlockState s,Mirror m){return rotate(s,m.getRotation(s.getValue(FACING)));}
}
