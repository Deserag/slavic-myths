package org.slavicmyths.kurgan;
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
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.tags.BlockTags;
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
public final class KurganCommands {
 @SubscribeEvent public static void register(RegisterCommandsEvent e){com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> locate=Commands.literal("kurgan"),tp=Commands.literal("tp_kurgan");String[] names={"small","warrior","great"};for(int i=0;i<3;i++){final int k=i;locate.then(Commands.literal(names[i]).executes(c->run(c.getSource(),k,false,false)).then(Commands.literal("next").executes(c->run(c.getSource(),k,true,false))));tp.then(Commands.literal(names[i]).executes(c->run(c.getSource(),k,false,true)).then(Commands.literal("next").executes(c->run(c.getSource(),k,true,true))));}com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> modern=Commands.literal("kurgan").requires(source->source.hasPermission(2)), search=Commands.literal("locate").requires(source->source.hasPermission(2)),generate=Commands.literal("generate");
  for(int i=0;i<3;i++){final int k=i;search.then(Commands.literal(names[i]).executes(c->run(c.getSource(),k,false,false)).then(Commands.literal("next").executes(c->run(c.getSource(),k,true,false))));generate.then(Commands.literal(names[i]).executes(c->KurganDebug.generate(c.getSource(),k,c.getSource().getLevel().random.nextLong())).then(Commands.argument("seed",com.mojang.brigadier.arguments.LongArgumentType.longArg()).executes(c->KurganDebug.generate(c.getSource(),k,com.mojang.brigadier.arguments.LongArgumentType.getLong(c,"seed")))));}
  modern.then(search).then(generate).then(Commands.literal("info").executes(c->KurganDebug.info(c.getSource())));
  com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> clear=Commands.literal("clear_kurgan_curse").executes(c->KurganDebug.clear(c.getSource(),c.getSource().getPlayerOrException())).then(Commands.argument("player",net.minecraft.commands.arguments.EntityArgument.player()).executes(c->KurganDebug.clear(c.getSource(),net.minecraft.commands.arguments.EntityArgument.getPlayer(c,"player"))));
  e.getDispatcher().register(Commands.literal("slavicmyths").then(modern).then(Commands.literal("locate").requires(source->source.hasPermission(2)).then(locate)).then(Commands.literal("dev").requires(source->source.hasPermission(2)).then(tp).then(clear)));}
 private static double distance(BlockPos a,BlockPos b){double x=a.getX()-b.getX(),z=a.getZ()-b.getZ();return x*x+z*z;}
 private static int run(CommandSourceStack source,int kind,boolean next,boolean teleport)throws CommandSyntaxException{
  ServerLevel w=source.getLevel();if(!w.dimension().equals(Level.OVERWORLD)){source.sendFailure(Component.translatable("swamp.command.overworld"));return 0;}
  if(teleport)source.getPlayerOrException();BlockPos origin=BlockPos.containing(source.getPosition());long previous=source.getEntity() instanceof ServerPlayer?source.getEntity().getPersistentData().getLong("LastKurgan"+kind):Long.MIN_VALUE;
  java.util.function.Predicate<BlockPos> allowed=p->!next||(distance(origin,p)>=192*192&&p.asLong()!=previous);
  var known=BurialRecords.get(w).instances.values().stream().filter(i->i.plan.tier==kind).map(i->i.origin.offset(0,1,i.plan.radius+2)).filter(allowed).min(Comparator.comparingDouble(p->distance(origin,p)));
  if(known.isPresent())return org.slavicmyths.worldgen.BoundedStructureSearch.deliver(source,known.get(),teleport,p->report(source,kind,teleport,p));
  Structure type=org.slavicmyths.swamp.SwampStructures.get(w,KurganStructures.id(kind));
  return org.slavicmyths.worldgen.BoundedStructureSearch.start(source,java.util.Collections.singletonList(type),start->{for(var piece:start.getPieces()){if(piece instanceof KurganPiece k)return k.arrival();if(piece instanceof KurganDungeonPiece k)return k.arrival();}return StructureCandidates.locate(start);},allowed,p->report(source,kind,teleport,p));
 }
 private static void report(CommandSourceStack source,int kind,boolean teleport,BlockPos pos){
  ServerLevel w=source.getLevel();BlockPos origin=BlockPos.containing(source.getPosition());
  if(source.getEntity()!=null)source.getEntity().getPersistentData().putLong("LastKurgan"+kind,pos.asLong());
  source.sendSuccess(()->Component.translatable("kurgan.command.found",Component.translatable("kurgan.kind."+kind),pos.getX(),pos.getY(),pos.getZ()),false);
  source.sendSuccess(()->Component.translatable("kurgan.command.distance",(int)Math.sqrt(distance(origin,pos))),false);
  if(teleport){BlockPos safe=null;outer:for(int r=0;r<=16;r++)for(int dx=-r;dx<=r;dx++)for(int dz=-r;dz<=r;dz++){if(Math.max(Math.abs(dx),Math.abs(dz))!=r)continue;BlockPos base=pos.offset(dx,0,dz);if(w.getChunkSource().getChunkNow(base.getX()>>4,base.getZ()>>4)==null)continue;BlockPos p=w.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,pos.offset(dx,0,dz));BlockState ground=w.getBlockState(p.below());if(w.getWorldBorder().isWithinBounds(p)&&ground.isFaceSturdy(w,p.below(),Direction.UP)&&!ground.is(BlockTags.LEAVES)&&!ground.is(BlockTags.LOGS)&&!ground.is(Blocks.MAGMA_BLOCK)&&!ground.is(Blocks.CAMPFIRE)&&w.isEmptyBlock(p)&&w.isEmptyBlock(p.above())&&w.getFluidState(p.below()).isEmpty()){safe=p;break outer;}}
   if(safe==null){source.sendFailure(Component.translatable("bandit.command.no_safe"));return;}ServerPlayer player=source.getEntity() instanceof ServerPlayer p?p:null;if(player==null)return;player.teleportTo(w,safe.getX()+.5,safe.getY(),safe.getZ()+.5,180,0);
  }
 }
}
