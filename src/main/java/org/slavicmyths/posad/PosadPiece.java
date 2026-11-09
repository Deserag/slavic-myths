package org.slavicmyths.posad;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import org.slavicmyths.netherstructures.NetherBridgeTerrain;

/** Persisted shell, clipped before any world read/write. No rewards or retrofit here. */
public final class PosadPiece extends StructurePiece {
    private static final java.util.Set<String> NETHER_FURNITURE_KINDS=java.util.Set.of("table","chair","stool","bench","shelf","wardrobe","bedside_cabinet","weapon_rack");

    private static final Object[][] INTERIOR_FURNITURE=new Object[][]{
            {47,1,19,"crimson_bedside_cabinet"},{47,1,24,"crimson_table"},{47,1,25,"crimson_chair"},
            {67,1,25,"crimson_table"},{67,1,26,"crimson_chair"},{68,1,26,"crimson_wardrobe"},
            {63,7,89,"crimson_bedside_cabinet"},{68,7,90,"crimson_bedside_cabinet"},
            {80,1,56,"warped_table"},{81,1,56,"warped_chair"},
            {20,1,79,"crimson_weapon_rack"},{20,7,79,"crimson_weapon_rack"}};
    private final BlockPos anchor;
    private final int turns;
    private final int schema;
    public BlockPos anchor() { return anchor; }
    public int turns() { return turns; }
    private final PosadTerrain.Plan terrain;
    private final java.util.Map<Long,Integer> supports=new java.util.HashMap<>();
    private static long key(int x,int z){return ((long)x<<32)^(z&0xffffffffL);}
    public PosadPiece(BlockPos anchor,int turns) { this(anchor,turns,PosadGeometry.SCHEMA_VERSION); }
    static PosadPiece previousPlan(BlockPos anchor,int turns){return new PosadPiece(anchor,turns,28);}
    private PosadPiece(BlockPos anchor,int turns,int version) {
        super(PosadWorldgen.PIECE.get(),0,new BoundingBox(anchor.getX(),anchor.getY()-2,anchor.getZ(),anchor.getX()+112,anchor.getY()+17,anchor.getZ()+112));
        this.anchor=anchor.immutable(); this.turns=Math.floorMod(turns,4); this.schema=version;
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
    public BlockState voxel(int x,int y,int z) {
        BlockState state = baseVoxel(x,y,z);
        if (schema < 25 || state == null) return state;
        if(schema>=30&&(y==1||y==2||y==7||y==8)&&(state.is(Blocks.CRIMSON_PLANKS)||state.is(Blocks.CRIMSON_SLAB)||state.is(Blocks.WARPED_PLANKS)||state.is(Blocks.WARPED_SLAB))){
            var above=baseVoxel(x,y+1,z);var upper=baseVoxel(x,y+2,z);
            if((above==null||above.isAir())||(upper==null||upper.isAir()))return Blocks.AIR.defaultBlockState();
        }
        if(schema>=30&&state.getBlock() instanceof org.slavicmyths.furniture.FurnitureBlock f&&"shelf".equals(f.kind)){
            var back=state.getValue(org.slavicmyths.furniture.FurnitureBlock.FACING).getOpposite();
            var support=baseVoxel(x+back.getStepX(),y,z+back.getStepZ());
            if(support!=null&&(support.is(Blocks.CRIMSON_PLANKS)||support.is(Blocks.WARPED_PLANKS))){
                var top=baseVoxel(x+back.getStepX(),y+1,z+back.getStepZ());var upper=baseVoxel(x+back.getStepX(),y+2,z+back.getStepZ());
                if(top==null||top.isAir()||upper==null||upper.isAir())return Blocks.AIR.defaultBlockState();
            }
        }
        // Floors are Y0/Y6. R1 planks/slabs above them are obsolete block furniture,
        // not floorboards. Preserve exterior walls, room partitions and actual furniture.
        if(schema>=29&&(y==1||y==2||y==7||y==8)&&x>=15&&x<=33&&z>=17&&z<=27
                &&x!=22&&x!=26&&z!=22&&(state.is(Blocks.CRIMSON_PLANKS)||state.is(Blocks.CRIMSON_SLAB)))
            return Blocks.AIR.defaultBlockState();
        if(schema>=27&&state.getBlock() instanceof org.slavicmyths.furniture.FurnitureBlock furniture
                && !state.is(org.slavicmyths.furniture.Furniture.get("posad_throne"))
                && NETHER_FURNITURE_KINDS.contains(furniture.kind)) {
            String wood=PosadGeometry.Slot.ZELEYNAYA_IZBA.local().contains(x,z)?"warped":"crimson";
            state=org.slavicmyths.furniture.Furniture.get(wood+"_"+furniture.kind).withPropertiesOf(state);
        }
        if(schema>=28&&PosadGeometry.Slot.VECHE_HALL.local().contains(x,z)&&y<=11){
            if(state.is(Blocks.POLISHED_BLACKSTONE_BRICKS))state=Blocks.QUARTZ_BRICKS.defaultBlockState();
            else if(state.is(Blocks.CHISELED_POLISHED_BLACKSTONE))state=Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState();
            else if(state.is(Blocks.POLISHED_BASALT))state=Blocks.QUARTZ_PILLAR.withPropertiesOf(state);
            else if(y==0&&state.is(Blocks.BLACKSTONE))state=Blocks.SMOOTH_QUARTZ.defaultBlockState();
        }
        if (state.is(Blocks.IRON_BARS)) return Blocks.BLACK_STAINED_GLASS_PANE.defaultBlockState();
        if (state.getBlock() instanceof net.minecraft.world.level.block.DoorBlock) {
            var door=pairedDoor(state,x,y,z);
            if(schema>=27){
                var hinge=door.getValue(net.minecraft.world.level.block.DoorBlock.HINGE);
                door=door.setValue(net.minecraft.world.level.block.DoorBlock.FACING,door.getValue(net.minecraft.world.level.block.DoorBlock.FACING).getOpposite())
                    .setValue(net.minecraft.world.level.block.DoorBlock.HINGE,hinge==net.minecraft.world.level.block.state.properties.DoorHingeSide.LEFT?net.minecraft.world.level.block.state.properties.DoorHingeSide.RIGHT:net.minecraft.world.level.block.state.properties.DoorHingeSide.LEFT);
            }
            return door;
        }
        return resolvedRoofSolid(state,x,y,z);
    }

    private BlockState pairedDoor(BlockState door,int x,int y,int z) {
        var facing = door.getValue(net.minecraft.world.level.block.DoorBlock.FACING);
        var right = facing.getClockWise();
        var left = right.getOpposite();
        boolean hasRight = matchingDoor(door,baseVoxel(x+right.getStepX(),y,z+right.getStepZ()));
        boolean hasLeft = matchingDoor(door,baseVoxel(x+left.getStepX(),y,z+left.getStepZ()));
        if (hasLeft == hasRight) return door;
        return door.setValue(net.minecraft.world.level.block.DoorBlock.HINGE, hasRight
            ? net.minecraft.world.level.block.state.properties.DoorHingeSide.LEFT
            : net.minecraft.world.level.block.state.properties.DoorHingeSide.RIGHT);
    }

    private static boolean matchingDoor(BlockState door,BlockState neighbor) {
        return neighbor != null && neighbor.is(door.getBlock())
            && neighbor.getValue(net.minecraft.world.level.block.DoorBlock.FACING) == door.getValue(net.minecraft.world.level.block.DoorBlock.FACING)
            && neighbor.getValue(net.minecraft.world.level.block.DoorBlock.HALF) == door.getValue(net.minecraft.world.level.block.DoorBlock.HALF);
    }

    private BlockState baseVoxel(int x,int y,int z) {
        if(schema>=31){
            var bed=PosadBeds.extraVoxel(x,y,z);if(bed!=null)return bed;
            var furnishing=interiorVoxel(x,y,z);if(furnishing!=null)return furnishing;
        }
        return unfurnishedVoxel(x,y,z);
    }
    private BlockState interiorVoxel(int x,int y,int z){
        if(y<1||y>16)return null;
        // Only the fixed authored interior is inspected, never live chunks or inventories.
        var old=unfurnishedVoxel(x,y,z);if(old!=null&&!old.isAir())return null;
        if(((x==17||x==25||x==33)&&z==85&&(y==1||y==2||y==7||y==8))||(x==25&&z==77&&(y==7||y==8))){
            if(y==2||y==8)return Blocks.LANTERN.defaultBlockState();
            return org.slavicmyths.furniture.Furniture.get("crimson_bedside_cabinet").defaultBlockState();
        }
        if(((x%4==0&&z%4==0)||(x==90&&z==24)||(x==26&&z==48)||(x==28&&z==46)||(x==34&&z==84)||(x==24&&z==78))&&x>=12&&x<=101&&z>=12&&z<=99){
            for(int floor:new int[]{0,6})if(y>=floor+4){
                var ground=unfurnishedVoxel(x,floor,z);var feet=unfurnishedVoxel(x,floor+1,z);
                if(feet!=null&&!feet.isAir()&&!(feet.getBlock() instanceof org.slavicmyths.furniture.FurnitureBlock)&&!feet.is(PosadBeds.COT.get()))continue;
                if(ground==null||ground.getCollisionShape(net.minecraft.world.level.EmptyBlockGetter.INSTANCE,BlockPos.ZERO).isEmpty())continue;
                int ceiling=-1;
                for(int h=floor+4;h<=17;h++){
                    var roof=unfurnishedVoxel(x,h,z);
                    if(roof!=null&&!roof.isAir()){
                        if(roof.isFaceSturdy(net.minecraft.world.level.EmptyBlockGetter.INSTANCE,BlockPos.ZERO,Direction.DOWN))ceiling=h;
                        break;
                    }
                }
                if(ceiling>floor+4&&y<ceiling){
                    if(y==floor+4)return Blocks.LANTERN.defaultBlockState().setValue(net.minecraft.world.level.block.LanternBlock.HANGING,true);
                    return Blocks.CHAIN.defaultBlockState();
                }
            }
        }
        for(var item:INTERIOR_FURNITURE){
            if(x==(int)item[0]&&y==(int)item[1]&&z==(int)item[2]){
                var ground=unfurnishedVoxel(x,y-1,z);var above=unfurnishedVoxel(x,y+1,z);
                if(ground!=null&&ground.isCollisionShapeFullBlock(net.minecraft.world.level.EmptyBlockGetter.INSTANCE,BlockPos.ZERO)&&(above==null||above.isAir()))
                    return org.slavicmyths.furniture.Furniture.get((String)item[3]).defaultBlockState();
            }
        }
        return null;
    }
    private BlockState unfurnishedVoxel(int x,int y,int z) {
        if(schema>=30){var bed=PosadBeds.additionalVoxel(x,y,z);if(bed!=null)return bed;}
        if(schema>=29&&x>=54&&x<=58&&z>=13&&z<=17&&y>=1&&y<=3){
            if(x==56&&z==14&&y==1)return org.slavicmyths.furniture.Furniture.get("posad_throne").defaultBlockState().setValue(org.slavicmyths.furniture.FurnitureBlock.FACING,Direction.SOUTH);
            if(x>=55&&x<=57&&z>=14&&z<=15)return Blocks.AIR.defaultBlockState();
            var original=PosadArchitectureR3.voxel(x,y,z);if(original==null)original=PosadBuildings.voxel(x,y,z);
            if(original!=null&&!original.is(Blocks.CRIMSON_PLANKS)&&!original.is(Blocks.CRIMSON_SLAB)&&!(original.getBlock() instanceof StairBlock))return original;
            return Blocks.AIR.defaultBlockState();
        }
        if(schema>=28&&(x==85||x==91)&&((z>=77&&z<=83)||(z>=86&&z<=91))&&y>=1&&y<=5){
            int doorZ=z<=83?80:88;
            if(z==doorZ&&y<=2)return Blocks.CRIMSON_DOOR.defaultBlockState().setValue(net.minecraft.world.level.block.DoorBlock.FACING,x==85?Direction.EAST:Direction.WEST).setValue(net.minecraft.world.level.block.DoorBlock.HALF,y==1?net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER:net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER);
            return Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
        }
        if(schema>=28&&(y==2||y==8)&&x>=16&&x<=32&&z>=17&&z<=27&&!(x>21&&x<27)&&z!=22){
            var oldFurniture=PosadBuildings.voxel(x,y,z);
            if(oldFurniture!=null&&(oldFurniture.is(Blocks.CRIMSON_PLANKS)||oldFurniture.is(Blocks.CRIMSON_SLAB)))return Blocks.AIR.defaultBlockState();
        }
        // Owner feedback 2026-11-05: replace the council's block-built throne.
        if(schema>=28&&x>=55&&x<=57&&z>=14&&z<=15&&y>=2&&y<=3){
            if(x==56&&z==14&&y==2)return org.slavicmyths.furniture.Furniture.get("posad_throne").defaultBlockState().setValue(org.slavicmyths.furniture.FurnitureBlock.FACING,Direction.SOUTH);
            return Blocks.AIR.defaultBlockState();
        }
        // Replace the reception chair with one coherent, fireproof throne model.
        // Historic pieces retain their original furniture and saved inventories.
        if(schema>=27&&x==69&&y==1&&z==82)return Blocks.AIR.defaultBlockState();
        if(schema>=31&&x==70&&y==1&&z==82)return org.slavicmyths.furniture.Furniture.get("crimson_chair").defaultBlockState().setValue(org.slavicmyths.furniture.FurnitureBlock.FACING,Direction.WEST);
        if(schema>=27&&x==70&&y==1&&z==82)return org.slavicmyths.furniture.Furniture.get("posad_throne")
            .defaultBlockState().setValue(org.slavicmyths.furniture.FurnitureBlock.FACING,Direction.WEST);
        if(schema>=27){var cot=PosadBeds.voxel(x,y,z,schema>=28);if(cot!=null)return cot;var hall=PosadHallDominant.voxel(x,y,z);if(hall!=null)return hall;}
        if (schema >= 25 && y > 0 && PosadGeometry.CITY.contains(x,z)) {
            var repair = PosadBuildingRepairs.voxel(x,y,z,schema >= 26);
            if (repair != null) return repair;
        }
        if(y < -2) {
            int depth=supports.getOrDefault(key(x,z),0);
            return -y<depth?Blocks.POLISHED_BASALT.defaultBlockState():null;
        }
        if(PosadGeometry.CITY.contains(x,z)) {
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
    private static boolean sameRoofRun(BlockState stair, BlockState neighbor) {
        return neighbor != null && neighbor.getBlock()==stair.getBlock() && neighbor.getBlock() instanceof StairBlock
            && neighbor.getValue(StairBlock.FACING)==stair.getValue(StairBlock.FACING)
            && neighbor.getValue(StairBlock.HALF)==stair.getValue(StairBlock.HALF);
    }
    private static BlockState roofSolidMaterial(BlockState stair) {
        if(stair.is(Blocks.NETHER_BRICK_STAIRS))return Blocks.NETHER_BRICKS.defaultBlockState();
        if(stair.is(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS))return Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
        if(stair.is(Blocks.CRIMSON_STAIRS))return Blocks.CRIMSON_PLANKS.defaultBlockState();
        if(stair.is(Blocks.BLACKSTONE_STAIRS))return Blocks.BLACKSTONE.defaultBlockState();
        if(stair.is(Blocks.POLISHED_BLACKSTONE_STAIRS))return Blocks.POLISHED_BLACKSTONE.defaultBlockState();
        return null;
    }
    /** New cities: the inner member of a two-cell stair row must be solid,
     * not the upside-down TOP stair introduced by schema 21. */
    private boolean roofLevel(int x,int y,int z){
        if(x>=13&&x<=35&&z>=15&&z<=29)return y>=11;
        if(x>=12&&x<=39&&z>=75&&z<=88)return y>=11;
        if(PosadGeometry.Slot.VECHE_HALL.local().contains(x,z))return y>=12;
        if(PosadGeometry.POSADNIK_TEREM_ENVELOPE.contains(x,z))return y>=12;
        return y>=4;
    }
    private BlockState resolvedRoofSolid(BlockState stair,int x,int y,int z) {
        if(schema>=30&&stair.getBlock() instanceof StairBlock&&roofLevel(x,y,z)){
            var axis=stair.getValue(StairBlock.FACING).getAxis();Direction uphill=stair.getValue(StairBlock.FACING);
            int best=0;
            for(var direction:Direction.Plane.HORIZONTAL){if(direction.getAxis()!=axis)continue;
                for(int distance=1;distance<=4;distance++)for(int rise=1;rise<=3;rise++){
                    var n=baseVoxel(x+direction.getStepX()*distance,y+rise,z+direction.getStepZ()*distance);
                    if(n!=null&&(n.is(Blocks.NETHER_BRICKS)||n.is(Blocks.NETHER_BRICK_STAIRS)||n.is(Blocks.POLISHED_BLACKSTONE_BRICKS)||n.is(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS)||n.is(Blocks.CRIMSON_STAIRS)||n.is(Blocks.CRIMSON_PLANKS))){int score=20-distance*4+rise;if(score>best){best=score;uphill=direction;}}
                }
            }
            // At the crest no higher course exists: its solid ridge (or the
            // supported opposite half of a two-stair ridge) still defines inward.
            if(best==0)for(var direction:Direction.Plane.HORIZONTAL){if(direction.getAxis()!=axis)continue;
                for(int distance=1;distance<=3;distance++){
                    int nx=x+direction.getStepX()*distance,nz=z+direction.getStepZ()*distance;var n=baseVoxel(nx,y,nz);var support=baseVoxel(nx,y-1,nz);
                    boolean solid=n!=null&&(n.is(Blocks.NETHER_BRICKS)||n.is(Blocks.POLISHED_BLACKSTONE_BRICKS)||n.is(Blocks.CHISELED_POLISHED_BLACKSTONE)||n.is(Blocks.CRIMSON_PLANKS));
                    boolean ridge=n!=null&&n.getBlock() instanceof StairBlock&&support!=null&&(support.is(Blocks.NETHER_BRICKS)||support.is(Blocks.POLISHED_BLACKSTONE_BRICKS));
                    int score=(solid?10:ridge?5:0)-distance;if(score>best){best=score;uphill=direction;}
                }
            }
            var toward=baseVoxel(x+uphill.getStepX(),y,z+uphill.getStepZ());
            var away=baseVoxel(x-uphill.getStepX(),y,z-uphill.getStepZ());
            boolean inner=toward!=null&&toward.getBlock() instanceof StairBlock&&toward.getValue(StairBlock.FACING).getAxis()==axis;
            boolean outer=away!=null&&away.getBlock() instanceof StairBlock&&away.getValue(StairBlock.FACING).getAxis()==axis;
            if(outer&&!inner){var solid=roofSolidMaterial(stair);if(solid!=null)return solid;}
            return stair.setValue(StairBlock.FACING,uphill).setValue(StairBlock.HALF,net.minecraft.world.level.block.state.properties.Half.BOTTOM).setValue(StairBlock.SHAPE,StairsShape.STRAIGHT);
        }
        if(schema>=27&&PosadGeometry.Slot.VECHE_HALL.local().contains(x,z))return stair;
        if(schema<23 || y<5 || !(stair.getBlock() instanceof StairBlock)
            || stair.getValue(StairBlock.HALF)!=net.minecraft.world.level.block.state.properties.Half.BOTTOM)return stair;
        Direction facing=stair.getValue(StairBlock.FACING);
        BlockState outward=baseVoxel(x+facing.getStepX(),y,z+facing.getStepZ());
        BlockState inward=baseVoxel(x-facing.getStepX(),y,z-facing.getStepZ());
        BlockState outward2=baseVoxel(x+2*facing.getStepX(),y,z+2*facing.getStepZ());
        if(!sameRoofRun(stair,outward)||sameRoofRun(stair,inward)||sameRoofRun(stair,outward2))return stair;
        BlockState solid=roofSolidMaterial(stair);
        if(solid==null)throw new IllegalStateException("Unknown roof stair at "+x+","+y+","+z+": "+stair);
        return solid;
    }
    private BlockState resolvedRoofHalf(BlockState stair,int x,int y,int z) {
        if(schema>=23)return stair;
        if(schema<21 || y<5 || !(stair.getBlock() instanceof StairBlock)
            || stair.getValue(StairBlock.HALF)!=net.minecraft.world.level.block.state.properties.Half.BOTTOM)return stair;
        Direction facing=stair.getValue(StairBlock.FACING);
        BlockState outward=voxel(x+facing.getStepX(),y,z+facing.getStepZ());
        BlockState inward=voxel(x-facing.getStepX(),y,z-facing.getStepZ());
        BlockState outward2=voxel(x+2*facing.getStepX(),y,z+2*facing.getStepZ());
        if(sameRoofRun(stair,outward) && !sameRoofRun(stair,inward) && !sameRoofRun(stair,outward2))
            return stair.setValue(StairBlock.HALF,net.minecraft.world.level.block.state.properties.Half.TOP);
        return stair;
    }
    private BlockState resolvedRoofHalfAt(int x,int y,int z) {
        BlockState neighbor=voxel(x,y,z);
        return neighbor==null||schema>=25?neighbor:(schema>=23?resolvedRoofSolid(neighbor,x,y,z):resolvedRoofHalf(neighbor,x,y,z));
    }
    private static boolean sameStairHalf(BlockState stair, BlockState neighbor) {
        return neighbor != null && neighbor.getBlock() instanceof StairBlock
            && neighbor.getValue(StairBlock.HALF) == stair.getValue(StairBlock.HALF);
    }
    private boolean freeCornerSide(BlockState stair, int x, int y, int z, Direction side) {
        BlockState neighbor=resolvedRoofHalfAt(x+side.getStepX(),y,z+side.getStepZ());
        return !sameStairHalf(stair,neighbor) || neighbor.getValue(StairBlock.FACING) != stair.getValue(StairBlock.FACING);
    }
    private BlockState resolvedRoofStair(BlockState stair, int x, int y, int z) {
        if(schema>=30&&roofLevel(x,y,z))return stair;
        // Schema 20+ resolves stair corner shape from the final authored graph
        // for every structural stair, not only roof rows. Schema 21 additionally
        // normalizes paired roof stairs into a continuous half-step pitch.
        if(schema<20 || !(stair.getBlock() instanceof StairBlock))return stair;
        if(schema<25)stair=resolvedRoofSolid(stair,x,y,z);
        if(!(stair.getBlock() instanceof StairBlock))return stair;
        stair=resolvedRoofHalf(stair,x,y,z);
        Direction facing=stair.getValue(StairBlock.FACING);
        BlockState forward=resolvedRoofHalfAt(x+facing.getStepX(),y,z+facing.getStepZ());
        if(sameStairHalf(stair,forward)) {
            Direction turn=forward.getValue(StairBlock.FACING);
            if(turn.getAxis()!=facing.getAxis() && freeCornerSide(stair,x,y,z,turn.getOpposite()))
                return stair.setValue(StairBlock.SHAPE,turn==facing.getCounterClockWise()?StairsShape.OUTER_LEFT:StairsShape.OUTER_RIGHT);
        }
        BlockState backward=resolvedRoofHalfAt(x-facing.getStepX(),y,z-facing.getStepZ());
        if(sameStairHalf(stair,backward)) {
            Direction turn=backward.getValue(StairBlock.FACING);
            if(turn.getAxis()!=facing.getAxis() && freeCornerSide(stair,x,y,z,turn))
                return stair.setValue(StairBlock.SHAPE,turn==facing.getCounterClockWise()?StairsShape.INNER_LEFT:StairsShape.INNER_RIGHT);
        }
        return stair.setValue(StairBlock.SHAPE,StairsShape.STRAIGHT);
    }
    /** Final block state shared with explicit standalone building placement. */
    public BlockState placedState(int x,int y,int z,WorldGenLevel world,BlockPos position){
        var state=voxel(x,y,z);return state==null?null:resolvedConnections(state,x,y,z,world,position);
    }
    /** Resolve iron bars/glass panes from final city-local voxels, not chunk placement order. */
    private net.minecraft.world.level.block.state.BlockState resolvedConnections(net.minecraft.world.level.block.state.BlockState local,int x,int y,int z,WorldGenLevel world,BlockPos worldPos) {
        net.minecraft.world.level.block.state.BlockState state=resolvedRoofStair(local,x,y,z).rotate(Rotation.values()[turns]);
        if(!(state.getBlock() instanceof net.minecraft.world.level.block.IronBarsBlock)
                && !(schema>=27&&state.getBlock() instanceof net.minecraft.world.level.block.FenceBlock))return state;
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
                boolean cutTerrain = schema >= 25 && world instanceof net.minecraft.server.level.WorldGenRegion
                    && y > 0 && y <= PosadTerrain.MAX_TERRAIN_CUT
                    && PosadGeometry.CITY.contains(local.x(),local.z());
                boolean freshInterior=schema>=27&&world instanceof net.minecraft.server.level.WorldGenRegion
                    &&PosadGeometry.CITY.contains(local.x(),local.z())&&y>=1&&y<=17;
                if ((cutTerrain||freshInterior) && state == null) state = Blocks.AIR.defaultBlockState();
                if(state==null || lateAttachment(state)!=(pass==1))continue;
                BlockPos at=new BlockPos(wx,anchor.getY()+y,wz);
                if(!clip.isInside(at)||!world.ensureCanWrite(at))continue;
                var old=world.getBlockState(at);
                boolean generatedVegetation=schema>=27&&world instanceof net.minecraft.server.level.WorldGenRegion
                    &&(org.slavicmyths.worldgen.LandTerrain.vegetation(old)||old.is(Blocks.NETHER_WART_BLOCK)||old.is(Blocks.WARPED_WART_BLOCK)||old.is(Blocks.SHROOMLIGHT)||old.is(Blocks.WEEPING_VINES)||old.is(Blocks.WEEPING_VINES_PLANT)||old.is(Blocks.TWISTING_VINES)||old.is(Blocks.TWISTING_VINES_PLANT)||old.is(Blocks.GLOW_LICHEN));
                if(old.hasBlockEntity()||old.is(Blocks.BEDROCK)||!(old.isAir()||old.is(Blocks.LAVA)||NetherBridgeTerrain.natural(old)||generatedVegetation))continue;
                if(state.isAir()&&!old.isAir() && !(cutTerrain && NetherBridgeTerrain.natural(old)) && !generatedVegetation && !(schema>=17 && PosadHallR2.isOpening(local.x(),y,local.z())))continue;
                try {
                    var placed=resolvedConnections(state,local.x(),y,local.z(),world,at);
                    if (schema < 25 && state.getBlock() instanceof net.minecraft.world.level.block.IronBarsBlock) {
                        world.setBlock(at,placed,3);
                    } else if (state.is(Blocks.CARVED_PUMPKIN) || shelf(state)
                        || state.getBlock() instanceof net.minecraft.world.level.block.IronBarsBlock) {
                        // Carved pumpkins avoid vanilla multi-chunk golem probes. Wall
                        // panes/shelves are deferred until masonry and doors exist.
                        // Chunk-local writes preserve precisely resolved pane connections
                        // and prevent later neighbor-shape churn across chunk boundaries.
                        var changed=world.getChunk(wx>>4,wz>>4).setBlockState(at,placed,false);
                        if (changed==null) throw new IllegalStateException("Chunk-local attachment write refused at "+at);
                        if(world instanceof net.minecraft.server.level.ServerLevel server)server.sendBlockUpdated(at,old,placed,2);
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
                    String loot = schema >= 25 ? PosadBuildingRepairs.chestLoot(local.x(),y,local.z()) : null;
                    if(loot==null && schema >= 9)loot=PosadMarketRenovation.chestLoot(local.x(),y,local.z());
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
