# Ocean Rewards / 1 — начальная интеграция (20.11.2026)

Ветка: `integration/ocean-updates`. Issue: #519 (эпик #518).

## Сделано в этом коммите
- Зарегистрированы 6 материалов и 6 знаков победы.
- Смерть каждого **настоящего** босса выдаёт точное количество материалов + один знак (обычные Фараонки и миньоны исключены).
- Использован LivingDropsEvent; существующие головы, гребень Бездны и Оберег Желвы не изменяются.
- `BuyanTrades` выкупает знаки только у жителя с ролью `knyaz`, за 18/16/22/20/12/22 древние монеты; предотвращён дубль одинаковой записи при повторных updateTrades.
- Модели ресурсов ссылаются на **существующие vanilla PNG**. Это временная безопасная база (не новый кубоидный предмет). Индивидуальные реколоры **пока не готовы**, issue #519 остаётся открытым.
- Регистрация через `SlavicMyths` до `ModItems.ITEMS.register(bus)`, каталог творческой вкладки обновлён.

## ID материалов
- `slavicmyths:pharaohka_tide_pearl` ×3
- `slavicmyths:deep_sea_serpent_scale` ×5
- `slavicmyths:ocean_zmey_storm_heart` ×3
- `slavicmyths:stratim_bird_feather` ×4
- `slavicmyths:tsar_pike_fang` ×3
- `slavicmyths:zhelva_shell_plate` ×6

## ID торговых знаков
- `slavicmyths:pharaohka_victory_token` → 18 ancient_coin
- `slavicmyths:deep_sea_serpent_victory_token` → 16 ancient_coin
- `slavicmyths:ocean_zmey_victory_token` → 22 ancient_coin
- `slavicmyths:stratim_bird_victory_token` → 20 ancient_coin
- `slavicmyths:tsar_pike_victory_token` → 12 ancient_coin
- `slavicmyths:zhelva_victory_token` → 22 ancient_coin

## Не выполнено / не выдавать за PASS
- Проверка компиляции Java 21, GameTests реального дропа/торговли и клиентские скриншоты.
- Оригинальные реколор-текстуры (в отличие от ссылок на временные ванильные изображения).
- RU/EN локализации в lang JSON — пока имена fallback в коде; добавить keys при последующей задаче.
- Все эффекты сета, оружия, рун, аксессуаров (issues #520–#524).

Сохранения и существующие registry ID не мигрируются, старые трофеи не меняются.
