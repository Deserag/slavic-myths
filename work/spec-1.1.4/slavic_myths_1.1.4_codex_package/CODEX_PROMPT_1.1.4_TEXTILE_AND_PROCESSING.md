# SLAVIC MYTHS 1.1.4 — «ИНСТРУМЕНТЫ, ПРОДУКЦИЯ И ТЕКСТИЛЬ»
## Финальный design document + implementation prompt для Codex

---

# 0. ОБЯЗАТЕЛЬНЫЕ ПРАВИЛА РАБОТЫ CODEX

Это точное техническое задание.
Codex НЕ проектирует версию самостоятельно и НЕ расширяет scope.

## 0.1 Экономия токенов — обязательное правило

Токены расходовать максимально рационально.

ЗАПРЕЩЕНО:
- проводить общий аудит всего проекта;
- читать весь репозиторий подряд;
- анализировать системы, не связанные с 1.1.4;
- составлять длинный архитектурный отчёт;
- рефакторить старый код «заодно»;
- переписывать работающие 1.1.0 / 1.1.2 / 1.1.3 без необходимости;
- повторять одни и те же поиски по проекту;
- делать лишние сборки;
- запускать Minecraft;
- переносить JAR в PolyMC;
- тратить токены на GitHub Release;
- придумывать дополнительные предметы;
- добавлять новые механики, не описанные в этом документе;
- задавать пользователю вопросы по дизайну.

Разрешён только минимально необходимый просмотр:
- item/block/entity registries;
- существующих creative tabs;
- существующих моделей/рендереров экипировки;
- 1.1.0 flax/crops;
- 1.1.3 livestock/meat entities;
- player data/attachment helpers, если они уже есть;
- recipe/tag/lang conventions;
- минимальных world/event hooks, необходимых для carcass/accessory/sleep logic.

Принцип:
**нашёл нужную точку интеграции → внёс правку → двигаешься дальше.**

## 0.2 Minecraft не запускать

НЕ запускать:
- runClient;
- runServer;
- Minecraft;
- тестовый мир;
- PolyMC.

Пользователь проводит игровые тесты вручную.

Разрешается:
- одна финальная compile/build проверка;
- максимум один повторный build только если первая ошибка вызвана изменениями 1.1.4 и после исправления нужно проверить компиляцию.

## 0.3 PolyMC

После реализации:
- НЕ копировать JAR в PolyMC;
- НЕ обновлять mods folder;
- НЕ менять instance;
- НЕ запускать PolyMC.

Перенос выполняется только по отдельной будущей команде пользователя.

## 0.4 Никакой самостоятельной фантазии

НЕ добавлять:
- новые виды одежды сверх списка;
- новые аксессуары;
- дубление кожи;
- сыр/масло/сметану;
- новые блюда;
- новые животные;
- новые поля/сады;
- новые NPC;
- новые GUI без прямого требования ниже;
- новую библиотеку аксессуаров;
- Curios/Accessories API как обязательную зависимость;
- новый loader;
- новую систему пола животных;
- новые carcass types;
- новые боевые эффекты одежды;
- сезонность;
- жажду;
- сложную экономику.

Если есть техническая развилка:
выбрать самое простое устойчивое решение, сохраняющее поведение из ТЗ.

## 0.5 Платформа

- Minecraft 1.21.1
- NeoForge
- Java 21
- текущий mod id проекта
- текущая package/resource/data структура

## 0.6 Git

Рабочая ветка:
`main`

После реализации:
- commit только в `main`;
- push в `origin/main`, если авторизация уже работает;
- НЕ трогать release branch;
- НЕ создавать tag;
- НЕ создавать/обновлять GitHub Release;
- НЕ force-push;
- НЕ удалять чужие изменения.

Commit message:
`feat: implement Slavic Myths 1.1.4 textile and processing`

Если push требует отдельной авторизации:
- не тратить токены на её настройку;
- оставить локальный commit;
- дать одну команду push в финальном отчёте.

---

# 1. ЦЕЛЬ ВЕРСИИ

1.1.4 связывает уже существующее хозяйство в производственные цепочки.

Версия должна добавить:

1. Разделочный нож.
2. Систему туш для взрослых хозяйственных животных.
3. Более выгодную ручную разделку туш.
4. Животный жир.
5. Козью шкуру.
6. Сальную свечу.
7. Переработку льна:
   - flax_stalk;
   - flax_fiber;
   - linen_thread;
   - linen_cloth.
8. Льномялку.
9. Прялку.
10. Ткацкий станок.
11. Бытовую одежду:
   - льняная рубаха;
   - порты;
   - сарафан;
   - пояс-аксессуар;
   - платок;
   - рабочий фартук;
   - жилет;
   - лапти.
12. Собственный один аксессуарный слот типа BELT.
13. Льняную постель.
14. Эффект «Хороший отдых».
15. Распределение всего нового контента по thematic Creative Tabs 1.1.2.
16. Один advancement за получение льняного полотна.

---

# 2. ВИЗУАЛЬНЫЙ РЕФЕРЕНС ОДЕЖДЫ

Главный утверждённый референс:

`references/01_clothing_design.png`

Он утверждён пользователем.

Использовать его как основу:
- цветов;
- вышивки;
- силуэтов;
- пропорций;
- народного характера одежды;
- внешнего вида пояса;
- платка;
- фартука;
- жилета;
- лаптей.

Текстовое ТЗ ниже имеет приоритет над картинкой.

---

# 3. РАЗДЕЛОЧНЫЙ НОЖ — `butchering_knife`

RU:
`Разделочный нож`

EN:
`Butchering Knife`

Creative tab:
`Инструменты и оружие`

## 3.1 Дизайн

Item texture:
- 32x32;
- transparent background;
- pixel art;
- без anti-aliasing.

Форма:
- короткое широкое лезвие;
- общая длина визуально меньше меча;
- клинок немного расширяется к основанию;
- кончик чуть загнут вверх;
- режущая кромка светлее основного металла;
- деревянная рукоять;
- рукоять тёмно-коричневая;
- маленькая металлическая заклёпка;
- гарда отсутствует либо очень небольшая;
- это хозяйственный нож, а не кинжал.

Не копировать старый dagger sprite.

## 3.2 Параметры

