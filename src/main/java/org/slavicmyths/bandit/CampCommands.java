package org.slavicmyths.bandit;
import java.util.*;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.*;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.gen.*;
import net.minecraft.world.gen.feature.structure.*;
import net.minecraft.world.gen.settings.StructureSeparationSettings;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
@Mod.EventBusSubscriber(modid="slavicmyths")
public final class CampCommands {
    @SubscribeEvent public static void register(RegisterCommandsEvent e){
        com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSource> locate=Commands.literal("bandit_camp"),tp=Commands.literal("tp_bandit_camp");
        for(String size:new String[]{"small","medium","large","any"}){
            locate.then(Commands.literal(size).executes(c->run(c.getSource(),size,false,false)).then(Commands.literal("next").executes(c->run(c.getSource(),size,true,false))));
            tp.then(Commands.literal(size).executes(c->run(c.getSource(),size,false,true)).then(Commands.literal("next").executes(c->run(c.getSource(),size,true,true))));
        }
        e.getDispatcher().register(Commands.literal("slavicmyths").requires(s->s.hasPermission(2)).then(Commands.literal("locate").then(locate)).then(Commands.literal("dev").then(tp)));
    }
    private static int run(CommandSource source,String size,boolean next,boolean teleport)throws CommandSyntaxException {
        ServerWorld w=source.getLevel();if(!w.dimension().equals(World.OVERWORLD)){source.sendFailure(new TranslationTextComponent("swamp.command.overworld"));return 0;}
        BlockPos origin=new BlockPos(source.getPosition());List<BlockPos> found=new ArrayList<>();Set<Long> visited=new HashSet<>();
        for(int r=0;r<=7;r++){
            for(Structure<?> type:new Structure<?>[]{CampStructures.SMALL.get(),CampStructures.MEDIUM.get(),CampStructures.LARGE.get()}){
                if(!size.equals("any")&&type!=(size.equals("small")?CampStructures.SMALL.get():size.equals("medium")?CampStructures.MEDIUM.get():CampStructures.LARGE.get()))continue;
                StructureSeparationSettings cfg=w.getChunkSource().getGenerator().getSettings().getConfig(type);if(cfg==null)continue;
                for(int dx=-r;dx<=r;dx++)for(int dz=-r;dz<=r;dz++){
                    if(Math.max(Math.abs(dx),Math.abs(dz))!=r)continue;ChunkPos p=type.getPotentialFeatureChunk(cfg,w.getSeed(),new SharedSeedRandom(),(origin.getX()>>4)+dx*cfg.spacing(),(origin.getZ()>>4)+dz*cfg.spacing());
                    long key=p.toLong()^((long)type.getRegistryName().hashCode()<<32);if(!visited.add(key))continue;
                    if(!w.getChunkSource().getGenerator().getBiomeSource().getNoiseBiome((p.x<<2)+2,16,(p.z<<2)+2).getGenerationSettings().isValidStart(type))continue;
                    StructureStart<?> start=w.getChunk(p.x,p.z,ChunkStatus.STRUCTURE_STARTS).getStartForFeature(type);
                    if(start!=null&&start.isValid()){BlockPos pos=start.getLocatePos();if(horizontal(origin,pos)>=96*96)found.add(pos);}
                }
            }
            if(r>=2&&found.size()>=(next?2:1))break;
        }
        found.sort(Comparator.comparingDouble(p->horizontal(origin,p)));int index=next&&!size.equals("large")?1:0;
        if(found.size()<=index){source.sendFailure(new TranslationTextComponent("swamp.command.not_found"));return 0;}
        BlockPos pos=found.get(index);source.sendSuccess(new TranslationTextComponent("swamp.command.structure","bandit_camp "+size,pos.getX(),pos.getY(),pos.getZ()),false);
        if(teleport){ServerPlayerEntity player=source.getPlayerOrException();BlockPos safe=null;
            outer:for(int r=0;r<=24;r+=2)for(int x=-r;x<=r;x+=2)for(int z=-r;z<=r;z+=2){if(Math.max(Math.abs(x),Math.abs(z))!=r)continue;BlockPos q=w.getHeightmapPos(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,pos.offset(x,0,z));net.minecraft.block.BlockState ground=w.getBlockState(q.below());if(w.getWorldBorder().isWithinBounds(q)&&ground.isFaceSturdy(w,q.below(),Direction.UP)&&!ground.is(net.minecraft.tags.BlockTags.LEAVES)&&!ground.is(net.minecraft.tags.BlockTags.LOGS)&&!ground.is(net.minecraft.block.Blocks.CAMPFIRE)&&!ground.is(net.minecraft.block.Blocks.MAGMA_BLOCK)&&w.isEmptyBlock(q)&&w.isEmptyBlock(q.above())&&w.getFluidState(q.below()).isEmpty()){safe=q;break outer;}}
            if(safe==null){source.sendFailure(new TranslationTextComponent("bandit.command.no_safe"));return 0;}player.teleportTo(w,safe.getX()+.5,safe.getY(),safe.getZ()+.5,player.yRot,player.xRot);
        }return 1;
    }
    private static double horizontal(BlockPos a,BlockPos b){double x=(double)a.getX()-b.getX(),z=(double)a.getZ()-b.getZ();return x*x+z*z;}
}
