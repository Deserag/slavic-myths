package org.slavicmyths.village;

import com.google.gson.*;
import com.mojang.brigadier.arguments.StringArgumentType;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import net.minecraft.commands.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.*;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** Explicit operator-only QA placement; no terrain search, no tick handler or persistent tickets. */
@EventBusSubscriber(modid="slavicmyths")
public final class VillageShowcase {
    public record Entry(ResourceLocation id,String palette,int surface,BlockPos entrance,BlockPos spawn,String job,BlockPos workstation){}
    private record Planned(Entry entry,StructureTemplate template,BlockPos origin,StructurePlaceSettings settings,BoundingBox box){}
    private static final TicketType<BlockPos> TICKET=TicketType.create("slavicmyths_village_showcase",Comparator.comparingLong(BlockPos::asLong));
    public static List<Entry> catalog(ServerLevel level)throws IOException{
        var resource=level.getServer().getResourceManager().getResource(VillageRoles.id("village/catalog.json")).orElseThrow(()->new IOException("Missing slavicmyths:village/catalog.json"));
        try(var reader=new InputStreamReader(resource.open(),StandardCharsets.UTF_8)){
            var result=new ArrayList<Entry>();
            for(var element:JsonParser.parseReader(reader).getAsJsonObject().getAsJsonArray("structures")){
                var e=element.getAsJsonObject();result.add(new Entry(ResourceLocation.parse(e.get("id").getAsString()),e.get("palette").getAsString(),e.get("surface").getAsInt(),pos(e.getAsJsonArray("entrance")),pos(e.getAsJsonArray("spawn")),e.get("job").isJsonNull()?null:e.get("job").getAsString(),e.get("workstation").isJsonNull()?null:pos(e.getAsJsonArray("workstation"))));
            }
            return List.copyOf(result);
        }
    }
    private static BlockPos pos(JsonArray a){return new BlockPos(a.get(0).getAsInt(),a.get(1).getAsInt(),a.get(2).getAsInt());}
    @SubscribeEvent public static void register(RegisterCommandsEvent event){
        var show=Commands.literal("village_showcase").executes(c->showcase(c.getSource(),climate(c.getSource())));
        for(String palette:List.of("temperate","cold","warm"))show.then(Commands.literal(palette).executes(c->showcase(c.getSource(),palette)));
        var root=event.getDispatcher().register(Commands.literal("slavicmyths")
            .then(Commands.literal("test").requires(s->s.hasPermission(2)).then(show))
            .then(Commands.literal("place").requires(s->s.hasPermission(2)).then(Commands.literal("village")
                .then(Commands.argument("template",net.minecraft.commands.arguments.ResourceLocationArgument.id()).suggests((c,b)->{
                    try{for(var e:catalog(c.getSource().getLevel()))b.suggest(e.id().toString());}catch(IOException ignored){}return b.buildFuture();
                }).executes(c->individual(c.getSource(),net.minecraft.commands.arguments.ResourceLocationArgument.getId(c,"template").toString()))))));
        if(event.getDispatcher().getRoot().getChild("sm")==null)event.getDispatcher().register(Commands.literal("sm").redirect(event.getDispatcher().getRoot().getChild("slavicmyths")));
    }
    private static String climate(CommandSourceStack s){var b=s.getLevel().getBiome(BlockPos.containing(s.getPosition())).value();return b.getBaseTemperature()<.25F?"cold":b.getBaseTemperature()>1F?"warm":"temperate";}
    private static Planned plan(ServerLevel level,Entry e,int x,int z,int ground,Rotation rotation)throws IOException{
        var template=level.getStructureManager().get(e.id()).orElseThrow(()->new IOException("Missing template "+e.id()));
        var size=template.getSize();if(size.getX()<1||size.getY()<1||size.getZ()<1)throw new IOException("Empty template "+e.id());
        var origin=template.getZeroPositionWithTransform(new BlockPos(x,ground-e.surface(),z),Mirror.NONE,rotation);
        var settings=new StructurePlaceSettings().setRotation(rotation).setIgnoreEntities(true).setKnownShape(true).addProcessor(VillageFoundation.INSTANCE).addProcessor(JigsawReplacementProcessor.INSTANCE);
        var box=template.getBoundingBox(settings,origin);
        if(box.minY()<level.getMinBuildHeight()||box.maxY()>=level.getMaxBuildHeight())throw new IOException(e.id()+" palette="+e.palette()+" box="+box+" outside world Y ["+level.getMinBuildHeight()+","+(level.getMaxBuildHeight()-1)+"]");
        if(!level.getWorldBorder().isWithinBounds(new BlockPos(box.minX(),ground,box.minZ()))||!level.getWorldBorder().isWithinBounds(new BlockPos(box.maxX(),ground,box.maxZ())))throw new IOException(e.id()+" outside world border: "+box);
        return new Planned(e,template,origin,settings,box);
    }
    public static int layoutSlot(int index){return index==33?21:index==21?33:index;}
    public static int showcase(CommandSourceStack source,String palette){
        var level=source.getLevel();var player=BlockPos.containing(source.getPosition());int x=player.getX()+8,z=player.getZ()+8;
        level.getChunk(x>>4,z>>4);
        int ground=level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,x,z)-1;
        List<Planned> plans=new ArrayList<>();
        try{
            var entries=catalog(level).stream().filter(e->e.palette().equals(palette)).toList();
            if(entries.size()!=34)throw new IOException("Incomplete catalog for "+palette+": "+entries.size()+"/34");
            for(int i=0;i<entries.size();i++){int slot=layoutSlot(i);plans.add(plan(level,entries.get(i),x+(slot%6)*28,z+(slot/6)*28,ground,Rotation.NONE));}
            for(int i=0;i<plans.size();i++)for(int j=i+1;j<plans.size();j++)if(plans.get(i).box().intersects(plans.get(j).box()))throw new IOException("Overlapping templates "+plans.get(i).entry().id()+" / "+plans.get(j).entry().id());
        }catch(Exception ex){source.sendFailure(Component.literal("Village showcase prevalidation: "+ex.getMessage()));return 0;}
        return execute(source,plans,new BlockPos(x,ground,z),true);
    }
    private static int individual(CommandSourceStack source,String id){
        try{
            var e=catalog(source.getLevel()).stream().filter(a->a.id().toString().equals(id)).findFirst().orElseThrow(()->new IOException("Unknown village template "+id));
            var p=BlockPos.containing(source.getPosition()).offset(4,0,4);source.getLevel().getChunk(p.getX()>>4,p.getZ()>>4);int y=source.getLevel().getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,p.getX(),p.getZ())-1;
            return execute(source,List.of(plan(source.getLevel(),e,p.getX(),p.getZ(),y,Rotation.NONE)),new BlockPos(p.getX(),y,p.getZ()),false);
        }catch(Exception ex){source.sendFailure(Component.literal("Village placement: "+ex.getMessage()));return 0;}
    }
    private static int execute(CommandSourceStack source,List<Planned> plans,BlockPos anchor,boolean roads){
        var level=source.getLevel();Set<ChunkPos> chunks=new LinkedHashSet<>();int reach=roads?168:28;
        for(int cx=(anchor.getX()-4)>>4;cx<=(anchor.getX()+reach)>>4;cx++)for(int cz=(anchor.getZ()-4)>>4;cz<=(anchor.getZ()+reach)>>4;cz++)chunks.add(new ChunkPos(cx,cz));
        int placed=0,spawned=0,animals=0;List<String>warnings=new ArrayList<>();
        try{
            for(var chunk:chunks){level.getChunkSource().addRegionTicket(TICKET,chunk,2,anchor);level.getChunk(chunk.x,chunk.z);}
            // Only template bounding volumes are cleared. Bedrock is preserved at every height.
            for(var p:plans){
                for(var q:BlockPos.betweenClosed(p.box().minX(),anchor.getY()+1,p.box().minZ(),p.box().maxX(),p.box().maxY(),p.box().maxZ())){
                    if(!level.getBlockState(q).is(Blocks.BEDROCK))level.setBlock(q,Blocks.AIR.defaultBlockState(),2);
                }
                if(!p.template().placeInWorld(level,p.origin(),p.origin(),p.settings(),RandomSource.create(124),2))throw new IOException("Placement failed id="+p.entry().id()+" palette="+p.entry().palette()+" box="+p.box()+" biome="+level.getBiome(p.origin()).unwrapKey());
                placed++;
            }
            if(roads){
                for(int row=0;row<6;row++)for(int a=-3;a<168;a++)for(int w=-3;w<=-1;w++)road(level,anchor.offset(a,0,row*28+w));
                for(int col=0;col<7;col++)for(int a=-3;a<168;a++)for(int w=-3;w<=-1;w++)road(level,anchor.offset(col*28+w,0,a));
                for(var p:plans){var entrance=p.origin().offset(p.entry().entrance());for(int a=-3;a<=0;a++)road(level,new BlockPos(entrance.getX(),anchor.getY(),p.box().minZ()+a));
                    if(p.entry().id().getPath().contains("/center/")){
                        int midX=(p.box().minX()+p.box().maxX())/2,midZ=(p.box().minZ()+p.box().maxZ())/2;
                        for(int a=1;a<=9;a++)for(int w=-1;w<=1;w++){
                            road(level,new BlockPos(p.box().minX()-a,anchor.getY(),midZ+w));road(level,new BlockPos(p.box().maxX()+a,anchor.getY(),midZ+w));
                            road(level,new BlockPos(midX+w,anchor.getY(),p.box().minZ()-a));road(level,new BlockPos(midX+w,anchor.getY(),p.box().maxZ()+a));
                        }
                    }
                }
            }
            for(var p:plans){
                // Keep authored livestock even though showcase suppresses natural resident spawns.
                var data=p.template().save(new net.minecraft.nbt.CompoundTag());
                for(var raw:data.getList("entities",10)){
                    var row=(net.minecraft.nbt.CompoundTag)raw;var tag=row.getCompound("nbt");String id=tag.getString("id");
                    if(id.equals("minecraft:villager")||id.equals("minecraft:zombie_villager"))continue;
                    var position=row.getList("pos",6);
                    var entity=EntityType.loadEntityRecursive(tag,level,e->{e.moveTo(p.origin().getX()+position.getDouble(0),p.origin().getY()+position.getDouble(1),p.origin().getZ()+position.getDouble(2),0,0);return e;});
                    if(entity!=null&&level.addFreshEntity(entity))animals++;
                }
                if(p.entry().job()==null)continue;
                String job=p.entry().job();ResourceLocation key=ResourceLocation.parse(job.contains(":")?job:"minecraft:"+job);
                var profession=BuiltInRegistries.VILLAGER_PROFESSION.get(key);
                if(profession==null){warnings.add("Missing profession "+key);continue;}
                int count=job.endsWith("druzhinnik")?3:1;
                for(int i=0;i<count;i++){
                    var v=EntityType.VILLAGER.create(level);if(v==null)continue;var pos=p.origin().offset(p.entry().spawn()).offset(0,0,i);
                    v.moveTo(pos.getX()+.5,pos.getY(),pos.getZ()+.5,180,0);v.setVillagerData(v.getVillagerData().setProfession(profession));v.refreshBrain(level);
                    // Profession assigned for visual QA; native AI must acquire its own POI, no JOB_SITE shortcut.
                    if(level.addFreshEntity(v))spawned++;
                }
            }
            int finalPlaced=placed,finalSpawned=spawned,finalAnimals=animals;
            int minY=plans.stream().mapToInt(p->p.box().minY()).min().orElse(anchor.getY()),maxY=plans.stream().mapToInt(p->p.box().maxY()).max().orElse(anchor.getY());
            source.sendSuccess(()->Component.literal("Village showcase placed: "+finalPlaced+"/"+plans.size()+" structures, "+finalSpawned+" profession villagers, "+finalAnimals+" livestock, origin "+anchor.toShortString()+", palette="+plans.getFirst().entry().palette()+", region="+new BlockPos(anchor.getX()-3,minY,anchor.getZ()-3).toShortString()+" .. "+new BlockPos(anchor.getX()+reach,maxY,anchor.getZ()+reach).toShortString()+", warnings="+warnings),true);
            return placed;
        }catch(Exception ex){source.sendFailure(Component.literal("Village placement stopped after "+placed+"/"+plans.size()+": "+ex.getMessage()));return 0;}
        finally{for(var chunk:chunks)level.getChunkSource().removeRegionTicket(TICKET,chunk,2,anchor);}
    }
    private static void road(ServerLevel level,BlockPos p){
        for(int i=1;i<=3;i++)if(!level.getBlockState(p.above(i)).is(Blocks.BEDROCK))level.setBlock(p.above(i),Blocks.AIR.defaultBlockState(),2);
        if(!level.getBlockState(p).is(Blocks.BEDROCK))level.setBlock(p,Blocks.GRAVEL.defaultBlockState(),2);
        for(int i=1;i<=8;i++){var q=p.below(i);if(q.getY()<level.getMinBuildHeight()||!level.getBlockState(q).canBeReplaced())break;level.setBlock(q,Blocks.COBBLESTONE.defaultBlockState(),2);}
    }
    private VillageShowcase(){}
}
