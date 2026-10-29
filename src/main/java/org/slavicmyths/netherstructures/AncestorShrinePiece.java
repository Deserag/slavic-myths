package org.slavicmyths.netherstructures;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

/**
 * 2.1.5: exact 13x15x11 voxel-authored house of ancestor memory.
 * Each Y/Z row below is exported directly from the owner-review TSV (X increases to the right).
 * Geometry variant 0=whole, 1=damaged, 2=severe ruin; no per-load random damage.
 * Rotation stays inside the start chunk in all cardinal directions (13x15 -> 15x13).
 * '.' is unmodified air; all other palette symbols denote real vanilla blocks.
 */
public final class AncestorShrinePiece extends StructurePiece {
    public static final int WIDTH=13, DEPTH=15, HEIGHT=11;
    private static final String[][][] VOXELS = {
    { // A: intact
    { // y=0
        ".............",
        "..SSSSSSSSS..",
        ".SSSSSSSSSSS.",
        ".SSSSSSSSSSS.",
        ".SSSSSSSSSSS.",
        ".SSSSSSSSSSS.",
        ".SSSSSSSSSSS.",
        ".SSSSSSSSSSS.",
        ".SSSSSSSSSSS.",
        ".SSSSSSSSSSS.",
        ".SSSSSSSSSSS.",
        ".SSSSSSSSSSS.",
        ".SSSSSSSSSSS.",
        "..SSSSSSSSS..",
        "............."
    },
    { // y=1
        ".............",
        ".............",
        "..BBBBBCBBB..",
        "..BBBBCBBBB..",
        "..BBBCBBBBB..",
        "..BBCBBBBBB..",
        "..BCBBBBBBB..",
        "..CBBBBBBBB..",
        "..BBBBBBBBC..",
        "..BBBBBBBCB..",
        "..BBBBBBCBB..",
        "..BBBBBCBBB..",
        "..BBBBCBBBB..",
        ".....BBB.....",
        ".....BBB....."
    },
    { // y=2
        ".............",
        ".............",
        "..PBBBBBBBP..",
        "..B.......B..",
        "..B..SSS..B..",
        "..B.....Q.B..",
        "..BU.....UB..",
        "..PU.....UP..",
        "..BU.....UB..",
        "..C.......C..",
        "..B.......B..",
        "..B.......B..",
        "..PTB...BTP..",
        "....P...P....",
        "............."
    },
    { // y=3
        ".............",
        ".............",
        "..PBBBBBBBP..",
        "..B..KKK..B..",
        "..B.......B..",
        "..B.......B..",
        "..B.......B..",
        "..P.......P..",
        "..C.......C..",
        "..B.......B..",
        "..B.......B..",
        "..B.......B..",
        "..PTB...BTP..",
        "....P...P....",
        "............."
    },
    { // y=4
        ".............",
        ".............",
        "..PBBBBBBCP..",
        "..B...K...B..",
        "..B.......B..",
        "..I.......I..",
        "..B.......B..",
        "..P.......P..",
        "..B.......B..",
        "..I.......I..",
        "..B.......B..",
        "..B.......B..",
        "..PTB...BTP..",
        "....PL.LP....",
        "............."
    },
    { // y=5
        ".............",
        ".............",
        "..PBBBBBCBP..",
        "..B.......B..",
        "..B.......B..",
        "..B.......B..",
        "..C.......C..",
        "..P.......P..",
        "..B.......B..",
        "..B.......B..",
        "..B.......B..",
        "..B.......B..",
        "..PTBBBBCTP..",
        "....PN.NP....",
        "............."
    },
    { // y=6
        ".............",
        ".............",
        "..PBBBBCBBP..",
        "..B.......B..",
        "..B.......B..",
        "..C.......C..",
        "..B.......B..",
        "..PTTTTTTTP..",
        "..B.......B..",
        "..B.......B..",
        "..B.......B..",
        "..B.......B..",
        "..PTBBBCBTP..",
        "....PPPPP....",
        "............."
    },
    { // y=7
        ".............",
        ".R.........R.",
        ".RTBBBBBBBTR.",
        ".RT.......TR.",
        ".RT.......TR.",
        ".RT.......TR.",
        ".RT.......TR.",
        ".RT.......TR.",
        ".RT.......TR.",
        ".RT.......TR.",
        ".RT.......TR.",
        ".RT.......TR.",
        ".RTBBBBBBBTR.",
        ".R.........R.",
        "............."
    },
    { // y=8
        ".............",
        "..RR.....RR..",
        "..RRBBBBBRR..",
        "..RR.....RR..",
        "..RR.....RR..",
        "..RR.....RR..",
        "..RR.....RR..",
        "..RR.....RR..",
        "..RR.....RR..",
        "..RR.....RR..",
        "..RR.....RR..",
        "..RR.....RR..",
        "..RRBBBBBRR..",
        "..RR.....RR..",
        "............."
    },
    { // y=9
        ".............",
        "....RR.RR....",
        "....RRBRR....",
        "....RR.RR....",
        "....RR.RR....",
        "....RR.RR....",
        "....RR.RR....",
        "....RR.RR....",
        "....RR.RR....",
        "....RR.RR....",
        "....RR.RR....",
        "....RR.RR....",
        "....RRBRR....",
        "....RR.RR....",
        "............."
    },
    { // y=10
        ".............",
        "......P......",
        "......P......",
        "......P......",
        "......P......",
        "......P......",
        "......P......",
        "......P......",
        "......P......",
        "......P......",
        "......P......",
        "......P......",
        "......P......",
        "......P......",
        "............."
    },
    },
    { // B: damaged
    { // y=0
        ".............",
        "..SSSSSSSSS..",
        ".SSSSSSSSSSS.",
        ".SSSSSSSSSSS.",
        ".SSSSSSSSSSS.",
        ".SSSSSSSSSSS.",
        ".SSSSSSSSSSS.",
        ".SSSSSSSSSSS.",
        ".SSSSSSSSSSS.",
        ".SSSSSSSSSSS.",
        ".SSSSSSSSSSS.",
        ".SSSSSSSSSSS.",
        ".SSSSSSSSSSS.",
        "..SSSSSSSSS..",
        "............."
    },
    { // y=1
        ".............",
        ".............",
        "..BBBBBCBCB..",
        "..BBBBCBCBB..",
        "..BBBCBCBBB..",
        "..BBCBCBBBB..",
        "..BCBCBBBBB..",
        "..CBCBBBBBB..",
        "..BCBBBBBBC..",
        "..CBBBBBBCB..",
        "..BBBBBBCBB..",
        "..BBBBBCBBB..",
        "..BBBBCBBBB..",
        ".....BBB.....",
        ".....BBB....."
    },
    { // y=2
        ".............",
        ".............",
        "..PBBBBCBBP..",
        "..B.......B..",
        "..B..SSS..B..",
        "..B.....Q.B..",
        "..BU.....UB..",
        "..PU.....UP..",
        "..BU.....UB..",
        "..C.......C..",
        "..B.......B..",
        "..B.......B..",
        "..PTB...BTP..",
        "....P...P....",
        "............."
    },
    { // y=3
        ".............",
        ".............",
        "..PBBCBBBBP..",
        "..B..KKK..B..",
        "..B.......B..",
        "..C.......B..",
        "..B.......B..",
        "..P.......P..",
        "..C.......C..",
        "..B.......B..",
        "..B.......C..",
        "..B.......B..",
        "..PTB...CTP..",
        "....P...P....",
        "............."
    },
    { // y=4
        ".............",
        ".............",
        "..PCBBBBBCP..",
        "..C...K...B..",
        "..B.......B..",
        "..I.......I..",
        "..B..........",
        "..P..........",
        "..B..........",
        "..I.......I..",
        "..B.......B..",
        "..B.......B..",
        "..PTB...BTP..",
        "....PL.LP....",
        "............."
    },
    { // y=5
        ".............",
        ".............",
        "..PBBBBBCBP..",
        "..B.......B..",
        "..B.......B..",
        "..B..........",
        "..C..........",
        "..P..........",
        "..B..........",
        "..B.......B..",
        "..B.......B..",
        "..B.......B..",
        "..PTCBBBCTP..",
        "....PN.NP....",
        "............."
    },
    { // y=6
        ".............",
        ".............",
        "..PBBBBCBBP..",
        "..B.......B..",
        "..B.......C..",
        "..C..........",
        "..B..........",
        "..PTTTTTTT...",
        "..B..........",
        "..B.......B..",
        "..B.......B..",
        "..B.......B..",
        "..PTBBBCBTP..",
        "....PPPPP....",
        "............."
    },
    { // y=7
        ".............",
        ".R...........",
        ".RTBBBBBBBT..",
        ".RT.......T..",
        ".RT.......T..",
        ".RT.......T..",
        ".RT.......TR.",
        ".RT.......TR.",
        ".RT.......TR.",
        ".RT.......TR.",
        ".RT.......TR.",
        "..T.......TR.",
        "..TBBBBBBBTR.",
        "...........R.",
        "............."
    },
    { // y=8
        ".............",
        "..RR.........",
        "..RRBBBBC....",
        "..RR.........",
        "..RR.........",
        "..RR.........",
        "..RR.....RR..",
        "..RR.....RR..",
        "..RR.....RR..",
        "..RR.....RR..",
        "..RR.....RR..",
        ".........RR..",
        "....BBBBBRR..",
        ".........RR..",
        "............."
    },
    { // y=9
        ".............",
        "....RR.R.....",
        "....RRCR.....",
        "....RR.R.....",
        "....RR.R.....",
        "....RR.R.....",
        "....RR.RR....",
        "....RR.RR....",
        "....RR.RR....",
        "....RR.RR....",
        "....RR.RR....",
        "....RR.RR....",
        "....RRBRR....",
        "....RR.RR....",
        "............."
    },
    { // y=10
        ".............",
        "......P......",
        "......P......",
        "......P......",
        "......P......",
        "......P......",
        "......P......",
        "......P......",
        "......P......",
        "......P......",
        "......P......",
        "......P......",
        "......P......",
        "......P......",
        "............."
    },
    },
    { // C: severe ruin
    { // y=0
        ".............",
        "..SSSSSSSSS..",
        ".SSSSSSSSSSS.",
        ".SSSSSSSSSSS.",
        ".SSSSSSSSSSS.",
        ".SSSSSSSSSSS.",
        ".SSSSSSSSSSS.",
        ".SSSSSSSSSSS.",
        ".SSSSSSSSSSS.",
        ".SSSSSSSSSSS.",
        ".SSSSSSSSSSS.",
        ".SSSSSSSSSSS.",
        ".SSSSSSSSSSS.",
        "..SSSSSSSSS..",
        "............."
    },
    { // y=1
        ".............",
        ".............",
        "..BBCBBCBBC..",
        "..BCBBCBBCB..",
        "..CBBCBBCBB..",
        "..BBCBBCBBC..",
        "..BCBBCBBCB..",
        "..CBBCBBCBB..",
        "..BBCBBCBBC..",
        "..BCBBCBBCB..",
        "..CBBCBBCBB..",
        "..BBCBBCBBC..",
        "..BCBBCBBCB..",
        ".....CBB.....",
        ".....BBC....."
    },
    { // y=2
        ".............",
        ".............",
        "..PBCBBCBBP..",
        "..B.......B..",
        "..C..SSS..B..",
        "..B.....Q.C..",
        "..BU.....UB..",
        "..PU.....UP..",
        "..BU.....UC..",
        "..C.......C..",
        "..C.......B..",
        "..B.......C..",
        "..PTB...BTP..",
        "....P...P....",
        "............."
    },
    { // y=3
        ".............",
        ".............",
        "..PBCBBCBBP..",
        "..B..KKK..B..",
        "..C.......B..",
        "..B.......C..",
        "..B.......B..",
        "..P.......P..",
        "..C.......C..",
        "..B.......B..",
        "..C.......B..",
        "..B.......C..",
        "..PTB...BTP..",
        "....P...P....",
        "............."
    },
    { // y=4
        ".............",
        ".............",
        "..P.CBBCB....",
        "......K...B..",
        "..B..........",
        "..I.......I..",
        "..B..........",
        ".............",
        "..........B..",
        "..I.......I..",
        "..........B..",
        "..B..........",
        "....B.....P..",
        "....PL.LP....",
        "............."
    },
    { // y=5
        ".............",
        ".............",
        "..P.CBBCC....",
        ".............",
        ".............",
        ".............",
        ".............",
        ".............",
        "..........B..",
        "..B..........",
        ".............",
        "..B..........",
        ".............",
        "....PN.NP....",
        "............."
    },
    { // y=6
        ".............",
        ".............",
        "....B..C.....",
        ".............",
        ".............",
        ".............",
        ".............",
        ".............",
        ".............",
        ".............",
        ".............",
        ".............",
        "........B....",
        "....P..PP....",
        "............."
    },
    { // y=7
        ".............",
        ".............",
        ".............",
        ".............",
        ".............",
        ".............",
        ".............",
        ".............",
        ".............",
        ".............",
        ".............",
        ".............",
        "........B....",
        ".............",
        "............."
    },
    { // y=8
        ".............",
        ".............",
        ".............",
        ".............",
        ".............",
        ".............",
        ".............",
        ".............",
        ".............",
        ".............",
        ".............",
        ".............",
        ".............",
        ".............",
        "............."
    },
    { // y=9
        ".............",
        ".............",
        ".............",
        ".............",
        ".............",
        ".............",
        ".............",
        ".............",
        ".............",
        ".............",
        ".............",
        ".............",
        ".............",
        ".............",
        "............."
    },
    { // y=10
        ".............",
        ".............",
        ".............",
        ".............",
        ".............",
        ".............",
        ".............",
        ".............",
        ".............",
        ".............",
        ".............",
        ".............",
        ".............",
        ".............",
        "............."
    }
    }
    };
    private final BlockPos center;
    private final Rotation rotation;
    private final int variant;
    private final long seed;

