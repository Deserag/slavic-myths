# Арбалет трёх дыханий 1.9.7.5

По запросу пользователя установлен в существующий PolyMC Testing.

Изменения: минимум14 урона стрелы до брони, без снижения более сильных hits;
немедленная слабость40ticks при прямом Море и горение40ticks при Пламени.
Отдельный GorynychImpactMode сохраняет выбранную силу при переключении оружия
между выстрелом и попаданием. Моровое облако80ticks/обычный змеиный арбалет/руны
сохранены. Пламя и Буря: кольца радиус3,40ticks, клиентский лимит32emitters,
24particles каждые4ticks/эмиттер, без server scans или урона от VFX.
Буря: прежние +3HP, SlowII40, knockback,8targets,LOS,30tick target cooldown.
Tooltips RU/EN и генератор описания обновлены. Реальных client visual тестов0.

Проверки: gradlew.bat clean build -I tools/reward-issues.init.gradle runRewardChecks:
PASS,13/13 tests,1 реальный GameTest server. Добавлен native direct status test
(малоскоростная стрела14,Weakness40,ignite40); scoped volley проверяет42 урона
тремя hits и защиту от duplicate/чужого залпа; storm native14+3=17 PASS.
После обновления текста во время первой сборки статический JAR gate обнаружил
несовпадение en_us. Повторный gradlew.bat clean build PASS (FINAL_BUILD.log),
финальная сверка всех source resources/production classes,JSON/PNG и отсутствия
test classes:13731 checks PASS. Новые серверные запуски после resource-only
пересборки не выполнялись. Build предупреждает о прежних deprecated API.

SHA256:75d00b6ee28d2991e2a76175d1d17158955928763e7c46ef6a788480aa59914c.
RESOURCE_JAR_CHECKS.json и POLYMC_INSTALLATION.json содержат gates/квитанцию.
Backup прежнего1.9.7.4 сохранён вне mods; один активный mod JAR,607 остальных
файлов сохранены побайтно, instance name обновлён. Старый1.16.5 и миры не тронуты.
Существовавшие незакоммиченные изменения сохранены и входят в сборку; эта задача
не объявляет завершённой приёмку других систем. Commit/push не выполнялись.

Ручная проверка: все3 режима веер/очередь; кольца Пламени/Бури2s и Мор;
броня/огнестойкость; переключение режима до попадания; multiplayer.
Клиент/PolyMC gameplay/MP/FPS/MSPT NOT_RUN; компиляция и GameTests не заменяют их.
