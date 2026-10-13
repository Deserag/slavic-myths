package org.slavicmyths.textile;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameRules;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.*;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid="slavicmyths")
public final class BeltData {
    public static final DeferredRegister<AttachmentType<?>> TYPES=DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES,"slavicmyths");
    public static final DeferredHolder<AttachmentType<?>,AttachmentType<ItemStack>> BELT=TYPES.register("belt",()->AttachmentType.builder(()->ItemStack.EMPTY).serialize(ItemStack.OPTIONAL_CODEC).copyOnDeath().sync(ItemStack.OPTIONAL_STREAM_CODEC).build());
    public static void sound(Player p){p.level().playSound(null,p.blockPosition(),net.minecraft.sounds.SoundEvents.ARMOR_EQUIP_LEATHER.value(),net.minecraft.sounds.SoundSource.PLAYERS,.6F,1F);}
    public static boolean unequip(Player p,InteractionHand hand){
        if(p.isSpectator()||!p.isAlive()||!p.isShiftKeyDown()||!p.getItemInHand(hand).isEmpty()||p.getData(BELT).isEmpty())return false;
        if(!p.level().isClientSide){ItemStack belt=p.getData(BELT).copy();p.setData(BELT,ItemStack.EMPTY);p.syncData(BELT);if(!p.getInventory().add(belt))p.drop(belt,false);sound(p);}
        return true;
    }
    public static void death(Player p,java.util.Collection<net.minecraft.world.entity.item.ItemEntity> drops){
        if(p.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)||p.getData(BELT).isEmpty())return;
        ItemStack belt=p.getData(BELT).copy();p.setData(BELT,ItemStack.EMPTY);p.syncData(BELT);
        drops.add(new net.minecraft.world.entity.item.ItemEntity(p.level(),p.getX(),p.getY(),p.getZ(),belt));
    }
    @SubscribeEvent public static void block(PlayerInteractEvent.RightClickBlock e){var block=e.getLevel().getBlockState(e.getPos()).getBlock();if(block instanceof org.slavicmyths.furniture.TableBlock||block instanceof org.slavicmyths.block.KitchenTableBlock||block instanceof org.slavicmyths.brewing.BrewBlock||block instanceof org.slavicmyths.storage.StorageBlock)return; // Table sneak interactions own this click.
        if(unequip(e.getEntity(),e.getHand())){e.setCanceled(true);e.setCancellationResult(InteractionResult.SUCCESS);}}
    @SubscribeEvent public static void entity(PlayerInteractEvent.EntityInteract e){if(unequip(e.getEntity(),e.getHand())){e.setCanceled(true);e.setCancellationResult(InteractionResult.SUCCESS);}}
    public record Unequip(boolean offHand) implements CustomPacketPayload {
        public static final Type<Unequip> TYPE=new Type<>(Textiles.id("unequip_belt"));
        public static final StreamCodec<FriendlyByteBuf,Unequip> CODEC=StreamCodec.of((b,v)->b.writeBoolean(v.offHand),b->new Unequip(b.readBoolean()));
        @Override public Type<Unequip> type(){return TYPE;}
    }
    public static void network(net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent e){e.registrar("1.1.4").playToServer(Unequip.TYPE,Unequip.CODEC,(msg,context)->unequip(context.player(),msg.offHand?InteractionHand.OFF_HAND:InteractionHand.MAIN_HAND));}
    private BeltData(){}
}
