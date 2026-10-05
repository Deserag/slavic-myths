package org.slavicmyths.bandit;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.placement.*;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import org.slavicmyths.worldgen.StructureCandidates;
import net.minecraft.network.chat.Component;
import java.util.*;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.sounds.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.*;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths")
public final class CampCommands {
    @SubscribeEvent public static void register(RegisterCommandsEvent e){
        com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> locate=Commands.literal("bandit_camp"),tp=Commands.literal("tp_bandit_camp");
        for(String size:new String[]{"small","medium","large","any"}){
            locate.then(Commands.literal(size).executes(c->run(c.getSource(),size,false,false)).then(Commands.literal("next").executes(c->run(c.getSource(),size,true,false))));
            tp.then(Commands.literal(size).executes(c->run(c.getSource(),size,false,true)).then(Commands.literal("next").executes(c->run(c.getSource(),size,true,true))));
        }
        e.getDispatcher().register(Commands.literal("slavicmyths").then(Commands.literal("locate").requires(source->source.hasPermission(2)).then(locate)).then(Commands.literal("dev").requires(source->source.hasPermission(2)).then(tp).then(Commands.literal("tp_nightingale").executes(c->run(c.getSource(),"nightingale",false,true))).then(Commands.literal("spawn_nightingale").executes(c->{ServerPlayer p=c.getSource().getPlayerOrException();NightingaleEntity boss=org.slavicmyths.registry.ModEntities.NIGHTINGALE.get().create(p.serverLevel());BlockPos at=p.blockPosition().relative(p.getDirection(),4);boss.home=at;boss.moveTo(at.getX()+.5,at.getY(),at.getZ()+.5,180,0);return p.serverLevel().addFreshEntity(boss)?1:0;}))));
    }
    private static int run(CommandSourceStack source,String size,boolean next,boolean teleport)throws CommandSyntaxException {
        ServerLevel w=source.getLevel();if(!w.dimension().equals(Level.OVERWORLD)){source.sendFailure(Component.translatable("swamp.command.overworld"));return 0;}
        BlockPos origin=BlockPos.containing(source.getPosition());List<BlockPos> found=new ArrayList<>();Set<Long> visited=new HashSet<>();
        for(int r=0;r<=7;r++){
            for(Structure type:new Structure[]{org.slavicmyths.swamp.SwampStructures.get(w,"bandit_camp_small"),org.slavicmyths.swamp.SwampStructures.get(w,"bandit_camp_medium"),org.slavicmyths.swamp.SwampStructures.get(w,"bandit_camp_large")}){
                if(!size.equals("any")&&type!=(size.equals("small")?org.slavicmyths.swamp.SwampStructures.get(w,"bandit_camp_small"):size.equals("medium")?org.slavicmyths.swamp.SwampStructures.get(w,"bandit_camp_medium"):org.slavicmyths.swamp.SwampStructures.get(w,"bandit_camp_large")))continue;
                RandomSpreadStructurePlacement cfg=StructureCandidates.placement(w,type);if(cfg==null)continue;
                for(int dx=-r;dx<=r;dx++)for(int dz=-r;dz<=r;dz++){
                    if(Math.max(Math.abs(dx),Math.abs(dz))!=r)continue;ChunkPos p=cfg.getPotentialStructureChunk(w.getSeed(),(origin.getX()>>4)+dx*cfg.spacing(),(origin.getZ()>>4)+dz*cfg.spacing());
                    long key=p.toLong()^((long)w.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.STRUCTURE).getKey(type).hashCode()<<32);if(!visited.add(key))continue;
                    if(!StructureCandidates.biome(w,type,p))continue;
                    StructureStart start=w.getChunk(p.x,p.z,ChunkStatus.STRUCTURE_STARTS).getStartForStructure(type);
                    if(start!=null&&start.isValid()){BlockPos pos=StructureCandidates.locate(start);if(size.equals("nightingale")){for(StructurePiece piece:start.getPieces())if(piece instanceof LargeCampPiece){BlockPos yard=((LargeCampPiece)piece).nightingaleArrival();if(yard!=null){pos=yard;break;}}}if(horizontal(origin,pos)>=96*96)found.add(pos);}
                }
            }
            if(r>=2&&found.size()>=(next?2:1))break;
        }
        found.sort(Comparator.comparingDouble(p->horizontal(origin,p)));int index=next&&!size.equals("large")?1:0;
        if(found.size()<=index){source.sendFailure(Component.translatable("swamp.command.not_found"));return 0;}
        BlockPos pos=found.get(index);source.sendSuccess(()->Component.translatable("swamp.command.structure","bandit_camp "+size,pos.getX(),pos.getY(),pos.getZ()),false);
        if(teleport){ServerPlayer player=source.getPlayerOrException();BlockPos safe=null;
            outer:for(int r=0;r<=24;r+=2)for(int x=-r;x<=r;x+=2)for(int z=-r;z<=r;z+=2){if(Math.max(Math.abs(x),Math.abs(z))!=r)continue;BlockPos q=w.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,pos.offset(x,0,z));net.minecraft.world.level.block.state.BlockState ground=w.getBlockState(q.below());if(w.getWorldBorder().isWithinBounds(q)&&ground.isFaceSturdy(w,q.below(),Direction.UP)&&!ground.is(net.minecraft.tags.BlockTags.LEAVES)&&!ground.is(net.minecraft.tags.BlockTags.LOGS)&&!ground.is(net.minecraft.world.level.block.Blocks.CAMPFIRE)&&!ground.is(net.minecraft.world.level.block.Blocks.MAGMA_BLOCK)&&w.isEmptyBlock(q)&&w.isEmptyBlock(q.above())&&w.getFluidState(q.below()).isEmpty()){safe=q;break outer;}}
            if(safe==null){source.sendFailure(Component.translatable("bandit.command.no_safe"));return 0;}player.teleportTo(w,safe.getX()+.5,safe.getY(),safe.getZ()+.5,player.getYRot(),player.getXRot());
        }return 1;
    }
    private static double horizontal(BlockPos a,BlockPos b){double x=(double)a.getX()-b.getX(),z=(double)a.getZ()-b.getZ();return x*x+z*z;}
}
