package org.slavicmyths.serpenthill;
import java.util.*;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.*;
import org.slavicmyths.worldgen.*;
public final class SerpentHillStructure extends Structure {
 public static final MapCodec<SerpentHillStructure> CODEC=simpleCodec(SerpentHillStructure::new);
 /** Far-world natural encounter; explicit dev generation remains unrestricted. */
 private static final long MIN_ORIGIN_DISTANCE_SQUARED=5000L*5000L;
 public SerpentHillStructure(StructureSettings s){super(s);}
 @Override public StructureType<?> type(){return HillContent.STRUCTURE.get();}
 @Override public Optional<GenerationStub> findGenerationPoint(GenerationContext c){if(c.heightAccessor() instanceof net.minecraft.world.level.WorldGenLevel w&&w.getLevel().dimension()!=net.minecraft.world.level.Level.OVERWORLD)return Optional.empty();var center=c.chunkPos().getMiddleBlockPosition(0);long x=center.getX(),z=center.getZ();if(x*x+z*z<MIN_ORIGIN_DISTANCE_SQUARED)return Optional.empty();var heights=new ArrayList<Integer>();for(int dx=-56;dx<=56;dx+=28)for(int dz=-56;dz<=56;dz+=28){int x=center.getX()+dx,z=center.getZ()+dz,y=c.chunkGenerator().getBaseHeight(x,z,Heightmap.Types.OCEAN_FLOOR_WG,c.heightAccessor(),c.randomState());var column=c.chunkGenerator().getBaseColumn(x,z,c.heightAccessor(),c.randomState());if(y<63||!column.getBlock(y-1).getFluidState().isEmpty())return Optional.empty();heights.add(y);}Collections.sort(heights);if(heights.getLast()-heights.getFirst()>40)return Optional.empty();int y=heights.get(heights.size()/2)-12;if(y<c.heightAccessor().getMinBuildHeight()||y+144>c.heightAccessor().getMaxBuildHeight())return Optional.empty();var rotation=Rotation.getRandom(c.random());var origin=HillGeometry.origin(new BlockPos(center.getX()-64,y,center.getZ()-64),rotation);var piece=new SerpentHillPiece(origin,rotation,c.random().nextLong(),new UUID(c.random().nextLong(),c.random().nextLong()));var parts=List.<StructurePiece>of(piece);if(!StructureCoverageService.referencesFit(c.chunkPos(),parts)||!StructureCandidates.clearAccepted(c,c.chunkPos(),other->other==null||other.equals(HillContent.id("serpent_hill"))?0:other.getNamespace().equals("minecraft")&&StructureCandidates.vanilla(other,"village","woodland_mansion","pillager_outpost","ruined_portal")?12:other.getNamespace().equals("slavicmyths")?8:0,parts))return Optional.empty();return Optional.of(new GenerationStub(origin.offset(new BlockPos(97,81,82).rotate(rotation)),b->b.addPiece(piece)));}
}
