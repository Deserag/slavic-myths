# #5 — Буря арбалета

Отдельная ветка main b7616080. Baseline из текущего кода: damage0,
Slowness III20 ticks, knockback~.65 без учёта resistance, одна dust вспышка.

Новая crossbow-only модель: damage3, Slowness II40 ticks, radius3,
до8 ближайших целей с LOS. Повтор того же owner по цели через30 ticks.
Это дополнительный разряд, не stun: AI не отключается. Knockback .65 теперь
использует нативное resistance. Одна ограниченная VFX передача рисует
до8 электрических дуг; vanilla lightning-impact sound, без LightningBolt entity,
поджога блоков/меди/громоотводов. Отдельная руна storm_strike сохраняет
старые damage/control. Мор не менялся.

Damage3 выбран ниже flame splash5, с более долгим полезным контролем.
Повторный overlap трёх стрел не утраивает специальный AoE урон. Native arrow
обрабатывается раньше разряда; иначе его i-frames поглощают дополнительный урон.
Scoped bypass применяется только к дополнительному damage3 с per-target CD,
не меняет глобальные i-frames. Shift+ПКМ работает и на заряженном арбалете.
Tooltip берёт реальные числа из StormCrossbow.

GameTest19:48 MSK:3/3 PASS. Native6+storm3: HP100→91, специальный marker
потребляется однажды. Три цели100→97 каждая, повторный burst без damage,
SlowII40. Solid wall блокирует damage/control. Java21 clean build PASS.

Визуал/звук/сетевая доставка клиенту PENDING. Бронированные и boss comparisons
будут дополнены общим аудитом #9. Issue не закрывается до полной приёмки.
Legacy main использует бутылки; последняя локальная механика пользователя
без бутылок должна быть сохранена при #10, а не возвращена этим PR.
Реальные запуски этапа:1 GameTest server/0 client, общий счёт5 servers/0 clients.
