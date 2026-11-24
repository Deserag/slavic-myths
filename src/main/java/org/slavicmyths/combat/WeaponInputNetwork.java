package org.slavicmyths.combat;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/** Client supplies an input slot, never an item, a target, or a damage amount. */
@EventBusSubscriber(modid = "slavicmyths", bus = EventBusSubscriber.Bus.MOD)
public final class WeaponInputNetwork {
    public record Input(int action) implements CustomPacketPayload {
        public static final Type<Input> TYPE = new Type<>(
                ResourceLocation.fromNamespaceAndPath("slavicmyths", "weapon_input"));
        public static final StreamCodec<FriendlyByteBuf, Input> CODEC =
                StreamCodec.of((buf, value) -> buf.writeVarInt(value.action()),
                               buf -> new Input(buf.readVarInt()));
        @Override public Type<Input> type() { return TYPE; }
    }

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(Input.TYPE, Input.CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (!(context.player() instanceof ServerPlayer player)
                            || !player.isAlive() || player.isSpectator()) return;
                    WeaponInputHandler.Action action =
                            WeaponInputHandler.Action.fromId(payload.action());
                    if (action == null) return;
                    ItemStack held = player.getMainHandItem();
                    if (held.isEmpty() || !(held.getItem() instanceof WeaponInputHandler handler)) return;
                    // The client router is not a security boundary. A forged packet
                    // must not activate a slot that this weapon never intercepts.
                    // Context-sensitive weapons (e.g. a staff bolt only in air)
                    // may authorize the same slot for just one hit-result type.
                    if (!handler.intercepts(action, net.minecraft.world.phys.HitResult.Type.MISS)
                            && !handler.intercepts(action, net.minecraft.world.phys.HitResult.Type.BLOCK)
                            && !handler.intercepts(action, net.minecraft.world.phys.HitResult.Type.ENTITY)) return;
                    // Bound packet rate on the server as well as per-skill cooldowns.
                    // This is input throttling, not a change to any weapon's cooldown.
                    if (!org.slavicmyths.rpg.PathData.ready(player, "weapon_input_request_"+action.id(), 2)) return;
                    handler.onWeaponAction(player, held, action);
                }));
    }

    private WeaponInputNetwork() {}
}