- stack size: 1;
- durability: 250;
- attack damage: умеренно низкий, около 3;
- attack speed: около -2.2;
- поддержать Unbreaking;
- поддержать Mending;
- не позиционировать как сильное оружие.

## 3.3 Основная функция

Right click по `carcass`:
- carcass разделывается;
- нож теряет 1 durability;
- carcass удаляется;
- выдаётся enhanced butchering loot;
- звук резки/работы использовать подходящий vanilla sound;
- небольшие частицы без крови/gore.

Если нож сломался на операции:
- операция всё равно завершается;
- carcass выдаёт loot;
- нож исчезает как сломанный инструмент.

Enchantments не увеличивают carcass loot.

---

# 4. СИСТЕМА ТУШ — ОБЩАЯ ЛОГИКА

Создать ОДИН reusable entity type:
`carcass`

НЕ создавать по entity type на каждый вид животного.

Carcass хранит enum/data variant:

- COW
- PIG
- SHEEP
- RABBIT
- GOOSE
- DUCK
- DOMESTIC_GOAT

Дополнительно:
- `wasBurning` boolean;
- `remainingLife` / age;
- owner/player UUID хранить НЕ обязательно;
- gender не существует.

## 4.1 Когда появляется carcass

Только если выполнено всё:

- погибшее животное взрослое;
- вид входит в список выше;
- непосредственная или косвенная причина смерти имеет игрока как responsible attacker.

То есть работают:
- меч;
- топор;
- стрела игрока;
- другое player-caused damage.

Если животное:
- умерло само;
- погибло от падения без player responsibility;
- сгорело само;
- погибло от другого моба;
- baby;

то использовать обычный существующий loot и НЕ создавать carcass.

## 4.2 Что происходит с обычным loot при player kill

Для adult eligible animal при player-caused death:
- стандартный meat/material loot соответствующего животного подавить;
- XP оставить стандартным;
- spawn 1 carcass entity.

Не создавать одновременно carcass + стандартное мясо.
Это предотвратит двойной loot.

## 4.3 Внешний вид carcass

Стиль:
- Minecraft-compatible;
- без крови;
- без внутренних органов;
- без реалистичного gore.

Визуально:
- аккуратно лежащая связанная хозяйственная туша/свёрток;
- вытянутая низкая форма;
- простая верёвочная перевязь;
- variant определяется цветом/масштабом.

Размеры:
- cow/pig/sheep/goat: крупнее;
- goose/duck: меньше;
- rabbit: самый маленький.

Можно использовать одну базовую геометрию с variant scaling/texture.
Не делать 7 полностью разных сложных моделей.

## 4.4 Поведение

Carcass:
- не двигается;
- не имеет AI;
- не атакуется обычными мобами;
- не толкается;
- не имеет обычного breeding/age;
- остаётся на земле;
- имеет небольшую hitbox;
- сохраняется при chunk save/load.

Lifetime:
- 6000 ticks = 5 минут после создания.

По истечении времени:
- carcass удаляется;
- выдаётся FALLBACK loot соответствующего вида.

Это нужно, чтобы игрок не потерял базовую добычу, если не разделал тушу.

## 4.5 Горящая смерть

Если животное было on fire в момент смерти:
- carcass получает `wasBurning=true`.

При butchering:
- мясные предметы заменяются на cooked equivalents, если cooked item существует.

При timeout fallback:
- мясо тоже cooked.

---

# 5. FALLBACK LOOT CARCASS

Если 5 минут прошли без ножа:

## Cow
- 2–4 minecraft:beef
- 0–2 minecraft:leather

## Pig
- 1–3 minecraft:porkchop

## Sheep
- 1–2 minecraft:mutton

Не выдавать дополнительную шерсть через carcass.
Shearing остаётся vanilla-механикой.

## Rabbit
- 1 minecraft:rabbit
- 50% шанс 1 minecraft:rabbit_hide

## Goose
- 1–2 slavicmyths:raw_goose
- 0–2 minecraft:feather

## Duck
- 1–2 slavicmyths:raw_duck
- 0–2 minecraft:feather

## Domestic Goat
- 1–3 slavicmyths:raw_goat

Если `wasBurning=true`:
- beef -> cooked_beef;
- porkchop -> cooked_porkchop;
- mutton -> cooked_mutton;
- rabbit -> cooked_rabbit;
- raw_goose -> cooked_goose;
- raw_duck -> cooked_duck;
- raw_goat -> cooked_goat.

---

# 6. ENHANCED BUTCHERING LOOT

Right click carcass разделочным ножом:

## Cow
- 4–6 beef;
- 1–3 leather;
- 1–2 animal_fat;
- 1–2 bone.

## Pig
- 4–6 porkchop;
- 2–4 animal_fat;
- 1–2 bone.

## Sheep
- 3–5 mutton;
- 1–2 animal_fat;
- 1–2 bone.

Не выдавать шерсть через разделку.

## Rabbit
- 1–2 rabbit;
- гарантированно 1 rabbit_hide;
- 50% шанс 1 bone;
- 25% шанс 1 animal_fat.

## Goose
- 2–4 raw_goose;
- 1–3 feather;
- 1 animal_fat;
- 50% шанс 1 bone.

## Duck
- 2–4 raw_duck;
- 1–3 feather;
- 50% шанс 1 animal_fat;
- 50% шанс 1 bone.

## Domestic Goat
- 3–5 raw_goat;
- 1 goat_hide;
- 1–2 animal_fat;
- 1–2 bone.

Looting:
- НЕ влияет на butchering loot.
- НЕ сохранять Looting level в carcass.
- система разделки сама является повышенной выгодой.

При `wasBurning=true`:
- meat -> cooked variant.

---

# 7. ЖИВОТНЫЙ ЖИР — `animal_fat`

RU:
`Животный жир`

EN:
`Animal Fat`

Creative tab:
`Ресурсы и материалы`

Stack:
64.

## Дизайн

32x32.

- небольшой неровный светло-кремовый кусок;
- оттенок слоновой кости/тёплого кремового;
- 1–2 более тёмных бежевых края;
- без реалистичной мясной детализации;
- силуэт не должен выглядеть как сыр или масло.

В 1.1.4 используется в:
- tallow candle.

Будущую кухонную переработку не делать сейчас.

---

