# Продолжение после 0.9.0

Проект D:\slavic-myths, Forge 36.2.42 / Minecraft 1.16.5 / Java8. Читайте статус,
roadmap/architecture и актуальный README. Игру автоматически не запускать.
Не начинать 0.9.1–0.9.4 без нового ТЗ. Не commit, не сбрасывать пользовательские
изменения четырёх оружий. Глобальный старый resource generator не запускать:
для новой прослойки только `python tools/hunt_090.py`.

Новые классы hunt/, client/HuntModel/HuntRenderer/HuntAccessoryRenderer/PouchScreen.
Обновлены старые OvinnikEntity/Model/Renderer, LandSpiritEntity scheduler gate,
HomesteadFeature (убран гарантированный новый овинный boss), FolkAccessoryItem,
SilverCombat, registries/client/common setup. Existing registry IDs сохранены.

Проверки Java8 `gradlew.bat --offline clean build verifyHunt verifyKurgan`,
`python tools/verify_hunt_090.py`, `python tools/verify_resources.py`.
Runtime TODO: MANUAL_QA_0.9.0.md. Headless не подтверждает AI, GUI/duplication,
реальный перезаход, JEI/runtime аксессуаров или MSPT. Minecraft запусков 0.

JAR build/libs/slavicmyths-0.9.0.jar; отчёты docs/verification/headless-0.9.0.json
и install-0.9.0.json. PolyMC Slavic-Myths-Testing в %APPDATA%/PolyMC/instances.
Старый JAR архивировать, оставлять один активный Slavic JAR, JEI/Curios не менять.
Рог cooldown сохранён по owner UUID, не через vanilla cooldown (иначе нельзя
переключать цель и показать конкретный отказ); missing loaded UUID = ACTIVE.
При death не удалять entity преждевременно: vanilla death tick должен выдать XP.
