# Закрытие программных долгов #324/#328/#325

## Руда

NetherOreFeature: подземные Y сохраняют индивидуальные диапазоны; открытый проход теперь просматривает весь фактический полезный minY+5..maxY−6. Открытый алатырит1–2 вместо1. Сера: базовые2–4 поверхностные попытки, thermal proposal до3–6, вхолодную выборка с вероятностью2/3 возвращает исходное математическое ожидание. Возле natural magma/lava в радиусе3 в том же новом чанке все thermal attempts допускаются, то есть ×1,5. Это множитель попыток, не обещание ×1,5 реально добытых блоков. Максимум343 локальных чтения на sulfur candidate, без chunk loads; структуры и кратерная арена/мосты пропускаются.

Native5seed ×100000 acceptance: hot/cold ratio1,498..1,506; surface thermal attempts3..6. Реальные5seed×4×256chunks, влияние на vanilla ores/доступность НЕ измерены. Fortune native formula10k/material/level повторена, loot alternatives сохраняют один Silk/raw путь. Существующие8 smelting/blasting рецептов не удалены. Read-only OP diagnostic `/slavicmyths_ores_count` считает только один уже загруженный чанк, не создаёт блоки/чанки. В подсчёте нет доказательства природного происхождения уже поставленного игроком блока.

## Двадцать существ

NETHER_CURRENT_ACTION_BUDGETS.csv:52 действия, current raw burst upper и DoT upper, окна, радиус, ограничения и source SHA. Отличены emitted attempts от принятого урона:5 spikes не равны гарантированным50 HP, двойной bolt ограничен native iframes,3 eruption pulses делят struck UUID, poison не убивает ниже1HP. master CREATURES_2.0.csv содержит140 строк с20 новыми сущностями, источниками spawn/loot/model/physiology, текущими базами и отдельными предложенными уровнями. Historical CSV SHA11dfa926… сохранён. Уровни IV–VI/A1–A7 не применялись; это SOURCE_BUDGET, не замер DPS/TTK.

## Оберег

Новый `nether_aura_amulet` — независимый necklace Curios; действует только в активном слоте, не в сумке/руке. Использует существующий FolkAccessoryItem/PassiveCharm slot lookup. Existing aura исключает надевшего из своего нарастания/урона и очищается на следующем существующем20t refresh; MobEffect Applicable также отклоняет этот единственный effect. После снятия прежняя аура начинает прежний отсчёт. Нет дополнительных огнестойкости, нового disease или механик Двух зим.

Инженерный рецепт: string сверху;2 alatyr ingots по бокам roasted_runnik; roasted_alatyrite снизу.1 шт. Экономика/слот BALANCE_OWNER_PENDING. `amulet_two_winters` registry/recipe/passives/активные режимы сохранены.

## Второй проход оружия

Добавлен proc Weakness также на обычном melee ножом; ограниченные4/6 fire particles лука; GreatswordClient вызывает native first-person arm hook для совместимости с armor overlays. Это найденные при повторном source review недоработки, не заявления об игровом тестировании. Native/reflection regression и ресурсы — debts-build.txt/debts-resources.json. StormStaff изменён только stale test48/42 и его текст отчёта; production-константы не менялись.

Все реальные бои, станция, XP, рост руды, Curios equip/relog/death/MP: NOT_RUN. ART originals BLOCKED_REFERENCE. OWNER_PENDING.