    public AncestorShrinePiece(AncestorShrineTerrain.Plan plan) {
        super(AncestorShrineWorldgen.PIECE.get(), 0, plan.bounds());
        center=plan.center();
        rotation=plan.rotation();
        variant=plan.variant();
        seed=plan.seed();
    }
    public AncestorShrinePiece(CompoundTag tag) {
        super(AncestorShrineWorldgen.PIECE.get(), tag);
        center=BlockPos.of(tag.getLong("Center"));
        rotation=Rotation.valueOf(tag.getString("Rotation"));
        variant=tag.getInt("Variant");
        seed=tag.getLong("DesignSeed");
    }
    @Override protected void addAdditionalSaveData(StructurePieceSerializationContext ctx, CompoundTag tag) {
        tag.putLong("Center",center.asLong());
        tag.putString("Rotation",rotation.name());
        tag.putInt("Variant",variant);
        tag.putLong("DesignSeed",seed);
    }
    public int variant() {return variant;}
    public BlockPos at(int x,int y,int z) {
        return center.offset(new BlockPos(x-6,y,z-7).rotate(rotation));
    }
    public static boolean present(int variant,int x,int y,int z) {
        return variant>=0&&variant<3&&x>=0&&x<WIDTH&&y>=0&&y<HEIGHT&&z>=0&&z<DEPTH
                &&VOXELS[variant][y][z].charAt(x)!='.';
    }
    public BlockState voxel(int x,int y,int z) {
        if(variant<0||variant>2||x<0||x>=WIDTH||y<0||y>=HEIGHT||z<0||z>=DEPTH)return null;
        return switch(VOXELS[variant][y][z].charAt(x)) {
            case 'B' -> Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
            case 'C' -> Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
            case 'S' -> Blocks.POLISHED_BLACKSTONE.defaultBlockState();
            case 'K' -> Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState();
            case 'P' -> Blocks.POLISHED_BASALT.defaultBlockState();
            case 'T' -> Blocks.WARPED_STEM.defaultBlockState();
            case 'R' -> Blocks.WARPED_PLANKS.defaultBlockState();
            case 'I' -> Blocks.IRON_BARS.defaultBlockState();
            case 'L' -> Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true);
            case 'N' -> Blocks.CHAIN.defaultBlockState();
            case 'Q' -> Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING,Direction.WEST);
            case 'U' -> Blocks.POLISHED_BLACKSTONE_BRICK_SLAB.defaultBlockState()
                    .setValue(SlabBlock.TYPE,net.minecraft.world.level.block.state.properties.SlabType.BOTTOM);
            default -> null;
        };
    }
    private static boolean replaceable(BlockState old) {
        // Deliberately refuse lava, bedrock, solid blocks, portals and block entities.
        return old.isAir() || (old.getFluidState().isEmpty() &&
                (old.is(Blocks.CRIMSON_ROOTS)||old.is(Blocks.WARPED_ROOTS)||
                 old.is(Blocks.NETHER_SPROUTS)||old.is(Blocks.CRIMSON_FUNGUS)||
                 old.is(Blocks.WARPED_FUNGUS) || old.canBeReplaced()));
    }
    /**
     * A single-chunk ALL-OR-NOTHING preflight. Never overwrites someone else's block
     * entity or a solid object. Repeated placement can keep its own blocks, chest and loot.
     */
    @Override public void postProcess(WorldGenLevel world,StructureManager manager,
            ChunkGenerator generator,RandomSource random,BoundingBox clip,ChunkPos chunk,BlockPos pivot) {
        if(variant<0||variant>2)return;
        // Check every proposed voxel BEFORE committing any changes.
        for(int y=0;y<HEIGHT;y++)for(int z=0;z<DEPTH;z++)for(int x=0;x<WIDTH;x++) {
            BlockState authored=voxel(x,y,z);
            if(authored==null)continue;
            BlockPos pos=at(x,y,z);
            if(!clip.isInside(pos)||!world.ensureCanWrite(pos))return;
            BlockState old=world.getBlockState(pos);
            BlockState rotated=authored.rotate(rotation);
            boolean ours=old.equals(rotated);
            if(world.getBlockEntity(pos)!=null &&
                   !(ours&&old.is(Blocks.CHEST)&&x==8&&y==2&&z==5))return;
            if(!ours&&!replaceable(old))return;
        }
        // Also reject a blocked entrance, walking spine or chest lid.
        for(int z=5;z<=14;z++)for(int y=2;y<=3;y++)for(int x=5;x<=7;x++) {
            BlockPos pos=at(x,y,z);
            if(!clip.isInside(pos)||!world.ensureCanWrite(pos))return;
            BlockState intended=voxel(x,y,z),old=world.getBlockState(pos);
            if(intended==null&&!replaceable(old))return;
        }
        BlockPos aboveChest=at(8,3,5);
        if(!clip.isInside(aboveChest)||!world.ensureCanWrite(aboveChest)||
                !replaceable(world.getBlockState(aboveChest)))return;
        // All three variants use unchanged accessible coordinates for the same single chest.
        // Set non-hanging pieces first; hanging lamps last once chain and roof exist.
        for(int y=0;y<HEIGHT;y++)for(int z=0;z<DEPTH;z++)for(int x=0;x<WIDTH;x++) {
            BlockState next=voxel(x,y,z);
            if(next==null||next.is(Blocks.SOUL_LANTERN))continue;
            BlockPos pos=at(x,y,z);
            next=next.rotate(rotation);
            if(!world.getBlockState(pos).equals(next))world.setBlock(pos,next,2);
        }
        for(int x:new int[]{5,7}) {
            BlockPos pos=at(x,4,13);
            BlockState lamp=voxel(x,4,13);
            if(lamp!=null&&!world.getBlockState(pos).equals(lamp.rotate(rotation)))
                world.setBlock(pos,lamp.rotate(rotation),2);
        }
        BlockPos chestPos=at(8,2,5);
        if(world.getBlockEntity(chestPos) instanceof RandomizableContainerBlockEntity chest
                &&chest.getLootTable()==null&&chest.isEmpty()) {
            chest.setLootTable(ResourceKey.create(Registries.LOOT_TABLE,
                    AncestorShrineWorldgen.id("chests/ancestor_shrine")),seed^chestPos.asLong());
        }
    }
}
