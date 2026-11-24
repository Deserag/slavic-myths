package org.slavicmyths.winter;

import java.io.*;
import java.util.*;
import java.util.zip.*;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.*;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import org.slavicmyths.commands.CommandTree;
import org.slavicmyths.worldgen.*;
import org.slavicmyths.winterboss.*;
import static org.slavicmyths.winter.WinterEncounterLayout.*;

/** Approved V2 blueprint; one shared builder for encounters and operator commands. */
public final class FrozenShip {
    static final int X0=-44,X1=38,Z0=-23,Z1=22,Y0=-1,Y1=36;
    static final int W=83,D=46,H=38,COUNT=16022;
    private static final Map<ServerLevel,Job> JOBS=new IdentityHashMap<>();
    private static final Map<ServerLevel,Search> SEARCHES=new WeakHashMap<>();
    private static final Map<ServerLevel,Set<Long>> REJECTED=new WeakHashMap<>();
    private static final Set<ServerLevel> FAILED_PENDING=Collections.newSetFromMap(new WeakHashMap<>());
    private static final class Plan {
        static final byte[] DATA=load();
        static final List<BlockPos> COLUMNS=columns();
        private static byte[] load(){
            try(var in=FrozenShip.class.getResourceAsStream("/data/slavicmyths/winter/frozen_ship_v2.b64")){
                if(in==null)throw new IOException("Missing frozen ship V2 blueprint");
                var compressed=Base64.getMimeDecoder().decode(in.readAllBytes());
                byte[] data;
                try(var stream=new InflaterInputStream(new ByteArrayInputStream(compressed))){data=stream.readNBytes(W*D*H+1);}
                var crc=new CRC32();crc.update(data);int count=0;
                for(byte value:data){if(value<0||value>22)throw new IOException("Invalid material");if(value!=0)count++;}
                if(data.length!=W*D*H||count!=COUNT||crc.getValue()!=0xcddb5d6eL)throw new IOException("V2 blueprint checksum/count mismatch");
                return data;
            }catch(IOException e){throw new IllegalStateException("Cannot read frozen ship V2",e);}
        }
        static int at(int x,int y,int z){
            if(x<X0||x>X1||y<Y0||y>Y1||z<Z0||z>Z1)return 0;
            return DATA[((y-Y0)*D+z-Z0)*W+x-X0];
        }
        static List<BlockPos> columns(){
            var list=new ArrayList<BlockPos>();
            for(int z=Z0;z<=Z1;z++)for(int x=X0;x<=X1;x++)
                for(int y=Y0;y<=Y1;y++)if(at(x,y,z)!=0){list.add(new BlockPos(x,0,z));break;}
            return List.copyOf(list);
        }
    }
    private static boolean ocean(net.minecraft.core.Holder<net.minecraft.world.level.biome.Biome> biome){
        return biome.is(Biomes.FROZEN_OCEAN)||biome.is(Biomes.DEEP_FROZEN_OCEAN);
    }
    private static boolean noiseOcean(ServerLevel world,BlockPos pos){
        return ocean(world.getChunkSource().getGenerator().getBiomeSource().getNoiseBiome(
            pos.getX()>>2,pos.getY()>>2,pos.getZ()>>2,world.getChunkSource().randomState().sampler()));
    }
    private static long cellKey(BlockPos p){return key(cell(p.getX()),cell(p.getZ()));}
    static boolean active(ServerLevel world){return JOBS.containsKey(world);}
    static boolean clearOfShips(WinterEncounterSpawns.Sites sites,BlockPos pos){
        for(long packed:sites.ships){
            BlockPos other=BlockPos.of(packed);
            if(Math.abs((long)other.getX()-pos.getX())<384&&Math.abs((long)other.getZ()-pos.getZ())<384)return false;
        }
        return true;
    }
    private static boolean clearOfOccupiedCells(WinterEncounterSpawns.Sites sites,BlockPos pos){
        int cx=cell(pos.getX()),cz=cell(pos.getZ());
        for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++)
            if(sites.occupied.contains(key(cx+dx,cz+dz))&&
                Math.abs((long)center(cx+dx)-pos.getX())<384+SEARCH&&
                Math.abs((long)center(cz+dz)-pos.getZ())<384+SEARCH)return false;
        return true;
    }
    private static boolean border(ServerLevel w,BlockPos p){
        return p.getY()+Y0-1>=w.getMinBuildHeight()+1&&p.getY()+Y1<w.getMaxBuildHeight()
            &&w.getWorldBorder().isWithinBounds(p.offset(X0,0,Z0))
            &&w.getWorldBorder().isWithinBounds(p.offset(X1,0,Z1));
    }
    static boolean queueNatural(ServerLevel world,BlockPos column){
        var origin=new BlockPos(column.getX(),world.getSeaLevel()-1,column.getZ());
        var sites=WinterEncounterSpawns.Sites.get(world);
        if(active(world)||sites.pendingShip!=null||sites.occupied.contains(cellKey(origin))||
            REJECTED.computeIfAbsent(world,k->new HashSet<>()).contains(origin.asLong())||
            !border(world,origin)||!clearOfShips(sites,origin)||!clearOfOccupiedCells(sites,origin)||!noiseOcean(world,origin))return false;
        for(var player:world.players())if(!player.isSpectator()&&
            Math.abs(player.getX()-origin.getX())<=W/2+16&&Math.abs(player.getZ()-origin.getZ())<=D/2+16)return false;
        JOBS.put(world,new Job(world,origin,true,null,false,false));return true;
    }
    public static void register(CommandTree tree){
        tree.add(Commands.literal("slavicmyths").then(Commands.literal("dev")
            .then(Commands.literal("generate").then(Commands.literal("frozen_ship")
                .requires(s->s.hasPermission(2)).executes(c->manual(c.getSource(),null))
                .then(Commands.argument("pos",BlockPosArgument.blockPos()).executes(c->manual(c.getSource(),BlockPosArgument.getBlockPos(c,"pos"))))))));
    }
    private static int manual(CommandSourceStack source,BlockPos explicit){
        var world=source.getLevel();var sites=WinterEncounterSpawns.Sites.get(world);
        if(active(world)||sites.pendingShip!=null){
            source.sendFailure(Component.literal("Корабль уже строится."));return 0;
        }
        var p=explicit!=null?explicit:BlockPos.containing(source.getPosition()).offset(0,0,-64);
        if(!world.getWorldBorder().isWithinBounds(p.offset(X0,0,Z0))||
           !world.getWorldBorder().isWithinBounds(p.offset(X1,0,Z1))){
            source.sendFailure(Component.literal("Корабль выходит за границы мира."));return 0;
        }
        if(explicit!=null&&!border(world,p)){source.sendFailure(Component.literal("Недостаточно высоты для корабля."));return 0;}
        JOBS.put(world,new Job(world,p,false,source,explicit==null,false));
        source.sendSuccess(()->Component.literal("Строительство ледяного корабля V2: "+p.toShortString()+" (без проверки биома и рельефа)."),false);
        return 1;
    }
    /** Progressive biome-noise search: no terrain/chunk generation until an ocean site is selected. */
    private static final class Search {
        final int cx,cz;int ring,edge,offset;
        Search(ServerLevel w){cx=cell(w.getSharedSpawnPos().getX());cz=cell(w.getSharedSpawnPos().getZ());}
        BlockPos next(int y){
            int dx=0,dz=0;
            if(ring>0){int side=2*ring,i=edge%side;
                switch(edge/side){case 0->{dx=-ring+i;dz=-ring;}case 1->{dx=ring;dz=-ring+i;}
                    case 2->{dx=ring-i;dz=ring;}default->{dx=-ring;dz=ring-i;}}
            }
            int ox=(offset%32-16)*16,oz=(offset/32-16)*16;
            if(++offset==1024){offset=0;if(++edge>=Math.max(1,8*ring)){edge=0;ring++;}}
            return new BlockPos(center(cx+dx)+ox,y,center(cz+dz)+oz);
        }
    }
    static void tick(MinecraftServer server){
        for(var world:server.getAllLevels()){
            var sites=WinterEncounterSpawns.Sites.get(world);
            if(!active(world)&&sites.pendingShip!=null&&!FAILED_PENDING.contains(world))
                JOBS.put(world,new Job(world,sites.pendingShip,sites.pendingNatural,null,false,true));
            var job=JOBS.get(world);if(job!=null){job.step();continue;}
            if(world.getGameTime()%20==0&&world.getDifficulty()!=Difficulty.PEACEFUL)spawnWaiting(world,sites);
            if(world!=server.overworld()||sites.naturalShip||sites.pendingShip!=null||world.players().isEmpty()||
                !(world.getChunkSource().getGenerator() instanceof NoiseBasedChunkGenerator)||
                world.getGameTime()%5!=0)continue;
            if(world.getChunkSource().getGenerator().getBiomeSource().possibleBiomes().stream().noneMatch(FrozenShip::ocean))continue;
            if(WinterWorld.isTest(world)&&queueNatural(world,new BlockPos(768,world.getSeaLevel(),256)))continue;
            var search=SEARCHES.computeIfAbsent(world,Search::new);
            // Finite vanilla world bounds; no unbounded work in any individual tick.
            long searchDeadline=System.nanoTime()+2_000_000L;
            for(int i=0;i<64&&search.ring<=117188&&System.nanoTime()<searchDeadline;i++){
                var p=search.next(world.getSeaLevel()-1);
                if(queueNatural(world,p))break;
            }
        }
    }
    private static void spawnWaiting(ServerLevel world,WinterEncounterSpawns.Sites sites){
        for(long packed:List.copyOf(sites.awaitingCaptain)){
            var pos=BlockPos.of(packed).offset(7,11,0);
            if(!world.hasChunkAt(pos))continue;
            var box=new AABB(pos).inflate(36,12,24);
            // A reload between entity insertion and SavedData persistence must not duplicate the boss.
            if(!world.getEntitiesOfClass(ShipCaptain.class,box).isEmpty()){
                sites.awaitingCaptain.remove(packed);sites.setDirty();continue;
            }
            var captain=ShipCaptainContent.BOSS.get().create(world);if(captain==null)continue;
            captain.moveTo(pos.getX()+.5,pos.getY(),pos.getZ()+.5,90,0);
            if(!world.noCollision(captain)||world.containsAnyLiquid(captain.getBoundingBox()))continue;
            captain.setPersistenceRequired();captain.restrictTo(pos,25);
            if(world.addFreshEntity(captain)){sites.awaitingCaptain.remove(packed);sites.setDirty();}
        }
    }
    private static BlockState block(int value,int x,int y,int z){
        Block b=switch(value){
            case 1,3->Blocks.BLUE_WOOL;case 2->Blocks.BARREL;case 4->Blocks.BLUE_ICE;
            case 5,16->Blocks.CHAIN;case 6->Blocks.SPRUCE_PLANKS;case 7->Blocks.DARK_OAK_PLANKS;
            case 8,18->Blocks.SPRUCE_PLANKS;case 9->Blocks.DARK_OAK_FENCE;
            case 10->Blocks.DARK_OAK_TRAPDOOR;case 11->Blocks.PACKED_ICE;
            case 12->FrozenStoneBlocks.ALL.get("frozen_deepslate_bricks").get();
            case 13->Blocks.LANTERN;case 14->Blocks.PACKED_ICE;case 15->Blocks.DARK_OAK_LOG;
            case 17->Blocks.SNOW_BLOCK;case 19->Blocks.DARK_OAK_PLANKS;
            case 20->Blocks.SPRUCE_STAIRS;case 21->Blocks.STRIPPED_SPRUCE_WOOD;
            case 22->Blocks.YELLOW_STAINED_GLASS;default->Blocks.AIR;
        };
        var state=b.defaultBlockState();
        if(value==20){
            if(Plan.at(x,y+1,z)==20)return Blocks.SPRUCE_PLANKS.defaultBlockState();
            Direction dir=z<=-13?Direction.SOUTH:(x>=12&&x<=18&&z>=-3&&z<=-1&&y<=9?Direction.WEST:Direction.EAST);
            state=state.setValue(BlockStateProperties.HORIZONTAL_FACING,dir);
        }else if(value==9){
            for(Direction dir:Direction.Plane.HORIZONTAL){
                int neighbor=Plan.at(x+dir.getStepX(),y,z+dir.getStepZ());
                boolean connected=neighbor!=0&&neighbor!=5&&neighbor!=13&&neighbor!=16;
                state=switch(dir){
                    case NORTH->state.setValue(BlockStateProperties.NORTH,connected);
                    case SOUTH->state.setValue(BlockStateProperties.SOUTH,connected);
                    case EAST->state.setValue(BlockStateProperties.EAST,connected);
                    case WEST->state.setValue(BlockStateProperties.WEST,connected);
                    default->state;
                };
            }
        }else if(value==5||value==16){
            Direction.Axis axis=Direction.Axis.Y;
            if(Plan.at(x,y-1,z)!=value&&Plan.at(x,y+1,z)!=value){
                if(Plan.at(x-1,y,z)==value||Plan.at(x+1,y,z)==value)axis=Direction.Axis.X;
                else if(Plan.at(x,y,z-1)==value||Plan.at(x,y,z+1)==value)axis=Direction.Axis.Z;
            }
            state=state.setValue(BlockStateProperties.AXIS,axis);
        }else if(value==13&&Plan.at(x,y-1,z)==0&&Plan.at(x,y+1,z)!=0){
            state=state.setValue(BlockStateProperties.HANGING,true);
        }
        return state;
    }
    private static final class Job {
        final ServerLevel world;BlockPos origin;final boolean natural,resume,autoY;final CommandSourceStack source;
        final SiteChunks chunks;final List<ChunkPos> footprint=new ArrayList<>();final int[] tops=new int[Plan.COLUMNS.size()];
        int loaded,column,vertical=Integer.MIN_VALUE,phase;
        Job(ServerLevel world,BlockPos origin,boolean natural,CommandSourceStack source,boolean autoY,boolean resume){
            this.world=world;this.origin=origin;this.natural=natural;this.source=source;this.autoY=autoY;this.resume=resume;
            chunks=new SiteChunks(world);
            for(int x=(origin.getX()+X0)>>4;x<=(origin.getX()+X1)>>4;x++)
                for(int z=(origin.getZ()+Z0)>>4;z<=(origin.getZ()+Z1)>>4;z++)footprint.add(new ChunkPos(x,z));
        }
        void step(){
            try{
                long deadline=System.nanoTime()+chunks.budgetNanos();
                int operations=0;
                while(System.nanoTime()<deadline&&operations++<2048){
                    if(phase==0){
                        if(loaded<footprint.size()){if(!chunks.ready(footprint.get(loaded)))return;loaded++;continue;}
                        if(autoY){
                            int y=world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,origin.getX(),origin.getZ())-1;
                            origin=new BlockPos(origin.getX(),Math.clamp(y,world.getMinBuildHeight()+3,world.getMaxBuildHeight()-H),origin.getZ());
                        }
                        if(!border(world,origin))throw new IllegalStateException("World bounds");
                        if(natural&&!resume&&!ocean(world.getBiome(origin)))throw new IllegalStateException("Not a frozen ocean");
                        var bounds=new BoundingBox(origin.getX()+X0,world.getMinBuildHeight(),origin.getZ()+Z0,
                            origin.getX()+X1,world.getMaxBuildHeight()-1,origin.getZ()+Z1);
                        if(natural&&!resume&&!ManualStructureRecords.get(world).overlapping(bounds).isEmpty())
                            throw new IllegalStateException("Existing construction");
                        phase=1;continue;
                    }
                    if(column>=Plan.COLUMNS.size()){
                        if(phase==1){
                            WinterEncounterSpawns.Sites.get(world).beginShip(origin,natural);
                            phase=2;column=0;vertical=Integer.MIN_VALUE;continue;
                        }
                        if(phase==2){phase=3;column=0;vertical=Integer.MIN_VALUE;continue;}
                        var sites=WinterEncounterSpawns.Sites.get(world);
                        sites.finishShip(origin,natural);
                        ManualStructureRecords.get(world).add(UUID.nameUUIDFromBytes(("frozen_ship:"+origin.asLong()).getBytes(java.nio.charset.StandardCharsets.UTF_8)),
                            "site/frozen_ship",0,origin.offset(5,1,-23),
                            new BoundingBox(origin.getX()+X0,origin.getY()-2,origin.getZ()+Z0,origin.getX()+X1,origin.getY()+Y1,origin.getZ()+Z1));
                        finish(true,"Ледяной корабль V2 создан: "+origin.toShortString());return;
                    }
                    var local=Plan.COLUMNS.get(column);
                    int x=local.getX(),z=local.getZ(),wx=origin.getX()+x,wz=origin.getZ()+z;
                    if(vertical==Integer.MIN_VALUE){
                        if(phase==1)tops[column]=Math.max(origin.getY()+Y1,world.getHeight(Heightmap.Types.WORLD_SURFACE,wx,wz)-1);
                        vertical=phase==3?origin.getY()+Y0:tops[column];
                    }
                    var pos=new BlockPos(wx,vertical,wz);
                    if(phase==1){
                        if(natural&&!resume&&(!ManualStructureJobs.natural(world.getBlockState(pos))||world.getBlockEntity(pos)!=null))
                            throw new IllegalStateException("Protected/non-natural block at "+pos);
                    }else if(phase==2){
                        // Clear all air volumes too, including the interior, and create a stable ice foundation.
                        if(vertical==origin.getY()-2)world.setBlock(pos,Blocks.PACKED_ICE.defaultBlockState(),18);
                        else if(!world.getBlockState(pos).isAir())world.setBlock(pos,Blocks.AIR.defaultBlockState(),18);
                    }else{
                        int localY=vertical-origin.getY(),value=Plan.at(x,localY,z);
                        if(value!=0)world.setBlock(pos,block(value,x,localY,z),18);
                    }
                    if(phase==3){if(++vertical>origin.getY()+Y1){column++;vertical=Integer.MIN_VALUE;}}
                    else if(--vertical<origin.getY()-2){column++;vertical=Integer.MIN_VALUE;}
                }
            }catch(RuntimeException e){finish(false,e.toString());}
        }
        void finish(boolean ok,String message){
            JOBS.remove(world,this);chunks.close();
            if(!ok&&natural)REJECTED.computeIfAbsent(world,k->new HashSet<>()).add(origin.asLong());
            if(!ok&&WinterEncounterSpawns.Sites.get(world).pendingShip!=null)FAILED_PENDING.add(world);
            if(!ok){LogUtils.getLogger().warn("Frozen ship: {}",message);if(source!=null)source.sendFailure(Component.literal(message));}
            else{LogUtils.getLogger().info("Frozen ship: {}",message);if(source!=null)source.sendSuccess(()->Component.literal(message),false);}
        }
    }
    @net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths")
    public static final class Lifecycle {
        @net.neoforged.bus.api.SubscribeEvent
        public static void unload(net.neoforged.neoforge.event.level.LevelEvent.Unload event){
            if(event.getLevel() instanceof ServerLevel world){
                var job=JOBS.remove(world);if(job!=null)job.chunks.close();
                SEARCHES.remove(world);REJECTED.remove(world);FAILED_PENDING.remove(world);
            }
        }
    }
    private FrozenShip(){}
}
