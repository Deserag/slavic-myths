# Навья роща — контракт реализации #251–#257

Прямое поручение 24.10.2026: выполнить весь пакет **одного** биома в существующей ветке `hell`. Следующие биомы, аура, оружие и активируемые руны не входят. Предыдущий пакет генерации #243–#248 остаётся IN_PROGRESS с owner runtime NOT_RUN; его готовность не подразумевается.

## Источники

- Project №9: #251, #252, #253, #254, #255, #256, #257; все семь фактически переведены в «В работе».
- Полные production документы и три SVG в `docs/concepts/navya-grove/` прочитаны. Более поздние HP/атаки в #255/#256 имеют приоритет над ранними открытыми гипотезами общей политики.
- В чате «Обсуждение биома 2.0.2» (6adb602b-2ef8-83ed-9ac4-2b7126b7ab4b) подтверждено одобрение художественного направления. Доступный через read_thread снимок содержит текст и `chatgpt-content-reference`, но **не PNG и не attachments**; эти ссылки не выданы за просмотренный рисунок. После ответа владельца «смотри в задачах» работа выполнена по доступным specs/SVG; точное сравнение с отсутствующими оригинальными PNG остаётся NOT_RUN.
- Блоки и древесина изготавливаются по доступному техническому контракту и SVG. Выгруженные игровые PNG — отдельные реальные файлы, не SVG в качестве готовой текстуры.
- Minecraft 1.21.1, NeoForge 21.1.255, Java21; фактические зависимости из локального Gradle cache.

## Контент и ID

Восемь геологических материалов: `navya_soil`, `mossy_navya_soil`, `navya_stone`, `cracked_navya_stone`, `grove_bone_block`, `grove_bone_column`, `grove_bone_spike`, `rooted_navya_stone`. Сталагмит/сталактит — один ID, `tip_direction=up/down`, согласованная модель и зеркальная коллизия. Камни/кость не излучают свет.

Одна порода `navya`: log/wood/stripped_log/stripped_wood, planks/stairs/slab/fence/gate/door/trapdoor/button/pressure_plate/sign/hanging_sign. Наружная старая кора — `weathered` у того же ID, без второй породы. Блоки используют nonflammable vanilla Crimson properties; дроп не получает привилегию netherite fire resistance. Tab — существующая decor; другие ID и tab IDs сохраняются.

Флора: `navya_dry_branches`, `navya_roots`, `navya_hanging_roots`, `navya_shrub`, `ghost_fungus` (light5), `navya_growth` (light4), возобновляемый `navya_sapling`, редкие настоящие `navya_dead_leaves`. Мох отдельно не дублируется. Настоящая сухая листва нужна установленному FallingTree, но не заполняет крону.

FallingTree config прочитана **без изменения**: WHOLE_TREE, maxSize100, maxScanSize500, min leaves1, allowMixedLogs=false. Поэтому все формы используют связанный скелет одного `navya_log` и редкие LeavesBlock у ветвей; гигант имеет 2×2 низ, затем сужается. Статический предел и связность проверяются отдельно, реальная рубка/безопасность соседних построек NOT_RUN. Внешнюю конфигурацию и старую сосну не изменять.

Существа: `navka`80HP, `rootling`120, `bone_crawler`150, `navya_wisp`20, `grove_guardian`700. Вспомогательные объекты `navya_shot` (ghost/bone/trunk modes), `navya_attack_roots` (warn/rise/hold/recede). Только локальные bounded queries, серверный урон в окне действия, синхронизация action+gameTime при смене состояния. У Сторожа две фазы, 350HP trigger, 20% дополнительная защита второй фазы как технический default из production spec; связь с ареной, максимум2 помеченных помощника без loot/XP, временные корни без block grief.

Начальные damage defaults выведены относительно фактического TugarinCombat (melee12): Навка6 melee/7 shot, Корневик9 melee/6 roots, Ползун10 melee/9 thorn; Сторож14 sweep/18 trunk/10 roots. Это технические параметры для проверки, **не утверждение о пройденном балансном тесте**. Windup12/20/28/48 ticks для соответствующих действий, более длинный sweep босса26; фазаII не убирает предупреждения.

Материалы: `navya_ash`, `living_root`, `bone_thorn`, `grove_core`, `guardian_branch`. Существующие soul/soul_fragment не дублируются. Добавлен `soul_blight`; владелец явно утвердил1–3 fragments, soul2%+1%/Looting, exclusive0.5% blight, own material1–2, boss core1/branches2–5/soul2–3/blight1.

## Порядок и gates

Материалы/флора → мобы → Сторож → **в конце** natural biome/worldgen/spawn/Heart. После изготовления моделей/UV/текстур и утверждения лута подключена финальная source-интеграция #257; runtime и визуальная приёмка остаются NOT_RUN. Отдельные готовые материалы не означают завершения #251 или последующих детей. Каждый scope имеет отдельную матрицу; технические FAIL/BLOCKED/NOT_RUN исключают передачу всей цепочки в «Тестирование».

Minecraft/PolyMC/GameTest/server не запускать. Текущий PolyMC не обновлять. Никаких автоматических commit/push/закрытия Issues, owner acceptance не выдумывать. Реальные скриншоты и прослушивание/механики отмечать NOT_RUN; source/native previews не считать игровым evidence.