# 8. КОЗЬЯ ШКУРА — `goat_hide`

RU:
`Козья шкура`

EN:
`Goat Hide`

Creative tab:
`Ресурсы и материалы`

Stack:
64.

## Дизайн

32x32.

- сложенная небольшая шкура;
- светло-коричнево-серо-кремовая;
- неровный внешний край;
- более тёмная изнанка;
- без крови;
- визуально отличается от vanilla rabbit hide.

В 1.1.4:
- это ресурс будущего ремесла;
- не вводить дубление;
- не конвертировать автоматически в leather;
- не создавать новую tanning system.

---

# 9. САЛЬНАЯ СВЕЧА — `tallow_candle`

RU:
`Сальная свеча`

EN:
`Tallow Candle`

Creative tab:
`Декор и блоки`

## 9.1 Дизайн

- короткая толстая свеча;
- тёплый кремово-жёлтый цвет;
- фитиль тёмный;
- поверхность чуть неровнее vanilla candle;
- маленький потёк воска/жира сбоку;
- не делать грязной/мрачной.

Block texture:
32x32.

## 9.2 Механика

Поведение максимально близко к vanilla candle:
- можно ставить 1–4 свечи в одном блоке;
- можно зажечь flint and steel/fire charge;
- можно потушить;
- поддержать waterlogged/стандартную water interaction, если vanilla candle её поддерживает;
- light level соответствует vanilla candle count logic;
- не делать особого дыма/дебаффов;
- не добавлять запах.

## 9.3 Рецепт

Shapeless:
- 1 `animal_fat`
- 1 `linen_thread`

Результат:
- 2 `tallow_candle`

Не добавлять цветные варианты.

---

# 10. ЦЕПОЧКА ЛЬНА

Уже существующий из 1.1.0:
`flax_stalk`

Новые ресурсы:

1. `flax_fiber`
2. `linen_thread`
3. `linen_cloth`

Цепочка:

`flax_stalk -> flax_breaker -> flax_fiber -> spinning_wheel -> linen_thread -> loom -> linen_cloth`

НЕ разрешать:
- прямой crafting flax_stalk -> linen_cloth;
- прямой crafting flax_stalk -> thread;
- обход рабочих блоков обычным верстаком.

---

# 11. ЛЬНЯНОЕ ВОЛОКНО — `flax_fiber`

RU:
`Льняное волокно`

EN:
`Flax Fiber`

Creative tab:
`Ресурсы и материалы`

Stack:
64.

## Дизайн

32x32.

- неровный пучок длинных светло-бежевых волокон;
- вытянутая вертикально-диагональная форма;
- отдельные тонкие концы;
- цвет сушёной светлой травы;
- не выглядит как wheat;
- не выглядит как string.

---

# 12. ЛЬНЯНАЯ НИТЬ — `linen_thread`

RU:
`Льняная нить`

EN:
`Linen Thread`

Creative tab:
`Ресурсы и материалы`

Stack:
64.

## Дизайн

32x32.

- небольшой аккуратный моток;
- светло-серо-бежевый;
- видны несколько намотанных витков;
- один короткий свободный конец;
- визуально толще vanilla string;
- не копировать string sprite.

---

# 13. ЛЬНЯНОЕ ПОЛОТНО — `linen_cloth`

RU:
`Льняное полотно`

EN:
`Linen Cloth`

Creative tab:
`Ресурсы и материалы`

Stack:
64.

## Дизайн

32x32.

- сложенный квадрат ткани;
- светло-кремово-серый;
- 2–3 видимых слоя сгиба;
- тонкая пиксельная структура переплетения;
- один край немного бахромчатый;
- без красной вышивки: базовое полотно нейтральное.

---

# 14. ЛЬНОМЯЛКА — `flax_breaker`

RU:
`Льномялка`

EN:
`Flax Breaker`

Creative tab:
`Декор и блоки`

## 14.1 Дизайн

Деревянный ручной станок.

Форма:
- низкое основание;
- две боковые деревянные опоры;
- длинная подвижная верхняя планка;
- рабочая щель посередине;
- при загруженном льне внутри лежит видимый сухой пучок;
- тёмные металлические гвозди/заклёпки допустимы;
- небольшая резьба на боковой части допустима, но не обязательна.

Размер:
- 1 block footprint;
- визуальная высота около 0.8 block;
- не full cube.

Facing:
- horizontal facing property.

## 14.2 Хранение

BlockEntity:
- input: до 4 flax_stalk;
- output не хранится отдельно после завершения: результат сразу выдаётся/drop рядом;
- progress 0..3.

## 14.3 Загрузка

Right click с flax_stalk:
- вставить 1 до max 4.

Sneak + right click с flax_stalk:
- вставить максимально возможное до 4.

Если уже 4:
- ничего не делать.

## 14.4 Работа

Мялка начинает ручной цикл только когда внутри ровно 4 flax_stalk.

Right click пустой рукой:
- progress +1;
- play wood creak/tool sound;
- короткая анимация/смена block model верхней планки.

После третьего рабочего нажатия:
- consume 4 flax_stalk;
- spawn/give 4 flax_fiber;
- progress -> 0;
- станок снова пуст.

Если внутри 1–3 stalk:
- пустой клик не начинает работу;
- можно показать actionbar:
  `Нужно 4 стебля льна`
  / `Requires 4 flax stalks`

Не делать GUI.

## 14.5 Визуальные состояния

BlockState:
- LOADED boolean;
- WORK_STEP 0..2;
- FACING.

Не пытаться хранить item count в blockstate, если это создаёт лишние модели.
Достаточно показать:
- empty;
- loaded;
- three handle positions.

---

# 15. РЕЦЕПТ ЛЬНОМЯЛКИ

Shaped:

`PPP`
` S `
`PPP`

Где:
- P = any planks tag
- S = stick

Результат:
1 flax_breaker.

---

# 16. ПРЯЛКА — `spinning_wheel`

RU:
`Прялка`

EN:
`Spinning Wheel`

Creative tab:
`Декор и блоки`

## 16.1 Дизайн

- деревянное основание;
- большая вертикальная круглая прялка/колесо;
- несколько спиц;
- тонкая стойка;
- веретено;
- маленький пучок волокна сбоку при загрузке;
- тёплое дерево;
- небольшой славянский красный орнамент на стойке допустим;
- форма не должна выглядеть как промышленная машина.

