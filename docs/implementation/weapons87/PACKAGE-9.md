# Пакет9 — #182, парный силуэт Тугарина

22.10.2026. #87 IN_PROGRESS. #182 OPEN, ручная игровая приёмка ещё не выполнена. Source-проекции ниже не являются скриншотами Minecraft.

Подтверждённый дефект: в root модели два полных диагональных спрайта32x32 натянуты на плоскости7x16 и повёрнуты навстречу. Иконка16px выглядит ломаной дугой; клинки потеряли пропорции. Исправлена только `assets/slavicmyths/models/item/tugarin_sword.json`: два параллельных клинка с canvas16x16, pixel-extrusion краёв, разнесённые по глубине. Исходный PNG32x32 сохранён; новый GUI front-lit. Это исходный принятый рисунок, не новый исторический канон.

Варианты и исходный чертёж до production: `blueprints/tugarin-icon/tugarin_sword.png`, plan/draft/manifest. Варианты: старые сжатые плоскости (подтверждённый дефект), выбранная пара с правильным aspect, одиночный исходный held KEEP. Первый неверный source-probe, ошибочно прочитавший PNG как16px, сохранён отдельно; production использует полные32px и126 элементов:2 поверхности плюс124 набора пограничных граней. Авторские пиксели не менялись.

В руке по-прежнему один исходный `tugarin_sword_held`, второй отображает существующий TugarinPairClient. Его source, held, PNG и серверный TugarinPair побайтно равны архиву `before-tugarin-icon.zip`. Не менялись урон, скорость, NBT, рецепты, ID, босс, анимация и хват. Старые модели Калина и перунитового меча не затрагивались: у Тугарина сохранён прямой узкий тёмный клинок с тёплой внутренней полосой и золотой гардой, у Калина — изгиб, у перунитового — широкий металлический силуэт.

Генератор `tools/weapons87_tugarin_icon.py` подключён последним в канонический pipeline после прежних пакетов. Проверка исторических генераторов выполняется только в изолированной копии ресурсов. Объём этой art Issue не включает ranged timing, эффекты и баланс.

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| Адресный диагноз и варианты до production | before-tugarin-icon.zip, blueprint/plan/draft | PASS |
| GUI1x, front/profile/3-4, толщина и source UV | blueprint, tugarin182-after/tugarin_sword-before-after.png; canvas16x16, pair nearest-depth gap.2 | PASS |
| Пара различима без сжатия; существующий характер KEEP | actual before/after source sheets | PASS |
| PNG/held/renderer/gameplay сохранены | tugarin182-after/preserved-files.json,4 byte-identical files | PASS |
| Обе физические руки, Steve/Alex, first person |18 actual native source matrices:16 custom swing +2 ordinary offhand; tugarin_sword-native-fp.png | PASS |
| Third person/source player scale | тот же held/PNG/TugarinPairClient; package3 fixed native body/hand evidence сохраняет применимость | PASS |
| Java21 clean build, реальные offline fixtures | package9-build.log; test NO-SOURCE, не JUnit PASS | PASS |
| Resources/actual JAR/generator replay | package9-gates.log + package9-receipt.json | PASS |
| Реальные GUI/свет/движение/FP/TP/рамка/drop/MP | запусков0, владелец проверяет | NOT_RUN / OWNER_PENDING |
| Установка PolyMC | промежуточный пакет не установлен | NOT_RUN / POLYMC_NOT_INSTALLED |

Точный JAR, SHA256, размер, source commit и hash полного uncommitted source manifest указаны в package9-receipt.json. Все локальные исходники незакоммичены. Не было commit/push/смены поля Project Status/закрытия Issue. Это не итоговая сборка87.

Владельцу: сравнить пару в инвентаре, на земле и в рамке; проверить оба main arms, Steve/Alex, парные взмахи от первого/третьего лица. Убедиться, что две иконные гарды читаются, а в руках сохранились прежние клинки и хват. Остальная программа продолжается.

Source-аудит15 uniques дополнен правильными реальными override состояниями плети. Номер182 — Тугарин,183 — Соловей; эта связь сверена с actual Issues, исправлена в новой source gallery. Полная ревизия остальных uniques и #192 ещё не закончена.
