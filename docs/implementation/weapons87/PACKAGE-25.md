# #87 пакет 25 — #189 / `pool_spear` — FIX GUI ONLY

22.10.2026. Production art source, без запуска Minecraft. GitHub Issue **OPEN**, owner runtime **NOT_RUN**.

**BEFORE:** 14 исходных 3D элементов, но GUI yaw135 и отсутствие переноса показывали почти одну светлую часть вместо древка/морских вставок.

**AFTER:** 14 cuboids и все vanilla block-textures сохранены, только `display.gui`: rotation[15,20,-30], translation[-0.22082777,-0.41396517,-0.02771159], scale0.47 и `gui_light:front`; рассчитано для полной 32-unit формы.

**Изменённые/проверенные файлы:** `pool_spear.json`; перед/после в `blueprints/uniques-p20-p25/`. Исходная версия: старый JSON blob `2b65672f01d1c9ac5a0f28c6cbf841d432cff9af`; план `pool-gui-plan.json`; источник `unique-source-review/pool_spear.png`.

**Объём и границы:** Рукоять, водная палитра, статусы, урон и анимации KEEP. GUI полноценно принять только по реальной игре..

Технические до-production листы двух геометрических исправлений и GUI-коррекции — `blueprints/uniques-p20-p25/`; там точно сохранён исходный модельный JSON и draft JSON до внесения production. Для KEEP есть индивидуальные source листы front/profile/3⁄4/GUI1x с исходными SHA256 в `unique-source-review/`. Скрипт `tools/weapons87_unique_art.py` детерминированно воспроизводит два новых item JSON, GUI pool и атлас Kalin. CI создаёт source after/before рендеры по фактическим JSON+UV/PNG (не gameplay).

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| source BEFORE и индивидуальная оценка | frozen source + scheme указан выше | PASS source-only |
| Адресный FIX GUI ONLY; отсутствие произвольного редизайна | whitelist art paths, draft, canonical art overlay | PASS source-only / CI pending |
| Java21 clean build и артефакт JAR | branch GitHub Actions `weapons87-p20-p25-java21.yml` | PENDING_CI |
| Совпадение production и source draft / texture/UV | `tools/weapons87_unique_art.py --check --jar`, исторические generator gates | PENDING_CI |
| 16px GUI, first-person/third-person обе руки/Steve/Alex при ходьбе/атаке | source/cases + обязательная ручная игровая приёмка | NOT_RUN / OWNER_PENDING |
| Minecraft/PolyMC установка, игровой бой, MP | запусков 0, установка 0 | NOT_RUN |

Задача #189 остаётся открытой: auto QA не равна признанию игроком.