Размер:
- 1 block footprint;
- visual height около 1.4 block;
- collision можно упростить до устойчивого разумного shape.

FACING horizontal.

## 16.2 Inventory

BlockEntity:
- input flax_fiber capacity: 16;
- output linen_thread capacity: 16.

Нет GUI.

## 16.3 Interaction

Right click с flax_fiber:
- вставить 1.

Sneak + right click:
- вставить максимум до 16.

Right click пустой рукой:
- если output есть -> забрать весь output stack;
- если output пуст -> ничего.

## 16.4 Производство

Recipe:
- 2 flax_fiber -> 1 linen_thread.

Process time:
- 100 ticks = 5 секунд.

Автоматически запускается при:
- input >= 2;
- output имеет место.

ACTIVE blockstate:
- true во время процесса.

В active:
- wheel визуально/анимационно вращается, если это можно сделать существующим простым renderer/model animation способом;
- если сложный renderer потребует тяжёлой новой архитектуры, достаточно client-side model rotation animation без новой библиотеки.

Не добавлять fuel.
Не требовать redstone.

---

# 17. РЕЦЕПТ ПРЯЛКИ

Shaped:

` S `
`PSP`
`PPP`

Где:
- P = any planks
- S = stick

Результат:
1 spinning_wheel.

---

# 18. ТКАЦКИЙ СТАНОК — `loom_table`

RU:
`Ткацкий станок`

EN:
`Weaving Loom`

Не использовать registry id `loom`, чтобы не путать с vanilla loom.

Creative tab:
`Декор и блоки`

## 18.1 Дизайн

- две вертикальные деревянные стойки;
- верхняя и нижняя перекладины;
- хорошо видимые натянутые вертикальные нити;
- нижняя рабочая часть;
- при ACTIVE появляется частично вытканная светлая ткань;
- дерево тёпло-коричневое;
- конструкция ремесленная, а не фабричная.

Размер:
- 1 block footprint;
- visual height около 1.6 block.

FACING horizontal.

## 18.2 Inventory

BlockEntity:
- input linen_thread: max 32;
- output linen_cloth: max 16.

Нет GUI.

## 18.3 Interaction

Right click linen_thread:
- вставить 1.

Sneak + right click:
- вставить максимум.

Right click empty hand:
- забрать весь output.

## 18.4 Производство

Recipe:
- 4 linen_thread -> 1 linen_cloth.

Process time:
- 160 ticks = 8 секунд.

ACTIVE:
- показывает частично вытканный кусок ткани.

Без fuel.
Без redstone requirement.

---

# 19. РЕЦЕПТ ТКАЦКОГО СТАНКА

Shaped:

`PSP`
`S S`
`PPP`

Где:
- P = any planks
- S = stick

Результат:
1 loom_table.

---

# 20. ОДЕЖДА — ОБЩИЕ ПРАВИЛА

Все новые предметы:
- бытовая/народная одежда;
- НЕ полноценная броня;
- не создают combat power creep;
- не дают toughness;
- не дают knockback resistance;
- не дают attack damage;
- не имеют potion effects.

Stack:
1.

Durability:
- отсутствует;
- одежда не ломается от обычного получения урона.

Creative tab:
`Броня и одежда`

Equipment rendering:
- отдельные корректные clothing layers/models;
- не просто item sprite на теле;
- design основан на approved reference.

Все вещи:
- gender-neutral mechanically;
- любой player model может носить любую вещь;
- никаких ограничений «мужское/женское».

---

# 21. ЛЬНЯНАЯ РУБАХА — `linen_shirt`

RU:
`Льняная рубаха`

EN:
`Linen Shirt`

Slot:
CHEST.

Armor:
0.

## Дизайн

Item icon 32x32:
- светлая льняная рубаха;
- длинные рукава;
- V/разрез воротника;
- красная геометрическая вышивка вокруг воротника;
- узкая красная вышивка на манжетах;
- небольшая вышивка по нижнему краю;
- цвет ткани не чисто белый, а тёплый льняной.

На игроке:
- длинные светлые рукава;
- красный ворот;
- манжеты;
- свободный простой силуэт;
- не превращать в bulky armor.

## Рецепт

Shaped:

`C C`
`CRC`
` C `

C = linen_cloth
R = red dye tag

Результат:
1 linen_shirt.

---

# 22. ПОРТЫ — `linen_ports`

RU:
`Порты`

EN:
`Linen Trousers`

Slot:
LEGS.

Armor:
0.

## Дизайн

- свободные льняные штаны;
- серо-бежевый/льняной цвет;
- немного темнее рубахи;
- поясная завязка;
- штанины чуть сужаются книзу;
- без бронированных деталей.

## Рецепт

Shaped:

`CCC`
`C C`
`C C`

C = linen_cloth

Результат:
1 linen_ports.

---

# 23. САРАФАН — `sarafan`

RU:
`Сарафан`

EN:
`Sarafan`

Slot:
CHEST.

Armor:
0.

## Дизайн

Item icon:
- глубокий бордово-красный;
- широкая нижняя часть;
- вертикальные тёмно-красные панели;
- светло-красная/белая геометрическая кайма;
- светлая рубаха визуально видна под верхней частью в wearable model.

На игроке:
- torso часть бордовая;
- длинная skirt-like нижняя визуальная часть до ног;
- под ней белые рукава;
- орнамент по подолу;
- не ограничивать по gender.

Важно:
это один chest item.
Не требовать отдельной linen_shirt под ним.
Белая рубаха в его wearable design является частью модели.

## Рецепт

Shaped:

`CRC`
`CCC`
`C C`

C = linen_cloth
R = red dye

Результат:
1 sarafan.

---

# 24. ПЛАТОК — `linen_headscarf`

RU:
`Льняной платок`

EN:
`Linen Headscarf`

Slot:
HEAD.

Armor:
0.

## Дизайн

- светлый льняной платок;
- красная узорная кайма;
- закрывает верх/заднюю часть головы;
- сзади треугольный свисающий конец;
- лицо полностью открыто;
- орнамент не слишком мелкий;
- не выглядит как шлем.

## Рецепт

Shaped:

