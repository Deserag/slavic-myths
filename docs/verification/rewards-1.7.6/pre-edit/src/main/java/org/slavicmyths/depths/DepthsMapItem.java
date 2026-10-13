package org.slavicmyths.depths;

import java.util.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

/** UNBOUND parchment becomes a native FILLED_MAP only after the persistent instance is COMPLETE. */
public final class DepthsMapItem extends Item {
    public DepthsMapItem(Properties properties){super(properties.stacksTo(1));}
    public static UUID operation(ItemStack stack){var n=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();return n.hasUUID("DepthsOperation")?n.getUUID("DepthsOperation"):null;}
    static void pending(ItemStack stack,UUID id){var n=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();n.putUUID("DepthsOperation",id);n.putString("DepthsState","PENDING");stack.set(DataComponents.CUSTOM_DATA,CustomData.of(n));}
    static void unbound(ItemStack stack){var n=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();n.remove("DepthsOperation");n.putString("DepthsState","UNBOUND");stack.set(DataComponents.CUSTOM_DATA,CustomData.of(n));}
    @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand){
        var stack=player.getItemInHand(hand);if(!level.isClientSide&&player instanceof ServerPlayer serverPlayer){
            UUID operation=operation(stack);
            if(operation!=null){
                var world=serverPlayer.server.getLevel(Level.OVERWORLD);var entry=DepthsSavedData.get(world).entries.get(operation);
                if(entry!=null&&entry.status.equals("COMPLETE")){
                    serverPlayer.sendSystemMessage(Component.translatable("depths.slavicmyths.bound"));
                    return InteractionResultHolder.success(DepthsService.boundMap(world,entry));
                }
                if(entry!=null&&!entry.status.equals("PENDING")){serverPlayer.sendSystemMessage(Component.translatable("depths.slavicmyths.failed"));return InteractionResultHolder.fail(stack);}
            }
            DepthsService.start(serverPlayer,stack);
        }
        return InteractionResultHolder.sidedSuccess(stack,level.isClientSide);
    }
    @Override public void appendHoverText(ItemStack stack,TooltipContext context,List<Component> lines,TooltipFlag flag){
        lines.add(Component.translatable(operation(stack)==null?"depths.slavicmyths.map_hint":"depths.slavicmyths.map_pending"));
        lines.add(Component.translatable("depths.slavicmyths.map_radius"));
    }
}
