package org.slavicmyths.kurgan;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
/** Damaged finds remain weaker than their restored counterparts. */
public final class BurialWeapon extends SwordItem {
 private final boolean spear;
 public BurialWeapon(boolean spear,boolean ancient,Properties p){super(ancient?Tiers.STONE:Tiers.IRON,p.durability(ancient?90:spear?380:450).attributes(SwordItem.createAttributes(ancient?Tiers.STONE:Tiers.IRON,ancient?2:3,spear?-2.8F:-2.4F)));this.spear=spear;}
 @Override public boolean hurtEnemy(ItemStack s,LivingEntity target,LivingEntity user){if(spear&&!user.level().isClientSide){Vec3 d=user.getLookAngle();target.push(d.x*.18,.03,d.z*.18);}return super.hurtEnemy(s,target,user);}
 @Override public void appendHoverText(ItemStack s,net.minecraft.world.item.Item.TooltipContext w,java.util.List<net.minecraft.network.chat.Component> lines,net.minecraft.world.item.TooltipFlag f){if(getDescriptionId().contains("ancient"))lines.add(Component.translatable("tooltip.slavicmyths.burial_weapon").withStyle(net.minecraft.ChatFormatting.GRAY));}
}