`CC `
`CR `
`   `

C = linen_cloth
R = red dye

Результат:
1 linen_headscarf.

---

# 25. РАБОЧИЙ ФАРТУК — `linen_apron`

RU:
`Льняной фартук`

EN:
`Linen Apron`

Slot:
CHEST.

Armor:
0.

## Дизайн

- светло-льняная передняя панель;
- широкая верхняя нагрудная часть;
- завязки по бокам;
- красная вышитая полоса в нижней части;
- тёмно-красные/коричневые тонкие ремешки;
- на спине видны завязки.

Он надевается как отдельный chest item.
Не создавать overlay slot.
Он может визуально лежать поверх обычного player skin.

## Рецепт

Shaped:

` C `
`CCC`
` R `

C = linen_cloth
R = red dye

Результат:
1 linen_apron.

---

# 26. ЖИЛЕТ — `folk_vest`

RU:
`Народный жилет`

EN:
`Folk Vest`

Slot:
CHEST.

Armor:
0.

## Дизайн

- безрукавный;
- тёмно-коричневый / бордово-коричневый;
- красная декоративная окантовка;
- открытая передняя часть;
- несколько вертикальных декоративных полос;
- без металлических пластин;
- бытовой, не броневой.

Wearable model:
- руки игрока остаются свободно видимыми;
- жилет поверх player skin.

## Рецепт

Shaped:

`B B`
`BRB`
`BBB`

B = brown dye + linen_cloth нельзя задавать двумя ингредиентами в одной клетке.

Поэтому реализовать как:
1. shapeless:
   - 3 linen_cloth
   - 1 brown dye
   - 1 red dye
=> 1 folk_vest

Именно shapeless.
Не создавать окрашенное промежуточное полотно.

---

# 27. ЛАПТИ — `bast_shoes`

RU:
`Лапти`

EN:
`Bast Shoes`

Slot:
FEET.

Armor:
0.

## Дизайн

- низкие плетёные туфли;
- тёплый светло-коричневый/соломенный цвет;
- заметный диагональный рисунок плетения;
- чуть выступающий нос;
- тонкие завязки около щиколотки;
- не выглядят как leather boots.

Материал в 1.1.4:
льняное волокно используется как игровое упрощение плетёного растительного материала.
Не добавлять отдельный bast/linden resource только ради лаптей.

## Рецепт

Shaped:

`F F`
`FTF`
`   `

F = flax_fiber
T = linen_thread

Результат:
1 bast_shoes.

---

# 28. ПОЯС-АКСЕССУАР — `woven_belt`

RU:
`Тканый пояс`

EN:
`Woven Belt`

Это НЕ armor item.

Это отдельный аксессуар типа:
`BELT`.

Creative tab:
`Броня и одежда`.

## 28.1 Дизайн

Item icon:
- длинная свернутая красно-бордовая тканая лента;
- светло-красный/бежевый геометрический узор;
- два свисающих конца;
- небольшие кисточки.

На игроке:
- тонкая полоса вокруг талии;
- основной цвет тёмно-красный;
- спереди небольшой узор;
- 1–2 коротких свисающих конца;
- не перекрывает весь torso.

Бонусов:
- нет.

Armor:
- нет.

Combat stats:
- нет.

Это косметический аксессуар и основа для будущих accessories.

---

# 29. ВСТРОЕННЫЙ ACCESSORY SLOT

Не добавлять внешнюю обязательную зависимость Curios/Accessories.

Создать минимальную внутреннюю систему:
- ровно один slot;
- slot type: BELT;
- в 1.1.4 принимает только items с tag:
  `slavicmyths:accessories/belts`.

В этот tag сейчас входит:
- woven_belt.

## 29.1 Хранение

Использовать NeoForge player attachment / другой текущий рекомендованный 1.21.1 persistent player-data механизм.

Хранить:
- один ItemStack.

Данные:
- сохраняются при save/load;
- синхронизируются server -> client для renderer.

## 29.2 Equip

Right click `woven_belt` в руке:

Если belt slot пуст:
- переместить 1 item из руки в accessory slot;
- play armor/equip cloth-like sound;
- показать actionbar:
  RU: `Пояс надет`
  EN: `Belt equipped`

Если slot занят:
- ничего не менять;
- actionbar:
  RU: `Слот пояса занят`
  EN: `Belt slot is occupied`

## 29.3 Unequip

Sneak + right click ПУСТОЙ рукой:

Если belt slot содержит item:
- переместить item в player inventory;
- если inventory полностью заполнен -> drop item у ног игрока;
- очистить accessory slot;
- play soft unequip sound.

Если slot пуст:
- ничего.

Не создавать отдельный большой GUI аксессуаров.
Не добавлять keybind.
Не модифицировать vanilla inventory screen через mixin только ради одного slot.

## 29.4 Death behavior

Если `keepInventory=true`:
- belt сохраняется.

Если `keepInventory=false`:
- belt item drop рядом с остальным инвентарём;
- slot очищается.

При respawn/clone:
- не дублировать item.

## 29.5 Rendering

Client player renderer:
- если belt slot содержит item с belt renderer:
  - отрисовать belt layer вокруг waist;
  - синхронно с body model;
  - работать с standard и slim player model.

Не рендерить belt поверх spectator/invisible player, если обычная render logic скрывает model.

Не создавать физику ткани.

---

# 30. РЕЦЕПТ ПОЯСА

Shapeless:
- 3 linen_thread
- 1 red dye

Результат:
1 woven_belt.

---

# 31. ФУНКЦИОНАЛ ОДЕЖДЫ

Все предметы одежды:
- косметические;
- не повышают armor;
- не дают speed;
- не дают resistance;
- не меняют hunger;
- не усиливают производство;
- не имеют set bonus в 1.1.4.

Их смысл:
- визуальная культурная идентичность;
- будущие NPC/бытовые системы;
- roleplay;
- подготовка системы аксессуаров.

НЕ придумывать бонусы.

---

# 32. ЛЬНЯНАЯ ПОСТЕЛЬ — `linen_bed`

RU:
`Льняная постель`

EN:
`Linen Bed`

Creative tab:
`Декор и блоки`

## 32.1 Дизайн

Полноценная кровать.

Основание:
- тёплое дерево;
- простые деревянные ножки.

