package org.slavicmyths.swamp;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.*;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.block.*;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.*;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.structure.*;
import net.minecraft.world.gen.settings.StructureSeparationSettings;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.*;

@Mod.EventBusSubscriber(modid="slavicmyths")
public final class SwampCommands {
    @SubscribeEvent public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("slavicmyths").requires(s->s.hasPermission(2))
            .then(Commands.literal("dev").then(Commands.literal("swamp").executes(c->swamp(c.getSource())))
            .then(Commands.literal("structure").then(Commands.argument("id",StringArgumentType.word())
                .suggests((c,b)->ISuggestionProvider.suggest(Arrays.copyOf(SwampStructures.IDS,6),b))
                .executes(c->structure(c.getSource(),StringArgumentType.getString(c,"id"),false))
                .then(Commands.literal("next").executes(c->structure(c.getSource(),StringArgumentType.getString(c,"id"),true)))))));
    }
    private static boolean overworld(CommandSource source) {
        if(source.getLevel().dimension().equals(World.OVERWORLD))return true;
        source.sendFailure(new TranslationTextComponent("swamp.command.overworld"));return false;
    }
    private static int swamp(CommandSource source)throws CommandSyntaxException {
        if(!overworld(source))return 0;
        ServerPlayerEntity player=source.getPlayerOrException(); ServerWorld world=source.getLevel();
        BlockPos origin=player.blockPosition();
        boolean another=world.getBiome(origin).getBiomeCategory()==Biome.Category.SWAMP;
        // Noise-biome queries only: no chunk generation during the search. 256-block cells,
        // refined to 32 blocks, bounded at 8192 blocks. An already-swamp player skips 1280 blocks.
        BlockPos found=null;
        search:for(int radius=another?5:0;radius<=32;radius++) {
            for(int dx=-radius;dx<=radius;dx++)for(int dz=-radius;dz<=radius;dz++) {
                if(Math.max(Math.abs(dx),Math.abs(dz))!=radius)continue;
                int x=origin.getX()+dx*256,z=origin.getZ()+dz*256;
                BlockPos p=world.getChunkSource().getGenerator().getBiomeSource().findBiomeHorizontal(x,64,z,128,32,
                    b->b.getBiomeCategory()==Biome.Category.SWAMP,new Random(world.getSeed()^BlockPos.asLong(x,64,z)),true);
                if(p!=null && (!another || horizontal(origin,p)>=1280L*1280 && crossesOtherBiome(world,origin,p))) {found=p;break search;}
            }
        }
        if(found==null){source.sendFailure(new TranslationTextComponent("swamp.command.not_found"));return 0;}
        BlockPos safe=safeSurface(world,found);
        if(safe==null){source.sendFailure(new TranslationTextComponent("swamp.command.no_safe",found.getX(),found.getZ()));return 0;}
        player.teleportTo(world,safe.getX()+.5,safe.getY(),safe.getZ()+.5,player.yRot,player.xRot);
        source.sendSuccess(new TranslationTextComponent("swamp.command.teleported",safe.getX(),safe.getY(),safe.getZ()),false);return 1;
    }
    private static long horizontal(BlockPos a,BlockPos b){long x=(long)a.getX()-b.getX(),z=(long)a.getZ()-b.getZ();return x*x+z*z;}
    private static boolean crossesOtherBiome(ServerWorld world,BlockPos origin,BlockPos target) {
        int steps=(int)(Math.sqrt(horizontal(origin,target))/128);
        for(int i=1;i<steps;i++) {
            int x=origin.getX()+(target.getX()-origin.getX())*i/steps,z=origin.getZ()+(target.getZ()-origin.getZ())*i/steps;
            if(world.getChunkSource().getGenerator().getBiomeSource().getNoiseBiome(x>>2,16,z>>2).getBiomeCategory()!=Biome.Category.SWAMP)return true;
        }
        return false;
    }
    private static BlockPos safeSurface(ServerWorld world,BlockPos center) {
        // Only the selected destination's 5x5 chunk neighbourhood may load, on explicit command.
        for(int r=0;r<=32;r+=2)for(int dx=-r;dx<=r;dx+=2)for(int dz=-r;dz<=r;dz+=2) {
            if(Math.max(Math.abs(dx),Math.abs(dz))!=r)continue;
            BlockPos p=world.getHeightmapPos(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,center.offset(dx,0,dz));
            BlockState ground=world.getBlockState(p.below());
            if(world.getWorldBorder().isWithinBounds(p)&&world.getBiome(p).getBiomeCategory()==Biome.Category.SWAMP &&
                ground.isFaceSturdy(world,p.below(),net.minecraft.util.Direction.UP)&&!ground.is(BlockTags.LOGS)&&!ground.is(BlockTags.LEAVES)&&
                !ground.is(Blocks.MAGMA_BLOCK)&&!ground.is(Blocks.CAMPFIRE)&&
                world.isEmptyBlock(p)&&world.isEmptyBlock(p.above())&&world.getFluidState(p.below()).isEmpty())return p;
        }
        return null;
    }
    private static int structure(CommandSource source,String id,boolean next)throws CommandSyntaxException {
        if(!overworld(source))return 0;
        if(!SwampStructures.TYPES.containsKey(id)||id.equals("swamp_remnants")){source.sendFailure(new TranslationTextComponent("swamp.command.bad_id"));return 0;}
        ServerWorld world=source.getLevel();BlockPos origin=new BlockPos(source.getPosition());
        SwampStructure type=SwampStructures.TYPES.get(id).get();
        StructureSeparationSettings settings=world.getChunkSource().getGenerator().getSettings().getConfig(type);
        if(settings==null){source.sendFailure(new TranslationTextComponent("swamp.command.not_found"));return 0;}
        // Inspect real starts at candidate chunks (vanilla locate uses the same STRUCTURE_STARTS
        // generation stage). Never places a building at the player or guesses from a grid point.
        net.minecraft.util.SharedSeedRandom random=new net.minecraft.util.SharedSeedRandom();
        Set<Long> seen=new HashSet<>();BlockPos first=null,best=null;long distance=Long.MAX_VALUE;
        for(int r=0;r<=12;r++) {
            for(int dx=-r;dx<=r;dx++)for(int dz=-r;dz<=r;dz++) {
                if(Math.max(Math.abs(dx),Math.abs(dz))!=r)continue;
                ChunkPos p=type.getPotentialFeatureChunk(settings,world.getSeed(),random,(origin.getX()>>4)+dx*settings.spacing(),(origin.getZ()>>4)+dz*settings.spacing());
                if(!seen.add(p.toLong()))continue;
                Biome biome=world.getChunkSource().getGenerator().getBiomeSource().getNoiseBiome((p.x<<2)+2,16,(p.z<<2)+2);
                if(!biome.getGenerationSettings().isValidStart(type))continue;
                net.minecraft.world.chunk.IChunk chunk=world.getChunk(p.x,p.z,ChunkStatus.STRUCTURE_STARTS);
                StructureStart<?> start=chunk.getStartForFeature(type);
                if(start==null||!start.isValid())continue;
                BlockPos pos=start.getLocatePos();long d=horizontal(origin,pos);
                if(d<distance){best=first;first=pos;distance=d;}else if(best==null||d<horizontal(origin,best))best=pos;
            }
            if(r>=2 && first!=null && (!next||best!=null))break;
        }
        BlockPos result=next?best:first;
        if(result==null){source.sendFailure(new TranslationTextComponent("swamp.command.not_found"));return 0;}
        source.sendSuccess(new TranslationTextComponent("swamp.command.structure",id,result.getX(),result.getY(),result.getZ()),false);return 1;
    }
    private SwampCommands(){}
}
