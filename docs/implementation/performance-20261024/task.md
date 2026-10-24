Прямое поручение владельца 24.10.2026: после интеграции main и установки тестовой 1.9.7.62 оптимизировать код в отдельной ветке.

Первый ограниченный пакет: EarthCamera — убрать временные списки из каждого кадра, сохранить радиус 16, типы, NO_SPECTATORS, формулы/тайминги и отсутствие долгоживущих entity references. Использовать установленный NeoForge API. Другие кандидаты только после аудита; registry IDs, баланс, ресурсы и сохранения не менять. Не удалять код по одному отсутствию текстовых ссылок: существуют reflection/registries/events/mixins.

Проверки: Java21 clean build; production JAR/resources; независимое offline сравнение результата до/после и измерение выделений на native entity query fixture. Minecraft/PolyMC/GameTest/server запрещены. FPS/MSPT и игровая приёмка NOT_RUN. Issue/card остаётся В работе до полного scope, не Готово.

Ветка codex/performance-20261024. Установленный PolyMC содержит main до оптимизации. Отчёт docs/implementation/performance-20261024/REPORT.md.
