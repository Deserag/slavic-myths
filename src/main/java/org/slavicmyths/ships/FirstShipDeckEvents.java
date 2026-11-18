package org.slavicmyths.ships;

/**
 * REMOVED THE PLAYER POSITION TELEPORT TICK ADAPTER.
 *
 * Real Minecraft world blocks provide actual collision, jumping, walking,
 * gravity and ladder climbing. No setPos/onGround hacks are applied to players.
 * Server-controlled whole-block translations are confined to helm commands in
 * FirstShipEntity. The existing rafts use their separate, unchanged logic.
 */
public final class FirstShipDeckEvents {
    private FirstShipDeckEvents() {}
}