Матрас:
- светло-кремовый.

Текстиль:
- светлая льняная простыня;
- подушка;
- тёмно-красное покрывало;
- на краю покрывала бело-красный геометрический орнамент.

Не копировать vanilla red bed texture.

Габарит:
- стандартные 2 блока как vanilla bed.

## 32.2 Поведение

Все vanilla bed функции сохраняются:
- сон;
- установка точки возрождения;
- невозможность нормального сна в запрещённых dimensions;
- стандартная bed explosion logic в dimensions, где bed нельзя использовать.

Главное отличие:
после УСПЕШНОГО полноценного сна игрок получает `well_rested`.

Не давать эффект если:
- игрок просто лёг и сразу встал;
- сон был прерван;
- игрок кликнул кровать днём и не спал;
- не произошёл корректный sleep completion.

## 32.3 Рецепт

Shaped:

`CCC`
`PPP`

C = linen_cloth
P = any planks

Результат:
1 linen_bed.

---

# 33. ЭФФЕКТ `well_rested`

RU:
`Хороший отдых`

EN:
`Well Rested`

Тип:
beneficial.

Duration после сна:
12000 ticks = 10 минут.

Amplifier:
0.

Не stack:
повторный успешный сон просто обновляет duration до 10 минут.

## 33.1 Эффекты

Целевое поведение:

- movement speed +5%;
- block breaking speed +10%.

Не давать:
- regeneration;
- resistance;
- damage;
- armor;
- luck;
- hunger restoration.

Если в 1.21.1 доступен стабильный attribute modifier для block break speed:
использовать его.

Если текущий официальный API версии не позволяет корректно модифицировать block break speed как attribute:
- реализовать эквивалент через стандартный mining speed hook только пока effect active;
- НЕ подменять это Haste I, если это даст заметно другой +20% баланс.

Целевой бонус должен оставаться примерно +10%.

## 33.2 Иконка

18x18/подходящий стандартный effect icon resource.

Дизайн:
- маленькая светлая подушка;
- над ней одна тёмно-красная декоративная линия/звёздочка;
- без текста.

---

# 34. CREATIVE TAB DISTRIBUTION

Использовать только систему вкладок 1.1.2.

## Resources & Materials
Добавить:
- animal_fat
- goat_hide
- flax_fiber
- linen_thread
- linen_cloth

## Tools & Weapons
Добавить:
- butchering_knife

## Armor & Clothing
Добавить:
- linen_shirt
- linen_ports
- sarafan
- linen_headscarf
- linen_apron
- folk_vest
- bast_shoes
- woven_belt

## Decor & Blocks
Добавить:
- flax_breaker
- spinning_wheel
- loom_table
- linen_bed
- tallow_candle

Carcass:
- не добавлять в Creative Tab как item.

Не возвращать старый общий dump tab.

---

# 35. TAGS

Item tags:

`slavicmyths:textile/flax_materials`
- flax_stalk
- flax_fiber
- linen_thread
- linen_cloth

`slavicmyths:textile/linen_clothing`
- linen_shirt
- linen_ports
- sarafan
- linen_headscarf
- linen_apron
- folk_vest
- bast_shoes

`slavicmyths:accessories/belts`
- woven_belt

`slavicmyths:butchering_knives`
- butchering_knife

Block tags:
- flax_breaker -> mineable/axe
- spinning_wheel -> mineable/axe
- loom_table -> mineable/axe
- linen_bed -> соответствующие bed tags
- tallow_candle -> candle-compatible tags, если нужны.

Не добавлять лишние публичные tags без необходимости.

---

# 36. РЕЦЕПТЫ — СВОДКА

Обязательные recipes:

Tools:
- butchering_knife.

Processing blocks:
- flax_breaker;
- spinning_wheel;
- loom_table.

Materials:
- flax_stalk НЕ крафтится в fiber вручную;
- fiber НЕ крафтится вручную в thread;
- thread НЕ крафтится вручную в cloth.

Decor:
- tallow_candle;
- linen_bed.

Clothing:
- linen_shirt;
- linen_ports;
- sarafan;
- linen_headscarf;
- linen_apron;
- folk_vest;
- bast_shoes;
- woven_belt.

## Butchering knife recipe

Shaped:

` I`
`S `

I = iron_ingot
S = stick

Результат:
1 butchering_knife.

Если recipe system требует 3x3:
использовать две диагональные клетки.
Не добавлять leather/gold/etc.

---

# 37. WORKSTATION SOUNDS / PARTICLES

Не создавать custom audio assets для станков в 1.1.4.

Использовать подходящие vanilla sounds:

Flax breaker:
- wood creak / wood hit.

Spinning wheel:
- мягкий wood movement sound при старте/завершении.

Loom:
- wool/loom-like click.

Butchering:
- subdued tool/shear-like sound;
- без gore sound.

Particles:
- flax breaker: 2–4 сухих растительных particles;
- spinning wheel: почти без particles;
- loom: без particles;
- carcass butchering: минимальные item/block particles, НИКАКИХ красных blood particles.

---

# 38. ЛОКАЛИЗАЦИЯ

Минимум:
- ru_ru
- en_us

RU:
- Разделочный нож
- Животный жир
- Козья шкура
- Сальная свеча
- Льняное волокно
- Льняная нить
- Льняное полотно
- Льномялка
- Прялка
- Ткацкий станок
- Льняная рубаха
- Порты
- Сарафан
- Льняной платок
- Льняной фартук
- Народный жилет
- Лапти
- Тканый пояс
- Льняная постель
- Хороший отдых
- Нужно 4 стебля льна
- Пояс надет
- Слот пояса занят

EN:
- Butchering Knife
- Animal Fat
- Goat Hide
- Tallow Candle
- Flax Fiber
- Linen Thread
- Linen Cloth
- Flax Breaker
- Spinning Wheel
- Weaving Loom
- Linen Shirt
- Linen Trousers
- Sarafan
- Linen Headscarf
- Linen Apron
- Folk Vest
- Bast Shoes
- Woven Belt
- Linen Bed
- Well Rested
- Requires 4 flax stalks
- Belt equipped
- Belt slot is occupied

Carcass entity:
RU display/internal fallback name:
`Туша`
EN:
`Carcass`

Не показывать variant name пользователю, если это не требуется.

