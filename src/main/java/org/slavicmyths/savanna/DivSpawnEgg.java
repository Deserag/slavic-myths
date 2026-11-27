package org.slavicmyths.savanna;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import org.slavicmyths.registry.ModEntities;
public final class DivSpawnEgg extends DeferredSpawnEggItem {
    public DivSpawnEgg(Properties p){super(ModEntities.DIV,0x282333,0x762d43,p);}
    @Override public Component getName(ItemStack stack){return Component.translatableWithFallback(getDescriptionId(),"Яйцо призыва Дива");}
    @Override public InteractionResult useOn(UseOnContext c){Player p=c.getPlayer();if(p==null||p.isSpectator())return InteractionResult.FAIL;if(c.getLevel().isClientSide)return InteractionResult.SUCCESS;if(!(c.getLevel() instanceof ServerLevel w)||!(p instanceof ServerPlayer player))return InteractionResult.FAIL;
        BlockPos at=c.getClickedPos().relative(c.getClickedFace());if(w.getDifficulty()==Difficulty.PEACEFUL||!w.hasChunkAt(at)||!w.mayInteract(p,at)||!p.mayUseItemAt(at,c.getClickedFace(),c.getItemInHand())||!w.getBlockState(at.below()).isFaceSturdy(w,at.below(),Direction.UP)||!w.getFluidState(at).isEmpty())return fail(player);
        Div boss=ModEntities.DIV.get().create(w);if(boss==null)return fail(player);boss.moveTo(at.getX()+.5,at.getY(),at.getZ()+.5,p.getYRot(),0);boss.setHealth(Div.MAX_HP);
        if(!w.noCollision(boss)||!w.addFreshEntity(boss)){boss.discard();return fail(player);}if(!p.getAbilities().instabuild)c.getItemInHand().shrink(1);return InteractionResult.CONSUME;
    }
    @Override public InteractionResultHolder<ItemStack> use(Level w,Player p,InteractionHand hand){return InteractionResultHolder.pass(p.getItemInHand(hand));}
    private static InteractionResult fail(ServerPlayer p){p.displayClientMessage(Component.translatableWithFallback("msg.slavicmyths.div_spawn_failed","Не удалось призвать Дива: нужна свободная площадка и не мирная сложность."),true);return InteractionResult.FAIL;}
}
