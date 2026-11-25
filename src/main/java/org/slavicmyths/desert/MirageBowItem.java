package org.slavicmyths.desert;

import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.InteractionHand;
import net.minecraft.network.chat.Component;
import org.slavicmyths.combat.*;
import org.slavicmyths.rpg.PathData;

/** Native held bow draw and release. Special volleys use the common server input contract. */
public final class MirageBowItem extends BowItem implements WeaponInputHandler {
 public MirageBowItem(){super(new Item.Properties().durability(Tiers.DIAMOND.getUses()).rarity(Rarity.RARE).attributes(org.slavicmyths.combat.MaceItem.attributes(DesertWeaponSpec.MIRAGE_BOW.baseDamage,1.4F)));}
 @Override public net.minecraft.world.entity.projectile.AbstractArrow customArrow(net.minecraft.world.entity.projectile.AbstractArrow arrow,ItemStack projectile,ItemStack weapon){
  arrow.setBaseDamage(arrow.getBaseDamage()+26D/3D-2D);arrow.setCritArrow(false);return arrow;
 }
 @Override public boolean intercepts(Action a){return a==Action.SHIFT_LEFT_CLICK||a==Action.SHIFT_RIGHT_CLICK;}
 @Override public void onWeaponAction(ServerPlayer p,ItemStack held,Action a){
  if(!intercepts(a)||held.getItem()!=this||p.getMainHandItem()!=held||!p.isAlive()||p.isSpectator()||!p.isShiftKeyDown())return;
  int slot=a==Action.SHIFT_LEFT_CLICK?1:2;
  String key=DesertWeaponItem.cooldownKey("mirage_bow",slot);long now=p.server.overworld().getGameTime();var state=PathData.data(p);
  if(state.getLong(key)>now)return;
  if(p.isUsingItem())p.stopUsingItem();
  boolean fired=false;
  for(int i=slot==1?-1:0;i<=(slot==1?1:0);i++){
   var shot=new WeaponProjectile(p.serverLevel(),p,new ItemStack(Items.ARROW),true,true);
   shot.configureDesert(slot==1?12:44,0,false,slot==2?4:0);
   shot.shootFromRotation(p,p.getXRot(),p.getYRot()+i*4,0,2.1F,0);
   fired|=p.serverLevel().addFreshEntity(shot);
  }
  if(fired){p.serverLevel().playSound(null,p.blockPosition(),net.minecraft.sounds.SoundEvents.ARROW_SHOOT,net.minecraft.sounds.SoundSource.PLAYERS,.8F,slot==1?1.1F:.8F);state.putLong(key,now+(slot==1?160:300));p.swing(InteractionHand.MAIN_HAND,true);if(!p.hasInfiniteMaterials())held.hurtAndBreak(1,p,EquipmentSlot.MAINHAND);}
 }
 @Override public void appendHoverText(ItemStack stack,TooltipContext context,List<Component> lines,TooltipFlag flag){
  lines.add(Component.translatable("tooltip.slavicmyths.mirage_bow.controls"));
  lines.add(Component.translatable("tooltip.slavicmyths.mirage_bow.volley"));
  lines.add(Component.translatable("tooltip.slavicmyths.mirage_bow.pierce"));
 }
}
