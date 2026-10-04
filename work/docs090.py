from pathlib import Path
root=Path('D:/slavic-myths')
section='''# Slavic Myths 0.9.0 — Охота на нечисть I

Овинник обновлён с сохранением `slavicmyths:ovinnik` и старых world/NBT связей;
добавлен Волколак. Новые охоты не перестраивают курганы. В новых овинах больше
нет гарантированного мини-босса при worldgen: овин даёт среду для редкой ночной
встречи или рога. Существующие Овинники переходят на новый профиль при загрузке.

| Мини-босс | HP | Механики |
| --- | ---: | --- |
| `ovinnik` — Овинник | 175 | Тяжёлый взмах (10), удар жаровни (8 / радиус 4), зольный конус (4 / Weakness I), тлеющая погоня с временным следом (2 fire damage), угольный снаряд (5), ярость <=50% |
| `volkolak` — Волколак | 165 | Коготь (9), физический прыжок (8), комбо 4/4/7 через 6 тиков, вой с Weakness I/меткой, обход и боковой рывок, отступление после серии, фаза запаха крови <=50% |

Овинник получает 2 урона в секунду в воде, не использует тлеющий след в воде и
не повреждается огнём/лавой. Дождь под открытым небом замедляет перезарядки на
20% и ослабляет его fire damage на 15%. В ярости он быстрее на 8%, интервал
взмаха сокращается с 22 до 18 тиков. Щит блокирует обычную атаку, снижает slam
до 3 до брони с меньшим отбрасыванием; зольный конус не исчезает от щита.

Волколак в фазе крови быстрее на 7%; прыжок перезаряжается примерно на 15%
быстрее. Ночью в полнолуние добавляется 5% скорости, без бонуса HP/урона.
Метка от воя даёт 10% скорости при преследовании отмеченной цели на 120 тиков.
Серебряный удар предметом из `slavicmyths:silver_weapons` наносит ему на 25%
больше итогового урона. Используются существующие меч, кинжал, копьё, булава,
ритуальный нож и серп; нового серебряного оружия нет.

Оба имеют собственную кубическую анатомию, модели/атласы 256×256, отдельные
замахи, восстановление и ограниченные cooldown. Огонь/угли и глаза Овинника,
только глаза Волколака имеют отдельный emissive pass. Трофеи — 256×256,
сложные предметы — 512×512. Рог имеет собственную объёмную held-модель,
оберег/пояс — видимые части экипировки через существующий renderer API Curios.
Звуки — проверенные vanilla Blaze/Wolf/wood/fire/Ravager/raid-horn sources;
новые OGG не создавались. Raid horn нормализован против его исходного gain .01,
без трансляции на весь радиус рейда. Самодельные fire blocks и разрушение земли
способностями не используются.

## Редкие встречи и рог

Естественные встречи: Overworld, ночь, не Peaceful, твёрдый пол и три свободных
блока. Овинник требует сено/костёр/коптильню в 16 блоках, Волколак — лес/тайгу.
Spawn weight 1, дополнительный шанс 1/96; группа из одного моба, проверка
отсутствия такого же загруженного мини-босса в 64/72 блоках. Нет сканирования
мира каждый tick или принудительной загрузки чанков.

`ohotnichiy_rog`: Shift + ПКМ меняет цель в серверном ItemStack NBT, обычный ПКМ
начинает охоту. Требования: Overworld, ночь, не Peaceful, игрок не в воде,
нет своей ACTIVE охоты; для Овинника сено/костёр/коптильня в радиусе 18, для
Волколака forest/taiga. До 32 попыток найти безопасную точку в 12–24 блоках:
проверяются пол, свободная высота, жидкость, коллизия, граница мира и отсутствие
кургана. Чанки не загружаются ради поиска. Ошибки сообщают конкретную причину.

Перезарядка игрока 6000 тиков / пять минут записывается в world saved data только
после успешного спавна. Неудача ничего не расходует. Переключение цели доступно
без ванильной блокировки use; при cooldown обычный ПКМ сообщает оставшееся время.
Смена рога, смерть и перезаход не обходят сохранённый cooldown.

Запись хранит owner UUID, target UUID, anchor, тип, ACTIVE/CLEARED/FAILED и время
готовности рога. Выгруженный UUID остаётся ACTIVE; не создаются новые волны.
После реальной смерти встреча CLEARED, новый призыв требует нового законного
ПКМ и истёкшего cooldown. Основная область боя около 40 блоков, край 56:
выход за неё ведёт к возвращению обычной навигацией. Призванный моб не проходит
через порталы в другое измерение и не despawn от расстояния. Debug clear
останавливает встречу; выгруженный участник будет удалён при загрузке.

## Предметы

| ID | Назначение |
| --- | --- |
| `ovinnaya_zola` | Ингредиент оберега; Овинник гарантированно 8–14 |
| `iskra_ovinnika` | Ингредиент оберега; Овинник гарантированно 1, ещё 1 с шансом 20% |
| `klyk_volkolaka` | Ингредиент пояса; Волколак 2–4, ещё 1 с шансом 25% |
| `nochnoy_kogot` | Ингредиент детектора; Волколак с шансом 35% |
| `zolny_obereg` | Только в Curios necklace: fire/lava damage ×0.80, новый остаток горения примерно −40%; не Fire Resistance и не безопасность в лаве |
| `volchiy_poyas` | Только в Curios belt: ночью +8% скорости, полнолуние +3% (максимум +11%); днём 0, без атаки; стабильный transient UUID |
| `obereg_ohotnika` | Активный детектор по ПКМ, cooldown 400 тиков; ближайшая живая hunt target в 128 блоках, направление и расстояние округлённое до 10; Glowing 100 тиков только <=48 и LOS/своя охота |
| `ohotnichiy_rog` | Контролируемый призыв одной охоты |
| `meshochek_trofeev` | Небольшой GUI, ровно 9 слотов в одну строку, содержимое в NBT предмета |
| `pustoy_ritualny_sosud` | Осознанно неактивный предмет будущего назначения: по 8% у обоих боссов, нет ПКМ и рецепта |

Эффекты аксессуаров не работают из обычного инвентаря и не складываются от
дубликатов. Пояс убирает свой modifier при снятии; permanent modifier не пишется
в player save. Мешочек принимает только `slavicmyths:hunt_trophies`: первые
четыре трофея и пустой сосуд. Вложить другой мешочек/shulker нельзя; shift-click
также проверяет tag. Удерживаемый мешочек и swap слота защищены, изменения
сразу сохраняются в его NBT. Выбрасывание/подбор и обычная глубокая копия
ItemStack сохраняют независимое содержимое.

Оба мини-босса дают 50 XP. Добыча определяется только loot tables. Достижения:
`hunt_begins` за успешный рог, `extinguish_the_barn` за Овинника,
`silver_remedy` за последний серебряный удар по Волколаку, `trophy_keeper`
за помещение подходящего трофея в мешочек.

## Пять рецептов / JEI

Пробел или точка — пустая клетка. Используется обычный `minecraft:crafting_shaped`;
JEI показывает эти рецепты штатной категорией crafting. Loot-only предметам
не добавлены фиктивные рецепты. Крафт рога не требует трофея мини-босса.

| Результат | Ряды | Материалы |
| --- | --- | --- |
| Рог | `BL.` / `.BI` / `S.B` | B bone, L leather, I iron_nugget, S string |
| Зольный оберег | `ISI` / `AKA` / `.A.` | I iron_nugget, S string, A ovinnaya_zola, K iskra_ovinnika |
| Волчий пояс | `LLL` / `FIF` | L leather, F klyk_volkolaka, I iron_ingot |
| Оберег охотника | `SBS` / `WCW` / `.R.` | S string, B bone, W oak_planks, C nochnoy_kogot, R red_dye |
| Мешочек | `LSL` / `L.L` / `LIL` | L leather, S string, I iron_nugget |

## Точные команды / ручной тест

Cheats / OP level 2; бой проверять в Survival на обычной сложности.
Vanilla summon/яйца создают тестового моба без ограничения окружения и без
владельца охоты. Dev summon обходит ночь/окружение/cooldown, сохраняя проверки
безопасного места и запрет второй ACTIVE охоты.

```mcfunction
/summon slavicmyths:ovinnik ~ ~ ~
/summon slavicmyths:volkolak ~ ~ ~
/give @s slavicmyths:ovinnik_spawn_egg
/give @s slavicmyths:volkolak_spawn_egg
/give @s slavicmyths:ohotnichiy_rog
/give @s slavicmyths:zolny_obereg
/give @s slavicmyths:volchiy_poyas
/give @s slavicmyths:obereg_ohotnika
/give @s slavicmyths:meshochek_trofeev
/give @s slavicmyths:pustoy_ritualny_sosud
/slavicmyths dev hunt summon ovinnik
/slavicmyths dev hunt summon volkolak
/slavicmyths dev hunt clear
```

В отдельном мире: переключить рог; проверить дневной отказ и отсутствие среды,
ночью призвать обоих по одному. Проверить slam/ash/trail/снаряд, воду/дождь,
прыжок/комбо/вой/обход, серебро и добычу. Надеть оберег/пояс в Curios и проверить
огонь, ночь/день, снятие. Использовать детектор; вложить трофеи/попробовать
посторонний предмет, выбросить/подобрать мешочек. Проверить инертный сосуд,
яйца, перезаход посреди охоты, отсутствие дубликата и cooldown. Полный список:
`docs/MANUAL_QA_0.9.0.md`.

Техническая проверка: Java8 `gradlew.bat --offline clean build verifyHunt verifyKurgan`,
`python tools/verify_hunt_090.py`, `python tools/verify_resources.py`.
Новая ресурсная прослойка: `python tools/hunt_090.py` отдельно; старый глобальный
генератор не запускался, пользовательское оружие сохранено.
Игровых запусков **0**, ручной GUI/AI/JEI/performance QA — **TODO**.
JAR: `build/libs/slavicmyths-0.9.0.jar`. Проверки и установка фиксируются в
`docs/verification/headless-0.9.0.json` и `install-0.9.0.json`.

0.9.1–0.9.4 не начинались: новые мини-боссы, Лихо/Тугарин, Баба-яга и её
избушка/котёл/обмен, живая/мёртвая вода не входят в этот патч.

---

'''
p=root/'README.md';p.write_text(section+p.read_text(encoding='utf-8').replace('# Slavic Myths 0.8.6 — Существа курганов','# Предыдущий этап: 0.8.6 — Существа курганов',1),encoding='utf-8')
p=root/'docs/PROJECT_STATUS.md';p.write_text('''# 0.9.0 — Охота на нечисть I

Овинник обновлён на 175 HP с сохранением ID/Home/guardian интеграции, Волколак
добавлен на 165 HP. Реализованы замахи, slam/ash/trail/ember, ярость/вода/дождь,
pounce/combo/howl/flank/dodge, кровь/луна/серебро. Собственные модели 256px,
десять предметов 256/512px, объёмный рог, видимые аксессуары в Curios.

HuntRecords сохраняет owner/target UUID, anchor, состояние и 6000-tick cooldown;
выгрузка не завершает ACTIVE. Один призыв на игрока, безопасный ограниченный
поиск, редкие natural spawn placements. Детектор по ПКМ, оберег/пояс только
экипированы, мешочек 9 слотов со slot/swap/tag защитой, сосуд осознанно инертен.
Пять точных JSON recipes для штатного JEI crafting, четыре advancements,
RU/EN и проверенные vanilla sounds. Новые овины не создают гарантированного
Овинника; существующие существа обновляют старые saved attributes при загрузке.

Результаты: verification/headless-0.9.0.json и install-0.9.0.json.
Java8 compile / headless HuntRecords и курганная регрессия прошли. Финальные
clean build / resource / reobf / установка записываются в эти отчёты.
Minecraft запусков 0, игровой бой/GUI/JEI/перезаход/MSPT не тестировались.
Пользовательское оружие сохранено, commit не выполнялся.

---

'''+p.read_text(encoding='utf-8'),encoding='utf-8')
p=root/'docs/ROADMAP.md';p.write_text('''# Актуальная ветка 0.9.x

- 0.9.0: Овинник/Волколак, первые трофеи и hunting система реализованы; headless QA, игровой QA TODO.
- 0.9.1: ещё два мини-босса и расширение наград — не начато.
- 0.9.2: Лихо Одноглазое / Тугарин Змей — не начато.
- 0.9.3: Баба-яга, избушка, задания/обмен/котёл — не начато.
- 0.9.4: сведение контента, баланс/JEI/advancements — не начато.
- 0.9.5+: content freeze, аудит/баги/визуал/баланс/performance/polish.
- 1.0.0: стабильный релиз.

Живая/мёртвая вода перенесены на поздний этап и в 0.9.0 не реализованы.
Следующее действие: ручной QA текущей сборки по MANUAL_QA_0.9.0.md.
Исторические планы ниже сохранены для контекста.

---

'''+p.read_text(encoding='utf-8'),encoding='utf-8')
p=root/'docs/ARCHITECTURE.md';p.write_text(p.read_text(encoding='utf-8')+'''

## 0.9.0 Hunt I

HuntMob наследует LandSpiritEntity для существующего Home/guardian/save API,
но отключает старое расписание usesSpiritSchedule и использует один Fight Goal.
OvinnikEntity сохраняет registry/class ID; VolkolakEntity — второй фиксированный
профиль. Synced action/start/rage управляют native HuntModel. Овинник применяет
physical EmberClump и ограниченный 40-tick trail без блоков/terrain grief.
Combo follow-ups учитывают vanilla hurt window только для собственного удара.
Старые saved attributes Овинника переводятся на 175/10/10/2/.55/32 без autoheal.

HuntRecords — минимальный WorldSavedData с последней охотой каждого owner:
target UUID, anchor, kind, status, HornReady. Отсутствие loaded entity не
завершает запись. Смерть = CLEARED, admin/Peaceful = FAILED. Summoned owner
сохраняется в entity NBT, смена измерения блокируется. После admin clear
выгруженный участник удаляется при следующей загрузке без награды.

HuntItems/HuntSpawns используют только use/placement scans с ограниченными
радиусами и количеством кандидатов. Проверяется только loaded пространство;
нет forced chunks и player tick scans на 128 блоков. Cooldown рога в records,
ванильный item cooldown не блокирует смену цели/локализованную причину отказа.

PouchMenu использует существующий RpgMenu.MENUS, 9-slot Inventory на точном
held ItemStack identity. Запись NBT немедленная; pouch slot/swap защищены,
tag применяется к ручной вставке/shift move. Vanilla ItemStack copy выполняет
глубокую копию nested NBT. PouchScreen переиспользует стиль CargoScreen.
HuntEffects проверяет Curios; stable transient UUID пояса и огненный множитель
без inventory-passive эффектов/дубликатов. SilverCombat использует один item
tag/helper, Volkolak получает ×1.25 в LivingDamageEvent после брони.
HuntAccessoryRenderer использует существующий Curios body API; все вызовы
client методов в OnlyIn/client коде. Custom category JEI не нужна для JSON
crafting. tools/hunt_090.py — воспроизводимая отдельная прослойка native art.

verifyHunt не запускает Minecraft: owner/UUID/state/cooldown NBT, модификаторы
по времени, сроки способностей и глубокая копия nested pouch NBT. Это не
runtime proof отсутствия duplication/AI bugs; ручной QA выделен отдельно.
''',encoding='utf-8')
prompt=next((root/'work/hunt090_reference').rglob('PROMPT*.md')).read_text(encoding='utf-8')
manual=prompt[prompt.index('# 29. MANUAL'):prompt.index('# 30. BUILD')]
(root/'docs/MANUAL_QA_0.9.0.md').write_text('# Ручной QA 0.9.0 — TODO\n\nMinecraft запусков 0. Проверки выполнять в отдельном мире/копии сохранения.\nHeadless тесты не подтверждают игровой бой, GUI, JEI или реальный перезаход.\n\n'+manual+'''
Дополнительно:
- [ ] Повторный рог/смена стека/смерть/перезаход не обходят ACTIVE и cooldown.
- [ ] Выгрузка чанка не создаёт вторую охоту. Admin clear удаляет старого
      участника после загрузки и не даёт добычу; естественная смерть даёт 50 XP.
- [ ] Шеститиковое комбо действительно наносит 4/4/7 при попадании всех ударов;
      уход из range/за стену или щит позволяют промахнуться/защититься.
- [ ] Нет terrain grief/реальных fire blocks и атак через сплошную стену.
- [ ] Пояс/оберег видны в Curios, дубликаты не складываются, эффект отсутствует
      в обычном инвентаре, transient modifier снят после unequip/relog.
- [ ] Pouch: shift-click, drag, number keys, offhand swap, close/death и drop/
      pickup не дублируют/теряют содержимое; creative clone независим.
- [ ] Пять рецептов видны в штатном crafting JEI; loot-only не имеют рецептов.
- [ ] Все четыре advancements выполняются; silver_remedy требует серебряный
      последний удар.
- [ ] Старый Овинник/старые миры/курганы сохраняются, новый Овинник имеет 175 HP.
- [ ] Отдельно измерить TPS/MSPT и проверить слышимость рога/эмиссию в игре.
''',encoding='utf-8')
(root/'docs/CONTINUE_0.9.0.md').write_text('''# Продолжение после 0.9.0

Проект D:\\slavic-myths, Forge 36.2.42 / Minecraft 1.16.5 / Java8. Читайте статус,
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
''',encoding='utf-8')
