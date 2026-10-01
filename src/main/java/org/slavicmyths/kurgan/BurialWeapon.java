package org.slavicmyths.kurgan;
import net.minecraft.item.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.vector.Vector3d;
/** Damaged finds remain weaker than their restored counterparts. */
public final class BurialWeapon extends SwordItem {
 private final boolean spear;
 public BurialWeapon(boolean spear,boolean ancient,Properties p){super(ancient?ItemTier.STONE:ItemTier.IRON,ancient?2:3,spear?-2.8F:-2.4F,p.durability(ancient?90:spear?380:450));this.spear=spear;}
 @Override public boolean hurtEnemy(ItemStack s,LivingEntity target,LivingEntity user){if(spear&&!user.level.isClientSide){Vector3d d=user.getLookAngle();target.push(d.x*.18,.03,d.z*.18);}return super.hurtEnemy(s,target,user);}
 @Override public void appendHoverText(ItemStack s,net.minecraft.world.World w,java.util.List<net.minecraft.util.text.ITextComponent> lines,net.minecraft.client.util.ITooltipFlag f){if(getDescriptionId().contains("ancient"))lines.add(new net.minecraft.util.text.TranslationTextComponent("tooltip.slavicmyths.burial_weapon").withStyle(net.minecraft.util.text.TextFormatting.GRAY));}
}
