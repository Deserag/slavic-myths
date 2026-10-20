package org.slavicmyths.skim;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.minecraft.commands.Commands;
@EventBusSubscriber(modid="slavicmyths")
public final class SkimEvents {
 @SubscribeEvent public static void tick(ServerTickEvent.Post e){var w=e.getServer().overworld();HeroCalls.get(w).tickLifecycles(e.getServer());HeroCalls.get(w).departures(e.getServer());HeroCalls.get(w).recover(w);SkimValley.get(w).tick(w);}
 @SubscribeEvent public static void incoming(LivingIncomingDamageEvent e){if(e.getEntity().level().isClientSide)return;if(e.getEntity() instanceof AlyoshaEntity a&&a.evade(e.getSource()))e.setCanceled(true);}
 @SubscribeEvent public static void post(LivingDamageEvent.Post e){if(e.getEntity() instanceof SkimBeastEntity b)b.registeredHit(e.getSource(),e.getOriginalDamage(),e.getNewDamage());}
 @SubscribeEvent public static void pre(LivingDamageEvent.Pre e){if(e.getEntity().level().isClientSide)return;if(e.getEntity() instanceof SkimBeastEntity b&&b.action()==SkimAction.EXHAUSTED)e.setNewDamage(e.getNewDamage()*1.25F);if(e.getEntity() instanceof AlyoshaEntity a&&e.getNewDamage()>0){a.incomingHit();if(e.getNewDamage()>=a.getHealth()-26.25F&&!a.recovering()&&!e.getSource().is(net.minecraft.world.damagesource.DamageTypes.GENERIC_KILL)){a.intercepted=true;e.setNewDamage(0);a.retreat();}}}
 @SubscribeEvent public static void join(net.neoforged.neoforge.event.entity.EntityJoinLevelEvent e){if(!(e.getLevel() instanceof net.minecraft.server.level.ServerLevel w))return;if(HeroCalls.get(w).retired.contains(e.getEntity().getUUID())){e.setCanceled(true);e.getEntity().discard();HeroCalls.get(w).setDirty();return;}HeroCalls.get(w).reconcile(e.getEntity());if(e.getEntity() instanceof net.minecraft.world.entity.monster.Monster m){m.targetSelector.addGoal(0,new net.minecraft.world.entity.ai.goal.Goal(){{setFlags(java.util.EnumSet.of(Flag.TARGET));}private AlyoshaEntity hero(){var n=m.getPersistentData();return n.hasUUID("AlyoshaTaunt")&&n.getLong("AlyoshaTauntUntil")>w.getGameTime()&&w.getEntity(n.getUUID("AlyoshaTaunt")) instanceof AlyoshaEntity h&&h.isAlive()&&!h.recovering()&&m.hasLineOfSight(h)?h:null;}public boolean canUse(){return hero()!=null;}public boolean canContinueToUse(){return hero()!=null;}public void tick(){m.setTarget(hero());}public void stop(){if(m.getTarget() instanceof AlyoshaEntity)m.setTarget(null);}});}}
 public static void commands(org.slavicmyths.commands.CommandTree tree){tree.add(Commands.literal("slavicmyths")
  .then(Commands.literal("hero").then(Commands.literal("alyosha").executes(c->HeroCalls.toggle(c.getSource().getPlayerOrException(),"alyosha_popovich")?1:0)))
  .then(Commands.literal("skim_valley").requires(s->s.hasPermission(2))
   .then(Commands.literal("place").executes(c->{var p=c.getSource().getPlayerOrException();var at=net.minecraft.core.BlockPos.containing(p.position().add(p.getLookAngle().multiply(70,0,70))).below();boolean ok=SkimValley.place(p,at,Math.floorMod(Math.round(p.getYRot()/90),4));if(!ok)p.sendSystemMessage(net.minecraft.network.chat.Component.translatable("message.slavicmyths.valley.unsafe"));return ok?1:0;}))
   .then(Commands.literal("status").executes(c->{var p=c.getSource().getPlayerOrException();var v=SkimValley.get(p.serverLevel()).near(p.position());c.getSource().sendSuccess(()->net.minecraft.network.chat.Component.literal(v==null?"No Skim valley":v.save().toString()),false);return 1;}))));}
 private SkimEvents(){}
}
