package org.slavicmyths.buyan;

import com.mojang.logging.LogUtils;
import java.util.*;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * Central Buyan sanctuary, based on make_city_core.py.
 * Prototype Z (height) becomes Minecraft Y; prototype Y becomes Minecraft Z.
 * Deliberately leaves the future Alatyr square without an Alatyr stone.
 */
public final class BuyanShrine {
    private BuyanShrine() {}
    private static final String SAVE = "slavicmyths_buyan_shrine_2622";
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    private static final BlockState WHITE = Blocks.QUARTZ_BRICKS.defaultBlockState();
    private static final BlockState IVORY = Blocks.SMOOTH_QUARTZ.defaultBlockState();
    private static final BlockState STONE = Blocks.STONE_BRICKS.defaultBlockState();
    private static final BlockState GRAY = Blocks.POLISHED_ANDESITE.defaultBlockState();
    private static final BlockState RED = Blocks.RED_TERRACOTTA.defaultBlockState();
    private static final BlockState BLUE = Blocks.BLUE_TERRACOTTA.defaultBlockState();
    private static final BlockState GOLD = Blocks.YELLOW_GLAZED_TERRACOTTA.defaultBlockState();
    private static final BlockState WOOD = Blocks.OAK_WOOD.defaultBlockState();
    private static final BlockState BARK = Blocks.DARK_OAK_WOOD.defaultBlockState();
    private static final BlockState CHAIN = Blocks.CHAIN.defaultBlockState();

