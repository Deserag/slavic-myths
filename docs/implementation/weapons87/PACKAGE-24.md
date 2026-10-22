# #87 пакет 24 — #188 / `thunder_spear` — KEEP

22.10.2026. Production art source, без запуска Minecraft. GitHub Issue **OPEN**, owner runtime **NOT_RUN**.

**BEFORE:** 30 отдельных source cuboids, прямое древко длиной40, стальной узкий колющий наконечник и мелкая отделка, профиль4×40×2.3.

**AFTER:** Геометрия/UV/GUI source и обе руки оставлены без изменений; оригинальный силуэт копья сохраняется и не повторяет громовой топор.

**Изменённые/проверенные файлы:** `thunder_spear.json` + `textures/item/thunder_spear.png`, sheet `unique-source-review/thunder_spear.png`. Исходная версия: model blob `d6a53369eef5fe763126fb16cc97657866c0a5e6`; SHA256 в исходном manifest.

**Объём и границы:** Не менять дальность (#159 пакет C), урон молний #160 или анимации #165 без отдельного решения..

Технические до-production листы двух геометрических исправлений и GUI-коррекции — `blueprints/uniques-p20-p25/`; там точно сохранён исходный модельный JSON и draft JSON до внесения production. Для KEEP есть индивидуальные source листы front/profile/3⁄4/GUI1x с исходными SHA256 в `unique-source-review/`. Скрипт `tools/weapons87_unique_art.py` детерминированно воспроизводит два новых item JSON, GUI pool и атлас Kalin. CI создаёт source after/before рендеры по фактическим JSON+UV/PNG (не gameplay).

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| source BEFORE и индивидуальная оценка | frozen source + scheme указан выше | PASS source-only |
| Адресный KEEP; отсутствие произвольного редизайна | whitelist art paths, draft, canonical art overlay | PASS source-only / CI pending |
| Java21 clean build и артефакт JAR | branch GitHub Actions `weapons87-p20-p25-java21.yml` | PENDING_CI |
| Совпадение production и source draft / texture/UV | `tools/weapons87_unique_art.py --check --jar`, исторические generator gates | PENDING_CI |
| 16px GUI, first-person/third-person обе руки/Steve/Alex при ходьбе/атаке | source/cases + обязательная ручная игровая приёмка | NOT_RUN / OWNER_PENDING |
| Minecraft/PolyMC установка, игровой бой, MP | запусков 0, установка 0 | NOT_RUN |

Задача #188 остаётся открытой: auto QA не равна признанию игроком.
