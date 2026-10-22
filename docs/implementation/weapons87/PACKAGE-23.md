# #87 пакет 23 — #187 / `thunder_axe` — KEEP

22.10.2026. Production art source, без запуска Minecraft. GitHub Issue **OPEN**, owner runtime **NOT_RUN**.

**BEFORE:** 24 отдельных source cuboids, тяжёлая асимметричная головка и коричневое древко, реальный профиль 13.5×26×2.7.

**AFTER:** Геометрия/UV/16px PNG сохранились: массивный рабочий клин, стальное крепление, неброская отделка рукояти, обе руки явно в `display`.

**Изменённые/проверенные файлы:** `thunder_axe.json` + `textures/item/thunder_axe.png`, sheet `unique-source-review/thunder_axe.png`. Исходная версия: model blob `b4c980a81fd2b1dd978e8ef85fdbee0c4b75a87e`; исходный manifest с SHA256.

**Объём и границы:** Проблема #160 с временем вторичного урона топора известна отдельно, не менять механику в художественном пакете..

Технические до-production листы двух геометрических исправлений и GUI-коррекции — `blueprints/uniques-p20-p25/`; там точно сохранён исходный модельный JSON и draft JSON до внесения production. Для KEEP есть индивидуальные source листы front/profile/3⁄4/GUI1x с исходными SHA256 в `unique-source-review/`. Скрипт `tools/weapons87_unique_art.py` детерминированно воспроизводит два новых item JSON, GUI pool и атлас Kalin. CI создаёт source after/before рендеры по фактическим JSON+UV/PNG (не gameplay).

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| source BEFORE и индивидуальная оценка | frozen source + scheme указан выше | PASS source-only |
| Адресный KEEP; отсутствие произвольного редизайна | whitelist art paths, draft, canonical art overlay | PASS source+scoped CI |
| Java21 clean build и артефакт JAR | branch GitHub Actions `weapons87-p20-p25-java21.yml` | PASS scoped CI; historical #87 gate BLOCKED (see receipt) |
| Совпадение production и source draft / texture/UV | `tools/weapons87_unique_art.py --check --jar`, исторические generator gates | PASS scoped CI; historical #87 gate BLOCKED (see receipt) |
| 16px GUI, first-person/third-person обе руки/Steve/Alex при ходьбе/атаке | source/cases + обязательная ручная игровая приёмка | NOT_RUN / OWNER_PENDING |
| Minecraft/PolyMC установка, игровой бой, MP | запусков 0, установка 0 | NOT_RUN |

Задача #187 остаётся открытой: auto QA не равна признанию игроком.
