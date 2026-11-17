package org.slavicmyths.buyan;

import java.util.*;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.tags.BlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.saveddata.SavedData;

/** Port structures on the EXISTING large Buyan island; never creates a second terrain generator. */
public final class BuyanHarbor {
    private BuyanHarbor(){}

    private static final class Record extends SavedData {
        final Set<Long> built=new HashSet<>();
        static Record get(ServerLevel world){
            return world.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(Record::new,Record::read),"slavicmyths_buyan_harbor_2622");
        }
        static Record read(CompoundTag n,HolderLookup.Provider registries){
            Record data=new Record();
            for(long value:n.getLongArray("Sites"))data.built.add(value);
            return data;
        }
        @Override public CompoundTag save(CompoundTag tag,HolderLookup.Provider registries){
            long[] sites=new long[built.size()];int i=0;
            for(long k:built)sites[i++]=k;
            tag.putLongArray("Sites",sites);
            return tag;
        }
        void done(BlockPos center){if(built.add(center.asLong()))setDirty();}
    }

    private static final class Plan {
        final Map<BlockPos,BlockState> blocks=new LinkedHashMap<>();
        void at(int x,int y,int z,BlockState state){
            BlockPos pos=new BlockPos(x,y,z);
            // Later fittings must be applied after their supporting shell, not at the old insertion position.
            blocks.remove(pos);
            blocks.put(pos,state);
        }
        void fill(int x1,int y1,int z1,int x2,int y2,int z2,BlockState b){
            for(int x=x1;x<=x2;x++)for(int z=z1;z<=z2;z++)for(int y=y1;y<=y2;y++)at(x,y,z,b);
        }
        void putLocal(int cx,int floor,int cz,int x,int y,int z,BlockState b){
            at(cx+x,floor+y,cz+z,b);
        }
    }

    private static final BlockState OAK=Blocks.OAK_PLANKS.defaultBlockState();
    private static final BlockState LOG=Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState();
    private static final BlockState FLOOR=Blocks.SPRUCE_PLANKS.defaultBlockState();
    private static final BlockState ROOF=Blocks.DARK_OAK_PLANKS.defaultBlockState();
    private static final BlockState AIR=Blocks.AIR.defaultBlockState();

    /** All doors face the sea (-Z), with a free two-block-wide central aisle. */
    private static void building(Plan p,int x,int z,int floor,int width,int depth,int height,String type){
        for(int dx=0;dx<width;dx++)for(int dz=0;dz<depth;dz++)
            p.fill(x+dx,floor-4,z+dz,x+dx,floor-1,z+dz,Blocks.COBBLESTONE.defaultBlockState());
        p.fill(x,floor,z,x+width-1,floor,z+depth-1,FLOOR);
        p.fill(x+1,floor+1,z+1,x+width-2,floor+height,z+depth-2,AIR);
        for(int y=1;y<=height;y++){
            for(int dx=0;dx<width;dx++){
                p.at(x+dx,floor+y,z,OAK);
                p.at(x+dx,floor+y,z+depth-1,OAK);
            }
            for(int dz=1;dz<depth-1;dz++){
                p.at(x,floor+y,z+dz,OAK);
                p.at(x+width-1,floor+y,z+dz,OAK);
            }
        }
        for(int dx:new int[]{0,width-1})for(int dz:new int[]{0,depth-1})
            p.fill(x+dx,floor+1,z+dz,x+dx,floor+height,z+dz,LOG);

        int door=width/2-1;
        for(int dx:new int[]{2,width-4})for(int dz:new int[]{0,depth-1})
            p.fill(x+dx,floor+3,z+dz,x+dx+1,floor+Math.min(height,4),z+dz,
                Blocks.GLASS.defaultBlockState());

        // A low, fully covered pitched roof: every horizontal column has a roof block.
        // Solid timber gables seal the space between the wall and each roof step.
        for(int dx=-1;dx<=width;dx++){
            int rise=Math.min(dx+1,width-dx)/2;
            int roofY=floor+height+1+rise;
            for(int dz=-1;dz<=depth;dz++)p.at(x+dx,roofY,z+dz,ROOF);
            if(dx>=0&&dx<width){
                for(int y=floor+height+1;y<roofY;y++){
                    p.at(x+dx,y,z,OAK);
                    p.at(x+dx,y,z+depth-1,OAK);
                }
            }
        }
        p.fill(x+door,floor,z-1,x+door+1,floor,z-1,FLOOR);

        // Real furniture is placed only after the wall, roof and floor passes.
        p.putLocal(x,floor,z,2,1,2,Blocks.FURNACE.defaultBlockState());
        p.putLocal(x,floor,z,2,1,3,Blocks.CRAFTING_TABLE.defaultBlockState());
        p.putLocal(x,floor,z,2,1,depth-3,Blocks.BARREL.defaultBlockState());
        p.putLocal(x,floor,z,3,1,depth-3,Blocks.CHEST.defaultBlockState());
        if(type.equals("house")){
            BlockState foot=Blocks.WHITE_BED.defaultBlockState()
                .setValue(BedBlock.FACING,Direction.SOUTH).setValue(BedBlock.PART,BedPart.FOOT);
            p.putLocal(x,floor,z,width-3,1,depth-4,foot);
            p.putLocal(x,floor,z,width-3,1,depth-3,foot.setValue(BedBlock.PART,BedPart.HEAD));
            p.putLocal(x,floor,z,width-4,1,3,Blocks.SPRUCE_STAIRS.defaultBlockState());
            p.putLocal(x,floor,z,width-5,1,depth-4,Blocks.SPRUCE_FENCE.defaultBlockState());
            p.putLocal(x,floor,z,width-5,2,depth-4,Blocks.SPRUCE_SLAB.defaultBlockState());
            p.putLocal(x,floor,z,3,1,depth-5,Blocks.BOOKSHELF.defaultBlockState());
        }else if(type.equals("market")){
            p.fill(x+1,floor,z-2,x+width-2,floor,z-1,FLOOR);
            // Display counters are to the sides, not across the entrance.
            p.fill(x+1,floor+1,z-2,x+door-2,floor+1,z-2,Blocks.SPRUCE_SLAB.defaultBlockState());
            p.fill(x+door+3,floor+1,z-2,x+width-2,floor+1,z-2,Blocks.SPRUCE_SLAB.defaultBlockState());
            p.putLocal(x,floor,z,width-3,1,3,Blocks.SMOKER.defaultBlockState());
            p.putLocal(x,floor,z,width-3,1,depth-3,Blocks.CHEST.defaultBlockState());
        }else{
            p.fill(x+door-1,floor,z-2,x+door+2,floor,z-1,FLOOR);
            for(int dz=4;dz<depth-2;dz+=2){
                p.putLocal(x,floor,z,width-3,1,dz,Blocks.BARREL.defaultBlockState());
                p.putLocal(x,floor,z,2,1,dz,Blocks.CHEST.defaultBlockState());
            }
        }

        // Wall torches have solid backing on the outer walls; hanging lamps have
        // a ceiling beam. Neither blocks the central aisle or either door leaf.
        p.putLocal(x,floor,z,1,2,3,Blocks.WALL_TORCH.defaultBlockState()
            .setValue(WallTorchBlock.FACING,Direction.EAST));
        p.putLocal(x,floor,z,width-2,2,depth-3,Blocks.WALL_TORCH.defaultBlockState()
            .setValue(WallTorchBlock.FACING,Direction.WEST));
        p.putLocal(x,floor,z,width-2,height,depth-2,LOG);
        p.putLocal(x,floor,z,width-2,height-1,depth-2,Blocks.LANTERN.defaultBlockState()
            .setValue(LanternBlock.HANGING,true));

        // Door pairs must be the LAST blocks in their openings. Each leaf gets
        // matching upper/lower facing and opposite hinges so vanilla can operate it.
        for(int offset=0;offset<2;offset++){
            DoorHingeSide hinge=offset==0?DoorHingeSide.LEFT:DoorHingeSide.RIGHT;
            BlockState bottom=Blocks.SPRUCE_DOOR.defaultBlockState()
                .setValue(DoorBlock.FACING,Direction.NORTH)
                .setValue(DoorBlock.HINGE,hinge)
                .setValue(DoorBlock.HALF,DoubleBlockHalf.LOWER);
            p.at(x+door+offset,floor+1,z,bottom);
            p.at(x+door+offset,floor+2,z,bottom.setValue(DoorBlock.HALF,DoubleBlockHalf.UPPER));
        }

        // The outer lamps hang below short roof-supported brackets, off the path.
        for(int dx:new int[]{door-1,door+2}){
            p.at(x+dx,floor+height+1,z-1,LOG);
            p.at(x+dx,floor+height,z-1,Blocks.LANTERN.defaultBlockState()
                .setValue(LanternBlock.HANGING,true));
        }
    }

    private static int shore(ServerLevel world,BuyanState.Site site,int lateral){
        int x=site.x()+lateral;
        int z=site.z()-Math.min(site.radius(),280);
        double radius=BuyanTerrain.shoreRadius(world.getSeed(),site.x(),site.z(),x,z);
        // Iterative projection onto the real irregular coast, not a circular guess.
        for(int i=0;i<5;i++){
            z=site.z()-(int)Math.floor(Math.sqrt(Math.max(1.0,
                radius*radius-lateral*lateral)));
            radius=BuyanTerrain.shoreRadius(world.getSeed(),site.x(),site.z(),x,z);
        }
        return z;
    }

    private static int surface(ServerLevel world,int x,int z){
        return world.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,x,z)-1;
    }

    private static void pier(Plan p,int x,int z,int top,boolean large,int sea){
        int width=large?5:3,length=large?15:10;
        for(int dx=0;dx<width;dx++)for(int dz=0;dz<length;dz++)
            p.at(x+dx,top,z-dz,FLOOR);
        for(int dx:new int[]{0,width-1})for(int dz=2;dz<length;dz+=5)
            p.fill(x+dx,sea-8,z-dz,x+dx,top-1,z-dz,LOG);
        if(large)for(int dx=-1;dx<=width;dx++)for(int dz=length-4;dz<length;dz++)
            p.at(x+dx,top,z-dz,FLOOR);
        // Lights occupy the edges; the middle of either pier stays passable.
        for(int dz=2;dz<length;dz+=5)for(int dx:new int[]{0,width-1}){
            p.fill(x+dx,top+1,z-dz,x+dx,top+2,z-dz,Blocks.SPRUCE_FENCE.defaultBlockState());
            p.at(x+dx,top+3,z-dz,Blocks.LANTERN.defaultBlockState());
        }
    }

    /** Four short timber approach sections join the existing three-wide trails to the pier heads. */
    private static void pierApproach(ServerLevel world,Plan p,int centerX,int coast,int top){
        int level=top;
        for(int z=coast+6;z<=coast+9;z++){
            int ground=top;
            for(int dx=-1;dx<=1;dx++)ground=Math.max(ground,surface(world,centerX+dx,z));
            level=Math.min(ground,level+1);
            p.fill(centerX-1,level,z,centerX+1,level,z,FLOOR);
        }
    }

    /** Lantern posts only on empty, solid ground immediately beside an existing path. */
    private static void streetLights(ServerLevel world,Plan p,int x,int from,int to){
        for(int z=from;z<=to;z+=11){
            for(int side:new int[]{2,-2}){
                int xx=x+side,y=surface(world,xx,z);
                if(y<world.getSeaLevel()+1)continue;
                BlockPos base=new BlockPos(xx,y,z);
                BlockState ground=world.getBlockState(base);
                if(ground.is(BlockTags.LOGS)||ground.is(BlockTags.LEAVES)
                    ||ground.isAir()||!ground.getFluidState().isEmpty())continue;
                boolean clear=true;
                for(int h=1;h<=4;h++)if(!world.getBlockState(base.above(h)).isAir()){
                    clear=false;
                    break;
                }
                if(!clear)continue;
                p.fill(xx,y+1,z,xx,y+3,z,Blocks.SPRUCE_FENCE.defaultBlockState());
                p.at(xx,y+4,z,Blocks.LANTERN.defaultBlockState());
                break;
            }
        }
    }

    private static void trail(ServerLevel world,Plan p,int x,int from,int to){
        int inc=from<=to?1:-1;
        for(int z=from;z!=to+inc;z+=inc){
            for(int dx=-1;dx<=1;dx++){
                int xx=x+dx;
                int y=surface(world,xx,z);
                if(y<world.getSeaLevel()-2)continue;
                p.at(xx,y,z,Blocks.COARSE_DIRT.defaultBlockState());
                for(int dy=1;dy<=3;dy++)
                    p.at(xx,y+dy,z,AIR);
            }
        }
    }

    /** Runs only after the existing nature pass is completed; never rebuilds terrain. */
    static boolean placeOnce(ServerLevel world,BuyanState.Site site){
        Record records=Record.get(world);
        BlockPos anchor=new BlockPos(site.x(),0,site.z());
        if(records.built.contains(anchor.asLong()))return true;
        if(BuyanState.get(world).active || BuyanNatureState.get(world).active
            || !BuyanNatureState.get(world).finishedAt(site))return false;

        Plan plan=new Plan();
        int sea=site.waterLevel();
        int coast=shore(world,site,0);
        int centerZ=coast+35;
        int[] offsets={-33,-9,16};
        int[] widths={12,10,16},depths={10,8,11},heights={4,4,5};
        String[] types={"house","market","warehouse"};
        for(int i=0;i<3;i++){
            int x=site.x()+offsets[i],z=centerZ;
            int ground=surface(world,x+widths[i]/2,z+depths[i]/2);
            if(ground<sea+1||ground>sea+24)return false;
            building(plan,x,z,ground+1,widths[i],depths[i],heights[i],types[i]);
            trail(world,plan,x+widths[i]/2,coast+8,z-2);
            streetLights(world,plan,x+widths[i]/2,coast+12,z-9);
        }
        pier(plan,site.x()-17,coast+5,sea+2,true,sea);
        pier(plan,site.x()+22,coast+5,sea+2,false,sea);
        trail(world,plan,site.x()-15,coast+7,centerZ-2);
        trail(world,plan,site.x()+23,coast+7,centerZ-2);
        streetLights(world,plan,site.x()-15,coast+12,centerZ-9);
        streetLights(world,plan,site.x()+23,coast+12,centerZ-9);
        pierApproach(world,plan,site.x()-15,coast,sea+2);
        pierApproach(world,plan,site.x()+23,coast,sea+2);

        Set<Long> loaded=new HashSet<>();
        for(var entry:plan.blocks.entrySet()){
            BlockPos pos=entry.getKey();
            if(pos.getY()<=world.getMinBuildHeight()||pos.getY()>=world.getMaxBuildHeight()-1
                ||!world.getWorldBorder().isWithinBounds(pos))return false;
            if(loaded.add(net.minecraft.world.level.ChunkPos.asLong(pos.getX()>>4,pos.getZ()>>4)))
                world.getChunk(pos.getX()>>4,pos.getZ()>>4);
        }
        for(var entry:plan.blocks.entrySet()){
            BlockState old=world.getBlockState(entry.getKey());
            if(!BuyanGenerator.canReplace(old))return false;
        }
        for(var entry:plan.blocks.entrySet())
            world.setBlock(entry.getKey(),entry.getValue(),2);
        records.done(anchor);
        return true;
    }

    public static int generate(CommandSourceStack source){
        ServerLevel world=source.getLevel();
        int x=(int)Math.floor(source.getPosition().x);
        int z=(int)Math.floor(source.getPosition().z);
        BuyanState.Site site=BuyanNatureGenerator.selectedFoundation(world,x,z);
        if(site==null||!BuyanNatureState.get(world).finishedAt(site)){
            source.sendFailure(Component.literal("Буян: сначала завершите основание и природу."));
            return 0;
        }
        boolean ok=placeOnce(world,site);
        if(!ok)source.sendFailure(Component.literal("Порт не размещён: участок занят или берег неподходящий; ничего не изменено."));
        else source.sendSuccess(()->Component.literal("Буян: портовые дома, интерьер, склад, лавка и две пристани проверены/созданы."),true);
        return ok?1:0;
    }
}
