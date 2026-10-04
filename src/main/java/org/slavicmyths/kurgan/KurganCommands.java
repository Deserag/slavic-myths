package org.slavicmyths.kurgan;
import java.util.*;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.*;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.block.*;
import net.minecraft.tags.BlockTags;
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
public final class KurganCommands {
 @SubscribeEvent public static void register(RegisterCommandsEvent e){com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSource> locate=Commands.literal("kurgan"),tp=Commands.literal("tp_kurgan");String[] names={"small","warrior","great"};for(int i=0;i<3;i++){final int k=i;locate.then(Commands.literal(names[i]).executes(c->run(c.getSource(),k,false,false)).then(Commands.literal("next").executes(c->run(c.getSource(),k,true,false))));tp.then(Commands.literal(names[i]).executes(c->run(c.getSource(),k,false,true)).then(Commands.literal("next").executes(c->run(c.getSource(),k,true,true))));}com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSource> modern=Commands.literal("kurgan"), search=Commands.literal("locate"),generate=Commands.literal("generate");
  for(int i=0;i<3;i++){final int k=i;search.then(Commands.literal(names[i]).executes(c->run(c.getSource(),k,false,false)).then(Commands.literal("next").executes(c->run(c.getSource(),k,true,false))));generate.then(Commands.literal(names[i]).executes(c->KurganDebug.generate(c.getSource(),k,c.getSource().getLevel().random.nextLong())).then(Commands.argument("seed",com.mojang.brigadier.arguments.LongArgumentType.longArg()).executes(c->KurganDebug.generate(c.getSource(),k,com.mojang.brigadier.arguments.LongArgumentType.getLong(c,"seed")))));}
  modern.then(search).then(generate).then(Commands.literal("info").executes(c->KurganDebug.info(c.getSource())));
  com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSource> clear=Commands.literal("clear_kurgan_curse").executes(c->KurganDebug.clear(c.getSource(),c.getSource().getPlayerOrException())).then(Commands.argument("player",net.minecraft.command.arguments.EntityArgument.player()).executes(c->KurganDebug.clear(c.getSource(),net.minecraft.command.arguments.EntityArgument.getPlayer(c,"player"))));
  e.getDispatcher().register(Commands.literal("slavicmyths").requires(s->s.hasPermission(2)).then(modern).then(Commands.literal("locate").then(locate)).then(Commands.literal("dev").then(tp).then(clear)));}
 private static double distance(BlockPos a,BlockPos b){double x=a.getX()-b.getX(),z=a.getZ()-b.getZ();return x*x+z*z;}
 private static int run(CommandSource source,int kind,boolean next,boolean teleport)throws CommandSyntaxException{
  ServerWorld w=source.getLevel();if(!w.dimension().equals(World.OVERWORLD)){source.sendFailure(new TranslationTextComponent("swamp.command.overworld"));return 0;}KurganStructure type=KurganStructures.type(kind);StructureSeparationSettings cfg=w.getChunkSource().getGenerator().getSettings().getConfig(type);if(cfg==null)return 0;
  BlockPos origin=new BlockPos(source.getPosition());List<BlockPos> found=new ArrayList<>();Set<Long> visited=new HashSet<>();long previous=source.getEntity() instanceof ServerPlayerEntity?source.getEntity().getPersistentData().getLong("LastKurgan"+kind):Long.MIN_VALUE;
  for(KurganInstance instance:BurialRecords.get(w).instances.values())if(instance.plan.tier==kind){BlockPos p=instance.origin.offset(0,1,instance.plan.radius+2);if(!next||(distance(origin,p)>=192*192&&p.asLong()!=previous))found.add(p);}
  for(int r=0;r<=10;r++){for(int dx=-r;dx<=r;dx++)for(int dz=-r;dz<=r;dz++){if(Math.max(Math.abs(dx),Math.abs(dz))!=r)continue;ChunkPos q=type.getPotentialFeatureChunk(cfg,w.getSeed(),new SharedSeedRandom(),(origin.getX()>>4)+dx*64,(origin.getZ()>>4)+dz*64);if(!visited.add(q.toLong())||KurganShape.kind(w.getSeed(),q.x,q.z)!=kind)continue;if(!w.getChunkSource().getGenerator().getBiomeSource().getNoiseBiome((q.x<<2)+2,16,(q.z<<2)+2).getGenerationSettings().isValidStart(type))continue;StructureStart<?> start=w.getChunk(q.x,q.z,ChunkStatus.STRUCTURE_STARTS).getStartForFeature(type);if(start!=null&&start.isValid()){BlockPos p=start.getLocatePos();for(StructurePiece piece:start.getPieces())if(piece instanceof KurganPiece)p=((KurganPiece)piece).arrival();else if(piece instanceof KurganDungeonPiece)p=((KurganDungeonPiece)piece).arrival();if(next&&(distance(origin,p)<192*192||p.asLong()==previous))continue;found.add(p);}}if(r>=2&&!found.isEmpty())break;}
  if(found.isEmpty()){source.sendFailure(new TranslationTextComponent("kurgan.command.not_found"));return 0;}found.sort(Comparator.comparingDouble(p->distance(origin,p)));BlockPos pos=found.get(0);if(source.getEntity()!=null)source.getEntity().getPersistentData().putLong("LastKurgan"+kind,pos.asLong());
  source.sendSuccess(new TranslationTextComponent("kurgan.command.found",new TranslationTextComponent("kurgan.kind."+kind),pos.getX(),pos.getY(),pos.getZ()),false);
  source.sendSuccess(new TranslationTextComponent("kurgan.command.distance",(int)Math.sqrt(distance(origin,pos))),false);
  if(teleport){BlockPos safe=null;outer:for(int r=0;r<=16;r++)for(int dx=-r;dx<=r;dx++)for(int dz=-r;dz<=r;dz++){if(Math.max(Math.abs(dx),Math.abs(dz))!=r)continue;BlockPos p=w.getHeightmapPos(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,pos.offset(dx,0,dz));BlockState ground=w.getBlockState(p.below());if(w.getWorldBorder().isWithinBounds(p)&&ground.isFaceSturdy(w,p.below(),Direction.UP)&&!ground.is(BlockTags.LEAVES)&&!ground.is(BlockTags.LOGS)&&!ground.is(Blocks.MAGMA_BLOCK)&&!ground.is(Blocks.CAMPFIRE)&&w.isEmptyBlock(p)&&w.isEmptyBlock(p.above())&&w.getFluidState(p.below()).isEmpty()){safe=p;break outer;}}
   if(safe==null){source.sendFailure(new TranslationTextComponent("bandit.command.no_safe"));return 0;}ServerPlayerEntity player=source.getPlayerOrException();player.teleportTo(w,safe.getX()+.5,safe.getY(),safe.getZ()+.5,180,0);
  }return 1;
 }
}
