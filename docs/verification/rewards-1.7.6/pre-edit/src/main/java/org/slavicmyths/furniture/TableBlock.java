package org.slavicmyths.furniture;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.slavicmyths.kitchen.TableTile;
public final class TableBlock extends FurnitureBlock {
 public enum Connection implements StringRepresentable {NONE,NORTH,SOUTH,EAST,WEST;public String getSerializedName(){return name().toLowerCase(Locale.ROOT);}public Direction direction(){return this==NONE?null:Direction.valueOf(name());}public static Connection of(Direction d){return valueOf(d.name());}}
 public static final EnumProperty<Connection> CONNECTION=EnumProperty.create("connection",Connection.class);
 private static final com.mojang.serialization.MapCodec<TableBlock> CODEC=simpleCodec(TableBlock::new);
 public TableBlock(){this(Properties.ofFullCopy(Blocks.OAK_PLANKS).strength(2).noOcclusion());}
 private TableBlock(Properties p){super("table",p);registerDefaultState(defaultBlockState().setValue(CONNECTION,Connection.NONE));}
 protected com.mojang.serialization.MapCodec<? extends TableBlock> codec(){return CODEC;}
 protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState>b){super.createBlockStateDefinition(b);b.add(CONNECTION);}
 public net.minecraft.world.phys.shapes.VoxelShape getShape(BlockState s,BlockGetter w,BlockPos p,net.minecraft.world.phys.shapes.CollisionContext c){Connection con=s.getValue(CONNECTION);if(con==Connection.NONE)return super.getShape(s,w,p,c);var shape=net.minecraft.world.phys.shapes.Shapes.box(0,.75,0,1,1,1);for(int x:new int[]{1,12})for(int z:new int[]{1,12}){if(con==Connection.WEST&&x==1||con==Connection.EAST&&x==12||con==Connection.NORTH&&z==1||con==Connection.SOUTH&&z==12)continue;shape=net.minecraft.world.phys.shapes.Shapes.or(shape,box(x,0,z,x+3,12,z+3));}return shape;}
 public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new TableTile(p,s);}
 public void onPlace(BlockState s,Level w,BlockPos p,BlockState old,boolean moving){super.onPlace(s,w,p,old,moving);if(!old.is(this))connect(w,p);}
 private List<Direction> eligible(Level w,BlockPos p){List<Direction>a=new ArrayList<>();for(Direction d:Direction.Plane.HORIZONTAL){BlockState s=w.getBlockState(p.relative(d));if(s.is(this)&&s.getValue(CONNECTION)==Connection.NONE)a.add(d);}return a;}
 private void connect(Level w,BlockPos p){if(w.isClientSide)return;BlockState s=w.getBlockState(p);if(!s.is(this))return;Connection c=s.getValue(CONNECTION);
  if(c!=Connection.NONE){BlockPos q=p.relative(c.direction());BlockState t=w.getBlockState(q);if(t.is(this)&&t.getValue(CONNECTION)==Connection.of(c.direction().getOpposite()))return;w.setBlock(p,s.setValue(CONNECTION,Connection.NONE),2);return;}
  List<Direction>a=eligible(w,p);if(a.size()!=1)return;Direction d=a.getFirst();BlockPos q=p.relative(d);
  // Both endpoints must be unambiguous; avoids recursive neighbor updates pairing an ambiguous new table.
  if(eligible(w,q).size()!=1)return;
  w.setBlock(p,s.setValue(CONNECTION,Connection.of(d)),2);w.setBlock(q,w.getBlockState(q).setValue(CONNECTION,Connection.of(d.getOpposite())),2);w.updateNeighborsAt(p,this);w.updateNeighborsAt(q,this);
 }
 public void neighborChanged(BlockState s,Level w,BlockPos p,Block neighbor,BlockPos q,boolean moving){connect(w,p);}
 public ItemInteractionResult useItemOn(ItemStack stack,BlockState s,Level w,BlockPos p,Player player,InteractionHand hand,BlockHitResult hit){if(w.getBlockEntity(p)instanceof TableTile t)t.interact(player,hand,hit);return stack.isEmpty()||stack.is(org.slavicmyths.kitchen.KitchenII.tag("placeable_table_foods"))?ItemInteractionResult.sidedSuccess(w.isClientSide):ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;}
 public void onRemove(BlockState s,Level w,BlockPos p,BlockState next,boolean moving){if(!s.is(next.getBlock())&&!w.isClientSide&&w.getBlockEntity(p)instanceof TableTile t)t.drop();super.onRemove(s,w,p,next,moving);}
 public BlockState rotate(BlockState s,Rotation r){s=super.rotate(s,r);Connection c=s.getValue(CONNECTION);return c==Connection.NONE?s:s.setValue(CONNECTION,Connection.of(r.rotate(c.direction())));}
 public BlockState mirror(BlockState s,Mirror m){Connection c=s.getValue(CONNECTION);BlockState result=s.setValue(FACING,m.mirror(s.getValue(FACING)));return c==Connection.NONE?result:result.setValue(CONNECTION,Connection.of(m.mirror(c.direction())));}
}
