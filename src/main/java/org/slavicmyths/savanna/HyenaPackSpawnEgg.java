package org.slavicmyths.savanna;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import org.slavicmyths.registry.ModEntities;

/** One egg, one complete encounter. Failed placement leaves the held egg untouched. */
public final class HyenaPackSpawnEgg extends DeferredSpawnEggItem {
    public HyenaPackSpawnEgg(Properties properties){
        super(ModEntities.BLACK_HYENA,0x202023,0x89765a,properties);
    }
    @Override public Component getName(ItemStack stack){
        return Component.translatableWithFallback(getDescriptionId(),"Яйцо призыва Чёрной гиены и свиты");
    }
    @Override public InteractionResult useOn(UseOnContext context){
        Player player=context.getPlayer();
        if(player==null||player.isSpectator())return InteractionResult.FAIL;
        if(context.getLevel().isClientSide)return InteractionResult.SUCCESS;
        if(!(context.getLevel() instanceof ServerLevel world)||!(player instanceof ServerPlayer server))return InteractionResult.FAIL;
        BlockPos clicked=context.getClickedPos();
        BlockPos pos=clicked.relative(context.getClickedFace());
        if(world.dimension()!=Level.OVERWORLD||!world.hasChunkAt(pos)||!world.mayInteract(player,pos)
            ||!player.mayUseItemAt(pos,context.getClickedFace(),context.getItemInHand())
            ||!world.getBlockState(pos.below()).isFaceSturdy(world,pos.below(),Direction.UP)
            ||!world.getFluidState(pos).isEmpty())return fail(server);
        SavannaAnimal seed=ModEntities.BLACK_HYENA.get().create(world);
        if(seed==null)return fail(server);
        seed.moveTo(pos.getX()+.5,pos.getY(),pos.getZ()+.5,player.getYRot(),0);
        if(!world.noCollision(seed)||!world.addFreshEntity(seed))return fail(server);
        if(!HyenaPacks.get(world).spawn(seed,world,server)){
            seed.discard();return fail(server);
        }
        if(!player.getAbilities().instabuild)context.getItemInHand().shrink(1);
        return InteractionResult.CONSUME;
    }
    @Override public InteractionResultHolder<ItemStack> use(Level world,Player player,InteractionHand hand){
        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }
    private static InteractionResult fail(ServerPlayer player){
        player.displayClientMessage(Component.translatableWithFallback("msg.slavicmyths.hyena_pack_spawn_failed","Не удалось призвать стаю: нужна свободная площадка под открытым небом."),true);
        return InteractionResult.FAIL;
    }
}
