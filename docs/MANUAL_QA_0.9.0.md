# Ручной QA 0.9.0 — TODO

Minecraft запусков 0. Проверки выполнять в отдельном мире/копии сохранения.
Headless тесты не подтверждают игровой бой, GUI, JEI или реальный перезаход.

# 29. MANUAL TEST CHECKLIST FOR README

Include:

1. `/give @s slavicmyths:ohotnichiy_rog`
2. Test target switching.
3. Test invalid daytime summon.
4. Test Ovinnik environment validation.
5. Successfully summon Ovinnik.
6. Check all four combat behaviors:
   - melee
   - slam
   - ash burst
   - chase/trail/projectile anti-kite
7. Put Ovinnik in water.
8. Test rain.
9. Kill and verify exact loot.
10. Test Volkolak summon in allowed biome at night.
11. Check pounce.
12. Check combo.
13. Check howl.
14. Check side movement.
15. Hit with non-silver weapon.
16. Hit with silver weapon and confirm bonus.
17. Check drops.
18. Craft/equip Ash Charm.
19. Verify fire damage reduction.
20. Craft/equip Wolf Belt and compare day/night.
21. Test Hunter Charm.
22. Open Trophy Pouch.
23. Insert allowed item.
24. Attempt invalid item.
25. Drop/re-pick pouch and confirm contents.
26. Verify Ritual Vessel does nothing except exist as intended.
27. Verify spawn eggs.

---


Дополнительно:
- [ ] Повторный рог/смена стека/смерть/перезаход не обходят ACTIVE и cooldown.
- [ ] Выгрузка чанка не создаёт вторую охоту. Admin clear удаляет старого
      участника после загрузки и не даёт добычу; естественная смерть даёт 50 XP.
- [ ] Шеститиковое комбо действительно наносит 4/4/7 при попадании всех ударов;
      уход из range/за стену или щит позволяют промахнуться/защититься.
- [ ] Нет terrain grief/реальных fire blocks и атак через сплошную стену.
- [ ] Пояс/оберег видны в Curios, дубликаты не складываются, эффект отсутствует
      в обычном инвентаре, transient modifier снят после unequip/relog.
- [ ] Pouch: shift-click, drag, number keys, offhand swap, close/death и drop/
      pickup не дублируют/теряют содержимое; creative clone независим.
- [ ] Пять рецептов видны в штатном crafting JEI; loot-only не имеют рецептов.
- [ ] Все четыре advancements выполняются; silver_remedy требует серебряный
      последний удар.
- [ ] Старый Овинник/старые миры/курганы сохраняются, новый Овинник имеет 175 HP.
- [ ] Отдельно измерить TPS/MSPT и проверить слышимость рога/эмиссию в игре.
