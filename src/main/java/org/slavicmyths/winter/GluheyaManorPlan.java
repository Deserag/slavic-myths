package org.slavicmyths.winter;

import java.io.*;
import java.util.*;
import java.util.zip.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;

/** Exact approved Python V1 voxel grid; Minecraft block states are derived from its shape palette. */
final class GluheyaManorPlan {
    static final int X0=-19,X1=19,Z0=-3,Z1=26,Y1=26,W=39,D=30,H=27;
    static final byte[] DATA;
    static final BlockState[] PALETTE;
    static final List<BlockPos> COLUMNS;
    static {
        try(var in=GluheyaManorPlan.class.getResourceAsStream("/data/slavicmyths/winter/gluheya_manor_v1.txt")){
            if(in==null)throw new IOException("Missing manor blueprint");
            try(var stream=new InflaterInputStream(new ByteArrayInputStream(Base64.getMimeDecoder().decode(in.readAllBytes())))){
                DATA=stream.readNBytes(W*D*H+1);
            }
            var crc=new CRC32();crc.update(DATA);int count=0;
            for(byte v:DATA){int n=Byte.toUnsignedInt(v);if(n>33)throw new IOException("Invalid manor palette");if(n!=0)count++;}
            if(DATA.length!=W*D*H||count!=6907||crc.getValue()!=2209565596L)throw new IOException("Manor V1 checksum/count mismatch");
            PALETTE=new BlockState[]{
                Blocks.AIR.defaultBlockState(), Blocks.PURPLE_BED.defaultBlockState(), Blocks.BOOKSHELF.defaultBlockState(),
                Blocks.BREWING_STAND.defaultBlockState(), Blocks.WATER_CAULDRON.defaultBlockState(), Blocks.CHAIN.defaultBlockState(),
                Blocks.CHEST.defaultBlockState(), frozen("frozen_deepslate_bricks"), frozen("frozen_deepslate_bricks_slab"),
                facing(frozen("frozen_deepslate_bricks_stairs"),Direction.EAST), facing(frozen("frozen_deepslate_bricks_stairs"),Direction.WEST),
                facing(Blocks.DARK_OAK_DOOR.defaultBlockState(),Direction.NORTH), facing(Blocks.FURNACE.defaultBlockState(),Direction.WEST),
                Blocks.LIGHT_BLUE_STAINED_GLASS.defaultBlockState(), Blocks.LIGHT_BLUE_STAINED_GLASS_PANE.defaultBlockState(),
                Blocks.LIGHT_BLUE_STAINED_GLASS_PANE.defaultBlockState(), Blocks.PACKED_ICE.defaultBlockState(),
                Blocks.IRON_BARS.defaultBlockState(), Blocks.END_ROD.defaultBlockState(), Blocks.LANTERN.defaultBlockState(),
                Blocks.DARK_OAK_PLANKS.defaultBlockState(), facing(Blocks.PURPLE_WALL_BANNER.defaultBlockState(),Direction.NORTH),
                frozen("frozen_cobbled_deepslate"), Blocks.SEA_LANTERN.defaultBlockState(), Blocks.SNOW.defaultBlockState(),
                frozen("frozen_stone_bricks"), frozen("frozen_stone_bricks_slab"), facing(frozen("frozen_stone_bricks_stairs"),Direction.SOUTH),
                frozen("frozen_stone_bricks_wall"), Blocks.SPRUCE_PLANKS.defaultBlockState(), Blocks.SPRUCE_SLAB.defaultBlockState(),
                Blocks.PURPLE_BED.defaultBlockState(), facing(Blocks.DARK_OAK_STAIRS.defaultBlockState(),Direction.SOUTH),
                facing(Blocks.DARK_OAK_STAIRS.defaultBlockState(),Direction.NORTH)
            };
            var columns=new ArrayList<BlockPos>();
            for(int z=Z0;z<=Z1;z++)for(int x=X0;x<=X1;x++)
                for(int y=0;y<=Y1;y++)if(raw(x,y,z)!=0){columns.add(new BlockPos(x,y,z));break;}
            COLUMNS=List.copyOf(columns);
        }catch(IOException e){throw new IllegalStateException("Cannot load manor V1",e);}
    }
    private static BlockState frozen(String name){
        Block b=BuiltInRegistries.BLOCK.getOptional(ResourceLocation.fromNamespaceAndPath("slavicmyths",name))
            .orElseThrow(()->new IllegalStateException("Missing manor block "+name));
        return b.defaultBlockState();
    }
    private static BlockState facing(BlockState state,Direction dir){return state.setValue(BlockStateProperties.HORIZONTAL_FACING,dir);}
    static int raw(int x,int y,int z){
        if(x<X0||x>X1||z<Z0||z>Z1||y<0||y>Y1)return 0;
        return Byte.toUnsignedInt(DATA[(y*D+z-Z0)*W+x-X0]);
    }
    static BlockState block(int x,int y,int z){
        int n=raw(x,y,z);BlockState s=PALETTE[n];
        if(n==1||n==31)return facing(s,Direction.NORTH).setValue(BlockStateProperties.BED_PART,n==31?BedPart.HEAD:BedPart.FOOT);
        if(n==11)return s.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF,y==2?DoubleBlockHalf.LOWER:DoubleBlockHalf.UPPER)
            .setValue(BlockStateProperties.DOOR_HINGE,x==-1?DoorHingeSide.LEFT:DoorHingeSide.RIGHT);
        if(n==19)return s.setValue(BlockStateProperties.HANGING,raw(x,y+1,z)!=0);
        if((n==8||n==26||n==30)&&raw(x,y+1,z)==24)return s.setValue(BlockStateProperties.SLAB_TYPE,SlabType.TOP);
        if(n==14||n==15||n==17){
            boolean north=n==14||n==17,south=north,east=n==15,west=east;
            if(n==17){east=raw(x+1,y,z)!=0;west=raw(x-1,y,z)!=0;}
            return s.setValue(BlockStateProperties.NORTH,north).setValue(BlockStateProperties.SOUTH,south)
                .setValue(BlockStateProperties.EAST,east).setValue(BlockStateProperties.WEST,west);
        }
        if(n==28){
            for(Direction d:Direction.Plane.HORIZONTAL){
                int other=raw(x+d.getStepX(),y,z+d.getStepZ());
                WallSide side=other==28||other==17||other==25||other==7?WallSide.LOW:WallSide.NONE;
                s=switch(d){
                    case NORTH -> s.setValue(BlockStateProperties.NORTH_WALL,side);
                    case SOUTH -> s.setValue(BlockStateProperties.SOUTH_WALL,side);
                    case EAST -> s.setValue(BlockStateProperties.EAST_WALL,side);
                    case WEST -> s.setValue(BlockStateProperties.WEST_WALL,side);
                    default -> s;
                };
            }
        }
        return s;
    }
    private GluheyaManorPlan(){}
}
