package org.slavicmyths.depth;
import java.util.UUID;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.nbt.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.*;
import net.minecraft.sounds.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.ParticleTypes;
import org.slavicmyths.registry.*;
public final class PoolStoneTile extends BlockEntity {
 private int state,intro;private UUID boss;public boolean natural;private CompoundTag flooded=new CompoundTag();
 public PoolStoneTile(net.minecraft.core.BlockPos pos,BlockState state){super(ModTiles.POOL_STONE.get(),pos,state);}
 public void activate(Player p,net.minecraft.world.InteractionHand h){if(state!=0||PoolIndex.get((ServerLevel)level).defeated(worldPosition)||p.getItemInHand(h).getItem()!=ModItems.ANCIENT_WATER_SIGN.get())return;if(!p.isCreative())p.getItemInHand(h).shrink(1);state=1;intro=60;setChanged();level.scheduleTick(worldPosition,ModBlocks.POOL_STONE.get(),1);org.slavicmyths.progression.Knowledge.award(p,"wake_depth_master");}
 public void onLoad(){super.onLoad();if(level!=null&&!level.isClientSide){PoolIndex.get((ServerLevel)level).record(worldPosition);if(PoolIndex.get((ServerLevel)level).defeated(worldPosition)){state=3;restore();}if(state==1)level.scheduleTick(worldPosition,ModBlocks.POOL_STONE.get(),20);}}
 public void step(){if(state!=1)return;ServerLevel w=(ServerLevel)level;w.sendParticles(ParticleTypes.BUBBLE,worldPosition.getX()+.5,worldPosition.getY()+1,worldPosition.getZ()+.5,8,.7,.3,.7,.025);if(intro==60)w.playSound(null,worldPosition,ModSounds.ELDER_PHASE.get(),SoundSource.HOSTILE,.8F,.75F);intro-=20;setChanged();if(intro>0){level.scheduleTick(worldPosition,ModBlocks.POOL_STONE.get(),20);return;}
  ElderVodyanoy e=ModEntities.ELDER_VODYANOY.get().create(level);if(e==null){state=0;setChanged();return;}e.home=worldPosition;e.moveTo(worldPosition.getX()+.5,worldPosition.getY()+1,worldPosition.getZ()+.5,0,0);boss=e.getUUID();state=2;setChanged();if(!level.addFreshEntity(e)){state=0;boss=null;net.minecraft.world.level.block.Block.popResource(level,worldPosition,new net.minecraft.world.item.ItemStack(ModItems.ANCIENT_WATER_SIGN.get()));setChanged();}
 }
 public void flood(){if(!natural||!flooded.isEmpty())return;for(int x=-5;x<=5;x++)for(int z=-5;z<=5;z++){BlockPos p=worldPosition.offset(x,3,z);if(level.hasChunkAt(p))flooded.put(Long.toString(p.asLong()),NbtUtils.writeBlockState(level.getBlockState(p)));}for(int x=-3;x<=3;x++)for(int z=-3;z<=3;z++){if(Math.abs(x)!=3&&Math.abs(z)!=3)continue;BlockPos p=worldPosition.offset(x,3,z);if(!level.hasChunkAt(p)||!level.isEmptyBlock(p)||!level.getBlockState(p.below()).is(Blocks.MOSSY_COBBLESTONE))continue;level.setBlock(p,Blocks.WATER.defaultBlockState(),2);}setChanged();}
 private void restore(){for(String key:flooded.getAllKeys()){BlockPos p=BlockPos.of(Long.parseLong(key));if(level.hasChunkAt(p)&&level.getBlockState(p).is(Blocks.WATER))level.setBlock(p,NbtUtils.readBlockState(level.holderLookup(net.minecraft.core.registries.Registries.BLOCK),flooded.getCompound(key)),2);}flooded=new CompoundTag();setChanged();}
 public void finish(UUID id){if(boss==null||!boss.equals(id)||state==3)return;state=3;PoolIndex.get((ServerLevel)level).defeat(worldPosition);restore();setChanged();}
 protected void saveAdditional(CompoundTag n,net.minecraft.core.HolderLookup.Provider registries){super.saveAdditional(n,registries);n.putInt("State",state);n.putInt("Intro",intro);n.putBoolean("Natural",natural);n.put("Flooded",flooded);if(boss!=null)n.putUUID("Boss",boss);}
 protected void loadAdditional(CompoundTag n,net.minecraft.core.HolderLookup.Provider registries){super.loadAdditional(n,registries);state=n.getInt("State");intro=n.getInt("Intro");natural=n.getBoolean("Natural");flooded=n.getCompound("Flooded");boss=n.hasUUID("Boss")?n.getUUID("Boss"):null;}
}