    private static long key(BuyanState.Site s) {
        return new BlockPos(s.x(),0,s.z()).asLong();
    }
    private static final class Record extends SavedData {
        final Map<Long,Long> built = new LinkedHashMap<>();
        static Record get(ServerLevel level) {
            return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(Record::new,Record::load),SAVE);
        }
        static Record load(CompoundTag tag,HolderLookup.Provider registries) {
            Record result=new Record();
            long[] keys=tag.getLongArray("Sites"),values=tag.getLongArray("Locations");
            for(int i=0;i<Math.min(keys.length,values.length);i++)
                result.built.put(keys[i],values[i]);
            return result;
        }
        @Override public CompoundTag save(CompoundTag tag,HolderLookup.Provider registries) {
            long[] keys=new long[built.size()],values=new long[built.size()];
            int i=0;
            for(var e:built.entrySet()) {
                keys[i]=e.getKey();values[i]=e.getValue();i++;
            }
            tag.putLongArray("Sites",keys);
            tag.putLongArray("Locations",values);
            return tag;
        }
        void mark(long k,BlockPos p) {built.put(k,p.asLong());setDirty();}
    }
    private static final class Plan {
        final ServerLevel world;
        final BuyanState.Site site;
        final int ox,oz;
        final Rotation rotation;
        final Map<BlockPos,BlockState> blocks=new LinkedHashMap<>();
        final long seed;
        int base, shrineFloor, oakFloor;
        Plan(ServerLevel world,BuyanState.Site site,int x,int z,Rotation rotation,long seed) {
            this.world=world;this.site=site;ox=x;oz=z;this.rotation=rotation;
            this.seed=seed;
        }
        BlockPos horizontal(int x,int z) {
            return switch(rotation) {
                case NONE -> new BlockPos(ox+x,0,oz+z);
                case CLOCKWISE_90 -> new BlockPos(ox-z,0,oz+x);
                case CLOCKWISE_180 -> new BlockPos(ox-x,0,oz-z);
                case COUNTERCLOCKWISE_90 -> new BlockPos(ox+z,0,oz-x);
            };
        }
        private static int outside(int x,int z,int left,int right,int near,int far) {
            return Math.max(Math.max(Math.max(left-x,x-right),0),
                Math.max(Math.max(near-z,z-far),0));
        }
        // Each monument remains level. The approaches rise at most one
        // block per step toward each monument instead of levelling 129x81 land.
        int floor(int x,int z) {
            int natural=ground(x,z)+1;
            return Math.max(natural,Math.max(
                shrineFloor-outside(x,z,-52,-17,-6,20),
                oakFloor-outside(x,z,0,58,-20,37)));
        }
        BlockPos p(int x,int z,int h) {
            BlockPos point=horizontal(x,z);
            return new BlockPos(point.getX(),floor(x,z)+h,point.getZ());
        }
        void at(int x,int z,int h,BlockState state) {
            blocks.put(p(x,z,h),state.rotate(rotation));
        }
        void absolute(int x,int y,int z,BlockState state) {
            blocks.put(new BlockPos(x,y,z),state);
        }
        void box(int x0,int x1,int z0,int z1,int h0,int h1,BlockState state) {
            for(int x=x0;x<=x1;x++)for(int z=z0;z<=z1;z++)
                for(int h=h0;h<=h1;h++)at(x,z,h,state);
        }
        int top(int x,int z) {
            return BuyanNature.top(seed,site.x(),site.z(),site.radius(),
                x,z,site.waterLevel());
        }
        int ground(int x,int z) {
            BlockPos point=horizontal(x,z);
            return top(point.getX(),point.getZ());
        }
        boolean fits() {
            int lo=Integer.MAX_VALUE,hi=Integer.MIN_VALUE;
            for(int x=-64;x<=64;x+=8)for(int z=-40;z<=40;z+=8) {
                int y=ground(x,z);
                if(y==Integer.MIN_VALUE||y<site.waterLevel()+7)return false;
                lo=Math.min(lo,y);hi=Math.max(hi,y);
            }
            // The full precinct may follow a hill; the individual shrine
            // and oak foundations must be substantially more level.
            if(hi-lo>30)return false;
            // Keep the future central Alatyr plaza entirely free; it is
            // not the sanctuary's ritual ring and has its own city plan.
            for(int x=-64;x<=64;x+=4)for(int z=-40;z<=40;z+=4) {
                BlockPos wp=horizontal(x,z);
                if(Math.abs(wp.getX()-site.x())<=35
                    &&wp.getZ()-site.z()>=-76&&wp.getZ()-site.z()<=-24)
                    return false;
            }
            int sLow=Integer.MAX_VALUE,sHigh=Integer.MIN_VALUE;
            for(int x=-49;x<=-19;x++)for(int z=-5;z<=18;z++) {
                int y=ground(x,z);
                if(y==Integer.MIN_VALUE||y<site.waterLevel()+7)return false;
                sLow=Math.min(sLow,y);sHigh=Math.max(sHigh,y);
            }
            int oLow=Integer.MAX_VALUE,oHigh=Integer.MIN_VALUE;
            for(int x=3;x<=55;x++)for(int z=-18;z<=34;z++) {
                int y=ground(x,z);
                if(y==Integer.MIN_VALUE||y<site.waterLevel()+7)return false;
                oLow=Math.min(oLow,y);oHigh=Math.max(oHigh,y);
            }
            if(sHigh-sLow>8||oHigh-oLow>12
                ||Math.max(sHigh,oHigh)+44>=world.getMaxBuildHeight())return false;
            shrineFloor=sHigh+1;oakFloor=oHigh+1;
            base=ground(0,0)+1;
            return true;
        }
        void pavement(int x,int z,BlockState tile) {
            int g=ground(x,z),level=floor(x,z);
            if(g==Integer.MIN_VALUE||g>=level)
                throw new IllegalStateException("Shrine floor crosses natural ground");
            for(int y=g+1;y<level;y++)
                at(x,z,y-level,y<level-3?STONE:GRAY);
            at(x,z,0,tile);
            for(int h=1;h<=3;h++)at(x,z,h,AIR);
        }
        void path(int x0,int z0,int x1,int z1) {
            if(x0!=x1&&z0!=z1)throw new IllegalArgumentException("Diagonal road");
            int n=Math.max(Math.abs(x1-x0),Math.abs(z1-z0));
            for(int i=0;i<=n;i++) {
                int x=x0+i*Integer.signum(x1-x0),z=z0+i*Integer.signum(z1-z0);
                for(int w=-1;w<=1;w++)
                    pavement(x0==x1?x+w:x,z0==z1?z+w:z,w==0?BLUE:IVORY);
            }
        }
        void exteriorTile(int x,int z,int y,BlockState tile) {
            int g=top(x,z);
            if(g==Integer.MIN_VALUE||g<site.waterLevel()+5||y<=g)
                throw new IllegalStateException("Sanctuary approach crosses coast or slope");
            for(int h=g+1;h<y;h++)absolute(x,h,z,STONE);
            absolute(x,y,z,tile);
            for(int h=y+1;h<=y+3;h++)absolute(x,h,z,AIR);
        }
        void street(int x0,int z0,int x1,int z1) {
            if(x0!=x1&&z0!=z1)
                throw new IllegalArgumentException("Sanctuary road is not axis aligned");
            int n=Math.max(Math.abs(x1-x0),Math.abs(z1-z0));
            int last=Integer.MIN_VALUE;
            for(int i=0;i<=n;i++) {
                int x=x0+i*Integer.signum(x1-x0);
                int z=z0+i*Integer.signum(z1-z0);
                int h=top(x,z);
                if(h==Integer.MIN_VALUE||h<site.waterLevel()+5
                    ||last!=Integer.MIN_VALUE&&Math.abs(h-last)>1)
                    throw new IllegalStateException("Sanctuary road cannot follow safe walkable grade");
                last=h;
                for(int w=-1;w<=1;w++) {
                    int wx=x0==x1?x+w:x,wz=x0==x1?z:z+w;
                    exteriorTile(wx,wz,top(wx,wz)+1,w==0?GRAY:IVORY);
                }
            }
        }
        void roof(int x0,int x1,int z0,int z1,int y0,BlockState main,BlockState alt) {
            for(int x=x0;x<=x1;x++) {
                int y=y0+Math.min(x-x0,x1-x);
                for(int z=z0;z<=z1;z++) {
                    at(x,z,y,Math.floorMod(z-z0,6)<2?alt:main);
                    if(z==z0||z==z1)at(x,z,y+1,GOLD);
                }
                BlockState stair=Blocks.RED_NETHER_BRICK_STAIRS.defaultBlockState()
                    .setValue(StairBlock.FACING,x<(x0+x1)/2?Direction.WEST:Direction.EAST);
                if(x!=x0&&x!=x1) {
                    at(x,z0-1,y,stair);at(x,z1+1,y,stair);
                }
            }
        }
        void door(int x,int z,int h,DoorHingeSide hinge) {
            BlockState state=Blocks.DARK_OAK_DOOR.defaultBlockState()
                .setValue(DoorBlock.FACING,Direction.NORTH)
                .setValue(DoorBlock.HINGE,hinge)
                .setValue(DoorBlock.HALF,DoubleBlockHalf.LOWER);
            at(x,z,h,state);
            at(x,z,h+1,state.setValue(DoorBlock.HALF,DoubleBlockHalf.UPPER));
        }
        void brazier(int x,int z) {
            box(x-1,x+1,z-1,z+1,1,1,GRAY);
            at(x,z,2,Blocks.CAMPFIRE.defaultBlockState());
            at(x-1,z,2,Blocks.IRON_BARS.defaultBlockState());
            at(x+1,z,2,Blocks.IRON_BARS.defaultBlockState());
        }
        void shrine() {
            int x=-34,z=9;
            // Support every plinth tile down to natural ground; no hovering floor.
            for(int dx=-13;dx<=13;dx++)
                for(int dz=-9;dz<=9;dz++)
                    pavement(x+dx,z+dz,GRAY);
            box(x-13,x+13,z-9,z+9,0,0,GRAY);
            box(x-12,x+12,z-8,z+8,1,1,IVORY);
            for(int dx=-10;dx<=10;dx++)for(int dz=-6;dz<=6;dz++)
                for(int h=2;h<=8;h++)
                    at(x+dx,z+dz,h,dx==-10||dx==10||dz==-6||dz==6?WHITE:AIR);
            box(x-12,x-11,z-5,z+5,2,6,WHITE);
            box(x+11,x+12,z-5,z+5,2,6,WHITE);
            for(int dz:new int[]{-7,7})for(int dx:new int[]{-8,-4,4,8}) {
                box(x+dx,x+dx,z+dz,z+dz,2,9,WHITE);
                at(x+dx,z+dz,5,RED);at(x+dx,z+dz,8,GOLD);
            }
            box(x-9,x+9,z-7,z-7,10,10,Blocks.DARK_OAK_LOG.defaultBlockState());
            door(x-1,z-6,2,DoorHingeSide.LEFT);
            door(x,z-6,2,DoorHingeSide.RIGHT);
            box(x-1,x,z-7,z-7,2,4,AIR);
            for(int dx:new int[]{-10,10})for(int dz:new int[]{-3,3}) {
                box(x+dx,x+dx,z+dz,z+dz+1,4,6,
                    Blocks.LIGHT_BLUE_STAINED_GLASS.defaultBlockState());
                at(x+dx,z+dz,7,GOLD);
            }
            for(int dx=-10;dx<=10;dx++) {
                int limit=11+Math.min(dx+11,11-dx);
                for(int h=9;h<limit;h++) {
                    at(x+dx,z-6,h,WHITE);at(x+dx,z+6,h,WHITE);
                }
            }
            for(int dz=-6;dz<=6;dz++)for(int h=9;h<=10;h++) {
                at(x-10,z+dz,h,WHITE);at(x+10,z+dz,h,WHITE);
            }
            roof(x-11,x+11,z-8,z+8,11,RED,BLUE);
            box(x-3,x+3,z-2,z+2,21,26,WHITE);
            // The tower interior starts ABOVE its solid, weatherproof
            // h=22 sill; hollowing at h=22 opened the main roof ridge.
            box(x-2,x+2,z-1,z+1,23,25,AIR);
            at(x,z-2,23,Blocks.YELLOW_STAINED_GLASS.defaultBlockState());
            at(x,z+2,23,Blocks.YELLOW_STAINED_GLASS.defaultBlockState());
            roof(x-4,x+4,z-3,z+3,27,BLUE,RED);
            for(int dx:new int[]{-8,8}) {
                box(x+dx-1,x+dx+1,z-4,z-2,15,17,WHITE);
                roof(x+dx-2,x+dx+2,z-5,z-1,18,RED,BLUE);
            }
            for(int dx=-10;dx<=10;dx++) {
                at(x+dx,z-8,6,GOLD);at(x+dx,z+8,6,GOLD);
            }
            for(int dz=-7;dz<=7;dz++) {
                at(x-11,z+dz,6,GOLD);at(x+11,z+dz,6,GOLD);
            }
            for(int dz=-11;dz<=-8;dz++)for(int dx=-4;dx<=4;dx++) {
                pavement(x+dx,z+dz,GRAY);
                if(dz==-8)at(x+dx,z+dz,1,
                    Blocks.STONE_BRICK_STAIRS.defaultBlockState()
                        .setValue(StairBlock.FACING,Direction.SOUTH));
            }
            // Furnished altar, offerings, benches, chests and hanging lights.
            box(x-3,x+3,z+3,z+5,2,2,GRAY);
            box(x-2,x+2,z+4,z+4,3,3,GOLD);
            for(int dx:new int[]{-2,0,2})
                at(x+dx,z+4,4,Blocks.CANDLE.defaultBlockState()
                    .setValue(BlockStateProperties.LIT,true));
            for(int dx:new int[]{-7,6})for(int dz:new int[]{-2,-1,1,2})
                at(x+dx,z+dz,2,Blocks.SPRUCE_STAIRS.defaultBlockState()
                    .setValue(StairBlock.FACING,dx<0?Direction.EAST:Direction.WEST));
            at(x-7,z+4,2,Blocks.LECTERN.defaultBlockState());
            at(x+7,z+4,2,Blocks.CHEST.defaultBlockState());
            at(x-7,z-4,2,Blocks.BARREL.defaultBlockState());
            for(int dx:new int[]{-7,7}) {
                box(x+dx,x+dx,z-3,z-3,8,14,CHAIN);
                at(x+dx,z-3,7,Blocks.LANTERN.defaultBlockState()
                    .setValue(LanternBlock.HANGING,true));
            }
            // Main roof ridge supports the suspended five-lantern chandelier.
            for(int h=9;h<=21;h++)at(x,z, h,CHAIN);
            at(x,z,8,Blocks.LANTERN.defaultBlockState()
                .setValue(LanternBlock.HANGING,true));
            for(Direction dir:new Direction[]{Direction.NORTH,Direction.SOUTH,
                Direction.EAST,Direction.WEST}) {
                int sx=dir.getStepX(),sz=dir.getStepZ();
                BlockState arm=CHAIN.setValue(ChainBlock.AXIS,
                    sx!=0?Direction.Axis.X:Direction.Axis.Z);
                at(x+sx,z+sz,9,arm);
                at(x+2*sx,z+2*sz,9,arm);
                at(x+2*sx,z+2*sz,8,Blocks.LANTERN.defaultBlockState()
                    .setValue(LanternBlock.HANGING,true));
            }
            // Wall lights near the ritual axis and near the main entrance.
            for(int dx:new int[]{-9,9})for(int dz:new int[]{-4,4}) {
                // Short inner-wall brackets attach each lantern to masonry.
                at(x+dx+Integer.signum(dx),z+dz,9,IVORY);
                at(x+dx,z+dz,9,IVORY);
                for(int h=7;h<=8;h++)at(x+dx,z+dz,h,CHAIN);
                at(x+dx,z+dz,6,Blocks.LANTERN.defaultBlockState()
                    .setValue(LanternBlock.HANGING,true));
            }
            for(int dx:new int[]{-4,4}) {
                at(x+dx,z-6,9,IVORY);
                at(x+dx,z-5,9,IVORY);
                for(int h=7;h<=8;h++)at(x+dx,z-5,h,CHAIN);
                at(x+dx,z-5,6,Blocks.LANTERN.defaultBlockState()
                    .setValue(LanternBlock.HANGING,true));
            }
            brazier(x-6,z-9);brazier(x+6,z-9);
        }
        void root(int x,int z,int h,int r) {
            for(int dx=-r;dx<=r;dx++)for(int dz=-r;dz<=r;dz++)
                if(dx*dx+dz*dz<=r*r)
                    box(x+dx,x+dx,z+dz,z+dz,1,h,
                        Math.floorMod(x+dx+z+dz,6)==0?BARK:WOOD);
        }
        void limb(int x,int z,int h,int r) {
            for(int dx=-r;dx<=r;dx++)for(int dz=-r;dz<=r;dz++)
                for(int dy=-r;dy<=r;dy++)
                    if(dx*dx+dz*dz+dy*dy<(r+.7)*(r+.7))
                        at(x+dx,z+dz,h+dy,WOOD);
        }
        void chain(int x,int z,int h,int tx,int tz,int th) {
            if(!blocks.getOrDefault(p(x,z,h),AIR).is(Blocks.CHAIN))
                at(x,z,h,CHAIN);
            while(x!=tx||z!=tz||h!=th) {
                if(x!=tx) {
                    x+=Integer.signum(tx-x);
                    at(x,z,h,CHAIN.setValue(ChainBlock.AXIS,Direction.Axis.X));
                }
                if(z!=tz) {
                    z+=Integer.signum(tz-z);
                    at(x,z,h,CHAIN.setValue(ChainBlock.AXIS,Direction.Axis.Z));
                }
                if(h!=th) {
                    h+=Integer.signum(th-h);
                    at(x,z,h,CHAIN);
                }
            }
        }
        void oak() {
            int cx=29,cz=8;
            // The enormous sacred-oak podium also needs continuous supports.
            for(int dx=-26;dx<=26;dx++)
                for(int dz=-26;dz<=26;dz++)
                    pavement(cx+dx,cz+dz,GRAY);
            box(cx-26,cx+26,cz-26,cz+26,0,0,GRAY);
            for(int x=-21;x<=21;x++)for(int z=-21;z<=21;z++) {
                double r=Math.hypot(x,z);
                if(r>18&&r<19.5)at(cx+x,cz+z,1,WHITE);
                if(r>16.6&&r<17.8&&Math.floorMod(x+z,3)==0)
                    at(cx+x,cz+z,1,RED);
            }
            box(cx-4,cx+4,cz-4,cz+4,1,12,WOOD);
            box(cx-3,cx+3,cz-3,cz+3,13,21,WOOD);
            box(cx-2,cx+2,cz-2,cz+2,22,25,WOOD);
            for(int[] b:new int[][]{{-4,-2},{-4,2},{4,-2},{4,2},
                {-2,-4},{2,-4},{-2,4},{2,4}})
                box(cx+b[0],cx+b[0],cz+b[1],cz+b[1],3,14,BARK);
            for(int i=0;i<12;i++)for(int j=3;j<18;j++) {
                double a=2*Math.PI*i/12;
                double bend=Math.sin(j*.24+i)*.4;
                int x=cx+(int)Math.round(j*Math.cos(a)-bend*Math.sin(a));
                int z=cz+(int)Math.round(j*Math.sin(a)+bend*Math.cos(a));
                root(x,z,Math.max(1,(int)Math.round(7-j*.36)),
                    Math.max(1,(int)Math.round(3.7-j*.16)));
            }
            for(int i=0;i<9;i++)for(int j=2;j<17;j++) {
                double a=2*Math.PI*i/9;
                limb(cx+(int)Math.round(j*Math.cos(a)),
                    cz+(int)Math.round(j*Math.sin(a)),21+(int)Math.round(j*.32),j<11?2:1);
            }
            Random random=new Random(31);
            int[][] lobes={{0,0,0,13,12,9},{-9,0,-1,12,11,7},
                {9,0,-1,12,11,7},{0,-9,-1,11,12,7},{0,9,-1,11,12,7},
                {-8,8,0,10,10,7},{8,-8,0,10,10,7}};
            for(int[] l:lobes)
                for(int x=l[0]-l[3];x<=l[0]+l[3];x++)
                    for(int z=l[1]-l[4];z<=l[1]+l[4];z++)
                        for(int y=28+l[2]-l[5];y<29+l[2]+l[5];y++) {
                            double d=Math.pow((x-l[0])/(double)l[3],2)
                                +Math.pow((z-l[1])/(double)l[4],2)
                                +Math.pow((y-28-l[2])/(double)l[5],2);
                            if(d<=1&&random.nextDouble()<.91
                                &&!blocks.containsKey(p(cx+x,cz+z,y))) {
                                int n=random.nextInt(5);
                                at(cx+x,cz+z,y,n<2?Blocks.OAK_LEAVES.defaultBlockState():
                                    n<4?Blocks.AZALEA_LEAVES.defaultBlockState():
                                        Blocks.FLOWERING_AZALEA_LEAVES.defaultBlockState());
                            }
                        }
            for(int[] post:new int[][]{{0,-21},{21,0},{0,21},{-21,0}}) {
                int x=cx+post[0],z=cz+post[1];
                box(x-1,x+1,z-1,z+1,1,2,STONE);
                box(x,x,z,z,3,12,WHITE);at(x,z,13,GOLD);
                int tx=cx+(int)Math.round(post[0]*.28);
                int tz=cz+(int)Math.round(post[1]*.28);
                int px=x,pz=z,py=14;
                for(int i=1;i<=32;i++) {
                    double t=i/32.0;
                    int nx=(int)Math.round(x*(1-t)+tx*t);
                    int nz=(int)Math.round(z*(1-t)+tz*t);
                    int ny=(int)Math.round(14*(1-t)+22*t-5*Math.sin(Math.PI*t));
                    chain(px,pz,py,nx,nz,ny);px=nx;pz=nz;py=ny;
                }
            }
            for(int[] hanging:new int[][]{{15,8,27,10},{-14,10,27,9},
                {15,-9,27,12},{-13,-11,27,10},{0,-17,27,10},{0,17,27,9}}) {
                // A solid wooden fixing above every uninterrupted vertical chain.
                at(cx+hanging[0],cz+hanging[1],hanging[2]+1,WOOD);
                for(int i=0;i<hanging[3];i++)
                    at(cx+hanging[0],cz+hanging[1],hanging[2]-i,CHAIN);
                at(cx+hanging[0],cz+hanging[1],hanging[2]-hanging[3],
                    Blocks.LANTERN.defaultBlockState()
                        .setValue(LanternBlock.HANGING,true));
            }
            for(int[] rootLight:new int[][]{{19,0},{-19,0},{0,19},{0,-19}}) {
                at(cx+rootLight[0],cz+rootLight[1],2,
                    Blocks.STONE_BRICK_WALL.defaultBlockState());
                at(cx+rootLight[0],cz+rootLight[1],3,
                    Blocks.LANTERN.defaultBlockState());
            }
            for(int[] b:new int[][]{{15,15},{-15,15},{-15,-15},{15,-15}})
                brazier(cx+b[0],cz+b[1]);
        }
        void fence() {
            for(int x=-64;x<=64;x++)for(int z:new int[]{-40,40}) {
                pavement(x,z,STONE);
                if(z==-40&&x>=-5&&x<=5)continue;
                at(x,z,1,WHITE);
                at(x,z,2,Math.floorMod(x,4)==0?BLUE:WHITE);
                if(Math.floorMod(x,8)==0||Math.abs(x)==64) {
                    box(x,x,z,z,1,4,WHITE);at(x,z,3,RED);at(x,z,5,GOLD);
                }
            }
            for(int z=-40;z<=40;z++)for(int x:new int[]{-64,64}) {
                pavement(x,z,STONE);
                at(x,z,1,WHITE);
                at(x,z,2,Math.floorMod(z,4)==0?RED:WHITE);
                if(Math.floorMod(z,8)==0) {
                    box(x,x,z,z,1,4,WHITE);at(x,z,5,GOLD);
                }
            }
            for(int x:new int[]{-7,7}) {
                box(x-1,x+1,-41,-39,1,5,WHITE);
                box(x-1,x+1,-41,-39,4,4,BLUE);
                at(x,-40,6,GOLD);
                at(x,-40,7,Blocks.LANTERN.defaultBlockState());
            }
            for(int x=-5;x<=5;x++)for(int h=1;h<=5;h++)at(x,-40,h,AIR);
        }
        void courtyard() {
            path(-34,-2,-34,-22);path(-34,-22,0,-22);
            path(0,-22,0,-40);path(0,-22,29,-22);path(29,-22,29,-18);
            for(int x=-14;x<=-5;x+=3)for(int z=21;z<=29;z+=4) {
                pavement(x,z,Blocks.MOSS_BLOCK.defaultBlockState());
                at(x,z,1,x%2==0?Blocks.RED_TULIP.defaultBlockState():
                    Blocks.BLUE_ORCHID.defaultBlockState());
            }
            for(int x=-57;x<=-51;x+=3)for(int z=-30;z<=-22;z+=4) {
                pavement(x,z,Blocks.MOSS_BLOCK.defaultBlockState());
                at(x,z,1,Blocks.CORNFLOWER.defaultBlockState());
            }
            for(int x:new int[]{-56,-12,58})for(int z:new int[]{-32,32}) {
                box(x-1,x+1,z-1,z+1,0,0,GRAY);
                box(x,x,z,z,1,4,WHITE);
                at(x,z,3,BLUE);at(x,z,5,GOLD);
                at(x,z,6,Blocks.LANTERN.defaultBlockState());
            }
            for(int[] b:new int[][]{{-17,-27},{13,-27},{-17,30}})
                brazier(b[0],b[1]);
        }
        void cityRoad() {
            BlockPos gate=p(0,-40,0);
            // Always leave the GATE toward the outside of the fence.
            // Going along its inner side would trap the road behind a wall.
            int dx=0,dz=0;
            switch(rotation) {
                case COUNTERCLOCKWISE_90 -> dx=-1;
                case CLOCKWISE_90 -> dx=1;
                case NONE -> dz=-1;
                case CLOCKWISE_180 -> dz=1;
            }
            int outerX=gate.getX()+3*dx;
            int outerZ=gate.getZ()+3*dz;
            street(gate.getX(),gate.getZ(),outerX,outerZ);

            int side=rotation==Rotation.CLOCKWISE_90?-1:1;
            int bypass=dz==0?outerX:site.x()+41;
            int rim=site.x()+side*34;
            int squareZ=site.z()-54;
            street(outerX,outerZ,bypass,outerZ);
            street(bypass,outerZ,bypass,squareZ);
            street(bypass,squareZ,rim,squareZ);
        }
        void build() {courtyard();shrine();oak();fence();cityRoad();}
        boolean geometryCheck() {
            // Check all 391 main roof positions, not merely whether a map
            // key exists: planned AIR is still a hole in the finished roof.
            for(int x=-11;x<=11;x++)for(int z=-8;z<=8;z++) {
                BlockState roofTile=blocks.get(p(-34+x,9+z,
                    11+Math.min(x+11,11-x)));
                if(roofTile==null||roofTile.isAir())return false;
            }
            BlockState left=blocks.get(p(-35,3,2));
            BlockState right=blocks.get(p(-34,3,2));
            if(left==null||right==null||!left.is(Blocks.DARK_OAK_DOOR)
                ||!right.is(Blocks.DARK_OAK_DOOR)
                ||left.getValue(DoorBlock.HALF)!=DoubleBlockHalf.LOWER
                ||right.getValue(DoorBlock.HALF)!=DoubleBlockHalf.LOWER
                ||left.getValue(DoorBlock.HINGE)!=DoorHingeSide.LEFT
                ||right.getValue(DoorBlock.HINGE)!=DoorHingeSide.RIGHT
                ||!blocks.getOrDefault(p(-35,3,3),AIR).is(Blocks.DARK_OAK_DOOR)
                ||!blocks.getOrDefault(p(-34,3,3),AIR).is(Blocks.DARK_OAK_DOOR))
                return false;
            BlockState step=blocks.getOrDefault(p(-34,1,1),AIR);
            if(!step.is(Blocks.STONE_BRICK_STAIRS)
                ||step.getValue(StairBlock.FACING)!=rotation.rotate(Direction.SOUTH))
                return false;
            if(!blocks.getOrDefault(p(-34,7,8),AIR).is(Blocks.LANTERN))
                return false;
            for(int[] hanging:new int[][]{{15,8,27,10},{-14,10,27,9},
                {15,-9,27,12},{-13,-11,27,10},{0,-17,27,10},{0,17,27,9}}) {
                if(!blocks.getOrDefault(p(29+hanging[0],8+hanging[1],
                    hanging[2]+1),AIR).is(Blocks.OAK_WOOD))return false;
                for(int i=0;i<hanging[3];i++)
                    if(!blocks.getOrDefault(p(29+hanging[0],8+hanging[1],
                        hanging[2]-i),AIR).is(Blocks.CHAIN))return false;
                if(!blocks.getOrDefault(p(29+hanging[0],8+hanging[1],
                    hanging[2]-hanging[3]),AIR).is(Blocks.LANTERN))return false;
            }
            // Both monument foundations and the actual road-connected gate
            // must remain in the finished staged geometry.
            if(!blocks.getOrDefault(p(29,8,20),AIR).is(Blocks.OAK_WOOD)
                ||!blocks.getOrDefault(p(29,-13,13),AIR).is(GOLD.getBlock())
                ||!blocks.getOrDefault(p(-27,13,2),AIR).is(Blocks.CHEST)
                ||!blocks.getOrDefault(p(-34,13,3),AIR).is(GOLD.getBlock())
                ||!blocks.getOrDefault(p(-34,1,1),AIR).is(Blocks.STONE_BRICK_STAIRS)
                ||!blocks.getOrDefault(p(0,-40,1),AIR).isAir()
                ||!blocks.getOrDefault(p(-64,-40,2),AIR).is(WHITE.getBlock())
                ||!blocks.getOrDefault(p(64,-40,2),AIR).is(WHITE.getBlock()))
                return false;
            int chains=0,leaves=0;
            int minX=Integer.MAX_VALUE,maxX=Integer.MIN_VALUE;
            int minZ=Integer.MAX_VALUE,maxZ=Integer.MIN_VALUE;
            for(var entry:blocks.entrySet()) {
                BlockState state=entry.getValue();
                if(state.is(Blocks.CHAIN))chains++;
                if(state.is(Blocks.OAK_LEAVES)||state.is(Blocks.AZALEA_LEAVES)
                    ||state.is(Blocks.FLOWERING_AZALEA_LEAVES)) {
                    leaves++;
                    minX=Math.min(minX,entry.getKey().getX());
                    maxX=Math.max(maxX,entry.getKey().getX());
                    minZ=Math.min(minZ,entry.getKey().getZ());
                    maxZ=Math.max(maxZ,entry.getKey().getZ());
                }
            }
            return chains>=100&&leaves>=1300&&maxX-minX>=40&&maxZ-minZ>=40
                &&!blocks.getOrDefault(p(-34,9,31),AIR).isAir();
        }
        boolean safe() {
            Set<Long> chunks=new HashSet<>();
            for(BlockPos point:blocks.keySet()) {
                if(point.getY()<=world.getMinBuildHeight()
                    ||point.getY()>=world.getMaxBuildHeight()-1
                    ||!world.getWorldBorder().isWithinBounds(point))return false;
                long id=ChunkPos.asLong(point.getX()>>4,point.getZ()>>4);
                if(chunks.add(id))world.getChunk(point.getX()>>4,point.getZ()>>4);
                BlockState old=world.getBlockState(point);
                if(!old.equals(blocks.get(point))&&!BuyanGenerator.canReplace(old))
                    return false;
            }
            // Also check unpaved gardens for existing buildings and tile entities.
            for(int x=-64;x<=64;x+=2)for(int z=-40;z<=40;z+=2) {
                BlockPos point=p(x,z,0);
                int g=top(point.getX(),point.getZ());
                if(g==Integer.MIN_VALUE||g>=floor(x,z))return false;
                for(int y=g+1;y<=floor(x,z)+37;y++)
                    if(!BuyanGenerator.canReplace(world.getBlockState(
                        new BlockPos(point.getX(),y,point.getZ()))))return false;
            }
            return true;
        }
        void commit() {
            List<Map.Entry<BlockPos,BlockState>> changed=new ArrayList<>();
            Map<BlockPos,BlockState> originals=new LinkedHashMap<>();
            for(var e:blocks.entrySet()) {
                BlockState previous=world.getBlockState(e.getKey());
                if(!previous.equals(e.getValue())) {
                    originals.put(e.getKey(),previous);changed.add(e);
                }
            }
            try {
                for(var e:changed)
                    if(!world.setBlock(e.getKey(),e.getValue(),2))
                        throw new IllegalStateException("Cannot place "+e.getKey());
            } catch(RuntimeException error) {
                for(int i=changed.size()-1;i>=0;i--) {
                    BlockPos p=changed.get(i).getKey();
                    world.setBlock(p,originals.get(p),2);
                }
                throw error;
            }
        }
    }
    /**
     * A non-destructive GameTest blueprint on the exact reported crash seed.
     * It checks the staged complex and never modifies the test world's blocks.
     */
    public static void verifyBlueprintFixture(ServerLevel world) {
        BuyanState.Site site=new BuyanState.Site(0,0,350,63);
        Plan plan=new Plan(world,site,80,-8,Rotation.COUNTERCLOCKWISE_90,
            -9047918643626153269L);
        if(!plan.fits())
            throw new IllegalStateException("Shrine fixture: valid inland site rejected");
        plan.build();
        if(!plan.geometryCheck())
            throw new IllegalStateException("Shrine fixture: roof, oak or courtyard incomplete");
        BlockPos roof=plan.p(-45,9,11);
        BlockState original=plan.blocks.put(roof,AIR);
        try {
            if(plan.geometryCheck())
                throw new IllegalStateException("Shrine fixture missed damaged roof");
        } finally {
            plan.blocks.put(roof,original);
        }
        if(!plan.geometryCheck())
            throw new IllegalStateException("Shrine fixture cannot restore roof");
        for(BlockPos point:plan.blocks.keySet())
            if(Math.abs(point.getX())<=3 && Math.abs(point.getZ()+54)<=3)
                throw new IllegalStateException(
                    "Shrine fixture invades reserved Alatyr monument space at "+point);
        LogUtils.getLogger().info(
            "Buyan shrine fixture: {} staged blocks; temple floor {} oak floor {}",
            plan.blocks.size(),plan.shrineFloor,plan.oakFloor);
    }

    /** Existing saves are not silently rebuilt. Explicit operator command is safe. */
    static boolean placeOnce(ServerLevel world,BuyanState.Site site) {
        if(world.dimension()!=Level.OVERWORLD
            ||BuyanState.get(world).active||BuyanNatureState.get(world).active
            ||!BuyanNatureState.get(world).finishedAt(site))return false;
        Record saved=Record.get(world);
        long k=key(site);
        if(saved.built.containsKey(k))return true;
        // Begin with near-central dry land, then expand; never clear the
        // centre or sacrifice another existing building just to find a site.
        for(int radius=80;radius<=176;radius+=8)
            for(int lateral:new int[]{0,-8,8,-16,16,-24,24,-32,32,-40,40,-48,48})
                for(int[] d:new int[][]{{1,0},{-1,0},{0,-1},{0,1}}) {
                int x=site.x()+d[0]*radius+d[1]*lateral;
                int z=site.z()+d[1]*radius-d[0]*lateral;
                Rotation rotation=d[0]==1?Rotation.COUNTERCLOCKWISE_90:
                    d[0]==-1?Rotation.CLOCKWISE_90:
                        d[1]==-1?Rotation.CLOCKWISE_180:Rotation.NONE;
                Plan plan=new Plan(world,site,x,z,rotation,world.getSeed());
                if(!plan.fits())continue;
                try {
                    plan.build();
                    if(!plan.geometryCheck()||!plan.safe())continue;
                    plan.commit();
                    saved.mark(k,new BlockPos(x,plan.base,z));
                    LogUtils.getLogger().info("Buyan shrine placed {},{},{}",x,plan.base,z);
                    return true;
                } catch(IllegalStateException exception) {
                    LogUtils.getLogger().warn("Shrine candidate {},{} rejected: {}",
                        x,z,exception.getMessage());
                }
            }
        LogUtils.getLogger().error("Buyan sanctuary: no non-overlapping site");
        return false;
    }
    public static int status(CommandSourceStack source) {
        ServerLevel world=source.getLevel();
        BuyanState.Site site=BuyanNatureGenerator.selectedFoundation(world,
            (int)Math.floor(source.getPosition().x),
            (int)Math.floor(source.getPosition().z));
        if(site==null) {
            source.sendFailure(Component.literal("Завершённый Буян не найден."));
            return 0;
        }
        Long location=Record.get(world).built.get(key(site));
        if(location==null) {
            source.sendSuccess(()->Component.literal("Святилище ещё не построено."),false);
            return 0;
        }
        BlockPos p=BlockPos.of(location);
        source.sendSuccess(()->Component.literal("Святилище: X="+p.getX()
            +" Y="+p.getY()+" Z="+p.getZ()
            +" (храм, дуб, цепи, двор, ограда и дорога)."),false);
        return 1;
    }
    public static int generate(CommandSourceStack source) {
        ServerLevel world=source.getLevel();
        BuyanState.Site site=BuyanNatureGenerator.selectedFoundation(world,
            (int)Math.floor(source.getPosition().x),
            (int)Math.floor(source.getPosition().z));
        if(site==null||!BuyanNatureState.get(world).finishedAt(site)) {
            source.sendFailure(Component.literal("Нужен завершённый остров Буян."));
            return 0;
        }
        if(!placeOnce(world,site)) {
            source.sendFailure(Component.literal(
                "Участок для святилища занят; ничего не перестроено."));
            return 0;
        }
        return status(source);
    }
}
