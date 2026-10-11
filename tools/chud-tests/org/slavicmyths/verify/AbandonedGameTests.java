package org.slavicmyths.verify;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.*;
import org.slavicmyths.chud.*;
@GameTestHolder("slavicmyths_abandoned") @PrefixGameTestTemplate(false)
public final class AbandonedGameTests {
    @GameTest(template="empty",timeoutTicks=2400) public static void twelveSeedGeometry(GameTestHelper t)throws Exception{
        var report=new com.google.gson.JsonArray();int failures=0;
        for(int seed=0;seed<12;seed++)for(var kind:AbandonedPlan.Kind.values()){
            long begin=System.nanoTime();var city=new ChudPlan(seed,new BlockPos(0,64,0),seed%3,seed%4,new int[]{56,60,63}[seed%3]);
            var p=new AbandonedPlan(seed,city.anchor,city.rotation,kind,kind==AbandonedPlan.Kind.ATTACHED_SMALL?city.entranceFloor:(kind==AbandonedPlan.Kind.INDEPENDENT_GREAT?new int[]{40,80,112}:new int[]{32,56,80})[kind==AbandonedPlan.Kind.INDEPENDENT_GREAT?(seed/3)%3:seed%3],kind==AbandonedPlan.Kind.ATTACHED_SMALL?city.cityId():null,kind==AbandonedPlan.Kind.INDEPENDENT_GREAT?(seed/4)%3:seed%3,kind==AbandonedPlan.Kind.INDEPENDENT_GREAT?seed%3:0);
            if(kind==AbandonedPlan.Kind.INDEPENDENT_GREAT){t.assertTrue(p.shaftHeight()>=42&&p.shaftHeight()<=85,"shaft height");t.assertTrue(p.shaftRings()>=3&&p.shaftRings()<=4,"shaft rings");t.assertTrue(p.shaftGap()>=14&&p.shaftGap()<=26,"shaft interval");}var all=new ArrayList<ChudGeometry.Fill>();if(kind==AbandonedPlan.Kind.ATTACHED_SMALL)all.addAll(ChudGeometry.build(city));var additions=AbandonedGeometry.build(p);all.addAll(additions);
            for(var fill:additions){var b=fill.box();t.assertTrue(p.nativeBounds().isInside(new BlockPos(b.minX(),b.minY(),b.minZ()))&&p.nativeBounds().isInside(new BlockPos(b.maxX(),b.maxY(),b.maxZ())),"out of native bounds "+kind+" "+fill);t.assertTrue(!fill.state().hasBlockEntity(),"new inventory/block entity "+fill.state());}
            var grid=new ChudGameTests.Grid(p.nativeBounds());ChudGeometry.rasterize(all,p.nativeBounds(),grid::set);
            assertRails(t,p,grid::get);
            assertRailSupports(t,additions,grid::get);
            assertWater(t,additions,grid::get);
            var start=kind==AbandonedPlan.Kind.ATTACHED_SMALL?city.world(city.points.get(ChudPlan.Role.ENTRANCE)):p.world(p.targets.get("entrance"));var seen=grid.flood(start);var unreachable=new ArrayList<String>();
            for(var target:p.targets.entrySet())if(!seen.get(grid.index(p.world(target.getValue()))))unreachable.add(target.getKey()+"="+target.getValue());
            if(kind==AbandonedPlan.Kind.ATTACHED_SMALL)for(var role:city.points.entrySet())if(!seen.get(grid.index(city.world(role.getValue()))))unreachable.add("CITY_A:"+role.getKey());
            System.out.println("ABANDONED_GEOMETRY seed="+seed+" type="+kind+" reachable="+seen.cardinality()+" unreachable="+unreachable);
            var row=new com.google.gson.JsonObject();row.addProperty("seed",seed);row.addProperty("type",kind.name());row.addProperty("rotation",p.rotation);row.addProperty("entranceFloor",p.entranceFloor);row.addProperty("sealedGate",p.sealedGate);row.addProperty("shaftProfile",p.shaftProfile);row.addProperty("levels",p.levels());row.addProperty("fills",all.size());row.addProperty("reachable",seen.cardinality());row.addProperty("elapsedMs",(System.nanoTime()-begin)/1_000_000.0);row.add("unreachable",new com.google.gson.Gson().toJsonTree(unreachable));report.add(row);
            if(!unreachable.isEmpty()){failures++;if(seed==0)for(var route:AbandonedGeometry.constructionRoutes(p)){int reached=0;var detail=new StringBuilder();for(int i=0;i<route.floors().size();i++){var q=p.world(route.floors().get(i).above());if(seen.get(grid.index(q)))reached++;else if(i%3==0)detail.append(" ").append(i).append(':').append(q).append(" floor=").append(grid.get(q.below())).append(" body=").append(grid.get(q)).append(" head=").append(grid.get(q.above()));}System.out.println("ABANDONED_ROUTE "+route.id()+" reached="+reached+"/"+route.floors().size()+detail);}}
            t.assertTrue(p.save().equals(AbandonedPlan.load(p.save()).save()),"NBT changed");
        }
        write(report);t.assertTrue(failures==0,"physical paths failed in "+failures+" of 36 plans; see geometry.json");
        for(var kind:AbandonedPlan.Kind.values())export(kind.name(),new AbandonedPlan(0,new BlockPos(0,64,0),0,kind,72,null,0));
        for(int seed=0;seed<100;seed++){var p=new AbandonedPlan(seed,new BlockPos(0,64,0),0,AbandonedPlan.Kind.INDEPENDENT_GREAT,72,null,0);if(p.sealedGate){export("GATE",p);break;}}
        t.succeed();
    }
    @GameTest(template="empty",timeoutTicks=200) public static void legacyAndSharedPlacement(GameTestHelper t){
        var city=new ChudPlan(151,new BlockPos(0,64,0),2,0,60);var original=new ChudPiece(city);var context=net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext.fromLevel(t.getLevel());
        var restored=new ChudPiece(original.createTag(context));t.assertTrue(restored.abandoned==null,"legacy unexpectedly opened gate");t.assertTrue(restored.getBoundingBox().equals(original.getBoundingBox()),"legacy bbox changed");
        var p=new AbandonedPlan(151,city.anchor,0,AbandonedPlan.Kind.ATTACHED_SMALL,60,city.cityId(),0);var extended=new ChudPiece(city,p);var saved=new ChudPiece(extended.createTag(context));t.assertTrue(saved.abandoned!=null&&saved.abandoned.save().equals(p.save()),"extension lost after piece roundtrip");
        var registry=t.getLevel().registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.STRUCTURE_SET);var a=registry.get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths","chud_city"));var b=registry.get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths","chud_abandoned"));
        var pa=(net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement)a.placement();var pb=(net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement)b.placement();
        for(int n=0;n<24;n++)t.assertTrue(pa.getPotentialStructureChunk(151,n*256,0).equals(pb.getPotentialStructureChunk(151,n*256,0)),"candidate grids diverge");
        t.succeed();
    }
    private static void write(com.google.gson.JsonArray report)throws Exception{var path=java.nio.file.Path.of(System.getProperty("slavicmyths.chudRoot"),"docs/verification/chud-1.5.2/geometry.json");java.nio.file.Files.createDirectories(path.getParent());java.nio.file.Files.writeString(path,new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(report));}
    private static void export(String name,AbandonedPlan p)throws Exception{
        var path=java.nio.file.Path.of(System.getProperty("slavicmyths.chudRoot"),"work/chud-152/preview-"+name+".csv.gz");java.nio.file.Files.createDirectories(path.getParent());
        try(var writer=new java.io.PrintWriter(new java.util.zip.GZIPOutputStream(java.nio.file.Files.newOutputStream(path)))){for(var fill:AbandonedGeometry.build(p)){var b=fill.box();writer.println(b.minX()+","+b.minY()+","+b.minZ()+","+b.maxX()+","+b.maxY()+","+b.maxZ()+","+net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(fill.state().getBlock()));}}
    }
    static void assertRails(GameTestHelper t,AbandonedPlan p,java.util.function.Function<BlockPos,net.minecraft.world.level.block.state.BlockState> blocks){
        for(var h:p.chambers){
            boolean gallery=h.role()==AbandonedPlan.Role.GALLERY;if(!gallery&&h.module()!=AbandonedPlan.Module.MINECART_LOOP)continue;
            int rx=gallery?h.width()/4:3,rz=gallery?h.depth()/4:3;var loop=new HashSet<BlockPos>();
            for(int dx=-rx;dx<=rx;dx++){loop.add(p.world(new BlockPos(h.x()+dx,h.y()+1,h.z()-rz)));loop.add(p.world(new BlockPos(h.x()+dx,h.y()+1,h.z()+rz)));}
            for(int dz=-rz;dz<=rz;dz++){loop.add(p.world(new BlockPos(h.x()-rx,h.y()+1,h.z()+dz)));loop.add(p.world(new BlockPos(h.x()+rx,h.y()+1,h.z()+dz)));}
            for(var pos:loop){
                var state=blocks.apply(pos);t.assertTrue(state.is(Blocks.RAIL),"rail circuit interrupted "+h.id()+" "+pos);
                var below=blocks.apply(pos.below()).getCollisionShape(net.minecraft.world.level.EmptyBlockGetter.INSTANCE,pos.below());t.assertTrue(!below.isEmpty()&&below.max(Direction.Axis.Y)>=.99,"unsupported rail "+pos);
                var dirs=railDirections(state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.RAIL_SHAPE));
                for(var direction:dirs){var next=pos.relative(direction);t.assertTrue(loop.contains(next)&&railDirections(blocks.apply(next).getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.RAIL_SHAPE)).contains(direction.getOpposite()),"rail direction mismatch "+h.id()+" "+pos);}
            }
        }
    }
    static void assertWater(GameTestHelper t,List<ChudGeometry.Fill> fills,java.util.function.Function<BlockPos,net.minecraft.world.level.block.state.BlockState> blocks){
        var sources=new HashSet<BlockPos>();
        for(var fill:fills)if(fill.state().is(Blocks.WATER)){var b=fill.box();for(int x=b.minX();x<=b.maxX();x++)for(int z=b.minZ();z<=b.maxZ();z++)for(int y=b.minY();y<=b.maxY();y++)sources.add(new BlockPos(x,y,z));}
        int visible=0;
        for(var pos:sources)if(blocks.apply(pos).is(Blocks.WATER)){
            t.assertTrue(blocks.apply(pos.above()).isAir(),"basin hidden under a solid lid "+pos);visible++;
            for(var direction:List.of(Direction.NORTH,Direction.SOUTH,Direction.EAST,Direction.WEST,Direction.DOWN)){
                var next=pos.relative(direction);var state=blocks.apply(next);var collision=state.getCollisionShape(net.minecraft.world.level.EmptyBlockGetter.INSTANCE,next);
                t.assertTrue(state.is(Blocks.WATER)||!collision.isEmpty()&&collision.max(Direction.Axis.Y)>=.99,"uncontained basin "+pos+" "+direction);
            }
        }
        t.assertTrue(sources.isEmpty()||visible>0,"all functional basin sources were erased");
    }
    static void assertRailSupports(GameTestHelper t,List<ChudGeometry.Fill> fills,java.util.function.Function<BlockPos,net.minecraft.world.level.block.state.BlockState> blocks){
        var positions=new HashSet<BlockPos>();for(var fill:fills)if(fill.state().is(Blocks.RAIL)){var b=fill.box();for(int x=b.minX();x<=b.maxX();x++)for(int z=b.minZ();z<=b.maxZ();z++)positions.add(new BlockPos(x,b.minY(),z));}
        for(var pos:positions)if(blocks.apply(pos).is(Blocks.RAIL))t.assertTrue(blocks.apply(pos.below()).isFaceSturdy(net.minecraft.world.level.EmptyBlockGetter.INSTANCE,pos.below(),Direction.UP),"rail lacks a full upper support face "+pos);
    }
    private static List<Direction> railDirections(net.minecraft.world.level.block.state.properties.RailShape shape){return switch(shape){
        case NORTH_SOUTH,ASCENDING_NORTH,ASCENDING_SOUTH->List.of(Direction.NORTH,Direction.SOUTH);
        case EAST_WEST,ASCENDING_EAST,ASCENDING_WEST->List.of(Direction.EAST,Direction.WEST);
        case SOUTH_EAST->List.of(Direction.SOUTH,Direction.EAST);case SOUTH_WEST->List.of(Direction.SOUTH,Direction.WEST);
        case NORTH_EAST->List.of(Direction.NORTH,Direction.EAST);case NORTH_WEST->List.of(Direction.NORTH,Direction.WEST);
    };}
    @GameTest(template="empty",timeoutTicks=2400) public static void chunkOrdersAndMaze(GameTestHelper t){
        var networks=new HashSet<String>();
        for(int seed=0;seed<12;seed++)for(var kind:AbandonedPlan.Kind.values()){
            var p=new AbandonedPlan(seed,new BlockPos(0,64,0),seed%4,kind,60,null,seed%3,kind==AbandonedPlan.Kind.INDEPENDENT_GREAT?seed%3:0);
            if(!p.mazeNodes.isEmpty()){
                int[] degree=new int[p.mazeNodes.size()];int extras=0;
                for(var edge:p.mazeEdges){degree[edge.from()]++;degree[edge.to()]++;if(edge.extra())extras++;}
                long leaves=Arrays.stream(degree).filter(d->d==1).count();int tree=degree.length-1;
                t.assertTrue(extras*100>=tree*15&&extras*100<=tree*30,"maze extra edges "+seed+" "+kind+" "+extras);
                t.assertTrue(leaves*100>=degree.length*20&&leaves*100<=degree.length*35,"maze dead ends "+seed+" "+kind+" "+leaves);
                t.assertTrue(networks.add(kind+":"+p.mazeEdges),"repeated maze topology");
            }
            if(seed>=4)continue;
            var fills=AbandonedGeometry.build(p);var whole=new ChudGameTests.Grid(p.nativeBounds());ChudGeometry.rasterize(fills,p.nativeBounds(),whole::set);
            var chunks=new ArrayList<net.minecraft.world.level.ChunkPos>();var b=p.nativeBounds();
            for(int x=b.minX()>>4;x<=b.maxX()>>4;x++)for(int z=b.minZ()>>4;z<=b.maxZ()>>4;z++)chunks.add(new net.minecraft.world.level.ChunkPos(x,z));
            for(int order=0;order<3;order++){
                if(order==1)Collections.reverse(chunks);if(order==2)Collections.shuffle(chunks,new Random(seed));
                var split=new ChudGameTests.Grid(b);
                for(var chunk:chunks){var clip=new net.minecraft.world.level.levelgen.structure.BoundingBox(Math.max(b.minX(),chunk.getMinBlockX()),b.minY(),Math.max(b.minZ(),chunk.getMinBlockZ()),Math.min(b.maxX(),chunk.getMaxBlockX()),b.maxY(),Math.min(b.maxZ(),chunk.getMaxBlockZ()));ChudGeometry.rasterize(fills,clip,(pos,state)->{t.assertTrue(clip.isInside(pos),"chunk escaped");split.set(pos,state);});}
                t.assertTrue(Arrays.equals(whole.states,split.states),"chunk order changed "+kind+" "+seed+" "+order);
            }
        }
        t.succeed();
    }
    @GameTest(template="empty",timeoutTicks=200) public static void unionSpacing(GameTestHelper t){
        for(var left:AbandonedPlan.Kind.values())for(var right:AbandonedPlan.Kind.values())for(int rotation=0;rotation<4;rotation++){
            var a=new AbandonedPlan(1,new BlockPos(0,64,0),rotation,left,60,null,0);
            var b=new AbandonedPlan(2,new BlockPos(2576,-20,0),rotation,right,60,null,0);
            t.assertTrue(ChudPlan.boundaryDistanceSquared(a.envelope(),b.envelope())>=4_000_000,"independent unions under 2000");
        }
        var a=new net.minecraft.world.level.levelgen.structure.BoundingBox(0,0,0,278,131,278);
        t.assertTrue(ChudPlan.boundaryDistanceSquared(a,new net.minecraft.world.level.levelgen.structure.BoundingBox(2277,300,0,2555,431,278))<4_000_000,"1999 gap incorrectly accepted");
        t.assertTrue(ChudPlan.boundaryDistanceSquared(a,new net.minecraft.world.level.levelgen.structure.BoundingBox(2278,-300,0,2556,-169,278))==4_000_000,"2000 gap rejected because of Y");
        t.assertTrue(ChudPlan.boundaryDistanceSquared(a,new net.minecraft.world.level.levelgen.structure.BoundingBox(1478,300,1878,1756,431,2156))==4_000_000,"1200/1600 diagonal");
        t.succeed();
    }
}
