package org.slavicmyths.morozko;
import net.minecraft.world.item.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import org.slavicmyths.combat.WeaponInputHandler;
public final class FrostStaff extends org.slavicmyths.combat.CombatStaffItem implements WeaponInputHandler {
 public FrostStaff(){super(new Properties().stacksTo(1).rarity(Rarity.EPIC),8,18);}
 // Preserve the existing melee hit on an entity, block mining and held RMB stream.
 // A bolt replaces only the old left-click-in-air behavior.
 @Override public boolean intercepts(Action action, HitResult.Type hit) {
  return switch(action) {
   case LEFT_CLICK -> hit==HitResult.Type.MISS;
   case RIGHT_CLICK -> false;
   case SHIFT_LEFT_CLICK, SHIFT_RIGHT_CLICK -> true;
  };
 }
 @Override public void onWeaponAction(ServerPlayer player,ItemStack held,Action action) {
  if(held!=player.getMainHandItem()||!player.isAlive()||player.isSpectator())return;
  switch(action) {
   case LEFT_CLICK -> FrostSkills.action(player,0);
   case SHIFT_LEFT_CLICK -> FrostSkills.action(player,1);
   case SHIFT_RIGHT_CLICK -> FrostSkills.action(player,2);
   case RIGHT_CLICK -> {}
  }
 }
 @Override public InteractionResultHolder<ItemStack> use(Level w,Player p,InteractionHand h){var s=p.getItemInHand(h);if(p instanceof ServerPlayer server&&!FrostSkills.startStream(server))return InteractionResultHolder.fail(s);p.startUsingItem(h);return InteractionResultHolder.consume(s);}
 @Override public int getUseDuration(ItemStack s,LivingEntity e){return FrostRules.STREAM_DURATION;}
 @Override public UseAnim getUseAnimation(ItemStack s){return UseAnim.BOW;}
 @Override public void onUseTick(Level w,LivingEntity e,ItemStack s,int left){if(e instanceof ServerPlayer p){if(p.getPersistentData().contains("FrostStaffPose")){if(FrostSkills.now(p)>=p.getPersistentData().getLong("FrostStaffPose")){p.getPersistentData().remove("FrostStaffPose");p.stopUsingItem();}return;}FrostSkills.streamTick(p,left);}}
 @Override public void releaseUsing(ItemStack s,Level w,LivingEntity e,int left){if(e instanceof ServerPlayer p)FrostSkills.stopStream(p,"stream_released");}
 @Override public ItemStack finishUsingItem(ItemStack s,Level w,LivingEntity e){if(e instanceof ServerPlayer p)FrostSkills.stopStream(p,"stream_finished");return s;}
 @Override public void appendHoverText(ItemStack s,TooltipContext c,java.util.List<net.minecraft.network.chat.Component> l,TooltipFlag f){super.appendHoverText(s,c,l,f);l.add(FrostContent.text("staff_hint"));l.add(FrostContent.text("staff_stats"));}
}
