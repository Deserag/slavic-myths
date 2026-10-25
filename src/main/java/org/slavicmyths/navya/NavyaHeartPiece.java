package org.slavicmyths.navya;
import java.util.*;
import java.nio.charset.StandardCharsets;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
public final class NavyaHeartPiece extends StructurePiece {
    private final BlockPos center;
    private final long seed;
    private boolean bossPlaced;
    public NavyaHeartPiece(BlockPos center,long seed){super(NavyaWorldgen.PIECE.get(),0,new BoundingBox(center.getX()-15,center.getY()-8,center.getZ()-15,center.getX()+15,center.getY()+12,center.getZ()+15));this.center=center;this.seed=seed;}
    public NavyaHeartPiece(CompoundTag tag){super(NavyaWorldgen.PIECE.get(),tag);center=BlockPos.of(tag.getLong("Center"));seed=tag.getLong("Seed");bossPlaced=tag.getBoolean("BossPlaced");}
    @Override protected void addAdditionalSaveData(StructurePieceSerializationContext context,CompoundTag tag){tag.putLong("Center",center.asLong());tag.putLong("Seed",seed);tag.putBoolean("BossPlaced",bossPlaced);}
    @Override public void postProcess(WorldGenLevel world,StructureManager manager,ChunkGenerator generator,RandomSource random,BoundingBox clip,ChunkPos chunk,BlockPos pivot){
        // Refuse overlap with any other registered structure, before any edits.
        for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++) {
            var check=center.offset(dx*16,0,dz*16);
            if(!world.hasChunkAt(check) || NavyaWorldgen.protectedChunk(world,check))return;
        }
        for(int dx=-12;dx<=12;dx++)for(int dz=-12;dz<=12;dz++) {
            double radius=Math.sqrt(dx*dx+dz*dz);if(radius>12+.8*NavyaProfile.noise(seed,center.getX()+dx,center.getZ()+dz,9))continue;
            var floor=center.offset(dx,0,dz);
            for(int dy=-7;dy<=9;dy++) {
                var pos=floor.above(dy);if(!clip.isInside(pos) || !world.ensureCanWrite(pos))continue;
                var old=world.getBlockState(pos);
                if(!old.isAir() && !old.is(Blocks.NETHERRACK))continue;
                if(dy<0)world.setBlock(pos,NavyaBlocks.get("rooted_navya_stone").defaultBlockState(),2);
                else if(dy==0)world.setBlock(pos,NavyaBlocks.get(radius<7?"navya_soil":"mossy_navya_soil").defaultBlockState(),2);
                else world.setBlock(pos,Blocks.CAVE_AIR.defaultBlockState(),2);
            }
        }
        for(int i=0;i<6;i++) {
            double angle=i*Math.PI/3;var base=center.offset((int)Math.round(Math.cos(angle)*11),1,(int)Math.round(Math.sin(angle)*11));
            var plan=new NavyaTreePlan(0,seed+i*17);
            for(var entry:plan.logs.entrySet()) {
                var c=entry.getKey();var pos=base.offset(c.x(),c.y(),c.z());if(!clip.isInside(pos) || !world.ensureCanWrite(pos) || !world.isEmptyBlock(pos))continue;
                world.setBlock(pos,NavyaBlocks.get("navya_log").defaultBlockState().setValue(RotatedPillarBlock.AXIS,Direction.Axis.values()[entry.getValue().axis()]).setValue(NavyaBlocks.WEATHERED,true),2);
            }
            for(var c:plan.foliage){var pos=base.offset(c.x(),c.y(),c.z());if(clip.isInside(pos)&&world.ensureCanWrite(pos)&&world.isEmptyBlock(pos))world.setBlock(pos,NavyaBlocks.get("navya_dead_leaves").defaultBlockState().setValue(LeavesBlock.DISTANCE,1),2);}
        }
        if(!bossPlaced && chunk.equals(new ChunkPos(center)) && clip.isInside(center.above())) {
            var boss=NavyaContent.MOBS.get(NavyaContent.Kind.GUARDIAN).get().create(world.getLevel());
            if(boss!=null) {
                boss.setUUID(UUID.nameUUIDFromBytes(("navya-heart:"+seed+":"+center.asLong()).getBytes(StandardCharsets.UTF_8)));
                boss.moveTo(center.getX()+.5,center.getY()+1,center.getZ()+.5,0,0);boss.home(center.above());
                bossPlaced=world.addFreshEntity(boss);
            }
        }
    }
}
