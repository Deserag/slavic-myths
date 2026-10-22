# #87 пакет 22 — #186 / `finist_bow` — KEEP

22.10.2026. Production art source, без запуска Minecraft. GitHub Issue **OPEN**, owner runtime **NOT_RUN**.

**BEFORE:** Ветвистый волшебный лук в индивидуальном PNG и 2D vanilla bow item-model.

**AFTER:** Не перерисовывался: узнаваемая арка, белая тетива, три настоящие модели натяжения `finist_bow_pulling_0..2` и `overrides` на 0/.65/.9.

**Изменённые/проверенные файлы:** `finist_bow.json`, три `finist_bow_pulling_*.json`, четыре соответствующих спрайта; sha и source render: `unique-source-review/`. Исходная версия: root JSON blob `8392dd4e20d3a13148cadf26210ba519c428dee4`; 16×16 GUI; vanilla bow controls two hands.

**Объём и границы:** Колчан Финита (#148) не изменялся. Камера и скорость натяжения целого #163 требуют игровой проверки..

Технические до-production листы двух геометрических исправлений и GUI-коррекции — `blueprints/uniques-p20-p25/`; там точно сохранён исходный модельный JSON и draft JSON до внесения production. Для KEEP есть индивидуальные source листы front/profile/3⁄4/GUI1x с исходными SHA256 в `unique-source-review/`. Скрипт `tools/weapons87_unique_art.py` детерминированно воспроизводит два новых item JSON, GUI pool и атлас Kalin. CI создаёт source after/before рендеры по фактическим JSON+UV/PNG (не gameplay).

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| source BEFORE и индивидуальная оценка | frozen source + scheme указан выше | PASS source-only |
| Адресный KEEP; отсутствие произвольного редизайна | whitelist art paths, draft, canonical art overlay | PASS source+scoped CI |
| Java21 clean build и артефакт JAR | branch GitHub Actions `weapons87-p20-p25-java21.yml` | PASS scoped CI; historical #87 gate BLOCKED (see receipt) |
| Совпадение production и source draft / texture/UV | `tools/weapons87_unique_art.py --check --jar`, исторические generator gates | PASS scoped CI; historical #87 gate BLOCKED (see receipt) |
| 16px GUI, first-person/third-person обе руки/Steve/Alex при ходьбе/атаке | source/cases + обязательная ручная игровая приёмка | NOT_RUN / OWNER_PENDING |
| Minecraft/PolyMC установка, игровой бой, MP | запусков 0, установка 0 | NOT_RUN |

Задача #186 остаётся открытой: auto QA не равна признанию игроком.
