# PolyMC: единая версия 1.9.7.4

По запросу «обновляй» 14 октября2026 установлен production JAR1.9.7.4
в существующий PolyMC1.21.1 Testing (NeoForge21.1.255,Java21).
База: полный опубликованный snapshot2389b876 версии1.9.7.3, включая
предыдущие оружейные/визуальные исправления, существ, крафтовые и инструментальные руны.

Включены production исправления PR#11/#12/#13/#14 (#3/#6/#4/#5):
урон продолжения залпа, ограниченный поток Морозка,40tick огненный VFX,
crossbow-only Storm3HP/SlowII40/LOS/targetCD30. Это локальная интеграция;
GitHub PR не сливались и Issues не объявлены завершёнными.
Уникальный UUID залпа передаётся и отложенным раундам очереди; основная
современная механика трёх выстрелов/веера/очереди без бутылок сохранена.
Сохранены текущие boss HP1400 Морозка, все модели/текстуры/registry IDs
и исходники прочих систем — проверено по baseline хешам.

Проверки: Java21 clean build PASS; один реальный GameTest server,
12/12 native checks PASS; ToolRunes11731, Craft3518, WeaponFeedback41260,
Night40/10000 simulations/8352UV, Poloz4215 PASS.
21432 source/resource/JAR/JSON/PNG checks PASS, test fixtures отсутствуют
в production JAR, его классы совпали с исполнявшимися server classes.

SHA256:588d1f21841868baedbd71ebaea03dff0d45004213247a24bbb0c4c1e4b68528.
Установка tools/install_reward_polymc.ps1: staged copy/hash, backup,
rollback protection, единственный активный модовый JAR,598 прочих файлов
сохранены побайтно. Миры и старый PolyMC1.16.5 не изменены.
Квитанция: POLYMC_INSTALLATION.json; gates: RESOURCE_JAR_CHECKS.json.

Ранний integration-script encoding error исправлен; исходный baseline
не перезаписывался. Первое offline обращение с неверным task name исправлено,
итоговые OFFLINE_FINAL.log и FINAL_BUILD_RUNTIME.log PASS.
Dev client/PolyMC runtime/connected dedicated/FPS в этой установке NOT_RUN.
Нативные tests не выдаются за эти проверки. Клиентских стартов0,
GameTest server1; предыдущий старт был19:59MSK, этот20:52MSK (>10мин).
Ребаланс#7, зеркало#8, полный количественный аудит#9 и полная приёмка#10
этим обновлением не завершены. Commit/push данной интеграции не выполнялись.
