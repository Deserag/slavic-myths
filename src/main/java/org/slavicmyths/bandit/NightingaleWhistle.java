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

public final class NightingaleWhistle extends Item {
    private final boolean horn;
    public NightingaleWhistle(Properties p,boolean horn){super(p);this.horn=horn;}
    @Override public int getUseDuration(ItemStack stack){return 24;}
    @Override public UseAction getUseAnimation(ItemStack stack){return UseAction.DRINK;}
    @Override public ActionResult<ItemStack> use(World w,PlayerEntity p,Hand hand){if(p.getCooldowns().isOnCooldown(this))return ActionResult.fail(p.getItemInHand(hand));p.startUsingItem(hand);if(!w.isClientSide&&!horn)w.playSound(null,p.blockPosition(),ModSounds.NIGHTINGALE_INHALE.get(),SoundCategory.PLAYERS,.6F,1.25F);return ActionResult.consume(p.getItemInHand(hand));}
    @Override public ItemStack finishUsingItem(ItemStack stack,World w,LivingEntity entity){if(!w.isClientSide&&entity instanceof PlayerEntity){PlayerEntity p=(PlayerEntity)entity;if(p.getCooldowns().isOnCooldown(this))return stack;
        p.getCooldowns().addCooldown(this,horn?100:600);
        w.playSound(null,p.blockPosition(),horn?ModSounds.NIGHTINGALE_HORN.get():ModSounds.NIGHTINGALE_WHISTLE.get(),SoundCategory.PLAYERS,.9F,horn?1:1.3F);
        if(!horn){net.minecraft.util.math.vector.Vector3d direction=WindAttack.facing(p.yRot);WindAttack.release(p,direction,9,3,1.8F,false,true);WindAttack.terrain(p,direction,9,false);for(int i=1;i<=9;i+=2)WindAttack.particles((ServerWorld)w,p.position().add(0,1,0),direction,i,false);}
    }return stack;}
    @Override public void appendHoverText(ItemStack stack,World w,List<ITextComponent> lines,ITooltipFlag flag){lines.add(new TranslationTextComponent(horn?"tooltip.slavicmyths.bandit_horn":"tooltip.slavicmyths.nightingale_whistle").withStyle(TextFormatting.GRAY));}
}
