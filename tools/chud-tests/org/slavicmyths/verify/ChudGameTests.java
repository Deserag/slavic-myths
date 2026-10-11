package org.slavicmyths.verify;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.neoforged.neoforge.gametest.*;
import org.slavicmyths.chud.*;

@GameTestHolder("slavicmyths_chud") @PrefixGameTestTemplate(false)
public final class ChudGameTests {
    private static ChudPlan plan(int variant,long seed,int rotation){return new ChudPlan(seed,new BlockPos(0,64,0),variant,rotation,56);}
    @GameTest(template="empty",timeoutTicks=2400) public static void tenSeedsGeometryBenchmarks(GameTestHelper t)throws Exception{
        var report=new com.google.gson.JsonArray();
        for(long seed=0;seed<10;seed++)for(int variant=0;variant<3;variant++){
            long begin=System.nanoTime();var p=new ChudPlan(seed,new BlockPos(0,64,0),variant,(int)seed&3,new int[]{56,60,63}[(int)seed%3],(seed&1)==0?-1:1);var fs=ChudGeometry.build(p);var grid=new Grid(p.bounds());ChudGeometry.rasterize(fs,p.bounds(),grid::set);int placed=0,carved=0;
            var reachable=grid.flood(p.world(p.points.get(ChudPlan.Role.ENTRANCE)));
            for(var target:p.points.entrySet())t.assertTrue(reachable.get(grid.index(p.world(target.getValue()))),"seed/rotation unreachable role "+seed+" "+variant+" "+target.getKey());
            for(var room:p.homes){var target=p.world(new BlockPos(room.x()+3+room.variant()%3,room.y()+1,room.z()+room.depth()-2));t.assertTrue(reachable.get(grid.index(target)),"seed/rotation unreachable home "+seed+" "+variant+" "+room.id());}
            for(var room:p.homes)if(room.variant()==2){var target=p.world(new BlockPos(room.x()+3,room.y()+6,room.z()+4));t.assertTrue(reachable.get(grid.index(target)),"seed/rotation unreachable upper home "+seed+" "+variant+" "+room.id());}
            for(var pos:List.of(new BlockPos(-20,8,8),new BlockPos(19,8,8),new BlockPos(0,8,17),new BlockPos(-70,-3,21),new BlockPos(-109,-27,-106),new BlockPos(-109,-17,-101)))t.assertTrue(reachable.get(grid.index(p.world(pos))),"seed/rotation unreachable gallery "+seed+" "+variant+" "+pos);
            for(var s:grid.states)if(s!=null){if(s.isAir())carved++;else placed++;}
            var actual=new ChudPiece(p).getBoundingBox();for(var f:fs)t.assertTrue(actual.isInside(new BlockPos(f.box().minX(),f.box().minY(),f.box().minZ()))&&actual.isInside(new BlockPos(f.box().maxX(),f.box().maxY(),f.box().maxZ())),"fill outside native piece");
            var row=new com.google.gson.JsonObject();row.addProperty("seed",seed);row.addProperty("variant",variant);row.addProperty("fillOperations",fs.size());row.addProperty("carvedBlocks",carved);row.addProperty("placedBlocks",placed);row.addProperty("blueprintAndFullRasterMilliseconds",(System.nanoTime()-begin)/1_000_000.0);report.add(row);
            if(seed==0&&variant==2){var output=java.nio.file.Path.of(System.getProperty("slavicmyths.chudRoot"),"work/chud-151/preview-fills.csv.gz");java.nio.file.Files.createDirectories(output.getParent());try(var writer=new java.io.PrintWriter(new java.util.zip.GZIPOutputStream(java.nio.file.Files.newOutputStream(output)))){for(var fill:fs){var b=fill.box();writer.println(b.minX()+","+b.minY()+","+b.minZ()+","+b.maxX()+","+b.maxY()+","+b.maxZ()+","+net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(fill.state().getBlock()));}}}
        }
        write("geometry-benchmarks.json",report);t.succeed();
    }
    public static void naturalTerrainCandidates(GameTestHelper t)throws Exception{
        var w=t.getLevel();var registry=w.registryAccess();var noise=registry.registryOrThrow(net.minecraft.core.registries.Registries.NOISE_SETTINGS).getHolderOrThrow(net.minecraft.world.level.levelgen.NoiseGeneratorSettings.OVERWORLD);
        var preset=registry.registryOrThrow(net.minecraft.core.registries.Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST).getHolderOrThrow(net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterLists.OVERWORLD);
        var generator=new net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator(net.minecraft.world.level.biome.MultiNoiseBiomeSource.createFromPreset(preset),noise);
        var structure=(ChudStructure)registry.registryOrThrow(net.minecraft.core.registries.Registries.STRUCTURE).get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths","chud_city"));
        var set=registry.registryOrThrow(net.minecraft.core.registries.Registries.STRUCTURE_SET).get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths","chud_city"));var placement=(net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement)set.placement();
        var report=new com.google.gson.JsonArray();int totalAccepted=0;
        for(long seed=0;seed<10;seed++){
            var random=net.minecraft.world.level.levelgen.RandomState.create(noise.value(),registry.lookupOrThrow(net.minecraft.core.registries.Registries.NOISE),seed);int eligible=0,accepted=0;long begin=System.nanoTime();var entries=new com.google.gson.JsonArray();var rejects=new HashMap<String,Integer>();
            record Cell(ChunkPos chunk,int surface){}var cells=new ArrayList<Cell>();
            for(int gx=-32;gx<=32;gx++)for(int gz=-32;gz<=32;gz++){
                var chunk=placement.getPotentialStructureChunk(seed,gx*256,gz*256);var biome=generator.getBiomeSource().getNoiseBiome((chunk.x<<2)+2,24,(chunk.z<<2)+2,random.sampler());if(!structure.biomes().contains(biome))continue;
                int height=generator.getBaseHeight(chunk.getMiddleBlockX(),chunk.getMiddleBlockZ(),net.minecraft.world.level.levelgen.Heightmap.Types.OCEAN_FLOOR_WG,w,random);cells.add(new Cell(chunk,height));
            }
            eligible=cells.size();cells.sort(Comparator.comparingInt(Cell::surface).reversed());
            for(int index=0;index<Math.min(64,cells.size());index++){
                var chunk=cells.get(index).chunk();
                var c=new net.minecraft.world.level.levelgen.structure.Structure.GenerationContext(registry,generator,generator.getBiomeSource(),random,w.getStructureManager(),seed,chunk,w,structure.biomes()::contains);
                var start=structure.generate(registry,generator,generator.getBiomeSource(),random,w.getStructureManager(),seed,chunk,0,w,structure.biomes()::contains);
                if(start.isValid()){accepted++;for(var piece:start.getPieces())if(piece instanceof ChudPiece p){entries.add(new com.google.gson.Gson().toJsonTree(Map.of("anchor",p.plan.anchor.toShortString(),"variant",p.plan.variant,"rotation",p.plan.rotation,"cityId",p.plan.cityId())));}}
                else{var diagnostic=org.slavicmyths.worldgen.StructureCoverageService.lastPlacement();rejects.merge(diagnostic==null?"unknown":diagnostic.lastMeasurements().toString(),1,Integer::sum);}
            }
            var row=new com.google.gson.JsonObject();row.addProperty("seed",seed);row.addProperty("eligibleCells",eligible);row.addProperty("sampling","highest 64 native mountain candidates within 65x65 placement regions");row.addProperty("highestCandidateSurface",cells.isEmpty()?0:cells.getFirst().surface());row.addProperty("sampledMountainCells",Math.min(64,eligible));row.addProperty("acceptedStarts",accepted);row.addProperty("elapsedMilliseconds",(System.nanoTime()-begin)/1_000_000.0);row.add("starts",entries);row.add("rejections",new com.google.gson.Gson().toJsonTree(rejects));report.add(row);totalAccepted+=accepted;System.out.println("CHUD_NATURAL seed="+seed+" eligible="+eligible+" accepted="+accepted+" reasons="+rejects);
        }
        write("natural-candidates.json",report);
        t.assertTrue(totalAccepted>0,"No naturally accepted city in measured mountain candidates");t.succeed();
    }
    private static void write(String name,com.google.gson.JsonElement data)throws Exception{var path=java.nio.file.Path.of(System.getProperty("slavicmyths.chudRoot"),"docs/verification/chud-1.5.1",name);java.nio.file.Files.createDirectories(path.getParent());java.nio.file.Files.writeString(path,new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(data));}
    @GameTest(template="empty",timeoutTicks=200) public static void plansPersistenceAndSpacing(GameTestHelper t){
        int n=0;
        for(long seed=0;seed<12;seed++)for(int variant=0;variant<3;variant++)for(int rotation=0;rotation<4;rotation++){
            var p=plan(variant,seed,rotation);p.validate();var q=ChudPlan.load(p.save());t.assertTrue(p.save().equals(q.save()),"NBT plan changed");t.assertTrue(p.majorHalls()==3+variant,"major halls");
            var owner=new ChunkPos(0,0);var piece=new ChudPiece(p);t.assertTrue(org.slavicmyths.worldgen.StructureCoverageService.referencesFit(owner,List.of(piece)),"native reference radius");
            var neighbor=new ChudPlan(seed,new BlockPos(2576,64,0),variant,rotation,14);
            t.assertTrue(ChudPlan.boundaryDistanceSquared(p.bounds(),neighbor.bounds())>=4_000_000,"nearest region boundary distance");n++;
        }
        t.assertTrue(n==144,"sample count");t.succeed();
    }
    @GameTest(template="empty",timeoutTicks=200) public static void boundaryDistanceCases(GameTestHelper t){
        var a=new BoundingBox(0,0,0,200,50,200);
        t.assertTrue(ChudPlan.boundaryDistanceSquared(a,new BoundingBox(2199,100,0,2399,150,200))<4_000_000,"1999 boundary gap accepted");
        t.assertTrue(ChudPlan.boundaryDistanceSquared(a,new BoundingBox(2200,100,0,2400,150,200))==4_000_000,"exact 2000 boundary gap rejected");
        t.assertTrue(ChudPlan.boundaryDistanceSquared(a,new BoundingBox(1400,-50,1800,1600,-10,2000))==4_000_000,"1200/1600 diagonal");
        t.assertTrue(ChudPlan.boundaryDistanceSquared(a,new BoundingBox(0,100,0,200,150,200))==0,"Y incorrectly contributes to spacing");t.succeed();
    }
    @GameTest(template="empty",timeoutTicks=200) public static void actualThreeHallNavigation(GameTestHelper t){navigation(t,0);}
    @GameTest(template="empty",timeoutTicks=200) public static void actualFourHallNavigation(GameTestHelper t){navigation(t,1);}
    @GameTest(template="empty",timeoutTicks=200) public static void actualFiveHallNavigation(GameTestHelper t){navigation(t,2);}
    private static void navigation(GameTestHelper t,int variant){
        var p=plan(variant,151,0);long begin=System.nanoTime();var fills=ChudGeometry.build(p);var grid=new Grid(p.bounds());
        ChudGeometry.rasterize(fills,p.bounds(),grid::set);
        var reached=grid.flood(p.world(p.points.get(ChudPlan.Role.ENTRANCE)));
        var failures=new ArrayList<String>();
        for(var role:ChudPlan.Role.values()){var pos=p.world(p.points.get(role));if(!reached.get(grid.index(pos)))failures.add(role+"="+pos.toShortString()+" body="+grid.get(pos)+" head="+grid.get(pos.above()));}
        for(var home:p.homes){var pos=p.world(new BlockPos(home.x()+2+(home.variant()%3)+1,home.y()+1,home.z()+home.depth()-2));if(!reached.get(grid.index(pos)))failures.add(home.id()+"="+pos.toShortString());}
        // All declared upper residential tiers, not just metadata edges.
        for(int tier=1;tier<(variant==0?2:3);tier++){var pos=p.world(new BlockPos(76,15+tier*12,37));if(!reached.get(grid.index(pos)))failures.add("upper tier "+tier);}
        for(var pos:List.of(new BlockPos(-20,8,8),new BlockPos(19,8,8),new BlockPos(0,8,17),new BlockPos(-70,-3,21),new BlockPos(-109,-27,-106),new BlockPos(-109,-17,-101))){var q=p.world(pos);if(!reached.get(grid.index(q)))failures.add("terrace="+pos.toShortString()+" body="+grid.get(q));}
        System.out.println("CHUD_NAV variant="+variant+" fills="+fills.size()+" visited="+reached.cardinality()+" elapsedMs="+(System.nanoTime()-begin)/1_000_000+" failures="+failures);
        t.assertTrue(failures.isEmpty(),"unreachable "+failures);t.succeed();
    }
    @GameTest(template="empty",timeoutTicks=200) public static void nativeChunkPlacementAndIdempotence(GameTestHelper t){
        var w=t.getLevel();var center=t.absolutePos(new BlockPos(32,90,32));
        var p=new ChudPlan(151,center,2,0,38);var piece=new ChudPiece(p);var chunk=new ChunkPos(p.world(new BlockPos(124,38,38)));
        var clip=new BoundingBox(chunk.getMinBlockX(),w.getMinBuildHeight(),chunk.getMinBlockZ(),chunk.getMaxBlockX(),w.getMaxBuildHeight()-1,chunk.getMaxBlockZ());
        w.getChunk(chunk.x,chunk.z);var expected=new HashMap<BlockPos,BlockState>();ChudGeometry.rasterize(piece.blueprint(),clip,expected::put);
        var sentinel=new BlockPos(chunk.getMinBlockX()-1,center.getY()+38,chunk.getMinBlockZ()+6);w.setBlock(sentinel,Blocks.GOLD_BLOCK.defaultBlockState(),2);
        for(var pos:expected.keySet())w.setBlock(pos,Blocks.STONE.defaultBlockState(),2);
        piece.postProcess(w,w.structureManager(),w.getChunkSource().getGenerator(),net.minecraft.util.RandomSource.create(1),clip,chunk,p.anchor);
        for(var entry:expected.entrySet())t.assertTrue(w.getBlockState(entry.getKey()).equals(entry.getValue()),"native placement differs "+entry.getKey());
        piece.postProcess(w,w.structureManager(),w.getChunkSource().getGenerator(),net.minecraft.util.RandomSource.create(999),clip,chunk,p.anchor);
        for(var entry:expected.entrySet())t.assertTrue(w.getBlockState(entry.getKey()).equals(entry.getValue()),"second placement differs");
        t.assertTrue(w.getBlockState(sentinel).is(Blocks.GOLD_BLOCK),"write outside chunk clip");t.succeed();
    }
    public static void fullCityNativePlacement(GameTestHelper t)throws Exception{
        var world=t.getLevel();var p=new ChudPlan(151,t.absolutePos(new BlockPos(640,150,640)),2,0,60,-1);var piece=new ChudPiece(p);
        var context=net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext.fromLevel(world);
        var saved=piece.createTag(context);var restored=new ChudPiece(saved);t.assertTrue(p.save().equals(restored.plan.save()),"native piece serialization changed plan");
        var box=piece.getBoundingBox();var chunks=new ArrayList<ChunkPos>();for(int x=box.minX()>>4;x<=box.maxX()>>4;x++)for(int z=box.minZ()>>4;z<=box.maxZ()>>4;z++)chunks.add(new ChunkPos(x,z));Collections.shuffle(chunks,new Random(151));
        long begin=System.nanoTime();for(var chunk:chunks){world.getChunk(chunk.x,chunk.z);var clip=new BoundingBox(chunk.getMinBlockX(),box.minY(),chunk.getMinBlockZ(),chunk.getMaxBlockX(),box.maxY(),chunk.getMaxBlockZ());for(int x=clip.minX();x<=clip.maxX();x++)for(int z=clip.minZ();z<=clip.maxZ();z++)for(int y=clip.minY();y<=clip.maxY();y++)world.setBlock(new BlockPos(x,y,z),Blocks.STONE.defaultBlockState(),2);restored.postProcess(world,world.structureManager(),world.getChunkSource().getGenerator(),net.minecraft.util.RandomSource.create(151),clip,chunk,p.anchor);}
        var audit=ChudNavigation.inspect(world,p);t.assertTrue(audit.status().equals("PASS"),"actual full city navigation "+audit);
        var result=new com.google.gson.JsonObject();result.addProperty("placedChunks",chunks.size());result.addProperty("nativePlacementMilliseconds",(System.nanoTime()-begin)/1_000_000.0);result.addProperty("pathStatus",audit.status());result.addProperty("visitedNodes",audit.visited());result.addProperty("nativePieceNbtRoundtrip",true);result.addProperty("realClientRelogTest",false);write("full-native-placement.json",result);t.succeed();
    }
    @GameTest(template="empty",timeoutTicks=200) public static void contentAndChunkClipping(GameTestHelper t){
        var p=plan(2,982,3);var fs=ChudGeometry.build(p);var a=new Grid(p.bounds());var b=new Grid(p.bounds());
        ChudGeometry.rasterize(fs,p.bounds(),a::set);
        List<ChunkPos>chunks=new ArrayList<>();var box=p.bounds();for(int x=box.minX()>>4;x<=box.maxX()>>4;x++)for(int z=box.minZ()>>4;z<=box.maxZ()>>4;z++)chunks.add(new ChunkPos(x,z));Collections.shuffle(chunks,new Random(9));
        for(var chunk:chunks){var clip=new BoundingBox(chunk.getMinBlockX(),box.minY(),chunk.getMinBlockZ(),chunk.getMaxBlockX(),box.maxY(),chunk.getMaxBlockZ());ChudGeometry.rasterize(fs,clip,(pos,s)->{t.assertTrue(clip.isInside(pos),"outside chunk");b.set(pos,s);});}
        t.assertTrue(Arrays.equals(a.states,b.states),"chunk order changes geometry");
        for(var s:a.states)if(s!=null)t.assertTrue(!s.is(Blocks.CHEST)&&!s.is(Blocks.TRAPPED_CHEST)&&!s.is(Blocks.BARREL)&&!s.is(Blocks.HOPPER)&&!(s.getBlock() instanceof ShulkerBoxBlock),"forbidden container");
        t.succeed();
    }
    @GameTest(template="empty",timeoutTicks=200) public static void registeredWorldgenAndPlacement(GameTestHelper t){
        var registry=t.getLevel().registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.STRUCTURE);
        var structure=registry.get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths","chud_city"));t.assertTrue(structure instanceof ChudStructure,"structure codec not loaded");
        var set=t.getLevel().registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.STRUCTURE_SET).get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths","chud_city"));
        t.assertTrue(set!=null&&set.placement() instanceof net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement,"structure set missing");var placement=(net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement)set.placement();t.assertTrue(placement.spacing()==256&&placement.separation()==160,"placement missing");t.succeed();
    }
    static final class Grid {
        final BoundingBox box;final int w,d,h;final BlockState[] states;final byte[] collision;
        Grid(BoundingBox b){box=b;w=b.getXSpan();d=b.getZSpan();h=b.getYSpan();states=new BlockState[w*d*h];collision=new byte[states.length];}
        int index(BlockPos p){return(p.getY()-box.minY())*w*d+(p.getZ()-box.minZ())*w+p.getX()-box.minX();}
        void set(BlockPos p,BlockState s){if(box.isInside(p)){int i=index(p);states[i]=s;collision[i]=0;}}
        BlockState get(BlockPos p){if(!box.isInside(p))return Blocks.STONE.defaultBlockState();var s=states[index(p)];return s==null?Blocks.STONE.defaultBlockState():s;}
        byte kind(BlockPos p){if(!box.isInside(p))return 2;int i=index(p);byte v=collision[i];if(v!=0)return v;var s=get(p);if(s.getBlock() instanceof DoorBlock)return collision[i]=1;var shape=s.getCollisionShape(EmptyBlockGetter.INSTANCE,p);return collision[i]=(byte)(shape.isEmpty()?1:shape.max(Direction.Axis.Y)>1.01?3:2);}
        boolean walk(BlockPos p){return kind(p)==1&&kind(p.above())==1&&kind(p.below())==2;}
        BitSet flood(BlockPos start){var seen=new BitSet(states.length);var queue=new ArrayDeque<BlockPos>();if(!walk(start))return seen;seen.set(index(start));queue.add(start);while(!queue.isEmpty()){
            var p=queue.removeFirst();for(var dir:List.of(Direction.NORTH,Direction.SOUTH,Direction.WEST,Direction.EAST))for(int dy:new int[]{0,1,-1}){var q=p.relative(dir).offset(0,dy,0);if(!box.isInside(q)||seen.get(index(q))||!walk(q))continue;if(dy==1&&kind(p.above(2))!=1)continue;seen.set(index(q));queue.add(q);}
        }return seen;}
    }
}
