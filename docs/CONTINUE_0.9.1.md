# Продолжение 0.9.1

Проект D:/slavic-myths; спецификация work/hunt091_reference/slavic_myths_0_9_1_codex_package.
Следующее действие: ручной QA MANUAL_QA_0.9.1.md. Автозапуск Minecraft запрещён.
Сначала проверить PROJECT_STATUS/ROADMAP/ARCHITECTURE и git status; сохранять
все прежние незакоммиченные изменения. Не запускать глобальный генератор
старых ресурсов ради II: использовать tools/hunt_091.py; оружие пользователя
battle_axe/carved_staff/club/retainer_shield не перерисовывать.

Java8: .tools/jdk8/jdk8u504-b01; gradlew.bat --offline clean build verifyHunt verifyKurgan.
Проверки: python -X utf8 tools/verify_hunt_091.py; python -X utf8 tools/verify_resources.py.
Не выдавать NBT/contracts за игровой runtime/перезаход. Копировать только reobf JAR.
PolyMC известен: C:/Users/pavel/AppData/Roaming/PolyMC/instances/Slavic-Myths-Testing/.minecraft/mods.
Старый Slavic JAR архивировать, JEI/Curios/миры сохранить. Commit не делать.
0.9.2 Лихо+Тугарин / 0.9.3 Баба-яга / 0.9.4 сведение только по новому заданию.
