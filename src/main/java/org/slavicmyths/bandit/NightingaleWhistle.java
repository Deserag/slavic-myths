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

public final class NightingaleWhistle extends Item {
    private final boolean horn;
    public NightingaleWhistle(Properties p,boolean horn){super(p);this.horn=horn;}
    @Override public int getUseDuration(ItemStack stack,net.minecraft.world.entity.LivingEntity user){return 24;}
    @Override public UseAnim getUseAnimation(ItemStack stack){return UseAnim.DRINK;}
    @Override public InteractionResultHolder<ItemStack> use(Level w,Player p,InteractionHand hand){if(p.getCooldowns().isOnCooldown(this))return InteractionResultHolder.fail(p.getItemInHand(hand));p.startUsingItem(hand);if(!w.isClientSide&&!horn)w.playSound(null,p.blockPosition(),ModSounds.NIGHTINGALE_INHALE.get(),SoundSource.PLAYERS,.6F,1.25F);return InteractionResultHolder.consume(p.getItemInHand(hand));}
    @Override public ItemStack finishUsingItem(ItemStack stack,Level w,LivingEntity entity){if(!w.isClientSide&&entity instanceof Player){Player p=(Player)entity;if(p.getCooldowns().isOnCooldown(this))return stack;
        p.getCooldowns().addCooldown(this,horn?100:600);
        w.playSound(null,p.blockPosition(),horn?ModSounds.NIGHTINGALE_HORN.get():ModSounds.NIGHTINGALE_WHISTLE.get(),SoundSource.PLAYERS,.9F,horn?1:1.3F);
        if(!horn){net.minecraft.world.phys.Vec3 direction=WindAttack.facing(p.getYRot());WindAttack.release(p,direction,9,3,1.8F,false,true);WindAttack.terrain(p,direction,9,false);for(int i=1;i<=9;i+=2)WindAttack.particles((ServerLevel)w,p.position().add(0,1,0),direction,i,false);}
    }return stack;}
    @Override public void appendHoverText(ItemStack stack,net.minecraft.world.item.Item.TooltipContext w,List<Component> lines,TooltipFlag flag){lines.add(Component.translatable(horn?"tooltip.slavicmyths.bandit_horn":"tooltip.slavicmyths.nightingale_whistle").withStyle(ChatFormatting.GRAY));}
}
