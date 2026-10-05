# Payload port 0.9.4

Four existing messages now use CustomPacketPayload / StreamCodec and RegisterPayloadHandlers, with logical-direction registration. Client GUI/state application is isolated in client-only event handlers; server handlers enqueue main-thread work.

| Payload | Direction | Validation / behavior |
|---|---|---|
| LoreOpen | server → client | mask codec, client lore screen |
| RpgSync | server → client | defensive CompoundTag copies + XP |
| Activate | client → server | alive/non-spectator player, existing ability/cooldown/ready validation |
| FlightInput | client → server | finite floats, alive player, pilot/ownership, server rate limit and controls |

Wire roundtrips, copy isolation, malformed/truncated buffers and NaN/Inf rejection are checked by verifyPortPayloads. Real interactive client handling remains manual. CombatTarget and generic HP overlay were explicitly removed by the finalization package; NPC mace behavior remains in MobMaceCombat.
