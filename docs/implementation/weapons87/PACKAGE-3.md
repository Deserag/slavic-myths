# Пакет 3 — первые лица и камера косы

Общая #87 IN_PROGRESS; #165 выполнена лишь в участке исправления player first-person косы. Следом обязательны динамика кистеней #164 и остальные разрешённые дети. Игровой приёмки нет, Issue не закрывались.

## Исправления

- `TugarinPairClient`: собственный RenderHandEvent отменял обычную отрисовку кисти и показывал только оружие. Теперь вызывается существующий `WeaponHands.renderGrip` с настоящими arm/sleeve и skin игрока; временные углы восстанавливаются в finally.
- Wrist/model/arm математика `WeaponHands` и wrist `TugarinPairClient` вынесена в stateless client-only методы. Offline exporter вызывает эти же production helpers и печёт настоящие PlayerModel/UV; мир/существо/клиент не создаются.
- Native source camera показала выход косы на windup за near plane/FOV из-за орбиты70°. Изменена только визуальная орбита до35°; при motion=0 матрица кадра контакта/покоя точно прежняя. `NightSwingPose`, `NightCombat`, урон/фазы/cooldowns/боссовые ресурсы сохранены.
- Обычный offhand косы не проходит через специальный mainhand renderer. Его FP JSON теперь точно совпадает с custom idle для соответствующей физической руки: одинаковые pivot, scale и разворот лезвия наружу. Проверено native ItemTransform matrix equality.

## Source-доказательства

`firstperson-source/`: 19 перспективных листов,592 samples для68 IDs. У обычных предметов — реальные native ItemTransform и basis стандартного idle из установленного ItemInHandRenderer; у четырёх custom IDs — настоящие production helpers. У custom weapon показаны обе руки Steve/Alex, исходные и новые mesh/UV рук, состояния зарядки/снаряда арбалетов, пять кадров всех пяти action/combo косы. Камера фиксированная FOV70/16:9, без bob/equip; это source projections, НЕ скриншоты Minecraft. В архиве `package3-fp-source-evidence.zip` сохранён этот пакет.

`blueprints/firstperson/` содержит предложения до production, старую модель/offhand source view и кадры, подтверждавшие исчезновение на прежней орбите. `fp-gates.json` отдельно проверяет наличие именно оружия, а не только нарисованной руки: во всех новых sampled фазах коса имеет882–2483 opaque item pixels при этой фиксированной камере.

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| Все68 IDs, физические стороны и custom пути определены | fp-inputs.json, fp-native.json, firstperson-source/matrix.json | PASS |
| Настоящие кисти Тугарина вместо отсутствующих | actual PlayerModel/UV capture, Tugarin source A/B | PASS |
| Коса остаётся перед near plane в sampled action frames | production helper/frustum gate, fp-native-check.log | PASS |
| В новых source-кадрах видимо само оружие | fp-gates.json, item_pixels отдельно от руки | PASS |
| Контактная матрица wrist прежняя; серверные кадры/урон не менялись | native contact equality, source-preservation whitelist | PASS |
| Offhand FP совпадает с соответствующим custom idle | native ItemTransform/ScytheGrip equality | PASS |
| Java21 clean build + native attrs/grips/FP | package3-build.log, test task NO-SOURCE указан отдельно | PASS |
| JAR содержит actual JSON и актуальные main classes, без offline exporter classes | package3-receipt.json, package-checks.json | PASS |
| Генераторы воспроизводят текущие ресурсы без отката других | generator-checks.json, отдельная копия src | PASS |
| Реальные GUI/FP/TP/анимации/бой/MP/FPS | запусков Minecraft/PolyMC/server/GameTest:0 | NOT_RUN / OWNER_PENDING |
| Установка текущего промежуточного JAR | не поручалась, не выполнялась | NOT_RUN / POLYMC_NOT_INSTALLED |

Точный JAR/source — `package3-receipt.json`: локальная промежуточная1.9.7.53, SHA256 `bdcf7869debdcd3b99c825642f2b4a952d15ffd47945423017b17c621fba9bf9`,23655980 bytes. Это НЕ установленная версия и НЕ финальная доставка незавершённой #87.
