# Текущий исполнительский статус — 30.10.2026

2.2 приостановлена. Продолжается полная реализация353/354 на codex/hell-bosses-completion-20261030; этот checkpoint не является сдачей.

Подключён код: серверные наборы атак/зон/волн; owner-bound retainer; предметы/компоненты; ритуал200тиков/96блоков с durable reservations; unique soul/scroll/pair reward policy; обе руны в штатных сокетах; Curios necklace/belt, активация с руки/настраиваемые Controls keys с проверкой экипировки сервером; cooldown3600 на владельца; рецепты и добавочные guaranteed loot pools. OP summons без легендарной награды. Retainer без лута/XP/водной конверсии/порталов, TTL по абсолютному серверному времени.

Новые раскладки/таблица компонентов разрешены владельцем30.10.2026 и приведены в docs/concepts/nether-bosses/INTEGRATION_2.1X_2026-10-30.md. Это инженерная конкретизация, не восстановленный старый постер.2.1.2 не изменена.

Java21 clean build/resources PASS.6/6 настоящих NeoForge GameTest assertions PASS: HP/NBT, plague modifiers,80 000loot simulations (2босса×4Looting×10000),500реальных chest loot evaluations, native recipe manager, RuneState/admission. Найденная ошибка one-pool-component исправлена: гарантированные2компонента теперь в2отдельных pools.

Остальные критерии ещё в работе: реальные ритуалы/негативные/конкурентные,30+боёв, native survival component acquisition, dedicated/2clients/relog/crash, client model/motion/render QA. Подготовлен branch-only isolated GitHub Xvfb workflow, Windows desktop не используется. Оригинальные постеры/отдельныеSFX физически не подтверждены: BLOCKED_ART_REFERENCE/BLOCKED_AUDIO; vanilla WIP sounds НЕ являются принятым звуком боссов. Полное художественное соответствие не заявляется.

Обе Issue открыты, PR359 не слит, hell/main не изменены текущей разработкой. PolyMC пока не обновлён: обязательный завершающий этап после доступных технических QA; рабочий профиль и миры сохраняются. Следующие проверки выполняются без повторного согласования.
