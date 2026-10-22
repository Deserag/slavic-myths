# #87 пакет 21 — #185 / `victor_spear` — FIX

22.10.2026. Production art source, без запуска Minecraft. GitHub Issue **OPEN**, owner runtime **NOT_RUN**.

**BEFORE:** 4 обычных кубоида и узкое копейное острие, слабое отличие от рядового материала.

**AFTER:** 14 элементов: прежнее цельное деревянное древко и центр ладони сохранены, новая расширенная стальная головка, выраженная золотая втулка, симметричные короткие боковые выступы/знаки отличия; длина по Y -5…30.

**Изменённые/проверенные файлы:** `victor_spear.json`; общая `weapon_scythe_surface.png` сохранена. Исходная версия: blob старого JSON `64cede0da5dafce90424d215692afb19a417d7dc`; до/после в `blueprints/uniques-p20-p25/`.

**Объём и границы:** Не трогали бонусы, boss/quest награды, дальность; #115/#117/#165 хват и атаки отдельно..

Технические до-production листы двух геометрических исправлений и GUI-коррекции — `blueprints/uniques-p20-p25/`; там точно сохранён исходный модельный JSON и draft JSON до внесения production. Для KEEP есть индивидуальные source листы front/profile/3⁄4/GUI1x с исходными SHA256 в `unique-source-review/`. Скрипт `tools/weapons87_unique_art.py` детерминированно воспроизводит два новых item JSON, GUI pool и атлас Kalin. CI создаёт source after/before рендеры по фактическим JSON+UV/PNG (не gameplay).

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| source BEFORE и индивидуальная оценка | frozen source + scheme указан выше | PASS source-only |
| Адресный FIX; отсутствие произвольного редизайна | whitelist art paths, draft, canonical art overlay | PASS source+scoped CI |
| Java21 clean build и артефакт JAR | branch GitHub Actions `weapons87-p20-p25-java21.yml` | PASS scoped CI; historical #87 gate BLOCKED (see receipt) |
| Совпадение production и source draft / texture/UV | `tools/weapons87_unique_art.py --check --jar`, исторические generator gates | PASS scoped CI; historical #87 gate BLOCKED (see receipt) |
| 16px GUI, first-person/third-person обе руки/Steve/Alex при ходьбе/атаке | source/cases + обязательная ручная игровая приёмка | NOT_RUN / OWNER_PENDING |
| Minecraft/PolyMC установка, игровой бой, MP | запусков 0, установка 0 | NOT_RUN |

Задача #185 остаётся открытой: auto QA не равна признанию игроком.
