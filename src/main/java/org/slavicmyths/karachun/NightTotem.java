package org.slavicmyths.karachun;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
public final class NightTotem extends Item {
 public NightTotem(){super(new Properties().stacksTo(1).rarity(Rarity.RARE));}
 @Override public InteractionResult useOn(UseOnContext c){if(!(c.getPlayer() instanceof ServerPlayer p))return InteractionResult.sidedSuccess(c.getLevel().isClientSide);var w=p.serverLevel();var d=NightLedger.get(w);if(w.dimension()!=Level.OVERWORLD||p.isSpectator()||!p.isAlive())return fail(p,"overworld");var sessions=d.activeSessions();if(sessions)return fail(p,"active");if(!w.mayInteract(p,c.getClickedPos())||p.server.isUnderSpawnProtection(w,c.getClickedPos(),p))return fail(p,"space");var aim=p.position().add(p.getLookAngle().multiply(1,0,1).normalize().scale(8));var at=org.slavicmyths.morozko.FrostWorld.safe(w,aim,1.5f,4,3);if(at==null||!w.mayInteract(p,net.minecraft.core.BlockPos.containing(at))||p.server.isUnderSpawnProtection(w,net.minecraft.core.BlockPos.containing(at),p))return fail(p,"space");var b=NightContent.BOSS.get().create(w);if(b==null)return InteractionResult.FAIL;b.session=java.util.UUID.randomUUID();b.owner=p.getUUID();b.anchor=p.position();b.configureHealth((float)Math.max(100,NightConfig.value("health")));b.setPos(at);if(!w.addFreshEntity(b))return fail(p,"space");var s=d.session(b.session);s.putUUID("Boss",b.getUUID());s.putUUID("Owner",p.getUUID());s.putLong("Started",d.now(w));d.setDirty();NightContent.sound(b,"enter_trial");org.slavicmyths.tales.TaleService.promote(p,"slavicmyths:karachun_trial",3,false);p.sendSystemMessage(NightContent.text("start"));return InteractionResult.CONSUME;}
 private InteractionResult fail(ServerPlayer p,String reason){p.displayClientMessage(NightContent.text(reason),true);return InteractionResult.FAIL;}
 @Override public void appendHoverText(ItemStack s,TooltipContext c,java.util.List<net.minecraft.network.chat.Component> t,TooltipFlag f){t.add(NightContent.text("totem_hint"));}
}
