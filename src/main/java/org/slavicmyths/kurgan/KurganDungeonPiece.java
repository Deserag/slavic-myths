package org.slavicmyths.kurgan;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.util.RandomSource;

import java.util.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.nbt.*;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.*;
import net.minecraft.sounds.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.*;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.*;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.Difficulty;
import net.minecraft.world.BossEvent;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.server.level.ServerLevel;
import org.slavicmyths.registry.ModBlocks;

/** Accepted immutable plan; placement is deterministic and clipped to the current chunk. */
public final class KurganDungeonPiece extends StructurePiece {
    public final UUID id;public final BlockPos origin;public final KurganPlan plan;
    private final Set<Integer> filled=new HashSet<>();private boolean registered;
    public KurganDungeonPiece(KurganPlan p,BlockPos origin,UUID id){super(KurganStructures.DUNGEON.get(),0,box(p,origin));plan=p;this.origin=origin;this.id=id;bounds();}
    public KurganDungeonPiece(StructureTemplateManager manager,CompoundTag n){super(KurganStructures.DUNGEON.get(),n);plan=KurganPlanNbt.read(n.getCompound("Plan"));origin=BlockPos.of(n.getLong("Origin"));id=n.getUUID("Id");for(int i:n.getIntArray("Filled"))filled.add(i);bounds();}
    private static BoundingBox box(KurganPlan p,BlockPos origin){KurganPlan.Box b=p.bounds();return new BoundingBox(origin.getX()+b.x0,origin.getY()+b.y0,origin.getZ()+b.z0,origin.getX()+b.x1,origin.getY()+b.y1,origin.getZ()+b.z1);}
    private void bounds(){KurganPlan.Box b=plan.bounds();boundingBox=new BoundingBox(origin.getX()+b.x0,origin.getY()+b.y0,origin.getZ()+b.z0,origin.getX()+b.x1,origin.getY()+b.y1,origin.getZ()+b.z1);}
    public BlockPos arrival(){return origin.offset(0,1,plan.radius+2);}
    @Override protected void addAdditionalSaveData(StructurePieceSerializationContext context,CompoundTag n){n.put("Plan",KurganPlanNbt.write(plan));n.putLong("Origin",origin.asLong());n.putUUID("Id",id);n.putIntArray("Filled",filled.stream().mapToInt(Integer::intValue).toArray());}
    private void put(WorldGenLevel w,BoundingBox clip,int x,int y,int z,BlockState s){BlockPos p=origin.offset(x,y,z);if(clip.isInside(p))w.setBlock(p,s,2);}
    private BlockState masonry(int palette,int x,int y,int z){int patch=Math.floorMod(Math.floorDiv(x,4)*19+Math.floorDiv(z,4)*13+Math.floorDiv(y,3)*7+(int)plan.seed,20);int v=patch<12?0:patch<17?1:patch<19?2:3;return KurganBlocks.stone(patch==19?1-palette:palette,v);}
    private boolean inClip(BoundingBox clip,KurganPlan.Box b){return clip.intersects(new BoundingBox(origin.getX()+b.x0,origin.getY()+b.y0,origin.getZ()+b.z0,origin.getX()+b.x1,origin.getY()+b.y1,origin.getZ()+b.z1));}
    @Override public synchronized void postProcess(WorldGenLevel w,StructureManager sm,ChunkGenerator g,RandomSource random,BoundingBox clip,ChunkPos chunk,BlockPos pivot){
        // SavedData is server-thread-owned; worldgen workers only enqueue the immutable instance.
        if(!registered){registered=true;ServerLevel server=w.getLevel();server.getServer().execute(()->BurialRecords.get(server).register(new KurganInstance(id,origin,plan)));}
        mound(w,g,clip);
        for(KurganPlan.Room room:plan.rooms)if(inClip(clip,room.box()))room(w,clip,room);
        for(KurganPlan.Link link:plan.links)corridor(w,clip,link);
        for(KurganPlan.Room room:plan.rooms)if(inClip(clip,room.box()))decorate(w,clip,room);
        return;
    }
    private void mound(WorldGenLevel w,ChunkGenerator g,BoundingBox clip){int r=plan.radius,h=new int[]{6,8,11}[plan.tier];
        for(int x=Math.max(-r,clip.minX()-origin.getX());x<=Math.min(r,clip.maxX()-origin.getX());x++)for(int z=Math.max(-r,clip.minZ()-origin.getZ());z<=Math.min(r,clip.maxZ()-origin.getZ());z++){
            double angle=Math.atan2(z,x),edge=r*(.94+.045*Math.sin(angle*3+plan.seed%17));double q=(x*x+z*z)/(edge*edge);if(q>1)continue;
            int ground=g.getBaseHeight(origin.getX()+x,origin.getZ()+z,Heightmap.Types.OCEAN_FLOOR_WG,w,w.getLevel().getChunkSource().randomState())-1-origin.getY();
            int top=(int)Math.round(h*Math.pow(1-q,1.7)+ground*q*q);int patch=Math.floorMod(Math.floorDiv(x,3)*19+Math.floorDiv(z,3)*31+(int)plan.seed,100);
            Block surface=patch<70?Blocks.GRASS_BLOCK:patch<83?Blocks.COARSE_DIRT:patch<92?Blocks.COBBLESTONE:patch<97?Blocks.MOSSY_COBBLESTONE:Blocks.GRAVEL;
            for(int y=ground;y<=top;y++)put(w,clip,x,y,z,(y==top?surface:Blocks.DIRT).defaultBlockState());
        }
        // An exposed timber-and-stone mouth, no custom exterior terrain or trees.
        int front=r-2;for(int x:new int[]{-3,3})for(int y=0;y<=4;y++)put(w,clip,x,y,front,(y<2?Blocks.MOSSY_COBBLESTONE:Blocks.STRIPPED_OAK_LOG).defaultBlockState());
        for(int x=-3;x<=3;x++)put(w,clip,x,5,front,Blocks.STRIPPED_OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS,Direction.Axis.X));
    }
    private void room(WorldGenLevel w,BoundingBox clip,KurganPlan.Room r){KurganPlan.Box b=r.box();
        for(int x=Math.max(b.x0,clip.minX()-origin.getX());x<=Math.min(b.x1,clip.maxX()-origin.getX());x++)for(int z=Math.max(b.z0,clip.minZ()-origin.getZ());z<=Math.min(b.z1,clip.maxZ()-origin.getZ());z++){
            int dx=Math.abs(x-r.x),dz=Math.abs(z-r.z);if(r.hall()&&dx+dz>23)continue;
            for(int y=r.y;y<=r.y+r.height+1;y++){
                boolean air=plan.roomAir(r,x,y,z);
                put(w,clip,x,y,z,air?Blocks.CAVE_AIR.defaultBlockState():r.hall()?KurganBlocks.get("sealed_kurgan_masonry").defaultBlockState():masonry(r.palette,x,y,z));
            }
        }
    }
    private void corridor(WorldGenLevel w,BoundingBox clip,KurganPlan.Link l){boolean alongX=l.steps.get(0).x!=l.steps.get(l.steps.size()-1).x;
        for(int i=0;i<l.steps.size();i++){KurganPlan.Step s=l.steps.get(i);KurganPlan.Box b=l.slice(i);if(!inClip(clip,b))continue;
            for(int x=b.x0;x<=b.x1;x++)for(int z=b.z0;z<=b.z1;z++)for(int y=b.y0;y<=b.y1;y++){
                if(plan.tier==2&&plan.rooms.get(plan.finalRoom).box().contains(x,y,z))continue;
                boolean side=alongX?z==b.z0||z==b.z1:x==b.x0||x==b.x1;
                BlockState state=side||y==b.y0||y==b.y1?masonry(l.palette,x,y,z):Blocks.CAVE_AIR.defaultBlockState();
                // Supports remain outside the clear width; cap rhythm does not lower headroom.
                if(side&&i%6==0)state=KurganBlocks.stone(l.palette,1);
                if(!side&&y==s.y&&l.stair&&i>0&&l.steps.get(i-1).y>s.y){KurganPlan.Step previous=l.steps.get(i-1);Direction up=previous.x>s.x?Direction.EAST:previous.x<s.x?Direction.WEST:previous.z>s.z?Direction.SOUTH:Direction.NORTH;state=Blocks.STONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING,up);}
                if((y>=0||l.a<0)&&!state.isAir()&&!state.is(Blocks.STONE_BRICK_STAIRS))state=(i%5==0?Blocks.MOSSY_COBBLESTONE:Blocks.COBBLESTONE).defaultBlockState();
                put(w,clip,x,y,z,state);
            }
            // A wall-recessed fixture: never occupies the 3/4/5-wide navigation envelope.
            if(!l.stair&&i%12==6){int x=alongX?s.x:b.x0,z=alongX?b.z0:s.z;put(w,clip,x,s.y+3,z,KurganBlocks.get("kurgan_wall_torch_holder").defaultBlockState().setValue(HorizontalDirectionalBlock.FACING,alongX?Direction.SOUTH:Direction.EAST));}
        }
    }
    private void prop(WorldGenLevel w,BoundingBox clip,KurganPlan.Room r,int dx,int dy,int dz,String name){put(w,clip,r.x+dx,r.y+dy,r.z+dz,KurganBlocks.get(name).defaultBlockState().setValue(HorizontalDirectionalBlock.FACING,name.contains("torch")?Direction.SOUTH:Direction.NORTH));}
    private void decorate(WorldGenLevel w,BoundingBox clip,KurganPlan.Room r){
        if(r.hall()){
            for(int dx=-2;dx<=2;dx++)for(int dz=-3;dz<=3;dz++){put(w,clip,r.x+dx,r.y+1,r.z+dz,KurganBlocks.stone(0,0));if(Math.abs(dx)==2||Math.abs(dz)==3)prop(w,clip,r,dx,2,dz,"burial_stone_slab");}
            for(int dx=-1;dx<=1;dx++)for(int dz=-2;dz<=2;dz++){put(w,clip,r.x+dx,r.y+2,r.z+dz,KurganBlocks.get("sealed_kurgan_masonry").defaultBlockState());prop(w,clip,r,dx,3,dz,"burial_stone_slab");}
            for(int[] a:new int[][]{{-8,-6},{8,-6},{-8,6},{8,6},{-6,-8},{6,-8},{-6,8},{6,8}}){for(int y=1;y<=8;y++)put(w,clip,r.x+a[0],r.y+y,r.z+a[1],KurganBlocks.stone(0,y%4==0?1:0));prop(w,clip,r,a[0],9,a[1],"kurgan_hanging_brazier");}
            for(KurganPlan.Box n:plan.niches){int x=(n.x0+n.x1)/2,z=(n.z0+n.z1)/2;put(w,clip,x,r.y+1,z,KurganBlocks.get("kurgan_stone_altar").defaultBlockState());}
            return;
        }
        // Furniture occupies side quadrants, leaving both connector axes at least three blocks clear.
        int side=r.rx-2,back=r.rz-2;
        prop(w,clip,r,-side,1,back,"kurgan_column_fragment");
        if(r.role==KurganPlan.Role.RUINED||r.role==KurganPlan.Role.ATMOSPHERIC){prop(w,clip,r,side,1,back,"kurgan_rubble");prop(w,clip,r,-side,1,-back,"kurgan_pottery");}
        else if(r.role==KurganPlan.Role.OFFERING){prop(w,clip,r,side,1,back,"kurgan_stone_altar");prop(w,clip,r,side,2,back,"kurgan_ritual_bowl");prop(w,clip,r,side,1,-back,"kurgan_pottery");}
        else if(!r.loot.isEmpty()){
            int length=r.rx>=6?3:2;for(int dz=0;dz<length;dz++){put(w,clip,r.x+side,r.y+1,r.z+2+dz,KurganBlocks.stone(r.palette,0));prop(w,clip,r,side,2,2+dz,"burial_stone_slab");if(r.rx>=6){put(w,clip,r.x+side-1,r.y+1,r.z+2+dz,KurganBlocks.stone(r.palette,1));prop(w,clip,r,side-1,2,2+dz,"burial_stone_slab");}}
            prop(w,clip,r,side,1,-back,"kurgan_ritual_bowl");
        }else prop(w,clip,r,side,1,back,"kurgan_rubble");
        prop(w,clip,r,side,3,-r.rz,"kurgan_wall_torch_holder");
        if(r.rx>=6)prop(w,clip,r,-side,r.height,-back,"kurgan_hanging_brazier");
        if(!r.loot.isEmpty())coffin(w,clip,r);
    }
    private void coffin(WorldGenLevel w,BoundingBox clip,KurganPlan.Room r){BlockPos foot=origin.offset(r.x-r.rx+2,r.y+1,r.z-r.rz+2);Direction direction=Direction.WEST;BlockPos head=foot.west();if((foot.getX()>>4)!=(head.getX()>>4)){foot=origin.offset(r.x+r.rx-2,r.y+1,r.z-r.rz+2);head=foot.east();direction=Direction.EAST;}BlockState s=ModBlocks.BURIAL_COFFIN.get().defaultBlockState().setValue(BurialCoffinBlock.FACING,direction);
        if(clip.isInside(head))w.setBlock(head,s.setValue(BurialCoffinBlock.PART,BedPart.HEAD),2);
        if(clip.isInside(foot)&&!filled.contains(r.id)){w.setBlock(foot,s,2);if(w.getBlockEntity(foot) instanceof BurialCoffinTile){BurialCoffinTile tile=(BurialCoffinTile)w.getBlockEntity(foot);tile.natural(id,Math.floorMod(r.id,4));String tier=new String[]{"small","warrior","great"}[plan.tier];tile.setLootTable(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE,ResourceLocation.fromNamespaceAndPath("slavicmyths","chests/kurgan_"+tier+"_"+r.loot)),plan.seed+r.id);filled.add(r.id);}}
    }
    /** Future boss patch hook; deliberately no command or survival opening mechanism. */
    public static boolean openSeal(ServerLevel world,UUID id){BurialRecords data=BurialRecords.get(world);KurganInstance i=data.instances.get(id);if(i==null||i.plan.seal==null)return false;KurganPlan.Box b=i.plan.seal;
        for(int x=b.x0;x<=b.x1;x++)for(int z=b.z0;z<=b.z1;z++)if(!world.hasChunkAt(i.origin.offset(x,b.y0,z)))return false;
        for(int x=b.x0;x<=b.x1;x++)for(int z=b.z0;z<=b.z1;z++)for(int y=b.y0;y<=b.y1;y++)world.setBlock(i.origin.offset(x,y,z),Blocks.CAVE_AIR.defaultBlockState(),3);
        i.sealOpened=true;data.setDirty();return true;
    }
}
