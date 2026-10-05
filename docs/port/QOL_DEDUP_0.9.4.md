# QoL deduplication 0.9.4

| Feature | Found | Action | Replacement / retained content | Status |
|---|---|---|---|---|
| Generic target name/HP overlay | CombatHud, CombatHudConfig, LoreNetwork.CombatTarget | Removed renderer/config/packet and emissions | Jade 15.10.6; MobMaceCombat retains NPC mace knockback separately | DONE (source removal; gameplay MANUAL) |
| Boss bars and unique HUD | Existing boss entities, RPG/flight UI | Retain and port | No generic replacement of phases, warnings, flight/progression indicators | COMPILES; client MANUAL |
| Item browser | Only existing JEI categories | Retain four categories and optional dependency | JEI 19.51.0.418 | COMPILES; four categories retained; client MANUAL |
| Generic minimap/world map | No implementation found | No deletion | Xaero Minimap / World Map pinned in package | DONE (installed); client MANUAL |
| Generic tree felling | No implementation found | No deletion | FallingTree pinned in package | DONE (installed); client MANUAL |

Client target bookkeeping and CombatTarget packet are gone. CombatHudEvents contained a
unique NPC mace push: it was retained in MobMaceCombat using LivingDamageEvent.Pre.
No items, mobs, boss bars, spells, rituals, loot or structures were removed.
Jade is not a core dependency. Client/gameplay checks remain MANUAL.
