package org.slavicmyths.buyan;

import java.util.*;
import net.minecraft.commands.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

/** Operator-only trial buildings. All interiors are placed before roofs are assembled. */
public final class BuyanPort {
    private static final class Plan {
        final Map<BlockPos, BlockState> cells = new LinkedHashMap<>();
        void put(int x,int y,int z,BlockState b){cells.put(new BlockPos(x,y,z),b);}
        void fill(int x1,int y1,int z1,int x2,int y2,int z2,BlockState b){
            for(int x=x1;x<=x2;x++)for(int y=y1;y<=y2;y++)for(int z=z1;z<=z2;z++)put(x,y,z,b);
        }
    }
    private static final BlockState AIR=Blocks.AIR.defaultBlockState();
    private static final BlockState LOG=Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState();
    private static final BlockState WALL=Blocks.OAK_PLANKS.defaultBlockState();
    private static final BlockState FLOOR=Blocks.SPRUCE_PLANKS.defaultBlockState();
    private static final BlockState ROOF=Blocks.DARK_OAK_PLANKS.defaultBlockState();

    public static void register(org.slavicmyths.commands.CommandTree tree){
        var root=Commands.literal("slavicmyths").then(
            Commands.literal("dev").requires(s->s.hasPermission(2)).then(
                Commands.literal("generate").then(
                    Commands.literal("buyan")
                        .then(Commands.literal("house").executes(c->place(c.getSource(),"house")))
                        .then(Commands.literal("market").executes(c->place(c.getSource(),"market")))
                        .then(Commands.literal("warehouse").executes(c->place(c.getSource(),"warehouse")))
                        .then(Commands.literal("main_pier").executes(c->place(c.getSource(),"main_pier")))
                        .then(Commands.literal("small_pier").executes(c->place(c.getSource(),"small_pier")))
                )
            )
        );
        tree.add(root);
    }

    private static void houseShell(Plan p,int width,int depth,int wallHeight){
        p.fill(0,0,0,width-1,0,depth-1,FLOOR);
        for(int y=1;y<=wallHeight;y++){
            for(int x=0;x<width;x++)for(int z=0;z<depth;z++){
                if(x==0||x==width-1||z==0||z==depth-1)p.put(x,y,z,WALL);
                else p.put(x,y,z,AIR);
            }
        }
        for(int x:new int[]{0,width-1})for(int z:new int[]{0,depth-1})
            p.fill(x,1,z,x,wallHeight,z,LOG);
        // Two-block-high entrance: front of every building, always openable.
        int mid=width/2;
        p.fill(mid-1,1,0,mid,3,0,AIR);
        for(int x:new int[]{2,width-4}){
            p.fill(x,3,0,x+1,4,0,Blocks.GLASS_PANE.defaultBlockState());
            p.fill(x,3,depth-1,x+1,4,depth-1,Blocks.GLASS_PANE.defaultBlockState());
        }
        // Layered gable roof, overhang by one block on all four sides.
        for(int i=0;i<=width/2;i++){
            for(int z=-1;z<=depth;z++){
                p.put(-1+i,wallHeight+1+i,z,ROOF);
                p.put(width-i,wallHeight+1+i,z,ROOF);
            }
        }
        for(int i=0;i<=width/2;i++)for(int x=i;x<width-i;x++){
            p.put(x,wallHeight+1+i,0,WALL);
            p.put(x,wallHeight+1+i,depth-1,WALL);
        }
        p.fill(0,-1,0,width-1,-1,depth-1,Blocks.COBBLESTONE.defaultBlockState());
        p.fill(mid-1,0,-1,mid,0,-1,Blocks.SPRUCE_SLAB.defaultBlockState());
    }

