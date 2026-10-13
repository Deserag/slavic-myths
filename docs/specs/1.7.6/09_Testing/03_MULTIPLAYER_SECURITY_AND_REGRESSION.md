# State robustness and exploit checklist

- Server-only boss reward and damage modifiers, authoritative UUID checks. Client particles only after server confirms proc.
- Idempotent claim storage before/simultaneously with delivery to prevent duplicate grants on logout. Recover incomplete delivery safely.
- No cooldown reset on equipment swap, backpack transfer, death, relog, dimension change or when action reaches another client.
- Do not multiply reflected damage with ring/pursuit/necklace, or recursively trigger new reflection/dodge from synthetic event.
- Inventory full: do not drop claim into other player pickup radius. No "mark claimed then item lost" on exception.
- Accessories: one per actual supported slot; no ghost attributes after unequip.
- Boss tags for Kartaus mantle must be real server-created minions; creative eggs, custom renaming and spawner-sourced mobs cannot masquerade as boss summons.
- Prev version: check Ilya shield raise, Dobrynya spear pose, Alyosha dodge, Finist falcon, old rune smith, quest records, boss summon mechanics unaffected.
- Test 2 clients and a dedicated server if available. On a single-client environment, do not claim multiplayer passed without tests.
