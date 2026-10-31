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
    public BlockPos cityAnchor() { return anchor; }
    public int cityTurns() { return turns; }
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
            if(schema >= 21) {
                var stairRepair=PosadArchitectureR4.voxel(x,y,z);
                if(stairRepair!=null)return stairRepair;
            }
            if(schema >= 20) {
                var architecture=PosadArchitectureR3.voxel(x,y,z);
                if(architecture!=null)return architecture;
            }
            if(schema >= 19) {
                var posadnik=PosadnikTerem.voxel(x,y,z);
                if(posadnik!=null)return posadnik;
            }
            if(schema >= 18) {
                var light=PosadLightingR3.voxel(x,y,z);
                if(light!=null)return light;
            }
            if(schema >= 17) {
                var hallR2=PosadHallR2.voxel(x,y,z);
                if(hallR2!=null)return hallR2;
            }
            if(schema >= 16) {
                var easternRoof=PosadEasternRoofRepair.voxel(x,y,z);
                if(easternRoof!=null)return easternRoof;
            }
            if(schema >= 15) {
                var chimney=PosadForgeChimneyRoofRepair.voxel(x,y,z);
                if(chimney!=null)return chimney;
            }
            if(schema >= 14) {
                var districtRoof=PosadDistrictRoofRepair.voxel(x,y,z);
                if(districtRoof!=null)return districtRoof;
            }
            if(schema >= 13) {
                var roof=PosadHallRoofRepair.voxel(x,y,z);
                if(roof!=null)return roof;
            }
            if(schema >= 12) {
                var comfort=PosadHallComfort.voxel(x,y,z);
                if(comfort!=null)return comfort;
            }
            if(schema >= 11) {
                var entrance=PosadDruzhinaEntrances.voxel(x,y,z);
                if(entrance!=null)return entrance;
            }
            if(schema >= 10) {
                var courtyards=PosadCourtyardFinishes.voxel(x,y,z);
                if(courtyards!=null)return courtyards;
            }
            if(schema >= 9) {
                var marketInterior=PosadMarketRenovation.voxel(x,y,z);
                if(marketInterior!=null)return marketInterior;
            }
            if(schema >= 8) {
                var polishedHall=PosadHallRefinement.voxel(x,y,z);
                if(polishedHall!=null)return polishedHall;
            }
            if(schema >= 7) {
                var house=PosadHouseRenovation.voxel(x,y,z);
                if(house!=null)return house;
            }
            if(schema >= 6) {
                var finish=PosadFinishes.voxel(x,y,z);
                if(finish!=null)return finish;
            }
            if(schema >= 5) {
                var market=PosadMarketBuildings.voxel(x,y,z);
                if(market!=null)return market;
            }
            if(schema >= 4) {
                var district=PosadAdditionalBuildings.voxel(x,y,z);
                if(district!=null)return district;
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
    private static boolean shelf(net.minecraft.world.level.block.state.BlockState state) {
        return state.getBlock() instanceof org.slavicmyths.furniture.FurnitureBlock f && "shelf".equals(f.kind);
    }
    private static boolean lateAttachment(net.minecraft.world.level.block.state.BlockState state) {
        if(shelf(state) || state.getBlock() instanceof net.minecraft.world.level.block.IronBarsBlock || state.is(Blocks.WALL_TORCH) || state.is(Blocks.SOUL_WALL_TORCH))return true;
        return (state.is(Blocks.LANTERN) || state.is(Blocks.SOUL_LANTERN))
            && state.getValue(net.minecraft.world.level.block.LanternBlock.HANGING);
    }
    /** Resolve iron bars/glass panes from final city-local voxels, not chunk placement order. */
    private net.minecraft.world.level.block.state.BlockState resolvedConnections(net.minecraft.world.level.block.state.BlockState local,int x,int y,int z,WorldGenLevel world,BlockPos worldPos) {
        net.minecraft.world.level.block.state.BlockState state=local.rotate(Rotation.values()[turns]);
        if(!(state.getBlock() instanceof net.minecraft.world.level.block.IronBarsBlock))return state;
        for(var direction:new net.minecraft.core.Direction[]{net.minecraft.core.Direction.NORTH,net.minecraft.core.Direction.EAST,net.minecraft.core.Direction.SOUTH,net.minecraft.core.Direction.WEST}) {
            // Direction is in the rotated WORLD frame. Map neighbor back to city coordinates.
            int dx=direction.getStepX(),dz=direction.getStepZ(),lx=x,lz=z;
            // World -> city inverse of the city's clockwise transform.
            for(int i=0;i<turns;i++){int t=dx;dx=dz;dz=-t;}
            lx+=dx;lz+=dz;
            var neighbor=voxel(lx,y,lz);
            if(neighbor==null)neighbor=Blocks.AIR.defaultBlockState();
            neighbor=neighbor.rotate(Rotation.values()[turns]);
            if(neighbor.getBlock() instanceof net.minecraft.world.level.block.DoorBlock) {
                // Door blockstates are two-high and their final support/shape
                // differs while the opposite half has not yet been written.
                // Never grow a pane arm into a hinged door leaf.
                net.minecraft.world.level.block.state.properties.BooleanProperty connection=switch(direction){
                    case NORTH -> net.minecraft.world.level.block.IronBarsBlock.NORTH;
                    case EAST -> net.minecraft.world.level.block.IronBarsBlock.EAST;
                    case SOUTH -> net.minecraft.world.level.block.IronBarsBlock.SOUTH;
                    case WEST -> net.minecraft.world.level.block.IronBarsBlock.WEST;
                    default -> throw new IllegalStateException("Non-horizontal pane direction");
                };
                state=state.setValue(connection,false);
            } else {
                state=state.updateShape(direction,neighbor,world,worldPos,worldPos.relative(direction));
            }
        }
        return state;
    }
    @Override public void postProcess(WorldGenLevel world,StructureManager manager,ChunkGenerator generator,RandomSource random,BoundingBox clip,ChunkPos chunk,BlockPos pivot) {
        // Two bounded 16x16x66 passes: masonry/doors first; panes, wall attachments and hanging lights second.
        int x0=Math.max(clip.minX(),boundingBox.minX()),x1=Math.min(clip.maxX(),boundingBox.maxX());
        int z0=Math.max(clip.minZ(),boundingBox.minZ()),z1=Math.min(clip.maxZ(),boundingBox.maxZ());
        for(int pass=0;pass<2;pass++) {
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
                if(state==null || lateAttachment(state)!=(pass==1))continue;
                BlockPos at=new BlockPos(wx,anchor.getY()+y,wz);
                if(!clip.isInside(at)||!world.ensureCanWrite(at))continue;
                var old=world.getBlockState(at);
                if(old.hasBlockEntity()||old.is(Blocks.BEDROCK)||!(old.isAir()||old.is(Blocks.LAVA)||NetherBridgeTerrain.natural(old)))continue;
                if(state.isAir()&&!old.isAir() && !(schema>=17 && PosadHallR2.isOpening(local.x(),y,local.z())))continue;
                try {
                    var placed=resolvedConnections(state,local.x(),y,local.z(),world,at);
                    if (state.getBlock() instanceof net.minecraft.world.level.block.IronBarsBlock) {
                        // Notify the active worldgen level so the initial chunk packet
                        // contains connected bars without depending on a client relog.
                        world.setBlock(at,placed,3);
                    } else if (state.is(Blocks.CARVED_PUMPKIN)) {
                        // Avoid vanilla snow/iron golem spawning while placing an authored pumpkin.
                        var changed=world.getChunk(wx>>4,wz>>4).setBlockState(at,placed,false);
                        if (changed==null) throw new IllegalStateException("Chunk-local carved-pumpkin write refused at "+at);
                    } else if (shelf(state)) {
                        // A shelf is a wall attachment. Notify the world after its supporting wall
                        // is placed so the blockstate, neighbors and initial chunk packet agree.
                        if (!world.setBlock(at,placed,3)) throw new IllegalStateException("Worldgen shelf placement refused at "+at);
                    } else {
                        world.setBlock(at,placed,2);
                    }
                } catch(RuntimeException e) {
                    throw new IllegalStateException("Posad placement failed: local "+local.x()+","+y+","+local.z()+" world "+at+" block "+state+" rotation "+turns+" schema "+schema, e);
                }
                if(schema >= 6 && state.is(Blocks.BARREL) && world.getBlockEntity(at) instanceof net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity barrel) {
                    String table=PosadFinishes.barrelLoot(local.x(),local.z());
                    barrel.setLootTable(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE,net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths",table)),at.asLong()^anchor.asLong());
                    barrel.setChanged();
                }
                if(schema >= 2 && state.is(Blocks.CHEST) && world.getBlockEntity(at) instanceof net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity chest) {
                    String loot=schema >= 9 ? PosadMarketRenovation.chestLoot(local.x(),y,local.z()) : null;
                    if(loot==null && schema >= 7)loot=PosadHouseRenovation.chestLoot(local.x(),y,local.z());
                    if(loot==null && schema >= 6)loot=PosadFinishes.chestLoot(local.x(),y,local.z());
                    if(loot==null && schema >= 5)loot=PosadMarketBuildings.chestLoot(local.x(),y,local.z());
                    if(loot==null && schema >= 4)loot=PosadAdditionalBuildings.chestLoot(local.x(),y,local.z());
                    if(loot==null && schema >= 3)loot=PosadNextBuildings.chestLoot(local.x(),y,local.z());
                    if(loot==null)loot=PosadBuildings.chestLoot(local.x(),y,local.z());
                    if(loot==null)throw new IllegalStateException("Unexpected posad chest at "+local);
                    chest.setLootTable(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE,net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths",loot)),at.asLong()^anchor.asLong());
                    chest.setChanged();
                }
            }
        }
        }
    }
}
