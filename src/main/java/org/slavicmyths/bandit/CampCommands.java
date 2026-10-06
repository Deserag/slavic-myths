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
        if(teleport)source.getPlayerOrException();BlockPos origin=BlockPos.containing(source.getPosition());
        var known=org.slavicmyths.worldgen.ManualStructureRecords.get(w).entries().stream().filter(e->e.family().equals("bandit_camp")&&(size.equals("any")||e.tier()==(size.equals("small")?0:size.equals("medium")?1:2))).map(org.slavicmyths.worldgen.ManualStructureRecords.Entry::arrival).filter(p->horizontal(origin,p)>=96*96).sorted(Comparator.comparingDouble(p->horizontal(origin,p))).toList();
        int knownIndex=next&&!size.equals("large")?1:0;if(!size.equals("nightingale")&&known.size()>knownIndex)return org.slavicmyths.worldgen.BoundedStructureSearch.deliver(source,known.get(knownIndex),teleport,p->report(source,size,teleport,p));
        var types=new java.util.ArrayList<Structure>();for(String id:new String[]{"bandit_camp_small","bandit_camp_medium","bandit_camp_large"})if(size.equals("any")||id.equals("bandit_camp_"+(size.equals("nightingale")?"large":size)))types.add(org.slavicmyths.swamp.SwampStructures.get(w,id));
        int[] seen={0};return org.slavicmyths.worldgen.BoundedStructureSearch.start(source,types,start->{if(size.equals("nightingale"))for(var piece:start.getPieces())if(piece instanceof LargeCampPiece large){var arrival=large.nightingaleArrival();if(arrival!=null)return arrival;}return StructureCandidates.locate(start);},p->horizontal(origin,p)>=96*96&&(!next||seen[0]++>0),p->report(source,size,teleport,p));
    }
    private static void report(CommandSourceStack source,String size,boolean teleport,BlockPos pos){
        ServerLevel w=source.getLevel();
        source.sendSuccess(()->Component.translatable("swamp.command.structure","bandit_camp "+size,pos.getX(),pos.getY(),pos.getZ()),false);
        if(teleport){ServerPlayer player=source.getEntity() instanceof ServerPlayer p?p:null;if(player==null)return;BlockPos safe=null;
            outer:for(int r=0;r<=24;r+=2)for(int x=-r;x<=r;x+=2)for(int z=-r;z<=r;z+=2){if(Math.max(Math.abs(x),Math.abs(z))!=r)continue;BlockPos base=pos.offset(x,0,z);if(w.getChunkSource().getChunkNow(base.getX()>>4,base.getZ()>>4)==null)continue;BlockPos q=w.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,pos.offset(x,0,z));net.minecraft.world.level.block.state.BlockState ground=w.getBlockState(q.below());if(w.getWorldBorder().isWithinBounds(q)&&ground.isFaceSturdy(w,q.below(),Direction.UP)&&!ground.is(net.minecraft.tags.BlockTags.LEAVES)&&!ground.is(net.minecraft.tags.BlockTags.LOGS)&&!ground.is(net.minecraft.world.level.block.Blocks.CAMPFIRE)&&!ground.is(net.minecraft.world.level.block.Blocks.MAGMA_BLOCK)&&w.isEmptyBlock(q)&&w.isEmptyBlock(q.above())&&w.getFluidState(q.below()).isEmpty()){safe=q;break outer;}}
            if(safe==null){source.sendFailure(Component.translatable("bandit.command.no_safe"));return;}player.teleportTo(w,safe.getX()+.5,safe.getY(),safe.getZ()+.5,player.getYRot(),player.getXRot());
        }
    }
    private static double horizontal(BlockPos a,BlockPos b){double x=(double)a.getX()-b.getX(),z=(double)a.getZ()-b.getZ();return x*x+z*z;}
}