    private static void furnishings(Plan p,String type,int width,int depth){
        // Real traversable interior: central route from entrance to back wall.
        p.put(2,1,2,Blocks.FURNACE.defaultBlockState());
        p.put(2,1,3,Blocks.CRAFTING_TABLE.defaultBlockState());
        p.put(2,1,depth-3,Blocks.BARREL.defaultBlockState());
        p.put(3,1,depth-3,Blocks.CHEST.defaultBlockState());
        if(type.equals("house")){
            p.put(width-3,1,depth-3,Blocks.WHITE_BED.defaultBlockState().setValue(net.minecraft.world.level.block.BedBlock.PART,net.minecraft.world.level.block.state.properties.BedPart.FOOT));
            p.put(width-3,1,depth-4,Blocks.WHITE_BED.defaultBlockState().setValue(net.minecraft.world.level.block.BedBlock.PART,net.minecraft.world.level.block.state.properties.BedPart.HEAD));
            p.put(width-3,1,depth-4,Blocks.SPRUCE_STAIRS.defaultBlockState());
            p.put(width-5,1,depth-5,Blocks.SPRUCE_FENCE.defaultBlockState());
            p.put(width-5,2,depth-5,Blocks.SPRUCE_PRESSURE_PLATE.defaultBlockState());
            p.put(3,1,depth-5,Blocks.BOOKSHELF.defaultBlockState());
            p.put(3,2,depth-5,Blocks.BOOKSHELF.defaultBlockState());
        } else if(type.equals("market")){
            p.fill(1,1,2,width-2,1,2,Blocks.SPRUCE_SLAB.defaultBlockState());
            p.put(2,1,depth-4,Blocks.SMOKER.defaultBlockState());
            p.put(width-3,1,depth-3,Blocks.CHEST.defaultBlockState());
            p.put(width-4,1,depth-3,Blocks.BARREL.defaultBlockState());
        } else {
            for(int z=4;z<depth-2;z+=2){
                p.put(2,1,z,Blocks.BARREL.defaultBlockState());
                p.put(width-3,1,z,Blocks.CHEST.defaultBlockState());
            }
            p.put(width-3,1,2,Blocks.CRAFTING_TABLE.defaultBlockState());
        }
        // Wall torches away from door, keeping the center passage clear.
        p.put(width-2,1,depth-2,Blocks.LANTERN.defaultBlockState());
    }

    private static Plan building(String type){
        Plan p=new Plan();
        int w=type.equals("warehouse")?16:type.equals("market")?10:12;
        int d=type.equals("warehouse")?11:type.equals("market")?8:10;
        houseShell(p,w,d,type.equals("warehouse")?7:6);
        furnishings(p,type,w,d);
        if(type.equals("market")){
            p.fill(2,1,-2,w-3,1,-2,Blocks.SPRUCE_SLAB.defaultBlockState());
            p.fill(2,0,-2,w-3,0,-1,FLOOR);
        }
        if(type.equals("warehouse")){
            // Unobstructed central double-wide entrance and wooden loading threshold.
            p.fill(w/2-2,1,0,w/2+1,4,0,AIR);
            p.fill(w/2-2,0,-2,w/2+1,0,-1,FLOOR);
        }
        return p;
    }

    private static Plan pier(boolean main){
        Plan p=new Plan();
        int w=main?5:3,len=main?14:10;
        for(int z=0;z<len;z++)for(int x=0;x<w;x++)
            p.put(x,0,-z,FLOOR);
        for(int z=1;z<len;z+=5)for(int x:new int[]{0,w-1}){
            p.fill(x,-5,-z,x,-1,-z,LOG);
            p.put(x,0,-z,Blocks.SPRUCE_PLANKS.defaultBlockState());
        }
        if(main)for(int x=-2;x<w+2;x++)for(int z=-len+1;z<=-len+3;z++)
            p.put(x,0,z,FLOOR);
        // The pier meets the shore at z=0; no oversized decorative elements.
        return p;
    }
    private static int place(CommandSourceStack source,String type){
        ServerLevel level=source.getLevel();
        BlockPos player=BlockPos.containing(source.getPosition());
        int px=player.getX()+5,pz=player.getZ()+5;
        level.getChunk(px>>4,pz>>4);
        int surface=level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,px,pz);
        BlockPos origin=new BlockPos(px,surface,pz);
        Plan plan=type.endsWith("pier")?pier(type.equals("main_pier")):building(type);
        // Preflight every block in every chunk before placing anything.
        for(var entry:plan.cells.entrySet()){
            BlockPos target=origin.offset(entry.getKey());
            if(target.getY()<level.getMinBuildHeight()+1||target.getY()>=level.getMaxBuildHeight()-1
                ||!level.getWorldBorder().isWithinBounds(target)){
                source.sendFailure(Component.literal("Buyan: outside world bounds "+target));return 0;
            }
            level.getChunk(target.getX()>>4,target.getZ()>>4);
            BlockState old=level.getBlockState(target);
            int localY=entry.getKey().getY();
            if(old.is(Blocks.BEDROCK)||old.hasBlockEntity()
                ||(localY>=0&&!old.isAir()&&!old.canBeReplaced()&&!old.is(Blocks.WATER))){
                source.sendFailure(Component.literal("Buyan: occupied site at "+target+" ("+old.getBlock()+")"));return 0;
            }
        }
        for(var entry:plan.cells.entrySet()){
            BlockPos target=origin.offset(entry.getKey());
            if(!level.setBlock(target,entry.getValue(),3)){
                source.sendFailure(Component.literal("Buyan: failed placement at "+target));return 0;
            }
        }
        source.sendSuccess(()->Component.literal("Buyan "+type+" placed at "+origin+"; "+plan.cells.size()+" block positions"),true);
        return 1;
    }
    private BuyanPort(){}
}
