# #87 пакет 20 — #184 / `trophy_kalin_sabre` — FIX

22.10.2026. Production art source, без запуска Minecraft. GitHub Issue **OPEN**, owner runtime **NOT_RUN**.

**BEFORE:** 6 старых серых ступенчатых элементов, визуально почти прямой клинок; использовался общий `weapon_scythe_surface.png`.

**AFTER:** 34 элемента: сегментированный выгнутый клинок с изолированным 16×16 атласом калинской палитры, односторонней золотой кромкой, поперечной гардой и красной кисточкой; GUI центрирован.

**Изменённые/проверенные файлы:** `trophy_kalin_sabre.json` + `textures/item/kalin_sabre_87.png`. Исходная версия: blob старого JSON `8eb7320b547c6ba3851c9e3bb3b92650617974c5`; исходный источник `before-trophy_kalin_sabre.json`, draft и сравнение в папке `blueprints/uniques-p20-p25/`.

**Объём и границы:** Старый общий `weapon_scythe_surface.png` не изменялся. Историческая спецификация также упоминает отдельную 64×64 иконку; эта поставка остаётся 3D-формой GUI с атласом16×16; окончательное художественное решение владельца ещё нужно..

Технические до-production листы двух геометрических исправлений и GUI-коррекции — `blueprints/uniques-p20-p25/`; там точно сохранён исходный модельный JSON и draft JSON до внесения production. Для KEEP есть индивидуальные source листы front/profile/3⁄4/GUI1x с исходными SHA256 в `unique-source-review/`. Скрипт `tools/weapons87_unique_art.py` детерминированно воспроизводит два новых item JSON, GUI pool и атлас Kalin. CI создаёт source after/before рендеры по фактическим JSON+UV/PNG (не gameplay).

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| source BEFORE и индивидуальная оценка | frozen source + scheme указан выше | PASS source-only |
| Адресный FIX; отсутствие произвольного редизайна | whitelist art paths, draft, canonical art overlay | PASS source+scoped CI |
| Java21 clean build и артефакт JAR | branch GitHub Actions `weapons87-p20-p25-java21.yml` | PASS scoped CI; historical #87 gate BLOCKED (see receipt) |
| Совпадение production и source draft / texture/UV | `tools/weapons87_unique_art.py --check --jar`, исторические generator gates | PASS scoped CI; historical #87 gate BLOCKED (see receipt) |
| 16px GUI, first-person/third-person обе руки/Steve/Alex при ходьбе/атаке | source/cases + обязательная ручная игровая приёмка | NOT_RUN / OWNER_PENDING |
| Minecraft/PolyMC установка, игровой бой, MP | запусков 0, установка 0 | NOT_RUN |

Задача #184 остаётся открытой: auto QA не равна признанию игроком.
