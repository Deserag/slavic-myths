package org.slavicmyths.winter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.*;
import com.google.gson.*;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.*;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.*;
import net.minecraft.server.level.*;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.storage.loot.LootTable;
import org.slavicmyths.commands.CommandTree;
import org.slavicmyths.icequeen.*;
import org.slavicmyths.worldgen.*;
import static org.slavicmyths.winter.WinterEncounterLayout.*;

/** Approved V2 blueprint. Uses the existing encounter grid and bounded site preparation. */
public final class WinterPalace {
    static final int X0=-32,X1=32,Z0=0,Z1=49,Y1=62,W=65,D=50,H=63;
    private static final net.minecraft.tags.TagKey<net.minecraft.world.level.biome.Biome> FOREST =
        net.minecraft.tags.TagKey.create(Registries.BIOME,ResourceLocation.fromNamespaceAndPath("slavicmyths","winter_forest"));
    private static final Set<String> NATURAL_WINTER = Set.of("frozen_stone","frozen_cobblestone","frozen_granite",
        "frozen_diorite","frozen_andesite","frozen_deepslate","frozen_cobbled_deepslate",
        "ice_pine_log","ice_pine_wood","ice_pine_leaves","ice_pine_sapling",
        "frost_willow_log","frost_willow_wood","frost_willow_leaves","frost_willow_sapling");
    private static final BlockPos QUEEN=new BlockPos(0,25,37);
    private static final Map<ServerLevel,Job> JOBS=new IdentityHashMap<>();
    private static final Map<ServerLevel,Set<Long>> REJECTED=new WeakHashMap<>();
    private static final Map<ServerLevel,Integer> LOOKAHEAD=new WeakHashMap<>();
    private static final int AHEAD_DISTANCE=2048, MIN_APPROACH=320, AHEAD_CELLS=9, AHEAD_SAMPLES=81;
    private static final Set<ServerLevel> FAILED_PENDING=Collections.newSetFromMap(new WeakHashMap<>());
    private record Loot(BlockPos pos,String kind){}
    private record Stand(BlockPos pos,float yaw){}
    private static final class Plan {
        static final byte[] DATA;
        static final BlockState[] PALETTE;
        static final List<Loot> LOOT;
        static final List<Stand> STANDS;
        static final List<BlockPos> COLUMNS;
        static {
            try(var in=WinterPalace.class.getResourceAsStream("/data/slavicmyths/winter/palace_v2.txt")){
                if(in==null)throw new IOException("Missing palace blueprint");
                var text=new String(in.readAllBytes(),StandardCharsets.UTF_8);
                int split=text.indexOf('\n');
                var header=JsonParser.parseString(text.substring(0,split)).getAsJsonObject();
                var compressed=Base64.getMimeDecoder().decode(text.substring(split+1));
                try(var stream=new InflaterInputStream(new ByteArrayInputStream(compressed))){DATA=stream.readNBytes(W*D*H+1);}
                var states=header.getAsJsonArray("states");
                PALETTE=new BlockState[states.size()+1];PALETTE[0]=Blocks.AIR.defaultBlockState();
                for(int i=0;i<states.size();i++){
                    var row=states.get(i).getAsJsonArray();
                    var id=ResourceLocation.parse(row.get(0).getAsString());
                    var block=BuiltInRegistries.BLOCK.getOptional(id).orElseThrow(()->new IllegalStateException("Missing block "+id));
                    var state=block.defaultBlockState();
                    for(var prop:row.get(1).getAsJsonObject().entrySet()){
                        var property=block.getStateDefinition().getProperty(prop.getKey());
                        if(property==null)throw new IllegalStateException("Unknown property "+prop.getKey()+" on "+id);
                        state=with(state,property,prop.getValue().getAsString());
                    }
                    PALETTE[i+1]=state;
                }
                var crc=new CRC32();crc.update(DATA);int count=0;
                for(byte value:DATA){int n=Byte.toUnsignedInt(value);if(n>=PALETTE.length)throw new IOException("Invalid palace material");if(n!=0)count++;}
                if(DATA.length!=W*D*H||count!=39380||crc.getValue()!=2830284266L)throw new IOException("Palace V2 checksum/count mismatch");
                var loot=new ArrayList<Loot>();
                for(var element:header.getAsJsonArray("loot")){
                    var row=element.getAsJsonArray();loot.add(new Loot(pos(row),row.get(3).getAsString()));
                }
                var stands=new ArrayList<Stand>();
                for(var element:header.getAsJsonArray("entities")){
                    var row=element.getAsJsonArray();stands.add(new Stand(pos(row),row.get(4).getAsJsonObject().get("yaw").getAsFloat()));
                }
                LOOT=List.copyOf(loot);STANDS=List.copyOf(stands);
                if(LOOT.size()!=32||STANDS.size()!=7)throw new IOException("Palace furnishing count mismatch");
                var cols=new ArrayList<BlockPos>();
                for(int z=Z0;z<=Z1;z++)for(int x=X0;x<=X1;x++)
                    for(int y=0;y<=Y1;y++)if(raw(x,y,z)!=0){cols.add(new BlockPos(x,y,z));break;}
                COLUMNS=List.copyOf(cols);
            }catch(IOException e){throw new IllegalStateException("Cannot load palace V2",e);}
        }
        private static BlockPos pos(JsonArray row){return new BlockPos(row.get(0).getAsInt(),row.get(1).getAsInt(),row.get(2).getAsInt());}
        private static <T extends Comparable<T>> BlockState with(BlockState state,Property<T> property,String value){
            return state.setValue(property,property.getValue(value).orElseThrow(()->new IllegalStateException("Bad property "+property.getName()+": "+value)));
        }
        static int raw(int x,int y,int z){
            if(x<X0||x>X1||y<0||y>Y1||z<Z0||z>Z1)return 0;
            return Byte.toUnsignedInt(DATA[(y*D+z)*W+x-X0]);
        }
        static BlockState at(int x,int y,int z){return PALETTE[raw(x,y,z)];}
        static BlockState block(int x,int y,int z){
            var state=at(x,y,z);
            // Recessed lighting on the approved floor plane; no change to the footprint,
            // stairs, carpets, chests, bed supports or other furnishings.
            if((y==4||y==14||y==24)&&Math.floorMod(x,5)==0&&Math.floorMod(z,5)==0
                &&(state.is(Blocks.QUARTZ_BLOCK)||state.is(Blocks.BLUE_TERRACOTTA)||state.is(Blocks.LIGHT_BLUE_GLAZED_TERRACOTTA))
                &&at(x,y+1,z).isAir()&&at(x,y+2,z).isAir())return Blocks.SEA_LANTERN.defaultBlockState();
            if(state.is(Blocks.QUARTZ_STAIRS))state=state.setValue(BlockStateProperties.HORIZONTAL_FACING,Direction.SOUTH);
            if(state.is(Blocks.DARK_OAK_FENCE)){
                for(var dir:Direction.Plane.HORIZONTAL){
                    boolean c=at(x+dir.getStepX(),y,z+dir.getStepZ()).is(Blocks.DARK_OAK_FENCE);
                    state=switch(dir){
                        case NORTH->state.setValue(BlockStateProperties.NORTH,c);
                        case SOUTH->state.setValue(BlockStateProperties.SOUTH,c);
                        case EAST->state.setValue(BlockStateProperties.EAST,c);
                        case WEST->state.setValue(BlockStateProperties.WEST,c);
                        default->state;
                    };
                }
            }
            return state;
        }
    }
    private static UUID id(BlockPos p,String suffix){return UUID.nameUUIDFromBytes(("slavicmyths:palace:"+p.asLong()+":"+suffix).getBytes(StandardCharsets.UTF_8));}
    private static BlockPos centerOf(BlockPos p){return p.offset(0,0,29);}
    private static long siteKey(BlockPos p){var c=centerOf(p);return key(cell(c.getX()),cell(c.getZ()));}
    private static BoundingBox bounds(ServerLevel w,BlockPos p){
        return new BoundingBox(p.getX()+X0,w.getMinBuildHeight(),p.getZ()+Z0,p.getX()+X1,w.getMaxBuildHeight()-1,p.getZ()+Z1);
    }
    private static boolean border(ServerLevel w,BlockPos p){
        return p.getY()>w.getMinBuildHeight()+1&&p.getY()+Y1<w.getMaxBuildHeight()
            &&w.getWorldBorder().isWithinBounds(p.offset(X0,0,Z0))&&w.getWorldBorder().isWithinBounds(p.offset(X1,0,Z1));
    }
    private static boolean playerInside(ServerLevel w,BlockPos p){
        for(var player:w.players())if(!player.isSpectator()
            &&player.getX()>=p.getX()+X0-4&&player.getX()<=p.getX()+X1+5
            &&player.getZ()>=p.getZ()+Z0-4&&player.getZ()<=p.getZ()+Z1+5)return true;
        return false;
    }
    static boolean active(ServerLevel w){return JOBS.containsKey(w);}
    private static boolean rejected(ServerLevel w,BlockPos p){
        return REJECTED.computeIfAbsent(w,k->new HashSet<>()).contains(new BlockPos(p.getX(),0,p.getZ()).asLong());
    }
    private static boolean queueAhead(ServerLevel w,WinterEncounterSpawns.Sites sites){
        if(!(w.getChunkSource().getGenerator() instanceof net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator)
            ||w.getChunkSource().getGenerator().getBiomeSource().possibleBiomes().stream().noneMatch(b->b.is(FOREST)))return false;
        var generator=w.getChunkSource().getGenerator();var sampler=w.getChunkSource().randomState().sampler();
        boolean test=WinterWorld.isTest(w);
        boolean gluheya=BuiltInRegistries.ENTITY_TYPE.getOptional(ResourceLocation.fromNamespaceAndPath("slavicmyths","gluheya")).isPresent();
        int total=AHEAD_CELLS*AHEAD_CELLS*AHEAD_SAMPLES,cursor=LOOKAHEAD.getOrDefault(w,0);
        long deadline=System.nanoTime()+2_000_000L;
        for(int attempt=0;attempt<128&&System.nanoTime()<deadline;attempt++){
            int index=cursor;cursor=(cursor+1)%total;
            int ci=index%(AHEAD_CELLS*AHEAD_CELLS),sample=index/(AHEAD_CELLS*AHEAD_CELLS);
            int dx=ci%AHEAD_CELLS-AHEAD_CELLS/2,dz=ci/AHEAD_CELLS-AHEAD_CELLS/2;
            int ox=sample==0?0:((sample-1)%9-4)*16,oz=sample==0?0:((sample-1)/9-4)*16;
            for(var player:w.players()){
                if(player.isSpectator())continue;
                int cx=cell(player.getBlockX())+dx,cz=cell(player.getBlockZ())+dz;
                var selected=select(Habitat.FOREST,cx,cz,test,gluheya,w.getSeed());
                if(sites.occupied.contains(key(cx,cz))||(selected!=Boss.QUEEN&&selected!=Boss.GLUHEYA))continue;
                var c=new BlockPos(center(cx)+ox,w.getSeaLevel(),center(cz)+oz);
                double dist=Math.max(Math.abs(player.getX()-c.getX()),Math.abs(player.getZ()-c.getZ()));
                if(dist<MIN_APPROACH||dist>AHEAD_DISTANCE)continue;
                boolean near=false;
                for(var other:w.players())if(!other.isSpectator()&&Math.max(Math.abs(other.getX()-c.getX()),Math.abs(other.getZ()-c.getZ()))<MIN_APPROACH){near=true;break;}
                if(near)continue;
                if(!generator.getBiomeSource().getNoiseBiome(c.getX()>>2,c.getY()>>2,c.getZ()>>2,sampler).is(FOREST))continue;
                if(selected==Boss.GLUHEYA?queueManor(w,c,true,null,true):queueNatural(w,c)){LOOKAHEAD.put(w,cursor);return true;}
            }
        }
        LOOKAHEAD.put(w,cursor);return false;
    }
    static boolean queueNatural(ServerLevel w,BlockPos center){
        var sites=WinterEncounterSpawns.Sites.get(w);
        int y=Math.clamp(center.getY(),w.getMinBuildHeight()+2,w.getMaxBuildHeight()-H);
        var p=new BlockPos(center.getX(),y,center.getZ()-29);
        if(active(w)||FrozenShip.active(w)||sites.pendingShip!=null||sites.pendingPalace!=null||sites.occupied.contains(siteKey(p))
            ||rejected(w,p)
            ||!border(w,p)||!FrozenShip.clearOfShips(sites,center)||playerInside(w,p))return false;
        JOBS.put(w,new Job(w,p,true,false,null,true));return true;
    }
    private static boolean manorBorder(ServerLevel w,BlockPos p){
        return p.getY()>w.getMinBuildHeight()+1&&p.getY()+GluheyaManorPlan.Y1<w.getMaxBuildHeight()
            &&w.getWorldBorder().isWithinBounds(p.offset(-19,0,-4))&&w.getWorldBorder().isWithinBounds(p.offset(19,0,26));
    }
    private static boolean queueManor(ServerLevel w,BlockPos center,boolean natural,CommandSourceStack source,boolean autoY){
        var sites=WinterEncounterSpawns.Sites.get(w);
        var p=center.offset(0,0,-13);
        if(autoY)p=p.atY(Math.clamp(p.getY(),w.getMinBuildHeight()+2,w.getMaxBuildHeight()-GluheyaManorPlan.H));
        if(active(w)||FrozenShip.active(w)||sites.pendingShip!=null||sites.pendingPalace!=null||!manorBorder(w,p))return false;
        if(natural&&(sites.occupied.contains(manorKey(p))||rejected(w,p)||w.players().stream().anyMatch(player->!player.isSpectator()&&Math.abs(player.getX()-center.getX())<24&&Math.abs(player.getZ()-center.getZ())<24)||!FrozenShip.clearOfShips(sites,center)))return false;
        JOBS.put(w,new Job(w,p,natural,false,source,autoY,true));return true;
    }
    private static long manorKey(BlockPos p){return key(cell(p.getX()),cell(p.getZ()+13));}
    private static int manualManor(CommandSourceStack s,BlockPos explicit){
        var w=s.getLevel();
        if(!w.dimension().equals(Level.OVERWORLD)){s.sendFailure(Component.literal("Поместье доступно в обычном измерении."));return 0;}
        var center=explicit==null?BlockPos.containing(s.getPosition()).offset(0,0,48):explicit.offset(0,0,13);
        if(!queueManor(w,center,false,s,explicit==null)){s.sendFailure(Component.literal("Нельзя начать строительство: граница мира, высота или другая активная стройка."));return 0;}
        s.sendSuccess(()->Component.literal("Строительство поместья Глухеи V1. Освободите площадку."),false);return 1;
    }
    private static int locateManor(CommandSourceStack s){
        var w=s.getLevel();var here=BlockPos.containing(s.getPosition());
        var sites=WinterEncounterSpawns.Sites.get(w);
        var best=ManualStructureRecords.get(w).entries().stream().filter(e->e.family().equals("site/gluheya_manor"))
            .min(Comparator.comparingDouble(e->here.distSqr(e.arrival())));
        if(best.isPresent()){reportLocation(s,best.get().arrival(),"Поместье Глухеи");return 1;}
        if(sites.pendingPalaceManor&&sites.pendingPalace!=null){
            s.sendSuccess(()->Component.literal("Поместье строится: "+sites.pendingPalace.toShortString()),false);return 1;
        }
        if(!w.dimension().equals(Level.OVERWORLD)||active(w)||FrozenShip.active(w)||sites.pendingShip!=null||sites.pendingPalace!=null){
            s.sendFailure(Component.literal("Поиск недоступен, либо зимняя постройка уже строится."));return 0;
        }
        var generator=w.getChunkSource().getGenerator();
        var sampler=w.getChunkSource().randomState().sampler();
        boolean test=WinterWorld.isTest(w);
        int cx=cell(here.getX()),cz=cell(here.getZ());
        for(int ring=0;ring<=8;ring++)for(int dz=-ring;dz<=ring;dz++)for(int dx=-ring;dx<=ring;dx++){
            if(Math.max(Math.abs(dx),Math.abs(dz))!=ring)continue;
            int x=cx+dx,z=cz+dz;
            if(sites.occupied.contains(key(x,z))||select(Habitat.FOREST,x,z,test,true,w.getSeed())!=Boss.GLUHEYA)continue;
            for(int oz=-SEARCH;oz<=SEARCH;oz+=32)for(int ox=-SEARCH;ox<=SEARCH;ox+=32){
                var c=new BlockPos(center(x)+ox,w.getSeaLevel(),center(z)+oz);
                if(!generator.getBiomeSource().getNoiseBiome(c.getX()>>2,c.getY()>>2,c.getZ()>>2,sampler).is(FOREST))continue;
                if(queueManor(w,c,true,s,true)){
                    s.sendSuccess(()->Component.literal("Найдена ячейка поместья: "+c.toShortString()+". Дождитесь завершения строительства."),false);return 1;
                }
            }
        }
        s.sendFailure(Component.literal("Свободная зимняя лесная ячейка поместья не найдена."));return 0;
    }
    public static void register(CommandTree tree){
        tree.add(Commands.literal("locate").requires(s->s.hasPermission(2)).then(Commands.literal("structure")
            .then(Commands.literal("slavicmyths:gluheya_manor").executes(c->locateManor(c.getSource())))));
        tree.add(Commands.literal("slavicmyths").then(Commands.literal("dev").then(Commands.literal("generate")
            .then(Commands.literal("gluheya_manor").requires(s->s.hasPermission(2)).executes(c->manualManor(c.getSource(),null))
                .then(Commands.argument("pos",BlockPosArgument.blockPos()).executes(c->manualManor(c.getSource(),BlockPosArgument.getBlockPos(c,"pos"))))))));
        tree.add(Commands.literal("locate").requires(s->s.hasPermission(2)).then(Commands.literal("structure")
            .then(Commands.literal("slavicmyths:winter_palace").executes(c->locate(c.getSource(),true)))
            .then(Commands.literal("slavicmyths:frozen_ship").executes(c->locate(c.getSource(),false)))));
        tree.add(Commands.literal("slavicmyths").then(Commands.literal("dev")
            .then(Commands.literal("generate").then(Commands.literal("winter_palace")
                .requires(s->s.hasPermission(2)).executes(c->manual(c.getSource(),null))
                .then(Commands.argument("pos",BlockPosArgument.blockPos()).executes(c->manual(c.getSource(),BlockPosArgument.getBlockPos(c,"pos"))))))));
    }
    /** Locate existing sites first. If no palace exists, select a deterministic queen cell
     * from biome noise, then use the same bounded construction job as natural encounters. */
    private static int locate(CommandSourceStack s,boolean palace){
        var w=s.getLevel();
        if(!w.dimension().equals(Level.OVERWORLD)){s.sendFailure(Component.literal("Поиск доступен в обычном мире."));return 0;}
        var sites=WinterEncounterSpawns.Sites.get(w);
        var here=BlockPos.containing(s.getPosition());
        BlockPos best=null;double distance=Double.MAX_VALUE;
        for(var entry:ManualStructureRecords.get(w).entries()){
            if(!entry.family().equals(palace?"site/winter_palace":"site/frozen_ship"))continue;
            var p=entry.arrival();double d=here.distSqr(p);
            if(d<distance){best=p;distance=d;}
        }
        if(best!=null){reportLocation(s,best,palace?"Дворец вечной зимы":"Ледяной корабль");return 1;}
        var pending=palace?(sites.pendingPalaceManor?null:sites.pendingPalace):sites.pendingShip;
        if(pending!=null){reportLocation(s,pending.offset(0,1,palace?0:-23),"Постройка ещё строится, не телепортируйтесь до завершения");return 1;}
        if(!palace){s.sendFailure(Component.literal("Готовый корабль пока не найден. Существующий генератор продолжает поиск."));return 0;}
        if(active(w)||FrozenShip.active(w)||sites.pendingShip!=null||sites.pendingPalace!=null){
            s.sendFailure(Component.literal("Зимняя постройка уже строится. Повторите поиск после завершения."));return 0;
        }
        if(!(w.getChunkSource().getGenerator() instanceof net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator)){
            s.sendFailure(Component.literal("В этом типе мира автоматический поиск зимних ячеек недоступен."));return 0;
        }
        var generator=w.getChunkSource().getGenerator();
        var sampler=w.getChunkSource().randomState().sampler();
        boolean test=WinterWorld.isTest(w);
        boolean gluheya=BuiltInRegistries.ENTITY_TYPE.getOptional(ResourceLocation.fromNamespaceAndPath("slavicmyths","gluheya")).isPresent();
        int cx=cell(here.getX()),cz=cell(here.getZ());
        for(int ring=0;ring<=8;ring++){
            for(int dz=-ring;dz<=ring;dz++)for(int dx=-ring;dx<=ring;dx++){
                if(Math.max(Math.abs(dx),Math.abs(dz))!=ring)continue;
                int x=cx+dx,z=cz+dz;
                if(sites.occupied.contains(key(x,z))||select(Habitat.FOREST,x,z,test,gluheya,w.getSeed())!=Boss.QUEEN)continue;
                for(int oz=-SEARCH;oz<=SEARCH;oz+=32)for(int ox=-SEARCH;ox<=SEARCH;ox+=32){
                    var c=new BlockPos(center(x)+ox,w.getSeaLevel(),center(z)+oz);
                    var p=c.offset(0,0,-29);
                    if(!border(w,p)||playerInside(w,p)||!FrozenShip.clearOfShips(sites,c)||
                        !ManualStructureRecords.get(w).overlapping(bounds(w,p)).isEmpty())continue;
                    if(rejected(w,p))continue;
                    if(!generator.getBiomeSource().getNoiseBiome(c.getX()>>2,c.getY()>>2,c.getZ()>>2,sampler).is(FOREST))continue;
                    JOBS.put(w,new Job(w,p,true,false,s,true));
                    s.sendSuccess(()->Component.literal("Найдена зимняя ячейка: X="+c.getX()+", Z="+c.getZ()+". Дворец будет построен; точные координаты входа появятся после завершения."),false);
                    return 1;
                }
            }
        }
        s.sendFailure(Component.literal("Подходящая свободная ячейка дворца не найдена в радиусе 8 ячеек (4096 блоков от текущей ячейки)."));return 0;
    }
    private static void reportLocation(CommandSourceStack s,BlockPos p,String title){
        s.sendSuccess(()->Component.literal(title+": "+p.toShortString()+" | /tp @s "+p.getX()+" "+p.getY()+" "+p.getZ()),false);
    }
    private static int manual(CommandSourceStack s,BlockPos explicit){
        var w=s.getLevel();var sites=WinterEncounterSpawns.Sites.get(w);
        if(!w.dimension().equals(Level.OVERWORLD)||active(w)||FrozenShip.active(w)||sites.pendingPalace!=null||sites.pendingShip!=null){
            s.sendFailure(Component.literal("Дворец недоступен в этом измерении или зимняя постройка уже строится."));return 0;
        }
        var here=BlockPos.containing(s.getPosition());
        var c=explicit;
        if(c==null){
            // Free nearby encounter cell, independent of view distance; the job surveys height after loading.
            int cx=cell(here.getX()),cz=cell(here.getZ());
            double nearest=Double.MAX_VALUE;
            for(int dz=-2;dz<=2;dz++)for(int dx=-2;dx<=2;dx++){
                int x=cx+dx,z=cz+dz;
                if(sites.occupied.contains(key(x,z)))continue;
                var candidate=new BlockPos(center(x),Math.clamp(w.getSeaLevel(),w.getMinBuildHeight()+2,w.getMaxBuildHeight()-H),center(z));
                var anchor=candidate.offset(0,0,-29);
                if(!border(w,anchor)||playerInside(w,anchor)||!FrozenShip.clearOfShips(sites,candidate)
                    ||!ManualStructureRecords.get(w).overlapping(bounds(w,anchor)).isEmpty())continue;
                double d=here.distSqr(candidate);if(d<nearest){nearest=d;c=candidate;}
            }
            if(c==null){s.sendFailure(Component.literal("Нет свободной зимней ячейки поблизости. Укажите координаты."));return 0;}
        }
        var p=c.offset(0,0,-29);
        if(!border(w,p)||playerInside(w,p)||sites.occupied.contains(siteKey(p))||!FrozenShip.clearOfShips(sites,c)){
            s.sendFailure(Component.literal("Недостаточно места или зимняя встреча уже существует в этой ячейке."));return 0;
        }
        JOBS.put(w,new Job(w,p,false,false,s,explicit==null));s.sendSuccess(()->Component.literal("Строительство дворца V2: "+p.toShortString()),false);return 1;
    }
    static void tick(ServerLevel w){
        var sites=WinterEncounterSpawns.Sites.get(w);
        if(!active(w)&&sites.pendingPalace!=null&&!FAILED_PENDING.contains(w)&&!FrozenShip.active(w)&&sites.pendingShip==null)
            JOBS.put(w,new Job(w,sites.pendingPalace,sites.pendingPalaceNatural,true,null,false,sites.pendingPalaceManor));
        var job=JOBS.get(w);if(job!=null){job.step();return;}
        if(w.getGameTime()%20==0&&w.getDifficulty()!=Difficulty.PEACEFUL){spawnWaiting(w,sites);spawnGluheya(w,sites);}
        if(w.getGameTime()%100==0)repairLighting(w,sites);
        if(w.getDifficulty()!=Difficulty.PEACEFUL&&!FrozenShip.active(w)&&sites.pendingShip==null&&sites.pendingPalace==null
            &&w.getGameTime()%5==0)queueAhead(w,sites);
    }
    /** Upgrade completed V2 palaces without rebuilding, resetting loot or respawning the queen.
     * Only touch loaded chunks, and only replace the original blueprint's unchanged floor. */
    private static void repairLighting(ServerLevel w,WinterEncounterSpawns.Sites sites){
        for(long packed:sites.palaces){
            if(sites.litPalaces.contains(packed))continue;
            var origin=BlockPos.of(packed);
            boolean allLoaded=true;
            for(int y:new int[]{4,14,24})for(int x=-30;x<=30;x+=5)for(int z=0;z<=45;z+=5){
                var old=Plan.at(x,y,z);var next=Plan.block(x,y,z);
                if(old.equals(next)||!next.is(Blocks.SEA_LANTERN))continue;
                var pos=origin.offset(x,y,z);
                if(w.getChunkSource().getChunkNow(pos.getX()>>4,pos.getZ()>>4)==null){allLoaded=false;continue;}
                if(w.getBlockState(pos).equals(old)&&w.getBlockState(pos.above()).isAir()
                    &&w.getBlockState(pos.above(2)).isAir())w.setBlock(pos,next,3);
            }
            if(allLoaded){sites.litPalaces.add(packed);sites.setDirty();}
            return;
        }
    }
    private static void spawnWaiting(ServerLevel w,WinterEncounterSpawns.Sites sites){
        if(w.getDifficulty()==Difficulty.PEACEFUL)return;
        for(long packed:List.copyOf(sites.awaitingQueen)){
            var origin=BlockPos.of(packed);var pos=origin.offset(QUEEN);
            if(!w.hasChunkAt(pos))continue;
            var uuid=id(origin,"queen");
            if(w.getEntity(uuid) instanceof IceQueen){sites.awaitingQueen.remove(packed);sites.setDirty();continue;}
            var queen=QueenContent.BOSS.get().create(w);if(queen==null)continue;
            queen.setUUID(uuid);queen.moveTo(pos.getX()+.5,pos.getY(),pos.getZ()+.5,180,0);
            if(!w.noCollision(queen)||w.containsAnyLiquid(queen.getBoundingBox()))continue;
            queen.setPersistenceRequired();queen.restrictTo(pos,30);
            if(w.addFreshEntity(queen)){sites.awaitingQueen.remove(packed);sites.setDirty();}
        }
    }
    private static UUID manorId(BlockPos p,String suffix){return UUID.nameUUIDFromBytes(("slavicmyths:manor:"+p.asLong()+":"+suffix).getBytes(StandardCharsets.UTF_8));}
    private static void spawnGluheya(ServerLevel w,WinterEncounterSpawns.Sites sites){
        if(w.getDifficulty()==Difficulty.PEACEFUL)return;
        var type=BuiltInRegistries.ENTITY_TYPE.getOptional(ResourceLocation.fromNamespaceAndPath("slavicmyths","gluheya")).orElse(null);
        if(type==null)return;
        for(long packed:List.copyOf(sites.awaitingGluheya)){
            var origin=BlockPos.of(packed);var pos=origin.offset(0,2,16);
            if(!w.hasChunkAt(pos))continue;
            var uuid=manorId(origin,"boss");
            if(w.getEntity(uuid)!=null){sites.awaitingGluheya.remove(packed);sites.setDirty();continue;}
            if(!(type.create(w) instanceof net.minecraft.world.entity.Mob mob))continue;
            mob.setUUID(uuid);mob.moveTo(pos.getX()+.5,pos.getY(),pos.getZ()+.5,180,0);
            if(!w.noCollision(mob)||w.containsAnyLiquid(mob.getBoundingBox()))continue;
            mob.setPersistenceRequired();mob.restrictTo(pos,24);
            if(w.addFreshEntity(mob)){sites.awaitingGluheya.remove(packed);sites.setDirty();}
        }
    }
    private static void loot(ServerLevel w,BlockPos origin,Loot entry){
        if(w.getBlockEntity(origin.offset(entry.pos())) instanceof ChestBlockEntity chest){
            var table=ResourceKey.create(Registries.LOOT_TABLE,ResourceLocation.fromNamespaceAndPath("slavicmyths","chests/winter_palace/"+entry.kind()));
            long seed=w.getSeed()^origin.offset(entry.pos()).asLong();
            chest.setLootTable(table,seed==0?1:seed);chest.setChanged();
        }
    }
    public static void reward(ServerLevel w,IceQueen queen){
        var sites=WinterEncounterSpawns.Sites.get(w);
        for(long packed:sites.palaces){
            var origin=BlockPos.of(packed);
            if(!queen.getUUID().equals(id(origin,"queen"))||sites.rewardedPalaces.contains(packed))continue;
            for(var entry:Plan.LOOT)if(entry.kind().equals("boss_reward"))loot(w,origin,entry);
            sites.rewardedPalaces.add(packed);sites.setDirty();return;
        }
    }
    private static boolean natural(ServerLevel w,BlockPos p){
        var s=w.getBlockState(p);
        var id=BuiltInRegistries.BLOCK.getKey(s.getBlock());
        return w.getBlockEntity(p)==null&&!s.is(Blocks.BEDROCK)
            &&!org.slavicmyths.rpg.tools.ToolLogOrigins.get(w).blocked(p)
            &&(ManualStructureJobs.natural(s)||!s.getFluidState().isEmpty()
                ||id.getNamespace().equals("slavicmyths")&&NATURAL_WINTER.contains(id.getPath()));
    }
    private static final class Job {
        final ServerLevel w;BlockPos origin;final boolean natural,resume,autoY;final CommandSourceStack source;
        final SiteChunks chunks;
        final boolean manor;
        final int X0,X1,Z0,Z1,Y1,H;
        final List<BlockPos> columns;
        final List<ChunkPos> footprint=new ArrayList<>();
        final int[] tops,bottoms;
        int loaded,column,vertical=Integer.MIN_VALUE,phase,furnish;
        Job(ServerLevel w,BlockPos origin,boolean natural,boolean resume,CommandSourceStack source){
            this(w,origin,natural,resume,source,false);
        }
        Job(ServerLevel w,BlockPos origin,boolean natural,boolean resume,CommandSourceStack source,boolean autoY){
            this(w,origin,natural,resume,source,autoY,false);
        }
        Job(ServerLevel w,BlockPos origin,boolean natural,boolean resume,CommandSourceStack source,boolean autoY,boolean manor){
            this.w=w;this.origin=origin;this.natural=natural;this.resume=resume;this.source=source;this.autoY=autoY;this.manor=manor;chunks=new SiteChunks(w);
            X0=manor?GluheyaManorPlan.X0:WinterPalace.X0; X1=manor?GluheyaManorPlan.X1:WinterPalace.X1;
            Z0=manor?GluheyaManorPlan.Z0:WinterPalace.Z0; Z1=manor?GluheyaManorPlan.Z1:WinterPalace.Z1;
            Y1=manor?GluheyaManorPlan.Y1:WinterPalace.Y1; H=Y1+1;
            columns=manor?GluheyaManorPlan.COLUMNS:Plan.COLUMNS; tops=new int[columns.size()];bottoms=new int[columns.size()];
            for(int x=(origin.getX()+X0)>>4;x<=(origin.getX()+X1)>>4;x++)
                for(int z=(origin.getZ()+Z0)>>4;z<=(origin.getZ()+Z1)>>4;z++)footprint.add(new ChunkPos(x,z));
        }
        void step(){
            try{
                long deadline=System.nanoTime()+chunks.budgetNanos();
                for(int ops=0;ops<4096&&System.nanoTime()<deadline;ops++){
                    if(phase==0){
                        if(loaded<footprint.size()){
                            chunks.prefetch(footprint,loaded);if(!chunks.ready(footprint.get(loaded)))return;loaded++;continue;
                        }
                        if(autoY){
                            var c=manor?origin.offset(0,0,13):centerOf(origin);
                            int y=w.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,c.getX(),c.getZ());
                            origin=origin.atY(Math.clamp(y,w.getMinBuildHeight()+2,w.getMaxBuildHeight()-H));
                        }
                        if(!(manor?manorBorder(w,origin):border(w,origin)))throw new IllegalStateException("Palace outside world bounds");
                        if(!resume&&(!manor||natural)&&!ManualStructureRecords.get(w).overlapping(manor?new BoundingBox(origin.getX()+X0,w.getMinBuildHeight(),origin.getZ()+Z0,origin.getX()+X1,w.getMaxBuildHeight()-1,origin.getZ()+Z1):bounds(w,origin)).isEmpty())throw new SiteRejected("Existing construction");
                        if(natural&&!resume&&!w.getBiome(manor?origin.offset(0,0,13):centerOf(origin)).is(net.minecraft.tags.TagKey.create(Registries.BIOME,ResourceLocation.fromNamespaceAndPath("slavicmyths","winter_forest"))))
                            throw new SiteRejected("Not winter forest");
                        phase=1;continue;
                    }
                    if(manor?w.players().stream().anyMatch(p->!p.isSpectator()&&p.getX()>=origin.getX()+X0-1&&p.getX()<=origin.getX()+X1+2&&p.getZ()>=origin.getZ()+Z0-1&&p.getZ()<=origin.getZ()+Z1+2):playerInside(w,origin))return;
                    if(phase==4){
                        if(manor){
                            var sites=WinterEncounterSpawns.Sites.get(w);
                            var arrival=origin.offset(0,1,-3);
                            ManualStructureRecords.get(w).add(manorId(origin,"record"),"site/gluheya_manor",0,arrival,
                                new BoundingBox(origin.getX()+X0,origin.getY(),origin.getZ()+Z0,origin.getX()+X1,origin.getY()+Y1,origin.getZ()+Z1));
                            sites.finishManor(origin,manorKey(origin));spawnGluheya(w,sites);
                            finish(true,"Поместье Глухеи V1 создано: "+origin.toShortString());
                            if(source!=null)reportLocation(source,arrival,"Вход в поместье");return;
                        }
                        if(furnish<Plan.LOOT.size()){
                            var entry=Plan.LOOT.get(furnish++);if(!entry.kind().equals("boss_reward"))loot(w,origin,entry);continue;
                        }
                        int i=furnish-Plan.LOOT.size();
                        if(i<Plan.STANDS.size()){
                            var entry=Plan.STANDS.get(i);var pos=origin.offset(entry.pos());var uuid=id(origin,"stand:"+i);
                            if(w.getEntity(uuid)==null){
                                var stand=EntityType.ARMOR_STAND.create(w);
                                if(stand==null)throw new IllegalStateException("Cannot create armour stand");
                                stand.setUUID(uuid);stand.moveTo(pos.getX()+.5,pos.getY(),pos.getZ()+.5,entry.yaw(),0);
                                if(!w.addFreshEntity(stand))return;
                            }
                            furnish++;continue;
                        }
                        var sites=WinterEncounterSpawns.Sites.get(w);
                        ManualStructureRecords.get(w).add(id(origin,"record"),"site/winter_palace",0,origin.offset(0,1,0),
                            new BoundingBox(origin.getX()+X0,origin.getY(),origin.getZ()+Z0,origin.getX()+X1,origin.getY()+Y1,origin.getZ()+Z1));
                        sites.finishPalace(origin,siteKey(origin));sites.litPalaces.add(origin.asLong());sites.setDirty();spawnWaiting(w,sites);
                        finish(true,"Дворец вечной зимы V2 создан: "+origin.toShortString());if(source!=null)reportLocation(source,origin.offset(0,1,0),"Вход во дворец");return;
                    }
                    if(column>=columns.size()){
                        if(phase==1){var sites=WinterEncounterSpawns.Sites.get(w);if(manor)sites.beginManor(origin,natural);else sites.beginPalace(origin,natural);}
                        phase++;column=0;vertical=Integer.MIN_VALUE;continue;
                    }
                    var local=columns.get(column);int x=local.getX(),z=local.getZ(),wx=origin.getX()+x,wz=origin.getZ()+z;
                    if(vertical==Integer.MIN_VALUE){
                        if(phase==1){
                            tops[column]=Math.max(origin.getY()+Y1,w.getHeight(Heightmap.Types.WORLD_SURFACE,wx,wz)-1);
                            int floor=origin.getY()-1;
                            while(floor>w.getMinBuildHeight()){
                                var p=new BlockPos(wx,floor,wz);var s=w.getBlockState(p);
                                if(s.getFluidState().isEmpty()&&s.isFaceSturdy(w,p,Direction.UP))break;
                                floor--;
                            }
                            bottoms[column]=floor+1;
                        }
                        vertical=phase==3?origin.getY():tops[column];
                    }
                    var p=new BlockPos(wx,vertical,wz);
                    if(phase==1){
                        if(!resume&&(!manor||natural)&&!WinterPalace.natural(w,p))throw new SiteRejected("Protected block "+BuiltInRegistries.BLOCK.getKey(w.getBlockState(p).getBlock())+" at "+p.toShortString());
                    }else if(phase==2){
                        if(vertical<origin.getY())w.setBlock(p,manor?GluheyaManorPlan.PALETTE[22]:Blocks.PACKED_ICE.defaultBlockState(),18);
                        else if(!w.getBlockState(p).isAir())w.setBlock(p,Blocks.AIR.defaultBlockState(),18);
                    }else{
                        int y=vertical-origin.getY();
                        var state=manor?GluheyaManorPlan.block(x,y,z):Plan.block(x,y,z);
                        if(!manor&&state.isAir()&&local.getY()<=4&&y<local.getY())state=Blocks.PACKED_ICE.defaultBlockState();
                        if(!state.isAir())w.setBlock(p,state,18);
                    }
                    if(phase==3){if(++vertical>origin.getY()+Y1){column++;vertical=Integer.MIN_VALUE;}}
                    else if(--vertical<bottoms[column]){column++;vertical=Integer.MIN_VALUE;}
                }
            }catch(RuntimeException e){
                if(e instanceof SiteRejected&&natural&&!resume)REJECTED.computeIfAbsent(w,k->new HashSet<>()).add(new BlockPos(origin.getX(),0,origin.getZ()).asLong());
                finish(false,e.toString());
            }
        }
        void finish(boolean ok,String msg){
            JOBS.remove(w,this);chunks.close();
            if(!ok&&WinterEncounterSpawns.Sites.get(w).pendingPalace!=null)FAILED_PENDING.add(w);
            if(ok){LogUtils.getLogger().info("Winter palace: {}",msg);if(source!=null)source.sendSuccess(()->Component.literal(msg),false);}
            else{LogUtils.getLogger().warn("Winter palace: {}",msg);if(source!=null)source.sendFailure(Component.literal(msg));}
        }
    }
    private static final class SiteRejected extends RuntimeException {SiteRejected(String message){super(message);}}
    @net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths")
    public static final class Lifecycle {
        @net.neoforged.bus.api.SubscribeEvent
        public static void unload(net.neoforged.neoforge.event.level.LevelEvent.Unload event){
            if(event.getLevel() instanceof ServerLevel w){
                var job=JOBS.remove(w);if(job!=null)job.chunks.close();REJECTED.remove(w);FAILED_PENDING.remove(w);LOOKAHEAD.remove(w);
            }
        }
    }
    private WinterPalace(){}
}
