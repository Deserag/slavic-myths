# Lamya — IX, first Java integration
- Registry lamya; white tier IX bossbar. Provisional logical 2200 HP (1024 internal), armor 28, three phases at 70/35%.
- Source geometry V5, V6 animation keyframes; actual polygon mesh exported without manual simplification. Server reads separate collision bounds, client reads indexed quantized vertices.
- Fire, solar, wind, claws, sweep, stinger, drought, three combinations and ultimate. Shared targeting, locked target, head stagger, line-of-sight checks, temporary server-owned zones.
- Original procedural OGG cues. No persistent auxiliary entities. On reload attacks and zones are cancelled; cooldowns and home are restored.
- Provisional implementation: drought uses Hunger/Weakness rather than custom status; exact grab and bite sequence, ground-stinger impact, per-head visual stagger, smooth gait transitions, collision optimization, turning and secondary physics need refinement. Spawn/phase/hurt/death are initial keyframes. Arena, survival summoning and final loot remain for owner approval. Real client, performance and MP QA required.
- Debug summon: /summon slavicmyths:lamya
