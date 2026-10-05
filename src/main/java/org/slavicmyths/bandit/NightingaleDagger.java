package org.slavicmyths.bandit;
import java.util.List;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.sounds.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.network.chat.*;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.TooltipFlag;
import org.slavicmyths.registry.ModSounds;
/** Broad short blade: 7 damage, 2 attacks/sec, 700 uses. Charged gust is utility. */
public final class NightingaleDagger extends SwordItem {
    public NightingaleDagger(Properties p){super(Tiers.IRON,p.durability(700).attributes(SwordItem.createAttributes(Tiers.IRON,4,-2F)));}
    @Override public int getUseDuration(ItemStack s,net.minecraft.world.entity.LivingEntity user){return 72000;}
    @Override public UseAnim getUseAnimation(ItemStack s){return UseAnim.SPEAR;}
    @Override public InteractionResultHolder<ItemStack> use(Level w,Player p,InteractionHand h){if(p.getCooldowns().isOnCooldown(this))return InteractionResultHolder.fail(p.getItemInHand(h));p.startUsingItem(h);return InteractionResultHolder.consume(p.getItemInHand(h));}
    @Override public void releaseUsing(ItemStack stack,Level w,LivingEntity e,int left){if(w.isClientSide||!(e instanceof Player)||getUseDuration(stack,e)-left<18)return;Player p=(Player)e;if(p.getCooldowns().isOnCooldown(this))return;p.getCooldowns().addCooldown(this,140);net.minecraft.world.phys.Vec3 d=WindAttack.facing(p.getYRot());WindAttack.release(p,d,3.5,2,.6F,false,true);WindAttack.particles((ServerLevel)w,p.position().add(0,1,0),d,2,false);w.playSound(null,p.blockPosition(),ModSounds.NIGHTINGALE_WAVE.get(),SoundSource.PLAYERS,.6F,1.3F);stack.hurtAndBreak(2,p,net.minecraft.world.entity.LivingEntity.getSlotForHand(p.getUsedItemHand()));}
    @Override public void appendHoverText(ItemStack s,net.minecraft.world.item.Item.TooltipContext w,List<Component> lines,TooltipFlag flag){lines.add(Component.translatable("tooltip.slavicmyths.nightingale_dagger").withStyle(ChatFormatting.GRAY));}
}
