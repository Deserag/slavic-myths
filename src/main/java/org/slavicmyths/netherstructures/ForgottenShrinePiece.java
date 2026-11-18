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

/** Four deterministic 15x15 open shrines. The two stone stairways are part of
 * the same atomic piece as the rooms: neither ceiling can seal their route. */
public final class ForgottenShrinePiece extends StructurePiece {
    private BlockPos center;
    private Rotation rotation;
    private final int variant;
    private final long seed;
    private boolean placed;
    public ForgottenShrinePiece(ForgottenShrineTerrain.Plan p) {
        super(ForgottenShrineWorldgen.PIECE.get(),0,p.bounds());
        center=p.center();rotation=p.rotation();variant=p.variant();seed=p.seed();
    }
    public ForgottenShrinePiece(CompoundTag n) {
        super(ForgottenShrineWorldgen.PIECE.get(),n);
        center=BlockPos.of(n.getLong("Center"));rotation=Rotation.valueOf(n.getString("Rotation"));
        variant=n.getInt("Variant");seed=n.getLong("DesignSeed");placed=n.getBoolean("Placed");
    }
    @Override protected void addAdditionalSaveData(StructurePieceSerializationContext c,CompoundTag n) {
        n.putLong("Center",center.asLong());n.putString("Rotation",rotation.name());
        n.putInt("Variant",variant);n.putLong("DesignSeed",seed);n.putBoolean("Placed",placed);
    }
    public int variant(){return variant;}
    public int bottom(){return (variant&1)==0?-5:-10;}
    public BlockPos at(int x,int y,int z){return center.offset(new BlockPos(x-7,y,z-7).rotate(rotation));}
    public BlockState voxel(int x,int y,int z){return voxel(variant,x,y,z);}
    public static BlockState voxel(int variant,int x,int y,int z) {
        int bottom=(variant&1)==0?-5:-10;
        if(x<0||x>14||z<0||z>14||y<bottom||y>8)return null;
        BlockState brick=Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
        // The first stair is at surface Y=0, not -1: otherwise the last step
        // back onto the surface is one block high and needs a jump.
        if(x>=6&&x<=8&&z>=9&&z<=13) {
            int stair=z-13;
            if(y==stair)return Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.SOUTH);
            if(y>stair&&y<=stair+3)return Blocks.AIR.defaultBlockState();
            if(y<stair&&y>=-5)return brick;
        }
        // Second staircase is TWO blocks wide (Z=3,4), westward. It avoids the
        // chest at Z=5 and joins a full landing at X=6,Y=-10.
        if(bottom==-10&&x>=7&&x<=11&&z>=3&&z<=4) {
            int stair=x-16;
            if(y==stair)return Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.EAST);
            if(y>stair&&y<=stair+3)return Blocks.AIR.defaultBlockState();
            if(y<stair&&y>=-10)return brick;
        }
        if(y<0) {
            if(x<2||x>12||z<2||z>12)return null;
            if(y==bottom+1&&x==7&&z==5)
                return Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING,Direction.SOUTH);
            if(y==bottom||y==-5||x==2||x==12||z==2||z==12)return brick;
            // Stable wall-supported lighting, outside both staircase paths.
            if((y==-3||y==-8)&&x==3&&(z==7||z==10))return Blocks.SOUL_LANTERN.defaultBlockState();
            if((y==-4||y==-9)&&x==3&&(z==7||z==10))return brick;
            return Blocks.AIR.defaultBlockState();
        }
        if(y==0) {
            if(z==14&&x>=6&&x<=8)return Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.NORTH);
            if((x==0||x==14)&&(z<2||z>12))return null;
            return (Math.floorMod(x*17+z*31,11)==0?Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS:Blocks.POLISHED_BLACKSTONE_BRICKS).defaultBlockState();
        }
        // Altar is north of the stair mouth, with a fireproof central hearth.
        if(x>=6&&x<=8&&z>=5&&z<=7&&y==1)return Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState();
        if(x==7&&z==6&&y==2)return Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT,variant!=3);
        if(x==7&&z==5&&y>=2&&y<=4)return (y==4?Blocks.CHISELED_POLISHED_BLACKSTONE:Blocks.POLISHED_BASALT).defaultBlockState();
        int[][] posts={{3,3},{11,3},{3,11},{11,11}};
        int[][] heights={{8,8,7,7},{8,8,7,7},{8,4,5,3},{4,3,3,2}};
        for(int i=0;i<4;i++)if(x==posts[i][0]&&z==posts[i][1]&&y<=heights[variant][i])
            return (y<=2?Blocks.POLISHED_BASALT:Blocks.CRIMSON_STEM).defaultBlockState();
        for(int side=0;side<2;side++)if(heights[variant][side]>=7) {
            int post=side==0?3:11,lamp=side==0?4:10;
            if(z==3&&y==7&&x>=post-1&&x<=post+1)return Blocks.CRIMSON_PLANKS.defaultBlockState();
            if(x==lamp&&z==3&&y==6)return Blocks.CHAIN.defaultBlockState();
            if(x==lamp&&z==3&&y==5)return Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true);
        }
        if(variant>=2&&y==1&&((x==4&&z==4)||(x==10&&z==10)))return Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
        if(y==1&&(x==1||x==13)&&z>=2&&z<=12&&(x+z+variant)%4!=0)
            return Blocks.POLISHED_BLACKSTONE_BRICK_WALL.defaultBlockState();
        return Blocks.AIR.defaultBlockState();
    }
    public boolean validSite(WorldGenLevel w,BoundingBox clip) {
        for(int x=0;x<15;x++)for(int z=0;z<15;z++)for(int y=bottom();y<=8;y++) {
            var next=voxel(x,y,z);if(next==null)continue;
            var p=at(x,y,z);
            if(!clip.isInside(p)||!w.ensureCanWrite(p))return false;
            var old=w.getBlockState(p);
            if(old.hasBlockEntity()||!old.getFluidState().isEmpty()||old.is(Blocks.BEDROCK))return rejected(w,p,old,"protected-fluid");
            if(y<=2 ? !(ForgottenShrineTerrain.natural(old)||y>=0&&(old.isAir()||NetherBridgePiece.harmlessFlora(old)||old.canBeReplaced())) :
                !(old.isAir()||NetherBridgePiece.harmlessFlora(old)||old.canBeReplaced()))return rejected(w,p,old,"terrain-envelope");
        }
        for(int x=0;x<15;x++)for(int z=0;z<15;z++) {
            var base=voxel(x,bottom(),z);
            if(base!=null&&!base.isAir()&&!ForgottenShrineTerrain.stable(w.getBlockState(at(x,bottom()-1,z))))return rejected(w,at(x,bottom()-1,z),w.getBlockState(at(x,bottom()-1,z)),"deep-support");
            var floor=voxel(x,0,z);
            if((x<2||x>12||z<2||z>12)&&floor!=null&&!floor.isAir()&&
                !ForgottenShrineTerrain.stable(w.getBlockState(at(x,-1,z))))return false;
        }
        return true;
    }
    private static final java.util.concurrent.atomic.AtomicInteger REJECTIONS=new java.util.concurrent.atomic.AtomicInteger();
    private static boolean rejected(WorldGenLevel w,BlockPos p,BlockState old,String reason) {
        if(w instanceof net.minecraft.server.level.WorldGenRegion&&System.getProperty("hellqa.seed")!=null&&REJECTIONS.getAndIncrement()<32)
            System.out.println("HELLQA_SHRINE_REJECT_DETAIL "+reason+" pos="+p+" old="+old);
        return false;
    }
    /** Same late-neighbour vegetation race as bridge headroom; never carve rock,
     * fluids, containers or completed chunks. Only this authored AIR envelope. */
    public boolean clearAmbient(WorldGenLevel w,int minX,int minZ,int maxX,int maxZ) {
        if(!placed)return false;
        boolean changed=false;
        for(int x=0;x<15;x++)for(int z=0;z<15;z++)for(int y=bottom();y<=8;y++) {
            var next=voxel(x,y,z);if(next==null||!next.isAir())continue;
            var pos=at(x,y,z);
            if(pos.getX()<minX||pos.getX()>maxX||pos.getZ()<minZ||pos.getZ()>maxZ)continue;
            if(w.getChunk(pos).getPersistedStatus().isOrAfter(net.minecraft.world.level.chunk.status.ChunkStatus.FULL))continue;
            var old=w.getBlockState(pos);
            if(NetherBridgePiece.harmlessFlora(old)&&!old.hasBlockEntity()&&w.ensureCanWrite(pos))
                changed|=w.setBlock(pos,Blocks.AIR.defaultBlockState(),2);
        }
        return changed;
    }
    @Override public void postProcess(WorldGenLevel w,StructureManager m,ChunkGenerator g,RandomSource r,
            BoundingBox clip,ChunkPos cp,BlockPos pivot) {
        if(placed||variant<0||variant>3)return;
        // Noise columns precede cave carving and gravel/lava surface features.
        // Try only the four legal 15x15 footprints in this same chunk, four
        // rotations and nearby elevations. Still zero writes until a whole fit.
        if(!validSite(w,clip)&&w instanceof net.minecraft.server.level.WorldGenRegion) {
            var original=center;var originalRotation=rotation;
            boolean found=false;
            search: for(int dy:new int[]{0,1,-1,2,-2})for(int k=0;k<4;k++)for(int turn=0;turn<4;turn++) {
                center=new BlockPos(cp.getMinBlockX()+7+(k&1),original.getY()+dy,cp.getMinBlockZ()+7+((k>>1)&1));
                rotation=Rotation.values()[(originalRotation.ordinal()+turn)%4];
                if(!ForgottenShrineTerrain.stable(w.getBlockState(at(7,bottom()-1,7))))continue;
                if(validSite(w,clip)){found=true;break search;}
            }
            if(found) {
                boundingBox=new ForgottenShrineTerrain.Plan(center,rotation,variant,seed).bounds();
                // Replace the start object too: its cached bounding box must
                // follow the actual piece, otherwise locate/save retain old Y.
                var structure=w.registryAccess().registryOrThrow(Registries.STRUCTURE).get(ForgottenShrineWorldgen.id("forgotten_shrine"));
                var owner=w.getChunk(cp.x,cp.z,net.minecraft.world.level.chunk.status.ChunkStatus.STRUCTURE_STARTS,false);
                var previous=owner.getStartForStructure(structure);
                if(previous!=null&&previous.isValid())owner.setStartForStructure(structure,
                    new StructureStart(structure,cp,previous.getReferences(),new net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer(java.util.List.of(this))));
                if(System.getProperty("hellqa.seed")!=null)System.out.println("HELLQA_SHRINE_LOCAL_FIT chunk="+cp+" from="+original+" to="+center+" rotation="+rotation);
            } else {center=original;rotation=originalRotation;}
        }
        if(!validSite(w,clip)) {
            var structure=w.registryAccess().registryOrThrow(Registries.STRUCTURE).get(ForgottenShrineWorldgen.id("forgotten_shrine"));
            var owner=w.getChunk(cp.x,cp.z,net.minecraft.world.level.chunk.status.ChunkStatus.STRUCTURE_STARTS,false);
            if(owner!=null&&structure!=null) {
                owner.setStartForStructure(structure,StructureStart.INVALID_START);
                var starts=java.util.Map.copyOf(owner.getAllStarts());
                w.getLevel().getServer().execute(()->m.structureCheck.onStructureLoad(cp,starts));
            }
            if(System.getProperty("hellqa.seed")!=null)System.out.println("HELLQA_SHRINE_REJECT chunk="+cp+" actual terrain unsafe; zero writes");
            return;
        }
        // Foundations first; decorative blocks only after their supports exist.
        for(int pass=0;pass<2;pass++)for(int y=bottom();y<=8;y++)for(int z=0;z<15;z++)for(int x=0;x<15;x++) {
            var next=voxel(x,y,z);if(next==null)continue;
            boolean decor=next.is(Blocks.SOUL_LANTERN)||next.is(Blocks.CAMPFIRE)||next.is(Blocks.CHEST)||next.is(Blocks.POLISHED_BLACKSTONE_BRICK_WALL);
            if(decor!=(pass==1))continue;
            w.setBlock(at(x,y,z),next.rotate(rotation),2);
        }
        var chestPos=at(7,bottom()+1,5);
        if(w.getBlockEntity(chestPos) instanceof RandomizableContainerBlockEntity chest)
            chest.setLootTable(ResourceKey.create(Registries.LOOT_TABLE,ForgottenShrineWorldgen.id("chests/forgotten_shrine")),seed^chestPos.asLong());
        placed=true;
    }
}
