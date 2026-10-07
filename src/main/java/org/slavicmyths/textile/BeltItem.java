package org.slavicmyths.textile;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
public final class BeltItem extends Item {
    public BeltItem(){super(new Properties().stacksTo(1));}
    @Override public InteractionResultHolder<ItemStack> use(Level l,Player p,InteractionHand hand){
        ItemStack held=p.getItemInHand(hand);
        if(!held.is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM,Textiles.id("accessories/belts"))))return InteractionResultHolder.pass(held);
        if(!l.isClientSide&&!p.isSpectator()){
            if(p.getData(BeltData.BELT).isEmpty()){
                p.setData(BeltData.BELT,held.split(1));p.syncData(BeltData.BELT);BeltData.sound(p);
                p.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.slavicmyths.belt_equipped"),true);
            }else p.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.slavicmyths.belt_occupied"),true);
        }
        return InteractionResultHolder.sidedSuccess(held,l.isClientSide);
    }
}
