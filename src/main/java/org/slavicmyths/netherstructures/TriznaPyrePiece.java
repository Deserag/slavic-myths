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

/** Open 15x15x9 memorial pyre; palette and coordinates derived from the approved SVG/spec.
 * All destruction is deterministic geometry, never per-load random deletion. */
public final class TriznaPyrePiece extends StructurePiece {
    private final BlockPos center;
    private final Rotation rotation;
    private final int variant;
    private final long seed;
    private boolean placed;
    public TriznaPyrePiece(TriznaPyreTerrain.Plan p) {
        super(TriznaPyreWorldgen.PIECE.get(),0,p.bounds());
        center=p.center();rotation=p.rotation();variant=p.variant();seed=p.seed();
    }
    public TriznaPyrePiece(CompoundTag n) {
        super(TriznaPyreWorldgen.PIECE.get(),n);
        center=BlockPos.of(n.getLong("Center"));rotation=Rotation.valueOf(n.getString("Rotation"));
        variant=n.getInt("Variant");seed=n.getLong("DesignSeed");placed=n.getBoolean("Placed");
    }
    @Override protected void addAdditionalSaveData(StructurePieceSerializationContext c,CompoundTag n) {
        n.putLong("Center",center.asLong());n.putString("Rotation",rotation.name());n.putInt("Variant",variant);
        n.putLong("DesignSeed",seed);n.putBoolean("Placed",placed);
    }
    public int variant(){return variant;}
    public BlockPos at(int x,int y,int z){return center.offset(new BlockPos(x-7,y,z-7).rotate(rotation));}
    public BlockState voxel(int x,int y,int z){return voxel(variant,x,y,z);}
    private static final String[] FLOOR={"......BKB......","....BBKBBBB....","...BBKBBBBBC...","..BBKBBBBBBCB..",".BBKBBBBBBKCBB.",".BKBBBBBBKBCBB.","CKBBBBBBKBBCBBB","CBBBBBBKBBBCBBK","CBBBBBKBBBBBBKB",".BBBBKBBBBBCKB.",".BBBKBBBBBBCBB.","..BKBBBBBBKCB..","...BBBBBBKBC...","....BBBBKBB....","......BKB......"};
    public static BlockState voxel(int v,int x,int y,int z) {
        if(v<0||v>3||x<0||x>14||z<0||z>14||y<0||y>8)return null;
        char material=FLOOR[z].charAt(x);
        if(material=='.')return null;
        if(y==0) {
            // Three ordinary entry steps meet the surrounding ground without a jump.
            if(z==14&&x>=6&&x<=8)return Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.NORTH);
            return (material=='K'?Blocks.BLACKSTONE:material=='C'?Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS:Blocks.POLISHED_BLACKSTONE_BRICKS).defaultBlockState();
        }
        if(x==11&&y==1&&z==8)return Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING,Direction.WEST);
        if(y==1&&x>=5&&x<=9&&z>=5&&z<=9)return (x>=6&&x<=8&&z>=6&&z<=8?Blocks.BLACKSTONE:Blocks.POLISHED_BLACKSTONE_BRICKS).defaultBlockState();
        if(y==1&&x>=6&&x<=8&&z==10)return Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.NORTH);
        if(y==2&&Math.abs(x-7)+Math.abs(z-7)<=1)
            return Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT,v<2||v==2&&!(x==7&&z==6||x==8&&z==7));
        if(y==2&&(x==6||x==8)&&(z==6||z==8))return Blocks.MAGMA_BLOCK.defaultBlockState();
        int[][] points={{3,3},{11,3},{3,11},{11,11}};
        int[][] heights={{8,8,8,8},{8,8,3,8},{8,3,8,4},{4,3,2,1}};
        for(int i=0;i<4;i++)if(x==points[i][0]&&z==points[i][1]&&y<=heights[v][i])
            return (y==1?Blocks.BASALT:y==2?Blocks.POLISHED_BASALT:Blocks.CRIMSON_STEM).defaultBlockState();
        for(int side=0;side<4;side++)if(heights[v][side]>=7) {
            int post=points[side][0],postZ=points[side][1],lamp=post==3?4:10;
            if(z==postZ&&y==7&&x>=post-1&&x<=post+1)return Blocks.CRIMSON_PLANKS.defaultBlockState();
            if(side<2&&x==lamp&&z==3&&y==6)return Blocks.CHAIN.defaultBlockState();
            if(side<2&&x==lamp&&z==3&&y==5)return Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true);
        }
        if(y==4&&z==10&&(v==0&&x==3||v<3&&x==11))
            return Blocks.RED_WALL_BANNER.defaultBlockState().setValue(WallBannerBlock.FACING,Direction.NORTH);
        // Coordinates are read from the supplied SVG's exact 15x15 top grid.
        if(y==1&&((x==9&&z==3)||(x==3&&z==8)))return Blocks.SKELETON_SKULL.defaultBlockState();
        int[][] candles={{4,6},{10,6},{4,10},{10,10}};
        for(int i=0;i<(v==3?0:4);i++)if(y==1&&x==candles[i][0]&&z==candles[i][1])
            return Blocks.CANDLE.defaultBlockState().setValue(CandleBlock.LIT,v<2);
        if(y>=1&&y<=Math.max(1,3-v)&&((x==7&&z==2)||(x==2&&z==7)||(x==12&&z==7)))
            return (v>=2?Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS:Blocks.POLISHED_BASALT).defaultBlockState();
        if(y==1&&x==12&&z==8)return Blocks.BASALT.defaultBlockState();
        if(y==1&&((x==6&&z==1)||(x==4&&z==2)||(x==2&&z==3)||(x==12&&z==4)||(x==12&&z==10)||(x==2&&z==11)||(x==4&&z==12)))
            return Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
        return Blocks.AIR.defaultBlockState();
    }
    public boolean ownsRaw(BlockPos p,BlockState actual) {
        // Three applications invert every cardinal rotation.
        var local=p.subtract(center).rotate(rotation).rotate(rotation).rotate(rotation);
        var expected=voxel(local.getX()+7,local.getY(),local.getZ()+7);
        return expected!=null&&expected.is(actual.getBlock())&&!expected.isAir();
    }
    public boolean validSite(WorldGenLevel w,BoundingBox clip) {
        for(int x=0;x<15;x++)for(int z=0;z<15;z++) {
            var floor=voxel(x,0,z);
            if(floor!=null&&(!TriznaPyreTerrain.stable(w.getBlockState(at(x,-1,z)))||w.getBlockState(at(x,-1,z)).hasBlockEntity()||!w.getFluidState(at(x,-1,z)).isEmpty()))return false;
            for(int y=0;y<=8;y++) {
                var intended=voxel(x,y,z);if(intended==null)continue;
                var p=at(x,y,z);if(!clip.isInside(p)||!w.ensureCanWrite(p))return false;
                var old=w.getBlockState(p);
                if(old.hasBlockEntity()||!old.getFluidState().isEmpty()||old.is(Blocks.BEDROCK))return false;
                if(!(old.isAir()||NetherBridgePiece.harmlessFlora(old)||old.canBeReplaced()||y<=2&&TriznaPyreTerrain.natural(old)))return false;
            }
        }
        return true;
    }
    @Override public void postProcess(WorldGenLevel w,StructureManager manager,ChunkGenerator g,RandomSource random,
            BoundingBox clip,ChunkPos cp,BlockPos pivot) {
        if(placed)return;
        if(!validSite(w,clip)) {
            var structure=w.registryAccess().registryOrThrow(Registries.STRUCTURE).get(TriznaPyreWorldgen.id("trizna_pyre"));
            var owner=w.getChunk(cp.x,cp.z,net.minecraft.world.level.chunk.status.ChunkStatus.STRUCTURE_STARTS,false);
            if(owner!=null&&structure!=null) {
                owner.setStartForStructure(structure,StructureStart.INVALID_START);
                var snapshot=java.util.Map.copyOf(owner.getAllStarts());
                w.getLevel().getServer().execute(()->manager.structureCheck.onStructureLoad(cp,snapshot));
            }
            if(System.getProperty("hellqa.seed")!=null)System.out.println("HELLQA_PYRE_REJECT chunk="+cp+" actual preflight unsafe; no writes");
            return;
        }
        for(int pass=0;pass<2;pass++)for(int y=0;y<=8;y++)for(int z=0;z<15;z++)for(int x=0;x<15;x++) {
            var next=voxel(x,y,z);if(next==null)continue;
            boolean decor=next.hasBlockEntity()||next.is(Blocks.LANTERN)||next.is(Blocks.CANDLE);
            if(decor!=(pass==1))continue;
            w.setBlock(at(x,y,z),next.rotate(rotation),2);
        }
        var chestPos=at(11,1,8);
        if(w.getBlockEntity(chestPos) instanceof RandomizableContainerBlockEntity chest)
            chest.setLootTable(ResourceKey.create(Registries.LOOT_TABLE,TriznaPyreWorldgen.id("chests/trizna_pyre")),seed^chestPos.asLong());
        placed=true;
    }
    public boolean clearAmbient(WorldGenLevel w,int minX,int minZ,int maxX,int maxZ) {
        if(!placed)return false;
        boolean changed=false;
        for(int x=0;x<15;x++)for(int z=0;z<15;z++)for(int y=1;y<=8;y++) {
            var state=voxel(x,y,z);if(state==null||!state.isAir())continue;
            var p=at(x,y,z);
            if(p.getX()<minX||p.getX()>maxX||p.getZ()<minZ||p.getZ()>maxZ)continue;
            if(w.getChunk(p).getPersistedStatus().isOrAfter(net.minecraft.world.level.chunk.status.ChunkStatus.FULL))continue;
            var old=w.getBlockState(p);
            if(NetherBridgePiece.harmlessFlora(old)&&!old.hasBlockEntity()&&w.ensureCanWrite(p))changed|=w.setBlock(p,Blocks.AIR.defaultBlockState(),2);
        }
        return changed;
    }
}
