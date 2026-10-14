package org.slavicmyths.gorynych;
import java.util.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
public final class SerpentCrossbow extends CrossbowItem {
 public final boolean special;private ItemStack firing=ItemStack.EMPTY;
 public SerpentCrossbow(boolean special){super(new Item.Properties().durability(special?680:520).rarity(special?Rarity.EPIC:Rarity.RARE));this.special=special;}
 public static CompoundTag state(ItemStack s){return s.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getCompound("Gorynych196");}
 private static void write(ItemStack s,CompoundTag state){var n=s.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();n.put("Gorynych196",state);s.set(DataComponents.CUSTOM_DATA,CustomData.of(n));}
 @Override public int getUseDuration(ItemStack s,LivingEntity e){return super.getUseDuration(s,e)+10;}
 @Override public void onUseTick(Level w,LivingEntity e,ItemStack s,int left){super.onUseTick(w,e,s,left+10);}
 @Override public void releaseUsing(ItemStack s,Level w,LivingEntity e,int left){boolean before=isCharged(s);super.releaseUsing(s,w,e,left+10);if(!special||w.isClientSide||before||!isCharged(s)||!(e instanceof Player p))return;var n=state(s);int mode=Math.floorMod(n.getInt("Mode"),3);String key="Ammo"+mode;if(n.getInt(key)>0)return;for(int i=0;i<p.getInventory().getContainerSize();i++){var source=p.getInventory().getItem(i);if(!source.is(GorynychContent.bottle(mode)))continue;source.shrink(1);n.putInt(key,3);write(s,n);var empty=new ItemStack(Items.GLASS_BOTTLE);if(!p.getInventory().add(empty))p.drop(empty,false);GorynychContent.sound(w,p.blockPosition(),"crossbow_charge");return;}}
 @Override public InteractionResultHolder<ItemStack> use(Level w,Player p,InteractionHand hand){var s=p.getItemInHand(hand);if(special&&p.isShiftKeyDown()){if(!w.isClientSide){var n=state(s);int mode=(Math.floorMod(n.getInt("Mode"),3)+1)%3;n.putInt("Mode",mode);write(s,n);p.displayClientMessage(GorynychContent.text("mode_"+mode,n.getInt("Ammo"+mode)),true);GorynychContent.sound(w,p.blockPosition(),"crossbow_charge");}return InteractionResultHolder.sidedSuccess(s,w.isClientSide);}return super.use(w,p,hand);}
 @Override public void performShooting(Level w,LivingEntity e,InteractionHand hand,ItemStack s,float speed,float spread,LivingEntity target){firing=s;try{super.performShooting(w,e,hand,s,speed,spread,target);}finally{firing=ItemStack.EMPTY;}}
 @Override protected void shootProjectile(LivingEntity owner,Projectile shot,int index,float velocity,float inaccuracy,float angle,LivingEntity target){super.shootProjectile(owner,shot,index,velocity,inaccuracy,angle,target);if(owner.level().isClientSide)return;if(shot instanceof AbstractArrow arrow)arrow.setBaseDamage(arrow.getBaseDamage()*1.18);if(special&&index==0&&!firing.isEmpty()){var n=state(firing);int mode=Math.floorMod(n.getInt("Mode"),3);String key="Ammo"+mode;if(n.getInt(key)>0){n.putInt(key,n.getInt(key)-1);write(firing,n);shot.getPersistentData().putInt("GorynychSpecial",mode+1);GorynychContent.sound(owner.level(),owner.blockPosition(),mode==0?"crossbow_special_mor":mode==1?"crossbow_special_fire":"crossbow_special_storm");}}}
 @Override public void appendHoverText(ItemStack s,TooltipContext c,List<net.minecraft.network.chat.Component> lines,TooltipFlag flag){super.appendHoverText(s,c,lines,flag);lines.add(net.minecraft.network.chat.Component.translatable(getDescriptionId()+".desc"));if(special){var n=state(s);for(int mode=0;mode<3;mode++)lines.add(GorynychContent.text("mode_"+mode,n.getInt("Ammo"+mode)));if(Math.floorMod(n.getInt("Mode"),3)==2)lines.add(GorynychContent.text("storm_stats",StormCrossbow.DAMAGE,StormCrossbow.RADIUS,StormCrossbow.CONTROL_TICKS/20,StormCrossbow.MAX_TARGETS,StormCrossbow.TARGET_COOLDOWN/20.0));}}
}
