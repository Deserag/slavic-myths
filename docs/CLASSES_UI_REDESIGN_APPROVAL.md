# Classes 2.0 — диагностика и макеты на утверждение

Статус 2026-10-08: выполнены этапы 1–2 нового ТЗ. Полная переделка GUI, сборка и установка исправления **не начаты**: в §18.2 пользователь требует «До моего утверждения не приступать к полной переделке GUI». Установленная в PolyMC версия остаётся 1.3.3. Клиент/сервер не запускались, commit не делался.

## Конкретные причины по текущему коду

| Область | Причина | Предлагаемое исправление после утверждения |
|---|---|---|
| Viewport | ClassScreen.init ограничивает panel шириной 900, постоянно резервирует справа 202–292 px, footer начинается H−72 | Полноширинный viewport `(8,64,W−16,H−116)`, footer `(8,H−47,W−16,39)` |
| Карточка | details рисуется постоянно, init автоматически выбирает первый skill; иконка 48 px | Закрыта по умолчанию; overlay 26–28%, максимум 160 px, не меняет viewport/camera; иконка 32 |
| Геометрия | Узел 44 px, root 52; горизонтальный шаг 58 | Узел 34, root 40, шаг 72×65; реальные prerequisites, таблицы всех трёх графов |
| Навигация | Нет zoom/навигационных кнопок; mouseDragged не хранит отдельное состояние захвата, нет порога 4 px и mouseReleased | Одна камера/transform и inverse hit-test; empty-space drag ЛКМ/СКМ, wheel 24, Shift/X, Ctrl/zoom 0.75–1.75, центрирование и bounds |
| Loadout | 36 px frame, 32 px icons, большие промежутки и длинный footer hint | 22 px slots / 18 px icons, 39 px footer, I/II/III; passive только отдельный slot |
| Модалка | Background/modal widgets смешаны; render и mouseClicked отбирают modal кнопки по совпадению Y; высота 122 не рассчитывается по Font.split | Отдельный слой/группа ввода, текстовые блоки измеряются, две кнопки после текста, pending confirmation защищает от повторов |
| Управление | Три кнопки до 350 px; конфликт — весь список через drawWordWrap между строками | Панель 320×165, rows 32/4, отдельные key 58×20 / reset 20×20, компактный счётчик и отдельный popup |
| HUD | Frame 36, icon 32, шаг 38; X=W−124, Y=H−62 или H−116 | На утверждение компактный вариант 18/14, шаг 21, правый/нижний отступ 8; legacy geometry отличается, см. ниже |
| Иконки | Полные UV уже исправлены ClassTheme.image; сами PNG 32px native, crests64 | Не заменять пригодные исходники без причины; nearest scaling 28/18/14, проверить каждый resource и смысл |

Состояние обучения, ClassDefinitions, ClassBalance, ClassState, network/server validation, rune system, модели предметов, KeyMapping и options.txt не изменены этапом проектирования. Наличие кода сохранения не выдаётся за проверку смерти/relog.

## Пять основных PNG

Основной вариант: 1649×928, фактический GUI Scale 3, scaled 550×310.

1. [Дерево](media/classes-ui-redesign/preview-tree.png): всё центральное пространство, root/первые два prerequisite направления, компактный footer. Продолжение графа находится за viewport; его достижимость после реализации должна проверяться камерой, а не уменьшением всего дерева.
2. [Выпад](media/classes-ui-redesign/preview-skill.png): overlay, прежний размер дерева, fixed header/action и scroll body. Статы прочитаны из текущего ClassDefinitions; fixture без специализации, rank1→2, level3. В loadout только изученные Выпад/Закалка.
3. [Подтверждение](media/classes-ui-redesign/preview-branches.png): фоновые карточки затемнены, отдельный верхний слой с двумя кнопками. [Карточки без модалки](media/classes-ui-redesign/preview-branches-base.png) позволяют проверить их полностью.
4. [Управление](media/classes-ui-redesign/preview-controls.png): реальные текущие назначения из options.txt: ЛКМ, H, J. Три raw key collisions ЛКМ: vanilla attack и два JEI действия. Runtime KeyMapping context/modifier matching ещё не проверялся; счётчик в рабочем GUI будет брать ClassClient.conflicts, а не этот offline snapshot.
5. [HUD](media/classes-ui-redesign/preview-hud.png): схематическая voxel-сцена и hotbar для сравнения размеров, не игровой screenshot. Маленький прежний passive indicator сохранён. ЛКМ/H/J соответствуют options snapshot.

