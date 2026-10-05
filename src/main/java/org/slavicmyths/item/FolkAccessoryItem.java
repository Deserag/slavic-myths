package org.slavicmyths.item;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.*;
import net.minecraft.network.chat.*;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;


import top.theillusivec4.curios.api.*;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public final class FolkAccessoryItem extends Item implements ICurioItem {
 public final String slot;
 public FolkAccessoryItem(Properties properties,String slot){super(properties.stacksTo(1));this.slot=slot;}
 public static ItemStack equipped(LivingEntity entity,Item item){return CuriosApi.getCuriosInventory(entity).flatMap(handler -> handler.findFirstCurio(item)).map(SlotResult::stack).orElse(ItemStack.EMPTY);}
 public static void award(LivingEntity entity,String id){
  if(entity instanceof net.minecraft.server.level.ServerPlayer){
   net.minecraft.server.level.ServerPlayer p=(net.minecraft.server.level.ServerPlayer)entity;
   net.minecraft.advancements.AdvancementHolder a=p.server.getAdvancements().get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths",id));
   if(a!=null)p.getAdvancements().award(a,"event");
  }
 }
 @Override public void onEquip(SlotContext context,ItemStack previous,ItemStack stack){
  LivingEntity entity=context.entity();
  if(entity.level().isClientSide)return;award(entity,"first_accessory");
  if(stack.getRarity()==Rarity.RARE)award(entity,"rare_accessory");
 }
 @Override public boolean canEquip(SlotContext context,ItemStack stack){return slot.equals(context.identifier());}
 @Override public void appendHoverText(ItemStack stack,Item.TooltipContext context,List<Component> lines,TooltipFlag flag){
  lines.add(Component.translatable(getDescriptionId()+".effect").withStyle(net.minecraft.ChatFormatting.GRAY));
 }
 @Override public void curioTick(SlotContext context,ItemStack stack){
  LivingEntity entity=context.entity();
  if(!(entity instanceof Player)||equipped(entity,this)!=stack)return;
  Player player=(Player)entity;
  if(!entity.level().isClientSide&&slot.equals("ring")&&player.tickCount%20==0&&FolkEquipmentEffects.wears(player,org.slavicmyths.registry.ModItems.PERUN_RING.get())&&FolkEquipmentEffects.wears(player,org.slavicmyths.registry.ModItems.RESIN_RING.get()))award(player,"two_rings");
  if(this==org.slavicmyths.registry.ModItems.INVISIBILITY_CAP.get()){
   net.minecraft.nbt.CompoundTag data=player.getPersistentData();
   int fade=data.getInt("SlavicCapFade");
    if(player.hurtTime>0||player.swinging||player.isSprinting()||player.isUsingItem())data.putLong("SlavicCapBroken",Math.max(data.getLong("SlavicCapBroken"),player.level().getGameTime()+100));
   if(player.level().getGameTime()<data.getLong("SlavicCapBroken"))fade=0;else fade=Math.min(20,fade+1);
   long now=player.level().getGameTime();
   if(fade==20){long until=data.getLong("SlavicCapUntil");if(until==0)data.putLong("SlavicCapUntil",now+160);else if(now>=until){data.putLong("SlavicCapBroken",now+600);data.remove("SlavicCapUntil");fade=0;}}
   data.putInt("SlavicCapFade",fade);
   if(!entity.level().isClientSide&&fade==20)player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY,5,0,false,false));
  }
  if(entity.level().isClientSide)return;
  if(this==org.slavicmyths.registry.ModItems.DEPTH_AMULET.get()&&player.isInWater()&&player.tickCount%4==0&&player.getAirSupply()>0)player.setAirSupply(Math.min(player.getMaxAirSupply(),player.getAirSupply()+1));
  if(this==org.slavicmyths.registry.ModItems.RESIN_RING.get()&&player.tickCount%100==0&&player.getFoodData().getFoodLevel()>=18&&player.getHealth()<player.getMaxHealth()){
   player.heal(1);player.causeFoodExhaustion(2);
  }
 }
 @Override public void onUnequip(SlotContext context,ItemStack next,ItemStack stack){
  LivingEntity entity=context.entity();
  if(this==org.slavicmyths.registry.ModItems.POYAS_TUGARINA.get()&&!entity.level().isClientSide)org.slavicmyths.hunt.BossEffects.clearBelt(entity);
  if(this==org.slavicmyths.registry.ModItems.VOLCHIY_POYAS.get()&&!entity.level().isClientSide)org.slavicmyths.hunt.HuntEffects.removeBelt(entity);
  if(this==org.slavicmyths.registry.ModItems.INVISIBILITY_CAP.get())entity.getPersistentData().remove("SlavicCapFade");
 }
}
