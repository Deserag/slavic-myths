package org.slavicmyths.depth;
import java.util.UUID;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.nbt.*;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.particles.ParticleTypes;
import org.slavicmyths.registry.*;
public final class PoolStoneTile extends TileEntity {
 private int state,intro;private UUID boss;public boolean natural;private CompoundNBT flooded=new CompoundNBT();
 public PoolStoneTile(){super(ModTiles.POOL_STONE.get());}
 public void activate(PlayerEntity p,Hand h){if(state!=0||PoolIndex.get((ServerWorld)level).defeated(worldPosition)||p.getItemInHand(h).getItem()!=ModItems.ANCIENT_WATER_SIGN.get())return;if(!p.isCreative())p.getItemInHand(h).shrink(1);state=1;intro=60;setChanged();level.getBlockTicks().scheduleTick(worldPosition,ModBlocks.POOL_STONE.get(),1);org.slavicmyths.progression.Knowledge.award(p,"wake_depth_master");}
 public void onLoad(){super.onLoad();if(level!=null&&!level.isClientSide){PoolIndex.get((ServerWorld)level).record(worldPosition);if(PoolIndex.get((ServerWorld)level).defeated(worldPosition)){state=3;restore();}if(state==1)level.getBlockTicks().scheduleTick(worldPosition,ModBlocks.POOL_STONE.get(),20);}}
 public void step(){if(state!=1)return;ServerWorld w=(ServerWorld)level;w.sendParticles(ParticleTypes.BUBBLE,worldPosition.getX()+.5,worldPosition.getY()+1,worldPosition.getZ()+.5,8,.7,.3,.7,.025);if(intro==60)w.playSound(null,worldPosition,ModSounds.ELDER_PHASE.get(),SoundCategory.HOSTILE,.8F,.75F);intro-=20;setChanged();if(intro>0){level.getBlockTicks().scheduleTick(worldPosition,ModBlocks.POOL_STONE.get(),20);return;}
  ElderVodyanoy e=ModEntities.ELDER_VODYANOY.get().create(level);if(e==null){state=0;setChanged();return;}e.home=worldPosition;e.moveTo(worldPosition.getX()+.5,worldPosition.getY()+1,worldPosition.getZ()+.5,0,0);boss=e.getUUID();state=2;setChanged();if(!level.addFreshEntity(e)){state=0;boss=null;net.minecraft.block.Block.popResource(level,worldPosition,new net.minecraft.item.ItemStack(ModItems.ANCIENT_WATER_SIGN.get()));setChanged();}
 }
 public void flood(){if(!natural||!flooded.isEmpty())return;for(int x=-5;x<=5;x++)for(int z=-5;z<=5;z++){BlockPos p=worldPosition.offset(x,3,z);if(level.hasChunkAt(p))flooded.put(Long.toString(p.asLong()),NBTUtil.writeBlockState(level.getBlockState(p)));}for(int x=-3;x<=3;x++)for(int z=-3;z<=3;z++){if(Math.abs(x)!=3&&Math.abs(z)!=3)continue;BlockPos p=worldPosition.offset(x,3,z);if(!level.hasChunkAt(p)||!level.isEmptyBlock(p)||!level.getBlockState(p.below()).is(Blocks.MOSSY_COBBLESTONE))continue;level.setBlock(p,Blocks.WATER.defaultBlockState(),2);}setChanged();}
 private void restore(){for(String key:flooded.getAllKeys()){BlockPos p=BlockPos.of(Long.parseLong(key));if(level.hasChunkAt(p)&&level.getBlockState(p).is(Blocks.WATER))level.setBlock(p,NBTUtil.readBlockState(flooded.getCompound(key)),2);}flooded=new CompoundNBT();setChanged();}
 public void finish(UUID id){if(boss==null||!boss.equals(id)||state==3)return;state=3;PoolIndex.get((ServerWorld)level).defeat(worldPosition);restore();setChanged();}
 public CompoundNBT save(CompoundNBT n){super.save(n);n.putInt("State",state);n.putInt("Intro",intro);n.putBoolean("Natural",natural);n.put("Flooded",flooded);if(boss!=null)n.putUUID("Boss",boss);return n;}
 public void load(BlockState s,CompoundNBT n){super.load(s,n);state=n.getInt("State");intro=n.getInt("Intro");natural=n.getBoolean("Natural");flooded=n.getCompound("Flooded");boss=n.hasUUID("Boss")?n.getUUID("Boss"):null;}
}