[Обзор](media/classes-ui-redesign/overview.png) уменьшен для совместного просмотра; точные физические PNG по ссылкам выше. Фон макетов не является фотографией или игровым рендером.

## HUD: решение, требующее утверждения

`git log --all -- '*ClassClient*'` не дал истории: ClassClient — untracked в текущей рабочей копии. ClassClient.class в резервном 1.3.2.1 побайтно совпадает с 1.3.1 (обе копии проверены). Резервный production JAR 1.3.1 даёт проверяемую предыдущую геометрию через javap: outer frame 24×24, step25, X=W−80 для внутреннего origin, Y=H−74, labels Y+25, passive14 выше группы. Диагностика `.tools/classes-ui-hud-131-bytecode.txt`.

Нижнее положение этой старой реализации расходится с новым требованием bottom8. Основной макет предлагает вариант §11.3: active18/icon14, gap3, group60×29, right8/bottom8; passive14 над группой. Это **предложение**, а не заявление, что geometry18 восстановлена из Git. До утверждения спорный выбор не применяется.

## Проверяемые размеры и ограничения

`tools/classes_ui_design.py` создаёт пять основных PNG и 40 state/resolution PNG для восьми обязательных пар. [Manifest](media/classes-ui-redesign/layout-manifest.json) хранит physical/requested/actual/scaled размеры, containers, modal buttons, camera fixture и raw bindings. [Дружинник](media/classes-ui-redesign/druzhinnik-graph.csv), [Ведун](media/classes-ui-redesign/vedun-graph.csv), [Разбойник](media/classes-ui-redesign/razboinik-graph.csv): 34 существующих навыка, names/kinds/rank fixture/prerequisite/branch/positions, без новых gameplay edges.

1344 offline geometry/font checks PASS: screen bounds, footer/viewport separation, modal gap/button non-overlap, card limits, label widths, initial root/first nodes/nav separation, node/rank separation, HUD/hotbar separation, bitmap glyph availability. Это проверки **предлагаемых макетов**, не работающих mouse/key handlers. Zoom anchor, inverse hit-testing, drag threshold, modal priority, reset/cancel и persistence будут проверяться после разрешённой реализации.

Шрифт: реальные bitmap glyphs/advance из установленного Minecraft 1.21.1 `include/default`, uniform=false. Text wrapping воспроизводит bitmap widths; Pillow rendering не является реализацией Minecraft Font и не подтверждает OpenGL pipeline. Только nearest-neighbor. Кнопки/рамки макета показывают палитру и размеры; texture states/pressed offsets/wood grain будут подготовлены как production GUI assets после утверждения.

При framebuffer1649×928 requested GUI4 vanilla Window.calculateScale(false) возвращает3 из-за минимального scaledHeight240. Поэтому реальные пары: 825×464 (2), 550×310 (3), 550×310 (requested4/actual3); 1920 — 960×540/640×360/480×270; 2560 — 854×480/640×360. Force Unicode/font/resource pack/window framebuffer могут менять фактический результат; их игровые варианты не проверены.

## Следующий этап

После утверждения этих изображений и выбранного HUD-варианта реализовать shared layout/camera/input layers, popup scrolling/conflicts, resets и GUI resources. Затем выполнить автоматические проверки именно реализации, визуальные snapshots, clean build и ресурсные gates; установить единственный новый JAR в существующий PolyMC с backup/hash checks. Клиент самостоятельно не запускать. Текущие макеты не означают, что пользовательские проблемы уже исправлены.
