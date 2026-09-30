package org.slavicmyths.item;

import java.util.List;
import net.minecraft.item.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.*;
import net.minecraft.util.text.*;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.world.World;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import top.theillusivec4.curios.api.*;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public final class FolkAccessoryItem extends Item implements ICurioItem {
 public final String slot;
 public FolkAccessoryItem(Properties properties,String slot){super(properties.stacksTo(1));this.slot=slot;}
 public static void slots(InterModEnqueueEvent event){
  for(SlotTypePreset slot:new SlotTypePreset[]{SlotTypePreset.HEAD,SlotTypePreset.NECKLACE,SlotTypePreset.RING,SlotTypePreset.BELT,SlotTypePreset.CHARM})
   InterModComms.sendTo("curios",SlotTypeMessage.REGISTER_TYPE,()->slot.getMessageBuilder().size(slot==SlotTypePreset.RING?2:1).build());
 }
 public static ItemStack equipped(LivingEntity entity,Item item){return CuriosApi.getCuriosHelper().findEquippedCurio(item,entity).map(t->t.getRight()).orElse(ItemStack.EMPTY);}
 public static void award(LivingEntity entity,String id){
  if(entity instanceof net.minecraft.entity.player.ServerPlayerEntity){
   net.minecraft.entity.player.ServerPlayerEntity p=(net.minecraft.entity.player.ServerPlayerEntity)entity;
   net.minecraft.advancements.Advancement a=p.server.getAdvancements().getAdvancement(new net.minecraft.util.ResourceLocation("slavicmyths",id));
   if(a!=null)p.getAdvancements().award(a,"event");
  }
 }
 @Override public void onEquip(String identifier,int index,LivingEntity entity,ItemStack stack){
  if(entity.level.isClientSide)return;award(entity,"first_accessory");
  if(stack.getRarity()==Rarity.RARE)award(entity,"rare_accessory");
 }
 @Override public boolean canEquip(String identifier,LivingEntity wearer,ItemStack stack){return slot.equals(identifier);}
 @Override public void appendHoverText(ItemStack stack,World world,List<ITextComponent> lines,ITooltipFlag flag){
  lines.add(new TranslationTextComponent(getDescriptionId()+".effect").withStyle(TextFormatting.GRAY));
 }
 @Override public void curioTick(String identifier,int index,LivingEntity entity,ItemStack stack){
  if(!(entity instanceof PlayerEntity)||equipped(entity,this)!=stack)return;
  PlayerEntity player=(PlayerEntity)entity;
  if(!entity.level.isClientSide&&slot.equals("ring")&&player.tickCount%20==0&&FolkEquipmentEffects.wears(player,org.slavicmyths.registry.ModItems.PERUN_RING.get())&&FolkEquipmentEffects.wears(player,org.slavicmyths.registry.ModItems.RESIN_RING.get()))award(player,"two_rings");
  if(this==org.slavicmyths.registry.ModItems.INVISIBILITY_CAP.get()){
   net.minecraft.nbt.CompoundNBT data=player.getPersistentData();
   int fade=data.getInt("SlavicCapFade");
   if(player.hurtTime>0||player.swinging||player.isSprinting()||player.isUsingItem())data.putLong("SlavicCapBroken",player.level.getGameTime()+100);
   if(player.level.getGameTime()<data.getLong("SlavicCapBroken"))fade=0;else fade=Math.min(20,fade+1);
   data.putInt("SlavicCapFade",fade);
   if(!entity.level.isClientSide&&fade==20)player.addEffect(new EffectInstance(Effects.INVISIBILITY,5,0,false,false));
  }
  if(entity.level.isClientSide)return;
  if(this==org.slavicmyths.registry.ModItems.RESIN_RING.get()&&player.tickCount%100==0&&player.getFoodData().getFoodLevel()>=18&&player.getHealth()<player.getMaxHealth()){
   player.heal(1);player.causeFoodExhaustion(2);
  }
 }
 @Override public void onUnequip(String identifier,int index,LivingEntity entity,ItemStack stack){
  if(this==org.slavicmyths.registry.ModItems.INVISIBILITY_CAP.get())entity.getPersistentData().remove("SlavicCapFade");
 }
}
