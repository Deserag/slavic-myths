package org.slavicmyths.posad;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import org.slavicmyths.netherstructures.NetherBridgeTerrain;

/** Persisted shell, clipped before any world read/write. No rewards or retrofit here. */
public final class PosadPiece extends StructurePiece {
    private final BlockPos anchor;
    private final int turns;
    private final int schema;
    private final PosadTerrain.Plan terrain;
    private final java.util.Map<Long,Integer> supports=new java.util.HashMap<>();
    private static long key(int x,int z){return ((long)x<<32)^(z&0xffffffffL);}
    public PosadPiece(BlockPos anchor,int turns) {
        super(PosadWorldgen.PIECE.get(),0,new BoundingBox(anchor.getX(),anchor.getY()-2,anchor.getZ(),anchor.getX()+112,anchor.getY()+17,anchor.getZ()+112));
        this.anchor=anchor.immutable(); this.turns=Math.floorMod(turns,4); this.schema=PosadGeometry.SCHEMA_VERSION;
        terrain=new PosadTerrain.Plan(anchor.getX(),anchor.getY(),anchor.getZ(),this.turns,0,java.util.List.of());
    }
    public PosadPiece(PosadTerrain.Plan plan) {
        super(PosadWorldgen.PIECE.get(),0,bounds(plan));
        terrain=plan;anchor=new BlockPos(plan.anchorX(),plan.deckY(),plan.anchorZ());turns=plan.turns();schema=PosadGeometry.SCHEMA_VERSION;
        for(var support:plan.supports())supports.put(key(support.x(),support.z()),support.depth());
    }
    private static BoundingBox bounds(PosadTerrain.Plan plan) {
        var a=plan.world(0,0);var b=plan.world(112,112);
        var c=plan.world(52,112+plan.bridgeLength());var d=plan.world(60,112+plan.bridgeLength());
        return new BoundingBox(Math.min(Math.min(a.x(),b.x()),Math.min(c.x(),d.x())),plan.deckY()-48,
            Math.min(Math.min(a.z(),b.z()),Math.min(c.z(),d.z())),
            Math.max(Math.max(a.x(),b.x()),Math.max(c.x(),d.x())),plan.deckY()+17,
            Math.max(Math.max(a.z(),b.z()),Math.max(c.z(),d.z())));
    }
    public PosadPiece(CompoundTag n) {
        super(PosadWorldgen.PIECE.get(),n);
        anchor=BlockPos.of(n.getLong("Anchor"));turns=Math.floorMod(n.getInt("Turns"),4);schema=n.getInt("Schema");
        var list=new java.util.ArrayList<PosadTerrain.Support>();
        var raw=n.getIntArray("Supports");
        if(raw.length%3!=0||raw.length>900)throw new IllegalArgumentException("Invalid posad supports");
        for(int i=0;i<raw.length;i+=3){var support=new PosadTerrain.Support(raw[i],raw[i+1],raw[i+2]);list.add(support);supports.put(key(support.x(),support.z()),support.depth());}
        int length=n.getInt("BridgeLength");
        if(length<0||length>PosadTerrain.MAX_BRIDGE)throw new IllegalArgumentException("Invalid posad bridge");
        terrain=new PosadTerrain.Plan(anchor.getX(),anchor.getY(),anchor.getZ(),turns,length,list);
    }
    @Override protected void addAdditionalSaveData(StructurePieceSerializationContext c,CompoundTag n) {
        n.putLong("Anchor",anchor.asLong());n.putInt("Turns",turns);n.putInt("Schema",schema);
        n.putInt("BridgeLength",terrain.bridgeLength());
        var raw=new int[terrain.supports().size()*3];int i=0;
        for(var support:terrain.supports()){raw[i++]=support.x();raw[i++]=support.z();raw[i++]=support.depth();}
        n.putIntArray("Supports",raw);
    }
    public net.minecraft.world.level.block.state.BlockState voxel(int x,int y,int z) {
        if(y < -2) {
            int depth=supports.getOrDefault(key(x,z),0);
            return -y<depth?Blocks.POLISHED_BASALT.defaultBlockState():null;
        }
        if(PosadGeometry.CITY.contains(x,z)) {
            if(schema >= 7) {
                var granary=PosadGranary.voxel(x,y,z);
                if(granary!=null)return granary;
            }
            if(schema >= 6) {
                var market=PosadMarket.voxel(x,y,z);
                if(market!=null)return market;
            }
            if(schema >= 5) {
                var hut=PosadHerbalHut.voxel(x,y,z);
                if(hut!=null)return hut;
            }
            // No retrofits: only brand-new schema-4 cities receive visual repairs.
            if(schema >= 4) {
                var repair=PosadVisualRepairs.voxel(x,y,z);
                if(repair!=null)return repair;
            }
            if(schema >= 3) {
                var district=PosadNextBuildings.voxel(x,y,z);
                if(district!=null)return district;
            }
            if(schema >= 2) {
                var building=PosadBuildings.voxel(x,y,z);
                if(building!=null)return building;
            }
            return PosadShell.voxel(x,y,z);
        }
        if(z>112&&z<=112+terrain.bridgeLength()&&x>=52&&x<=60) {
            if(y>=-2&&y<=0)return Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
            if(y==1||y==2)return (x==52||x==60?Blocks.POLISHED_BLACKSTONE_BRICKS:Blocks.AIR).defaultBlockState();
            if(y==3&&x>52&&x<60)return Blocks.AIR.defaultBlockState();
        }
        return null;
    }
    @Override public void postProcess(WorldGenLevel world,StructureManager manager,ChunkGenerator generator,RandomSource random,BoundingBox clip,ChunkPos chunk,BlockPos pivot) {
        // At most 16x16x66 voxel decisions per chunk; never scan the whole city per chunk.
        int x0=Math.max(clip.minX(),boundingBox.minX()),x1=Math.min(clip.maxX(),boundingBox.maxX());
        int z0=Math.max(clip.minZ(),boundingBox.minZ()),z1=Math.min(clip.maxZ(),boundingBox.maxZ());
        for(int wz=z0;wz<=z1;wz++)for(int wx=x0;wx<=x1;wx++) {
            int dx=wx-anchor.getX(),dz=wz-anchor.getZ();
            var local=switch(turns){
                case 0 -> new PosadGeometry.Cell(dx,dz);
                case 1 -> new PosadGeometry.Cell(dz,112-dx);
                case 2 -> new PosadGeometry.Cell(112-dx,112-dz);
                default -> new PosadGeometry.Cell(112-dz,dx);
            };
            for(int y=Math.max(boundingBox.minY(),clip.minY())-anchor.getY();y<=Math.min(anchor.getY()+17,clip.maxY())-anchor.getY();y++) {
                var state=voxel(local.x(),y,local.z());
                if(state==null)continue;
                BlockPos at=new BlockPos(wx,anchor.getY()+y,wz);
                if(!clip.isInside(at)||!world.ensureCanWrite(at))continue;
                var old=world.getBlockState(at);
                if(old.hasBlockEntity()||old.is(Blocks.BEDROCK)||!(old.isAir()||old.is(Blocks.LAVA)||NetherBridgeTerrain.natural(old)))continue;
                if(state.isAir()&&!old.isAir())continue;
                try {
                    var placed=state.rotate(Rotation.values()[turns]);
                    if (state.is(Blocks.CARVED_PUMPKIN)) {
                        // R1 military practice dummies use three original carved pumpkins.
                        // Vanilla onPlace invokes a multi-chunk golem BlockPattern read even
                        // in WorldGenRegion where neighbor states may be inaccessible.
                        // Chunk-local write preserves the EXACT approved block/facing,
                        // skips that unrelated entity-spawn probe and never loads neighbors.
                        var changed=world.getChunk(wx>>4,wz>>4).setBlockState(at,placed,false);
                        if (changed==null) throw new IllegalStateException("Chunk-local pumpkin write refused at "+at);
                    } else {
                        world.setBlock(at,placed,2);
                    }
                } catch(RuntimeException e) {
                    throw new IllegalStateException("Posad placement failed: local "+local.x()+","+y+","+local.z()+" world "+at+" block "+state+" rotation "+turns+" schema "+schema, e);
                }
                if(schema >= 2 && state.is(Blocks.CHEST) && world.getBlockEntity(at) instanceof net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity chest) {
                    String loot=schema >= 3 ? PosadNextBuildings.chestLoot(local.x(),y,local.z()) : null;
                    if(loot==null&&schema>=7)loot=PosadGranary.chestLoot(local.x(),y,local.z());
                    if(loot==null&&schema>=6)loot=PosadMarket.chestLoot(local.x(),y,local.z());
                    if(loot==null&&schema>=5)loot=PosadHerbalHut.chestLoot(local.x(),y,local.z());
                    if(loot==null)loot=PosadBuildings.chestLoot(local.x(),y,local.z());
                    if(loot==null)throw new IllegalStateException("Unexpected posad chest at "+local);
                    chest.setLootTable(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE,net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths",loot)),at.asLong()^anchor.asLong());
                    chest.setChanged();
                }
            }
        }
    }
}
