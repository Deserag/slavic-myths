# #87 — пакет №27 / #160: исправление Thunder Axe бонуса после попадания

**Дефект (актуальный main до изменения):** `src/main/java/org/slavicmyths/item/ThunderAxeItem.java` (Git blob `b0bc4bbba52122eabbe80d21e0c71688fae6e18b`) делает `target.hurt(target.damageSources().magic(), 1.0F)` из `hurtEnemy` после vanilla attack. Цель уже может иметь `invulnerableTime`, поэтому отдельные 1 HP могут игнорироваться, а particles/sound/cooldown всё равно происходят. Структура исходника сохранена буквально в `P27-THUNDER-AXE-BEFORE.java.txt`.

**FIX:** в том же классе `ThunderAxeItem`, без изменения регистрации, перенесён серверный proc на `@SubscribeEvent LivingDamageEvent.Pre`. Это точка до вычитания здоровья и после брони/обычных модификаторов в NeoForge 1.21.1; при положительном принятом уроне `event.setNewDamage(event.getNewDamage()+1.0F)` даёт бонус поверх финального удара без второго `hurt()` и без сброса invulnerability. Фильтр строго vanilla `DamageTypes.PLAYER_ATTACK`, `ServerPlayer` как прямой атакующий, `getMainHandItem()==THUNDER_AXE`, cooldown отсутствует, roll<0.2. Затем один ENCHANT burst (8), TRIDENT_THUNDER sound и прежний cooldown60 тиков. `hurtEnemy` вызывает только `super` — обычный износ, зачарования и другие эффекты vanilla не изменяются.

| ACTUAL / TEST CASE | EXPECTED / GUARD | EVIDENCE | STATUS |
|---|---|---|---|
| успешный melee, d>0, roll<0.2, CD 0 | итоговый урон d+1 до absorption; ровно один звук/particles/cooldown60 | `LivingDamageEvent.Pre` + exact 0.2/1.0/60 regression checks | PASS source; runtime NOT_RUN |
| vanilla attack blocked/0 damage, cooldown, luck miss | никакого proc / sound / cooldown | guards `getNewDamage()<=0`, cooldown, chance | PASS source; runtime NOT_RUN |
| не-player или иное оружие/снаряд | не добавлять урон | tag PLAYER_ATTACK, ServerPlayer/direct, actual THUNDER_AXE mainhand | PASS source; runtime NOT_RUN |
| iFrames после первого удара | **нет второго `hurt()`**, прибавка к тому же подтверждённому хиту | source regression; проверять в игре | PASS source; runtime NOT_RUN |
| броня/щит/поглощение/криты/PvP/MP/уникальные mod targets | не обходить vanilla pipeline/дружественные ограничения, сохранять взаимодействия | Native Minecraft tests НЕ запускались | NOT_RUN / OWNER_PENDING |
| Java21 build/production JAR | NeoForge 21.1.255 + assembled class/resource | CI | PENDING_CI |

**Граница:** дополнительные 1 HP теперь прибавляются после редукций брони, но до absorption, что соответствует исходной идее отдельного magic hit (не шансовому увеличению физического исходного raw урона до брони). В случае 100% поглощения бонус может быть поглощён вместе с ударом — это допустимая особенность vanilla damage pipeline, подлежит игровой проверке. Не переписывать другие эффекты #160 и VFX #161. Урон топора 8.5, скорость 0.9 и другие утверждённые балансы сохранены.

**Игровая проверка владельца:** в Survival (результат ремонта НЕ доказан игровым тестом) несколько попаданий по zombie/броня20; со щитом, absorb, PvP, sweep; наблюдать совпадение вспышки и ровно 1 HP дополнительно, отсутствие на CD. Minecraft/PolyMC NOT_RUN.