---

# 39. ADVANCEMENT

ID:
`linen_craft`

Parent:
`minecraft:husbandry/root`
или ближайший подходящий существующий Slavic Myths farming advancement, если уже есть и логично использовать его.

Trigger:
получить `linen_cloth`.

RU:
Title: `От стебля до полотна`
Description: `Изготовь льняное полотно`

EN:
Title: `From Stalk to Cloth`
Description: `Craft a piece of linen cloth`

Frame:
task.

Icon:
linen_cloth.

Без XP reward.
Не hidden.

Не создавать ещё 5 achievements в этой версии.

---

# 40. СОХРАНЕНИЕ ДАННЫХ

## Carcass
Сохранять:
- variant;
- wasBurning;
- age/remaining lifetime.

## Flax breaker
Сохранять:
- input count;
- work progress.

## Spinning wheel
Сохранять:
- input;
- output;
- process progress.

## Loom
Сохранять:
- input;
- output;
- process progress.

## Belt accessory
Сохранять:
- ItemStack belt slot.

## Linen bed
Никаких специальных permanent player flags.
Эффект хранится обычной potion/effect системой.

Не сериализовать визуальные derived states, если их можно восстановить.

---

# 41. PERFORMANCE

Запрещено:
- каждый tick сканировать мир на carcass;
- global manager туш;
- global accessory manager;
- каждый tick сканировать всех игроков ради belts;
- сложные reflection hacks;
- постоянные inventory scans;
- большие scheduled world searches.

Правильно:
- carcass живёт как обычная entity и считает свой age;
- workstation tick только для своего BlockEntity;
- accessory renderer читает synced player attachment;
- well_rested использует обычный effect system;
- no global loops.

---

# 42. CARCASS EVENT INTEGRATION

Предпочитать NeoForge events/hooks.

Не использовать mixin, если можно:
- перехватить eligible death/loot через event;
- suppress стандартный drops для player-killed eligible adult;
- spawn carcass;
- XP оставить.

Если конкретный NeoForge 1.21.1 event не позволяет безопасно подавить loot без mixin:
- разрешён минимальный targeted mixin ТОЛЬКО к loot/drop path;
- не переписывать LivingEntity death logic целиком;
- документировать его одной строкой в финальном отчёте.

Цель поведения важнее внутреннего способа.

---

# 43. СОВМЕСТИМОСТЬ С 1.1.3

Не ломать:

Goose:
- breeding;
- defensive behavior;
- egg laying;
- nest;
- feeder;
- sounds.

Duck:
- breeding;
- swimming;
- egg laying;
- nest;
- feeder.

Domestic goat:
- breeding;
- milk;
- feeder.

Vanilla animal feed integration:
- cow;
- sheep;
- pig;
- chicken;
- rabbit.

Carcass добавляется поверх взрослого player-kill logic.

Chicken:
- НЕ получает carcass.
- остаётся со стандартным vanilla loot.

---

# 44. ВАЖНАЯ ЛОГИКА О ВАНИЛЬНЫХ ЖИВОТНЫХ

Не заменять:
- cow;
- pig;
- sheep;
- rabbit
новыми сущностями.

Не менять:
- их модель;
- базовый AI;
- breeding foods из vanilla;
- обычные milk/shearing mechanics;
- base stats.

1.1.4 меняет только player-killed adult drop flow:
- вместо обычного мгновенного meat/material drop появляется carcass;
- carcass можно разделать ножом;
- если не разделали, через 5 минут возвращается fallback loot.

Это продолжает переработку хозяйственных животных, начатую в 1.1.3, без полной замены vanilla mobs.

---

# 45. CLOTHING RENDERING

Не использовать один armor texture для всех вещей.

Нужно минимум отдельное отображение:

- linen_shirt;
- linen_ports;
- sarafan;
- linen_headscarf;
- linen_apron;
- folk_vest;
- bast_shoes;
- woven_belt accessory.

Разрешается:
- использовать совместимые player model layers;
- переиспользовать общий базовый clothing model class.

Нельзя:
- делать все предметы только обычными armor sprite без нормального wearable appearance;
- использовать один recolor для shirt/apron/vest/sarafan;
- превращать одежду в bulky vanilla armor geometry.

Slim player model:
- рукава/слои должны корректно работать.

---

# 46. ASSET QUALITY

Item sprites:
32x32.

Block textures:
32x32.

Clothing wearable textures:
64x64 или 128x128, если это нужно для аккуратного embroidery и текущая render system это поддерживает.

Carcass textures:
64x64 достаточно.

Правила:
- Minecraft-compatible pixel art;
- no blur;
- no anti-aliasing;
- no photo textures;
- no AI-generated texture pasted directly;
- референс используется как дизайн-направление, а не как готовый atlas.

Особенно:
- орнамент должен быть геометрическим и читаемым;
- не рисовать микротекст;
- красный узор не должен покрывать всю одежду.

---

# 47. ЧЕГО НЕТ В 1.1.4

НЕ добавлять:

- tannery;
- tanning rack;
- leather tanning;
- cheese;
- butter;
- sour cream;
- yogurt;
- flour;
- kitchen expansion;
- alcohol;
- storage overhaul;
- bags;
- barrels;
- drying racks;
- new livestock;
- chicken carcass;
- fish carcass;
- hostile mob carcasses;
- boss carcasses;
- combat set bonuses;
- armor stats clothing;
- new accessory types besides BELT;
- accessories GUI;
- keybind for accessories;
- external accessories dependency;
- custom workstation GUI;
- dyeable clothing variants;
- seasons;
- temperature;
- clothing warmth;
- gender restrictions;
- NPC outfits.

---

# 48. ОБНОВЛЕНИЕ ВЕРСИИ

Обновить version source of truth до:
`1.1.4`

Не создавать несколько разных version constants.

---

# 49. DATA / RESOURCE FILES

Создать все необходимые:

- item registrations;
- block registrations;
- carcass entity registration;
- block entities;
- MobEffect registration;
- player attachment для belt;
- recipes;
- blockstates;
- block models;
- item models;
- textures;
- loot/data integration;
- tags;
- lang;
- advancement;
- renderer layers;
- sound usage mappings при необходимости.

Не создавать runtime-generated textures.

---

