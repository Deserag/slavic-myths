package org.slavicmyths.nethersmithing;
import java.util.*;import net.minecraft.world.item.*;import net.minecraft.world.entity.*;import net.minecraft.world.entity.projectile.*;import net.minecraft.world.level.Level;import net.minecraft.server.level.ServerPlayer;import net.minecraft.server.level.ServerLevel;import net.minecraft.world.InteractionHand;
/** The same FieldBow/vanilla ammo, enchantment and arrow engine. One shot, no second entity. */
public final class InfernalBow extends org.slavicmyths.combat.FieldBowItem {
 private static final class Shot {final ServerPlayer owner;final ItemStack stack;final boolean requested;boolean committed,special;Shot(ServerPlayer p,ItemStack s,boolean requested){owner=p;stack=s;this.requested=requested;}}
 private static final ThreadLocal<Shot> SHOT=new ThreadLocal<>();
 public InfernalBow(){super(true,new Item.Properties().durability(1200).fireResistant().rarity(Rarity.EPIC));}
 @Override public int drawTicks(){return 24;}
 @Override public void releaseUsing(ItemStack stack,Level w,LivingEntity user,int remaining){if(!(user instanceof ServerPlayer p)){super.releaseUsing(stack,w,user,remaining);return;}var old=SHOT.get();SHOT.set(new Shot(p,stack,p.isShiftKeyDown()&&NetherSkills.ready(p,stack,"infernal_bow")));try{super.releaseUsing(stack,w,user,remaining);}finally{if(old==null)SHOT.remove();else SHOT.set(old);}}
 @Override protected void shoot(ServerLevel server,LivingEntity owner,InteractionHand hand,ItemStack weapon,List<ItemStack> ammo,float velocity,float inaccuracy,boolean critical,LivingEntity target){if(ammo.isEmpty())return;super.shoot(server,owner,hand,weapon,List.of(ammo.getFirst()),velocity,inaccuracy,critical,target);}
 @Override protected int getDurabilityUse(ItemStack ammo){var shot=SHOT.get();return shot!=null&&shot.special?4:1;}
 @Override protected void shootProjectile(LivingEntity owner,Projectile projectile,int index,float velocity,float inaccuracy,float angle,LivingEntity target){super.shootProjectile(owner,projectile,index,velocity,inaccuracy,angle,target);if(!(projectile instanceof AbstractArrow arrow))return;var shot=SHOT.get();boolean special=shot!=null&&shot.owner==owner&&shot.requested&&velocity>=3.599F;float raw=special?13:10*Math.min(1,velocity/3.6F);arrow.getPersistentData().putFloat("InfernalRaw",raw);arrow.getPersistentData().putBoolean("InfernalSkill",special);arrow.setBaseDamage(NetherWeaponRules.bowBase(raw,arrow.getDeltaMovement().length()));if(special&&!shot.committed){shot.special=true;shot.committed=true;NetherSkills.consume(shot.owner,shot.stack,"infernal_bow");}}
 @Override public boolean isValidRepairItem(ItemStack s,ItemStack addition){return NetherWeapons.TIER.getRepairIngredient().test(addition);}
}
