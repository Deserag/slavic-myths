package org.slavicmyths.blackcinder;
import java.util.UUID;
import java.nio.charset.StandardCharsets;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
/** Saved one-shot encounter: BossPlaced is never cleared when the boss dies. */
public final class CinderArenaPiece extends StructurePiece {
 private final BlockPos center;private final long seed;private boolean bossPlaced;
 public CinderArenaPiece(BlockPos p,long seed){super(CinderWorldgen.PIECE.get(),0,new BoundingBox(p.getX()-16,p.getY()-7,p.getZ()-16,p.getX()+16,174,p.getZ()+16));center=p;this.seed=seed;}
 public CinderArenaPiece(CompoundTag t){super(CinderWorldgen.PIECE.get(),t);center=BlockPos.of(t.getLong("Center"));seed=t.getLong("Seed");bossPlaced=t.getBoolean("BossPlaced");}
 @Override protected void addAdditionalSaveData(StructurePieceSerializationContext c,CompoundTag t){t.putLong("Center",center.asLong());t.putLong("Seed",seed);t.putBoolean("BossPlaced",bossPlaced);}
 @Override public void postProcess(WorldGenLevel w,StructureManager m,ChunkGenerator g,RandomSource r,BoundingBox clip,ChunkPos chunk,BlockPos pivot){
  for(int dx=-16;dx<=16;dx++)for(int dz=-16;dz<=16;dz++){
   double dist=Math.sqrt(dx*dx+dz*dz);if(dist>16)continue;boolean trail=Math.abs(dx)<=2||Math.abs(dz)<=2||dist<7;
   for(int dy=-6;dy<=174-center.getY();dy++){
    var p=center.offset(dx,dy,dz);if(!clip.isInside(p)||!w.ensureCanWrite(p)||w.getBlockEntity(p)!=null)continue;var old=w.getBlockState(p);
    if(!old.isAir()&&!old.is(Blocks.NETHERRACK)&&!old.is(Blocks.LAVA)&&!old.is(Blocks.MAGMA_BLOCK))continue;
    if(dy<0)w.setBlock(p,CinderBlocks.get("charred_stone").defaultBlockState(),2);
    else if(dy==0)w.setBlock(p,trail?CinderBlocks.get("ash_crust").defaultBlockState():dist>13&&dist<15&&dx*dz< -70?Blocks.LAVA.defaultBlockState():dist>13&&dx*dz>50?Blocks.MAGMA_BLOCK.defaultBlockState():CinderBlocks.get("black_cinder_soil").defaultBlockState(),2);
    else w.setBlock(p,Blocks.CAVE_AIR.defaultBlockState(),2);
   }
  }
  if(!bossPlaced&&chunk.equals(new ChunkPos(center))&&clip.isInside(center.above())){
   var id=UUID.nameUUIDFromBytes(("cinder-arena:"+seed+":"+center.asLong()).getBytes(StandardCharsets.UTF_8));
   var boss=CinderContent.TYPES.get(CinderContent.Kind.ZHARENITSA).get().create(w.getLevel());if(boss!=null){boss.setUUID(id);boss.moveTo(center.getX()+.5,center.getY()+1,center.getZ()+.5,0,0);boss.home(center.above());bossPlaced=w.addFreshEntity(boss);}
  }
 }
}
