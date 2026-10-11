package org.slavicmyths.verify;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.neoforged.neoforge.gametest.*;
import org.slavicmyths.chud.*;
@GameTestHolder("slavicmyths_abandoned_full") @PrefixGameTestTemplate(false)
public final class AbandonedFullGameTests {
    @GameTest(template="empty",timeoutTicks=7200) public static void threeNativeComplexes(GameTestHelper t)throws Exception{
        var world=t.getLevel();var report=new com.google.gson.JsonArray();
        for(int scenario=0;scenario<5;scenario++){
            var kind=AbandonedPlan.Kind.values()[Math.min(scenario,2)];int profile=Math.max(0,scenario-2);
            var anchor=t.absolutePos(new BlockPos(640+scenario*640,140,640));var city=new ChudPlan(152,anchor,2,scenario%4,60);
            var p=new AbandonedPlan(152,anchor,city.rotation,kind,kind==AbandonedPlan.Kind.INDEPENDENT_GREAT?112:kind==AbandonedPlan.Kind.INDEPENDENT_MEDIUM?80:60,kind==AbandonedPlan.Kind.ATTACHED_SMALL?city.cityId():null,1,profile);
            net.minecraft.world.level.levelgen.structure.StructurePiece original=kind==AbandonedPlan.Kind.ATTACHED_SMALL?new ChudPiece(city,p):new AbandonedPiece(p);
            var context=net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext.fromLevel(world);var tag=original.createTag(context);
            net.minecraft.world.level.levelgen.structure.StructurePiece restored=kind==AbandonedPlan.Kind.ATTACHED_SMALL?new ChudPiece(tag):new AbandonedPiece(tag);
            t.assertTrue(tag.equals(restored.createTag(context)),"native NBT changed "+kind);
            long planningBegin=System.nanoTime();
            var nativeBlueprint=restored instanceof ChudPiece cp?cp.blueprint():((AbandonedPiece)restored).blueprint();
            double blueprintPlanningMs=(System.nanoTime()-planningBegin)/1_000_000.0;
            long[] finalBlocks={0,0};
            var box=restored.getBoundingBox();var chunks=new ArrayList<ChunkPos>();for(int x=box.minX()>>4;x<=box.maxX()>>4;x++)for(int z=box.minZ()>>4;z<=box.maxZ()>>4;z++)chunks.add(new ChunkPos(x,z));Collections.shuffle(chunks,new Random(152));
            var nativeMillis=new ArrayList<Double>();long begin=System.nanoTime();for(var chunk:chunks){world.getChunk(chunk.x,chunk.z);var clip=new BoundingBox(chunk.getMinBlockX(),box.minY(),chunk.getMinBlockZ(),chunk.getMaxBlockX(),box.maxY(),chunk.getMaxBlockZ());for(int x=clip.minX();x<=clip.maxX();x++)for(int z=clip.minZ();z<=clip.maxZ();z++)for(int y=clip.minY();y<=clip.maxY();y++)world.setBlock(new BlockPos(x,y,z),Blocks.STONE.defaultBlockState(),2);long pieceBegin=System.nanoTime();restored.postProcess(world,world.structureManager(),world.getChunkSource().getGenerator(),net.minecraft.util.RandomSource.create(152),clip,chunk,anchor);nativeMillis.add((System.nanoTime()-pieceBegin)/1_000_000.0);}
            AbandonedGameTests.assertRails(t,p,world::getBlockState);
            for(var chunk:chunks){var clip=new BoundingBox(chunk.getMinBlockX(),box.minY(),chunk.getMinBlockZ(),chunk.getMaxBlockX(),box.maxY(),chunk.getMaxBlockZ());var cells=new net.minecraft.world.level.block.state.BlockState[(box.maxY()-box.minY()+1)*256];ChudGeometry.rasterize(nativeBlueprint,clip,(pos,state)->cells[(pos.getY()-box.minY())*256+(pos.getZ()&15)*16+(pos.getX()&15)]=state);for(var state:cells)if(state!=null){finalBlocks[0]++;if(state.isAir())finalBlocks[1]++;}}
            var geometry=AbandonedGeometry.build(p);AbandonedGameTests.assertWater(t,geometry,world::getBlockState);AbandonedGameTests.assertRailSupports(t,geometry,world::getBlockState);
            var targets=new LinkedHashMap<String,BlockPos>();p.targets.forEach((id,pos)->targets.put(id,p.world(pos)));if(kind==AbandonedPlan.Kind.ATTACHED_SMALL)city.points.forEach((role,pos)->targets.put("CITY:"+role,city.world(pos)));
            var audit=ChudNavigation.inspect(world,p.nativeBounds(),kind==AbandonedPlan.Kind.ATTACHED_SMALL?city.world(city.points.get(ChudPlan.Role.ENTRANCE)):p.world(p.targets.get("entrance")),targets);
            t.assertTrue(audit.status().equals("PASS"),"actual native paths "+kind+" "+audit);
            var row=new com.google.gson.JsonObject();row.addProperty("kind",kind.name());row.addProperty("chunks",chunks.size());row.addProperty("elapsedMsIncludingStoneFixture",(System.nanoTime()-begin)/1_000_000.0);row.addProperty("pathStatus",audit.status());row.addProperty("visited",audit.visited());Collections.sort(nativeMillis);row.addProperty("nativePostProcessTotalMs",nativeMillis.stream().mapToDouble(Double::doubleValue).sum());row.addProperty("nativePostProcessP95Ms",nativeMillis.get((int)Math.ceil(nativeMillis.size()*.95)-1));row.addProperty("nativePostProcessMaxMs",nativeMillis.getLast());row.addProperty("nbtRoundtrip",true);row.addProperty("clientRelog",false);report.add(row);System.out.println("ABANDONED_FULL "+row);
            row.addProperty("shaftProfile",profile);row.addProperty("pieces",1);row.addProperty("blueprintPlanningMs",blueprintPlanningMs);row.addProperty("finalDestinationBlocks",finalBlocks[0]);row.addProperty("carvedAirBlocks",finalBlocks[1]);
        }
        var path=java.nio.file.Path.of(System.getProperty("slavicmyths.chudRoot"),"docs/verification/chud-1.5.2/full-native-placement.json");java.nio.file.Files.writeString(path,new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(report));t.succeed();
    }
}
