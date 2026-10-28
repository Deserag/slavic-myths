<!-- UP_TO_DATE_REFERENCES_20261028 -->
## 28.10.2026 — АНАЛИЗ ОТСУТСТВУЮЩИХ РЕФЕРЕНСОВ И МАТЕРИАЛЫ, КОТОРЫЕ РЕАЛЬНО ЛЕЖАТ В GITHUB

**Причина:** прежние markdown задачи указывали sha и имя картинки, но сами большие PNG в GitHub не загружались. Это не ложная ошибка Codex — реальных source PNG действительно нет в дереве ветки `hell`.

**Сейчас на GitHub действительно существуют следующие отдельно открываемые SVG-файлы:**
- [MAGMA_LORD_ORTHOGRAPHIC_DESIGN_GUIDE.svg — настоящий SVG трёх ракурсов босса](MAGMA_LORD_ORTHOGRAPHIC_DESIGN_GUIDE.svg).
- [MAGMA_LORD_TROPHY_HEAD_MODEL_GUI_GUIDE.svg — настоящий SVG головы в мире и инвентаре](MAGMA_LORD_TROPHY_HEAD_MODEL_GUI_GUIDE.svg).

**Оригинальные пять PNG были НАЙДЕНЫ в ChatGPT и проверены локально по SHA256.** Собран новый архив **`BURNING_ISLANDS_205_COMPLETE_REFERENCES_FOR_GITHUB.zip`** (12,002,229 байт; SHA256 **`22256bf4df4f8c8abf6c5aec2ec6a9d30c3be02b0a3d7b59c5751908bc6edada`**). Архив **представлен владельцу в этой беседе как файл для скачивания**, он **НЕ** лежит в GitHub и не становится доступен Codex автоматически. Дерево ZIP готово к извлечению **в корень репозитория** на ветке `hell` с `docs/concepts/burning-islands/references/` внутри.

**Список исходных PNG, которые нужно импортировать и в действительности закоммитить:**
| Исходное имя в репозитории | SHA256 |
| --- | --- |
| `BURNING_ISLANDS_205_BIOME_CONCEPT.png` | `a770acc9cb8401baa8199994f73cf28ee1fb3d43eff25fe0fd5bdbff92fbf605` |
| `BURNING_ISLANDS_205_CREATURES_CONCEPT.png` | `a06f8d7730e43f93003a0a9c465c806e6e28a41260e7a61640d1f336924651d1` |
| `BURNING_ISLANDS_205_RUNE_MATERIALS_CONCEPT.png` | `47a9f11cd0e3c98e48983cf28c8b5ed7c6687689d9abb9cc5ee28edd7a47fdab` |
| `BURNING_ISLANDS_205_BOSS_CONCEPT.png` | `354c2c318c01ee67b21b60adc653a0a4747acc81786a433cb28e1c3fc26881a6` |
| `BURNING_ISLANDS_205_MAGMA_LORD_HEAD_PLACED_INVENTORY.png` | `287ce3f15e28b1a646cf31de1e51fab85519bb03dc935d83bb7aea920395bc93` |

**До импорта больших PNG на `hell` статус для оригинального owner-reference сравнения: `BLOCKED_REFERENCE_BINARY`.** SVG — настоящие доступные справочные **технические чертежи**, не выдавать их за источник/полноразмерную ModelPart, UV или проверенный Minecraft screenshot. Полное ТЗ: [этап 3 — Повелитель магмы](../CODEX_STAGE3_MAGMA_LORD_IMPLEMENTATION_2.0.5.md). Задачи [#317–#323](https://github.com/Deserag/slavic-myths/issues/317).

---
# Slavic Myths 2.0.5 — оригинальные художественные референсы (ожидается импорт PNG)

**Статус:** оригинальные три PNG созданы в беседе ChatGPT 28.10.2026 и упакованы в архив `BURNING_ISLANDS_2.0.5_ART_REFERENCES.zip` (SHA256 `b44acff750454a15942552a91c16d17ae5a62d969e60dabdbd1ff1db0c760467`). **Бинарные PNG пока не находятся в ветке `hell`**. Этот README — только manifest, не подмена исходников. Экспортированный архив приложен к беседе и доступен владельцу для добавления в repo.

| Ожидаемый путь в этой папке | Размер | SHA256 | Статус решений |
| --- | --- | --- | --- |
| `BURNING_ISLANDS_205_CREATURES_CONCEPT.png` | 1536×1024 | `a06f8d7730e43f93003a0a9c465c806e6e28a41260e7a61640d1f336924651d1` | 3 моба/общая визуальная идея босса утверждены; детали моделей могут уточняться |
| `BURNING_ISLANDS_205_RUNE_MATERIALS_CONCEPT.png` | 1536×1024 | `47a9f11cd0e3c98e48983cf28c8b5ed7c6687689d9abb9cc5ee28edd7a47fdab` | визуальное направление рун I–II и предметов утверждено; точные рецепты/числа открыты |
| `BURNING_ISLANDS_205_BOSS_CONCEPT.png` | 1536×1024 | `354c2c318c01ee67b21b60adc653a0a4747acc81786a433cb28e1c3fc26881a6` | **«Повелитель магмы» — утверждённое имя и основной дизайн**; боёвка/HP/арена/трофеи обсуждаются отдельно |

**Перед исполнением**: импортировать PNG как **справочные референсы**, сравнить SHA256; не считать их готовыми ModelPart/UV/texture или скриншотами фактического Minecraft. При отсутствии файла — `BLOCKED_REFERENCE_BINARY` для визуального сравнения. Не копировать модели/коммерческую IP сторонних игр; все готовые исходники должны быть самостоятельными. Основное ТЗ [CODEX_STAGE2_MOBS_LOOT_MAGMA_RUNE_2.0.5.md](../CODEX_STAGE2_MOBS_LOOT_MAGMA_RUNE_2.0.5.md), задача [#316](https://github.com/Deserag/slavic-myths/issues/316).

## 28.10.2026 — новый утверждённый референс головы Повелителя магмы

Владелец **одобрил конкретную картинку установленной головы и её статичной иконки инвентаря**. Ожидаемое имя нового оригинального файла: `BURNING_ISLANDS_205_MAGMA_LORD_HEAD_PLACED_INVENTORY.png` (оригинал в чате был `голова_повелителя_магмы_трофей_и_иконка.png`), **SHA256 `287ce3f15e28b1a646cf31de1e51fab85519bb03dc935d83bb7aea920395bc93`**, ~2 МБ. **Binary PNG НЕ находится в репозитории**, добавить только оригинал и проверить hash при импорте. Модель установленного трофея, подвижная челюсть, парящие осколки, локальное свечение и отдельная статичная иконка GUI подробно описаны в [MAGMA_LORD_REWARDS_HEAD_CONCEPT_2.0.5.md](../MAGMA_LORD_REWARDS_HEAD_CONCEPT_2.0.5.md). Нельзя объявлять готовую/совпадающую художественную реализацию до фактического PNG и owner QA.
