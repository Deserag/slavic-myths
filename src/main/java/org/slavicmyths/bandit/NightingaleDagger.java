package org.slavicmyths.bandit;
import java.util.List;
import net.minecraft.item.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.util.*;
import net.minecraft.util.text.*;
import net.minecraft.client.util.ITooltipFlag;
import org.slavicmyths.registry.ModSounds;
/** Broad short blade: 7 damage, 2 attacks/sec, 700 uses. Charged gust is utility. */
public final class NightingaleDagger extends SwordItem {
    public NightingaleDagger(Properties p){super(ItemTier.IRON,4,-2F,p.durability(700));}
    @Override public int getUseDuration(ItemStack s){return 72000;}
    @Override public UseAction getUseAnimation(ItemStack s){return UseAction.SPEAR;}
    @Override public ActionResult<ItemStack> use(World w,PlayerEntity p,Hand h){if(p.getCooldowns().isOnCooldown(this))return ActionResult.fail(p.getItemInHand(h));p.startUsingItem(h);return ActionResult.consume(p.getItemInHand(h));}
    @Override public void releaseUsing(ItemStack stack,World w,LivingEntity e,int left){if(w.isClientSide||!(e instanceof PlayerEntity)||getUseDuration(stack)-left<18)return;PlayerEntity p=(PlayerEntity)e;if(p.getCooldowns().isOnCooldown(this))return;p.getCooldowns().addCooldown(this,140);net.minecraft.util.math.vector.Vector3d d=WindAttack.facing(p.yRot);WindAttack.release(p,d,3.5,2,.6F,false,true);WindAttack.particles((ServerWorld)w,p.position().add(0,1,0),d,2,false);w.playSound(null,p.blockPosition(),ModSounds.NIGHTINGALE_WAVE.get(),SoundCategory.PLAYERS,.6F,1.3F);stack.hurtAndBreak(2,p,u->u.broadcastBreakEvent(p.getUsedItemHand()));}
    @Override public void appendHoverText(ItemStack s,World w,List<ITextComponent> lines,ITooltipFlag flag){lines.add(new TranslationTextComponent("tooltip.slavicmyths.nightingale_dagger").withStyle(TextFormatting.GRAY));}
}
