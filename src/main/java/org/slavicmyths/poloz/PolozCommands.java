package org.slavicmyths.poloz;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
public final class PolozCommands {
 public static void register(org.slavicmyths.commands.CommandTree tree){var root=Commands.literal("test");
  root.then(Commands.literal("poloz_spawn").then(Commands.literal("land").executes(c->spawn(c.getSource(),false))).then(Commands.literal("water").executes(c->spawn(c.getSource(),true))));
  root.then(Commands.literal("poloz_phase").then(Commands.argument("phase",com.mojang.brigadier.arguments.IntegerArgumentType.integer(1,3)).executes(c->{var b=PolozEvent.get(c.getSource().getLevel()).loaded(c.getSource().getLevel());if(b==null)return 0;b.setPhaseForTest(com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(c,"phase"));return 1;})));
  root.then(Commands.literal("poloz_weakpoints").executes(c->{var b=PolozEvent.get(c.getSource().getLevel()).loaded(c.getSource().getLevel());if(b==null)return 0;b.debugWeakpoints();return 1;}));
  root.then(Commands.literal("poloz_cancel").executes(c->{var w=c.getSource().getLevel();var d=PolozEvent.get(w);var b=d.loaded(w);if(b!=null)b.cancel();else d.finish(w,false);c.getSource().sendSuccess(()->Component.translatable("poloz.cancel_warning"),true);return 1;}));
  tree.add(Commands.literal("sm").requires(s->s.hasPermission(2)).then(root));
 }
 private static int spawn(net.minecraft.commands.CommandSourceStack source,boolean water){var w=source.getLevel();var d=PolozEvent.get(w);if(w.dimension()!=net.minecraft.world.level.Level.OVERWORLD||d.running())return 0;var pos=net.minecraft.core.BlockPos.containing(source.getPosition().add(0,0,12));pos=w.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,pos);if(water&&w.getFluidState(pos.below()).isEmpty()){source.sendFailure(Component.translatable("poloz.need_water"));return 0;}var boss=PolozContent.BOSS.get().create(w);if(boss==null)return 0;boss.moveTo(Vec3.atBottomCenterOf(pos));boss.setHealth(3000);boss.initializeBody();if(!w.addFreshEntity(boss))return 0;d.boss=boss.getUUID();d.event=java.util.UUID.randomUUID();d.spawn=pos;d.state=PolozEvent.State.ACTIVE;d.elapsed=180;d.setDirty();source.sendSuccess(()->Component.translatable("poloz.cancel_warning"),true);return 1;}
}
