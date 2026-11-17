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
import net.minecraft.world.level.block.Rotation;
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

    /** Port-space coordinates: X follows the coast, +Z points toward inland. */
    private static final class Plan {
        final Map<BlockPos,BlockState> blocks=new LinkedHashMap<>();
        final int ox,oz;
        final Rotation rotation;
        Plan(int ox,int oz,Rotation rotation){
            this.ox=ox;this.oz=oz;this.rotation=rotation;
        }
        BlockPos world(int x,int y,int z){
            return switch(rotation){
                case NONE->new BlockPos(ox+x,y,oz+z);
                case CLOCKWISE_90->new BlockPos(ox-z,y,oz+x);
                case CLOCKWISE_180->new BlockPos(ox-x,y,oz-z);
                case COUNTERCLOCKWISE_90->new BlockPos(ox+z,y,oz-x);
            };
        }
        void at(int x,int y,int z,BlockState state){
            BlockPos pos=world(x,y,z);
            blocks.remove(pos);
            blocks.put(pos,state.rotate(rotation));
        }
        void fill(int x1,int y1,int z1,int x2,int y2,int z2,BlockState b){
            for(int x=x1;x<=x2;x++)for(int z=z1;z<=z2;z++)for(int y=y1;y<=y2;y++)at(x,y,z,b);
        }
        void putLocal(int cx,int floor,int cz,int x,int y,int z,BlockState b){
            at(cx+x,floor+y,cz+z,b);
        }
    }

    /** Derived entirely from the existing island generator, never from another island. */
    private static final class Coast {
        final Plan plan;
        final BuyanState.Site site;
        final long seed;
        final Map<Integer,Integer> shore=new HashMap<>();
        int base;
        Coast(Plan plan,BuyanState.Site site,long seed){
            this.plan=plan;this.site=site;this.seed=seed;
        }
        int land(int x,int z){
            BlockPos pos=plan.world(x,0,z);
            return BuyanTerrain.top(seed,site.x(),site.z(),site.radius(),
                pos.getX(),pos.getZ(),site.waterLevel());
        }
        int shore(int x){
            return shore.computeIfAbsent(x,u->{
                for(int z=-site.radius()-12;z<=0;z++)
                    if(land(u,z)!=Integer.MIN_VALUE)return z;
                return Integer.MAX_VALUE;
            });
        }
        int score(){
            int min=Integer.MAX_VALUE,max=Integer.MIN_VALUE;
            for(int u=-52;u<=52;u+=2){
                int z=shore(u);
                if(z==Integer.MAX_VALUE)return Integer.MAX_VALUE;
                min=Math.min(min,z);
                max=Math.max(max,z);
            }
            base=max;
            return max-min;
        }
    }

    private static Coast chooseCoast(ServerLevel world,BuyanState.Site site){
        Coast selected=null;
        int best=Integer.MAX_VALUE;
        for(Rotation rotation:Rotation.values())for(int shift:new int[]{0,-24,24,-48,48}){
            Plan origin=new Plan(site.x(),site.z(),rotation);
            BlockPos displaced=origin.world(shift,0,0);
            Coast coast=new Coast(new Plan(displaced.getX(),displaced.getZ(),rotation),site,world.getSeed());
            int score=coast.score();
            // A safe usable shore section has no extreme 90-block-wide inlet.
            if(score<best && score<=24){best=score;selected=coast;}
        }
        return selected;
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
        }else if(type.equals("warehouse")){
            p.fill(x+door-1,floor,z-2,x+door+2,floor,z-1,FLOOR);
            for(int dz=4;dz<depth-2;dz+=2){
                p.putLocal(x,floor,z,width-3,1,dz,Blocks.BARREL.defaultBlockState());
                p.putLocal(x,floor,z,2,1,dz,Blocks.CHEST.defaultBlockState());
            }
        }else if(type.equals("smokehouse")){
            p.putLocal(x,floor,z,width-3,1,2,Blocks.SMOKER.defaultBlockState());
            p.putLocal(x,floor,z,width-3,1,3,Blocks.SMOKER.defaultBlockState());
            p.putLocal(x,floor,z,2,1,depth-3,Blocks.BARREL.defaultBlockState());
            // The solid roof remains sealed beneath an exterior smoking chimney.
            int chimneyX=width-2,chimneyZ=depth-3;
            int rise=Math.min(chimneyX+1,width-chimneyX)/2;
            int roofY=floor+height+1+rise;
            p.fill(x+chimneyX,roofY+1,z+chimneyZ,
                x+chimneyX,roofY+2,z+chimneyZ,Blocks.COBBLESTONE.defaultBlockState());
            p.at(x+chimneyX,roofY+3,z+chimneyZ,Blocks.CAMPFIRE.defaultBlockState());
        }else if(type.equals("workshop")){
            p.putLocal(x,floor,z,width-3,1,2,Blocks.SMITHING_TABLE.defaultBlockState());
            p.putLocal(x,floor,z,width-3,1,depth-3,Blocks.BARREL.defaultBlockState());
            p.putLocal(x,floor,z,2,1,depth-4,Blocks.LOOM.defaultBlockState());
            p.putLocal(x,floor,z,3,1,depth-4,Blocks.SPRUCE_SLAB.defaultBlockState());
        }else if(type.equals("elder")){
            p.putLocal(x,floor,z,width-3,1,3,Blocks.LECTERN.defaultBlockState());
            p.putLocal(x,floor,z,width-3,1,depth-3,Blocks.BOOKSHELF.defaultBlockState());
            p.putLocal(x,floor,z,3,1,depth-3,Blocks.CHEST.defaultBlockState());
            p.putLocal(x,floor,z,width-4,1,depth-4,Blocks.SPRUCE_STAIRS.defaultBlockState());
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

    private static BlockState stone(int x,int z){
        return switch(Math.floorMod(x*37+z*19,19)){
            case 0,1->Blocks.MOSSY_COBBLESTONE.defaultBlockState();
            case 2,3,4->Blocks.ANDESITE.defaultBlockState();
            case 5->Blocks.STONE_BRICKS.defaultBlockState();
            case 6->Blocks.STONE.defaultBlockState();
            default->Blocks.COBBLESTONE.defaultBlockState();
        };
    }

    private static int ground(Coast coast,int x,int z){
        return coast.land(x,z);
    }

    private static void paving(Coast coast,Plan p,int x,int z,boolean square){
        int y=ground(coast,x,z);
        if(y==Integer.MIN_VALUE)return;
        int n=Math.floorMod(x*29+z*43,17);
        BlockState surface=square
            ?(n<8?Blocks.COBBLESTONE.defaultBlockState()
                :n<12?Blocks.ANDESITE.defaultBlockState()
                :n==12?Blocks.MOSSY_COBBLESTONE.defaultBlockState()
                :Blocks.COARSE_DIRT.defaultBlockState())
            :(n<6?Blocks.COARSE_DIRT.defaultBlockState()
                :n<12?Blocks.GRAVEL.defaultBlockState():Blocks.DIRT_PATH.defaultBlockState());
        p.at(x,y,z,surface);
        for(int h=1;h<=3;h++)p.at(x,y+h,z,AIR);
    }

    /** Road segments lie on real terrain rather than on an artificial flat plane. */
    private static void road(Coast coast,Plan p,int x1,int z1,int x2,int z2,int width){
        if(x1!=x2&&z1!=z2)throw new IllegalArgumentException("Only straight roads");
        if(x1==x2){
            for(int z=Math.min(z1,z2);z<=Math.max(z1,z2);z++)
                for(int dx=-(width/2);dx<=width/2;dx++)paving(coast,p,x1+dx,z,false);
        }else{
            for(int x=Math.min(x1,x2);x<=Math.max(x1,x2);x++)
                for(int dz=-(width/2);dz<=width/2;dz++)paving(coast,p,x,z1+dz,false);
        }
    }

    /** Irregular 84-block waterfront, retaining masonry backed by timber decking. */
    private static void waterfront(Coast coast,Plan p,int sea){
        for(int x=-42;x<=41;x++){
            int shore=coast.shore(x);
            int wall=shore-1;
            for(int y=sea-3;y<=sea+2;y++)p.at(x,y,wall,stone(x,y));
            for(int z=shore;z<=shore+6;z++){
                int land=ground(coast,x,z);
                if(land==Integer.MIN_VALUE)continue;
                p.at(x,sea+3,z,FLOOR);
                for(int h=1;h<=3;h++)p.at(x,sea+3+h,z,AIR);
                if(z==shore||z==shore+5){
                    // Both ends of every short timber strip are structurally supported.
                    p.fill(x,sea-3,z,x,sea+2,z,LOG);
                }
            }
        }
        // Localized upper terraces, linked to the natural higher shore.
        for(int x=9;x<=27;x++)for(int z=coast.base+11;z<=coast.base+18;z++)
            paving(coast,p,x,z,true);
        for(int x=-39;x<=-27;x++)for(int z=coast.base+11;z<=coast.base+17;z++)
            paving(coast,p,x,z,true);
    }

    private static void pier(Coast coast,Plan p,int center,int width,int length,int sea,boolean terminal){
        int shore=coast.shore(center);
        int top=sea+3;
        for(int dx=-(width/2);dx<=width/2;dx++)
            for(int dz=0;dz<length;dz++)
                p.at(center+dx,top,shore+4-dz,FLOOR);
        for(int dx:new int[]{-(width/2),width/2})for(int dz=2;dz<length;dz+=4){
            int x=center+dx,z=shore+4-dz;
            p.fill(x,sea-6,z,x,top-1,z,LOG);
            p.fill(x,top+1,z,x,top+2,z,Blocks.SPRUCE_FENCE.defaultBlockState());
            p.at(x,top+3,z,Blocks.LANTERN.defaultBlockState());
        }
        if(terminal){
            for(int dx=-(width/2)-1;dx<=width/2+1;dx++)
                for(int dz=length-4;dz<length;dz++)
                    p.at(center+dx,top,shore+4-dz,FLOOR);
        }
        // Mooring bollards on edges; the middle three blocks remain unobstructed.
        for(int dx:new int[]{-(width/2),width/2})
            p.at(center+dx,top+1,shore+3-length,LOG);
    }

    /** The two cargo pads flank the unobstructed 5-wide main street. */
    private static void cargoPad(Coast c,Plan p,int x,int z,int width,int depth,boolean fish){
        for(int dx=0;dx<width;dx++)for(int dz=0;dz<depth;dz++)
            paving(c,p,x+dx,z+dz,dx%3==0);
        int y=ground(c,x,z)+1;
        p.at(x,y,z,Blocks.BARREL.defaultBlockState());
        p.at(x+width-1,ground(c,x+width-1,z)+1,z,Blocks.BARREL.defaultBlockState());
        p.at(x+1,ground(c,x+1,z+depth-2)+1,z+depth-2,Blocks.CHEST.defaultBlockState());
        p.at(x+width-2,ground(c,x+width-2,z+depth-2)+1,z+depth-2,Blocks.SPRUCE_PLANKS.defaultBlockState());
        p.at(x+width-2,ground(c,x+width-2,z+depth-2)+2,z+depth-2,Blocks.SPRUCE_SLAB.defaultBlockState());
        if(fish){
            for(int t=0;t<=2;t+=2){
                int u=x+width-1,v=z+2+t,level=ground(c,u,v);
                p.fill(u,level+1,v,u,level+3,v,Blocks.SPRUCE_FENCE.defaultBlockState());
                p.at(u,level+4,v,Blocks.SPRUCE_SLAB.defaultBlockState());
            }
        }else{
            int[] xs={x+1,x+4};
            for(int u:xs){
                int v=z+depth-3,level=ground(c,u,v);
                p.fill(u,level+1,v,u,level+3,v,LOG);
                p.at(u,level+4,v,Blocks.SPRUCE_SLAB.defaultBlockState());
            }
        }
    }

    private static void post(Coast c,Plan p,int x,int z){
        int floor=ground(c,x,z);
        if(floor==Integer.MIN_VALUE)return;
        for(int h=1;h<=4;h++)p.at(x,floor+h,z,AIR);
        p.fill(x,floor+1,z,x,floor+3,z,Blocks.SPRUCE_FENCE.defaultBlockState());
        p.at(x,floor+4,z,Blocks.LANTERN.defaultBlockState());
    }

    private static void stall(Coast c,Plan p,int x,int z,boolean fish){
        for(int dx=0;dx<=2;dx+=2)for(int dz=0;dz<=2;dz+=2){
            int y=ground(c,x+dx,z+dz);
            p.fill(x+dx,y+1,z+dz,x+dx,y+3,z+dz,Blocks.SPRUCE_FENCE.defaultBlockState());
        }
        for(int dx=0;dx<=2;dx++)for(int dz=0;dz<=2;dz++){
            int y=ground(c,x+dx,z+dz);
            p.at(x+dx,y+4,z+dz,Blocks.SPRUCE_SLAB.defaultBlockState());
        }
        int y=ground(c,x+1,z+1);
        p.at(x+1,y+1,z+1,fish?Blocks.BARREL.defaultBlockState():Blocks.CHEST.defaultBlockState());
    }

    private static void well(Coast c,Plan p,int x,int z){
        int level=ground(c,x,z);
        for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++)
            if(dx!=0||dz!=0)p.at(x+dx,level+1,z+dz,stone(x+dx,z+dz));
        p.at(x,level,z,Blocks.WATER.defaultBlockState());
        for(int dx:new int[]{-1,1})for(int dz:new int[]{-1,1})
            p.fill(x+dx,level+2,z+dz,x+dx,level+3,z+dz,Blocks.SPRUCE_FENCE.defaultBlockState());
        p.fill(x-1,level+4,z-1,x+1,level+4,z+1,Blocks.DARK_OAK_SLAB.defaultBlockState());
    }

    private static void marketSquare(Coast c,Plan p){
        int base=c.base;
        for(int x=-12;x<=11;x++)for(int z=base+39;z<=base+60;z++)
            paving(c,p,x,z,x>=-6&&x<=5&&z>=base+44&&z<=base+53);
        // The water well is west of the movement axis, not in its middle.
        well(c,p,-6,base+49);
        stall(c,p,-11,base+43,true);
        stall(c,p,-11,base+54,true);
        stall(c,p,9,base+49,false);
        for(int x:new int[]{-10,10})for(int z:new int[]{base+40,base+57})
            post(c,p,x,z);
        for(int[] bench:new int[][]{{-4,base+58},{9,base+58},{-10,base+51}}){
            int y=ground(c,bench[0],bench[1]);
            p.at(bench[0],y+1,bench[1],Blocks.SPRUCE_STAIRS.defaultBlockState());
        }
        for(int[] load:new int[][]{{-11,base+59},{11,base+57},{10,base+42}}){
            int y=ground(c,load[0],load[1]);
            p.at(load[0],y+1,load[1],Blocks.BARREL.defaultBlockState());
        }
    }

    private static void fishingYard(Coast c,Plan p){
        int z=c.base+19;
        for(int x=-40;x<=-31;x+=4){
            int level=ground(c,x,z);
            p.fill(x,level+1,z,x,level+3,z,Blocks.SPRUCE_FENCE.defaultBlockState());
            p.at(x,level+4,z,Blocks.SPRUCE_SLAB.defaultBlockState());
        }
        for(int x:new int[]{-48,-46,-44}){
            int v=c.base+35,level=ground(c,x,v);
            p.at(x,level+1,v,LOG);
        }
    }

    private static void boatYard(Coast c,Plan p){
        for(int x:new int[]{43,47,51}){
            int z=c.base+36,level=ground(c,x,z);
            p.at(x,level+1,z,LOG);
            p.at(x,level+2,z,Blocks.SPRUCE_SLAB.defaultBlockState());
        }
        // Compact repair slip aimed toward the water rather than a new large pier.
        for(int x=46;x<=48;x++)for(int z=c.base+15;z<=c.base+21;z++){
            int level=ground(c,x,z);
            p.at(x,level,z,FLOOR);
            if(x!=47)p.at(x,level+1,z,Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState());
        }
    }

    private static boolean siteAvailable(Coast coast,int x,int z,int width,int depth,int sea){
        int min=Integer.MAX_VALUE,max=Integer.MIN_VALUE;
        for(int dx=0;dx<width;dx++)for(int dz=0;dz<depth;dz++){
            int y=ground(coast,x+dx,z+dz);
            if(y==Integer.MIN_VALUE)return false;
            min=Math.min(min,y);max=Math.max(max,y);
        }
        return min>=sea+2&&max<=sea+9&&max-min<=2;
    }

    /** Runs only after the existing nature pass is completed; never rebuilds terrain. */
    static boolean placeOnce(ServerLevel world,BuyanState.Site site){
        Record records=Record.get(world);
        BlockPos anchor=new BlockPos(site.x(),0,site.z());
        if(records.built.contains(anchor.asLong()))return true;
        if(BuyanState.get(world).active || BuyanNatureState.get(world).active
            || !BuyanNatureState.get(world).finishedAt(site))return false;

        Coast coast=chooseCoast(world,site);
        if(coast==null)return false;
        Plan plan=coast.plan;
        int sea=site.waterLevel(),base=coast.base;
        // Eight capital buildings in four zones; the original three count in the total.
        int[][] buildings={
            {-48,base+63,12,10,4}, // Fishing home 1: residential line
            {-30,base+77,10,8,4},  // Fishing home 2: offset farther inland
            {25,base+75,11,8,4},   // Fishing home 3: opposite side of the street
            {-29,base+41,10,8,4},  // Merchant shop left of the square
            {25,base+41,14,10,5},  // Cargo warehouse right of the square
            {-48,base+27,8,7,4},   // Fish smoker at fishing yard
            {42,base+24,11,9,4},   // Boatwright workshop at service slip
            {-5,base+79,11,9,4}    // Harbor elder house at back of the square
        };
        String[] types={"house","house","house","market","warehouse","smokehouse","workshop","elder"};
        for(int i=0;i<buildings.length;i++){
            int[] b=buildings[i];
            if(!siteAvailable(coast,b[0],b[1],b[2],b[3],sea))return false;
        }

        // Roads and quays are created before houses, so they cannot erase furniture.
        waterfront(coast,plan,sea);
        pier(coast,plan,17,5,18,sea,true);
        pier(coast,plan,-33,3,12,sea,false);
        pier(coast,plan,44,3,6,sea,false);
        road(coast,plan,5,base+9,5,base+76,5);
        road(coast,plan,5,base+35,-25,base+35,3);
        road(coast,plan,-25,base+35,-25,base+40,3);
        road(coast,plan,5,base+35,32,base+35,3);
        road(coast,plan,32,base+35,32,base+40,3);
        road(coast,plan,17,coast.shore(17)+7,17,base+18,3);
        road(coast,plan,-33,coast.shore(-33)+7,-33,base+19,3);
        road(coast,plan,44,coast.shore(44)+7,44,base+23,3);
        road(coast,plan,-33,base+23,-45,base+23,3);
        road(coast,plan,-45,base+23,-45,base+26,3);
        road(coast,plan,5,base+58,-43,base+58,3);
        road(coast,plan,-43,base+58,-43,base+62,3);
        road(coast,plan,5,base+73,-26,base+73,3);
        road(coast,plan,-26,base+73,-26,base+76,3);
        road(coast,plan,5,base+71,30,base+71,3);
        road(coast,plan,30,base+71,30,base+74,3);
        road(coast,plan,5,base+75,0,base+75,3);
        road(coast,plan,0,base+75,0,base+78,3);
        road(coast,plan,44,base+22,46,base+22,3);
        road(coast,plan,46,base+22,46,base+23,3);

        cargoPad(coast,plan,13,base+18,10,12,false);
        cargoPad(coast,plan,-34,base+17,6,8,true);
        marketSquare(coast,plan);
        fishingYard(coast,plan);
        boatYard(coast,plan);
        for(int i=0;i<buildings.length;i++){
            int[] b=buildings[i];
            int floor=Integer.MIN_VALUE;
            for(int dx=0;dx<b[2];dx++)for(int dz=0;dz<b[3];dz++)
                floor=Math.max(floor,ground(coast,b[0]+dx,b[1]+dz));
            building(plan,b[0],b[1],floor+1,b[2],b[3],b[4],types[i]);
        }
        // Clearly visible lanterns for approach paths and working docks.
        for(int z:new int[]{base+13,base+28,base+66,base+73})post(coast,plan,10,z);
        for(int[] lamp:new int[][]{{-38,base+28},{-35,base+11},{20,base+14},{47,base+14}})
            post(coast,plan,lamp[0],lamp[1]);

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
        else source.sendSuccess(()->Component.literal("Буян: торгово-рыбацкий порт, 8 зданий, площадь, набережная и 3 причала проверены/созданы."),true);
        return ok?1:0;
    }
}
