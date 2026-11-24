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
    private static final BlockPos QUEEN=new BlockPos(0,25,37);
    private static final Map<ServerLevel,Job> JOBS=new IdentityHashMap<>();
    private static final Map<ServerLevel,Set<Long>> REJECTED=new WeakHashMap<>();
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
    static boolean queueNatural(ServerLevel w,BlockPos center){
        var sites=WinterEncounterSpawns.Sites.get(w);
        int y=Math.clamp(center.getY(),w.getMinBuildHeight()+2,w.getMaxBuildHeight()-H);
        var p=new BlockPos(center.getX(),y,center.getZ()-29);
        if(active(w)||sites.pendingPalace!=null||sites.occupied.contains(siteKey(p))
            ||REJECTED.computeIfAbsent(w,k->new HashSet<>()).contains(p.asLong())
            ||!border(w,p)||!FrozenShip.clearOfShips(sites,center)||playerInside(w,p))return false;
        JOBS.put(w,new Job(w,p,true,false,null));return true;
    }
    public static void register(CommandTree tree){
        tree.add(Commands.literal("slavicmyths").then(Commands.literal("dev")
            .then(Commands.literal("generate").then(Commands.literal("winter_palace")
                .requires(s->s.hasPermission(2)).executes(c->manual(c.getSource(),null))
                .then(Commands.argument("pos",BlockPosArgument.blockPos()).executes(c->manual(c.getSource(),BlockPosArgument.getBlockPos(c,"pos"))))))));
    }
    private static int manual(CommandSourceStack s,BlockPos explicit){
        var w=s.getLevel();var sites=WinterEncounterSpawns.Sites.get(w);
        if(!w.dimension().equals(Level.OVERWORLD)||active(w)||FrozenShip.active(w)||sites.pendingPalace!=null||sites.pendingShip!=null){
            s.sendFailure(Component.literal("Дворец недоступен в этом измерении или зимняя постройка уже строится."));return 0;
        }
        var c=explicit==null?BlockPos.containing(s.getPosition()).offset(0,0,80):explicit;
        if(explicit==null){
            if(!w.hasChunkAt(c)){s.sendFailure(Component.literal("Укажи загруженную точку командой."));return 0;}
            c=w.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,c);
        }
        var p=c.offset(0,0,-29);
        if(!border(w,p)||playerInside(w,p)||sites.occupied.contains(siteKey(p))||!FrozenShip.clearOfShips(sites,c)){
            s.sendFailure(Component.literal("Недостаточно места или зимняя встреча уже существует в этой ячейке."));return 0;
        }
        JOBS.put(w,new Job(w,p,false,false,s));s.sendSuccess(()->Component.literal("Строительство дворца V2: "+p.toShortString()),false);return 1;
    }
    static void tick(ServerLevel w){
        var sites=WinterEncounterSpawns.Sites.get(w);
        if(!active(w)&&sites.pendingPalace!=null&&!FAILED_PENDING.contains(w)&&!FrozenShip.active(w)&&sites.pendingShip==null)
            JOBS.put(w,new Job(w,sites.pendingPalace,sites.pendingPalaceNatural,true,null));
        var job=JOBS.get(w);if(job!=null){job.step();return;}
        if(w.getGameTime()%20==0&&w.getDifficulty()!=Difficulty.PEACEFUL)spawnWaiting(w,sites);
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
        return w.getBlockEntity(p)==null&&!s.is(Blocks.BEDROCK)
            &&(ManualStructureJobs.natural(s)||s.is(FrozenStoneBlocks.ALL.get("frozen_stone").get()));
    }
    private static final class Job {
        final ServerLevel w;final BlockPos origin;final boolean natural,resume;final CommandSourceStack source;
        final SiteChunks chunks;
        final List<ChunkPos> footprint=new ArrayList<>();
        final int[] tops=new int[Plan.COLUMNS.size()],bottoms=new int[Plan.COLUMNS.size()];
        int loaded,column,vertical=Integer.MIN_VALUE,phase,furnish;
        Job(ServerLevel w,BlockPos origin,boolean natural,boolean resume,CommandSourceStack source){
            this.w=w;this.origin=origin;this.natural=natural;this.resume=resume;this.source=source;chunks=new SiteChunks(w);
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
                        if(!border(w,origin))throw new IllegalStateException("Palace outside world bounds");
                        if(!resume&&!ManualStructureRecords.get(w).overlapping(bounds(w,origin)).isEmpty())throw new SiteRejected("Existing construction");
                        if(natural&&!resume&&!w.getBiome(centerOf(origin)).is(net.minecraft.tags.TagKey.create(Registries.BIOME,ResourceLocation.fromNamespaceAndPath("slavicmyths","winter_forest"))))
                            throw new SiteRejected("Not winter forest");
                        phase=1;continue;
                    }
                    if(playerInside(w,origin))return;
                    if(phase==4){
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
                        sites.finishPalace(origin,siteKey(origin));spawnWaiting(w,sites);
                        finish(true,"Дворец вечной зимы V2 создан: "+origin.toShortString());return;
                    }
                    if(column>=Plan.COLUMNS.size()){
                        if(phase==1)WinterEncounterSpawns.Sites.get(w).beginPalace(origin,natural);
                        phase++;column=0;vertical=Integer.MIN_VALUE;continue;
                    }
                    var local=Plan.COLUMNS.get(column);int x=local.getX(),z=local.getZ(),wx=origin.getX()+x,wz=origin.getZ()+z;
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
                        if(!resume&&!WinterPalace.natural(w,p))throw new SiteRejected("Protected block at "+p);
                    }else if(phase==2){
                        if(vertical<origin.getY())w.setBlock(p,Blocks.PACKED_ICE.defaultBlockState(),18);
                        else if(!w.getBlockState(p).isAir())w.setBlock(p,Blocks.AIR.defaultBlockState(),18);
                    }else{
                        int y=vertical-origin.getY();
                        var state=Plan.block(x,y,z);
                        if(state.isAir()&&local.getY()<=4&&y<local.getY())state=Blocks.PACKED_ICE.defaultBlockState();
                        if(!state.isAir())w.setBlock(p,state,18);
                    }
                    if(phase==3){if(++vertical>origin.getY()+Y1){column++;vertical=Integer.MIN_VALUE;}}
                    else if(--vertical<bottoms[column]){column++;vertical=Integer.MIN_VALUE;}
                }
            }catch(RuntimeException e){
                if(e instanceof SiteRejected&&natural&&!resume)REJECTED.computeIfAbsent(w,k->new HashSet<>()).add(origin.asLong());
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
                var job=JOBS.remove(w);if(job!=null)job.chunks.close();REJECTED.remove(w);FAILED_PENDING.remove(w);
            }
        }
    }
    private WinterPalace(){}
}