# 50. BUILD

После реализации:

1. НЕ запускать Minecraft.
2. НЕ запускать PolyMC.
3. Выполнить одну финальную подходящую compile/build command.
4. Если success — закончить.
5. Если fail из-за изменений 1.1.4 — исправить конкретную ошибку.
6. После такого исправления допускается один повторный build.
7. Если fail связан с заранее существующей независимой ошибкой проекта:
   - не ремонтировать весь проект;
   - указать точную ошибку в финальном отчёте.

---

# 51. GIT FINISH

Проверить:
- branch = main;
- изменения относятся к 1.1.4;
- чужие изменения не удалены.

Commit:
`feat: implement Slavic Myths 1.1.4 textile and processing`

Если origin auth уже работает:
- push origin main.

НЕ:
- release branch;
- tag;
- GitHub Release;
- PolyMC copy;
- force push.

---

# 52. КРИТЕРИИ ГОТОВНОСТИ

## Butchering
[ ] butchering_knife существует.
[ ] carcass entity один reusable type.
[ ] 7 carcass variants.
[ ] carcass только у adult player-killed animals.
[ ] babies не дают carcass.
[ ] chicken не даёт carcass.
[ ] standard player-kill loot не дублируется.
[ ] XP сохраняется.
[ ] carcass lifetime 5 min.
[ ] timeout fallback loot работает.
[ ] burning state сохраняется.
[ ] butchering knife выдаёт enhanced loot.
[ ] knife теряет durability.
[ ] Looting не влияет на butchering output.
[ ] никакого gore.

## Materials
[ ] animal_fat.
[ ] goat_hide.
[ ] flax_fiber.
[ ] linen_thread.
[ ] linen_cloth.

## Tallow candle
[ ] craft работает.
[ ] ставится как candle.
[ ] поддерживает 1–4 candles.
[ ] можно зажечь/потушить.
[ ] свет соответствует vanilla candle logic.

## Flax breaker
[ ] 4-stalk capacity.
[ ] 3 ручных work actions.
[ ] 4 stalk -> 4 fiber.
[ ] нет crafting bypass.
[ ] сохранение progress/input.
[ ] no GUI.

## Spinning wheel
[ ] input/output.
[ ] 2 fiber -> 1 thread.
[ ] 100 ticks.
[ ] ACTIVE state.
[ ] no GUI.
[ ] save/load works.

## Loom
[ ] input/output.
[ ] 4 thread -> 1 cloth.
[ ] 160 ticks.
[ ] ACTIVE state.
[ ] no GUI.
[ ] save/load works.

## Clothing
[ ] linen_shirt.
[ ] linen_ports.
[ ] sarafan.
[ ] linen_headscarf.
[ ] linen_apron.
[ ] folk_vest.
[ ] bast_shoes.
[ ] wearable visuals match design direction.
[ ] zero armor.
[ ] no combat bonuses.
[ ] gender-neutral mechanics.

## Belt accessory
[ ] woven_belt.
[ ] separate BELT accessory slot.
[ ] belt is NOT armor.
[ ] right-click equips.
[ ] occupied slot prevents replacement.
[ ] sneak+empty-hand unequips.
[ ] keepInventory behavior correct.
[ ] death drop behavior correct.
[ ] save/load correct.
[ ] server-client sync.
[ ] waist renderer works standard/slim.
[ ] no external accessories dependency.
[ ] no accessories GUI.

## Linen bed
[ ] standard bed behavior.
[ ] successful sleep applies well_rested.
[ ] interrupted/no sleep gives no buff.
[ ] spawn point still works.
[ ] dimension bed behavior preserved.

## Well Rested
[ ] 10 minute duration.
[ ] +5% movement.
[ ] +10% block break speed.
[ ] no combat/regen/resistance bonuses.
[ ] refreshes, not stacks.

## Creative Tabs
[ ] resources in Resources & Materials.
[ ] knife in Tools & Weapons.
[ ] clothes/belt in Armor & Clothing.
[ ] workstations/bed/candle in Decor & Blocks.
[ ] no old dump tab use.

## Data
[ ] ru_ru.
[ ] en_us.
[ ] recipes.
[ ] tags.
[ ] advancement.
[ ] models.
[ ] textures.
[ ] persistence.

## Workflow
[ ] tokens used economically.
[ ] no full project audit.
[ ] no Minecraft launch.
[ ] no PolyMC operation.
[ ] version = 1.1.4.
[ ] branch = main.
[ ] no release/tag.
[ ] final build only as required.

---

# 53. MANUAL TEST CHECKLIST ДЛЯ ПОЛЬЗОВАТЕЛЯ

Codex НЕ выполняет игровые тесты.

В финальном отчёте дать короткий checklist:

1. Убить взрослую cow игроком — проверить carcass вместо обычного immediate loot.
2. Разделать cow carcass ножом.
3. Оставить carcass на 5 минут и проверить fallback.
4. Убить burning animal и проверить cooked meat.
5. Проверить pig/sheep/rabbit/goose/duck/domestic goat carcasses.
6. Проверить, что baby carcass не создаётся.
7. Проверить flax breaker 4 stalk / 3 actions.
8. Проверить spinning wheel.
9. Проверить loom.
10. Надеть каждый clothing item.
11. Проверить standard и slim player rendering.
12. Right-click woven belt -> equip.
13. Sneak + empty hand -> unequip.
14. Проверить belt death/keepInventory.
15. Поспать в linen bed полностью.
16. Проверить Well Rested 10 min.
17. Проверить движение и mining bonus.
18. Проверить tallow candle 1–4 / ignition.
19. Проверить creative tabs.
20. Проверить ru/en localization и advancement.

---

# 54. ФОРМАТ ИТОГОВОГО ОТЧЁТА CODEX

Без длинного анализа.

## Implemented
5–12 коротких bullets.

## Build
- command;
- success/fail;
- точная причина только при fail.

## Git
- branch;
- commit hash;
- push status.

## Manual test checklist
Короткий список из раздела выше.

Если использован targeted mixin для carcass loot suppression:
добавить одну строку:
`Technical note: targeted mixin used for ...`

НЕ писать:
- chain-of-thought;
- историю архитектурных решений;
- предложения новых фич;
- общий аудит;
- идеи для 1.1.5.
