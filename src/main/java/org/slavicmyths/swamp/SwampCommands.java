package org.slavicmyths.swamp;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.placement.*;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import org.slavicmyths.worldgen.StructureCandidates;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.tags.BlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.*;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.network.chat.*;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import java.util.*;

@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths")
public final class SwampCommands {
    @SubscribeEvent public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("slavicmyths")
            .then(Commands.literal("dev").requires(source->source.hasPermission(2)).then(Commands.literal("swamp").executes(c->swamp(c.getSource())))
            .then(Commands.literal("structure").then(Commands.argument("id",StringArgumentType.word())
                .suggests((c,b)->SharedSuggestionProvider.suggest(SwampStructures.IDS,b))
                .executes(c->structure(c.getSource(),StringArgumentType.getString(c,"id"),false))
                .then(Commands.literal("next").executes(c->structure(c.getSource(),StringArgumentType.getString(c,"id"),true)))))));
    }
    private static boolean overworld(CommandSourceStack source) {
        if(source.getLevel().dimension().equals(Level.OVERWORLD))return true;
        source.sendFailure(Component.translatable("swamp.command.overworld"));return false;
    }
    private static int swamp(CommandSourceStack source)throws CommandSyntaxException {
        if(!overworld(source))return 0;
        ServerPlayer player=source.getPlayerOrException(); ServerLevel world=source.getLevel();
        BlockPos origin=player.blockPosition();
        boolean another=world.getBiome(origin).is(net.neoforged.neoforge.common.Tags.Biomes.IS_SWAMP);
        // Noise-biome queries only: no chunk generation during the search. 256-block cells,
        // refined to 32 blocks, bounded at 8192 blocks. An already-swamp player skips 1280 blocks.
        BlockPos found=null;
        search:for(int radius=another?5:0;radius<=32;radius++) {
            for(int dx=-radius;dx<=radius;dx++)for(int dz=-radius;dz<=radius;dz++) {
                if(Math.max(Math.abs(dx),Math.abs(dz))!=radius)continue;
                int x=origin.getX()+dx*256,z=origin.getZ()+dz*256;
                com.mojang.datafixers.util.Pair<BlockPos,net.minecraft.core.Holder<Biome>> match=world.getChunkSource().getGenerator().getBiomeSource().findBiomeHorizontal(x,64,z,128,32,
                    b->b.is(net.neoforged.neoforge.common.Tags.Biomes.IS_SWAMP),net.minecraft.util.RandomSource.create(world.getSeed()^BlockPos.asLong(x,64,z)),true,world.getChunkSource().randomState().sampler());
                BlockPos p=match==null?null:match.getFirst();
                if(p!=null && (!another || horizontal(origin,p)>=1280L*1280 && crossesOtherBiome(world,origin,p))) {found=p;break search;}
            }
        }
        if(found==null){source.sendFailure(Component.translatable("swamp.command.not_found"));return 0;}
        BlockPos safe=safeSurface(world,found);
        if(safe==null){source.sendFailure(Component.translatable("swamp.command.no_safe",found.getX(),found.getZ()));return 0;}
        player.teleportTo(world,safe.getX()+.5,safe.getY(),safe.getZ()+.5,player.getYRot(),player.getXRot());
        source.sendSuccess(()->Component.translatable("swamp.command.teleported",safe.getX(),safe.getY(),safe.getZ()),false);return 1;
    }
    private static long horizontal(BlockPos a,BlockPos b){long x=(long)a.getX()-b.getX(),z=(long)a.getZ()-b.getZ();return x*x+z*z;}
    private static boolean crossesOtherBiome(ServerLevel world,BlockPos origin,BlockPos target) {
        int steps=(int)(Math.sqrt(horizontal(origin,target))/128);
        for(int i=1;i<steps;i++) {
            int x=origin.getX()+(target.getX()-origin.getX())*i/steps,z=origin.getZ()+(target.getZ()-origin.getZ())*i/steps;
            if(!world.getChunkSource().getGenerator().getBiomeSource().getNoiseBiome(x>>2,16,z>>2,world.getChunkSource().randomState().sampler()).is(net.neoforged.neoforge.common.Tags.Biomes.IS_SWAMP))return true;
        }
        return false;
    }
    private static BlockPos safeSurface(ServerLevel world,BlockPos center) {
        // Only the selected destination's 5x5 chunk neighbourhood may load, on explicit command.
        for(int r=0;r<=32;r+=2)for(int dx=-r;dx<=r;dx+=2)for(int dz=-r;dz<=r;dz+=2) {
            if(Math.max(Math.abs(dx),Math.abs(dz))!=r)continue;
            BlockPos p=world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,center.offset(dx,0,dz));
            BlockState ground=world.getBlockState(p.below());
            if(world.getWorldBorder().isWithinBounds(p)&&world.getBiome(p).is(net.neoforged.neoforge.common.Tags.Biomes.IS_SWAMP) &&
                ground.isFaceSturdy(world,p.below(),net.minecraft.core.Direction.UP)&&!ground.is(BlockTags.LOGS)&&!ground.is(BlockTags.LEAVES)&&
                !ground.is(Blocks.MAGMA_BLOCK)&&!ground.is(Blocks.CAMPFIRE)&&
                world.isEmptyBlock(p)&&world.isEmptyBlock(p.above())&&world.getFluidState(p.below()).isEmpty())return p;
        }
        return null;
    }
    private static int structure(CommandSourceStack source,String id,boolean next)throws CommandSyntaxException {
        if(!overworld(source))return 0;
        if(!SwampStructures.TYPES.containsKey(id)||id.equals("swamp_remnants")){source.sendFailure(Component.translatable("swamp.command.bad_id"));return 0;}
        ServerLevel world=source.getLevel();BlockPos origin=BlockPos.containing(source.getPosition());
        Structure type=SwampStructures.get(world,id);
        RandomSpreadStructurePlacement settings=StructureCandidates.placement(world,type);
        if(settings==null){source.sendFailure(Component.translatable("swamp.command.not_found"));return 0;}
        // Inspect real starts at candidate chunks (vanilla locate uses the same STRUCTURE_STARTS
        // generation stage). Never places a building at the player or guesses from a grid point.
        Set<Long> seen=new HashSet<>();BlockPos first=null,best=null;long distance=Long.MAX_VALUE;
        for(int r=0;r<=12;r++) {
            for(int dx=-r;dx<=r;dx++)for(int dz=-r;dz<=r;dz++) {
                if(Math.max(Math.abs(dx),Math.abs(dz))!=r)continue;
                ChunkPos p=settings.getPotentialStructureChunk(world.getSeed(),(origin.getX()>>4)+dx*settings.spacing(),(origin.getZ()>>4)+dz*settings.spacing());
                if(!seen.add(p.toLong()))continue;
                if(!StructureCandidates.biome(world,type,p))continue;
                net.minecraft.world.level.chunk.ChunkAccess chunk=world.getChunk(p.x,p.z,ChunkStatus.STRUCTURE_STARTS);
                StructureStart start=chunk.getStartForStructure(type);
                if(start==null||!start.isValid())continue;
                BlockPos pos=StructureCandidates.locate(start);long d=horizontal(origin,pos);
                if(d<distance){best=first;first=pos;distance=d;}else if(best==null||d<horizontal(origin,best))best=pos;
            }
            if(r>=2 && first!=null && (!next||best!=null))break;
        }
        BlockPos result=next?best:first;
        if(result==null){source.sendFailure(Component.translatable("swamp.command.not_found"));return 0;}
        source.sendSuccess(()->Component.translatable("swamp.command.structure",id,result.getX(),result.getY(),result.getZ()),false);return 1;
    }
    private SwampCommands(){}
}
