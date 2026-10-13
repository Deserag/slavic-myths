package org.slavicmyths.mor;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.util.ColorRGBA;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.*;

/** Exact, non-spreading conversion table. No world scans, inventory writes or chunk loads. */
public final class MorBlocks {
    public enum Result { CONVERTED, NOT_SUPPORTED, ALREADY_INFECTED, PROTECTED, NOT_LOADED, BLOCKED_BY_EVENT }
    public static final String[] IDS={"dirt","grass_block","stone","cobblestone","sand","gravel","oak_log","oak_leaves","oak_planks","birch_log","sandstone","obsidian","stone_bricks","moss_block"};
    public static final Block[] BASE={Blocks.DIRT,Blocks.GRASS_BLOCK,Blocks.STONE,Blocks.COBBLESTONE,Blocks.SAND,Blocks.GRAVEL,Blocks.OAK_LOG,Blocks.OAK_LEAVES,Blocks.OAK_PLANKS,Blocks.BIRCH_LOG,Blocks.SANDSTONE,Blocks.OBSIDIAN,Blocks.STONE_BRICKS,Blocks.MOSS_BLOCK};
    public static final Map<String,DeferredHolder<Block,Block>> BLOCKS=new LinkedHashMap<>();
    static {
        for(int i=0;i<IDS.length;i++) {
            String id=IDS[i]; Block base=BASE[i];
            var holder=ModBlocks.BLOCKS.register("mor_"+id,()->create(id,base));
            BLOCKS.put(id,holder);
            ModItems.ITEMS.register("mor_"+id,()->new BlockItem(holder.get(),new Item.Properties()));
        }
    }
    static Block create(String id,Block base) {
        var p=BlockBehaviour.Properties.ofFullCopy(base).lootFrom(()->base);
        if(id.endsWith("_log")) return new RotatedPillarBlock(p) {
            @Override public int getFlammability(BlockState s,net.minecraft.world.level.BlockGetter w,BlockPos pos,Direction d){return 5;}
            @Override public int getFireSpreadSpeed(BlockState s,net.minecraft.world.level.BlockGetter w,BlockPos pos,Direction d){return 5;}
            @Override public BlockState getToolModifiedState(BlockState s,net.minecraft.world.item.context.UseOnContext c,net.neoforged.neoforge.common.ItemAbility ability,boolean simulate){if(ability==net.neoforged.neoforge.common.ItemAbilities.AXE_STRIP&&c.getItemInHand().canPerformAction(ability))return (base==Blocks.BIRCH_LOG?Blocks.STRIPPED_BIRCH_LOG:Blocks.STRIPPED_OAK_LOG).defaultBlockState().setValue(AXIS,s.getValue(AXIS));return super.getToolModifiedState(s,c,ability,simulate);}
        };
        if(id.equals("oak_leaves")) return new LeavesBlock(p) {
            @Override public int getFlammability(BlockState s,net.minecraft.world.level.BlockGetter w,BlockPos pos,Direction d){return 60;}
            @Override public int getFireSpreadSpeed(BlockState s,net.minecraft.world.level.BlockGetter w,BlockPos pos,Direction d){return 30;}
        };
        // Vanilla grass/bonemeal behavior; when covered, the infected material becomes infected dirt.
        if(id.equals("grass_block")) return new GrassBlock(p) {
            @Override protected void randomTick(BlockState s,ServerLevel w,BlockPos pos,net.minecraft.util.RandomSource r) {org.slavicmyths.quest185.Quest185Origins.grassTick(w,pos,()->{super.randomTick(s,w,pos,r);if(w.getBlockState(pos).is(Blocks.DIRT))w.setBlockAndUpdate(pos,BLOCKS.get("dirt").get().defaultBlockState());});}
            @Override public BlockState getToolModifiedState(BlockState s,net.minecraft.world.item.context.UseOnContext c,net.neoforged.neoforge.common.ItemAbility ability,boolean simulate){if(ability==net.neoforged.neoforge.common.ItemAbilities.SHOVEL_FLATTEN&&c.getItemInHand().canPerformAction(ability))return Blocks.DIRT_PATH.defaultBlockState();if(ability==net.neoforged.neoforge.common.ItemAbilities.HOE_TILL&&c.getItemInHand().canPerformAction(ability))return Blocks.FARMLAND.defaultBlockState();return super.getToolModifiedState(s,c,ability,simulate);}
        };
        if(id.equals("sand")||id.equals("gravel")) return new ColoredFallingBlock(new ColorRGBA(id.equals("sand")?0xDBD3A0:0x837A78),p);
        if(id.equals("oak_planks")) return new Block(p) {
            @Override public int getFlammability(BlockState s,net.minecraft.world.level.BlockGetter w,BlockPos pos,Direction d){return 20;}
            @Override public int getFireSpreadSpeed(BlockState s,net.minecraft.world.level.BlockGetter w,BlockPos pos,Direction d){return 5;}
        };
        return new Block(p) {
            @Override public BlockState getToolModifiedState(BlockState state,net.minecraft.world.item.context.UseOnContext context,net.neoforged.neoforge.common.ItemAbility ability,boolean simulate){return base.getToolModifiedState(copy(state,base.defaultBlockState()),context,ability,simulate);}
        };
    }
    public static int index(BlockState state) { for(int i=0;i<IDS.length;i++)if(state.is(BLOCKS.get(IDS[i]).get()))return i;return -1; }
    public static BlockState prototype(BlockState state) { int i=index(state);return i<0?state:copy(state,BASE[i].defaultBlockState()); }
    public static BlockState copy(BlockState source,BlockState target) {
        for(Property<?> property:source.getProperties())if(target.hasProperty(property))target=copyProperty(source,target,property);
        return target;
    }
    private static <T extends Comparable<T>> BlockState copyProperty(BlockState s,BlockState t,Property<T> p){return t.setValue(p,s.getValue(p));}
    public static Result convert(ServerLevel level,BlockPos pos,net.minecraft.world.entity.Entity cause) {
        if(!level.hasChunkAt(pos))return Result.NOT_LOADED;
        if(!level.getWorldBorder().isWithinBounds(pos)||level.getBlockEntity(pos)!=null)return Result.PROTECTED;
        BlockState state=level.getBlockState(pos);
        if(org.slavicmyths.quest185.CleansedLand.protectedAt(level,pos))return Result.PROTECTED;
        if(state.is(Blocks.OBSIDIAN))for(Direction direction:Direction.values()){BlockPos adjacent=pos.relative(direction);if(!level.hasChunkAt(adjacent))return Result.PROTECTED;if(level.getBlockState(adjacent).is(Blocks.NETHER_PORTAL)||level.getBlockState(adjacent).is(Blocks.END_PORTAL)||level.getBlockState(adjacent).is(Blocks.END_GATEWAY))return Result.PROTECTED;}
        if(index(state)>=0)return Result.ALREADY_INFECTED;
        for(int i=0;i<BASE.length;i++)if(state.is(BASE[i])) {
            if(cause!=null&&!net.neoforged.neoforge.event.EventHooks.canEntityGrief(level,cause))return Result.BLOCKED_BY_EVENT;
            if(cause instanceof net.minecraft.world.entity.LivingEntity living&&net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.entity.living.LivingDestroyBlockEvent(living,pos,state)).isCanceled())return Result.BLOCKED_BY_EVENT;
            if(cause instanceof net.minecraft.server.level.ServerPlayer player && net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.level.BlockEvent.BreakEvent(level,pos,state,player)).isCanceled())return Result.BLOCKED_BY_EVENT;
            var infected=copy(state,BLOCKS.get(IDS[i]).get().defaultBlockState());if(!level.setBlock(pos,infected,3))return Result.BLOCKED_BY_EVENT;org.slavicmyths.quest185.Quest185Origins.remember(level,pos,state,infected);return Result.CONVERTED;
        }
        return Result.NOT_SUPPORTED;
    }
    public static void init() {}
    private MorBlocks() {}
}
