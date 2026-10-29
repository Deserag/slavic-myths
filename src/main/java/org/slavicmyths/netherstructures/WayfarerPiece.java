package org.slavicmyths.netherstructures;

import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

/**
 * Exact 9x7x6 authored coordinates. A half-ruin, B mostly intact, C heavy ruin.
 * Never changes inventories already present in a world. All writes fit in one
 * owning chunk; preflight is rechecked before any writes, not per voxel after
 * half of the building has already been placed.
 */
public final class WayfarerPiece extends StructurePiece {
    private final BlockPos center;
    private final Rotation rotation;
    private final int variant;
    private final boolean lit;
    private final long seed;
    public WayfarerPiece(WayfarerTerrain.Plan p) {
        super(WayfarerWorldgen.PIECE.get(),0,p.bounds());
        center=p.center();rotation=p.rotation();variant=p.variant();lit=p.lit();seed=p.seed();
    }
    public WayfarerPiece(CompoundTag nbt) {
        super(WayfarerWorldgen.PIECE.get(),nbt);
        center=BlockPos.of(nbt.getLong("Center"));
        rotation=Rotation.valueOf(nbt.getString("Rotation"));
        variant=nbt.getInt("Variant");lit=nbt.getBoolean("Lit");seed=nbt.getLong("DesignSeed");
    }
    @Override protected void addAdditionalSaveData(StructurePieceSerializationContext ctx, CompoundTag nbt) {
        nbt.putLong("Center",center.asLong());
        nbt.putString("Rotation",rotation.name());
        nbt.putInt("Variant",variant);nbt.putBoolean("Lit",lit);
        nbt.putLong("DesignSeed",seed);
    }
    public int variant(){return variant;}
    public BlockPos at(int x,int y,int z) {
        return center.offset(new BlockPos(x-4,y,z-3).rotate(rotation));
    }
    /** Paving is sparse by design and each pad needs real rock below. */
    public static boolean foundation(int x,int z) {
        return (z==0&&x>=2&&x<=6) ||
            (z==1&&(x==1||x==7)) ||
            (z==3&&(x==1||x==7)) ||
            (z==2&&(x==3||x==6)) ||
            (z==5&&(x==3||x==4||x==5));
    }
    private boolean roofGap(int x,int z) {
        if(z==3||x==1||x==7)return false; // beams, corner anchors never disappear
        if(variant==1)return false;
        if((x==2&&z==1)||(x==5&&z==1)||(x==6&&z==1)||
            (x==6&&z==2)||(x==2&&z==2))return true;
        return variant==2 && ((x==3&&z==1)||(x==4&&z==2)||(x==5&&z==2));
    }
    public BlockState voxel(int x,int y,int z) {
        if(x<0||x>8||y<0||y>5||z<0||z>6)return null;
        if(y==0) {
            if(!foundation(x,z))return null;
            return (z==5&&x==4 ? Blocks.BLACKSTONE : Blocks.POLISHED_BLACKSTONE_BRICKS).defaultBlockState();
        }
        if(y>=1&&y<=4&&((x==1||x==7)&&(z==1||z==3)))
            return Blocks.CRIMSON_STEM.defaultBlockState();
        if(y==5&&(x==1||x==7)&&z==3)return Blocks.CRIMSON_STEM.defaultBlockState();
        if(y==4&&x==1&&z==4)return Blocks.CRIMSON_PLANKS.defaultBlockState(); // true lantern cantilever
        if(y==3&&x==1&&z==4)return Blocks.CHAIN.defaultBlockState();
        if(y==2&&x==1&&z==4)
            return Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true);
        if(y==4&&x>=1&&x<=7&&z>=1&&z<=3) {
            if(roofGap(x,z))return null;
            if(z==3||z==1)return Blocks.CRIMSON_PLANKS.defaultBlockState();
            return Blocks.CRIMSON_SLAB.defaultBlockState();
        }
        if(z==0&&x>=2&&x<=6&&(y==1||y==2)) {
            if(variant==2&&(x==3||x==5)&&y==2)return null;
            if(variant==0&&x==4&&y==2)return null;
            return ((x+y)%3==0?Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS:
                Blocks.POLISHED_BLACKSTONE_BRICKS).defaultBlockState();
        }
        if(y==1&&x==3&&z==2)
            return Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING,Direction.SOUTH);
        if(y==1&&x==6&&z==2)return Blocks.CRAFTING_TABLE.defaultBlockState();
        if(y==1&&x==4&&z==5)
            return Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT,lit);
        if(y==1&&(x==3||x==5)&&z==5)
            return Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS.defaultBlockState()
                .setValue(StairBlock.FACING,Direction.SOUTH);
        // Protect actual access, chest clearance, workbench access and walking headroom
        // from later replaceable vegetation, never carve solid terrain or other builds.
        if(y>=1&&y<=3&&x>=1&&x<=7&&z>=1&&z<=5)return Blocks.AIR.defaultBlockState();
        return null;
    }
    private static boolean replaceable(BlockState old) {
        return old.isAir() || old.is(Blocks.CRIMSON_FUNGUS) || old.is(Blocks.CRIMSON_ROOTS) ||
            old.is(Blocks.WARPED_FUNGUS) || old.is(Blocks.WARPED_ROOTS) ||
            old.is(Blocks.NETHER_SPROUTS) || old.is(Blocks.WEEPING_VINES) ||
            old.is(Blocks.WEEPING_VINES_PLANT) || old.is(Blocks.TWISTING_VINES) ||
            old.is(Blocks.TWISTING_VINES_PLANT) || old.is(Blocks.BROWN_MUSHROOM) ||
            old.is(Blocks.RED_MUSHROOM) ||
            (old.canBeReplaced() && old.getFluidState().isEmpty());
    }
    private static final java.util.concurrent.atomic.AtomicInteger DEBUG_REJECTIONS =
        new java.util.concurrent.atomic.AtomicInteger();
    private static void diagnostic(String reason,BlockPos p,BlockState state) {
        if(System.getProperty("hellqa.seed") != null && DEBUG_REJECTIONS.getAndIncrement()<24)
            System.out.println("HELLQA_WAYFARER_REJECT "+reason+" pos="+p+" block="+state);
    }
    @Override public void postProcess(WorldGenLevel world,StructureManager manager,
            ChunkGenerator generator,RandomSource random,BoundingBox clip,ChunkPos chunk,BlockPos pivot) {
        // The bounds and 9x7 layout are wholly inside the start chunk in all four rotations.
        // Abort intact if another generator/player feature occupies a single proposed voxel.
        for(int y=0;y<=5;y++)for(int x=0;x<9;x++)for(int z=0;z<7;z++) {
            BlockState state=voxel(x,y,z);
            if(state==null)continue;
            BlockPos pos=at(x,y,z);
            BlockState old=world.getBlockState(pos);
            if(!clip.isInside(pos)||!world.ensureCanWrite(pos)||
                    world.getBlockEntity(pos)!=null||!replaceable(old)) {
                diagnostic("occupied-or-clip",pos,old);
                return;
            }
        }
        for(int x=0;x<9;x++)for(int z=0;z<7;z++)if(foundation(x,z)) {
            BlockPos below=at(x,-1,z);
            if(!WayfarerTerrain.natural(world.getBlockState(below))) {
                diagnostic("foundation",below,world.getBlockState(below));
                return;
            }
        }
        // Ground pads first, then top-down for real roof -> chain -> hanging lantern.
        for(int layer=0;layer<=6;layer++) {
            int y=layer==0?0:6-layer;
            for(int x=0;x<9;x++)for(int z=0;z<7;z++) {
                BlockState next=voxel(x,y,z);
                if(next==null)continue;
                BlockPos pos=at(x,y,z);
                if(!world.getBlockState(pos).equals(next.rotate(rotation)))
                    world.setBlock(pos,next.rotate(rotation),2);
            }
        }
        BlockPos chestPos=at(3,1,2);
        if(world.getBlockEntity(chestPos) instanceof RandomizableContainerBlockEntity chest &&
                chest.getLootTable()==null && chest.isEmpty()) {
            chest.setLootTable(ResourceKey.create(Registries.LOOT_TABLE,
                    WayfarerWorldgen.id("chests/wayfarer_camp")),seed ^ chestPos.asLong());
        }
    }
}
